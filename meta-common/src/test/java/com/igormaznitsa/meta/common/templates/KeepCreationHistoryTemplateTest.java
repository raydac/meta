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
package com.igormaznitsa.meta.common.templates;

import org.junit.Test;
import static org.junit.Assert.*;

public class KeepCreationHistoryTemplateTest {
  
  private static class KCPTest extends KeepCreationPointTemplate {
  }
  
  @Test
  public void testCreateInstance () {
    final KCPTest t = new KCPTest();
    final String [] parsed = t.getCreationPoint().toString().split("\n");
    assertTrue(parsed[0].contains("KeepCreationHistoryTemplateTest") && parsed[0].contains("testCreateInstance") && parsed[0].contains(":28"));
  }
  
}
