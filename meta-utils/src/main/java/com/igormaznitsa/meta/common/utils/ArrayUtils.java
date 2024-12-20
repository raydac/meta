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

import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.Weight;
import java.lang.reflect.Array;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Set of auxiliary methods to process arrays.
 *
 * @since 1.0
 */
@ThreadSafe
public final class ArrayUtils {

  /**
   * Empty object array.
   *
   * @since 1.0.2
   */
  public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  /**
   * Empty string array.
   *
   * @since 1.0.2
   */
  public static final String[] EMPTY_STRING_ARRAY = new String[0];
  /**
   * Empty byte array.
   *
   * @since 1.0.2
   */
  public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  /**
   * Empty char array.
   *
   * @since 1.0.2
   */
  public static final char[] EMPTY_CHAR_ARRAY = new char[0];
  /**
   * Empty short array.
   *
   * @since 1.0.2
   */
  public static final short[] EMPTY_SHORT_ARRAY = new short[0];
  /**
   * Empty boolean array.
   *
   * @since 1.0.2
   */
  public static final boolean[] EMPTY_BOOL_ARRAY = new boolean[0];
  /**
   * Empty int array.
   *
   * @since 1.0.2
   */
  public static final int[] EMPTY_INT_ARRAY = new int[0];
  /**
   * Empty long array.
   *
   * @since 1.0.2
   */
  public static final long[] EMPTY_LONG_ARRAY = new long[0];

  private ArrayUtils() {
  }

  /**
   * Join arrays provided as parameters, all arrays must be the same type, null
   * values allowed.
   *
   * @param <T>    type of array
   * @param arrays array of arrays to be joined
   * @return all joined arrays as single array
   * @since 1.0
   */
  @SafeVarargs
  @Nonnull
  @MayContainNull
  @Weight(Weight.Unit.NORMAL)
  public static <T> T[] joinArrays(@MayContainNull final T[]... arrays) {
    int commonLength = 0;
    for (final T[] array : arrays) {
      if (array != null) {
        commonLength += array.length;
      }
    }
    @SuppressWarnings("unchecked") final T[] result =
        (T[]) Array.newInstance(arrays.getClass().getComponentType().getComponentType(),
            commonLength);
    int position = 0;
    for (final T[] array : arrays) {
      if (array != null) {
        System.arraycopy(array, 0, result, position, array.length);
        position += array.length;
      }
    }
    return result;
  }

  /**
   * Append element to the start of an array.
   *
   * @param <T>     type of array elements
   * @param element element to be added into start of array, can be null
   * @param array   target array
   * @return new array where the element on the first position
   * @since 1.1.3
   */
  @Nonnull
  @MayContainNull
  @Weight(Weight.Unit.NORMAL)
  public static <T> T[] append(@Nullable final T element,
                               @MayContainNull @Nonnull final T[] array) {
    @SuppressWarnings("unchecked") final T[] result =
        (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
    System.arraycopy(array, 0, result, 1, array.length);
    result[0] = element;
    return result;
  }

  /**
   * Append elements to the end of an array
   *
   * @param <T>      type of array elements
   * @param array    target array
   * @param elements elements to be added to the end of the target array
   * @return new array where elements are placed in the end
   * @since 1.1.3
   */
  @SafeVarargs
  @Nonnull
  @MayContainNull
  @Weight(Weight.Unit.NORMAL)
  public static <T> T[] append(@MayContainNull @Nonnull final T[] array,
                               @MayContainNull final T... elements) {
    @SuppressWarnings("unchecked") final T[] result =
        (T[]) Array.newInstance(array.getClass().getComponentType(),
            array.length + elements.length);
    System.arraycopy(array, 0, result, 0, array.length);
    int index = array.length;
    for (final T element : elements) {
      result[index++] = element;
    }
    return result;
  }

}
