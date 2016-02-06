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
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.ParameterAnnotationEntry;

public class Warning extends AbstractMetaAnnotationProcessor {

  @Override
  protected void doProcessing (final Context context, final JavaClass clazz, final ElementType type, final ParameterAnnotationEntry pae, final AnnotationEntry ae) {
    final String text = extractStrValue("value", ae, "");
    context.warning(text + " at " + context.nodeToString(clazz));
  }

  @Override
  public Class<? extends Annotation> getAnnotationClass () {
    return com.igormaznitsa.meta.annotation.Warning.class;
  }
  
}
