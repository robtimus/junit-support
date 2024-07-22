<head>
  <title>@TestLogger</title>
</head>

## Logging on test failures

Use [@LogOnFailure](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/logonfailure/LogOnFailure.html) to easily suppress logging for successful tests but not for failed tests.

### Supported logging frameworks

The following logging framework implementations are supported:

#### java.util.logging

Any field with a value of type [java.util.logging.Logger](https://docs.oracle.com/en/java/javase/21/docs/api/java.logging/java/util/logging/Logger.html) is supported.

#### Log4j

Any field with a value of type [org.apache.logging.log4j.core.Logger](https://logging.apache.org/log4j/2.x/javadoc/log4j-core/org/apache/logging/log4j/core/Logger.html) is supported. This means that using [Log4j 2.x](https://logging.apache.org/log4j/2.x/) with `org.apache.logging.log4j:log4j-core` as implementation allows you to simply use [LogManager](https://logging.apache.org/log4j/2.x/javadoc/log4j-api/org/apache/logging/log4j/LogManager.html)` to assign the value. Using different implementations (e.g. `org.apache.logging.log4j:log4j-jul`) is **not** supported.

#### Logback

Any field with a value of type [ch.qos.logback.classic.Logger](https://logback.qos.ch/apidocs/ch.qos.logback.classic/ch/qos/logback/classic/Logger.html) is supported. Since logback is a native SLF4J implementation, this allows you to simply use [LoggerFactory](https://www.slf4j.org/apidocs/org/slf4j/LoggerFactory.html)` to assign the value. Using different bindings is **not** supported.

#### Reload4j

Any field with a value of type [org.apache.log4j.Logger](https://reload4j.qos.ch/apidocs/org/apache/log4j/Logger.html) is supported.

Since reload4j is a fork of Log4j 1.2.17, `org.apache.log4j.Logger` should also be usable for when using Log4j 1.2.17.

#### API based frameworks

For API based frameworks like SLF4J and Log4j, the logger to use depends on the binding. For instance, use `java.util.logging.Logger` if the binding is `org.slf4j:slf4j-jdk14` or `org.apache.logging.log4j:log4j-jul`, etc.
