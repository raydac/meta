package com.igormaznitsa.meta.checker.processors;

import com.igormaznitsa.meta.checker.Context;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.List;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.generic.Type;

public class PureFunction extends AbstractMetaAnnotationProcessor {

  @Override
  protected int doProcessing(
      final Context context,
      final JavaClass javaClass,
      final ElementType type,
      final ParameterAnnotationEntry parameterAnnotationEntry,
      final AnnotationEntry annotationEntry
  ) {
    if (type == ElementType.METHOD) {
      final Method method = (Method) context.getNode();
      if (method.getReturnType() == Type.VOID) {
        context.error("pure function without result", true);
      }
    }
    return 1;
  }

  @Override
  public List<Class<? extends Annotation>> getProcessedAnnotationClasses() {
    return List.of(com.igormaznitsa.meta.annotation.PureFunction.class);
  }
}
