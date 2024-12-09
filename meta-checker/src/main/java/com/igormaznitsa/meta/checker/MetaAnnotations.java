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
import com.igormaznitsa.meta.checker.processors.ReturnsOriginal;
import com.igormaznitsa.meta.checker.processors.Risky;
import com.igormaznitsa.meta.checker.processors.ThrowsRuntimeException;
import com.igormaznitsa.meta.checker.processors.ThrowsRuntimeExceptions;
import com.igormaznitsa.meta.checker.processors.TimeComplexity;
import com.igormaznitsa.meta.checker.processors.ToDo;
import com.igormaznitsa.meta.checker.processors.Warning;
import com.igormaznitsa.meta.checker.processors.Weight;

public enum MetaAnnotations {
  CONSTRAINT(new Constraint()),
  DETERMINED(new Determined()),
  LAZYINITED(new LazyInited()),
  LINK(new Link()),
  MAYCONTAINNULL(new MayContainNull()),
  MUSTNOTCONTAINNULL(new MustNotContainNull()),
  NEEDSREFACTORING(new NeedsRefactoring()),
  NONDETERMINED(new NonDetermined()),
  ONEWAYCHANGE(new OneWayChange()),
  RETURNSORIGHNAL(new ReturnsOriginal()),
  RISKY(new Risky()),
  EXPERIMENTAL(new Experimental()),
  TODO(new ToDo()),
  WARNING(new Warning()),
  IMPLSPECIFIC(new ImplementationNote()),
  WEIGHT(new Weight()),
  THROWSRE(new ThrowsRuntimeException()),
  THROWSREs(new ThrowsRuntimeExceptions()),
  TIMECOMPLEXITY(new TimeComplexity()),
  MEMORYCOMPLEXITY(new MemoryComplexity());
  
  private final AbstractMetaAnnotationProcessor INSTANCE;

  public AbstractMetaAnnotationProcessor getInstance(){
    return this.INSTANCE;
  }

  MetaAnnotations(final AbstractMetaAnnotationProcessor instance) {
    this.INSTANCE = instance;
  }

  public String getAnnotationClassName () {
    return this.INSTANCE.getAnnotationClass().getName();
  }
}
