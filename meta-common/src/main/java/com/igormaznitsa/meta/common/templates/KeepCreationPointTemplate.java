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
package com.igormaznitsa.meta.common.templates;

import com.igormaznitsa.meta.common.annotations.NonNull;
import com.igormaznitsa.meta.common.annotations.ThreadSafe;
import com.igormaznitsa.meta.common.annotations.Warning;
import com.igormaznitsa.meta.common.utils.CallTrace;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Allows to keep information about point where instance of the class was created.
 * 
 * @since 1.0
 * @see CallTrace
 */
public abstract class KeepCreationPointTemplate {
  
  private static final Map<KeepCreationPointTemplate,CallTrace> REGISTRY = Collections.synchronizedMap(new WeakHashMap<KeepCreationPointTemplate, CallTrace>());

  /**
   * The Constructor.
   */
  @Warning("Must be called in successors, must not be called through constructor chain else you will see your constructors in stack trace!")
  public KeepCreationPointTemplate(){
    REGISTRY.put(this, new CallTrace(5, true, CallTrace.EOL_LINUX));
  }

  /**
   * Get the creation point stack trace for the instance.
   * @return the creation point stack trace
   */
  @NonNull
  @ThreadSafe
  public final CallTrace getCreationPoint(){
    return REGISTRY.get(this);
  }

}
