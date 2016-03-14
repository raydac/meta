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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.Type;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.igormaznitsa.meta.common.utils.Assertions;

public abstract class Utils {

  private static final PeriodFormatter TIME_FORMATTER = new PeriodFormatterBuilder()
          .printZeroAlways()
          .minimumPrintedDigits(2)
          .appendHours().appendSeparator(":")
          .appendMinutes().appendSeparator(":")
          .appendSeconds().appendSeparator(".")
          .minimumPrintedDigits(3)
          .appendMillis().toFormatter();

  public static String extractClassName(final String className) {
    if (className == null) return null;
    final String normalized = className.indexOf('/') >= 0 ? classNameToNormalView(className) : className;
    return extractShortNameOfClass(normalized);
  }

  private Utils() {
  }

  public static final String LINE_SEPARATOR = System.getProperty("line.separator", "\r\n");

  public static boolean checkClassAndMethodForPattern(final String juteTest, final String className, final String methofName, final boolean onlyClass) {
    if (className == null || (!onlyClass && methofName == null)) {
      return false;
    }
    if (juteTest == null || juteTest.isEmpty()) {
      return true;
    }
    final String classPattern;
    final String methodPattern;
    final int methodPrefix = juteTest.indexOf('#');
    if (methodPrefix < 0) {
      classPattern = juteTest;
      methodPattern = "*";
    }
    else {
      classPattern = juteTest.substring(0, methodPrefix);
      methodPattern = juteTest.substring(methodPrefix + 1);
    }
    return FilenameUtils.wildcardMatch(className, classPattern) && (onlyClass ? true : FilenameUtils.wildcardMatch(methofName, methodPattern));
  }

  public static String printTimeDelay(final long timeInMilliseconds) {
    final Duration duration = new Duration(timeInMilliseconds);
    final Period period = duration.toPeriod().normalizedStandard(PeriodType.time());
    return TIME_FORMATTER.print(period);
  }

  public static int getMaxLineWidth(final List<String> str) {
    int max = 0;
    if (str != null && !str.isEmpty()) {
      for (final String s : str) {
        if (s != null && s.length() > max) {
          max = s.length();
        }
      }
    }
    return max;
  }

  public static String extractShortNameOfClass(final String cacnonicalClassName){
    int index = cacnonicalClassName.lastIndexOf('.');
    String text = index<0 ? cacnonicalClassName : cacnonicalClassName.substring(index+1);
    index = text.lastIndexOf('$');
    text = index < 0 ? text : cacnonicalClassName.substring(index + 1);
    return text;
  }
  
  public static String classNameToNormalView(final String className){
    if (className.startsWith("L") && className.endsWith(";")){
      return className.substring(1,className.length()-1).replace('/', '.');
    }else{
      return className;
    }
  }
  
  public static String makeSignatureForClass(final Class<?> klazz){
    return makeSignatureForClass(klazz.getName());
  }
  
  public static String makeSignatureForClass(final String name){
    return 'L'+name.replace('.', '/')+';';
  }
  
  public static String makeStr(final int len, final char ch) {
    final StringBuilder result = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      result.append(ch);
    }
    return result.toString();
  }

  public static String normalizeClassNameAndRemoveSubclassName(final String canonicalClassName) {
    String result = canonicalClassName.replace('.', '/');
    final int index = result.lastIndexOf('$');
    result = index >= 0 ? result.substring(0, index) : result;
    return result;
  }

  public static String asString(final JavaClass clazz,final FieldOrMethod item) {
    final StringBuilder result = new StringBuilder();
    if (item != null) {
      if (item instanceof Field) {
        final Field field = (Field) item;
        final String type = Utility.signatureToString(field.getSignature());
        final String name = field.getName();
        result.append(type).append(' ').append(name);
      }
      else {
        final Method method = (Method) item;
        String type = Utility.methodSignatureReturnType(method.getSignature());
        
        String name = method.getName();
        if (name.equals("<init>")){
          name = Utils.extractClassName(clazz.getClassName());
          type = "";
        } else {
          type += ' ';
        }
        
        final String args[] = Utility.methodSignatureArgumentTypes(method.getSignature());
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
            final String argName = locVars.getLocalVariable(argIndex, 0).getName();
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
    if (fieldOrMethod != null) {
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
      else {
        final Field field = (Field) fieldOrMethod;
      }
    }
    return result;
  }

  public static List<String> splitToLines(final String str) {
    if (str == null || str.length() == 0) {
      return Collections.<String>emptyList();
    }

    final String[] strarray = str.split("\\n");
    for (int i = 0; i < strarray.length; i++) {
      strarray[i] = strarray[i].replace("\t", "    ");
    }

    return Arrays.asList(strarray);
  }

  public static String escapeRegexToWildCat(final String text){
    final StringBuilder result = new StringBuilder(text.length()*3);
    result.append('^');
    for(final char c : text.toCharArray()){
      switch(c){
        case ' ' : result.append("\\s");break;
        case '*' : result.append(".*?");break;
        case '?' : result.append(".");break;
        default: 
          result.append(utfCode(c));break;
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
    return "\\u"+ "0000".substring(4-s.length())+s;
  }
}
