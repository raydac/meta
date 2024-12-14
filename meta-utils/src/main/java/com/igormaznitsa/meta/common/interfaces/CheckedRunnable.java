package com.igormaznitsa.meta.common.interfaces;

/**
 * Checked version of runnable which can throw checked exception.
 *
 * @since 1.2.1
 */
@FunctionalInterface
public interface CheckedRunnable {

  void run() throws Exception;

}
