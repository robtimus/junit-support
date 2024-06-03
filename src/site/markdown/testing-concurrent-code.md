<head>
  <title>Testing concurrent code</title>
</head>

## ConcurrentRunner

Class [ConcurrentRunner](apidocs/com/github/robtimus/junit/support/concurrent/ConcurrentRunner.html) provides an easy-to-use API for running code concurrently.
It can work with both [Executable](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/function/Executable.html) and [ThrowingSupplier](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/function/ThrowingSupplier.html) for the code to run concurrently.

### Working with ThrowingSupplier

The most common use with `ThrowingSupplier` is as follows, where each code block returns an instance of `MyResult`:

```java
List<MyResult> results = ConcurrentRunner.running(() -> firstCodeBlock())
        .concurrentlyWith(() -> secondCodeBlock())
        .execute()
        .andStreamResults()
        .collect(Collectors.toList()); // or .toList() since Java 16
```

### Running the same block of code several times

Both `running` and `concurrentlyWith` are overloaded to accept a number of times to call the `Executable` or `ThrowableSupplier`. For instance, the following will run the same code block 10 times:

```java
List<MyResult> results = ConcurrentRunner.running(() -> codeBlock(), 10)
        .execute()
        .andStreamResults()
        .collect(Collectors.toList());
```

### Working with Executable

The `andStreamResults()` method returns a stream, which means that you need to execute a terminal operator to get the actual results. When working only with `Executable`, the results will always be `null`, so collecting these into a `List<Void>` wouldn't add much value. The `andAssertNoFailures()` method can be used instead to process the results without collecting them:

```java
ConcurrentRunner.running(() -> codeBlock(), 10)
        .execute()
        .andAssertNoFailures();
```

As a shortcut to calling `execute().andAssertNoFailures()`, some static methods have been added that work with `Executable` only:

```java
// Run the same code block 10 times
ConcurrentRunner.runConcurrently(() -> codeBlock(), 10);
// Run different code blocks:
ConcurrentRunner.runConcurrently(
        () -> firstCodeBlock(),
        () -> secondCodeBlock()
);
// The above is equivalent to:
ConcurrentRunner.runConcurrently(Arrays.asList(
        () -> firstCodeBlock(),
        () -> secondCodeBlock()
));
```

### Thread count

By default, one thread for each provided `Executable` or `ThrowingSupplier` is used, and each is called at approximately the same time. By calling `withThreadCount` it's possible to use a lower number of threads. This allows testing that code that uses lazy initialization works fine if initialization has already taken place; some of the provided `Executables` or `ThrowingSuppliers` will be called after at least one other has already finished.

### Handling errors and exceptions

Any error or exception thrown from an `Executable` or `ThrowingSupplier` is thrown when results are evaluated, including when calling the static `runConcurrently` methods. If the code to run concurrently may throw exceptions, make sure to handle those inside the `Executable` or `ThrowingSupplier`, e.g. using [assertThrows](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Assertions.html#assertThrows(java.lang.Class,org.junit.jupiter.api.function.Executable)).
