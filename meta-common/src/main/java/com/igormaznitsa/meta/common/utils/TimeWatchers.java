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
import com.igormaznitsa.meta.common.exceptions.TimeViolationError;
import com.igormaznitsa.meta.common.global.special.GlobalErrorListeners;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.igormaznitsa.meta.common.annotations.MustNotContainNull;
import com.igormaznitsa.meta.common.annotations.NonNull;

/**
 * Allows to detect violations of execution time for blocks. It works separately
 * for every Thread with ThreadLocal and check stack depth to be informed about
 * current operation level.
 *
 * @since 1.0
 */
@ThreadSafe
@Weight (Weight.Unit.VARIABLE)
@Warning ("Productivity depends on stack depth")
public final class TimeWatchers {

  private TimeWatchers () {
  }

  /**
   * Interface for any object to be informed about time bound violation.
   *
   * @since 1.0
   */
  @ThreadSafe
  @Weight (Weight.Unit.EXTRALIGHT)
  public interface TimeAlertProcessor extends Serializable {

    /**
     * Process time bound violation.
     *
     * @param detectedTimeDelayInMilliseconds detected time delay in
     * milliseconds
     * @param timeAlertItem data container with values provided during
     * registration of time watching.
     * @since 1.0
     */
    void onTimeAlert (long detectedTimeDelayInMilliseconds, @NonNull TimeAlertItem timeAlertItem);
  }

  /**
   * Data container for time watching action.
   *
   * @since 1.0
   */
  @ThreadSafe
  @Immutable
  @Weight (Weight.Unit.VARIABLE)
  public static final class TimeAlertItem implements Serializable {

    private static final long serialVersionUID = -2417415112571257128L;

    /**
     * Contains detected stack depth for creation.
     */
    private final int detectedStackDepth;

    /**
     * Max allowed time delay in milliseconds.
     */
    private final long maxAllowedDelayInMilliseconds;

    /**
     * The Creation time of the data container in milliseconds.
     */
    private final long creationTimeInMilliseconds;

    /**
     * The Alert message to be provided into log or somewhere else if detected
     * violation.
     */
    private final String alertMessage;

    /**
     * Some provided processor to be called for violation.
     */
    private final TimeAlertProcessor alertProcessor;

    public TimeAlertItem (@Nullable final String alertMessage, final long maxAllowedDelayInMilliseconds, @Nullable final TimeAlertProcessor alertProcessor) {
      this.detectedStackDepth = ThreadUtils.stackDepth() - 1;
      this.maxAllowedDelayInMilliseconds = maxAllowedDelayInMilliseconds;
      this.creationTimeInMilliseconds = System.currentTimeMillis();
      this.alertMessage = alertMessage;
      this.alertProcessor = alertProcessor;
    }

    /**
     * Get alert processor if provided
     *
     * @return the provided processor or null
     */
    @Nullable
    public TimeAlertProcessor getAlertProcessor () {
      return this.alertProcessor;
    }

    /**
     * Get the alert message.
     *
     * @return defined alert message or null.
     */
    @Nullable
    public String getAlertMessage () {
      return this.alertMessage;
    }

    /**
     * Get the detected stack depth during the container creation.
     *
     * @return the detected stack depth
     */
    public int getDetectedStackDepth () {
      return this.detectedStackDepth;
    }

    /**
     * Get the creation time of the container.
     *
     * @return the creation time in milliseconds
     */
    public long getCreationTimeInMilliseconds () {
      return this.creationTimeInMilliseconds;
    }

    /**
     * Get defined max allowed time delay in milliseconds.
     *
     * @return the max allowed time delay in milliseconds
     */
    public long getMaxAllowedDelayInMilliseconds () {
      return this.maxAllowedDelayInMilliseconds;
    }
  }

  /**
   * Inside thread local storage of registered processors.
   * @since 1.0
   */
  @MustNotContainNull
  private static final ThreadLocal<List<TimeAlertItem>> REGISTRY = new ThreadLocal<List<TimeAlertItem>>() {
    @Override
    protected List<TimeAlertItem> initialValue () {
      return new ArrayList<TimeAlertItem>();
    }
  };

  @Nullable
  private static volatile TimeAlertProcessor globalAlertProcessor;

  /**
   * Set the global processor of time violations.
   * @param alertProcessor alert processor to be notified about <b>all non-processed violations</b>
   * @since 1.0
   */
  public static void setGlobalAlertProcessor (@Nullable final TimeAlertProcessor alertProcessor) {
    globalAlertProcessor = alertProcessor;
  }

