[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.igormaznitsa/meta-annotations/badge.svg)](http://search.maven.org/#artifactdetails|com.igormaznitsa|meta-annotations|1.1.3|jar)
[![Java 6.0+](https://img.shields.io/badge/java-6.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-red.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
[![Yandex.Money donation](https://img.shields.io/badge/donation-Я.деньги-yellow.svg)](https://money.yandex.ru/embed/small.xml?account=41001158080699&quickpay=small&yamoney-payment-type=on&button-text=01&button-size=l&button-color=orange&targets=%D0%9F%D0%BE%D0%B6%D0%B5%D1%80%D1%82%D0%B2%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5+%D0%BD%D0%B0+%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D1%8B+%D1%81+%D0%BE%D1%82%D0%BA%D1%80%D1%8B%D1%82%D1%8B%D0%BC+%D0%B8%D1%81%D1%85%D0%BE%D0%B4%D0%BD%D1%8B%D0%BC+%D0%BA%D0%BE%D0%B4%D0%BE%D0%BC&default-sum=100&successURL=)

# Introduction
It is a small general-purpose library includes:
* set of run-time annotations to mark code
* set of utility classes
* shaded com.google.code.findbugs:jsr305 annotation library
* maven plugin to log info about some annotations and check java class version

# Change log

- __1.2.0 (10-dec-2024)__
 - __minimal JDK 11__
 - __minimal Maven 3.8.1__
 - removed vulnerable dependencies
 - minor renaming of methods
- use of Joda Time replaced by Java Time API
- updated dependencies and improved new JDK support
- refactoring and typo fixing

- __1.1.3 (06-jan-2019)__
 - updated the `uber-pom` dependency
 - utils: added `append` into `ArrayUtils`
 - annotations: added `UiThread` and `Critical` annotations
 - annotations: updated shaded findbugs annotations up to 3.0.2 version
 - plugin: added `check-jar` goal

# Annotations
It contains number annotations to mark code, plus JSR-305 annotations provided by the shaded findbugs annotation library.
* ImplementationNote
* UiThread
* Critical
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
* TimeComplexity
* MemoryComplexity

# How to add the annotation library into maven project
To use annotations just add dependency to the library
```
<dependency>
    <groupId>com.igormaznitsa</groupId>
    <artifactId>meta-annotations</artifactId>
    <version>1.1.3</version>
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
    <version>1.1.3</version>
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
    <version>1.1.3</version>
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
