/*
 * Copyright 2019 Igor Maznitsa.
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

import com.igormaznitsa.meta.checker.jversion.JavaVersion;
import com.igormaznitsa.meta.checker.jversion.LongComparator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.springframework.util.AntPathMatcher;

/**
 * It allows to make check of classes in a JAR packed archive.
 * @since 1.1.3
 */
@Mojo(name = "check-jar", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class JarCheckerMojo extends AbstractMojo {

  /**
   * Restrict compiled class format version. Also
   * '=','&lt;=','&gt;=','&lt;','&gt;' can be used. Java version can be
   * '1.1','1.2','1.3','1.4','5','6','7','8','5.0','6.0','7.0','8.0','9.0','10.0','11.0'.
   *
   * <code>
   * &lt;restrictClassFormat&gt;&lt;![CDATA[&lt;8]]&gt;&lt;/restrictClassFormat&gt;
   * </code>
   */
  @Parameter(name = "restrictClassFormat")
  private String restrictClassFormat;

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  /**
   * Archive file to be analyzed, if not provided then artifact file will be
   * used.
   */
  @Parameter(name = "archive")
  private String archive;

  /**
   * List of ant path patterns for files to be excluded.
   */
  @Parameter(name = "exclude")
  private List<String> exclude = new ArrayList<String>();

  /**
   * List of ant path patterns for files to be included.
   */
  @Parameter(name = "include")
  private List<String> include = new ArrayList<String>();

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final Log log = this.getLog();

    final File archiveFile;

    if (this.archive == null) {
      archiveFile = this.project.getArtifact().getFile();
    } else {
      archiveFile = new File(this.archive);
    }

    if (!archiveFile.isFile()) {
      throw new MojoExecutionException("Can't find archive file: " + archiveFile);
    } else {
      log.info("Check archive: " + archiveFile);
    }

    final JarFile jarFile;
    try {
      jarFile = new JarFile(archiveFile);
    } catch (IOException ex) {
      throw new MojoExecutionException("Can't open archive file: " + archiveFile, ex);
    }

    final AtomicInteger classCounter = new AtomicInteger();

    LongComparator javaVersionComparator;
    final JavaVersion javaVersion;

    if (this.restrictClassFormat != null) {
      
      String javaClassVersion = this.restrictClassFormat.trim();
      
      if (javaClassVersion.isEmpty()) {
        throw new IllegalArgumentException("Detected empty value for 'restrictClassFormat'");
      }

      javaVersionComparator = LongComparator.find(javaClassVersion);

      final int versionOffset;
      if (javaVersionComparator == null) {
        if (Character.isDigit(javaClassVersion.charAt(0))) {
          javaVersionComparator = LongComparator.EQU;
        }
        versionOffset = 0;
      } else {
        versionOffset = javaVersionComparator.getText().length();
      }
      javaClassVersion = javaClassVersion.substring(versionOffset).trim();

      javaVersion = JavaVersion.decode(javaClassVersion);
      if (javaVersion == null) {
        throw new IllegalArgumentException("Illegal java version in 'restrictClassFormat': " + javaClassVersion);
      }

      log.info("Class version restriction: " + javaVersionComparator.getText() + ' ' + javaVersion.getText());

    } else {
      javaVersionComparator = null;
      javaVersion = null;
    }

    final LongComparator finalJavaVersionComparator = javaVersionComparator;
    final AtomicInteger errorCounter = new AtomicInteger();
    final int MAX_SHOWN_ERRORS = 48;

    final ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM7) {
      @Override
      public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        if (finalJavaVersionComparator != null) {
          if (!finalJavaVersionComparator.compare(version & 0xFFFF, javaVersion.getValue())) {
            final int errorNum = errorCounter.getAndIncrement();
            if (errorNum < MAX_SHOWN_ERRORS) {
              final JavaVersion classVersion = JavaVersion.decode(version & 0xFFFF);
              log.error("Detected illegal class version " + (classVersion == null ? "UNKNOWN" : classVersion.getText()) + " : " + name);
            } else if (errorNum == MAX_SHOWN_ERRORS) {
              log.error("...");
            }
          }
        }
      }
    };

    final AntPathMatcher antPathMatcher = new AntPathMatcher();

    final Enumeration<JarEntry> entries = jarFile.entries();

    while (entries.hasMoreElements() && !Thread.currentThread().isInterrupted()) {
      final JarEntry entry = entries.nextElement();

      final String path = entry.getName();

      boolean process = true;

      if (!this.include.isEmpty()) {
        process = false;
        for (final String s : this.include) {
          if (antPathMatcher.match(s, path)) {
            log.debug(path + " included by pattern " + s);
            process = true;
            break;
          }
        }
      }

      if (process) {
        for (final String s : this.exclude) {
          if (antPathMatcher.match(s, path)) {
            log.debug(path + " excluded by pattern " + s);
            process = false;
            break;
          }
        }
      }

      if (!process || entry.isDirectory() || !entry.getName().toLowerCase(Locale.ENGLISH).endsWith(".class")) {
        continue;
      }

      classCounter.incrementAndGet();

      try {
        new ClassReader(IOUtils.readFully(jarFile.getInputStream(entry), (int) entry.getSize())).accept(classVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
      } catch (IOException ex) {
        throw new MojoExecutionException("Can't parse JAR item: " + entry.getName(), ex);
      }
    }

    if (Thread.currentThread().isInterrupted()) {
      log.warn("Interrupted...");
      return;
    }
    
    log.info("Processed " + classCounter.get() + " class(es)");

    if (errorCounter.get() != 0) {
      throw new MojoFailureException("Detected " + errorCounter.get() + " error(s)");
    }

  }

}
