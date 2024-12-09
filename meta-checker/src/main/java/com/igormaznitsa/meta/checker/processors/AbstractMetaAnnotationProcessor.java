/*
 * Copyright 2016 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.meta.checker.processors;

import static com.igormaznitsa.meta.checker.Utils.classNameToCanonical;

import com.igormaznitsa.meta.checker.Context;
import com.igormaznitsa.meta.checker.Utils;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.bcel.classfile.AnnotationElementValue;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ArrayElementValue;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;

public abstract class AbstractMetaAnnotationProcessor {

  private final Set<ElementType> allowedElements = EnumSet.noneOf(ElementType.class);
  private final Set<String> annotationTypes;
  private final boolean containsAlert;

  public AbstractMetaAnnotationProcessor() {
    super();
    final List<Class<? extends Annotation>> annotationClasses = getProcessedAnnotationClasses();
    this.annotationTypes = annotationClasses.stream().map(Utils::makeSignatureForClass).collect(
        Collectors.toSet());
    annotationClasses.forEach(x -> {
      final Target annotation = x.getAnnotation(Target.class);
      if (annotation != null) {
        this.allowedElements.addAll(List.of(annotation.value()));
      }
    });

    this.containsAlert = annotationClasses.stream().anyMatch(x -> {
      try {
        final java.lang.reflect.Method method = x.getMethod("alert");
        if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
          return true;
        }
      } catch (NoSuchMethodException | SecurityException ex) {
        if (ex instanceof SecurityException) {
          throw new Error("Unexpected security error", ex);
        }
      }
      return false;
    });
  }

  protected static List<AnnotationEntry> extractsAnnotationsIfRepeatable(
      final Context context,
      final AnnotationEntry annotationEntry,
      final Class<? extends Annotation> repeatable
  ) {
    final List<AnnotationEntry> entries;

    final String canonicalAnnotationEntryClassName =
        classNameToCanonical(annotationEntry.getAnnotationType());

    if (canonicalAnnotationEntryClassName.equals(repeatable.getCanonicalName())) {
      final ElementValue elementValue = extractValue("value", annotationEntry);
      if (elementValue instanceof ArrayElementValue) {
        entries = Arrays.stream(((ArrayElementValue) elementValue).getElementValuesArray())
            .filter(x -> x instanceof AnnotationElementValue)
            .map(x -> ((AnnotationElementValue) x).getAnnotationEntry())
            .collect(Collectors.toList());
      } else {
        context.error(
            "Detected annotation " + canonicalAnnotationEntryClassName + " without value", true);
        entries = List.of();
      }
    } else {
      entries = List.of(annotationEntry);
    }
    return entries;
  }

  protected static String extractStringValue(
      final String field,
      final AnnotationEntry entry,
      final String defaultValue
  ) {
    final ElementValue value = extractValue(field, entry);
    return value == null ? defaultValue : value.stringifyValue();
  }

  protected static Map<String, ElementValue> parseValues(final AnnotationEntry entry) {
    final Map<String, ElementValue> result = new HashMap<>();
    for (final ElementValuePair p : entry.getElementValuePairs()) {
      result.put(p.getNameString(), p.getValue());
    }
    return result;
  }

  protected static ElementValue extractValue(final String field, final AnnotationEntry entry) {
    ElementValue result = null;
    for (final ElementValuePair p : entry.getElementValuePairs()) {
      if (field.equals(p.getNameString())) {
        result = p.getValue();
        break;
      }
    }
    return result;
  }

  protected static String addSemicolonIfNeeded(final String text) {
    return text == null ? null : text.isEmpty() ? text : " : " + text;
  }

  public boolean hasAlert() {
    return this.containsAlert;
  }

  public final boolean isElementTypeAllowed(final ElementType type) {
    return this.allowedElements.contains(type);
  }

  public final int processClass(final Context context, final JavaClass clazz,
                                final int itemIndex) {
    context.setProcessingItem(clazz, null, itemIndex);

    int processed = 0;

    if (isElementTypeAllowed(ElementType.TYPE)) {
      processed += processType(context, clazz, clazz.getAnnotationEntries());
    }

    context.setProcessingItem(clazz, null, itemIndex);
    if (isElementTypeAllowed(ElementType.FIELD)) {
      processed += processFields(context, clazz, clazz.getFields());
    }

    context.setProcessingItem(clazz, null, itemIndex);
    if (isElementTypeAllowed(ElementType.METHOD) || isElementTypeAllowed(ElementType.PARAMETER)) {
      processed += processMethods(context, clazz, clazz.getMethods());
    }
    context.setProcessingItem(clazz, null, itemIndex);

    return processed;
  }

  private int processType(final Context context, final JavaClass clazz,
                          final AnnotationEntry[] annotations) {
    int processed = 0;
    for (final AnnotationEntry entry : annotations) {
      if (annotationTypes.contains(entry.getAnnotationType())) {
        processed += process(context, clazz, ElementType.TYPE, null, entry);
      }
    }
    return processed;
  }

  private int processFields(final Context context, final JavaClass clazz, final Field[] fields) {
    int processed = 0;
    int index = 0;
    for (final Field f : fields) {
      context.setProcessingItem(clazz, f, index++);
      for (final AnnotationEntry ae : f.getAnnotationEntries()) {
        if (annotationTypes.contains(ae.getAnnotationType())) {
          processed += process(context, clazz, ElementType.FIELD, null, ae);
        }
      }
    }
    return processed;
  }

  private int process(final Context context, final JavaClass clazz, final ElementType type,
                      final ParameterAnnotationEntry pae, final AnnotationEntry ae) {
    return this.doProcessing(context, clazz, type, pae, ae);
  }

  private int processMethods(final Context context, final JavaClass clazz,
                             final Method[] methods) {
    int processed = 0;
    int methodIndex = 0;
    for (final Method m : methods) {
      final int argNumber = m.getArgumentTypes().length;

      if (isElementTypeAllowed(ElementType.METHOD)) {
        for (final AnnotationEntry ae : m.getAnnotationEntries()) {
          context.setProcessingItem(clazz, m, methodIndex);
          if (annotationTypes.contains(ae.getAnnotationType())) {
            processed += process(context, clazz, ElementType.METHOD, null, ae);
          }
        }
      }
      if (isElementTypeAllowed(ElementType.PARAMETER)) {
        int paramIndex = 0;
        for (final ParameterAnnotationEntry pe : m.getParameterAnnotationEntries()) {
          context.setProcessingItem(clazz, m, (paramIndex++) % argNumber);
          for (final AnnotationEntry ae : pe.getAnnotationEntries()) {
            if (annotationTypes.contains(ae.getAnnotationType())) {
              processed += process(context, clazz, ElementType.PARAMETER, pe, ae);
            }
          }
        }
      }
      methodIndex++;
    }
    return processed;
  }

  protected abstract int doProcessing(
      Context context,
      JavaClass javaClass,
      ElementType type,
      ParameterAnnotationEntry parameterAnnotationEntry,
      AnnotationEntry annotationEntry
  );

  public abstract List<Class<? extends Annotation>> getProcessedAnnotationClasses();
}
