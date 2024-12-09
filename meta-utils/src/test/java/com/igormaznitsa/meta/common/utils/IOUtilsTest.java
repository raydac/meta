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
package com.igormaznitsa.meta.common.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class IOUtilsTest {
  
  @Test
  public void testPackUnpackData () {
    final byte [] data = new byte []{1,1,1,1,1,1,2,3,55,22,4,55,6,-11,33,-34,0,0,0,0,0,0,0,0,0,0,0,0,44,66,33};
    final byte [] packed = IOUtils.packData(data);
    assertTrue(packed.length<data.length);
    assertArrayEquals(data, IOUtils.unpackData(packed));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPackUnpackData_ExceptionForWrongFormat () {
    IOUtils.unpackData(new byte[]{1,2,3,4,5});
  }

  @Test
  public void testCloseQuietly() {
    final AtomicInteger callCounter = new AtomicInteger();
    
    final Closeable clb = new Closeable() {
      @Override
      public void close () throws IOException {
        callCounter.incrementAndGet();
        throw new NullPointerException("Some error!");
      }
    };

    IOUtils.closeQuietly(clb);
    assertEquals(1,callCounter.get());
  }
  
}
