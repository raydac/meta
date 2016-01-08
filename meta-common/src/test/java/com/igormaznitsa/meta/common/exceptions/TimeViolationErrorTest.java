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

import com.igormaznitsa.meta.common.utils.TimeWatchers;
import org.junit.Test;
import static org.junit.Assert.*;

public class TimeViolationErrorTest {
  
  @Test
  public void testConstructorAndGetters () {
    final TimeWatchers.TimeData data = new TimeWatchers.TimeData(334,"msg", 6789L, null);
    final TimeViolationError error = new TimeViolationError(1234L, data);
    assertEquals("msg",error.getMessage());
    assertSame(data, error.getData());
    assertEquals(1234L, error.getDetectedTimeInMilliseconds());
    assertEquals(1234L-6789L, error.getDetectedViolationInMilliseconds());
  }
  
}
