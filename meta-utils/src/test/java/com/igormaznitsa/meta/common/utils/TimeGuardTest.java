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

import com.igormaznitsa.meta.common.exceptions.TimeViolationError;
import com.igormaznitsa.meta.common.exceptions.UnexpectedProcessingError;
import com.igormaznitsa.meta.common.exceptions.MetaErrorListeners;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class TimeGuardTest {

  @Before
  public void before(){
    MetaErrorListeners.clear();
    TimeGuard.cancelAll();
  }
  
  @After
  public void after(){
    MetaErrorListeners.clear();
    TimeGuard.cancelAll();
  }
  
  @Test
  public void testTimeWatcher_OneLevel_InTime () throws Exception {
    TimeGuard.addGuard("test1", 1000L,new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeGuard.TimeData timeAlertItem) {
        fail("Must not be called");
      }
    });
    Thread.sleep(500L);
    TimeGuard.check();
  }
  
  @Test
  public void testTimeWatcher_OneLevel_Detected () throws Exception {
    final AtomicInteger detector = new AtomicInteger(0);
    TimeGuard.addGuard("test1", 100L,new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeGuard.TimeData timeAlertItem) {
        detector.incrementAndGet();
      }
    });
    Thread.sleep(200L);
    TimeGuard.check();
    TimeGuard.check();
    assertEquals(1,detector.get());
  }
  
  @Test
  public void testGuard_OneLevel_NotificationOfGELaboutErrorDuringProcessing () throws Exception {
    final AtomicInteger detector = new AtomicInteger(0);
    
    MetaErrorListeners.addErrorListener((text, error) -> {
      assertEquals("UUUHHH!", error.getCause().getMessage());
      assertTrue(error instanceof UnexpectedProcessingError);
      detector.incrementAndGet();
    });

    TimeGuard.addGuard("test1", 100L, new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = -3206954026782297941L;
      @Override
      public void onTimeAlert (long detectedTimeDelayInMilliseconds, TimeGuard.TimeData timeViolationAlert) {
        throw new IllegalStateException("UUUHHH!");
      }
    });
    Thread.sleep(200L);
    TimeGuard.check();
    TimeGuard.check();
    assertEquals(1,detector.get());
  }
  
  @Test
  public void testGuard_OneLevel_NotificationOfGEL_aboutViolation() throws Exception {
    final AtomicInteger detector = new AtomicInteger(0);
    
    MetaErrorListeners.addErrorListener((text, error) -> {
      assertTrue(error instanceof TimeViolationError);
      assertEquals("test1", error.getMessage());
      detector.incrementAndGet();
    });

    TimeGuard.addGuard("test1", 100L);
    Thread.sleep(200L);
    TimeGuard.check();
    TimeGuard.check();
    assertEquals(1,detector.get());
  }
  
  @Test
  public void testGuard_OneLevel_DetectedAndNotDetected () throws Exception {
    final List<String> detector = new ArrayList<>();
    TimeGuard.addGuard("test1", 300L,new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeGuard.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    TimeGuard.addGuard("test2", 100L,new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeGuard.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    Thread.sleep(200L);
    TimeGuard.check();
    TimeGuard.check();
    assertArrayEquals(new String[]{"test2"},detector.toArray());
  }
  
  private void __TimeWatcher_MultiLevel_2_notDetected(final List<String> detector) throws Exception {
    TimeGuard.addGuard("test2n", 200L, new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeGuard.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    __TimeWatcher_MultiLevel_3_Detected(detector);
    Thread.sleep(30L);
    TimeGuard.check();
  }
  
  private void __TimeWatcher_MultiLevel_3_Detected(final List<String> detector) throws Exception {
    TimeGuard.addGuard("test3n", 50L, new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeGuard.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    Thread.sleep(100L);
    TimeGuard.check();
  }
  
  @Test
  public void testGuard_MultiLevel () throws Exception {
    final List<String> detector = new ArrayList<>();
    TimeGuard.addGuard("test1", 300L,new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeGuard.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    TimeGuard.addGuard("test2", 200L,new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeGuard.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    __TimeWatcher_MultiLevel_2_notDetected(detector);
    Thread.sleep(150L);
    TimeGuard.check();
    assertArrayEquals(new String[]{"test3n","test2"},detector.toArray());
  }

  @Test
  public void testIsEmpty(){
    assertTrue(TimeGuard.isEmpty());
    TimeGuard.addPoint("hello", new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 5438765546843774527L;
      @Override
      public void onTimeAlert (long detectedTimeDelayInMilliseconds, TimeGuard.TimeData timeViolationAlert) {
      }
    });
    assertFalse(TimeGuard.isEmpty());
  }
  
  @Test
  public void testPoint_ForName(){

    final AtomicInteger ai1 = new AtomicInteger();
    final AtomicInteger ai2 = new AtomicInteger();
    
    final TimeGuard.TimeAlertListener listenerPnt1 = new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = -2291183279100986316L;
      @Override
      public void onTimeAlert (long detectedTimeDelayInMilliseconds, TimeGuard.TimeData timeData) {
        assertEquals("pnt1",timeData.getAlertMessage());
        ai1.incrementAndGet();
        assertTrue(detectedTimeDelayInMilliseconds >= 150L);
      }
    };
    
    final TimeGuard.TimeAlertListener listenerPnt2 = new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = -2241183279100986316L;
      @Override
      public void onTimeAlert (long detectedTimeDelayInMilliseconds, TimeGuard.TimeData timeData) {
        assertEquals("pnt2",timeData.getAlertMessage());
        ai2.incrementAndGet();
        assertTrue(detectedTimeDelayInMilliseconds>=100L && detectedTimeDelayInMilliseconds<150L);
      }
    };
    
    
    TimeGuard.addPoint("pnt1", listenerPnt1);
    TimeGuard.addPoint("pnt2", listenerPnt2);
    
    ThreadUtils.silentSleep(100L);
    
    TimeGuard.checkPoint("pnt2");

    ThreadUtils.silentSleep(50L);

    TimeGuard.checkPoint("pnt1");
    
    assertEquals(1,ai1.get());
    assertEquals(1,ai2.get());
    
    assertTrue(TimeGuard.isEmpty());
  }
  
  @Test
  public void testPoint_OneLevel(){
    final String [] timePoints = new String [] {"p1","p2","p3"};
    final long [] minTimeDelays = new long [] {300L,200L,100L};
    final AtomicInteger counter = new AtomicInteger(0);
    final TimeGuard.TimeAlertListener testListener = new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 5438765546843774527L;
      @Override
      public void onTimeAlert (final long detectedTimeDelayInMilliseconds, TimeGuard.TimeData timeData) {
        assertEquals("point = " + timeData.getAlertMessage(), timePoints[counter.get()], timeData.getAlertMessage());
        assertTrue("point = "+timeData.getAlertMessage(), minTimeDelays[counter.getAndIncrement()]<=detectedTimeDelayInMilliseconds);
      }
    };
    
    TimeGuard.addPoint("p1", testListener);
    ThreadUtils.silentSleep(100L);
    TimeGuard.addPoint("p2", testListener);
    ThreadUtils.silentSleep(100L);
    TimeGuard.addPoint("p3", testListener);
    ThreadUtils.silentSleep(100L);
    TimeGuard.checkPoints();

    assertEquals(counter.get(), timePoints.length);
  
    assertTrue(TimeGuard.isEmpty());
  }
  
  @Test
  public void testCancelAll(){
    final AtomicInteger counter = new AtomicInteger();
    final TimeGuard.TimeAlertListener listener = new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 5438765546843774527L;
      @Override
      public void onTimeAlert (final long detectedTimeDelayInMilliseconds, TimeGuard.TimeData timeData) {
        counter.incrementAndGet();
      }
    };
    
    TimeGuard.addGuard("test", 100, listener);
    TimeGuard.addPoint("test2", listener);
    assertFalse(TimeGuard.isEmpty());
    TimeGuard.cancelAll();
    
    assertTrue(true);
  }
  
  private void _testCancel(final AtomicInteger counter){
    final TimeGuard.TimeAlertListener listener = new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 5438765546843774527L;
      @Override
      public void onTimeAlert (final long detectedTimeDelayInMilliseconds, TimeGuard.TimeData timeData) {
        counter.incrementAndGet();
      }
    };
    
    TimeGuard.addGuard("test1", 100, listener);
    TimeGuard.addPoint("test2", listener);
    TimeGuard.cancel();
  }

  @Test
  public void testCancel_Multilevel(){
    final AtomicInteger counter = new AtomicInteger();
    final TimeGuard.TimeAlertListener listener = new TimeGuard.TimeAlertListener() {
      private static final long serialVersionUID = 5438765546843774527L;
      @Override
      public void onTimeAlert (final long detectedTimeDelayInMilliseconds, TimeGuard.TimeData timeData) {
        assertTrue(timeData.getAlertMessage().equals("ddd1") || timeData.getAlertMessage().equals("www1"));
        counter.addAndGet(1000);
      }
    };
    
    TimeGuard.addPoint("ddd1", listener);
    TimeGuard.addGuard("www1", 10L, listener);
    ThreadUtils.silentSleep(50L);
    _testCancel(counter);
    assertFalse(TimeGuard.isEmpty());
    TimeGuard.check();
    assertTrue(TimeGuard.isEmpty());
    assertEquals(2000,counter.get());
  }
  
}
