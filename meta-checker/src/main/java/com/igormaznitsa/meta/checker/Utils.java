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

import java.util.*;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public abstract class Utils {

  private static final PeriodFormatter TIME_FORMATTER = new PeriodFormatterBuilder()
          .printZeroAlways()
          .minimumPrintedDigits(2)
          .appendHours().appendSeparator(":")
          .appendMinutes().appendSeparator(":")
          .appendSeconds().appendSeparator(".")
          .minimumPrintedDigits(3)
          .appendMillis().toFormatter();

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

  public static String makeStr(final int len, final char ch) {
    final StringBuilder result = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      result.append(ch);
    }
    return result.toString();
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
}
