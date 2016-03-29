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
package com.igormaznitsa.meta.checker.processors;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConstraintTest {
  
  @Test
  public void testArgPattern_False() {
    assertFalse(Constraint.ARG_SEARCHER.matcher("").matches());
    assertFalse(Constraint.ARG_SEARCHER.matcher("1x2").matches());
    assertFalse(Constraint.ARG_SEARCHER.matcher("abcdxf").matches());
    assertFalse(Constraint.ARG_SEARCHER.matcher("xef").matches());
    assertFalse(Constraint.ARG_SEARCHER.matcher("efx").matches());
  }
  
  @Test
  public void testArgPattern_True() {
    assertTrue(Constraint.ARG_SEARCHER.matcher("abs(x)").matches());
    assertTrue(Constraint.ARG_SEARCHER.matcher("!empty( x )").matches());
    assertTrue(Constraint.ARG_SEARCHER.matcher("x>3").matches());
    assertTrue(Constraint.ARG_SEARCHER.matcher("3<x").matches());
    assertTrue(Constraint.ARG_SEARCHER.matcher("x").matches());
    assertTrue(Constraint.ARG_SEARCHER.matcher("x ").matches());
    assertTrue(Constraint.ARG_SEARCHER.matcher(" x").matches());
    assertTrue(Constraint.ARG_SEARCHER.matcher(";x").matches());
    assertTrue(Constraint.ARG_SEARCHER.matcher("x\n").matches());
    assertTrue(Constraint.ARG_SEARCHER.matcher("\nx\n").matches());
  }
  
}
