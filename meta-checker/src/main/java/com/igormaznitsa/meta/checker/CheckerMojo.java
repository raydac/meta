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

import static com.igormaznitsa.meta.checker.Utils.pressing;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.meta.Complexity;
import com.igormaznitsa.meta.annotation.Weight;
import com.igormaznitsa.meta.checker.extracheck.MethodParameterChecker;
import com.igormaznitsa.meta.checker.jversion.JavaVersion;
import com.igormaznitsa.meta.checker.jversion.LongComparator;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "check", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class CheckerMojo extends AbstractMojo {

  private static final String DELIMITER = "................................";
  private static final String FAILURE_STRING =
      "Detected annotation '%s' defined to be recognized as error";

  private static final String[] BANNER = new String[] {
      "  __  __  ____  ____   __   ",
      " (  \\/  )( ___)(_  _) /__\\  ",
      "  )    (  )__)   )(  /(__)\\ ",
      " (_/\\/\\_)(____) (__)(__)(__)",
      "https://github.com/raydac/meta",
      ""};

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Parameter(defaultValue = "${session}", readonly = true, required = true)
  private MavenSession session;

  /**
   * Folder which will be recursively used as the source of class files.
   */
  @Parameter(defaultValue = "${project.build.outputDirectory}", name = "targetDirectory")
  private String targetDirectory;
  /**
   * Restrict compiled class format version. Also '=','&lt;=','&gt;=','&lt;','&gt;' can be used. Java version can be
   * '1.1','1.2','1.3','1.4','5','6','7','8','5.0','6.0','7.0','8.0'.
   * <p>
   * <code>
   * &lt;restrictClassFormat&gt;&lt;![CDATA[&lt;8]]&gt;&lt;/restrictClassFormat&gt;
   * </code>
   *
   * @since 1.0.2
   */
  @Parameter(name = "restrictClassFormat")
  private String restrictClassFormat;
  /**
   * List of meta annotations in full canonical or short form. If checker met annotation from the list then it will be recognized as error.
   * NB! Annotation names are case-insensitive.
   */
  @Parameter(name = "failForAnnotations")
  private String[] failForAnnotations;
  /**
   * List of classes to be ignored by checker. Class name must be defined in canonical form and wildcat chars '*' and '?' are allowed.
   *
   * @since 1.1.0
   */
  @Parameter(name = "ignoreClasses")
  private String[] ignoreClasses;
  /**
   * Check that method object arguments are marked by @Nullable or @Nonnull (also allowed Intellij IDEA annotations).
   *
   * @since 1.1.0
   */
  @Parameter(name = "checkNullable", defaultValue = "false")
  private boolean checkNullable;
  /**
   * Check that list and object array types in methods are marked by @MayContainNull and @MustNotContainNull annotations.
   *
   * @since 1.1.0
   */
  @Parameter(name = "checkMayContainNull", defaultValue = "false")
  private boolean checkMayContainNull;
  /**
   * Define max allowed value for detected weight annotation, if detected annotation has bigger weight then it will be recognized as error.
   *
   * @since 1.1.2
   */
  @Parameter(name = "maxAllowedWeight")
  private String maxAllowedWeight;
  /**
   * Define max allowed value for detected time complexity annotation, if detected annotation has bigger complexity then it will be recognized as error.
   *
   * @since 1.1.2
   */
  @Parameter(name = "maxAllowedTimeComplexity")
  private String maxAllowedTimeComplexity;

  /**
   * Limit number of output class names for violated jdk version.
   *
   * @since 1.2.0
   */
  @Parameter(name = "violatingClassOutputLimit", defaultValue = "42")
  private int violatingClassOutputLimit = 42;
  /**
   * Define max allowed value for detected memory complexity annotation, if detected annotation has bigger complexity then it will be recognized as error.
   *
   * @since 1.1.2
   */
  @Parameter(name = "maxAllowedMemoryComplexity")
  private String maxAllowedMemoryComplexity;
  /**
   * Hide pseudo-graphic banner.
   *
   * @since 1.1.0
   */
  @Parameter(name = "hideBanner", defaultValue = "false")
  private boolean hideBanner = false;

  private LongComparator comparatorForJavaVersion;
  private JavaVersion decodedJavaVersion;
  private Pattern[] ignoreClassesAsPatterns;

  private static Weight.Unit decodeWeight(final String value) {
    final String normalized = pressing(value == null ? "" : value).replace("_", "");
    if (normalized.isEmpty()) {
      return null;
    }

    for (final Weight.Unit u : Weight.Unit.values()) {
      if (normalized.equalsIgnoreCase(pressing(u.name()).replace("_", ""))) {
        return u;
      }
    }
    throw new NoSuchElementException("Can't recognize weight unit for its name : " + value);
  }

  private static Complexity decodeComplexity(final String value) {
    final String normalized = pressing(value == null ? "" : value).replace("_", "");
    if (normalized.isEmpty()) {
      return null;
    }

    Complexity detected = null;

    for (final Complexity c : Complexity.values()) {
      final String name = pressing(c.name()).replace("_", "");
      final String formula = pressing(c.getFormula()).replace("_", "");
      if (normalized.equalsIgnoreCase(name) || normalized.equalsIgnoreCase(formula)) {
        detected = c;
        break;
      }
    }

    if (detected == null) {
      throw new NoSuchElementException(
          "Can't recognize complexity level from string value : " + value);
    }

    return detected;
  }

  private static AtomicInteger extractCounter(final Map<MetaAnnotation, AtomicInteger> counters,
                                              final MetaAnnotation annotation) {
    return counters.computeIfAbsent(annotation, key -> new AtomicInteger(0));
  }

  public String getMaxAllowedWeight() {
    return this.maxAllowedWeight;
  }

  public String getMaxAllowedTimeComplexity() {
    return this.maxAllowedTimeComplexity;
  }

  public String getMaxAllowedMemoryComplexity() {
    return this.maxAllowedMemoryComplexity;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    prepareIgnoreClassPatterns();

    final File targetDirectoryFile = new File(this.targetDirectory);
    if (!targetDirectoryFile.isDirectory()) {
      getLog().warn(
          "Can't find directory for investigation, may be there are not classes for compilation : " +
              this.targetDirectory);
      return;
    } else {
      if (!this.hideBanner && !this.session.isParallel()) {
        for (final String s : BANNER) {
          getLog().info(s);
        }
        getLog().info(DELIMITER);
      }
      getLog().info("Class file folder : " + targetDirectoryFile.getAbsolutePath());
      getLog().info(DELIMITER);
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
      } else {
        versionOffset = this.comparatorForJavaVersion.getText().length();
      }
      javaClassVersion = javaClassVersion.substring(versionOffset).trim();

      this.decodedJavaVersion = JavaVersion.decode(javaClassVersion);
      if (this.decodedJavaVersion == null) {
        throw new IllegalArgumentException(
            "Illegal java version in 'restrictClassFormat': " + javaClassVersion);
      }
    }

    final Map<MetaAnnotation, AtomicInteger> counters = new EnumMap<>(MetaAnnotation.class);

    final AtomicInteger counterWarnings = new AtomicInteger();
    final AtomicInteger counterTotalErrors = new AtomicInteger();
    final AtomicInteger counterInfo = new AtomicInteger();

    final Complexity theMaxAllowedTimeComplexity;
    final Complexity theMaxAllowedMemoryComplexity;
    final Weight.Unit theMaxAllowedWeight;

    try {
      theMaxAllowedWeight = decodeWeight(getMaxAllowedWeight());
    } catch (NoSuchElementException ex) {
      getLog().error("Can't recognize weight value : " + getMaxAllowedWeight());
      getLog().error("Allowed values : " + Arrays.toString(Weight.Unit.values()));
      throw new MojoExecutionException(ex.getMessage(), ex);
    }

    try {
      theMaxAllowedTimeComplexity = decodeComplexity(getMaxAllowedTimeComplexity());
      theMaxAllowedMemoryComplexity = decodeComplexity(getMaxAllowedMemoryComplexity());
    } catch (NoSuchElementException ex) {
      getLog().error("Can't recognize a complexity constant");
      getLog().error("Allowed values : " + Arrays.toString(Complexity.values()));
      throw new MojoExecutionException(ex.getMessage(), ex);
    }

    final Context context = new Context() {
      FieldOrMethod node;
      JavaClass javaClass;
      int itemIndex;

      @Override
      public Weight.Unit getMaxAllowedWeightLevel() {
        return theMaxAllowedWeight;
      }

      @Override
      public Complexity getMaxAllowedTimeComplexity() {
        return theMaxAllowedTimeComplexity;
      }

      @Override
      public Complexity getMaxAllowedMemoryComplexity() {
        return theMaxAllowedMemoryComplexity;
      }

      @Override
      public JavaClass getProcessingClass() {
        return this.javaClass;
      }

      @Override
      public int getItemIndex() {
        return this.itemIndex;
      }

      @Override
      public boolean isCheckNullableArgs() {
        return checkNullable;
      }

      @Override
      public boolean isCheckMayContainNullArgs() {
        return checkMayContainNull;
      }

      @Override
      public File getTargetDirectoryFolder() {
        return targetDirectoryFile;
      }

      public String currentProcessingItemAsString() {
        final StringBuilder builder = new StringBuilder();

        final int line = Utils.findLineNumber(this.node);
        String className = Utils.normalizeClassNameAndRemoveSubclassName(
            requireNonNull(this.javaClass).getClassName());
        final String nodeName = Utils.asString(this.javaClass, this.node);

        builder.append(className).append(".java").append(":[");
        if (line < 0) {
          builder.append("-,-]");
        } else {
          builder.append(line).append(',').append(1).append(']');
        }
        builder.append(' ');
        if (this.node != null) {
          if (this.node instanceof Field) {
            builder.append("field ").append(nodeName);
          } else {
            final Method method = (Method) node;
            if (method.getName().equals("<init>")) {
              builder.append("constructor ");
            } else {
              builder.append("method ");
            }
            builder.append(nodeName).append(" (flags: #")
                .append(Integer.toHexString(method.getAccessFlags()).toUpperCase(Locale.ENGLISH))
                .append(") ");
          }
        } else {
          builder.append("whole class");
        }

        return builder.toString();
      }

      @Override
      public void setProcessingItem(final JavaClass javaClass, final FieldOrMethod node,
                                    final int itemIndex) {
        this.node = node;
        this.javaClass = javaClass;
        this.itemIndex = itemIndex;
      }

      @Override
      public FieldOrMethod getNode() {
        return this.node;
      }

      @Override
      public void info(final String info, final boolean showProcessingItem) {
        counterInfo.incrementAndGet();
        getLog().info((showProcessingItem ? currentProcessingItemAsString() + ' ' : "") + info);
      }

      @Override
      public void warning(final String warning, final boolean showProcessingItem) {
        counterWarnings.incrementAndGet();
        getLog().warn((showProcessingItem ? currentProcessingItemAsString() + ' ' : "") + warning);
      }

      @Override
      public void error(final String error, final boolean showProcessingItem) {
        counterTotalErrors.incrementAndGet();
        getLog().error((showProcessingItem ? currentProcessingItemAsString() + ' ' : "") + error);
      }

      @Override
      public void abort(final String error, final boolean showProcessingItem) {
        throw new AbortException(
            (showProcessingItem ? currentProcessingItemAsString() + ' ' : "") + error);
      }

    };

    int classVersionViolationCounter = 0;

    final long startTime = System.currentTimeMillis();
    int processedClasses = 0;
    try {
      final Iterator<File> iterator =
          FileUtils.iterateFiles(targetDirectoryFile, new String[] {"class", "CLASS"}, true);
      int classIndex = 0;
      while (iterator.hasNext() && !Thread.currentThread().isInterrupted()) {
        final File file = iterator.next();
        getLog().debug(String.format("Processing class file : %s", file.getAbsolutePath()));
        try {
          final JavaClass parsed = new ClassParser(file.getAbsolutePath()).parse();

          if (isClassIgnored(parsed)) {
            context.info(String.format("Ignored class file : %s", file.getAbsolutePath()), false);
            continue;
          }

          processedClasses++;

          if (!isClassVersionAllowed(parsed)) {
            if (classVersionViolationCounter < this.getViolatingClassOutputLimit()) {
              classVersionViolationCounter++;
              final int major = parsed.getMajor();
              final JavaVersion javaVersion = JavaVersion.decode(major);
              context.error(String.format("detected class version violation, detected %s: %s",
                  javaVersion == null ?
                      ("0x" + Integer.toHexString(major).toUpperCase(Locale.ENGLISH)) : javaVersion,
                  file.getAbsolutePath()), false);
            }
            counterTotalErrors.incrementAndGet();
          }
          classIndex++;
          for (final MetaAnnotation p : MetaAnnotation.VALUES) {
            extractCounter(counters, p).addAndGet(
                p.getProcessor().processClass(context, parsed, classIndex));
          }
          checkMethodsForMarkedObjectTypes(context, parsed);
        } catch (AbortException ex) {
          throw new MojoFailureException(ex.getMessage());
        } catch (IOException ex) {
          context.error(String.format("Can't read class file : %s", file.getAbsolutePath()), false);
        } catch (ClassFormatException ex) {
          context.error(String.format("Can't parse class file : %s", file.getAbsolutePath()),
              false);
        }
      }

      if (this.failForAnnotations != null && this.failForAnnotations.length > 0) {
        getLog().debug("Defined annotations to be interpreted as error : " +
            Arrays.toString(this.failForAnnotations));
        for (final Map.Entry<MetaAnnotation, AtomicInteger> detectedAnnotation : counters.entrySet()) {
          if (detectedAnnotation.getValue().get() > 0) {
            for (final String s : this.failForAnnotations) {
              if (detectedAnnotation.getKey().isAmongClassNames(s)) {
                final String text = String.format(FAILURE_STRING, s);
                context.error(text, false);
              }
            }
          }
        }
        if (counterTotalErrors.get() > 0) {
          throw new MojoFailureException(
              String.format("The check found %d error(s)", counterTotalErrors.get()));
        }
      } else {
        getLog().debug("There are no annotations defined that are interpreted as an error");
      }
    } finally {
      if ((counterTotalErrors.get() | counterInfo.get() | counterWarnings.get()) != 0) {
        getLog().info(DELIMITER);
      }

      final int totalAnnotations = counters.values().stream().mapToInt(AtomicInteger::get).sum();

      getLog().info(String.format("          Processed classes: %d", processedClasses));
      getLog().info(String.format("       Detected annotations: %d", totalAnnotations));
      getLog().info(String.format("             Detected To-Do: %d",
          extractCounter(counters, MetaAnnotation.TODO).get()));
      getLog().info(
          String.format("             Detected risks: %d",
              extractCounter(counters, MetaAnnotation.RISKY).get()));
      getLog().info(
          String.format("      Detected experimental: %d",
              extractCounter(counters, MetaAnnotation.EXPERIMENTAL).get()));
      if (classVersionViolationCounter > 0) {
        getLog().error(
            String.format("Class version violation(s): %d",
                classVersionViolationCounter));
      } else {
        getLog().info(
            String.format(" Class version violation(s): %d",
                classVersionViolationCounter));
      }

      if (counterWarnings.get() > 0) {
        getLog().warn(
            String.format("          Total warnings: %d", counterWarnings.get()));
      } else {
        getLog().info(
            String.format("             Total warnings: %d", counterWarnings.get()));
      }

      if (counterTotalErrors.get() > 0) {
        getLog().error(
            String.format("              Total errors: %d", counterTotalErrors.get()));
      } else {
        getLog().info(
            String.format("               Total errors: %d", counterTotalErrors.get()));
      }

      getLog().info(DELIMITER);
      getLog().info(
          String.format(" Total spent time: %s",
              Utils.printTimeDelay(Duration.ofMillis(System.currentTimeMillis() - startTime))));
    }

    if (counterTotalErrors.get() > 0) {
      throw new MojoFailureException(
          String.format("Detected %d error(s), see the log", counterTotalErrors.get()));
    }
  }

  public int getViolatingClassOutputLimit() {
    return this.violatingClassOutputLimit;
  }

  public void setViolatingClassOutputLimit(int value) {
    this.violatingClassOutputLimit = value;
  }

  public boolean isHideBanner() {
    return this.hideBanner;
  }

  public String getRestrictClassFormat() {
    return this.restrictClassFormat;
  }

  public boolean isCheckMayContainNullArgs() {
    return this.checkMayContainNull;
  }

  public boolean isCheckNullableArgs() {
    return this.checkNullable;
  }

  public String[] getIgnoreClasses() {
    return this.ignoreClasses;
  }

  public String getTargetDirectory() {
    return this.targetDirectory;
  }

  public String[] getFailForAnnotations() {
    return this.failForAnnotations;
  }

  private void prepareIgnoreClassPatterns() {
    this.ignoreClassesAsPatterns = null;
    if (this.ignoreClasses != null) {
      this.ignoreClassesAsPatterns = new Pattern[this.ignoreClasses.length];
      int index = 0;
      for (final String str : this.ignoreClasses) {
        this.ignoreClassesAsPatterns[index++] = Pattern.compile(Utils.escapeRegexToWildCat(str));
      }
    }
  }

  private void checkMethodsForMarkedObjectTypes(final Context context, final JavaClass clazz) {
    if ((context.isCheckMayContainNullArgs() || context.isCheckNullableArgs()) &&
        !(clazz.isAnnotation() || clazz.isSynthetic())) {
      int index = 0;
      for (final Method m : clazz.getMethods()) {
        context.setProcessingItem(clazz, m, index++);
        final String name = m.getName();
        if ("<clinit>".equals(name)) {
          continue;
        }
        if ((m.getModifiers() & (0x40 | 0x1000)) == 0) {

          final boolean skipCheckParameters =
              clazz.isNested() && m.getAccessFlags() == 0 && "<init>".equals(m.getName());

          if (clazz.isEnum() &&
              ("values".equals(name) || "valueOf".equals(name) || "<init>".equals(name))) {
            continue;
          }
          if (context.isCheckNullableArgs()) {
            MethodParameterChecker.checkReturnTypeForNullable(context, m);
            if (!skipCheckParameters) {
              MethodParameterChecker.checkParamsTypeForNullable(context, m);
            }
          }
          if (context.isCheckMayContainNullArgs()) {
            MethodParameterChecker.checkReturnTypeForMayContainNull(context, m);
            if (!skipCheckParameters) {
              MethodParameterChecker.checkParamsTypeForMayContainNull(context, m);
            }
          }
        }
      }
    }
  }

  private boolean isClassIgnored(final JavaClass clazz) {
    if (this.ignoreClassesAsPatterns == null || this.ignoreClassesAsPatterns.length == 0) {
      return false;
    }

    final String klazzName = clazz.getClassName();

    for (final Pattern pattern : this.ignoreClassesAsPatterns) {
      if (pattern.matcher(klazzName).matches()) {
        return true;
      }
    }

    return false;
  }

  private boolean isClassVersionAllowed(final JavaClass klazz) {
    if (this.comparatorForJavaVersion == null) {
      return true;
    }
    return this.comparatorForJavaVersion.compare(klazz.getMajor(),
        requireNonNull(this.decodedJavaVersion).getValue());
  }

  private static class AbortException extends RuntimeException {

    private static final long serialVersionUID = -1153122159632822978L;

    public AbortException(String message) {
      super(message);
    }
  }

}
