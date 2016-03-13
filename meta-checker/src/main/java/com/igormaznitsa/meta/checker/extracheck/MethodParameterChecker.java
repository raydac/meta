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
package com.igormaznitsa.meta.checker.extracheck;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.generic.Type;

import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.checker.Context;
import com.igormaznitsa.meta.checker.Utils;

public class MethodParameterChecker {

  private static final Class<?>[] CLASSES_WHERE_POSSIBLE_NULL = new Class<?>[]{java.util.List.class, java.util.Vector.class, java.util.Queue.class};

  private static final Set<String> CACHED_CLASS_NAMES_POSITIVE_RECOGNIZED = new HashSet<String>();
  private static final Set<String> CACHED_CLASS_NAMES_NEGATIVE_RECOGNIZED = new HashSet<String>();

  private static final Set<String> ANNOTATIONS_FOR_OBJECT = classesToNameSet(javax.annotation.Nullable.class, javax.annotation.Nonnull.class);
  private static final Set<String> ANNOTATIONS_FOR_ARRAY_OR_COLLECTION = classesToNameSet(MayContainNull.class, MustNotContainNull.class);

  public static void checkReturnType(final Context context, final Method method) {
    final Type returnType = method.getReturnType();
    if (isNullableType(returnType)) {
      final AnnotationEntry[] annotations = method.getAnnotationEntries();
      if (!hasAnnotationFromSet(annotations, ANNOTATIONS_FOR_OBJECT)) {
        context.error("Must have either @" + Nullable.class.getName() + " or @" + Nonnull.class.getName() + " for return type", true);
      }
      if (isArrayOfObjectsOrCollection(context, returnType) && !hasAnnotationFromSet(annotations, ANNOTATIONS_FOR_ARRAY_OR_COLLECTION)) {
        context.error("Must have either @" + MayContainNull.class.getName() + " or @" + MustNotContainNull.class.getName() + " for return type", true);
      }
    }
  }

  public static void checkParamsType(final Context context, final Method method) {
    final Type[] arguments = method.getArgumentTypes();
    final ParameterAnnotationEntry[] argAnnotations = method.getParameterAnnotationEntries();

    for (int i = 0; i < arguments.length; i++) {
      if (isNullableType(arguments[i])) {
        if (i >= argAnnotations.length || !hasAnnotationFromSet(argAnnotations[i].getAnnotationEntries(), ANNOTATIONS_FOR_OBJECT)) {
          context.error("Must have either @" + Nullable.class.getName() + " or @" + Nonnull.class.getName() + " for argument #" + (i + 1), true);
        }

        if (isArrayOfObjectsOrCollection(context, arguments[i])) {
          if (i >= argAnnotations.length || !hasAnnotationFromSet(argAnnotations[i].getAnnotationEntries(), ANNOTATIONS_FOR_ARRAY_OR_COLLECTION)) {
            context.error("Must have either @" + MayContainNull.class.getName() + " or @" + MustNotContainNull.class.getName() + " for argument #" + (i + 1), true);
          }
        }
      }
    }
  }

  private static Set<String> classesToNameSet(final Class<?>... klazzes) {
    final Set<String> result = new HashSet<String>();
    for (final Class<?> k : klazzes) {
      result.add(Utils.makeSignatureForClass(k));
    }
    return result;
  }

  private static boolean isNullableType(final Type type) {
    final String signature = type.getSignature();
    return signature.length() > 0 && (signature.charAt(0) == '[' || signature.charAt(0) == 'L');
  }

  private static boolean isArrayOfObjectsOrCollection(final Context context, final Type type) {
    final String signature = type.getSignature();

    final int arrayLastChar = signature.lastIndexOf('[');

    if (arrayLastChar > 0) {
      return true;
    }

    if (!signature.endsWith(";")) return false;
    
    if (CACHED_CLASS_NAMES_POSITIVE_RECOGNIZED.contains(signature)) {
      return true;
    }
    if (CACHED_CLASS_NAMES_NEGATIVE_RECOGNIZED.contains(signature)) {
      return false;
    }

    return investigateClassThatCanContainNull(context, signature);
  }

  private static boolean investigateClassThatCanContainNull(final Context context, final String classTypeInInsideFormat) {
    final String klazzName = Utils.classNameToNormalView(classTypeInInsideFormat);
    final File classFile = new File(context.getTargetDirectoryFolder(), klazzName.replace('.', '/') + ".class");
    if (classFile.isFile()) {
      try {
        final JavaClass jclazz = new ClassParser(classFile.getAbsolutePath()).parse();

        for (final String interfaceName : jclazz.getInterfaceNames()) {
          if (investigateClassThatCanContainNull(context, Utils.makeSignatureForClass(interfaceName))) {
            CACHED_CLASS_NAMES_POSITIVE_RECOGNIZED.add(classTypeInInsideFormat);
            return true;
          }
        }

        final String superclassName = jclazz.getSuperclassName();
        if (superclassName != null && !superclassName.equals("java.lang.Object")) {
          final String superClassInInsideFormat = Utils.makeSignatureForClass(superclassName);
          if (investigateClassThatCanContainNull(context, superClassInInsideFormat)) {
            CACHED_CLASS_NAMES_POSITIVE_RECOGNIZED.add(classTypeInInsideFormat);
            return true;
          }
        }
      }
      catch (Exception ex) {
        context.warning("Can't parse class file " + classFile.getAbsolutePath(), false);
      }
    }
    else {
      // it is possible that it is standard class
      final String normalClassName = klazzName.replace('$', '.');
      try {
        final Class<?> klazz = Class.forName(normalClassName);
        for (final Class<?> klazzWherePossibleNull : CLASSES_WHERE_POSSIBLE_NULL) {
          if (klazzWherePossibleNull.isAssignableFrom(klazz)) {
            CACHED_CLASS_NAMES_POSITIVE_RECOGNIZED.add(classTypeInInsideFormat);
            return true;
          }
        }
      }
      catch (ClassNotFoundException ex) {
      }
    }
    CACHED_CLASS_NAMES_NEGATIVE_RECOGNIZED.add(classTypeInInsideFormat);
    return false;
  }

  private static boolean hasAnnotationFromSet(final AnnotationEntry[] annotations, final Set<String> namesToFind) {
    if (annotations != null) {
      for (final AnnotationEntry a : annotations) {
        if (namesToFind.contains(a.getAnnotationType())) {
          return true;
        }
      }
    }
    return false;
  }

}
