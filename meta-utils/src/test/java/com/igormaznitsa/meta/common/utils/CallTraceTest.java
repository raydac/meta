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

public class CallTraceTest {

  @Test
  public void testConstructorEmpty () {
    final CallTrace callTrace = new CallTrace();
    final String[] lines = callTrace.toString().split("\n");
    assertTrue(lines[0].contains("THREAD_ID : "));
    assertTrue(lines[1].contains("testConstructorEmpty") && lines[1].contains("CallTraceTest") && lines[1].contains(":25"));
  }

  @Test
  public void testThreadDescriptorConstructorEmpty() {
    final CallTrace callTrace = new CallTrace();
    assertEquals(callTrace.getThreadDescriptor(), Thread.currentThread().toString());
  }

  @Test
  public void testConstructor() {
    final CallTrace callTrace = new CallTrace(true,false,"\n");
    final String[] lines = callTrace.toString().split("\n");
    assertTrue(lines[0].contains("THREAD_ID : "));
    assertTrue(lines[1].contains("testConstructor") && lines[1].contains("CallTraceTest") && lines[1].contains(":39"));
  }

}
