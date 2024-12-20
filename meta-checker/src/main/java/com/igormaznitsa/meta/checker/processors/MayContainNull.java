/*
 * Copyright 2016 Igor Maznitsa.
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

package com.igormaznitsa.meta.checker.processors;

import java.lang.annotation.Annotation;
import java.util.List;

public class MayContainNull extends MustNotContainNull {

  @Override
  public List<Class<? extends Annotation>> getProcessedAnnotationClasses() {
    return List.of(com.igormaznitsa.meta.annotation.MayContainNull.class);
  }

}
