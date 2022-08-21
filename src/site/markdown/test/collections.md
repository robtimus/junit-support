<head>
  <title>Pre-defined Collections framework tests</title>
</head>

## Pre-defined Collections framework tests

Package [com.github.robtimus.junit.support.test.collections](../apidocs/com/github/robtimus/junit/support/test/collections/package-summary.html) contains tests for the following interfaces in the Collections Framework:

* [Collection](../apidocs/com/github/robtimus/junit/support/test/collections/CollectionTests.html)
* [Iterable](../apidocs/com/github/robtimus/junit/support/test/collections/IterableTests.html)
* [Iterator](../apidocs/com/github/robtimus/junit/support/test/collections/IteratorTests.html)
* [Enumeration](../apidocs/com/github/robtimus/junit/support/test/collections/EnumerationTests.html)
* [List](../apidocs/com/github/robtimus/junit/support/test/collections/ListTests.html) and [ListIterator](../apidocs/com/github/robtimus/junit/support/test/collections/ListIteratorTests.html)
* [Map](../apidocs/com/github/robtimus/junit/support/test/collections/MapTests.html) and [Map.Entry](../apidocs/com/github/robtimus/junit/support/test/collections/MapEntryTests.html)
* [Set](../apidocs/com/github/robtimus/junit/support/test/collections/SetTests.html)
* [Spliterator](../apidocs/com/github/robtimus/junit/support/test/collections/SpliteratorTests.html)

For `Collection`, `Iterator`, `List`, `ListIterator`, `Map`, `Map.Entry` and `Set` there are also tests for unmodifiable versions of these interfaces. By implementing regular (modifiable) test interfaces for one set of operations and unmodifiable test interfaces for another set, it's easy to test implementations that support some operations but not others.

### Examples

See [here](https://github.com/robtimus/junit-support/tree/master/src/test/java/com/github/robtimus/junit/support/examples/collections) for examples on using the interfaces in this library to create test classes.
