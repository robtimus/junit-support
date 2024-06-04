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
        .andListResults();
```

### Running the same block of code several times

Both `running` and `concurrentlyWith` are overloaded to accept a number of times to call the `Executable` or `ThrowableSupplier`. For instance, the following will run the same code block 10 times:

```java
List<MyResult> results = ConcurrentRunner.running(() -> codeBlock(), 10)
        .execute()
        .andListResults();
```

### Working with Executable

The `andListResults()` methods collects all results into a list. When working only with `Executable`, the results will always be `null`, so collecting these into a `List<Void>` wouldn't add much value. The `andAssertNoFailures()` method can be used instead to process the results without collecting them:

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

### Handling errors and exceptions

Any error or exception thrown from an `Executable` or `ThrowingSupplier` is thrown when results are evaluated, including when calling the static `runConcurrently` methods. If the code to run concurrently may throw exceptions, make sure to handle those inside the `Executable` or `ThrowingSupplier`, e.g. using [assertThrows](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Assertions.html#assertThrows(java.lang.Class,org.junit.jupiter.api.function.Executable)).

### Handling results

Besides the `andListResults()` and `andAssertNoFailures()` methods mentioned earlier, the object returned by the `execute()` method has some more methods that expose the results:

* `andStreamResults()` returns a stream that lazily evaluates each result. If any `Executable` or `ThrowingSupplier` threw an error or exception, the stream will throw an error or exception if a terminal operation is executed.
* `andCollectResults(Collector)` is a general purpose version of `andListResults()` that can take any `Collector`. Like `andListResults()` and `andAssertNoFailures()`, every result is evaluated.

### Thread count

By default, one thread for each provided `Executable` or `ThrowingSupplier` is used, and each is called at approximately the same time. By calling `withThreadCount` it's possible to use a lower number of threads. This allows testing that code that uses lazy initialization works fine if initialization has already taken place; some of the provided `Executables` or `ThrowingSuppliers` will be called after at least one other has already finished.
