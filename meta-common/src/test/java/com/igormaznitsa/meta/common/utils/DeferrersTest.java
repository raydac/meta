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

public class DeferrersTest {
  
  private static class TestDeferrer extends Deferrers.Deferred {
    private final AtomicInteger value;
    private final int id;
    private final List<Integer> order;
    
    public TestDeferrer(final AtomicInteger value, final List<Integer> order, final int id){
      super();
      this.value = value;
      this.order = order;
      this.id = id;
    }

    @Override
    public void execute () throws Exception {
      this.value.decrementAndGet();
      this.order.add(id);
    }
  }
  
  private static int [] toArray(final List<Integer> value){
    final int [] result = new int[value.size()];
    for(int i=0;i<value.size();i++){
      result[i] = value.get(i);
    }
    return result;
  } 
  
  @Test
  public void testEnd_OneMethod () {
    final AtomicInteger value = new AtomicInteger(1);
    final List<Integer> order = new ArrayList<Integer>();
    
    Deferrers.defer(new TestDeferrer(value,order,1));
    Deferrers.processDeferredActions();
    
    assertArrayEquals(new int[]{1}, toArray(order));
    assertEquals(0, value.get());
  }
  
  @Test
  public void testProcessDeferredActions_TwoMethods () {
    final AtomicInteger value = new AtomicInteger(2);
    final List<Integer> order = new ArrayList<Integer>();
    
    Deferrers.defer(new TestDeferrer(value,order,1));
    Deferrers.defer(new TestDeferrer(value,order,2));
    Deferrers.processDeferredActions();
    
    assertEquals(0, value.get());
    assertArrayEquals(new int[]{1,2}, toArray(order));
  }
  
  @Test
  public void testCancelDeferredActions () {
    final AtomicInteger value = new AtomicInteger(2);
    final List<Integer> order = new ArrayList<Integer>();
    
    Deferrers.defer(new TestDeferrer(value,order,1));
    Deferrers.defer(new TestDeferrer(value,order,2));
    Deferrers.cancelDeferredActions();
    Deferrers.processDeferredActions();
    
    assertEquals(2, value.get());
    assertTrue(order.isEmpty());
  }
  
  @Test
  public void testProcessDeferredActions_MultiLevel () {
    final AtomicInteger value = new AtomicInteger(5);
    final List<Integer> order = new ArrayList<Integer>();
    
    Deferrers.defer(new TestDeferrer(value,order,1));
    Deferrers.defer(new TestDeferrer(value,order,2));

    secondLevel(value,2,order);

    Deferrers.processDeferredActions();
    
    assertEquals(0, value.get());
    assertArrayEquals(new int[]{5,3,4,1,2}, toArray(order));
  }
  
  private void secondLevel(final AtomicInteger value, final int num, final List<Integer> order){
    Deferrers.defer(new TestDeferrer(value,order,3));
    Deferrers.defer(new TestDeferrer(value,order,4));
    thirdLevel(value, 4, order);
    Deferrers.processDeferredActions();
    assertEquals(num, value.get());
  }
  
  private void thirdLevel(final AtomicInteger value, final int num, final List<Integer> order){
    Deferrers.defer(new TestDeferrer(value,order,5));
    Deferrers.processDeferredActions();
    assertEquals(num, value.get());
  }
  
}
