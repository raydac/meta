[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.igormaznitsa/meta-annotations/badge.svg)](http://search.maven.org/#artifactdetails|com.igormaznitsa|meta-annotations|1.1.1|jar)
[![Java 6.0+](https://img.shields.io/badge/java-6.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-red.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)

# Introduction
It is a small general-purpose library includes:
* set of run-time annotations to mark code
* set of utility classes
* shaded com.google.code.findbugs:jsr305 annotation library
* maven plugin to log info about some annotations and check java class version

# Change log
* __1.1.2-SNAPSHOT__
 * annotations : added annotations to describe time and memory complexity : `@TimeComplexity` and `@MemoryComplexity`
 * plugin : fixed false positive for constructors of nested classes
 * plugin : fixed successful completion with non-zero error counter
 * plugin : fixed nested class name extraction for log
 * plugin : added check for placing of `@Nullable` or `@NonNullable` on non-object return type
 * plugin : added check for max allowed levels for `@Weight`, `@TimeComplexity` and `@MemoryComplexity` and it throws error if rule is violated
 * utils  : added `StrUtils`

* __1.1.1 (31-mar-2016)__
 * added Assertions#assertEquals
 * fixed issue #1
 * fixed checking of @MayContainNull and @MustNotContainNull for fields
 * little improvement in JCL check in @Constraint
 * added GetUtils#ensureNonNullAndNonEmpty and GetUtils#ensureNonNullStr for String

* __1.1.0 (19-mar-2016)__
 * __utilities and auxiliary classes extracted as separated module meta-utils__
 * __meta-common module renamed to meta-annotations__
 * __annotation classes compiled in JVM 1.5 format__
 * improved maven plugin to check marks for method arguments, NB! It also supports @Nullable and @NotNull annotations from IDEA annotation pack.
 * changed retention policy for annotations to CLASS, but shaded annotations from JR-305 have retention policy RUNTIME
 * extended scope for many annotations
 * improved utility methods and assertions
 * added annotations @Experimental, @ThrowsRuntimeException, @ThrowsRuntimeExceptions
 * bugfixing and refactoring, also fixed NPE in the maven plugin for missing class format option

* __1.0.2 (06-mar-2016)__
 * added Assertions.assertIsValid method
 * improved logging in meta-checker plugin
 * added empty arrays for primitives into Arrays class
 * fixed KeepCreationPointTemplate, also added thread id info
 * added ImplementationNote annotation
 * improved the meta-check plugin, added support to check java class version 

* __1.0.1 (12-feb-2016)__
 * Bug fixing

* __1.0 (10-feb-2016)__
 * Initial version

#Annotations
It contains number annotations to mark code, plus JSR-305 annotations provided by the shaded findbugs annotation library.
* ImplementationNote
* Constraint
* Determined
* NonDetermined
* LazyInited
* Link
* MayContainNull
* MustNotContainNull
* NeedsRefactoring
* OneWayChange
* ReturnsOriginal
* Risky
* ToDo
* Warning
* Weight
* ThrowsRuntimeException
* ThrowsRuntimeExceptions
* Experimental

# How to add the annotation library into maven project
To use annotations just add dependency to the library
```
<dependency>
    <groupId>com.igormaznitsa</groupId>
    <artifactId>meta-annotations</artifactId>
    <version>1.1.1</version>
</dependency>
```
It shades JSR-305 annotations from the FindBugs library so that they also will be available for usage automatically.

# Utilities
Since 1.1.0 utility classes extracted into separated module which is available in maven central
Just add the lines below into build section.
```
<dependency>
    <groupId>com.igormaznitsa</groupId>
    <artifactId>meta-utils</artifactId>
    <version>1.1.1</version>
</dependency>
```
## com.igormaznitsa.meta.common.utils.Deferrers
It allows to defer some operations, like it works in Go but unfortunately the call to process all deferred operations must be placed into try...finally block to ensure the call.
It checks stack frames and all deferred operations will be processed by `Deferrers.processDeferredActions()` only for actual stack depth, it means that deferred operations added on higher stack levels will be ignored.
```
    try {
      // it processes runnable, the code will be executed
      Deferrers.defer(new Runnable() {public void run() { System.out.println("Hello world");}});
      final InputStream is = new FileInputStream("/home/test/nonexistfile.txt");
      // it processes closeable, registered closeable object will be closed automatically
      Deferrers.defer(is);
    }
    finally {
      Deferrers.processDeferredActions();
    }
```
## com.igormaznitsa.meta.common.utils.TimeGuard
```
    final TimeGuard.TimeAlertListener listener = new TimeGuard.TimeAlertListener() {
      @Override
      public void onTimeAlert(long l, TimeGuard.TimeData td) {
        System.out.println("Too long delay for " + td.getAlertMessage());
      }
    };

    try {
      TimeGuard.addGuard("Checkpoint1", 100L, listener);
      Thread.sleep(200L);
    }
    finally {
      TimeGuard.check();
    }
```

# How to use the maven plugin
I have also published some maven plugin which allows to check compiled classes for the annotations and print some information and check that methods marked by nullable and nonnull annotations.
Also the plugin allows to fail build process if detected some annotations, it allows to avoid publishing of project with to-do or experimental stuff.
```
<plugin>
    <groupId>com.igormaznitsa</groupId>
    <artifactId>meta-checker</artifactId>
    <version>1.1.1</version>
    <configuration>
        <restrictClassFormat>7</restrictClassFormat>
        <failForAnnotations>
            <param>risky</param>
        </failForAnnotations>
    </configuration>
    <executions>
        <execution>
            <goals>
              <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
