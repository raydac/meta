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
package com.igormaznitsa.meta.common.utils;

import com.igormaznitsa.meta.common.annotations.Nullable;
import com.igormaznitsa.meta.common.annotations.ThreadSafe;
import com.igormaznitsa.meta.common.annotations.Weight;
import com.igormaznitsa.meta.common.global.special.GlobalCommonErrorProcessorService;
import java.io.Closeable;

/**
 * Auxiliary methods for IO operations.
 * @since 1.0
 */
@ThreadSafe
public final class IOUtils {

  private IOUtils () {
  }

  @Weight (Weight.Unit.LIGHT)
  public static void closeQuetly (@Nullable final Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    }
    catch (Exception ex) {
      GlobalCommonErrorProcessorService.error("Excecption in closeQuetly", ex);
    }
  }
}
