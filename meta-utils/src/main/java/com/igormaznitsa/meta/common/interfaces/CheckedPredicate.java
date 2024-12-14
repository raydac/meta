package com.igormaznitsa.meta.common.interfaces;

import static java.util.Objects.requireNonNull;

/**
 * Checked version of predicate.
 *
 * @param <T> type of argument
 * @since 1.2.1
 */
@FunctionalInterface
public interface CheckedPredicate<T> {
  boolean test(T t) throws Exception;

  default CheckedPredicate<T> and(CheckedPredicate<? super T> other) throws Exception {
    requireNonNull(other);
    return (t) -> test(t) && other.test(t);
  }

  default CheckedPredicate<T> negate() throws Exception {
    return (t) -> !test(t);
  }

  default CheckedPredicate<T> or(CheckedPredicate<? super T> other) throws Exception {
    requireNonNull(other);
    return (t) -> test(t) || other.test(t);
  }
}