  /**
   * Check that provided global alert processor.
   * @return true if such processor is defined, false otherwise
   * @since 1.0
   */
  public static boolean hasDefinedGlobalAlertProcessor () {
    return globalAlertProcessor != null;
  }

  /**
   * Add a time watcher. The Global alert processor will be notified (if it is defined)
   * @param alertMessage message for time violation
   * @param maxAllowedDelayInMilliseconds max allowed delay in milliseconds for executing block
   * @see #checkTimeWatchers() 
   * @see #cancelAllTimeWatchersGlobally() 
   * @see #setGlobalAlertProcessor(com.igormaznitsa.meta.common.utils.TimeWatchers.TimeAlertProcessor) 
   * @since 1.0
   */
  public static void addTimeWatcher (@Nullable final String alertMessage, final long maxAllowedDelayInMilliseconds) {
    addTimeWatcher(alertMessage, maxAllowedDelayInMilliseconds, null);
  }

  /**
   * Add a time watcher and provide processor of time violation.
   * @param alertMessage message for time violation
   * @param maxAllowedDelayInMilliseconds max allowed delay in milliseconds for executing block
   * @param alertProcessor alert processor to be notified, if it is null then the global one will get notification
   * @see #checkTimeWatchers()
   * @see #cancelAllTimeWatchersGlobally()
   * @see #setGlobalAlertProcessor(com.igormaznitsa.meta.common.utils.TimeWatchers.TimeAlertProcessor)
   * @since 1.0
   */
  public static void addTimeWatcher (@Nullable final String alertMessage, final long maxAllowedDelayInMilliseconds, @Nullable final TimeAlertProcessor alertProcessor) {
    final List<TimeAlertItem> list = REGISTRY.get();
    list.add(new TimeAlertItem(alertMessage, maxAllowedDelayInMilliseconds, alertProcessor));
  }

  /**
   * Cancel all time watchers globally.
   * @since 1.0
   */
  public static void cancelAllTimeWatchersGlobally () {
    final List<TimeAlertItem> list = REGISTRY.get();
    list.clear();
    REGISTRY.remove();
  }

  /**
   * Cancel all time watchers for the current stack level.
   * @see #cancelAllTimeWatchersGlobally() 
   * @since 1.0
   */
  public static void cancelTimeWatchers () {
    final int stackDepth = ThreadUtils.stackDepth();

    final List<TimeAlertItem> list = REGISTRY.get();
    final Iterator<TimeAlertItem> iterator = list.iterator();

    while (iterator.hasNext()) {
      final TimeAlertItem timeWatchItem = iterator.next();
      if (timeWatchItem.getDetectedStackDepth() >= stackDepth) {
        iterator.remove();
      }
    }
    if (list.isEmpty()) {
      REGISTRY.remove();
    }
  }

  /**
   * Check all registered time watchers for time bound violations.
   * @see #addTimeWatcher(java.lang.String, long) 
   * @see #addTimeWatcher(java.lang.String, long, com.igormaznitsa.meta.common.utils.TimeWatchers.TimeAlertProcessor) 
   * @since 1.0
   */
  public static void checkTimeWatchers () {
    final long time = System.currentTimeMillis();

    final int stackDepth = ThreadUtils.stackDepth();

    final List<TimeAlertItem> list = REGISTRY.get();
    final Iterator<TimeAlertItem> iterator = list.iterator();

    while (iterator.hasNext()) {
      final TimeAlertItem timeWatchItem = iterator.next();
      if (timeWatchItem.getDetectedStackDepth() >= stackDepth) {
        try {
          final long detectedDelay = time - timeWatchItem.getCreationTimeInMilliseconds();
          if (detectedDelay > timeWatchItem.getMaxAllowedDelayInMilliseconds()) {
            final TimeAlertProcessor processor = timeWatchItem.getAlertProcessor() == null ? globalAlertProcessor : timeWatchItem.getAlertProcessor();
            if (processor == null) {
              GlobalErrorListeners.error("Detected time violation without any processor", new TimeViolationError(detectedDelay, timeWatchItem));
            }else{
              try {
                processor.onTimeAlert(detectedDelay, timeWatchItem);
              }
              catch (Exception ex) {
                GlobalErrorListeners.error("Error during time alert processing", ex);
              }
            }
          }
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

}
