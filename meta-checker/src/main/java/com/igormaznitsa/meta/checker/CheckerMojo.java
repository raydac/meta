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

import com.igormaznitsa.meta.annotation.Risky;
import com.igormaznitsa.meta.annotation.ToDo;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo (name = "check", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class CheckerMojo extends AbstractMojo {

  private static class AbortException extends RuntimeException {

    private static final long serialVersionUID = -1153122159632822978L;

    public AbortException (String message) {
      super(message);
    }
  }

//  @Parameter (defaultValue = "${project}", readonly = true, required = true)
//  private MavenProject project;

  /**
   * The Directory contains compiled classes of the project.
   */
  @Parameter (defaultValue = "${project.build.outputDirectory}", name = "targetDirectory")
  private String targetDirectory;

  /**
   * List of meta annotations which existing will be recognized as failure.
   */
  @Parameter (name = "failForAnnotations")
  private String [] failForAnnotations;
  
  @Override
  public void execute () throws MojoExecutionException, MojoFailureException {
    final File targetDirectoryFile = new File(this.targetDirectory);
    if (!targetDirectoryFile.isDirectory()) {
      throw new MojoExecutionException("Can't find the build output directory [" + this.targetDirectory + ']');
    }
    else {
      getLog().info("Folder to find classes : " + targetDirectoryFile.getAbsolutePath());
      getLog().info("................................");
    }

    final Map<Class<? extends Annotation>, AtomicInteger> counters = new HashMap<Class<? extends Annotation>, AtomicInteger>();

    final AtomicInteger counterWarings = new AtomicInteger();
    final AtomicInteger counterErrors = new AtomicInteger();

    final Context context = new Context() {
      FieldOrMethod node;

      @Override
      public String nodeToString (final JavaClass klazz) {
        return (klazz == null ? "<null>" : klazz.getClassName()) + " "+ this.node.toString();
      }

      @Override
      public void setNode (final FieldOrMethod node) {
        this.node = node;
      }

      @Override
      public FieldOrMethod getNode () {
        return this.node;
      }

      @Override
      public void info (final String info) {
        getLog().info(info);
      }

      @Override
      public void warning (final String warning) {
        counterWarings.incrementAndGet();
        getLog().warn(warning);
      }

      @Override
      public void error (final String error) {
        counterErrors.incrementAndGet();
        getLog().error(error);
      }

      @Override
      public void abort (final String error) {
        throw new AbortException(error);
      }

      @Override
      public void countProcessedAnnotation (final Class<? extends Annotation> annotation) {
        AtomicInteger counter = counters.get(annotation);
        if (counter == null) {
          counter = new AtomicInteger();
          counters.put(annotation, counter);
        }
        counter.incrementAndGet();
      }
    };

    final long startTime = System.currentTimeMillis();
    try {
      final Iterator<File> iterator = FileUtils.iterateFiles(targetDirectoryFile, new String[]{"class", "CLASS"}, true);
      while (iterator.hasNext()) {
        final File file = iterator.next();
        getLog().debug("Processing class file : " + file.getAbsolutePath());
        try {
          final JavaClass parsed = new ClassParser(file.getAbsolutePath()).parse();
          for (final AnnotationProcessor p : AnnotationProcessor.values()) {
            p.getInstance().processClass(context, parsed);
          }
        }
        catch (AbortException ex) {
          throw new MojoFailureException(ex.getMessage());
        }
        catch (IOException ex) {
          getLog().error("Can't read class file [" + file.getAbsolutePath() + ']');
        }
        catch (ClassFormatException ex) {
          getLog().error("Can't parse class file [" + file.getAbsolutePath() + ']');
        }
      }

      if (counterErrors.get() > 0) {
        throw new MojoFailureException(String.format("Detected %d error(s)", counterErrors.get()));
      }
      
      if (this.failForAnnotations != null && this.failForAnnotations.length > 0){
        getLog().debug("Should be recognied as a failure : "+Arrays.toString(this.failForAnnotations));
        for(final Class<? extends Annotation> detected : counters.keySet()){
          if (counters.get(detected).get()>0){
            final String name = detected.getSimpleName().toLowerCase(Locale.ENGLISH);
            for(final String s : this.failForAnnotations){
              if (name.equalsIgnoreCase(s)){
                throw new MojoFailureException("Failure for detected '"+s+"' annotation");
              }
            }
          }
        }
      }else{
        getLog().debug("there is not any detected annotation in config to be recognized as failure");
      }
    }
    finally {
      getLog().info("................................");
      if (counterErrors.get()>0)
        getLog().error(String.format("Total errors : %d",counterErrors.get()));
      else
        getLog().info(String.format("Total errors : %d", counterErrors.get()));
      
      if (counterWarings.get() > 0)
        getLog().warn(String.format("Total warnings : %d",counterErrors.get()));
      else
        getLog().info(String.format("Total warnings : %d", counterErrors.get()));
      
      getLog().info(String.format("Total To-Do : %d", extractCounter(counters, ToDo.class)));
      getLog().info(String.format("Total risks : %d", extractCounter(counters, Risky.class)));
      getLog().info("................................");
      getLog().info(String.format("Total time : %s", Utils.printTimeDelay(System.currentTimeMillis() - startTime)));
    }
  }

  private static int extractCounter(final Map<Class<? extends Annotation>, AtomicInteger> counters,final Class<? extends Annotation> annotation){
    final AtomicInteger result = counters.get(annotation);
    return result == null ? 0 : result.get();
  }
  
}
