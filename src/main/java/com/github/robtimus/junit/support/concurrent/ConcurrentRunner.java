/*
 * ConcurrentRunner.java
 * Copyright 2024 Rob Spoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robtimus.junit.support.concurrent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;

/**
 * A class that will run code concurrently.
 *
 * @author Rob Spoor
 * @param <T> The type of result for instances.
 * @since 2.3
 */
@SuppressWarnings("nls")
public final class ConcurrentRunner<T> {

    private final List<ThrowingSupplier<? extends T>> suppliers;

    private int threadCount = Integer.MAX_VALUE;

    private ConcurrentRunner() {
        this.suppliers = new ArrayList<>();
    }

    /**
     * Creates a new concurrent runner.
     *
     * @param <T> The type of result.
     * @param supplier The first supplier to call.
     * @return The created concurrent runner.
     * @throws NullPointerException If the given supplier is {@code null}.
     */
    public static <T> ConcurrentRunner<T> running(ThrowingSupplier<? extends T> supplier) {
        return running(supplier, 1);
    }

    /**
     * Creates a new concurrent runner.
     *
     * @param <T> The type of result.
     * @param supplier The first supplier to call.
     * @param count The number of times to call the supplier.
     * @return The created concurrent runner.
     * @throws NullPointerException If the given supplier is {@code null}.
     * @throws IllegalArgumentException If the given count is not positive.
     */
    public static <T> ConcurrentRunner<T> running(ThrowingSupplier<? extends T> supplier, int count) {
        ConcurrentRunner<T> runner = new ConcurrentRunner<>();
        return runner.concurrentlyWith(supplier, count);
    }

    /**
     * Creates a new concurrent runner. This is equivalent to calling {@link #running(ThrowingSupplier)} with a supplier that calls
     * {@link Executable#execute() executable.execute()} and then returns {@code null}.
     *
     * @param executable The first executable to call.
     * @return This object.
     * @throws NullPointerException If the given executable is {@code null}.
     */
    public static ConcurrentRunner<Void> running(Executable executable) {
        return running(executable, 1);
    }

    /**
     * Creates a new concurrent runner. This is equivalent to calling {@link #running(ThrowingSupplier)} with a supplier that calls
     * {@link Executable#execute() executable.execute()} and then returns {@code null}.
     *
     * @param executable The first executable to call.
     * @param count The number of times to call the executable.
     * @return This object.
     * @throws NullPointerException If the given executable is {@code null}.
     * @throws IllegalArgumentException If the given count is not positive.
     */
    public static ConcurrentRunner<Void> running(Executable executable, int count) {
        Objects.requireNonNull(executable);
        return running(asSupplier(executable), count);
    }

    /**
     * Adds a supplier to call concurrently.
     *
     * @param supplier The additional supplier to call.
     * @return This object.
     * @throws NullPointerException If the given supplier is {@code null}.
     */
    public ConcurrentRunner<T> concurrentlyWith(ThrowingSupplier<? extends T> supplier) {
        return concurrentlyWith(supplier, 1);
    }

    /**
     * Adds a supplier to call concurrently.
     *
     * @param supplier The additional supplier to call.
     * @param count The number of times to call the supplier.
     * @return This object.
     * @throws NullPointerException If the given supplier is {@code null}.
     * @throws IllegalArgumentException If the given count is not positive.
     */
    public ConcurrentRunner<T> concurrentlyWith(ThrowingSupplier<? extends T> supplier, int count) {
        Objects.requireNonNull(supplier);
        if (count < 1) {
            throw new IllegalArgumentException(count + " < 1");
        }
        for (int i = 0; i < count; i++) {
            suppliers.add(supplier);
        }
        return this;
    }

    /**
     * Adds an executable to call concurrently. This is equivalent to calling {@link #concurrentlyWith(ThrowingSupplier)} with a supplier that calls
     * {@link Executable#execute() executable.execute()} and then returns {@code null}.
     *
     * @param executable The additional executable to call.
     * @return This object.
     * @throws NullPointerException If the given executable is {@code null}.
     */
    public ConcurrentRunner<T> concurrentlyWith(Executable executable) {
        return concurrentlyWith(executable, 1);
    }

    /**
     * Adds an executable to call concurrently. This is equivalent to calling {@link #concurrentlyWith(ThrowingSupplier)} with a supplier that calls
     * {@link Executable#execute() executable.execute()} and then returns {@code null}.
     *
     * @param executable The additional executable to call.
     * @param count The number of times to call the supplier.
     * @return This object.
     * @throws NullPointerException If the given executable is {@code null}.
     * @throws IllegalArgumentException If the given count is not positive.
     */
    public ConcurrentRunner<T> concurrentlyWith(Executable executable, int count) {
        Objects.requireNonNull(executable);
        return concurrentlyWith(asSupplier(executable), count);
    }

    /**
     * Sets the number of threads to use. By default each provided supplier will get its own thread.
     *
     * @param threadCount The number of threads to use.
     * @return This object.
     * @throws IllegalArgumentException If the given thread count is not at least 2.
     */
    public ConcurrentRunner<T> withThreadCount(int threadCount) {
        if (threadCount < 2) {
            throw new IllegalArgumentException(threadCount + " < 2");
        }
        this.threadCount = threadCount;
        return this;
    }

