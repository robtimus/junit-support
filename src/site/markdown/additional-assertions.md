<head>
  <title>Additional assertions</title>
</head>

## Additional assertions

### IO related

Class [IOAssertions](apidocs/com/github/robtimus/junit/support/IOAssertions.html) provides some additional I/O related assertions, for instance for checking the content of a `Reader` or `InputStream` or to check the serializability of objects.

### Optional related

Class [OptionalAssertions](apidocs/com/github/robtimus/junit/support/OptionalAssertions.html) provides some additional related assertions to check if `Optional`, `OptionalInt`, `OptionalLong` and `OptionalDouble` instances are present or empty.

### Throwable related

Class [Assertions](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Assertions.html) has methods `assertThrows`, `assertThrowsExactly` and `assertDoesNotThrow` to assert that code throws or does not throw any exceptions. Class [ThrowableAssertions](apidocs/com/github/robtimus/junit/support/ThrowableAssertions.html) provides some additional assertions, for instance:

* assertions similar to `assertDoesNotThrow` that lets unchecked exceptions to pass through (including `AssertionFailedError`)
* assertions similar to `assertThrows` and `assertThrowsExactly` that check for more than one exception type
* assertions similar to `assertThrows` and `assertThrowsExactly` that check for optional exceptions
* assertions for checking exception causes

#### ThrowableAsserter

In some cases, the "one-of" assertions of `ThrowableAssertions` is not sufficient, and you need different assertions for each of the possible exceptions. If that's the case, class [ThrowableAsserter](apidocs/com/github/robtimus/junit/support/ThrowableAsserter.html) can be used. Besides specifying each error type that can be thrown, for each error type the type's specific assertions must be specified. For instance, from one of the [pre-defined tests](pre-defined-tests.html):

```
executing(() -> map.computeIfAbsent(key, function))
        .whenThrows(UnsupportedOperationException.class).thenAssertNothing()
        .whenThrows(IllegalArgumentException.class).thenAssert(thrown -> assertSame(exception, thrown))
        .runAssertions();
```
