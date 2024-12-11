package com.igormaznitsa.meta.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows marking methods that should not be called from methods in the same class, only external call is allowed.
 *
 * @since 1.2.1
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface DisableSelfInvocation {
  /**
   * Field allows to add some comment.
   *
   * @return comment text or empty string
   */
  String value() default "";
}
