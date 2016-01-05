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

import com.igormaznitsa.meta.common.annotations.Immutable;
import com.igormaznitsa.meta.common.annotations.Nullable;
import com.igormaznitsa.meta.common.annotations.ThreadSafe;
import com.igormaznitsa.meta.common.annotations.Warning;
import com.igormaznitsa.meta.common.annotations.Weight;
import com.igormaznitsa.meta.common.global.special.GlobalCommonErrorProcessorService;
import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.igormaznitsa.meta.common.annotations.MustNotContainNull;
import com.igormaznitsa.meta.common.interfaces.Disposable;
import com.igormaznitsa.meta.common.annotations.NonNull;

/**
 * Auxiliary tool to defer some actions and process them in some point
 in future. it check stack depth and executes only locally (for the stack
 level) defer actions. <b>It works through ThreadLocal so that
 * actions saved separately for every thread.</b>
 *
 * @since 1.0
 * @see ThreadLocal
 */
@ThreadSafe
@Warning ("Productivity depends on stack depth")
public final class Deferrers {

  private Deferrers () {
  }

  /**
   * Class wrapping execute method and stack depth for action.
   *
   * @since 1.0
   */
  @ThreadSafe
  @Immutable
  @Weight (Weight.Unit.VARIABLE)
  public abstract static class Deferred {

    private final int stackDepth;

    public Deferred () {
      this.stackDepth = ThreadUtils.stackDepth() - 1;
    }

    public int getStackDepth () {
      return this.stackDepth;
    }

    public abstract void execute () throws Exception;
  }

  /**
   * Inside registry for defer actions.
   */
  @MustNotContainNull
  private static final ThreadLocal<List<Deferred>> REGISTRY = new ThreadLocal<List<Deferred>>() {
    @Override
    protected List<Deferred> initialValue () {
      return new ArrayList<Deferred>();
    }
  };

  /**
   * Defer some action.
   *
   * @param deferred action to be defer.
   */
  @Weight (Weight.Unit.NORMAL)
  public static void defer (@NonNull final Deferred deferred) {
    REGISTRY.get().add(assertNotNull(deferred));
  }

  /**
   * Defer object containing public close() method.
   *
   * @param closeable an object with close() method.
   * @since 1.0
   */
  @Weight (Weight.Unit.NORMAL)
  public static void deferredClosing (@Nullable final Object closeable) {
    if (closeable != null) {
      defer(new Deferred() {
        @Override
        public void execute () throws Exception {
          try {
            closeable.getClass().getMethod("close").invoke(closeable);
          }
          catch (Exception thr) {
            GlobalCommonErrorProcessorService.error("Error during deferred closing action", thr);
          }
        }
      });
    }
  }

  /**
   * Defer closing of an closeable object.
   *
   * @param closeable an object implements java.io.Closeable
   * @since 1.0
   */
  @Weight (Weight.Unit.NORMAL)
  public static void defer (@Nullable final Closeable closeable) {
    if (closeable != null) {
      defer(new Deferred() {
        @Override
        public void execute () throws Exception {
          IOUtils.closeQuetly(closeable);
        }
      });
    }
  }

  /**
   * Defer execution of some runnable action.
   *
   * @param runnable some runnable action to be executed in future
   * @throws AssertionError if the runnable object is null
   */
  @Weight (Weight.Unit.NORMAL)
  public static void defer (@NonNull final Runnable runnable) {
    assertNotNull(runnable);
    defer(new Deferred() {
      private final Runnable value = runnable;

      @Override
      public void execute () throws Exception {
        this.value.run();
      }
    });

  }

  /**
   * Defer execution of some disposable object.
   *
   * @param disposable some disposable object to be processed.
   * @throws AssertionError if the disposable object is null
   * @see Disposable
   */
  @Weight (Weight.Unit.NORMAL)
  public static void defer (@NonNull final Disposable disposable) {
    assertNotNull(disposable);
    defer(new Deferred() {
      private final Disposable value = disposable;

      @Override
      public void execute () throws Exception {
        this.value.dispose();
      }
    });

  }

  /**
   * Cancel all defer actions globally.
   *
   * @since 1.0
   */
  @Weight (Weight.Unit.LIGHT)
  public static void cancelAllDeferredActionsGlobally () {
    final List<Deferred> list = REGISTRY.get();
    list.clear();
    REGISTRY.remove();
  }

  /**
   * Cancel all defer actions for the current stack depth.
   *
   * @since 1.0
   */
  @Weight (Weight.Unit.LIGHT)
  public static void cancelDeferredActions () {
    final int stackDepth = ThreadUtils.stackDepth();

    final List<Deferred> list = REGISTRY.get();
    final Iterator<Deferred> iterator = list.iterator();

    while (iterator.hasNext()) {
      final Deferred deferred = iterator.next();
      if (deferred.getStackDepth() >= stackDepth) {
        iterator.remove();
      }
    }
    if (list.isEmpty()) {
      REGISTRY.remove();
    }
  }

  /**
   * Process all defer actions for the current stack depth level.
   *
   * @since 1.0
   */
  @Weight (Weight.Unit.VARIABLE)
  public static void processDeferredActions () {
    final int stackDepth = ThreadUtils.stackDepth();

    final List<Deferred> list = REGISTRY.get();
    final Iterator<Deferred> iterator = list.iterator();

    while (iterator.hasNext()) {
      final Deferred deferred = iterator.next();
      if (deferred.getStackDepth() >= stackDepth) {
        try {
          deferred.execute();
        }
        catch (Exception ex) {
          GlobalCommonErrorProcessorService.error("Error during a deferred processor execution", ex);
        }
        finally {
          iterator.remove();
        }
      }
    }
    if (list.isEmpty()) {
      REGISTRY.remove();
    }
  }

  /**
   * Check that presented defer actions for the current thread.
   *
   * @return true if presented, false otherwise
   * @since 1.0
   */
  @Weight (Weight.Unit.NORMAL)
  public static boolean isEmpty () {
    final boolean result = REGISTRY.get().isEmpty();
    if (result) {
      REGISTRY.remove();
    }
    return result;
  }
}
