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

import com.igormaznitsa.meta.checker.Context;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;

import com.igormaznitsa.meta.checker.Utils;

public abstract class AbstractMetaAnnotationProcessor {

  private final Set<ElementType> ALLOWED_ELEMENTS = EnumSet.noneOf(ElementType.class);
  private final String ANNOTATION_TYPE;
  private final boolean alertable;

  public AbstractMetaAnnotationProcessor () {
    super();
    final Class<? extends Annotation> theKlazz = getAnnotationClass();
    this.ANNOTATION_TYPE = Utils.makeSignatureForClass(theKlazz);
    for (final ElementType t : theKlazz.getAnnotation(Target.class).value()) {
      ALLOWED_ELEMENTS.add(t);
    }

    boolean hasAlertField = false;
    try {
      final java.lang.reflect.Method alert = theKlazz.getMethod("alert");
      hasAlertField = alert.getReturnType() == boolean.class;
    }
    catch (NoSuchMethodException ex) {
    }
    catch (SecurityException ex) {
      throw new Error("Unexpected security error", ex);
    }
    this.alertable = hasAlertField;
  }

  public boolean isAlertable () {
    return this.alertable;
  }

  public final boolean isElementTypeAllowed (final ElementType type) {
    return this.ALLOWED_ELEMENTS.contains(type);
  }

  public final void processClass (final Context context, final JavaClass clazz, final int itemIndex) {
    context.setProcessingItem(clazz, null, itemIndex);

    if (isElementTypeAllowed(ElementType.TYPE)) {
      processType(context, clazz, clazz.getAnnotationEntries());
    }

    context.setProcessingItem(clazz, null, itemIndex);
    if (isElementTypeAllowed(ElementType.FIELD)) {
      processFields(context, clazz, clazz.getFields());
    }

    context.setProcessingItem(clazz, null, itemIndex);
    if (isElementTypeAllowed(ElementType.METHOD) || isElementTypeAllowed(ElementType.PARAMETER)) {
      processMethods(context, clazz, clazz.getMethods());
    }

    context.setProcessingItem(clazz, null, itemIndex);
  }

  private void processType (final Context context, final JavaClass clazz, final AnnotationEntry[] annotations) {
    for (final AnnotationEntry ae : annotations) {
      if (ANNOTATION_TYPE.equals(ae.getAnnotationType())) {
        process(context, clazz, ElementType.TYPE, null, ae);
      }
    }
  }

  private void processFields (final Context context, final JavaClass clazz, final Field[] fields) {
    int index = 0;
    for (final Field f : fields) {
      context.setProcessingItem(clazz, f, index++);
      for (final AnnotationEntry ae : f.getAnnotationEntries()) {
        if (ANNOTATION_TYPE.equals(ae.getAnnotationType())) {
          process(context, clazz, ElementType.FIELD, null, ae);
        }
      }
    }
  }

  private void processMethods (final Context context, final JavaClass clazz, final Method[] methods) {
    int method = 0;
    for (final Method m : methods) {
      context.setProcessingItem(clazz, m, method++);
      if (isElementTypeAllowed(ElementType.METHOD)) {
        for (final AnnotationEntry ae : m.getAnnotationEntries()) {
          if (ANNOTATION_TYPE.equals(ae.getAnnotationType())) {
            process(context, clazz, ElementType.METHOD, null, ae);
          }
        }
      }
      if (isElementTypeAllowed(ElementType.PARAMETER)) {
        int param = 0;
        for (final ParameterAnnotationEntry pe : m.getParameterAnnotationEntries()) {
          context.setProcessingItem(clazz, m, param++);
          for (final AnnotationEntry ae : pe.getAnnotationEntries()) {
            if (ANNOTATION_TYPE.equals(ae.getAnnotationType())) {
              process(context, clazz, ElementType.PARAMETER, pe, ae);
            }
          }
        }
      }
    }
  }

  protected static String extractStrValue(final String field, final AnnotationEntry entry, final String dflt) {
    final ElementValue value = extractValue(field, entry);
    return value == null ? dflt : value.stringifyValue();
  }
  
//  protected static boolean extractBoolValue(final String field, final AnnotationEntry entry, final boolean dflt) {
//    final SimpleElementValue value = (SimpleElementValue)extractValue(field, entry);
//    return value == null ? dflt : value.getValueBoolean();
//  }
  
  protected static ElementValue extractValue(final String field, final AnnotationEntry entry) {
    ElementValue result = null;
    for (final ElementValuePair p : entry.getElementValuePairs()) {
      if (field.equals(p.getNameString())){
        result = p.getValue();
        break;
      }
    }
    return result;
  }
  
  protected static Map<String, ElementValue> parseValues (final AnnotationEntry entry) {
    final Map<String, ElementValue> result = new HashMap<String, ElementValue>();
    for (final ElementValuePair p : entry.getElementValuePairs()) {
      result.put(p.getNameString(), p.getValue());
    }
    return result;
  }

  private void process (final Context context, final JavaClass clazz, final ElementType type, final ParameterAnnotationEntry pae, final AnnotationEntry ae) {
    doProcessing(context, clazz, type, pae, ae);
  }

  protected static String addSemicolonIfNeeded(final String text) {
    return text == null ? text : text.isEmpty() ? text : " : "+text;
  }
  
  protected abstract void doProcessing (Context context, JavaClass clazz, ElementType type, ParameterAnnotationEntry pae, AnnotationEntry ae);

  public abstract Class<? extends Annotation> getAnnotationClass ();
}
