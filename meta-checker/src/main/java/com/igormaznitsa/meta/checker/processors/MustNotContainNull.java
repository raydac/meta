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

import static com.igormaznitsa.meta.checker.Utils.isObjectType;

import com.igormaznitsa.meta.checker.Context;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;

public class MustNotContainNull extends AbstractMetaAnnotationProcessor {

  @Override
  protected void doProcessing(final Context context, final JavaClass clazz, final ElementType type, final ParameterAnnotationEntry pae, final AnnotationEntry ae) {

    boolean error = false;

    String name = "";

    switch (type) {
      case FIELD: {
        final Field method = (Field) context.getNode();
        error = !isObjectType(method.getType());
        name = "a non-object field";
        break;
      }
      case PARAMETER: {
        final Method method = (Method) context.getNode();
        error = !isObjectType(method.getArgumentTypes()[context.getItemIndex()]);
        name = "a non-object parameter";
        break;
      }
      case METHOD: {
        final Method method = (Method) context.getNode();
        error = !isObjectType(method.getReturnType());
        name = "the non-object result";
        break;
      }
      default:
        throw new Error("Unexpected item type [" + type + ']');
    }

    if (error) {
      context.error(name + " is marked by @" + getAnnotationClass().getName(), true);
    }

  }

  @Override
  public Class<? extends Annotation> getAnnotationClass() {
    return com.igormaznitsa.meta.annotation.MustNotContainNull.class;
  }

}
