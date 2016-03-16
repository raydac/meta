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
package com.igormaznitsa.meta.common.exceptions;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

/**
 *
 * @author igorm
 */
public class MetaErrorListenersTest {
  
  @Test
  public void testHasListenersAndClear(){
    final MetaErrorListener listener = Mockito.mock(MetaErrorListener.class);
    assertFalse(MetaErrorListeners.hasListeners());
    MetaErrorListeners.addErrorListener(listener);
    assertTrue(MetaErrorListeners.hasListeners());
    MetaErrorListeners.clear();
    assertFalse(MetaErrorListeners.hasListeners());
    MetaErrorListeners.fireError("test", new Throwable());
    Mockito.verify(listener,Mockito.never()).onDetectedError(Mockito.anyString(), Mockito.any(Throwable.class));
  }
  
  @Test
  public void testAddFireRemoveListener () {
    final AtomicInteger callCounter = new AtomicInteger();
    
    final MetaErrorListener listener = new MetaErrorListener() {
      @Override
      public void onDetectedError (String text, Throwable error) {
        callCounter.incrementAndGet();
      }
    };
        
    MetaErrorListeners.fireError("test", new Throwable());
    assertEquals(0, callCounter.get());
    MetaErrorListeners.addErrorListener(listener);
    MetaErrorListeners.fireError("test", new Throwable());
    assertEquals(1, callCounter.get());
    callCounter.set(0);
    MetaErrorListeners.removeErrorListener(listener);
    MetaErrorListeners.fireError("test", new Throwable());
  }
  
}
