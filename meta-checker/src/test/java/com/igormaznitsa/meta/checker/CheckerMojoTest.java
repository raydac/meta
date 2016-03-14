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
import java.io.File;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class CheckerMojoTest extends AbstractMojoTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Test
  public void testDefaultCfg() throws Exception {
    final File testPom = new File(this.getClass().getResource("default.xml").toURI());
    final CheckerMojo mojo = (CheckerMojo) lookupMojo("check", testPom);
    assertNotNull(mojo);
    assertFalse(mojo.isCheckMayContainNullArgs());
    assertFalse(mojo.isCheckNullableArgs());
    assertNull(mojo.getRestrictClassFormat());
    assertNull(mojo.getIgnoreClasses());
    assertNull(mojo.getFailForAnnotations());
  }
  
  @Test
  public void testAllSetCfg() throws Exception {
    final File testPom = new File(this.getClass().getResource("allset.xml").toURI());
    final CheckerMojo mojo = (CheckerMojo) lookupMojo("check", testPom);
    assertNotNull(mojo);
    assertTrue(mojo.isCheckMayContainNullArgs());
    assertTrue(mojo.isCheckNullableArgs());
    assertEquals(">=7",mojo.getRestrictClassFormat());
    assertArrayEquals(new String[]{"com.hello.world","com.*?.test"},mojo.getIgnoreClasses());
    assertArrayEquals(new String[]{"risky"},mojo.getFailForAnnotations());
  }
  
}
