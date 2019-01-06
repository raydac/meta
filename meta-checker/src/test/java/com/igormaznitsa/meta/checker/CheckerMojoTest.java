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

public final class CheckerMojoTest extends AbstractMojoTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testDefaultCfg_CheckJar() throws Exception {
    final File testPom = new File(this.getClass().getResource("default.xml").toURI());
    final JarCheckerMojo mojo = (JarCheckerMojo) lookupMojo("check-jar", testPom);
    assertNotNull(mojo);
    assertNull(mojo.getArchive());
    assertNull(mojo.getRestrictClassFormat());
    assertTrue(mojo.getExclude().isEmpty());
    assertTrue(mojo.getInclude().isEmpty());
    assertTrue(mojo.getExpected().isEmpty());
    assertTrue(mojo.getUnexpected().isEmpty());
    assertTrue(mojo.getManifestHas().isEmpty());
    assertTrue(mojo.getManifestHasNot().isEmpty());
  }

  @Test
  public void testAllCfg_CheckJar() throws Exception {
    final File testPom = new File(this.getClass().getResource("allset_checkjar.xml").toURI());
    final JarCheckerMojo mojo = (JarCheckerMojo) lookupMojo("check-jar", testPom);
    assertNotNull(mojo);
    assertEquals("some.jar",mojo.getArchive());
    assertEquals(">=7",mojo.getRestrictClassFormat());
    assertArrayEquals(new String[]{"some1/**/*.class","some2/**/*.class"},mojo.getExclude().toArray());
    assertArrayEquals(new String[]{"some3/**/*.class", "some4/**/*.class"},mojo.getInclude().toArray());
    assertArrayEquals(new String[]{"some/**/image1.jpg","some/**/image2.jpg"},mojo.getExpected().toArray());
    assertArrayEquals(new String[]{"some/**/image3.jpg", "some/**/image4.jpg"},mojo.getUnexpected().toArray());
    assertArrayEquals(new String[]{"Main-Class","Compiled-By"},mojo.getManifestHas().toArray());
    assertArrayEquals(new String[]{"Attribute1","Attribute2"},mojo.getManifestHasNot().toArray());
  }

  @Test
  public void testDefaultCfg() throws Exception {
    final File testPom = new File(this.getClass().getResource("default.xml").toURI());
    final CheckerMojo mojo = (CheckerMojo) lookupMojo("check", testPom);
    assertNotNull(mojo);
    assertFalse(mojo.isCheckMayContainNullArgs());
    assertFalse(mojo.isCheckNullableArgs());
    assertFalse(mojo.isHideBanner());
    assertNull(mojo.getRestrictClassFormat());
    assertNull(mojo.getIgnoreClasses());
    assertNull(mojo.getFailForAnnotations());
    assertNull(mojo.getMaxAllowedWeight());
    assertNull(mojo.getMaxAllowedTimeComplexity());
    assertNull(mojo.getMaxAllowedMemoryComplexity());
  }
  
  @Test
  public void testAllSetCfg() throws Exception {
    final File testPom = new File(this.getClass().getResource("allset.xml").toURI());
    final CheckerMojo mojo = (CheckerMojo) lookupMojo("check", testPom);
    assertNotNull(mojo);
    assertTrue(mojo.isCheckMayContainNullArgs());
    assertTrue(mojo.isCheckNullableArgs());
    assertTrue(mojo.isHideBanner());
    assertEquals(">=7",mojo.getRestrictClassFormat());
    assertArrayEquals(new String[]{"com.hello.world","com.*?.test"},mojo.getIgnoreClasses());
    assertArrayEquals(new String[]{"risky"},mojo.getFailForAnnotations());
    assertEquals("maxWeight",mojo.getMaxAllowedWeight());
    assertEquals("maxTime",mojo.getMaxAllowedTimeComplexity());
    assertEquals("maxMemory",mojo.getMaxAllowedMemoryComplexity());
  }
  
}
