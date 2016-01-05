/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.meta.common.global.special;

import com.igormaznitsa.meta.common.annotations.Nullable;
import com.igormaznitsa.meta.common.annotations.ThreadSafe;
import com.igormaznitsa.meta.common.annotations.Weight;
import com.igormaznitsa.meta.common.annotations.NonNull;

/**
 * Global service processing all error notifications in the common module methods.
 * 
 * @since 1.0
 */
@ThreadSafe
@Weight (Weight.Unit.LIGHT)
public final class GlobalCommonErrorProcessorService {

  private GlobalCommonErrorProcessorService () {
  }

  @Nullable
  private static volatile GlobalCommonErrorProcessor errorProcessor;

  public static void setErrorProcessor (@Nullable final GlobalCommonErrorProcessor value) {
    errorProcessor = value;
  }

  public static boolean hasErrorProcessor () {
    return errorProcessor != null;
  }

  public static void error (@NonNull final String text, @Nullable final Throwable error) {
    final GlobalCommonErrorProcessor log = errorProcessor;
    if (log != null) {
      log.error(text, error);
    }
  }
}
