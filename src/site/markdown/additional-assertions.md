<head>
  <title>Additional assertions</title>
</head>

## Additional assertions

### Predicate-based

When using `assertTrue` or `assertFalse`, you'll end up with failure messages `expected: <true> but was: <false>` or `expected: <false> but was: <true>`. While these tell you there is a failure, they don't tell you _why_.

Class [PredicateAssertions](apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/PredicateAssertions.html) provides alternatives that take a predicate and the value to apply the predicate to, and provide failure messages like `expected: matching predicate but was: <foo>` and `expected: not matching predicate but was: <foo>`. For instance:

```java
// assertTrue(StringUtils.isNotBlank(value));
assertMatches(StringUtils::isNotBlank, value);
// assertFalse(StringUtils.isBlank(value));
assertDoesNotMatch(StringUtils::isBlank, value);

// assertTrue(i > 0);
assertMatches(n -> n > 0, i);
// assertFalse(i < 0);
assertDoesNotMatch(n -> n < 0, i);
```

In case you want even more information in your failure messages, you should consider using [Hamcrest](https://hamcrest.org/JavaHamcrest/).

### IO related

Class [IOAssertions](apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/IOAssertions.html) provides some additional I/O related assertions, for instance for checking the content of a `Reader` or `InputStream` or to check the serializability of objects.

### Optional related

Class [OptionalAssertions](apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/OptionalAssertions.html) provides some additional related assertions to check if `Optional`, `OptionalInt`, `OptionalLong` and `OptionalDouble` instances are present or empty.

### Throwable related

Class [Assertions](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Assertions.html) has methods `assertThrows`, `assertThrowsExactly` and `assertDoesNotThrow` to assert that code throws or does not throw any exceptions. Class [ThrowableAssertions](apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/ThrowableAssertions.html) provides some additional assertions, for instance:

* assertions similar to `assertDoesNotThrow` that lets unchecked exceptions pass through (including `AssertionFailedError`)
* assertions similar to `assertThrows` and `assertThrowsExactly` that check for more than one exception type
* assertions similar to `assertThrows` and `assertThrowsExactly` that check for optional exceptions
* assertions for checking exception causes
* assertions for checking exception chains

#### ThrowableAsserter

In some cases, the "one-of" assertions of `ThrowableAssertions` is not sufficient, and you need different assertions for each of the possible exceptions. If that's the case, class [ThrowableAsserter](apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/ThrowableAsserter.html) can be used. Besides specifying each error type that can be thrown, for each error type the type's specific assertions must be specified. For instance, from one of the [pre-defined tests](pre-defined-tests.html):

```java
whenThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(key, function)).thenAssertNothing()
        .whenThrows(IllegalArgumentException.class).thenAssert(thrown -> assertSame(exception, thrown))
        .execute();
```
