# unit-test-support

Contains interfaces and classes that make it easier to write unit tests with [JUnit](https://junit.org/).

This library mainly contains interfaces that each test one small aspect of a class or interface, often a single method. This is done for two reasons:

* It allows testing only those methods that have been overridden / implemented. For instance, when extending [AbstractList](https://docs.oracle.com/javase/8/docs/api/java/util/AbstractList.html), it's not necessary to test methods like [subList](https://docs.oracle.com/javase/8/docs/api/java/util/List.html#subList-int-int-) if the class to test doesn't override them.
* It allows nesting tests using [@Nested](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Nested.html).

To add tests to a class, simply implement the appropriate interface. All tests in the interface will then be added to the test class.

## Collection tests

Package [com.github.robtimus.unittestsupport.collections](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/collections/package-summary.html) contains tests for the following interfaces in the Collections Framework:

* [Collection](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/collections/CollectionTests.html)
* [Iterable](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/collections/IterableTests.html)
* [Iterator](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/collections/IteratorTests.html)
* [List](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/collections/ListTests.html)
* [ListIterator](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/collections/ListIteratorTests.html)
* [Map](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/collections/MapTests.html) and [Map.Entry](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/collections/MapEntryTests.html)
* [Set](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/collections/SetTests.html)
* [Spliterator](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/collections/SpliteratorTests.html)

For `Collection`, `Iterator`, `List`, `ListIterator`, `Map`, `Map.Entry` and `Set` there are also tests for unmodifiable versions of these interfaces. By implementing regular (modifiable) test interfaces for one set of operations and unmodifiable test interfaces for another set, it's easy to test implementations that support some operations but not others.

## I/O tests

Package [com.github.robtimus.unittestsupport.io](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/io/package-summary.html) contains tests for input streams, output streams, readers and writers.

## Testing method delegation

[DelegateTests](https://robtimus.github.io/unit-test-support/apidocs/com/github/robtimus/unittestsupport/DelegateTests.html) makes it relatively easy to test that objects delegate to objects of the same type.

## Disabling tests

Sometimes it's necessary to disable a test, e.g. because it doesn't apply to the class to test. An example is testing a partial sub list of an empty list; the only sub list to return is the full list. To disable a test, simply override it and don't add apply any (JUnit) annotation to the method; this will make JUnit ignore the test.

## Examples

See [here](https://github.com/robtimus/unit-test-support/tree/master/src/test/java/com/github/robtimus/unittestsupport/examples) for examples on using the interfaces in this library to create test classes.
