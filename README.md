# junit-support
[![Maven Central](https://img.shields.io/maven-central/v/com.github.robtimus/junit-support)](https://search.maven.org/artifact/com.github.robtimus/junit-support)
[![Build Status](https://github.com/robtimus/junit-support/actions/workflows/build.yml/badge.svg)](https://github.com/robtimus/junit-support/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.robtimus%3Ajunit-support&metric=alert_status)](https://sonarcloud.io/summary/overall?id=com.github.robtimus%3Ajunit-support)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.github.robtimus%3Ajunit-support&metric=coverage)](https://sonarcloud.io/summary/overall?id=com.github.robtimus%3Ajunit-support)
[![Known Vulnerabilities](https://snyk.io/test/github/robtimus/junit-support/badge.svg)](https://snyk.io/test/github/robtimus/junit-support)

Contains interfaces and classes that make it easier to write unit tests with [JUnit](https://junit.org/).

A quick overview of the functionality in this library:

## Injecting resources

Instead of having to write a utility method to read the contents of a resource, simply annotate a field, constructor parameter or method parameter with `@TestResource`, and JUnit will inject the resource for you:

```
@Test
void testWithResource(@TestResource("input.json") String json) {
    // use json as needed
}
```

See [@TestResource](https://robtimus.github.io/junit-support/extension/test-resource.html) for more information, including options to configure how the resource is read or how to convert it to an object.

## Reconfigure static loggers

Loggers are often defined as `private static final` fields. That makes them difficult to mock. Using [@TestLogger](https://robtimus.github.io/junit-support/extension/test-logger.html) allows you to reconfigure these for test purposes.

## Simplify writing JUnit extensions

If you want to write a JUnit extension that performs method lookups like [@MethodSource](https://junit.org/junit5/docs/current/api/org.junit.jupiter.params/org/junit/jupiter/params/provider/MethodSource.html), [MethodLookup](https://robtimus.github.io/junit-support/extension/method-lookup.html) provides an easy to use API.

If you want to write a JUnit extension that can inject values into fields, constructor parameters or method parameters, some [base classes](https://robtimus.github.io/junit-support/extension/injecting-extensions.html) are provided that let you focus on what's important - provide the values to inject.

## Predefined tests

This library mainly contains several [pre-defined tests](https://robtimus.github.io/junit-support/pre-defined-tests.html), defined in interfaces that each test one small aspect of a class or interface, often a single method. This makes it easier to test custom implementations of various common interfaces or base classes. The currently supported list is:

* [Collection](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/collections/CollectionTests.html), both modifiable and unmodifiable
* [Iterable](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/collections/IterableTests.html)
* [Iterator](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/collections/IteratorTests.html), both modifiable and unmodifiable
* [List](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/collections/ListTests.html) and [ListIterator](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/collections/ListIteratorTests.html), both modifiable and unmodifiable
* [Map](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/collections/MapTests.html) and [Map.Entry](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/collections/MapEntryTests.html), both modifiable and unmodifiable
* [Set](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/collections/SetTests.html), both modifiable and unmodifiable
* [Spliterator](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/collections/SpliteratorTests.html)
* [InputStream, OutputStream, Reader and Writer](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/io/package-summary.html)

In addition, there are pre-defined tests for [MethodDelegation](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/DelegateTests.html) and [covariant return types](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/test/CovariantReturnTests.html).

## Additional assertions

Several [additional assertions](https://robtimus.github.io/junit-support/additional-assertions.html) are provided. Some examples:

* alternatives to `assertTrue` and `assertFalse` that provide better failure messages
* assertions for checking the content of a `Reader` or `InputStream`
* assertions for `Optional`, `OptionalInt`, `OptionalLong` and `OptionalDouble`
* assertions for exception causes
* assertions for code that can can throw more than one different types of exceptions
* assertions for code that optionally throws an exception

## Parameterized test support

[JUnit Pioneer](https://junit-pioneer.org/) has [@CartesianTest](https://junit-pioneer.org/docs/cartesian-product/) to provide the Cartesian product of sets of arguments. Using `@CartesianTest.MethodFactory` allows you to create argument sets programmatically. It does not provide the possibility to filter out combinations though. Class [ArgumentsCombiner](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/params/ArgumentsCombiner.html) works like JUnit Pioneer's `ArgumentSets` class but allows filtering out combinations.
