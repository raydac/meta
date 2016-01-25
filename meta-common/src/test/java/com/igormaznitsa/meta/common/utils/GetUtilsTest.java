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

public class GetUtilsTest {
  
  @Test
  public void testEnsureNonNull_NonNull () {
    final Object obj = new Object();
    assertSame(obj,GetUtils.ensureNonNull(obj));
  }
  
  @Test (expected = AssertionError.class)
  public void testEnsureNonNull_Null () {
    final Object obj = null;
    GetUtils.ensureNonNull(obj);
  }

  @Test
  public void testEnsureNonNull_ValueNonNull () {
    final Object obj = new Object();
    final Object dflt = new Object();
    assertSame(obj, GetUtils.ensureNonNull(obj,dflt));
  }

  @Test
  public void testEnsureNotNull_ValueNull () {
    final Object obj = null;
    final Object dflt = new Object();
    assertSame(dflt, GetUtils.ensureNonNull(obj,dflt));
  }

  @Test (expected = AssertionError.class)
  public void testEnsureNotNull_ValueAndDefaultAreNull () {
    final Object obj = null;
    final Object dflt = null;
    GetUtils.ensureNonNull(obj,dflt);
  }

  @Test
  public void testFindFirstNonNull_Presented() {
    final String str = "test";
    assertSame(str, GetUtils.findFirstNonNull(null,str,new String("test")));
  }

  @Test(expected = AssertionError.class)
  public void testFindFirstNonNull_NotPresented() {
    GetUtils.findFirstNonNull(null,null,null);
  }
}
