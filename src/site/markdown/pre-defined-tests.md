<head>
  <title>Pre-defined tests</title>
</head>

## Pre-defined tests

This library mainly contains interfaces that each test one small aspect of a class or interface, often a single method. This is done for two reasons:

* It allows testing only those methods that have been overridden / implemented. For instance, when testing a class that extends [AbstractList](https://docs.oracle.com/javase/8/docs/api/java/util/AbstractList.html), it's not necessary to test methods like [subList](https://docs.oracle.com/javase/8/docs/api/java/util/List.html#subList-int-int-) if the class doesn't override them.
* It allows nesting tests using [@Nested](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Nested.html). However, it's not required to do so.

To add tests to a class, simply implement the appropriate interface. All tests in the interface will then be added to the test class.

### Collections framework

Package [com.github.robtimus.junit.support.test.collections](apidocs/com/github/robtimus/junit/support/test/collections/package-summary.html) contains tests for the following interfaces in the Collections Framework:

* [Collection](apidocs/com/github/robtimus/junit/support/test/collections/CollectionTests.html)
* [Iterable](apidocs/com/github/robtimus/junit/support/test/collections/IterableTests.html)
* [Iterator](apidocs/com/github/robtimus/junit/support/test/collections/IteratorTests.html)
* [Enumeration](apidocs/com/github/robtimus/junit/support/test/collections/EnumerationTests.html)
* [List](apidocs/com/github/robtimus/junit/support/test/collections/ListTests.html) and [ListIterator](apidocs/com/github/robtimus/junit/support/test/collections/ListIteratorTests.html)
* [Map](apidocs/com/github/robtimus/junit/support/test/collections/MapTests.html) and [Map.Entry](apidocs/com/github/robtimus/junit/support/test/collections/MapEntryTests.html)
* [Set](apidocs/com/github/robtimus/junit/support/test/collections/SetTests.html)
* [Spliterator](apidocs/com/github/robtimus/junit/support/test/collections/SpliteratorTests.html)

For `Collection`, `Iterator`, `List`, `ListIterator`, `Map`, `Map.Entry` and `Set` there are also tests for unmodifiable versions of these interfaces. By implementing regular (modifiable) test interfaces for one set of operations and unmodifiable test interfaces for another set, it's easy to test implementations that support some operations but not others.

[Examples](https://github.com/robtimus/junit-support/tree/master/src/test/java/com/github/robtimus/junit/support/examples/collections)

### I/O

Package [com.github.robtimus.junit.support.test.io](apidocs/com/github/robtimus/junit/support/test/io/package-summary.html) contains tests for input streams, output streams, readers and writers.

[Examples](https://github.com/robtimus/junit-support/tree/master/src/test/java/com/github/robtimus/junit/support/examples/io)

### Method delegation

[DelegateTests](apidocs/com/github/robtimus/junit/support/test/DelegateTests.html) makes it relatively easy to test that objects delegate to objects of the same type.

[Examples](https://github.com/robtimus/junit-support/tree/master/src/test/java/com/github/robtimus/junit/support/examples/delegation)

### Covariant return type

[CovariantReturnTests](apidocs/com/github/robtimus/junit/support/test/CovariantReturnTests.html) makes it relatively easy to test that classes override all fluent methods (methods returning `this`) to change the return type. It's also possible to use a different return type to check 

[Examples](https://github.com/robtimus/junit-support/tree/master/src/test/java/com/github/robtimus/junit/support/examples/covariantreturn)

### Disabling pre-defined tests

Sometimes it's necessary to disable a test, e.g. because it doesn't apply to the class to test. An example is testing a partial sub list of an empty list; the only sub list to return is the full list. To disable a test, simply override it and don't apply any JUnit annotation to the method; this will make JUnit ignore the test.
