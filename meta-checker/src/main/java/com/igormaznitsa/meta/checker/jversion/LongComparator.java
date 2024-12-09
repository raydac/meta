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
package com.igormaznitsa.meta.checker.jversion;

import com.igormaznitsa.meta.checker.jversion.comparators.AbstractCompareLongAction;
import com.igormaznitsa.meta.checker.jversion.comparators.ComparatorLongEQU;
import com.igormaznitsa.meta.checker.jversion.comparators.ComparatorLongEQUGT;
import com.igormaznitsa.meta.checker.jversion.comparators.ComparatorLongEQULESS;
import com.igormaznitsa.meta.checker.jversion.comparators.ComparatorLongGT;
import com.igormaznitsa.meta.checker.jversion.comparators.ComparatorLongLESS;

public enum LongComparator {
  LESS_EQU("<=", new ComparatorLongEQULESS()),
  GT_EQU(">=", new ComparatorLongEQUGT()),
  EQU("=", new ComparatorLongEQU()),
  LESS("<", new ComparatorLongLESS()),
  GT(">", new ComparatorLongGT());

  private final String text;
  private final AbstractCompareLongAction action;

  LongComparator(final String text, final AbstractCompareLongAction action) {
    this.text = text;
    this.action = action;
  }

  public String getText() {
    return this.text;
  }

  public boolean compare(final long value1, final long value2) {
    return this.action.compare(value1, value2);
  }

  public static LongComparator find(final String text) {
    if (text == null) {
      return null;
    }
    final String trimmed = text.trim();
    for (final LongComparator op : values()) {
      if (trimmed.startsWith(op.text)) {
        return op;
      }
    }
    return null;
  }

}
