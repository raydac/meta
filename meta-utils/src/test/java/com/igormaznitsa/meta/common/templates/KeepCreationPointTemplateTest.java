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

public class KeepCreationPointTemplateTest {
  
  private static class KCPTest extends KeepCreationPointTemplate {

    private static final long serialVersionUID = 4734211109886249142L;

  }
  
  @Test
  public void testCreateInstance() {
    final KCPTest t = new KCPTest();
    final String [] parsed = t.getCreationPoint().toString().split("\n");
    assertTrue(parsed[1].contains("KeepCreationPointTemplateTest") && parsed[1].contains("testCreateInstance") && parsed[1].contains(":31"));
  }

  private static KCPTest _make() {
    return new KCPTest();
  }

  @Test
  public void testCreateInstance_2() {
    final KCPTest t = _make();
    final String[] parsed = t.getCreationPoint().toString().split("\n");
    assertTrue(parsed[1].contains("KeepCreationPointTemplateTest") && parsed[1].contains("_make") && parsed[1].contains(":37"));
  }

}
