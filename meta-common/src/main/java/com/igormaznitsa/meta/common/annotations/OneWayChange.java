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
package com.igormaznitsa.meta.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows mark methods as impacting on something without any chance for rollback.
 * @since 1.0
 */
@Documented
@Target ({ElementType.METHOD,ElementType.TYPE})
@Retention (RetentionPolicy.RUNTIME)
public @interface OneWayChange {
  /**
   * Allows to drop some comments about object of the impact.
   * @return comment as String
   * @since 1.0
   */
  String comment() default "";
}