    /**
     * Calls all provided suppliers concurrently using the provided {@link #withThreadCount(int) number of threads}.
     * If no thread count has been given a thread for each provided supplier will be used.
     * <p>
     * This method assumes that none of the suppliers throws an error or exception, including failed assertions.
     * If any supplier does, the returned stream will throw an error or exception when a terminal operator is executed.
     * Any checked exception will be wrapped in a {@link ConcurrentException}.
     *
     * @return The results of calling the suppliers. The order matches the order of the added suppliers.
     */
    public Stream<T> execute() {
        return execute(results -> results.stream()
                .map(ConcurrentResult::getOrThrow));
    }

    /**
     * Calls all provided suppliers concurrently using the provided {@link #withThreadCount(int) number of threads}.
     * If no thread count has been given a thread for each provided supplier will be used.
     * <p>
     * The given handler is called for the result of each supplier. The arguments are either the result and a {@code null} {@link Throwable} if the
     * supplier was successful, or {@code null} and the thrown {@link Throwable} if the supplier was unsuccessful. Unlike with {@link #execute()},
     * any checked exception will not be wrapped in a {@link ConcurrentException}.
     *
     * @param <R> The type of result.
     * @param resultHandler A handler for results.
     * @return A stream with the results of calling the result handler. The order matches the order of the added suppliers.
     * @throws NullPointerException If the given result handler is {@code null}.
     */
    public <R> Stream<R> execute(BiFunction<? super T, ? super Throwable, ? extends R> resultHandler) {
        Objects.requireNonNull(resultHandler);
        return execute(results -> results.stream()
                .map(result -> result.handle(resultHandler)));
    }

    private <R> R execute(Function<List<ConcurrentResult<T>>, R> resultMapper) {
        int poolSize = Math.min(suppliers.size(), threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        try {
            return execute(executor, poolSize, resultMapper);
        } finally {
            executor.shutdown();
        }
    }

    private <R> R execute(ExecutorService executor, int poolSize, Function<List<ConcurrentResult<T>>, R> resultMapper) {
        CountDownLatch readyLatch = new CountDownLatch(poolSize);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<CompletableFuture<ConcurrentResult<T>>> futures = suppliers.stream()
                .map(supplier -> newFuture(supplier, executor, readyLatch, startLatch))
                .collect(Collectors.toList());

        assertDoesNotThrow(() -> readyLatch.await()); // NOSONAR, a method reference gives an ambiguity error
        startLatch.countDown();

        List<ConcurrentResult<T>> results = futures.stream()
                .map(future -> assertDoesNotThrow(() -> future.get())) // NOSONAR, a method reference gives an ambiguity error
                .collect(Collectors.toList());

        return resultMapper.apply(results);
    }

    private CompletableFuture<ConcurrentResult<T>> newFuture(ThrowingSupplier<? extends T> supplier, ExecutorService executor,
            CountDownLatch readyLatch, CountDownLatch startLatch) {

        return CompletableFuture.supplyAsync(() -> call(supplier, readyLatch, startLatch), executor);
    }

    private ConcurrentResult<T> call(ThrowingSupplier<? extends T> supplier, CountDownLatch readyLatch, CountDownLatch startLatch) {
        readyLatch.countDown();
        assertDoesNotThrow(() -> startLatch.await()); // NOSONAR, a method reference gives an ambiguity error
        try {
            T result = supplier.get();
            return new ConcurrentResult<>(result);
        } catch (Throwable t) {
            return new ConcurrentResult<>(t);
        }
    }

    /**
     * Runs a block of code several times concurrently. Each block of code will start at approximately the same time.
     * <p>
     * This method assumes that each block of code does not throw any exception, including failed assertions.
     * If any block of code does, this method will throw an error or exception. Any checked exception will be wrapped in a
     * {@link ConcurrentException}.
     *
     * @param executable The block of code to run concurrently.
     * @param count The number of times to run the block of code.
     * @throws NullPointerException If the given executable is {@code null}.
     * @throws IllegalArgumentException If the given count is not positive.
     */
    public static void runConcurrently(Executable executable, int count) {
        running(executable, count).execute(results -> {
            results.forEach(ConcurrentResult::getOrThrow);
            return null;
        });
    }

    /**
     * Runs several blocks of code concurrently. Each block of code will start at approximately the same time.
     * <p>
     * This method assumes that each block of code does not throw any exception, including failed assertions.
     * If any block of code does, this method will throw an error or exception. Any checked exception will be wrapped in a
     * {@link ConcurrentException}.
     *
     * @param executables The blocks of code to run concurrently.
     * @throws NullPointerException If any of the given executables is {@code null}.
     */
    public static void runConcurrently(List<Executable> executables) {
        if (executables.isEmpty()) {
            return;
        }
        Iterator<Executable> iterator = executables.iterator();
        ConcurrentRunner<Void> runner = running(iterator.next());
        while (iterator.hasNext()) {
            runner.concurrentlyWith(iterator.next());
        }
        runner.execute(results -> {
            results.forEach(ConcurrentResult::getOrThrow);
            return null;
        });
    }

    private static <T> ThrowingSupplier<T> asSupplier(Executable executable) {
        return () -> {
            executable.execute();
            return null;
        };
    }
}
