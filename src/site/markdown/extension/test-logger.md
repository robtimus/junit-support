<head>
  <title>@TestLogger</title>
</head>

## @TestLogger

Loggers are often defined as `private static final` fields. That makes them difficult to mock.

Using [@TestLogger](../apidocs/com/github/robtimus/junit/support/extension/testlogger/TestLogger.html) you can inject a so-called _logger context_ that allows you to configure a logger:

* Setting the level
* Add, remove or replace appenders / handlers
* Enable or disable inheriting appenders / handlers from the parent logger

Where `@TestLogger` requires the logger name to be given, [@TestLogger.ForClass](../apidocs/com/github/robtimus/junit/support/extension/testlogger/TestLogger.ForClass.html) can be used to define the logger using a class literal. [@TestLogger.Root](../apidocs/com/github/robtimus/junit/support/extension/testlogger/TestLogger.Root.html) can be used for the root logger.

### Supported logging frameworks

The following logging framework implementations are supported:

* `java.util.logging`, represented by class [JdkLoggerContext](../apidocs/com/github/robtimus/junit/support/extension/testlogger/JdkLoggerContext.html)
* [Log4j 2.x](https://logging.apache.org/log4j/2.x/) (core), represented by class [Log4jLoggerContext](../apidocs/com/github/robtimus/junit/support/extension/testlogger/Log4jLoggerContext.html)
* [Logback](https://logback.qos.ch/), represented by class [LogbackLoggerContext](../apidocs/com/github/robtimus/junit/support/extension/testlogger/LogbackLoggerContext.html)
* [reload4j](https://reload4j.qos.ch/), represented by class [Reload4jLoggerContext](../apidocs/com/github/robtimus/junit/support/extension/testlogger/Reload4jLoggerContext.html)
    * Since reload4j is a fork of Log4j 1.2.17, `Reload4jLoggerContext` can also be used for that

#### Delegating logging frameworks

Some frameworks, like SLF4J, Log4j 2.x and Apache Commons Logging (JCL), delegate to other logging frameworks for the actual implementation. It's the implementation that determines which logging context class to use. For instance, when using `slf4j-reload4j`, you should use `Reload4jLoggerContext`; when using `log4j-jul`, you should use `JdkLoggerContext`, etc.

### Log4j appender limitations

While appenders / handlers for `java.util.logging`, logback and reload4j can easily be provided as mocks, the same is not true for Log4j for a few reasons:

* Appenders must have a name
* Appenders should be started
* The event passed to appenders may be mutable and shared; capturing them may result in unexpected results

Class [Log4jNullAppender](../apidocs/com/github/robtimus/junit/support/extension/testlogger/Log4jNullAppender.html) has been created to overcome these issues. `Log4jNullAppender.create` creates a named, started appender. Its `append` method has been implemented to turn the event into an immutable event, which is then passed to the `ignore` method. This method is safe to be _spied_ upon:

```java
Log4jNullAppender appender = spy(Log4jNullAppender.create("mock"));

// perform calls that will trigger the appender

ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
verify(appender, atLeastOnce()).ignore(eventCaptor.capture());
List<LogEvent> events = eventCaptor.getAllValues();
// perform assertions on events
```

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
