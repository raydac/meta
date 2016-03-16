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

import com.igormaznitsa.meta.annotation.Warning;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import static org.junit.Assert.*;

public class ThreadUtilsTest {
  private int testCall1 () {
    return testCall2();
  }

  private int testCall2 () {
    return ThreadUtils.stackDepth();
  }

  @Test
  public void testGetCallStackDepth () {
    assertEquals(ThreadUtils.stackDepth() + 2, testCall1());
  }

  @Test
  public void testSilentSleep_NotInterrupted(){
    final long start = System.currentTimeMillis();
    assertTrue(ThreadUtils.silentSleep(100L));
    assertTrue(System.currentTimeMillis()-start >= 100L);
  }
  
  @Test
  public void testSilentSleep_Interrupted() throws Exception {
    final AtomicBoolean result = new AtomicBoolean(true);
    
    final Thread thr = new Thread(){
      
      @Override
      public void run(){
        result.set(ThreadUtils.silentSleep(1000L));
      }
    };
    
    thr.start();
    Thread.sleep(100L);
    thr.interrupt();
    thr.join();
    assertFalse(result.get());
  }
  
  @Test
  @Warning("The Test depends on its position in the class sources")
  public void testGetCurrentStackElement () {
    final StackTraceElement element = ThreadUtils.stackElement();
    assertEquals(66,element.getLineNumber());
    assertEquals("testGetCurrentStackElement",element.getMethodName());
    assertEquals("com.igormaznitsa.meta.common.utils.ThreadUtilsTest",element.getClassName());
  }
}
