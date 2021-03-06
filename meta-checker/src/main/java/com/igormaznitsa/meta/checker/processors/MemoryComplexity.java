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

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.ParameterAnnotationEntry;

public class MemoryComplexity extends AbstractMetaAnnotationProcessor {

  @Override
  protected void doProcessing (final Context context, final JavaClass clazz, final ElementType type, final ParameterAnnotationEntry pae, final AnnotationEntry ae) {
    final ElementValue element = extractValue("value", ae);
    if (element == null) {
      context.error("Can't find value int " + getAnnotationClass().getCanonicalName() + " annotation", true);
    }
    else {
      try {
        final com.igormaznitsa.meta.Complexity unit = com.igormaznitsa.meta.Complexity.valueOf(element.stringifyValue());
        final com.igormaznitsa.meta.Complexity maxAllowed = context.getMaxAllowedMemoryComplexity();
        if (maxAllowed!=null){
          if (unit.ordinal()>maxAllowed.ordinal()) {
            context.error("Detected violation for memory complexity : "+unit.name(), true);
          }
        }
      }
      catch (Exception ex) {
        context.error("Can't get information about complexity from annotation " + getAnnotationClass().getCanonicalName() + " [" + element.stringifyValue() + "]", true);
      }
    }
  }

  @Override
  public Class<? extends Annotation> getAnnotationClass () {
    return com.igormaznitsa.meta.annotation.MemoryComplexity.class;
  }
  
}
