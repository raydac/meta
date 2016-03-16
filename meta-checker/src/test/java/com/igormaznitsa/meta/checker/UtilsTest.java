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
package com.igormaznitsa.meta.checker;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilsTest {
  
  @Test
  public void testEscapeRegexToWildCat() throws Exception {
    assertEquals("^$",Utils.escapeRegexToWildCat(""));
    assertEquals("^\\u0048\\u0065\\u006C\\u006C\\u006F$",Utils.escapeRegexToWildCat("Hello"));
    assertEquals("^\\u0048\\u0065\\u006C\\u006C\\u006F.*?\\u0057\\u006F\\u0072\\u006C\\u0064$",Utils.escapeRegexToWildCat("Hello*World"));
    assertEquals("^\\u30C6\\u30F3\\u30E9\\u30A4$",Utils.escapeRegexToWildCat("テンライ"));
  }
  
}
