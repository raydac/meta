package com.igormaznitsa.meta.common.interfaces;

import static java.util.Objects.requireNonNull;

/**
 * Function allows throw checked exception.
 *
 * @param <T> type of argument
 * @param <R> type of result
 * @since 1.2.1
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {
  default <V> CheckedFunction<V, R> compose(CheckedFunction<? super V, ? extends T> before)
      throws Exception {
    requireNonNull(before);
    return (V v) -> apply(before.apply(v));
  }

  default <V> CheckedFunction<T, V> andThen(CheckedFunction<? super R, ? extends V> after)
      throws Exception {
    requireNonNull(after);
    return (T t) -> after.apply(apply(t));
  }

  R apply(T t) throws Exception;
}
