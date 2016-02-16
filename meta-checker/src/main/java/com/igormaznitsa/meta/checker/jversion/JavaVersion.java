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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum JavaVersion {
  JDK11("1.1", 0x2D),
  JDK12("1.2", 0x2E),
  JDK13("1.3", 0x2F),
  JDK14("1.4", 0x30),
  JDK50("5.0", 0x31),
  JDK60("6.0", 0x32),
  JDK70("7.0", 0x33),
  JDK80("8.0", 0x34);

  private final String text;
  private final int value;

  private JavaVersion(final String text, final int value) {
    this.text = text;
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }

  public String getText() {
    return this.text;
  }

  @Nullable
  public static JavaVersion decode(@Nonnull final String text) {
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

  @Nullable
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
