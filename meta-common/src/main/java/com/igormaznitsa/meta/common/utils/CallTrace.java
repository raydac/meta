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

import com.igormaznitsa.meta.common.annotations.Immutable;
import com.igormaznitsa.meta.common.annotations.NonNull;
import com.igormaznitsa.meta.common.annotations.ThreadSafe;
import com.igormaznitsa.meta.common.annotations.Weight;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * The Class allows to save stack trace history (it is possible to keep it in packed format) and restore it to text representation for request.
 * 
 * @since 1.0
 */
@Weight(Weight.Unit.VARIABLE)
@ThreadSafe
@Immutable
public class CallTrace implements Serializable {

  private static final long serialVersionUID = -3908621401136825952L;

  private static final Charset UTF8 = Charset.forName("UTF-8");

  /**
   * Default end-of-line for linux.
   * @since 1.0
   */
  public static final String EOL_LINUX = "\n";
  
  /**
   * Default end-of-line for windows.
   * @since 1.0
   */
  public static final String EOL_WINDOWS = "\r\n";

  private final boolean packed;
  private final byte[] stacktrace;

  /**
   * The Constructor allows to create call trace history point for the called method.
   * @since 1.0
   * @see #EOL_LINUX
   */
  public CallTrace () {
    this(3, true, EOL_LINUX);
  }

  /**
   * The Constructor allows to create call trace history with defined end-of-line symbol and since needed stack item position.
   * @param numberOfFirstIgnoredStackItems number of ignorable first stack trace items to avoid saving of internal calls.
   * @param pack flag shows that string data must be packed, false if should not be packed
   * @param eol string shows which end-of-line should be used
   * 
   * @since 1.0
   * @see #EOL_LINUX
   * @see #EOL_WINDOWS
   */
  @Weight(value = Weight.Unit.VARIABLE, comment = "Depends on the call stack depth")
  public CallTrace (final int numberOfFirstIgnoredStackItems, final boolean pack, @NonNull final String eol) {
    final StackTraceElement[] allElements = Thread.currentThread().getStackTrace();
    final StringBuilder buffer = new StringBuilder((allElements.length - numberOfFirstIgnoredStackItems) * 32);
    for (int i = numberOfFirstIgnoredStackItems; i < allElements.length; i++) {
      if (buffer.length() > 0) {
        buffer.append(eol);
      }
      buffer.append(allElements[i].toString());
    }
    this.packed = pack;
    if (pack) {
      this.stacktrace = IOUtils.packData(buffer.toString().getBytes(UTF8));
    }
    else {
      this.stacktrace = buffer.toString().getBytes(UTF8);
    }
  }

  /**
   * Restore stack trace as a string from inside data representation.
   * @return the stack trace as String
   */
  @NonNull
  public String restoreStackTrace () {
    return new String(this.packed ? IOUtils.unpackData(this.stacktrace) : this.stacktrace, UTF8);
  }

  @Override
  public String toString () {
    return restoreStackTrace();
  }
}
