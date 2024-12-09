/*
 * Copyright 2016 Igor Maznitsa (http://www.igormaznitsa.com).
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

package com.igormaznitsa.meta.checker;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.Type;

public abstract class Utils {


  public static String extractClassName(final String className) {
    if (className == null) {
      return null;
    }
    final String normalized =
        className.indexOf('/') >= 0 ? classNameToCanonical(className) : className;
    return extractShortNameOfClass(normalized);
  }

  private Utils() {
  }

  public static String printTimeDelay(final Duration duration) {
    long hours = duration.toHours();
    long minutes = duration.toMinutes() % 60;
    long seconds = duration.getSeconds() % 60;
    long millis = duration.toMillis() % 1000;

    return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
  }

  public static String extractShortNameOfClass(final String canonicalClassName) {
    int index = canonicalClassName.lastIndexOf('.');
    String text = index < 0 ? canonicalClassName : canonicalClassName.substring(index + 1);
    index = text.lastIndexOf('$');
    text = index < 0 ? text : text.substring(index);
    return text;
  }

  public static String classNameToCanonical(final String className) {
    if (className.startsWith("L") && className.endsWith(";")) {
      return className.substring(1, className.length() - 1).replace('/', '.');
    } else {
      return className;
    }
  }

  public static String makeSignatureForClass(final Class<?> javaClass) {
    return makeSignatureForClass(javaClass.getName());
  }

  public static String makeSignatureForClass(final String name) {
    return 'L' + name.replace('.', '/') + ';';
  }

  public static String normalizeClassNameAndRemoveSubclassName(final String canonicalClassName) {
    String result = canonicalClassName.replace('.', '/');
    final int index = result.lastIndexOf('$');
    result = index >= 0 ? result.substring(0, index) : result;
    return result;
  }

  public static String extractOuterClassName(final String jvmInnerClassName) {
    final int lastDelimiter = jvmInnerClassName.lastIndexOf('$');
    String result = "";
    if (lastDelimiter >= 0) {
      result = jvmInnerClassName.substring(0, lastDelimiter).replace('.', '/');
    }
    return result;
  }

  public static String asString(final JavaClass clazz, final FieldOrMethod item) {
    final StringBuilder result = new StringBuilder();
    if (item != null) {
      if (item instanceof Field) {
        final Field field = (Field) item;
        final String type = Utility.signatureToString(field.getSignature());
        final String name = field.getName();
        result.append(type).append(' ').append(name);
      } else {
        final Method method = (Method) item;
        String type = Utility.methodSignatureReturnType(method.getSignature());

        String name = method.getName();
        if (name.equals("<init>")) {
          name = Utils.extractClassName(clazz.getClassName());
          type = "";
        } else {
          type += ' ';
        }

        final String[] args = Utility.methodSignatureArgumentTypes(method.getSignature());
        final LocalVariableTable locVars = method.getLocalVariableTable();

        result.append(type).append(name).append('(');
        final int locVarOffset = method.isStatic() ? 0 : 1;
        int index = 0;
        for (final String a : args) {
          if (index > 0) {
            result.append(',');
          }
          result.append(a);
          final int argIndex = locVarOffset + index;
          if (locVars != null && argIndex < locVars.getTableLength()) {
            final LocalVariable localVariable = locVars.getLocalVariable(argIndex, 0);
            final String argName = localVariable == null ? "<unknown>" : localVariable.getName();
            if (argName != null) {
              result.append(' ').append(argName);
            }
          }
          index++;
        }
        result.append(')');
      }
    }
    return result.toString();
  }

  public static int findLineNumber(final FieldOrMethod fieldOrMethod) {
    int result = -1;
    if (fieldOrMethod instanceof Method) {
      final Method method = (Method) fieldOrMethod;
      final LineNumberTable lineTable = method.getLineNumberTable();
      if (lineTable != null && lineTable.getTableLength() > 0) {
        result = lineTable.getLineNumberTable()[0].getLineNumber();
        if (result > 1) {
          result--;
        }
      }
    }
    return result;
  }

  public static List<String> splitToLines(final String str) {
    if (str == null || str.isEmpty()) {
      return Collections.emptyList();
    }

    final String[] splitArray = str.split("\\n");
    for (int i = 0; i < splitArray.length; i++) {
      splitArray[i] = splitArray[i].replace("\t", "    ");
    }

    return Arrays.asList(splitArray);
  }

  public static String escapeRegexToWildCat(final String text) {
    final StringBuilder result = new StringBuilder(text.length() * 3);
    result.append('^');
    for (final char c : text.toCharArray()) {
      switch (c) {
        case ' ':
          result.append("\\s");
          break;
        case '*':
          result.append(".*?");
          break;
        case '?':
          result.append(".");
          break;
        default:
          result.append(utfCode(c));
          break;
      }
    }
    result.append('$');
    return result.toString();
  }

  public static boolean isObjectType(final Type type) {
    final String text = type.getSignature();
    return text.endsWith(";");
  }

  private static String utfCode(final char ch) {
    final String s = Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    return "\\u" + "0000".substring(0, 4 - s.length()) + s;
  }
}
