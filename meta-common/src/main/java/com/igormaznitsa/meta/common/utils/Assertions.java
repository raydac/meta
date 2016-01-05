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
import java.util.Collection;
import com.igormaznitsa.meta.common.exceptions.AlreadyDisposedError;
import com.igormaznitsa.meta.common.interfaces.Disposable;
import com.igormaznitsa.meta.common.annotations.NonNull;

/**
 * Set of auxiliary methods for assertion.
 *
 * @since 1.0
 */
@ThreadSafe
@Weight (Weight.Unit.LIGHT)
public final class Assertions {

  private Assertions () {
  }

  /**
   * Assert that value is null
   *
   * @param <T> type of the object to check
   * @param object the object to check
   * @return the same input parameter if all is ok
   * @throws AssertionError it will be thrown if the value is not null
   * @since 1.0
   */
  public static <T> T assertNull (@Nullable final T object) {
    if (object != null) {
      throw new AssertionError("Object must be NULL");
    }
    return object;
  }

  /**
   * Assert that value is not null
   *
   * @param <T> type of the object to check
   * @param object the object to check
   * @return the same input parameter if all is ok
   * @throws AssertionError it will be thrown if the value is null
   * @since 1.0
   */
  public static <T> T assertNotNull (@NonNull final T object) {
    if (object == null) {
      throw new AssertionError("Object must not be NULL");
    }
    return object;
  }

  /**
   * Assert that array doesn't contain null value.
   *
   * @param <T> type of the object to check
   * @param array an array to be checked for null value
   * @return the same input parameter if all is ok
   * @throws AssertionError it will be thrown if either array is null or it
   * contains null
   * @since 1.0
   */
  public static <T> T[] assertDoesntContainNull (@NonNull final T[] array) {
    assertNotNull(array);
    for (final T obj : array) {
      if (obj == null) {
        throw new AssertionError("Array must not contain NULL");
      }
    }
    return array;
  }

  /**
   * Assert that collection doesn't contain null value.
   *
   * @param <T> type of collection to check
   * @param collection a collection to be checked for null value
   * @return the same input parameter if all is ok
   * @throws AssertionError it will be thrown if either collection is null or it
   * contains null
   * @since 1.0
   */
  public static <T extends Collection<?>> T assertDoesntContainNull (@NonNull final T collection) {
    assertNotNull(collection);
    for (final Object obj : collection) {
      assertNotNull(obj);
    }
    return collection;
  }

  /**
   * Assert that a disposable object is not disposed.
   * @param <T> type of the object
   * @param disposable disposable object to be checked
   * @return the disposable object if it is not disposed yet
   * @throws AlreadyDisposedError it will be thrown if the object is already disposed;
   * @since 1.0
   */
  public static <T extends Disposable> T assertNotDisposed (@NonNull final T disposable) {
    if (disposable.isDisposed()) {
      throw new AlreadyDisposedError("Object already disposed");
    }
    return disposable;
  }

}
