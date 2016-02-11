[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Java 6.0+](https://img.shields.io/badge/java-6.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-red.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)

It is a small general-purpose library includes:
* set of run-time annotations to mark code
* shaded com.google.code.findbugs:jsr305 annotation library
* maven plugin to print info about the annotations into log

#Annotations
It contains number annotations to mark code, plus JSR-305 annotations provided by the shaded findbugs annotation library.
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

#How to add the library into maven project
To use annotations just add dependency to the library
```
<dependency>
    <groupId>com.igormaznitsa</groupId>
    <artifactId>meta-common</artifactId>
    <version>1.0</version>
</dependency>
```

#How to use the maven plugin
Just add the lines below into build section.
```
<plugin>
    <groupId>com.igormaznitsa</groupId>
    <artifactId>meta-checker</artifactId>
    <version>1.0</version>
    <configuration>
        <enforceAlerts>true</enforceAlerts>
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