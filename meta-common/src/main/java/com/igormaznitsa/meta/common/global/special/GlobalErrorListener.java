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

import com.igormaznitsa.meta.common.annotations.NonNull;
import com.igormaznitsa.meta.common.annotations.Nullable;
import com.igormaznitsa.meta.common.annotations.ThreadSafe;
import com.igormaznitsa.meta.common.annotations.Weight;

/**
 * Interface of global processor catching errors detected by the common module methods.
 * 
 * @since 1.0
 */
@ThreadSafe
public interface GlobalErrorListener {
  
  /**
   * The Method will be called if detected some error.
   * @param text text message
   * @param error error object
   * 
   * @since 1.0
   */
  @Weight (Weight.Unit.LIGHT)
  void onDetectedError (@Nullable String text, @NonNull Throwable error);
}