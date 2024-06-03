<head>
  <title>Testing concurrent code</title>
</head>

## ConcurrentRunner

Class [ConcurrentRunner](apidocs/com/github/robtimus/junit/support/concurrent/ConcurrentRunner.html) provides an easy-to-use API for running code concurrently.
It can work with both [Executable](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/function/Executable.html) and [ThrowingSupplier](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/function/ThrowingSupplier.html) for the code to run concurrently.

The most common use with `ThrowingSupplier` is as follows, where each code block returns an instance of `MyResult`:

```java
List<MyResult> results = ConcurrentRunner.running(() -> firstCodeBlock())
        .concurrentlyWith(() -> secondCodeBlock())
        ...
        .execute()
        .collect(Collectors.toList()); // or .toList() since Java 16
```

If any of the code blocks throws an exception or error, including failed assertions, this is re-thrown when a terminal operation is executed on the stream returned by the `execute()` method. Any checked exception will be wrapped in a [ConcurrentRunner](apidocs/com/github/robtimus/junit/support/concurrent/ConcurrentException.html).

### Running the same block of code several times

Both `running` and `concurrentlyWith` are overloaded to accept a number of times to call the `Executable` or `ThrowableSupplier`. For instance, the following will run the same code block 10 times:

```java
List<MyResult> results = ConcurrentRunner.running(() -> codeBlock(), 10)
        .execute()
        .collect(Collectors.toList());
```

### Handling errors and exceptions

Errors and exceptions can be handled by catching exceptions thrown by the stream's terminal operator, including using [assertThrows](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Assertions.html#assertThrows(java.lang.Class,org.junit.jupiter.api.function.Executable)). However, it's also possible to provide a handler function to the `execute` method. For instance, to only get exceptions of a specific checked type:

```java
List<IOException> exceptions = ConcurrentRunner.running(() -> firstCodeBlock())
        .concurrentlyWith(() -> secondCodeBlock())
        ...
        .execute((result, throwable) -> throwable)
        .filter(Objects::nonNull)
        .map(throwable -> assertInstanceOf(IOException.class, throwable))
        .collect(Collectors.toList());
```

### Working with Executable

Both `execute` methods return a stream. This means that you need to execute a terminal operator to get the actual results. When working only with `Executable`, the results will always be `null`, so collecting these into a `List<Void>` wouldn't add much value. Therefore, two static methods have been added that work with `Executable` only:

```java
// Run the same code block 10 times
ConcurrentRunner.runConcurrently(() -> codeBlock(), 10);
// Run different code blocks:
ConcurrentRunner.runConcurrently(Arrays.asList(
        () -> firstCodeBlock(),
        () -> secondCodeBlock()
));
```

Like the `execute()` methods, any error or exception is re-thrown. If you want to handle errors and exceptions you need to use the `running` and `concurrentlyWith` methods that take an `Executable`. Keep in mind that the first argument to the handler function will always be `null`.

### Thread count

By default, one thread for each provided `Executable` or `ThrowingSupplier` is used, and each is called at approximately the same time. By calling `withThreadCount` it's possible to use a lower number of threads. This allows testing that code that uses lazy initialization works fine if initialization has already taken place; some of the provided `Executables` or `ThrowingSuppliers` will be called after at least one other has already finished.
