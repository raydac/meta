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
import java.util.Locale;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;

public final class Constraint extends AbstractMetaAnnotationProcessor {

  private final JexlEngine JEXL = new JexlEngine();
  
  public Constraint(){
    super();
  }
  
  @Override
  protected void doProcessing (final Context context, final JavaClass clazz, final ElementType type, final ParameterAnnotationEntry pae, final AnnotationEntry ae) {
    final String text = extractStrValue("value", ae, "");
    try{
      final Expression expression = JEXL.createExpression(text);
      final String dumped = expression.dump().toUpperCase(Locale.ENGLISH);
      if (!(dumped.contains("X ") || dumped.contains(" X ") || dumped.contains(" X;")))
        context.error(String.format("Can't detect 'X' at constraint expression '%s' at %s", text, context.nodeToString(clazz)));
    }catch(Throwable thr){
      context.error(String.format("Wrong constraint expression '%s' at %s", text, context.nodeToString(clazz)));
    }
  }

  @Override
  public Class<? extends Annotation> getAnnotationClass () {
    return com.igormaznitsa.meta.annotation.Constraint.class;
  }
  
}
