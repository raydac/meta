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
package com.igormaznitsa.meta.common.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.igormaznitsa.meta.common.exceptions.AlreadyDisposedError;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class DisposableTemplateTest {

  @Test
  public void testIsDisposed () {
    final DisposableTemplate impl = new DisposableTemplate() {
      private static final long serialVersionUID = 2487088576281544111L;

      @Override
      protected void doDispose () {
      }
    };

    assertFalse(impl.isDisposed());
    impl.dispose();
    assertTrue(impl.isDisposed());
  }

  @Test
  public void testAssertNotDisposed () {
    final DisposableTemplate impl = new DisposableTemplate() {
      private static final long serialVersionUID = 2487088576281544111L;

      @Override
      protected void doDispose () {
      }
    };

    impl.assertNotDisposed();
    impl.dispose();
    try {
      impl.assertNotDisposed();
      fail("Must throw error");
    } catch (AlreadyDisposedError ignored) {
    }
  }

  @Test
  public void testCallDoDispose() {
    final AtomicInteger counter = new AtomicInteger();
    
    final DisposableTemplate impl = new DisposableTemplate() {
      private static final long serialVersionUID = 2487088576281544111L;

      @Override
      protected void doDispose () {
        counter.incrementAndGet();
      }
    };

    assertFalse(impl.isDisposed());
    impl.dispose();
    assertTrue(impl.isDisposed());
    assertEquals(1, counter.get());
  }

  @Test
  public void testDispose_ErrorForDoubleDisposing () {
    final DisposableTemplate impl = new DisposableTemplate() {
      private static final long serialVersionUID = 2487088576281544111L;

      @Override
      protected void doDispose () {
      }
    };

    impl.dispose();
    try {
      impl.dispose();
      fail("Must throw error");
    }
    catch (AlreadyDisposedError ex) {
    }
  }

  @Test
  public void testDisposableObjectCounter () {
    assertEquals(0L, DisposableTemplate.getNonDisposedObjectCounter());

    final List<DisposableTemplate> list = new ArrayList<DisposableTemplate>();
    for (int i = 0; i < 1000; i++) {
      final DisposableTemplate impl = new DisposableTemplate() {
        private static final long serialVersionUID = 2487088576281544111L;

        @Override
        protected void doDispose () {
        }
      };
      list.add(impl);
    }
    assertEquals(1000L, DisposableTemplate.getNonDisposedObjectCounter());
    for(final DisposableTemplate t : list){
      t.dispose();
    }
    assertEquals(0L, DisposableTemplate.getNonDisposedObjectCounter());
  }

}
