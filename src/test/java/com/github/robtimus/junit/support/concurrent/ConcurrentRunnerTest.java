/*
 * ConcurrentRunnerTest.java
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

import static com.github.robtimus.junit.support.ThrowableAssertions.assertDoesNotThrowCheckedException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

@SuppressWarnings("nls")
class ConcurrentRunnerTest {

    private static final int CONCURRENT_COUNT = 100;

    private static final ConcurrencySettings CONCURRENCY_SETTINGS = ConcurrencySettings.withCount(CONCURRENT_COUNT)
            .withThreadCount(CONCURRENT_COUNT / 2);

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Nested
        @DisplayName("with Suppliers")
        class WithSuppliers {

            @Test
            @DisplayName("success")
            void testSuccess() {
                ThrowingSupplier<Integer> supplier1 = mockSupplier(1);
                ThrowingSupplier<Integer> supplier2 = mockSupplier(2);

                List<Integer> results = ConcurrentRunner.running(supplier1)
                        .concurrentlyWith(supplier2)
                        .execute()
                        .andStreamResults()
                        .collect(Collectors.toList());

                List<Integer> expected = Arrays.asList(1, 2);

                assertEquals(expected, results);

                verifyCalled(supplier1, 1);
                verifyCalled(supplier2, 1);
            }

            @Test
            @DisplayName("throwing multiple errors")
            void testThrowingMultipleErrors() {
                ThrowingSupplier<Integer> supplier1 = mockSupplier(1);
                ThrowingSupplier<Integer> supplier2 = mockSupplier(2);

                Error error1 = new InternalError("test error");
                mockThrowAlways(supplier1, error1);

                Error error2 = new InternalError("test error");
                mockThrowAlways(supplier2, error2);

                Stream<Integer> results = ConcurrentRunner.running(supplier1)
                        .concurrentlyWith(supplier2)
                        .execute()
                        .andStreamResults();

                Error thrown = assertThrows(Error.class, results::toArray);

                assertSame(error1, thrown);

                verifyCalled(supplier1, 1);
                verifyCalled(supplier2, 1);
            }

            @Test
            @DisplayName("throwing single error")
            void testThrowingSingleError() {
                ThrowingSupplier<Integer> supplier1 = mockSupplier(1);
                ThrowingSupplier<Integer> supplier2 = mockSupplier(2);

                Error error = new InternalError("test error");
                mockThrowAlways(supplier2, error);

                Stream<Integer> results = ConcurrentRunner.running(supplier1)
                        .concurrentlyWith(supplier2)
                        .execute()
                        .andStreamResults();

                Error thrown = assertThrows(Error.class, results::toArray);

                assertSame(error, thrown);

                verifyCalled(supplier1, 1);
                verifyCalled(supplier2, 1);
            }

            @Test
            @DisplayName("throwing multiple unchecked exceptions")
            void testThrowingUncheckedExceptions() {
                ThrowingSupplier<Integer> supplier1 = mockSupplier(1);
                ThrowingSupplier<Integer> supplier2 = mockSupplier(2);

                RuntimeException exception1 = new IllegalArgumentException("test exception");
                mockThrowAlways(supplier1, exception1);

                RuntimeException exception2 = new IllegalArgumentException("test exception");
                mockThrowAlways(supplier2, exception2);

                Stream<Integer> results = ConcurrentRunner.running(supplier1)
                        .concurrentlyWith(supplier2)
                        .execute()
                        .andStreamResults();

                RuntimeException thrown = assertThrows(RuntimeException.class, results::toArray);

                assertSame(exception1, thrown);

                verifyCalled(supplier1, 1);
                verifyCalled(supplier2, 1);
            }

            @Test
            @DisplayName("throwing single unchecked exception")
            void testThrowingUncheckedException() {
                ThrowingSupplier<Integer> supplier1 = mockSupplier(1);
                ThrowingSupplier<Integer> supplier2 = mockSupplier(2);

                RuntimeException exception = new IllegalArgumentException("test exception");
                mockThrowAlways(supplier2, exception);

                Stream<Integer> results = ConcurrentRunner.running(supplier1)
                        .concurrentlyWith(supplier2)
                        .execute()
                        .andStreamResults();

                RuntimeException thrown = assertThrows(RuntimeException.class, results::toArray);

                assertSame(exception, thrown);

                verifyCalled(supplier1, 1);
                verifyCalled(supplier2, 1);
            }

            @Test
            @DisplayName("throwing multiple checked exceptions")
            void testThrowingCheckedExceptions() {
                ThrowingSupplier<Integer> supplier1 = mockSupplier(1);
                ThrowingSupplier<Integer> supplier2 = mockSupplier(2);

                Exception exception1 = new IOException("test exception");
                mockThrowAlways(supplier1, exception1);

                Exception exception2 = new IOException("test exception");
                mockThrowAlways(supplier2, exception2);

                Stream<Integer> results = ConcurrentRunner.running(supplier1)
                        .concurrentlyWith(supplier2)
                        .execute()
                        .andStreamResults();

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, results::toArray);

                assertSame(exception1, thrown.getCause());

                verifyCalled(supplier1, 1);
                verifyCalled(supplier2, 1);
            }

            @Test
            @DisplayName("throwing single checked exception")
            void testThrowingCheckedException() {
                ThrowingSupplier<Integer> supplier1 = mockSupplier(1);
                ThrowingSupplier<Integer> supplier2 = mockSupplier(2);

                Exception exception = new IOException("test exception");
                mockThrowAlways(supplier2, exception);

                Stream<Integer> results = ConcurrentRunner.running(supplier1)
                        .concurrentlyWith(supplier2)
                        .execute()
                        .andStreamResults();

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, results::toArray);

                assertSame(exception, thrown.getCause());

                verifyCalled(supplier1, 1);
                verifyCalled(supplier2, 1);
            }
        }

        @Nested
        @DisplayName("with Executables")
        class WithExecutables {

            @Test
            @DisplayName("success")
            void testSuccess() {
                Executable executable1 = mock(Executable.class);
                Executable executable2 = mock(Executable.class);

                List<Void> results = ConcurrentRunner.running(executable1)
                        .concurrentlyWith(executable2)
                        .execute()
                        .andStreamResults()
                        .collect(Collectors.toList());

                List<Void> expected = Arrays.asList(null, null);

                assertEquals(expected, results);

                verifyExecuted(executable1, 1);
                verifyExecuted(executable2, 1);
            }

            @Test
            @DisplayName("throwing multiple errors")
            void testThrowingMultipleErrors() {
                Executable executable1 = mock(Executable.class);
                Executable executable2 = mock(Executable.class);

                Error error1 = new InternalError("test error");
                mockThrowAlways(executable1, error1);

                Error error2 = new InternalError("test error");
                mockThrowAlways(executable2, error2);

                Stream<Void> results = ConcurrentRunner.running(executable1)
                        .concurrentlyWith(executable2)
                        .execute()
                        .andStreamResults();

                Error thrown = assertThrows(Error.class, results::toArray);

                assertSame(error1, thrown);

                verifyExecuted(executable1, 1);
                verifyExecuted(executable2, 1);
            }

            @Test
            @DisplayName("throwing single error")
            void testThrowingSingleError() {
                Executable executable1 = mock(Executable.class);
                Executable executable2 = mock(Executable.class);

                Error error = new InternalError("test error");
                mockThrowAlways(executable2, error);

                Stream<Void> results = ConcurrentRunner.running(executable1)
                        .concurrentlyWith(executable2)
                        .execute()
                        .andStreamResults();

                Error thrown = assertThrows(Error.class, results::toArray);

                assertSame(error, thrown);

                verifyExecuted(executable1, 1);
                verifyExecuted(executable2, 1);
            }

            @Test
            @DisplayName("throwing multiple unchecked exceptions")
            void testThrowingUncheckedExceptions() {
                Executable executable1 = mock(Executable.class);
                Executable executable2 = mock(Executable.class);

                RuntimeException exception1 = new IllegalArgumentException("test exception");
                mockThrowAlways(executable1, exception1);

                RuntimeException exception2 = new IllegalArgumentException("test exception");
                mockThrowAlways(executable2, exception2);

                Stream<Void> results = ConcurrentRunner.running(executable1)
                        .concurrentlyWith(executable2)
                        .execute()
                        .andStreamResults();

                RuntimeException thrown = assertThrows(RuntimeException.class, results::toArray);

                assertSame(exception1, thrown);

                verifyExecuted(executable1, 1);
                verifyExecuted(executable2, 1);
            }

            @Test
            @DisplayName("throwing single unchecked exception")
            void testThrowingUncheckedException() {
                Executable executable1 = mock(Executable.class);
                Executable executable2 = mock(Executable.class);

                RuntimeException exception = new IllegalArgumentException("test exception");
                mockThrowAlways(executable2, exception);

                Stream<Void> results = ConcurrentRunner.running(executable1)
                        .concurrentlyWith(executable2)
                        .execute()
                        .andStreamResults();

                RuntimeException thrown = assertThrows(RuntimeException.class, results::toArray);

                assertSame(exception, thrown);

                verifyExecuted(executable1, 1);
                verifyExecuted(executable2, 1);
            }

            @Test
            @DisplayName("throwing multiple checked exceptions")
            void testThrowingCheckedExceptions() {
                Executable executable1 = mock(Executable.class);
                Executable executable2 = mock(Executable.class);

                Exception exception1 = new IOException("test exception");
                mockThrowAlways(executable1, exception1);

                Exception exception2 = new IOException("test exception");
                mockThrowAlways(executable2, exception2);

                Stream<Void> results = ConcurrentRunner.running(executable1)
                        .concurrentlyWith(executable2)
                        .execute()
                        .andStreamResults();

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, results::toArray);

                assertSame(exception1, thrown.getCause());

                verifyExecuted(executable1, 1);
                verifyExecuted(executable2, 1);
            }

            @Test
            @DisplayName("throwing single checked exception")
            void testThrowingCheckedException() {
                Executable executable1 = mock(Executable.class);
                Executable executable2 = mock(Executable.class);

                Exception exception = new IOException("test exception");
                mockThrowAlways(executable2, exception);

                Stream<Void> results = ConcurrentRunner.running(executable1)
                        .concurrentlyWith(executable2)
                        .execute()
                        .andStreamResults();

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, results::toArray);

                assertSame(exception, thrown.getCause());

                verifyExecuted(executable1, 1);
                verifyExecuted(executable2, 1);
            }
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, 0, 1 })
        @DisplayName("invalid thread count")
        void testInvalidThreadCount(int threadCount) {
            ThrowingSupplier<Integer> supplier = mockSupplier(1);

            ConcurrentRunner<Integer> runner = ConcurrentRunner.running(supplier);

            assertThrows(IllegalArgumentException.class, () -> runner.withThreadCount(threadCount));
        }

        @ParameterizedTest
        @ValueSource(ints = { 2, CONCURRENT_COUNT / 2 })
        @DisplayName("with fewer threads")
        void testWithFewerThreads(int threadCount) {
            ThrowingSupplier<Integer> supplier = mockSupplier(1);

            List<Integer> results = ConcurrentRunner.running(supplier, CONCURRENT_COUNT)
                    .withThreadCount(threadCount)
                    .execute()
                    .andStreamResults()
                    .collect(Collectors.toList());

            List<Integer> expected = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> 1)
                    .collect(Collectors.toList());

            assertEquals(expected, results);

            verifyCalled(supplier, CONCURRENT_COUNT);
        }
    }

    @Nested
    @DisplayName("runConcurrently(Executable, int)")
    class RunExecutableRepeatedly {

        @Test
        @DisplayName("null executable")
        void testNullExecutable() {
            Executable executable = null;
            assertThrows(NullPointerException.class, () -> ConcurrentRunner.runConcurrently(executable, 1));
        }

        @Test
        @DisplayName("non-positive count")
        void testNonPositiveCount() {
            Executable executable = mock(Executable.class);
            assertThrows(IllegalArgumentException.class, () -> ConcurrentRunner.runConcurrently(executable, 0));
        }

        @Test
        @DisplayName("success")
        void testSuccess() {
            Executable executable = mock(Executable.class);

            ConcurrentRunner.runConcurrently(executable, CONCURRENT_COUNT);

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing multiple errors")
        void testThrowingMultipleErrors() {
            Executable executable = mock(Executable.class);

            Error error = new InternalError("test error");
            mockThrowAlways(executable, error);

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class,
                    () -> ConcurrentRunner.runConcurrently(executable, CONCURRENT_COUNT));

            List<Error> expected = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> error)
                    .collect(Collectors.toList());

            assertEquals(expected, thrown.getFailures());

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing single error")
        void testThrowingSingleError() {
            Executable executable = mock(Executable.class);

            Error error = new InternalError("test error");
            mockThrowOnce(executable, error, CONCURRENT_COUNT);

            Error thrown = assertThrows(Error.class, () -> ConcurrentRunner.runConcurrently(executable, CONCURRENT_COUNT));

            assertSame(error, thrown);

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing multiple unchecked exceptions")
        void testThrowingUncheckedExceptions() {
            Executable executable = mock(Executable.class);

            RuntimeException exception = new IllegalArgumentException("test exception");
            mockThrowAlways(executable, exception);

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class,
                    () -> ConcurrentRunner.runConcurrently(executable, CONCURRENT_COUNT));

            List<RuntimeException> expected = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> exception)
                    .collect(Collectors.toList());

            assertEquals(expected, thrown.getFailures());

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing single unchecked exception")
        void testThrowingUncheckedException() {
            Executable executable = mock(Executable.class);

            RuntimeException exception = new IllegalArgumentException("test exception");
            mockThrowOnce(executable, exception, CONCURRENT_COUNT);

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> ConcurrentRunner.runConcurrently(executable, CONCURRENT_COUNT));

            assertSame(exception, thrown);

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing multiple checked exceptions")
        void testThrowingCheckedExceptions() {
            Executable executable = mock(Executable.class);

            Exception exception = new IOException("test exception");
            mockThrowAlways(executable, exception);

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class,
                    () -> ConcurrentRunner.runConcurrently(executable, CONCURRENT_COUNT));

            List<Exception> expected = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> exception)
                    .collect(Collectors.toList());

            assertEquals(expected, thrown.getFailures());

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing single checked exception")
        void testThrowingCheckedException() {
            Executable executable = mock(Executable.class);

            Exception exception = new IOException("test exception");
            mockThrowOnce(executable, exception, CONCURRENT_COUNT);

            AssertionFailedError thrown = assertThrows(AssertionFailedError.class,
                    () -> ConcurrentRunner.runConcurrently(executable, CONCURRENT_COUNT));

            assertSame(exception, thrown.getCause());

            verifyExecuted(executable, CONCURRENT_COUNT);
        }
    }

    @Nested
    @DisplayName("runConcurrently(Executable, ConcurrencySettings)")
    class RunExecutableWithConcurrencySettings {

        @Test
        @DisplayName("null executable")
        void testNullExecutable() {
            Executable executable = null;
            ConcurrencySettings settings = ConcurrencySettings.withCount(1);
            assertThrows(NullPointerException.class, () -> ConcurrentRunner.runConcurrently(executable, settings));
        }

        @Test
        @DisplayName("null settings")
        void testNullSettings() {
            Executable executable = mock(Executable.class);
            assertThrows(NullPointerException.class, () -> ConcurrentRunner.runConcurrently(executable, null));
        }

        @Test
        @DisplayName("success")
        void testSuccess() {
            Executable executable = mock(Executable.class);

            ConcurrentRunner.runConcurrently(executable, CONCURRENCY_SETTINGS);

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing multiple errors")
        void testThrowingMultipleErrors() {
            Executable executable = mock(Executable.class);

            Error error = new InternalError("test error");
            mockThrowAlways(executable, error);

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class,
                    () -> ConcurrentRunner.runConcurrently(executable, CONCURRENCY_SETTINGS));

            List<Error> expected = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> error)
                    .collect(Collectors.toList());

            assertEquals(expected, thrown.getFailures());

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing single error")
        void testThrowingSingleError() {
            Executable executable = mock(Executable.class);

            Error error = new InternalError("test error");
            mockThrowOnce(executable, error, CONCURRENT_COUNT);

            Error thrown = assertThrows(Error.class, () -> ConcurrentRunner.runConcurrently(executable, CONCURRENCY_SETTINGS));

            assertSame(error, thrown);

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing multiple unchecked exceptions")
        void testThrowingUncheckedExceptions() {
            Executable executable = mock(Executable.class);

            RuntimeException exception = new IllegalArgumentException("test exception");
            mockThrowAlways(executable, exception);

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class,
                    () -> ConcurrentRunner.runConcurrently(executable, CONCURRENCY_SETTINGS));

            List<RuntimeException> expected = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> exception)
                    .collect(Collectors.toList());

            assertEquals(expected, thrown.getFailures());

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing single unchecked exception")
        void testThrowingUncheckedException() {
            Executable executable = mock(Executable.class);

            RuntimeException exception = new IllegalArgumentException("test exception");
            mockThrowOnce(executable, exception, CONCURRENT_COUNT);

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> ConcurrentRunner.runConcurrently(executable, CONCURRENCY_SETTINGS));

            assertSame(exception, thrown);

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing multiple checked exceptions")
        void testThrowingCheckedExceptions() {
            Executable executable = mock(Executable.class);

            Exception exception = new IOException("test exception");
            mockThrowAlways(executable, exception);

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class,
                    () -> ConcurrentRunner.runConcurrently(executable, CONCURRENCY_SETTINGS));

            List<Exception> expected = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> exception)
                    .collect(Collectors.toList());

            assertEquals(expected, thrown.getFailures());

            verifyExecuted(executable, CONCURRENT_COUNT);
        }

        @Test
        @DisplayName("throwing single checked exception")
        void testThrowingCheckedException() {
            Executable executable = mock(Executable.class);

            Exception exception = new IOException("test exception");
            mockThrowOnce(executable, exception, CONCURRENT_COUNT);

            AssertionFailedError thrown = assertThrows(AssertionFailedError.class,
                    () -> ConcurrentRunner.runConcurrently(executable, CONCURRENCY_SETTINGS));

            assertSame(exception, thrown.getCause());

            verifyExecuted(executable, CONCURRENT_COUNT);
        }
    }

    @Nested
    @DisplayName("runConcurrently(Executor...)")
    class RunExecutors {

        @Test
        @DisplayName("null executable")
        void testNullExecutable() {
            List<Executable> executables = Arrays.asList(mock(Executable.class), null);
            assertThrows(NullPointerException.class, () -> ConcurrentRunner.runConcurrently(executables));
        }

        @Test
        @DisplayName("no executables")
        void testNoExecutables() {
            assertDoesNotThrow(() -> ConcurrentRunner.runConcurrently());
        }

        @Test
        @DisplayName("success")
        void testSuccess() {
            List<Executable> executables = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> mock(Executable.class))
                    .collect(Collectors.toList());

            ConcurrentRunner.runConcurrently(executables);

            assertAll(executables.stream()
                    .map(executable -> () -> verifyExecuted(executable, 1)));
        }

        @Test
        @DisplayName("throwing multiple errors")
        void testThrowingMultipleErrors() {
            List<Executable> executables = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> mock(Executable.class))
                    .collect(Collectors.toList());

            Error error = new InternalError("test error");
            executables.forEach(executable -> mockThrowAlways(executable, error));

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class, () -> ConcurrentRunner.runConcurrently(executables));

            List<Error> expected = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> error)
                    .collect(Collectors.toList());

            assertEquals(expected, thrown.getFailures());

            verifyEachExecutedOnce(executables);
        }

        @Test
        @DisplayName("throwing single error")
        void testThrowingSingleError() {
            List<Executable> executables = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> mock(Executable.class))
                    .collect(Collectors.toList());

            Error error = new InternalError("test error");
            mockThrowAlways(executables.get(CONCURRENT_COUNT / 2), error);

            Error thrown = assertThrows(Error.class, () -> ConcurrentRunner.runConcurrently(executables));

            assertSame(error, thrown);

            verifyEachExecutedOnce(executables);
        }

        @Test
        @DisplayName("throwing multiple unchecked exceptions")
        void testThrowingUncheckedExceptions() {
            List<Executable> executables = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> mock(Executable.class))
                    .collect(Collectors.toList());

            RuntimeException exception = new IllegalArgumentException("test exception");
            executables.forEach(executable -> mockThrowAlways(executable, exception));

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class, () -> ConcurrentRunner.runConcurrently(executables));

            List<RuntimeException> expected = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> exception)
                    .collect(Collectors.toList());

            assertEquals(expected, thrown.getFailures());

            verifyEachExecutedOnce(executables);
        }

        @Test
        @DisplayName("throwing single unchecked exception")
        void testThrowingUncheckedException() {
            List<Executable> executables = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> mock(Executable.class))
                    .collect(Collectors.toList());

            RuntimeException exception = new IllegalArgumentException("test exception");
            mockThrowAlways(executables.get(CONCURRENT_COUNT / 2), exception);

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> ConcurrentRunner.runConcurrently(executables));

            assertSame(exception, thrown);

            verifyEachExecutedOnce(executables);
        }

        @Test
        @DisplayName("throwing multiple checked exceptions")
        void testThrowingCheckedExceptions() {
            List<Executable> executables = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> mock(Executable.class))
                    .collect(Collectors.toList());

            Exception exception = new IOException("test exception");
            executables.forEach(executable -> mockThrowAlways(executable, exception));

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class, () -> ConcurrentRunner.runConcurrently(executables));

            List<Exception> expected = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> exception)
                    .collect(Collectors.toList());

            assertEquals(expected, thrown.getFailures());

            verifyEachExecutedOnce(executables);
        }

        @Test
        @DisplayName("throwing single checked exception")
        void testThrowingCheckedException() {
            List<Executable> executables = IntStream.range(0, CONCURRENT_COUNT)
                    .mapToObj(i -> mock(Executable.class))
                    .collect(Collectors.toList());

            Exception exception = new IOException("test exception");
            mockThrowAlways(executables.get(CONCURRENT_COUNT / 2), exception);

            AssertionFailedError thrown = assertThrows(AssertionFailedError.class, () -> ConcurrentRunner.runConcurrently(executables));

            assertSame(exception, thrown.getCause());

            verifyEachExecutedOnce(executables);
        }
    }

    private ThrowingSupplier<Integer> mockSupplier(int value) {
        @SuppressWarnings("unchecked")
        ThrowingSupplier<Integer> supplier = mock(ThrowingSupplier.class);
        assertDoesNotThrowCheckedException(() -> doReturn(value).when(supplier).get());
        return supplier;
    }

    private void mockThrowAlways(ThrowingSupplier<?> supplier, Throwable throwable) {
        assertDoesNotThrowCheckedException(() -> doThrow(throwable).when(supplier).get());
    }

    private void mockThrowAlways(Executable executable, Throwable throwable) {
        assertDoesNotThrowCheckedException(() -> doThrow(throwable).when(executable).execute());
    }

    private void mockThrowOnce(Executable executable, Throwable throwable, int minNumberOfCalls) {
        AtomicInteger counter = new AtomicInteger();
        int throwAtCount = minNumberOfCalls / 2;
        assertDoesNotThrowCheckedException(() -> doAnswer(i -> {
            if (counter.getAndIncrement() == throwAtCount) {
                throw throwable;
            }
            return null;
        }).when(executable).execute());
    }

    private void verifyCalled(ThrowingSupplier<?> supplier, int times) {
        assertDoesNotThrowCheckedException(() -> verify(supplier, times(times)).get());
        verifyNoMoreInteractions(supplier);
    }

    private void verifyExecuted(Executable executable, int times) {
        assertDoesNotThrowCheckedException(() -> verify(executable, times(times)).execute());
        verifyNoMoreInteractions(executable);
    }

    private void verifyEachExecutedOnce(List<Executable> executables) throws MultipleFailuresError {
        assertAll(executables.stream()
                .map(executable -> () -> verifyExecuted(executable, 1)));
    }
}
