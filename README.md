# junit-support

Contains interfaces and classes that make it easier to write unit tests with [JUnit](https://junit.org/).

## Predefined tests

This library mainly contains interfaces that each test one small aspect of a class or interface, often a single method. This is done for two reasons:

* It allows testing only those methods that have been overridden / implemented. For instance, when testing a class that extends [AbstractList](https://docs.oracle.com/javase/8/docs/api/java/util/AbstractList.html), it's not necessary to test methods like [subList](https://docs.oracle.com/javase/8/docs/api/java/util/List.html#subList-int-int-) if the class doesn't override them.
* It allows nesting tests using [@Nested](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Nested.html). However, it's not required to do so.

To add tests to a class, simply implement the appropriate interface. All tests in the interface will then be added to the test class.

### Collection tests

Package [com.github.robtimus.junit.support.collections](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/collections/package-summary.html) contains tests for the following interfaces in the Collections Framework:

* [Collection](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/collections/CollectionTests.html)
* [Iterable](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/collections/IterableTests.html)
* [Iterator](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/collections/IteratorTests.html)
* [List](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/collections/ListTests.html) and [ListIterator](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/collections/ListIteratorTests.html)
* [Map](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/collections/MapTests.html) and [Map.Entry](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/collections/MapEntryTests.html)
* [Set](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/collections/SetTests.html)
* [Spliterator](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/collections/SpliteratorTests.html)

For `Collection`, `Iterator`, `List`, `ListIterator`, `Map`, `Map.Entry` and `Set` there are also tests for unmodifiable versions of these interfaces. By implementing regular (modifiable) test interfaces for one set of operations and unmodifiable test interfaces for another set, it's easy to test implementations that support some operations but not others.

### I/O tests

Package [com.github.robtimus.junit.support.io](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/io/package-summary.html) contains tests for input streams, output streams, readers and writers.

### Testing method delegation

[DelegateTests](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/DelegateTests.html) makes it relatively easy to test that objects delegate to objects of the same type.

### Testing method overrides with covariant return types

[CovariantReturnTests](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/CovariantReturnTests.html) makes it relatively easy to test that classes override all fluent methods (methods returning `this`) to change the return type. It's also possible to use a different return type to check on.

### Disabling tests

Sometimes it's necessary to disable a test, e.g. because it doesn't apply to the class to test. An example is testing a partial sub list of an empty list; the only sub list to return is the full list. To disable a test, simply override it and don't apply any (JUnit) annotation to the method; this will make JUnit ignore the test.

### Examples

See [here](https://github.com/robtimus/junit-support/tree/master/src/test/java/com/github/robtimus/junit/support/examples) for examples on using the interfaces in this library to create test classes.

## Additional assertions

Classes [AdditionalAssertions](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/AdditionalAssertions.html) and [IOAssertions](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/io/IOAssertions.html) provide some additional assertions that can be used in unit tests. Some examples:

* `AdditionalAssertions` provides assertions similar to `assertDoesNotThrow` that lets unchecked exceptions to pass through (including `AssertionFailedError`)
* `AdditionalAssertions` provides assertions for checking exception causes
* `AdditionalAssertions` provides assertions for `Optional`
* `AdditionalAssertions` provides assertions similar to `assertThrows` that check for more than one exception type
* `AdditionalAssertions` provides assertions similar to `assertThrows` that check for optional exceptions
* `IOAssertions` provides assertions for checking the content of `Reader` or `InputStream` instances
* `IOAssertions` provides assertions for testing that objects are or are not serializable

## Injecting resources

A lot of people have one or more utility methods like this:

    private static String readResource(String name) {
        StringBuilder sb = new StringBuilder();
        try (Reader input = new InputStreamReader(MyClassTest.class.getResourceAsStream(name), StandardCharsets.UTF_8)) {
            char[] buffer = new char[4096];
            int len;
            while ((len = input.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb.toString();
    }

Instead of having to write this boilerplate code for every project (and sometimes for each test), annotate fields, constructor arguments or method arguments with [TestResource](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/extension/testresource/TestResource.html) to inject a Java resource into the field, constructor argument or method argument, as `String`, `CharSequence`, `StringBuilder` or `byte[]`:

    @Test
    void testWithResource(@Resource("input.json") String json) {
        // use json as needed
    }

Combine `TestResource` with [LoadWith](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/extension/testresource/LoadWith.html) to provide your own resource-to-object conversion:

    @Test
    void testWithResource(@Resource("person.json") @LoadWith("jsonToPerson") Person person) {
        // use person as needed
    }

    Person jsonToPerson(Reader reader) throws IOException {
        // perform conversion, e.g. using Jackson or Gson
    }

A specialized version of `@LoadWith` is provided for `Properties` objects, [AsProperties](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/extension/testresource/AsProperties.html):

    // inject as a static field
    @TestResource("test.properties")
    @AsProperties
    private static Properties testProperties;

Note that the resource name is relative to the class that defines the method. Use a leading `/` to start from the root of the class path.

## Utility methods for working with @TempDir

JUnit 4's [TemporaryFolder](https://junit.org/junit4/javadoc/4.13/org/junit/rules/TemporaryFolder.html) had some utility methods to create files and folders inside the temporary folder. JUnit 5's [TempDir](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/io/TempDir.html) injects a `Path` or `File`, and is therefore missing all of these utility methods. Class [TempDirUtils](https://robtimus.github.io/junit-support/apidocs/com/github/robtimus/junit/support/extension/tempdir/TempDirUtils.html) adds most of these methods as static methods.
