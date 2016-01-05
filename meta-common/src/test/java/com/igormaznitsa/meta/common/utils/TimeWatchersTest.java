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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class TimeWatchersTest {

  @Before
  public void before(){
    TimeWatchers.cancelAllTimeWatchersGlobally();
  }
  
  @Test
  public void testTimeWatcher_OneLevel_InTime () throws Exception {
    TimeWatchers.addTimeWatcher("test1", 1000L,new TimeWatchers.TimeAlertProcessor() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeAlertItem timeAlertItem) {
        fail("Must not be called");
      }
    });
    Thread.sleep(500L);
    TimeWatchers.checkTimeWatchers();
  }
  
  @Test
  public void testTimeWatcher_OneLevel_Detected () throws Exception {
    final AtomicInteger detector = new AtomicInteger(0);
    TimeWatchers.addTimeWatcher("test1", 100L,new TimeWatchers.TimeAlertProcessor() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeAlertItem timeAlertItem) {
        detector.incrementAndGet();
      }
    });
    Thread.sleep(200L);
    TimeWatchers.checkTimeWatchers();
    TimeWatchers.checkTimeWatchers();
    assertEquals(1,detector.get());
  }
  
  @Test
  public void testTimeWatcher_OneLevel_DetectedAndNotDetected () throws Exception {
    final List<String> detector = new ArrayList<String>();
    TimeWatchers.addTimeWatcher("test1", 300L,new TimeWatchers.TimeAlertProcessor() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeAlertItem timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    TimeWatchers.addTimeWatcher("test2", 100L,new TimeWatchers.TimeAlertProcessor() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeAlertItem timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    Thread.sleep(200L);
    TimeWatchers.checkTimeWatchers();
    TimeWatchers.checkTimeWatchers();
    assertArrayEquals(new String[]{"test2"},detector.toArray());
  }
  
  private void __TimeWatcher_MultiLevel_2_notDetected(final List<String> detector) throws Exception {
    TimeWatchers.addTimeWatcher("test2n", 200L, new TimeWatchers.TimeAlertProcessor() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeAlertItem timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    __TimeWatcher_MultiLevel_3_Detected(detector);
    Thread.sleep(30L);
    TimeWatchers.checkTimeWatchers();
  }
  
  private void __TimeWatcher_MultiLevel_3_Detected(final List<String> detector) throws Exception {
    TimeWatchers.addTimeWatcher("test3n", 50L, new TimeWatchers.TimeAlertProcessor() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeAlertItem timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    Thread.sleep(100L);
    TimeWatchers.checkTimeWatchers();
  }
  
  @Test
  public void testTimeWatcher_MultiLevel () throws Exception {
    final List<String> detector = new ArrayList<String>();
    TimeWatchers.addTimeWatcher("test1", 300L,new TimeWatchers.TimeAlertProcessor() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeAlertItem timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    TimeWatchers.addTimeWatcher("test2", 200L,new TimeWatchers.TimeAlertProcessor() {
      private static final long serialVersionUID = 6599462085266065454L;
      @Override
      public void onTimeAlert (long delayInMilliseconds, TimeWatchers.TimeAlertItem timeAlertItem) {
        detector.add(timeAlertItem.getAlertMessage());
      }
    });
    __TimeWatcher_MultiLevel_2_notDetected(detector);
    Thread.sleep(150L);
    TimeWatchers.checkTimeWatchers();
    assertArrayEquals(new String[]{"test3n","test2"},detector.toArray());
  }
  
}
