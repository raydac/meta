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
package com.igormaznitsa.meta.common.global.special;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

/**
 *
 * @author igorm
 */
public class GlobalErrorListenersTest {
  
  @Test
  public void testHasListenersAndClear(){
    final GlobalErrorListener listener = Mockito.mock(GlobalErrorListener.class);
    assertFalse(GlobalErrorListeners.hasListeners());
    GlobalErrorListeners.addErrorListener(listener);
    assertTrue(GlobalErrorListeners.hasListeners());
    GlobalErrorListeners.clear();
    assertFalse(GlobalErrorListeners.hasListeners());
    GlobalErrorListeners.fireError("test", new Throwable());
    Mockito.verify(listener,Mockito.never()).onDetectedError(Mockito.anyString(), Mockito.any(Throwable.class));
  }
  
  @Test
  public void testAddFireRemoveListener () {
    final AtomicInteger callCounter = new AtomicInteger();
    
    final GlobalErrorListener listener = new GlobalErrorListener() {
      @Override
      public void onDetectedError (String text, Throwable error) {
        callCounter.incrementAndGet();
      }
    };
        
    GlobalErrorListeners.fireError("test", new Throwable());
    assertEquals(0, callCounter.get());
    GlobalErrorListeners.addErrorListener(listener);
    GlobalErrorListeners.fireError("test", new Throwable());
    assertEquals(1, callCounter.get());
    callCounter.set(0);
    GlobalErrorListeners.removeErrorListener(listener);
    GlobalErrorListeners.fireError("test", new Throwable());
  }
  
}
