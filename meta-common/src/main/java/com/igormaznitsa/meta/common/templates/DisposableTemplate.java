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

import com.igormaznitsa.meta.common.exceptions.AlreadyDisposedError;
import com.igormaznitsa.meta.common.global.special.GlobalCommonErrorProcessorService;
import com.igormaznitsa.meta.common.interfaces.Disposable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Template providing disposable functionality. It makes notification of the GCEPS.
 * 
 * @see #doDispose() 
 * @see GlobalCommonErrorProcessorService
 * @since 1.0
 */
public abstract class DisposableTemplate implements Disposable {
  
  private final AtomicBoolean disposedFlag = new AtomicBoolean();

  public DisposableTemplate () {
  }

  protected void assertNotDisposed(){
    if (this.disposedFlag.get()){
      final AlreadyDisposedError error = new AlreadyDisposedError("Object already disposed");
      GlobalCommonErrorProcessorService.error("Detected call to disposed object", error);
      throw error;
    }
  }
  
  @Override
  public boolean isDisposed () {
    return this.disposedFlag.get();
  }

  @Override
  public final void dispose () {
    if (this.disposedFlag.compareAndSet(false, true)){
      doDispose();
    }else{
      assertNotDisposed();
    }
  }
  
  /**
   * The Template method is called once during disposing.
   */
  protected abstract void doDispose();
}
