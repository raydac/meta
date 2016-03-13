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

import com.igormaznitsa.meta.checker.jversion.LongComparator;
import com.igormaznitsa.meta.checker.jversion.JavaVersion;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;

import com.igormaznitsa.meta.checker.extracheck.MethodParameterChecker;

@Mojo(name = "check", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class CheckerMojo extends AbstractMojo {

  private static class AbortException extends RuntimeException {

    private static final long serialVersionUID = -1153122159632822978L;

    public AbortException(String message) {
      super(message);
    }
  }

//  @Parameter (defaultValue = "${project}", readonly = true, required = true)
//  private MavenProject project;
  /**
   * The Directory contains compiled classes of the project.
   */
  @Parameter(defaultValue = "${project.build.outputDirectory}", name = "targetDirectory")
  private String targetDirectory;

  /**
   * Restrict compiled class format version. Also '=','&lt;=','&gt;=','&lt;','&gt;' can be used. Java version can be
   * '1.1','1.2','1.3','1.4','5','6','7','8','5.0','6.0','7.0','8.0'.
   *
   * <code>
   * &lt;restrictClassFormat&gt;&lt;![CDATA[&lt;8]]&gt;&lt;/restrictClassFormat&gt;
   * </code>
   *
   * @since 1.0.2
   */
  @Parameter(name = "restrictClassFormat")
  private String restrictClassFormat;

  /**
   * List of meta annotations which will be recognized as failure. Names are case insensitive and if name doesn't contain dot char then only class name will be checked (without
   * package part).
   */
  @Parameter(name = "failForAnnotations")
  private String[] failForAnnotations;

  /**
   * List of classes to be ignored by checker. Class namme must be defined in canonical view and can contain wildcat chars '*' and '?'.
   * @since 1.0.3
   */
  @Parameter(name = "ignoreClasses")
  private String[] ignoreClasses;
  
  /**
   * Check every object argument and result to be marked by annotations describing its nullable or non-nullable state and fail if the rule is violated.
   *
   * @since 1.0.3
   */
  @Parameter(name = "failForNonMarkedParams", defaultValue = "false")
  private boolean failForNonMarkedParams;

  private LongComparator comparatorForJavaVersion;
  private JavaVersion decodedJavaVersion;
  private Pattern[] ignoreClassesAsPatterns;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    prepareIgnoreClassPatterns();
    
    final File targetDirectoryFile = new File(this.targetDirectory);
    if (!targetDirectoryFile.isDirectory()) {
      throw new MojoExecutionException("Can't find the build output directory [" + this.targetDirectory + ']');
    }
    else {
      getLog().info("Folder to find classes : " + targetDirectoryFile.getAbsolutePath());
      getLog().info("................................");
    }

    if (this.restrictClassFormat != null) {
      String javaClassVersion = this.restrictClassFormat.trim();
      if (javaClassVersion.isEmpty()) {
        throw new IllegalArgumentException("Detected empty value for 'restrictClassFormat'");
      }

      this.comparatorForJavaVersion = LongComparator.find(javaClassVersion);

      final int versionOffset;
      if (this.comparatorForJavaVersion == null) {
        if (Character.isDigit(javaClassVersion.charAt(0))) {
          this.comparatorForJavaVersion = LongComparator.EQU;
        }
        versionOffset = 0;
      }
      else {
        versionOffset = this.comparatorForJavaVersion.getText().length();
      }
      javaClassVersion = javaClassVersion.substring(versionOffset).trim();

      this.decodedJavaVersion = JavaVersion.decode(javaClassVersion);
      if (this.decodedJavaVersion == null) {
        throw new IllegalArgumentException("Illegal java version in 'restrictClassFormat': " + javaClassVersion);
      }
    }

    final Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();

    final AtomicInteger counterWarings = new AtomicInteger();
    final AtomicInteger counterErrors = new AtomicInteger();

    final Context context = new Context() {
      FieldOrMethod node;
      JavaClass klazz;
      int itemIndex;

      @Override
      public int getItemIndex() {
        return this.itemIndex;
      }
      
      @Override
      public File getTargetDirectoryFolder() {
        return targetDirectoryFile;
      }

      public String currentProcessingItemAsString() {
        final StringBuilder builder = new StringBuilder();

        final int line = Utils.findLineNumber(this.node);
        String klazzName = Utils.normalizeClassNameAndRemoveSubclassName(this.klazz.getClassName());
        final String nodeName = Utils.asString(this.klazz,this.node);

        builder.append(klazzName).append(".java").append(":[");
        if (line < 0) {
          builder.append("-,-]");
        }
        else {
          builder.append(line).append(',').append(1).append(']');
        }
        builder.append(' ');
        if (this.node != null) {
          if (this.node instanceof Field){
            builder.append("field ").append(nodeName);
          }else{
            final Method method = (Method)node;
            if (method.getName().equals("<init>")) {
              builder.append("constructor ");
            } else {
              builder.append("method ");
            }
            builder.append(nodeName);
          }
        }
        else {
          builder.append("whole class");
        }

        return builder.toString();
      }

      @Override
      public boolean isObjResultAndParamsMustBeMarked() {
        return failForNonMarkedParams;
      }

      @Override
      public void setProcessingItem(final JavaClass klazz, final FieldOrMethod node, final int itemIndex) {
        this.node = node;
        this.klazz = klazz;
        this.itemIndex = itemIndex;
      }

      @Override
      public FieldOrMethod getNode() {
        return this.node;
      }

      @Override
      public void info(final String info, final boolean showProcessingItem) {
        getLog().info((showProcessingItem ? currentProcessingItemAsString() + ' ' : "") + info);
      }

      @Override
      public void warning(final String warning, final boolean showProcessingItem) {
        counterWarings.incrementAndGet();
        getLog().warn((showProcessingItem ? currentProcessingItemAsString() + ' ' : "") + warning);
      }

      @Override
      public void error(final String error, final boolean showProcessingItem) {
        counterErrors.incrementAndGet();
        getLog().error((showProcessingItem ? currentProcessingItemAsString() + ' ' : "") + error);
      }

      @Override
      public void abort(final String error, final boolean showProcessingItem) {
        throw new AbortException((showProcessingItem ? currentProcessingItemAsString() + ' ' : "") + error);
      }

      @Override
      public void countDetectedAnnotation(final String annotationClassName) {
        AtomicInteger counter = counters.get(annotationClassName);
        if (counter == null) {
          counter = new AtomicInteger();
          counters.put(annotationClassName, counter);
        }
        counter.incrementAndGet();
      }
    };

    final long startTime = System.currentTimeMillis();
    try {
      final Iterator<File> iterator = FileUtils.iterateFiles(targetDirectoryFile, new String[]{"class", "CLASS"}, true);
      int classIndex = 0;
      while (iterator.hasNext()) {
        final File file = iterator.next();
        getLog().debug("Processing class file : " + file.getAbsolutePath());
        try {
          final JavaClass parsed = new ClassParser(file.getAbsolutePath()).parse();
          
          if (isClassIgnored(parsed)) {
            getLog().info("Ignoring class file : " + file.getAbsolutePath());
            continue;
          }
          
          if (!isClassVersionAllowed(parsed)) {
            getLog().error("Detected illegal class format '" + JavaVersion.decode(parsed.getMajor()) + "' at class file " + file.getAbsolutePath());
            counterErrors.incrementAndGet();
            break;
          }
          countAllDetectedAnnotations(context, parsed);
          classIndex ++;
          for (final AnnotationProcessor p : AnnotationProcessor.values()) {
            p.getInstance().processClass(context, parsed, classIndex);
          }
          if (context.isObjResultAndParamsMustBeMarked()) {
            checkMethodsForMarkedObjectTypes(context, parsed);
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

      if (this.failForAnnotations != null && this.failForAnnotations.length > 0) {
        getLog().debug("Should be recognized as failure : " + Arrays.toString(this.failForAnnotations));
        for (final String detected : counters.keySet()) {
          if (counters.get(detected).get() > 0) {
            final String name = detected.toLowerCase(Locale.ENGLISH);
            final String shortName = Utils.extractShortNameOfClass(name);
            for (final String s : this.failForAnnotations) {
              if (s.indexOf('.') < 0) {
                if (shortName.equalsIgnoreCase(s)) {
                  throw new MojoFailureException("Failure for detected '" + s + "' annotation");
                }
              }
              else if (name.equalsIgnoreCase(s)) {
                throw new MojoFailureException("Failure for detected '" + s + "' annotation");
              }
            }
          }
        }
      }
      else {
        getLog().debug("there is not any detected annotation in config to be recognized as failure");
      }
    }
    finally {
      getLog().info("................................");
      getLog().info(String.format("Total To-Do : %d", extractCounter(counters, AnnotationProcessor.TODO)));
      getLog().info(String.format("Total risks : %d", extractCounter(counters, AnnotationProcessor.RISKY)));

      if (counterErrors.get() > 0) {
        getLog().error(String.format("Total errors : %d", counterErrors.get()));
      }
      else {
        getLog().info(String.format("Total errors : %d", counterErrors.get()));
      }

      if (counterWarings.get() > 0) {
        getLog().warn(String.format("Total warnings : %d", counterWarings.get()));
      }
      else {
        getLog().info(String.format("Total warnings : %d", counterWarings.get()));
      }

      getLog().info("................................");
      getLog().info(String.format("Total time : %s", Utils.printTimeDelay(System.currentTimeMillis() - startTime)));
    }
  }

  private void prepareIgnoreClassPatterns() {
    this.ignoreClassesAsPatterns = null;
    if (this.ignoreClasses != null) {
      this.ignoreClassesAsPatterns = new Pattern[this.ignoreClasses.length];
      int index = 0;
      for(final String str : this.ignoreClasses) {
        this.ignoreClassesAsPatterns [index++] = Pattern.compile(Utils.escapeRegexToWildCat(str));
      }
    }
  }
  
  private void checkMethodsForMarkedObjectTypes(final Context context, final JavaClass clazz) {
    if (!(clazz.isAnnotation() || clazz.isSynthetic())) {
      int index = 0;
      for (final Method m : clazz.getMethods()) {
        context.setProcessingItem(clazz, m, index++);
        final String name = m.getName();
        if ("<clinit>".equals(name)) continue;
        if ((m.getModifiers() & (0x40 | 0x1000)) == 0){
          if (clazz.isEnum()){
            if ("values".equals(name) || "valueOf".equals(name) || "<init>".equals(name)) continue;
          }
          MethodParameterChecker.checkReturnType(context, m);
          MethodParameterChecker.checkParamsType(context, m);
        }
      }
    }
  }

  private boolean isClassIgnored(final JavaClass clazz) {
    if (this.ignoreClassesAsPatterns == null || this.ignoreClassesAsPatterns.length == 0) return false;
    
    final String klazzName = clazz.getClassName();
    
    for(final Pattern pattern : this.ignoreClassesAsPatterns){
      if (pattern.matcher(klazzName).matches()) return true;
    }
    
    return false;
  }
  
  private boolean isClassVersionAllowed(final JavaClass klazz) {
    if (this.comparatorForJavaVersion == null) {
      return true;
    }
    return this.comparatorForJavaVersion.compare(klazz.getMajor(), this.decodedJavaVersion.getValue());
  }

  private static void countAllDetectedAnnotations(final Context context, final JavaClass clazz) {
    for (final AnnotationEntry ae : clazz.getAnnotationEntries()) {
      context.countDetectedAnnotation(Utils.classNameToNormalView(ae.getAnnotationType()));
    }
    for (final Field field : clazz.getFields()) {
      for (final AnnotationEntry ae : field.getAnnotationEntries()) {
        context.countDetectedAnnotation(Utils.classNameToNormalView(ae.getAnnotationType()));
      }
    }
    for (final Method method : clazz.getMethods()) {
      for (final AnnotationEntry ae : method.getAnnotationEntries()) {
        context.countDetectedAnnotation(Utils.classNameToNormalView(ae.getAnnotationType()));
      }
      for (final ParameterAnnotationEntry pae : method.getParameterAnnotationEntries()) {
        for (final AnnotationEntry ae : pae.getAnnotationEntries()) {
          context.countDetectedAnnotation(Utils.classNameToNormalView(ae.getAnnotationType()));
        }
      }
    }
  }

  private static int extractCounter(final Map<String, AtomicInteger> counters, final AnnotationProcessor annotation) {
    final AtomicInteger result = counters.get(annotation.getAnnotationClassName());
    return result == null ? 0 : result.get();
  }

}
