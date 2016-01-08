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
import com.igormaznitsa.meta.common.utils.Assertions;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Global service processing all onDetectedError notifications in the common module methods.
 * 
 * @since 1.0
 */
@ThreadSafe
@Weight (Weight.Unit.NORMAL)
public final class GlobalErrorListeners {

  private static final List<GlobalErrorListener> ERROR_LISTENERS = new CopyOnWriteArrayList<GlobalErrorListener>();

  private GlobalErrorListeners () {
  }

  /**
   * Remove all listeners.
   * @since 1.0
   */
  public static void clear () {
    ERROR_LISTENERS.clear();
  }

  /**
   * Add new fireError listener for global fireError events.
   * @param value listener to be added
   * @since 1.0
   */
  public static void addErrorListener (@NonNull final GlobalErrorListener value) {
    ERROR_LISTENERS.add(Assertions.assertNotNull(value));
  }

  /**
   * Remove listener.
   * @param value listener to be removed
   * @since 1.0
   */
  public static void removeErrorListener (@NonNull final GlobalErrorListener value) {
    ERROR_LISTENERS.remove(Assertions.assertNotNull(value));
  }

  /**
   * Check that there are registered listeners.
   * @return true if presented listeners for global fireError events, false otherwise
   * @since 1.0
   */
  public static boolean hasErrorListeners () {
    return !ERROR_LISTENERS.isEmpty();
  }

  /**
   * Send notifications to all listeners.
   * @param text message text
   * @param error error object
   * @since 1.0
   */
  @Weight(Weight.Unit.VARIABLE)
  public static void fireError (@NonNull final String text, @NonNull final Throwable error) {
    for(final GlobalErrorListener p : ERROR_LISTENERS){
      p.onDetectedError(text, error);
    }
  }
}
