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

import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.checker.Context;
import com.igormaznitsa.meta.checker.Utils;
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

public final class MethodParameterChecker {

  private static final Class<?>[] CLASSES_WHERE_POSSIBLE_NULL = new Class<?>[]{java.util.List.class, java.util.Vector.class, java.util.Queue.class};

  private static final Set<String> CACHED_CLASS_NAMES_POSITIVE_RECOGNIZED = new HashSet<>();
  private static final Set<String> CACHED_CLASS_NAMES_NEGATIVE_RECOGNIZED = new HashSet<>();

  private static final Set<String> ANNOTATIONS_FOR_OBJECT = classesToNameSet(javax.annotation.Nullable.class, javax.annotation.Nonnull.class, "org.jetbrains.annotations.Nullable", "org.jetbrains.annotations.NotNull");
  private static final Set<String> ANNOTATIONS_FOR_ARRAY_OR_COLLECTION = classesToNameSet(MayContainNull.class, MustNotContainNull.class);

  private static final String RETURN_TYPE_NOTIFICATION = "Return type must be marked by either @%s or @%s";
  private static final String RETURN_TYPE_NOTIFICATION_WRONG_TYPE = "Non-object result type can't be marked by either @%s or @%s";
  private static final String ARG_TYPE_NOTIFICATION = "Arg. #%d must be marked by either @%s or @%s";

  public static void checkReturnTypeForNullable(final Context context, final Method method) {
    final Type returnType = method.getReturnType();
    final AnnotationEntry[] annotations = method.getAnnotationEntries();
    if (isNullableType(returnType)) {
      if (!hasAnnotationFromSet(annotations, ANNOTATIONS_FOR_OBJECT)) {
        context.error(String.format(RETURN_TYPE_NOTIFICATION, Utils.extractClassName(Nullable.class.getName()), Utils.extractClassName(Nonnull.class.getName())), true);
      }
    } else {
      if (hasAnnotationFromSet(annotations, ANNOTATIONS_FOR_OBJECT)) {
        context.error(String.format(RETURN_TYPE_NOTIFICATION_WRONG_TYPE, Utils.extractClassName(Nullable.class.getName()), Utils.extractClassName(Nonnull.class.getName())), true);
      }
    }
  }

  public static void checkReturnTypeForMayContainNull(final Context context, final Method method) {
    final Type returnType = method.getReturnType();
    if (isNullableType(returnType)) {
      final AnnotationEntry[] annotations = method.getAnnotationEntries();

      if (isArrayOfObjectsOrCollection(context, returnType) && !hasAnnotationFromSet(annotations, ANNOTATIONS_FOR_ARRAY_OR_COLLECTION)) {
        context.error(String.format(RETURN_TYPE_NOTIFICATION, Utils.extractClassName(MayContainNull.class.getName()), Utils.extractClassName(MustNotContainNull.class.getName())), true);
      }
    }
  }

  private static boolean shouldSkipFirstMethodArg(final JavaClass klazz, final Method method) {
    if (klazz.isNested() && "<init>".equals(method.getName()) && method.getArgumentTypes().length>0){
      if (klazz.isStatic()) return false;
      final String firstArgType = method.getArgumentTypes()[0].getSignature();
      final String outerKlazzSignature = 'L'+Utils.extractOuterClassName(klazz.getClassName())+';';
      return firstArgType.equals(outerKlazzSignature);
    }
    return method.getName().equals("<init>") && klazz.isNested() && !klazz.isStatic();
  }

  public static void checkParamsTypeForNullable(final Context context, final Method method) {
    final Type[] arguments = method.getArgumentTypes();
    final ParameterAnnotationEntry[] paramAnnotations = method.getParameterAnnotationEntries();

    final boolean ignoreFirst = shouldSkipFirstMethodArg(context.getProcessingClass(), method);
    final int argLength = arguments.length;
    final int realArgLength = ignoreFirst ? argLength - 1 : argLength;

    int paramAnnoIndex = 0;
    for (int argIndex = 0; argIndex < argLength; argIndex++) {
      if (ignoreFirst && argIndex == 0) {
        continue;
      }
      if (isNullableType(arguments[argIndex])
          && (paramAnnoIndex >= paramAnnotations.length || !hasParameterAnnotationFromSet(realArgLength, paramAnnoIndex, paramAnnotations, ANNOTATIONS_FOR_OBJECT))) {
        context.error(String.format(ARG_TYPE_NOTIFICATION, argIndex + 1, Utils.extractClassName(Nullable.class.getName()), Utils.extractClassName(Nonnull.class.getName())), true);
      }
      paramAnnoIndex++;
    }
  }

