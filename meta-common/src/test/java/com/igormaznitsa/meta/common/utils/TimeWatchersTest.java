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
import com.igormaznitsa.meta.common.global.special.GlobalErrorListener;
import com.igormaznitsa.meta.common.global.special.GlobalErrorListeners;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class TimeWatchersTest {

  @Before
  public void before(){
    GlobalErrorListeners.clear();
    TimeWatchers.cancelAll();
  }
  
  @After
  public void after(){
    GlobalErrorListeners.clear();
    TimeWatchers.cancelAll();
  }
  
  @Test
  public void testTimeWatcher_OneLevel_InTime () throws Exception {
    TimeWatchers.addWatcher("test1", 1000L,new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeData timeAlertItem) {
        fail("Must not be called");
      }
    });
    Thread.sleep(500L);
    TimeWatchers.checkTime();
  }
  
  @Test
  public void testTimeWatcher_OneLevel_Detected () throws Exception {
    final AtomicInteger detector = new AtomicInteger(0);
    TimeWatchers.addWatcher("test1", 100L,new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeData timeAlertItem) {
        detector.incrementAndGet();
      }
    });
    Thread.sleep(200L);
    TimeWatchers.checkTime();
    TimeWatchers.checkTime();
    assertEquals(1,detector.get());
  }
  
  @Test
  public void testTimeWatcher_OneLevel_NotificationOfGELaboutErrorDuringProcessing () throws Exception {
    final AtomicInteger detector = new AtomicInteger(0);
    
    GlobalErrorListeners.addErrorListener(new GlobalErrorListener() {
      @Override
      public void onDetectedError (final String text, final Throwable error) {
        assertEquals("UUUHHH!", error.getCause().getMessage());
        assertTrue(error instanceof UnexpectedProcessingError);
        detector.incrementAndGet();
      }
    });

    TimeWatchers.addWatcher("test1", 100L, new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = -3206954026782297941L;
      @Override
      public void onTimeAlert (long detectedTimeDelayInMilliseconds, TimeWatchers.TimeData timeViolationAlert) {
        throw new IllegalStateException("UUUHHH!");
      }
    });
    Thread.sleep(200L);
    TimeWatchers.checkTime();
    TimeWatchers.checkTime();
    assertEquals(1,detector.get());
  }
  
  @Test
  public void testTimeWatcher_OneLevel_NotificationOfGELaboutViolation () throws Exception {
    final AtomicInteger detector = new AtomicInteger(0);
    
    GlobalErrorListeners.addErrorListener(new GlobalErrorListener() {
      @Override
      public void onDetectedError (final String text, final Throwable error) {
        assertTrue(error instanceof TimeViolationError);
        assertEquals("test1", error.getMessage());
        detector.incrementAndGet();
      }
    });

    TimeWatchers.addWatcher("test1", 100L);
    Thread.sleep(200L);
    TimeWatchers.checkTime();
    TimeWatchers.checkTime();
    assertEquals(1,detector.get());
  }
  
  @Test
  public void testTimeWatcher_OneLevel_DetectedAndNotDetected () throws Exception {
    final List<String> detector = new ArrayList<String>();
    TimeWatchers.addWatcher("test1", 300L,new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    TimeWatchers.addWatcher("test2", 100L,new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    Thread.sleep(200L);
    TimeWatchers.checkTime();
    TimeWatchers.checkTime();
    assertArrayEquals(new String[]{"test2"},detector.toArray());
  }
  
  private void __TimeWatcher_MultiLevel_2_notDetected(final List<String> detector) throws Exception {
    TimeWatchers.addWatcher("test2n", 200L, new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    __TimeWatcher_MultiLevel_3_Detected(detector);
    Thread.sleep(30L);
    TimeWatchers.checkTime();
  }
  
  private void __TimeWatcher_MultiLevel_3_Detected(final List<String> detector) throws Exception {
    TimeWatchers.addWatcher("test3n", 50L, new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    Thread.sleep(100L);
    TimeWatchers.checkTime();
  }
  
  @Test
  public void testTimeWatcher_MultiLevel () throws Exception {
    final List<String> detector = new ArrayList<String>();
    TimeWatchers.addWatcher("test1", 300L,new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    TimeWatchers.addWatcher("test2", 200L,new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeData timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    __TimeWatcher_MultiLevel_2_notDetected(detector);
    Thread.sleep(150L);
    TimeWatchers.checkTime();
    assertArrayEquals(new String[]{"test3n","test2"},detector.toArray());
  }

  @Test
  public void testIsEmpty(){
    assertTrue(TimeWatchers.isEmpty());
    TimeWatchers.addPoint("hello", new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 5438765546843774527L;
      @Override
      public void onTimeAlert (long detectedTimeDelayInMilliseconds, TimeWatchers.TimeData timeViolationAlert) {
      }
    });
    assertFalse(TimeWatchers.isEmpty());
  }
  
  @Test
  public void testTimePoint_OneLevel(){
    final String [] timePoints = new String [] {"p1","p2","p3"};
    final long [] minTimeDelays = new long [] {300L,200L,100L};
    final AtomicInteger counter = new AtomicInteger(0);
    final TimeWatchers.TimeAlertListener testListener = new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 5438765546843774527L;
      @Override
      public void onTimeAlert (final long detectedTimeDelayInMilliseconds, TimeWatchers.TimeData timeData) {
        assertEquals("point = " + timeData.getAlertMessage(), timePoints[counter.get()], timeData.getAlertMessage());
        assertTrue("point = "+timeData.getAlertMessage(), minTimeDelays[counter.getAndIncrement()]<=detectedTimeDelayInMilliseconds);
      }
    };
    
    TimeWatchers.addPoint("p1", testListener);
    ThreadUtils.silentSleep(100L);
    TimeWatchers.addPoint("p2", testListener);
    ThreadUtils.silentSleep(100L);
    TimeWatchers.addPoint("p3", testListener);
    ThreadUtils.silentSleep(100L);
    TimeWatchers.endPoints();
    
    assertTrue(counter.get()==timePoints.length);
  
    assertTrue(TimeWatchers.isEmpty());
  }
  
  @Test
  public void testCancelAll(){
    final AtomicInteger counter = new AtomicInteger();
    final TimeWatchers.TimeAlertListener listener = new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 5438765546843774527L;
      @Override
      public void onTimeAlert (final long detectedTimeDelayInMilliseconds, TimeWatchers.TimeData timeData) {
        counter.incrementAndGet();
      }
    };
    
    TimeWatchers.addWatcher("test", 100, listener);
    TimeWatchers.addPoint("test2", listener);
    assertFalse(TimeWatchers.isEmpty());
    TimeWatchers.cancelAll();
    
    assertTrue(true);
  }
  
  private void _testCancel(final AtomicInteger counter){
    final TimeWatchers.TimeAlertListener listener = new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 5438765546843774527L;
      @Override
      public void onTimeAlert (final long detectedTimeDelayInMilliseconds, TimeWatchers.TimeData timeData) {
        counter.incrementAndGet();
      }
    };
    
    TimeWatchers.addWatcher("test1", 100, listener);
    TimeWatchers.addPoint("test2", listener);
    TimeWatchers.cancel();
  }

  @Test
  public void testCancel_Multilevel(){
    final AtomicInteger counter = new AtomicInteger();
    final TimeWatchers.TimeAlertListener listener = new TimeWatchers.TimeAlertListener() {
      private static final long serialVersionUID = 5438765546843774527L;
      @Override
      public void onTimeAlert (final long detectedTimeDelayInMilliseconds, TimeWatchers.TimeData timeData) {
        assertTrue(timeData.getAlertMessage().equals("ddd1") || timeData.getAlertMessage().equals("www1"));
        counter.addAndGet(1000);
      }
    };
    
    TimeWatchers.addPoint("ddd1", listener);
    TimeWatchers.addWatcher("www1", 10L, listener);
    ThreadUtils.silentSleep(50L);
    _testCancel(counter);
    assertFalse(TimeWatchers.isEmpty());
    TimeWatchers.checkTime();
    assertTrue(TimeWatchers.isEmpty());
    assertEquals(2000,counter.get());
  }
  
}
