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
import com.igormaznitsa.meta.common.annotations.NonNull;

/**
 * Auxiliary methods to get values.
 *
 * @since 1.0
 */
@ThreadSafe
public final class GetUtils {

  private GetUtils () {
  }

  /**
   * Get value and ensure that the value is not null
   *
   * @param <T> type of value
   * @param value the value
   * @param defaultValue the default value to be returned if the value is null
   * @return not null value
   * @throws AssertionError if both the value and the default value are null
   * @since 1.0
   */
  @NonNull
  @Weight (Weight.Unit.LIGHT)
  public static <T> T ensureNonNull (@Nullable final T value, @NonNull final T defaultValue) {
    return value == null ? Assertions.assertNotNull(defaultValue) : value;
  }

  /**
   * Get value if it is not null.
   *
   * @param <T> type of value
   * @param value the value
   * @return the value if it is not null
   * @throws AssertionError if the value is null
   * @since 1.0
   */
  @NonNull
  @Weight (Weight.Unit.LIGHT)
  public static <T> T ensureNonNull (@NonNull final T value) {
    return Assertions.assertNotNull(value);
  }

  /**
   * Find the first non-null value in an array and return that.
   *
   * @param <T> type of value
   * @param objects array to find value
   * @throws AssertionError if the array is null or it doesn't contain a
   * non-null value
   * @return the first non-null value from the array
   * @since 1.0
   */
  @NonNull
  public static <T> T findFirstNonNull (@NonNull final T... objects) {
    for (final T obj : ensureNonNull(objects)) {
      if (obj != null) {
        return obj;
      }
    }
    throw Assertions.fail("Can't find non-null item [" + objects + ']');
  }
}
