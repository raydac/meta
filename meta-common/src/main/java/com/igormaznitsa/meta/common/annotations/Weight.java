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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to define computational weight of an entity, in fuzzy subjective relative units.
 * Also it can mark interface methods as their desired weight. For instance it makes easier to understand should implementation process something in the same thread or in another thread.
 * @since 1.0
 */
@Documented
@Target ({ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR})
@Retention (RetentionPolicy.RUNTIME)
@Inherited
public @interface Weight {
  /**
   * Contains units. They are very subjective ones but allow to describe weight of a method in subjective units.
   */
  enum Unit {
    /**
     * Variable weight, can be changed in wide interval. Mainly the same like undefined.
     */
    VARIABLE,
    /**
     * Very very extra light.
     */
    FLUFF,
    /**
     * Lighter than light one. May be just a getter for a field.
     */
    EXTRALIGHT,
    /**
     * Light, for instance a getter with some condition of light logic.
     */
    LIGHT,
    /**
     * Normal weight for regular execution with conditions and short loops.
     */
    NORMAL,
    /**
     * Contains long loops or calls for hard methods.
     */
    HARD,
    /**
     * A Call like ask "Ultimate Question of Life, the Universe, and Everything".
     */
    EXTRAHARD,
    /** 
     * A Call of the method can break the universe.
     */
    BLACK_HOLE
  }

  /**
   * Contains weight value for marked entity.
   * @return weight value for marked entity.
   */
  Unit value ();
}
