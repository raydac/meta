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

import com.igormaznitsa.meta.annotation.Weight;
import java.lang.reflect.Array;
import com.igormaznitsa.meta.annotation.MayContainNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Set of auxiliary methods to process arrays.
 * 
 * @since 1.0
 */
@ThreadSafe
public final class ArrayUtils {
    
    private ArrayUtils(){
    }
  
    /**
     * Join arrays provided as parameters, all arrays must be the same type, null values allowed.
     * @param <T> type of array
     * @param arrays array of arrays to be joined
     * @return all joined arrays as single array
     * @since 1.0
     */
    @Nonnull
    @MayContainNull
    @Weight (Weight.Unit.NORMAL)
    public static <T> T[] joinArrays(@MayContainNull final T[] ... arrays){
      int commonLength = 0;
      for(final T [] array : arrays){
        if (array!=null)
        commonLength += array.length;
      }
      @SuppressWarnings ("unchecked")
      final T[] result = (T[])Array.newInstance(arrays.getClass().getComponentType().getComponentType(), commonLength);
      int position = 0;
      for (final T[] array : arrays) {
        if (array != null) {
          System.arraycopy(array, 0, result, position, array.length);
          position += array.length;
        }
      }
      return result;
    }
  
}
