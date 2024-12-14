package com.igormaznitsa.meta.common.interfaces;

/**
 * Supplier allows to throw checked exception.
 *
 * @param <T> type of result
 * @since 1.2.1
 */
@FunctionalInterface
public interface CheckedSupplier<T> {
  T get() throws Exception;
}
