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

import com.igormaznitsa.meta.common.exceptions.AlreadyDisposedError;
import com.igormaznitsa.meta.common.interfaces.Disposable;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

@SuppressWarnings ("rawtypes")
public class AssertionsTest {

  @Test
  public void testAssertNotNull_NotNull(){
    final Object notNull = new Object();
    assertSame(notNull,Assertions.assertNotNull(notNull));
  }
  
  @Test(expected = AssertionError.class)
  public void testAssertNotNull_Null(){
    Assertions.assertNotNull(null);
  }
  
  @Test(expected = AssertionError.class)
  public void testAssertNull_NotNull(){
    final Object notNull = new Object();
    Assertions.assertNull(notNull);
  }
  
  @Test
  public void testAssertNull_Null(){
    assertNull(Assertions.assertNull(null));
  }
  
  @Test
  public void testAssertDoesntContainNull_array_doesntContain(){
    final String [] array = new String[]{"Hello","World","Universe"};
    assertSame(array,Assertions.assertDoesntContainNull(array));
  }
  
  @Test(expected = AssertionError.class)
  public void testAssertDoesntContainNull_array_contains(){
    final String [] array = new String[]{"Hello",null,"World","Universe"};
    Assertions.assertDoesntContainNull(array);
  }
  
  @Test
  public void testAssertDoesntContainNull_collection_doesntContain(){
    final List list = Arrays.asList("Hello", "World", "Universe");
    assertSame(list,Assertions.assertDoesntContainNull(list));
  }
  
  @Test(expected = AssertionError.class)
  public void testAssertDoesntContainNull_collection_contains(){
    final List list = Arrays.asList("Hello",null,"World","Universe");
    Assertions.assertDoesntContainNull(list);
  }

  @Test
  public void testAssertTrue(){
    Assertions.assertTrue("test",true);
    try{
      Assertions.assertTrue("test", false);
      fail("Must throw exception");
    }catch(AssertionError exx){
      
    }
  }
  
  @Test
  public void testAssertFalse(){
    Assertions.assertFalse("test",false);
    try{
      Assertions.assertFalse("test", true);
      fail("Must throw exception");
    }catch(AssertionError exx){
      
    }
  }

  @Test
  public void testAssertNotDisposed_NotDisposed() {
    final Disposable obj = new Disposable() {
      @Override
      public boolean isDisposed () {
        return false;
      }

      @Override
      public void dispose () {
        fail();
      }
    };
    
    Assertions.assertNotDisposed(obj);
  }
  
  @Test
  public void testAssertNotDisposed_Disposed() {
    final Disposable obj = new Disposable() {
      @Override
      public boolean isDisposed () {
        return true;
      }

      @Override
      public void dispose () {
        fail();
      }
    };
    try{
      Assertions.assertNotDisposed(obj);
      fail();
    }catch(AlreadyDisposedError ex){
      
    }
  }
  
}
