<head>
  <title>@TestResource</title>
</head>

## @TestResource

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

Instead of having to write this boilerplate code for every project (and sometimes for multiple test classes inside the same project), annotate fields, constructor parameters or method parameters with [@TestResource](../apidocs/com/github/robtimus/junit/support/extension/testresource/TestResource.html) to inject a Java resource into the field, constructor parameter or method parameter.

Note that the resource name is relative to the class that defines the method. Use a leading `/` to start from the root of the class path.

### Injecting byte[]

The simplest form is to inject into a field, constructor parameter or method parameter of type `byte[]`. Just annotate the injection target with `@TestResource`:

```
@Test
void testWithResource(@TestResource("my-file.bin") byte[] myFile) {
    // use myFile as needed
}
```

### Injecting String, CharSequence or StringBuilder

Injecting into a field, constructor parameter or method parameter of type `String`, `CharSequence` or `StringBuilder` works the same as for `byte[]`. In addition, the encoding and line separators can be specified.

#### Specifying the encoding

By default, resources are loaded as UTF-8. In some cases this may not be the correct encoding. There are two ways to specify the encoding to use:

* Use [@Encoding](../apidocs/com/github/robtimus/junit/support/extension/testresource/Encoding.html). This annotation can be placed on the injection target, or any enclosing element. For instance, for a method parameter, it can be placed on the method parameter itself, the method, or the class that defines the method. If that class is nested inside another class, that class is also checked, up to the root class.
* Use the `com.github.robtimus.junit.support.extension.testresource.encoding` JUnit configuration parameter to define the default encoding, in case no `@Encoding` annotation is found. This configuration parameter can have the following values:
    * `DEFAULT` for `Charset.defaultCharset()`
    * `SYSTEM` for system property `file.encoding`; if this system property is not set, an error will be thrown
    * `NATIVE` for system property `native.encoding`, which is available since Java 17; if this system property is not set, an error will be thrown
    * The name of a charset for that specific charset

For instance:

```
@Test
void testWithResource(@TestResource("my-file.txt") @Encoding("ISO-8859-1") String myFile) {
    // myFile has been loaded using the ISO-8859-1 charset
}
```


#### Specifying the line separator

By default, the resource is loaded as-is. This may cause issues when tests are run on platforms with different line separators, for example Linux and Windows. There are two ways to specify the line separator to use:

* Use [@EOL](../apidocs/com/github/robtimus/junit/support/extension/testresource/EOL.html). This annotation can be placed on the injection target, or any enclosing element. For instance, for a method parameter, it can be placed on the method parameter itself, the method, or the class that defines the method. If that class is nested inside another class, that class is also checked, up to the root class.
* Use the `com.github.robtimus.junit.support.extension.testresource.lineSeparator` JUnit configuration parameter to define the default line separator, in case no `@EOL` annotation is found. This configuration parameter can have the following values:
    * `LF` for `\n`
    * `CR` for `\r`
    * `CRLF` for `\r\n`
    * `SYSTEM` for `System.lineSeparator()`
    * `ORIGINAL` for line separators from the original file; this is the default setting
    * `NONE` for no line separators
    * A literal value for that specific line separator

For instance:

```
@Test
void testWithResource(@TestResource("my-file.txt") @EOL(EOL.LF) String myFile) {
    // myFile uses \n as line separator, no matter the platform
}
```

### Custom resource-to-object conversion

Combine `@TestResource` with [@LoadWith](../apidocs/com/github/robtimus/junit/support/extension/testresource/LoadWith.html) to provide your own resource-to-object conversion. For instance:

```
@Test
void testWithResource(@TestResource("person.xml") @LoadWith("xmlToPerson") Person person) {
    // use person as needed
}

Person xmlToPerson(Reader reader) throws IOException {
    // perform conversion, e.g. using JAXB
}
```

The method reference in the annotation can come in the following formats:

| Format                                                   | Description                                                                       |
|----------------------------------------------------------|-----------------------------------------------------------------------------------|
|`<methodName>`                                            | A method in the test class itself                                                 |
|`<methodName(<parameterTypes>)`                           | A method in the test class itself, with the given comma separated parameter types |
|`<fullyQualifiedClassName>#<methodName>`                  | A method in the given class                                                       |
|`<fullyQualifiedClassName>#<methodName>(<parameterTypes>)`| A method in the given class, with the given comma separated parameter types       |

The parameter types can be one of the following combination; if they are not given, the method lookup will be performed in this order:
* `java.io.Reader`, `com.github.robtimus.junit.support.extension.InjectionTarget`
* `java.io.Reader`, `java.lang.Class`
* `java.io.Reader`
* `java.io.InputStream`, `com.github.robtimus.junit.support.extension.InjectionTarget`
* `java.io.InputStream`, `java.lang.Class`
* `java.io.InputStream`

The `InjectionTarget` or `Class` arguments represents the target or target type respectively, and can be used for dynamic conversion methods. For instance:

```
@Test
void testWithResource(@TestResource("person.xml") @LoadWith("xmlToObject") Person person) {
    // use person as needed
}

@Test
void testWithResource(@TestResource("address.xml") @LoadWith("xmlToObject") Address address) {
    // use address as needed
}

<T> T xmlToObject(Reader reader, Class<T> type) throws IOException {
    // perform conversion, e.g. using JAXB
}
```


#### Specifying the encoding

When using a method that takes a `Reader`, the encoding can be specified in the same way as for `String`, `CharSequence` or `StringBuilder`, using `@Encoding` or the `com.github.robtimus.junit.support.extension.testresource.encoding` JUnit configuration parameter. It's not allowed to use `@Encoding` for methods that take an `InputStream`.

### Injecting Properties

A specialized version of `@LoadWith` is provided for `Properties` objects, [@AsProperties](../apidocs/com/github/robtimus/junit/support/extension/testresource/AsProperties.html):

    // inject as a static field
    @TestResource("test.properties")
    @AsProperties
    private static Properties testProperties;
