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
import java.util.List;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.ParameterAnnotationEntry;

public class ThrowsRuntimeException extends AbstractMetaAnnotationProcessor {

  @Override
  protected int doProcessing(final Context context, final JavaClass javaClass,
                             final ElementType type,
                             final ParameterAnnotationEntry parameterAnnotationEntry,
                             final AnnotationEntry annotationEntry) {
    final List<AnnotationEntry> annotations =
        extractsAnnotationsIfRepeatable(context, annotationEntry,
            com.igormaznitsa.meta.annotation.ThrowsRuntimeExceptions.class);

    for (final AnnotationEntry entry : annotations) {
      final ElementValue value = extractValue("value", entry);
      if (value == null) {
        context.error("detected @" +
            com.igormaznitsa.meta.annotation.ThrowsRuntimeExceptions.class.getSimpleName() +
            " without value", true);
        break;
      }
    }

    return annotations.size();
  }

  @Override
  public List<Class<? extends Annotation>> getProcessedAnnotationClasses() {
    return List.of(com.igormaznitsa.meta.annotation.ThrowsRuntimeException.class,
        com.igormaznitsa.meta.annotation.ThrowsRuntimeExceptions.class);
  }

}
