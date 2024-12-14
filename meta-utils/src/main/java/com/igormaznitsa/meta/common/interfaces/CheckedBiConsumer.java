package com.igormaznitsa.meta.common.interfaces;

import java.util.Objects;

/**
 * BiConsumer allows throw checked exception.
 *
 * @param <T>
 * @since 1.2.1
 */
@FunctionalInterface
public interface CheckedBiConsumer<T, U> {

  default CheckedBiConsumer<T, U> andThen(CheckedBiConsumer<? super T, ? super U> after) {
    Objects.requireNonNull(after);
    Objects.requireNonNull(after);

    return (l, r) -> {
      accept(l, r);
      after.accept(l, r);
    };
  }

  void accept(T t, U u) throws Exception;
}
