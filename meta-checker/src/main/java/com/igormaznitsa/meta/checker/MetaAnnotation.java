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
package com.igormaznitsa.meta.checker;

import com.igormaznitsa.meta.checker.processors.AbstractMetaAnnotationProcessor;
import com.igormaznitsa.meta.checker.processors.Constraint;
import com.igormaznitsa.meta.checker.processors.Determined;
import com.igormaznitsa.meta.checker.processors.DisableSelfInvocation;
import com.igormaznitsa.meta.checker.processors.Experimental;
import com.igormaznitsa.meta.checker.processors.ImplementationNote;
import com.igormaznitsa.meta.checker.processors.LazyInited;
import com.igormaznitsa.meta.checker.processors.Link;
import com.igormaznitsa.meta.checker.processors.MayContainNull;
import com.igormaznitsa.meta.checker.processors.MemoryComplexity;
import com.igormaznitsa.meta.checker.processors.MustNotContainNull;
import com.igormaznitsa.meta.checker.processors.NeedsRefactoring;
import com.igormaznitsa.meta.checker.processors.NonDetermined;
import com.igormaznitsa.meta.checker.processors.OneWayChange;
import com.igormaznitsa.meta.checker.processors.PureFunction;
import com.igormaznitsa.meta.checker.processors.ReturnsOriginal;
import com.igormaznitsa.meta.checker.processors.Risky;
import com.igormaznitsa.meta.checker.processors.ThrowsRuntimeException;
import com.igormaznitsa.meta.checker.processors.TimeComplexity;
import com.igormaznitsa.meta.checker.processors.ToDo;
import com.igormaznitsa.meta.checker.processors.Warning;
import com.igormaznitsa.meta.checker.processors.Weight;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public enum MetaAnnotation {
  CONSTRAINT(new Constraint()),
  DETERMINED(new Determined()),
  DISABLE_SELF_INVOCATION(new DisableSelfInvocation()),
  EXPERIMENTAL(new Experimental()),
  IMPLEMENTATION_NOTE(new ImplementationNote()),
  LAZY_INITED(new LazyInited()),
  LINK(new Link()),
  MAY_CONTAIN_NULL(new MayContainNull()),
  MEMORY_COMPLEXITY(new MemoryComplexity()),
  MUST_NOT_CONTAIN_NULL(new MustNotContainNull()),
  NEEDS_REFACTORING(new NeedsRefactoring()),
  NON_DETERMINED(new NonDetermined()),
  ONE_WAY_CHANGE(new OneWayChange()),
  PURE_FUNCTION(new PureFunction()),
  RETURNS_ORIGINAL(new ReturnsOriginal()),
  RISKY(new Risky()),
  THROW_RUNTIME_EXCEPTION(new ThrowsRuntimeException()),
  TIME_COMPLEXITY(new TimeComplexity()),
  TODO(new ToDo()),
  WARNING(new Warning()),
  WEIGHT(new Weight());

  public static final List<MetaAnnotation> VALUES = List.of(MetaAnnotation.values());
  private final AbstractMetaAnnotationProcessor processor;
  private final Set<String> canonicalClassNames;

  MetaAnnotation(final AbstractMetaAnnotationProcessor instance) {
    this.processor = instance;
    this.canonicalClassNames = this.processor.getProcessedAnnotationClasses().stream().map(
        Class::getCanonicalName).collect(
        Collectors.toSet());
  }

  public AbstractMetaAnnotationProcessor getProcessor() {
    return this.processor;
  }

  public boolean isAmongClassNames(final String name) {
    final String trimmed = name.trim();
    if (trimmed.contains(".")) {
      // canonical form
      return this.canonicalClassNames.stream()
          .anyMatch(x -> x.toLowerCase(Locale.ENGLISH).equalsIgnoreCase(trimmed));
    } else {
      // short form
      final String shortForm = ('.' + trimmed).toLowerCase(Locale.ENGLISH);
      return this.canonicalClassNames.stream()
          .anyMatch(x -> x.toLowerCase(Locale.ENGLISH).endsWith(shortForm));
    }
  }

}
