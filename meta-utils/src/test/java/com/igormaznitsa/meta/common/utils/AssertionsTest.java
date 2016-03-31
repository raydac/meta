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

import javax.annotation.Nullable;

import com.igormaznitsa.meta.common.exceptions.InvalidObjectError;

@SuppressWarnings({"rawtypes", "unchecked"})
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

  @Test(expected = AssertionError.class)
  public void testAssertAmong_Null_NotPresented() {
    Assertions.assertAmong(null, 1, 2, 3, 4, 5);
  }

  @Test
  public void testAssertAmong_Null_Presented() {
    assertNull(Assertions.assertAmong(null, 1, 2, null, 3, 4, 5));
  }

  @Test(expected = AssertionError.class)
  public void testAssertAmong_Obj_NotPresented() {
    Assertions.assertAmong("Hello", 1, 2, null, "Yu", 3, 4, 5);
  }

  @Test
  public void testAssertAmong_Obj_Presented() {
    final String test = "Hello";
    final String newStr = new String("Hello");
    final String text = Assertions.assertAmong(test, "h", "kjf", null, newStr, "fsfds", "dfd4", "5");
    assertNotSame(test, text);
    assertSame(text, newStr);
  }

  @Test
  public void testAssertIsValid_Valid() {
    final Validator testValidator = new Validator<Integer>() {
      @Override
      public boolean isValid(@Nullable final Integer object) {
        return object > 300;
      }
    };

    Assertions.assertIsValid(4887, testValidator);
  }

  @Test(expected = InvalidObjectError.class)
  public void testAssertIsValid_Invalid() {
    final Validator testValidator = new Validator<Integer>() {
      @Override
      public boolean isValid(@Nullable final Integer object) {
        return object > 300;
      }
    };

    Assertions.assertIsValid(100, testValidator);
  }

  @Test(expected = AssertionError.class)
  public void testAssertEquals_NullAndNotNull() {
    Assertions.assertEquals(null, "Hello");
  }
  
  @Test(expected = AssertionError.class)
  public void testAssertEquals_NotNullAndNull() {
    Assertions.assertEquals("Hello",null);
  }
  
  @Test(expected = AssertionError.class)
  public void testAssertEquals_NotEquals() {
    Assertions.assertEquals("Hello","Goodbye");
  }
  
  @Test
  public void testAssertEquals_Equals() {
    assertEquals("Hello",Assertions.assertEquals("Hello","Hello"));
  }
  
}
