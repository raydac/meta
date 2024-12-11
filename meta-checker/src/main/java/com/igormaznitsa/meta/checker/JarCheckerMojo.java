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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
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
 *
 * @since 1.1.3
 */
@Mojo(name = "check-jar", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class JarCheckerMojo extends AbstractMojo {

  /**
   * Restrict compiled class format version. Also,
   * '=','&lt;=','&gt;=','&lt;','&gt;' can be used. Java version can be
   * '1.1','1.2','1.3','1.4','5','6','7','8','5.0','6.0','7.0','8.0','9.0','10.0','11.0'.
   * <p>
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
   * List of ANT path patterns for files to be excluded.
   */
  @Parameter(name = "exclude")
  private List<String> exclude = new ArrayList<>();

  /**
   * Limit total error messages to show in log.
   *
   * @since 1.2.0
   */
  @Parameter(name = "limitOutputErrors", defaultValue = "65534")
  private int limitOutputErrors = 65534;

  /**
   * Limit list of error for JDK version to show in log.
   *
   * @since 1.2.0
   */
  @Parameter(name = "limitIllegalClassVersionErrors", defaultValue = "52")
  private int limitIllegalClassVersionErrors = 52;

  /**
   * List of ANT path patterns for files to be included.
   */
  @Parameter(name = "include")
  private List<String> include = new ArrayList<>();

  /**
   * ANT path patterns to detect resources which must be presented in archive.
   */
  @Parameter(name = "expected")
  private List<String> expected = new ArrayList<>();

  /**
   * ANT path patterns to detect resources which must NOT be presented in
   * archive.
   */
  @Parameter(name = "unexpected")
  private List<String> unexpected = new ArrayList<>();

  /**
   * Expected keys in manifest.
   */
  @Parameter(name = "manifestHas")
  private List<String> manifestHas = new ArrayList<>();

  /**
   * Unexpected keys in manifest.
   */
  @Parameter(name = "manifestHasNot")
  private List<String> manifestHasNot = new ArrayList<>();

  public String getRestrictClassFormat() {
    return this.restrictClassFormat;
  }

  public int getLimitOutputErrors() {
    return limitOutputErrors;
  }

  public void setLimitOutputErrors(int value) {
    this.limitOutputErrors = value;
  }

  public int getLimitIllegalClassVersionErrors() {
    return limitIllegalClassVersionErrors;
  }

  public void setLimitIllegalClassVersionErrors(int value) {
    this.limitIllegalClassVersionErrors = value;
  }

  public String getArchive() {
    return this.archive;
  }

  public List<String> getExclude() {
    return this.exclude;
  }

  public List<String> getInclude() {
    return this.include;
  }

  public List<String> getExpected() {
    return this.expected;
  }

  public List<String> getUnexpected() {
    return this.unexpected;
  }

  public List<String> getManifestHas() {
    return this.manifestHas;
  }

  public List<String> getManifestHasNot() {
    return this.manifestHasNot;
  }

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

    try (final JarFile jarFile = new JarFile(archiveFile)) {
      final AtomicInteger classCounter = new AtomicInteger();

      LongComparator javaVersionComparator;
      final JavaVersion javaVersion;

      if (this.restrictClassFormat != null) {

        String jdkVersionText = this.restrictClassFormat.trim();

        if (jdkVersionText.isEmpty()) {
          throw new IllegalArgumentException("Detected empty value for 'restrictClassFormat'");
        }

        javaVersionComparator = LongComparator.find(jdkVersionText);

        final int versionOffset;
        if (javaVersionComparator == null) {
          javaVersionComparator = LongComparator.EQU;
          versionOffset = 0;
        } else {
          versionOffset = javaVersionComparator.getText().length();
        }
        jdkVersionText = jdkVersionText.substring(versionOffset).trim();

        javaVersion = JavaVersion.decode(jdkVersionText);
        if (javaVersion == null) {
          throw new IllegalArgumentException(
              "Unexpected java version for 'restrictClassFormat': " + jdkVersionText);
        }

        log.info("Class version restriction: " + javaVersionComparator.getText() + ' ' +
            javaVersion.getText());

      } else {
        javaVersionComparator = null;
        javaVersion = null;
      }

      final LongComparator finalJavaVersionComparator = javaVersionComparator;
      final AtomicInteger errorCounter = new AtomicInteger();

      if (!this.manifestHas.isEmpty() || !this.manifestHasNot.isEmpty()) {
        try {
          final Manifest manifest = jarFile.getManifest();

          if (manifest == null) {
            final int errors = errorCounter.incrementAndGet();
            if (errors <= this.getLimitOutputErrors()) {
              log.error("Java can't find MANIFEST.MF in the archive");
            } else {
              log.debug("Java can't find MANIFEST.MF in the archive");
            }
          } else {

            log.debug("Detected manifest entries: " + manifest.getEntries().keySet());
            log.debug(
                "Detected manifest main attributes: " + manifest.getMainAttributes().keySet());

            for (final String key : this.manifestHas) {
              final Attributes.Name keyName = new Attributes.Name(key);
              if (manifest.getAttributes(key) == null &&
                  !manifest.getMainAttributes().containsKey(keyName)) {
                final int errors = errorCounter.incrementAndGet();
                if (errors <= this.getLimitOutputErrors()) {
                  log.error("Can't find key '" + key + "' in MANIFEST.MF");
                } else {
                  log.debug("Can't find key '" + key + "' in MANIFEST.MF");
                }
              }
            }

            for (final String key : this.manifestHasNot) {
              final Attributes.Name keyName = new Attributes.Name(key);
              if (manifest.getAttributes(key) != null ||
                  manifest.getMainAttributes().containsKey(keyName)) {
                final int errors = errorCounter.incrementAndGet();
                if (errors <= this.getLimitOutputErrors()) {
                  log.error("Detected key '" + key + "' in MANIFEST.MF");
                } else {
                  log.debug("Detected key '" + key + "' in MANIFEST.MF");
                }
              }
            }
          }
        } catch (IOException ex) {
          log.error("Can't read manifest file from the archive", ex);
          errorCounter.incrementAndGet();
        }
      }

      final AtomicInteger illegalClassVersionCounter = new AtomicInteger();
      final AtomicInteger maxFoundJavaVersion = new AtomicInteger();

      final ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9) {
        @Override
        public void visit(int version, final int access, final String name,
                          final String signature, final String superName,
                          final String[] interfaces) {
          version &= 0xFFFF;
          maxFoundJavaVersion.set(Math.max(maxFoundJavaVersion.get(), version));
          if (finalJavaVersionComparator != null) {
            if (!finalJavaVersionComparator.compare(version, javaVersion.getValue())) {
              final int foundIllegalVersionErrors = illegalClassVersionCounter.incrementAndGet();
              final int foundErrors = errorCounter.getAndIncrement();
              final boolean end = foundErrors >= getLimitOutputErrors() ||
                  foundIllegalVersionErrors >= getLimitIllegalClassVersionErrors();
              if (end) {
                if (foundIllegalVersionErrors == getLimitIllegalClassVersionErrors() + 1) {
                  log.error("more illegal class versions...");
                }
              } else {
                final JavaVersion classVersion = JavaVersion.decode(version);
                log.error("Detected class version violation " +
                    (classVersion == null ?
                        ("0x" + Integer.toHexString(version).toUpperCase(Locale.ENGLISH)) :
                        classVersion.getText()) + " : " + name.replace("/", "."));
              }
            }
          }
        }
      };

      final AntPathMatcher antPathMatcher = new AntPathMatcher();

      final Enumeration<JarEntry> entries = jarFile.entries();

      final List<String> resourcesExpected = new ArrayList<>(this.expected);
      final List<String> resourcesUnexpected = new ArrayList<>(this.unexpected);
      final List<String> detectedUnexpectedPattern = new ArrayList<>();

      while (entries.hasMoreElements() && !Thread.currentThread().isInterrupted()) {
        final JarEntry entry = entries.nextElement();

        final String path = entry.getName();

        if (!resourcesExpected.isEmpty()) {
          final Iterator<String> expectedResourcesIterator = resourcesExpected.iterator();
          while (expectedResourcesIterator.hasNext()) {
            final String pattern = expectedResourcesIterator.next();
            if (antPathMatcher.match(pattern, path)) {
              log.info("Contains " + path + " (pattern: " + pattern + ")");
              expectedResourcesIterator.remove();
            }
          }
        }

        if (!resourcesUnexpected.isEmpty()) {
          final Iterator<String> unexpectedResourcesIterator = resourcesUnexpected.iterator();
          while (unexpectedResourcesIterator.hasNext()) {
            final String pattern = unexpectedResourcesIterator.next();
            if (antPathMatcher.match(pattern, path)) {
              detectedUnexpectedPattern.add(pattern);
              final int errors = errorCounter.incrementAndGet();
              if (errors <= this.getLimitOutputErrors()) {
                log.error("Detected unexpected " + path + " (pattern: " + pattern + ")");
              } else {
                log.debug("Detected unexpected " + path + " (pattern: " + pattern + ")");
              }
              unexpectedResourcesIterator.remove();
            }
          }
        }

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

        if (!process
            || entry.isDirectory()
            || path.startsWith("META-INF/")
            || !entry.getName().toLowerCase(Locale.ENGLISH).endsWith(".class")) {
          continue;
        }

        try {
          classCounter.incrementAndGet();
          new ClassReader(
              IOUtils.readFully(jarFile.getInputStream(entry), (int) entry.getSize())).accept(
              classVisitor,
              ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException ex) {
          throw new MojoExecutionException("Can't parse JAR item: " + entry.getName(), ex);
        }
      }

      if (!resourcesExpected.isEmpty()) {
        for (final String s : resourcesExpected) {
          final int errors = errorCounter.incrementAndGet();
          if (errors <= this.getLimitOutputErrors()) {
            log.error("Can't find resource for pattern: " + s);
          } else {
            log.debug("Can't find resource for pattern: " + s);
          }
        }
      }

      if (!detectedUnexpectedPattern.isEmpty()) {
        for (final String s : detectedUnexpectedPattern) {
          final int errors = errorCounter.incrementAndGet();
          if (errors <= this.getLimitOutputErrors()) {
            log.error("Detected unexpected resource for pattern: " + s);
          } else {
            log.debug("Detected unexpected resource for pattern: " + s);
          }
        }
      }

      log.info("Processed " + classCounter.get() + " class(es)");
      if (illegalClassVersionCounter.get() > 0) {
        final JavaVersion version = JavaVersion.decode(maxFoundJavaVersion.get());
        log.info("Total detected illegal class versions " + illegalClassVersionCounter.get() +
            ", detected max JDK version "
            + (version == null ?
            " 0x" + Integer.toHexString(maxFoundJavaVersion.get()).toUpperCase(Locale.ENGLISH) :
            version.name()));
      }

      if (errorCounter.get() != 0) {
        throw new MojoFailureException("Detected " + errorCounter.get() + " error(s)");
      }
    } catch (IOException ex) {
      throw new MojoExecutionException("IO Error during archive file processing: " + archiveFile,
          ex);
    }
  }

}
