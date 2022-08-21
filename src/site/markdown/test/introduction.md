<head>
  <title>Pre-defined tests</title>
</head>

## Pre-defined tests

This library mainly contains interfaces that each test one small aspect of a class or interface, often a single method. This is done for two reasons:

* It allows testing only those methods that have been overridden / implemented. For instance, when testing a class that extends [AbstractList](https://docs.oracle.com/javase/8/docs/api/java/util/AbstractList.html), it's not necessary to test methods like [subList](https://docs.oracle.com/javase/8/docs/api/java/util/List.html#subList-int-int-) if the class doesn't override them.
* It allows nesting tests using [@Nested](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Nested.html). However, it's not required to do so.

To add tests to a class, simply implement the appropriate interface. All tests in the interface will then be added to the test class.
