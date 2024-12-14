package com.igormaznitsa.meta.common.interfaces;

import static java.util.Objects.requireNonNull;

/**
 * Consumer allows throw checked exception.
 *
 * @param <T> type of argument
 * @since 1.2.1
 */
@FunctionalInterface
public interface CheckedConsumer<T> {

  default CheckedConsumer<T> andThen(CheckedConsumer<? super T> after) {
    requireNonNull(after);
    return (T t) -> {
      accept(t);
      after.accept(t);
    };
  }

  void accept(T t) throws Exception;
}
