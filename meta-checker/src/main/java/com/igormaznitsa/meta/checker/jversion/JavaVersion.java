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
package com.igormaznitsa.meta.checker.jversion;

public enum JavaVersion {
  JDK1_1("1.1", 0x2D),
  JDK1_2("1.2", 0x2E),
  JDK1_3("1.3", 0x2F),
  JDK1_4("1.4", 0x30),
  JDK5_0("5.0", 0x31),
  JDK6_0("6.0", 0x32),
  JDK7_0("7.0", 0x33),
  JDK8_0("8.0", 0x34),
  JDK9_0("9.0", 0x35),
  JDK10("10.0", 0x36),
  JDK11("11.0", 0x37),
  JDK12("12.0", 0x38),
  JDK13("13.0", 0x39),
  JDK14("14.0", 0x3A),
  JDK15("15.0", 0x3B),
  JDK16("16.0", 0x3C),
  JDK17("17.0", 0x3D),
  JDK18("18.0", 0x3E),
  JDK19("19.0", 0x3F),
  JDK20("20.0", 0x40),
  JDK21("21.0", 0x41),
  JDK22("22.0", 0x42),
  JDK23("23.0", 0x43);

  private final String text;
  private final int value;

  JavaVersion(final String text, final int value) {
    this.text = text;
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }

  public String getText() {
    return this.text;
  }

  public static JavaVersion decode(final String text) {
    String trimmed = text.trim();
    if (trimmed.indexOf('.') < 0) {
      trimmed += ".0";
    }
    for (final JavaVersion v : values()) {
      if (v.text.equals(trimmed)) {
        return v;
      }
    }
    return null;
  }

  public static JavaVersion decode(final int major) {
    for (final JavaVersion v : values()) {
      if (major == v.getValue()) {
        return v;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return this.text;
  }
}
