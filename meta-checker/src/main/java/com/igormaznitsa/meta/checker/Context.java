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

import java.io.File;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.JavaClass;

public interface Context {

  void info(String info, boolean showProcessingItem);

  void warning(String warning, boolean showProcessingItem);

  void error(String error, boolean showProcessingItem);

  void abort(String error, boolean showProcessingItem);

  void setProcessingItem(JavaClass klazz, FieldOrMethod node, int itemIndex);

  JavaClass getProcessingClass();
  
  boolean isCheckNullableArgs();
  
  boolean isCheckMayContainNullArgs();
  
  FieldOrMethod getNode();

  int getItemIndex();
  
  File getTargetDirectoryFolder();
  
  com.igormaznitsa.meta.annotation.Weight.Unit getMaxAllowedWeightLevel();
  
  com.igormaznitsa.meta.Complexity getMaxAllowedTimeComplexity();
  
  com.igormaznitsa.meta.Complexity getMaxAllowedMemoryComplexity();
}
