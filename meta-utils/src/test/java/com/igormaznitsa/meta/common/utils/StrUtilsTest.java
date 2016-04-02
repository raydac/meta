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
package com.igormaznitsa.meta.common.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class StrUtilsTest {
  
  @Test
  public void testTrimLeft_NonNull() {
    assertEquals("",StrUtils.trimLeft(""));
    assertEquals("",StrUtils.trimLeft("    "));
    assertEquals("аааа ",StrUtils.trimLeft("   аааа "));
    assertEquals("аааа ",StrUtils.trimLeft(" \t\n  аааа "));
    assertEquals("аа\bаа ",StrUtils.trimLeft(" \t\n  аа\bаа "));
    assertEquals("1234567    \n\b\t", StrUtils.trimLeft(" 1234567    \n\b\t"));
  }

  @Test(expected = AssertionError.class)
  public void testTrimLeft_AEforNull() {
    StrUtils.trimLeft(null);
  }  

  @Test
  public void testTrimRight_NonNull() {
    assertEquals("",StrUtils.trimRight(""));
    assertEquals("",StrUtils.trimRight("    "));
    assertEquals(" 1234567",StrUtils.trimRight(" 1234567    \n\b\t"));
    assertEquals("   аааа",StrUtils.trimRight("   аааа "));
    assertEquals(" \t\n  аааа",StrUtils.trimRight(" \t\n  аааа "));
    assertEquals(" \t\n  аа\bаа",StrUtils.trimRight(" \t\n  аа\bаа "));
  }

  @Test(expected = AssertionError.class)
  public void testTrimRight_AEforNull() {
    StrUtils.trimRight(null);
  }  

  @Test
  public void testPressing_NonNull() {
    assertEquals("",StrUtils.pressing(""));
    assertEquals("",StrUtils.pressing("    "));
    assertEquals("1234567",StrUtils.pressing(" 1234567    \n\b\t"));
    assertEquals("аааа",StrUtils.pressing("   аааа "));
    assertEquals("аааа",StrUtils.pressing(" \t\n  аааа "));
    assertEquals("аааа",StrUtils.pressing(" \t\n  аа\bаа "));
  }

  @Test(expected = AssertionError.class)
  public void testPressing_AEforNull() {
    StrUtils.pressing(null);
  }  
}
