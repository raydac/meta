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
package com.igormaznitsa.meta;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ComplexityTest {
  
  public static String pressing(final String value) {
    final StringBuilder result = new StringBuilder(value.length());
    for (int index = 0; index < value.length(); index++) {
      final char chr = value.charAt(index);
      if (Character.isWhitespace(chr) || Character.isISOControl(chr)) {
        continue;
      }
      result.append(chr);
    }
    return result.toString();
  }
  
  @Test
  public void testCheckDescriptionDuplication() {
    final Set<String> set = new HashSet<String>();
    for(final Complexity c : Complexity.values()){
      assertTrue("Detected non-uniq text (formula): "+c.getFormula(),set.add(pressing(c.getFormula()).toLowerCase(Locale.ENGLISH)));
      assertTrue("Detected non-uniq text (name): "+c.name(),set.add(pressing(c.name()).toLowerCase(Locale.ENGLISH)));
    }
  }
  
}
