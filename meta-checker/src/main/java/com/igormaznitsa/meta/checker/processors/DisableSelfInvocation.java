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

import com.igormaznitsa.meta.checker.Context;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.List;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;

public class DisableSelfInvocation extends AbstractMetaAnnotationProcessor {

  @Override
  protected int doProcessing(final Context context, final JavaClass javaClass,
                             final ElementType type,
                             final ParameterAnnotationEntry parameterAnnotationEntry,
                             final AnnotationEntry annotationEntry) {
    final Method method = (Method) context.getNode();
    final ConstantPoolGen constantPoolGen = new ConstantPoolGen(javaClass.getConstantPool());
    for (final Method m : javaClass.getMethods()) {
      if (m.equals(method)) {
        continue;
      }
      final MethodGen thatMethod = new MethodGen(m, javaClass.getClassName(), constantPoolGen);
      final LineNumberTable lineNumberTable = thatMethod.getLineNumberTable(constantPoolGen);

      for (final InstructionHandle instructionHandle : thatMethod.getInstructionList()
          .getInstructionHandles()) {
        final Instruction instruction = instructionHandle.getInstruction();
        if (instruction instanceof InvokeInstruction) {

          final InvokeInstruction invokeInstruction = (InvokeInstruction) instruction;

          if (invokeInstruction.getClassName(constantPoolGen).equals(javaClass.getClassName())
              && invokeInstruction.getMethodName(constantPoolGen).equals(method.getName())
              && invokeInstruction.getSignature(constantPoolGen).equals(method.getSignature())) {

            final int bytecodePosition = instructionHandle.getPosition();
            final int lineNumber =
                lineNumberTable == null ? -1 : lineNumberTable.getSourceLine(bytecodePosition);
            if (lineNumber < 0) {
              context.error("detected self invocation in the class", true);
            } else {
              context.error("detected self invocation at line " + lineNumber, true);
            }
          }
        }
      }
    }
    return 1;
  }

  @Override
  public List<Class<? extends Annotation>> getProcessedAnnotationClasses() {
    return List.of(com.igormaznitsa.meta.annotation.DisableSelfInvocation.class);
  }

}
