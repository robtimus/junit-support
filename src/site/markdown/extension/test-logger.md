<head>
  <title>@TestLogger</title>
</head>

## @TestLogger

Loggers are often defined as `private static final` fields. That makes them difficult to mock.

Using [@TestLogger](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/testlogger/TestLogger.html), [@TestLogger.ForClass](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/testlogger/TestLogger.ForClass.html) or [@TestLogger.Root](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/testlogger/TestLogger.Root.html) you can inject a so-called _logger context_ that allows you to configure a logger:

* Setting the level
* Add, remove or replace appenders / handlers
* Enable or disable inheriting appenders / handlers from the parent logger
* Capturing logging events / records

When the logger context goes out of scope (when injected as a field or method parameter, this is when the test ends), all original settings are restored.

### Supported logging frameworks

The following logging framework implementations are supported:

#### java.util.logging

Class [JdkLoggerContext](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/testlogger/JdkLoggerContext.html) can be used for the logger context when `java.util.logging` is used as logging implementation. This is true not only when using `java.util.logging.Logger` directly, but also when `java.util.logging` is used through dependencies like `org.slf4j:slf4j-jdk14` or `org.apache.logging.log4j:log4j-jul`.

#### Log4j core

Class [Log4jLoggerContext](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/testlogger/Log4jLoggerContext.html) can be used for the logger context when [Log4j 2.x](https://logging.apache.org/log4j/2.x/) is used as logging implementation. This means that `org.apache.logging.log4j:log4j-core` must be used. When using `org.apache.logging.log4j:log4j-api` with a different implementation (e.g. `org.apache.logging.log4j:log4j-jul`), class `Log4jLoggerContext` can **not** be used.

##### Appender mocking

Unlike appenders / handlers for other logging frameworks, Log4j appenders cannot be easily provided as mocks for the following reasons:

* Appenders must have a name
* Appenders should be started
* The event passed to appenders may be mutable and shared; capturing them may result in unexpected results

Class [Log4jNullAppender](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/testlogger/Log4jNullAppender.html) has been created to overcome these issues. `Log4jNullAppender.create` creates a named, started appender. Its `append` method has been implemented to turn the event into an immutable event, which is then passed to the `ignore` method. This method is safe to be _spied_ upon:

```java
Log4jNullAppender appender = spy(Log4jNullAppender.create("mock"));

// perform calls that will trigger the appender

ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
verify(appender, atLeastOnce()).ignore(eventCaptor.capture());
List<LogEvent> events = eventCaptor.getAllValues();
// perform assertions on events
```

#### Logback

Class [LogbackLoggerContext](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/testlogger/LogbackLoggerContext.html) can be used for the logger context when [Logback](https://logback.qos.ch/) is used as logging implementation. Since logback is a native SLF4J implementation, SLF4J should not have any other bindings.

#### Reload4j

Class [Reload4jLoggerContext](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/testlogger/Reload4jLoggerContext.html) can be used for the logger context when [reload4j](https://reload4j.qos.ch/) is used as logging implementation. This is true not only when using reload4 directly, but also when reload4j is used through dependencies like `org.slf4j:slf4j-reload4j`.

Since reload4j is a fork of Log4j 1.2.17, `Reload4jLoggerContext` should also be usable for when using Log4j 1.2.17 (possibly through dependency `org.slf4j:slf4j-log4j12`).

#### API based frameworks

For API based frameworks like SLF4J and Log4j, the logger context class to used depends on the binding. For instance, use class [JdkLoggerContext](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/testlogger/JdkLoggerContext.html) if the binding is `org.slf4j:slf4j-jdk14` or `org.apache.logging.log4j:log4j-jul`, etc.

### Examples

Using method injection:

```java
@Test
void testLogging(@TestLogger.ForClass(MyClass.class) Reload4jLoggerContext loggerContext) {
    Appender appender = mock(Appender.class);
    loggerContext
            .setLevel(Level.INFO)
            .useParentAppenders(false)
            .setAppender(appender);

    // perform calls that trigger the logger

    ArgumentCaptor<LoggingEvent> eventCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
    verify(appender, atLeastOnce()).doAppend(eventCaptor.capture());
    List<LoggingEvent> events = eventCaptor.getAllValues();
    // perform assertions on events
}
```

Using field injection:

```java
@TestLogger.ForClass(MyClass.class)
private Reload4jLoggerContext loggerContext;

private Appender appender;

@BeforeEach
void configureLogger() {
    appender = mock(Appender.class);
    loggerContext
            .setLevel(Level.INFO)
            .useParentAppenders(false)
            .setAppender(appender);
}

@Test
void testLogging() {
    // perform calls that trigger the logger

    ArgumentCaptor<LoggingEvent> eventCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
    verify(appender, atLeastOnce()).doAppend(eventCaptor.capture());
    List<LoggingEvent> events = eventCaptor.getAllValues();
    // perform assertions on events
}
```

The above can also be achieved as follows:

```java
@Test
void testLogging(@TestLogger.ForClass(MyClass.class) Reload4jLoggerContext loggerContext) {
    LogCaptor<LoggingEvent> logCaptor = loggerContext
            .setLevel(Level.INFO)
            .useParentAppenders(false)
            .capture();

    // perform calls that trigger the logger

    List<LoggingEvent> events = logCaptor.logged();
    // perform assertions on events
}
```