  public static void checkParamsTypeForMayContainNull(final Context context, final Method method) {
    final Type[] arguments = method.getArgumentTypes();
    final ParameterAnnotationEntry[] paramAnnotations = method.getParameterAnnotationEntries();

    final boolean ignoreFirst = shouldSkipFirstMethodArg(context.getProcessingClass(), method);
    final int argLength = arguments.length;
    final int realArgLength = ignoreFirst ? argLength - 1 : argLength;

    int paramAnnoIndex = 0;
    for (int argIndex = 0; argIndex < argLength; argIndex++) {
      if (ignoreFirst && argIndex == 0) {
        continue;
      }

      if (isNullableType(arguments[argIndex]) && isArrayOfObjectsOrCollection(context, arguments[argIndex])
          && (paramAnnoIndex >= paramAnnotations.length || !hasParameterAnnotationFromSet(realArgLength, paramAnnoIndex, paramAnnotations, ANNOTATIONS_FOR_ARRAY_OR_COLLECTION))) {
        context.error(String.format(ARG_TYPE_NOTIFICATION, argIndex + 1, Utils.extractClassName(MayContainNull.class.getName()), Utils.extractClassName(MustNotContainNull.class.getName())), true);
      }
      paramAnnoIndex++;
    }
  }

  private static Set<String> classesToNameSet(final Object... classObjects) {
    final Set<String> result = new HashSet<>();
    for (final Object a : classObjects) {
      if (a instanceof Class) {
        result.add(Utils.makeSignatureForClass((Class<?>) a));
      } else if (a instanceof String) {
        result.add(Utils.makeSignatureForClass((String) a));
      } else {
        throw new IllegalArgumentException("Unexpected object type [" + a + ']');
      }
    }
    return result;
  }

  private static boolean isNullableType(final Type type) {
    final String signature = type.getSignature();
    return !signature.isEmpty() && (signature.charAt(0) == '[' || signature.charAt(0) == 'L');
  }

  private static boolean isArrayOfObjectsOrCollection(final Context context, final Type type) {
    final String signature = type.getSignature();

    final int arrayLastChar = signature.lastIndexOf('[');

    if (arrayLastChar > 0) {
      return true;
    }

    if (signature.endsWith(";")) {
      if (arrayLastChar == 0) {
        return true;
      }
    } else {
      return false;
    }

    if (CACHED_CLASS_NAMES_POSITIVE_RECOGNIZED.contains(signature)) {
      return true;
    }
    if (CACHED_CLASS_NAMES_NEGATIVE_RECOGNIZED.contains(signature)) {
      return false;
    }

    return investigateClassThatCanContainNull(context, signature);
  }

  private static boolean investigateClassThatCanContainNull(final Context context, final String classTypeInInsideFormat) {
    final String klazzName = Utils.classNameToCanonical(classTypeInInsideFormat);
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
      } catch (Exception ex) {
        context.warning("Can't parse class file : " + classFile.getAbsolutePath(), false);
      }
    } else {
      // it is possible that it is standard class
      final String normalClassName = klazzName.replace('$', '.');
      try {
        final Class<?> foundClass = Class.forName(normalClassName);
        for (final Class<?> classWhereNullPossible : CLASSES_WHERE_POSSIBLE_NULL) {
          if (classWhereNullPossible.isAssignableFrom(foundClass)) {
            CACHED_CLASS_NAMES_POSITIVE_RECOGNIZED.add(classTypeInInsideFormat);
            return true;
          }
        }
      } catch (ClassNotFoundException ignored) {
      }
    }
    CACHED_CLASS_NAMES_NEGATIVE_RECOGNIZED.add(classTypeInInsideFormat);
    return false;
  }

  private static boolean hasAnnotationFromSet(final AnnotationEntry[] annotations, final Set<String> namesToFind) {
    for (final AnnotationEntry a : annotations) {
      if (namesToFind.contains(a.getAnnotationType())) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasParameterAnnotationFromSet(final int numberOfArguments, int indexInParamAnnotations, final ParameterAnnotationEntry[] paramAnnotations, final Set<String> namesToFind) {
    while (indexInParamAnnotations < paramAnnotations.length) {
      for (final AnnotationEntry a : paramAnnotations[indexInParamAnnotations].getAnnotationEntries()) {
        if (namesToFind.contains(a.getAnnotationType())) {
          return true;
        }
      }
      indexInParamAnnotations += numberOfArguments;
    }
    return false;
  }

}
