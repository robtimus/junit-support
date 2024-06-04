/*
 * ConcurrentResultsTest.java
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

class ConcurrentResultsTest {

    @Nested
    @DisplayName("andStreamResults")
    class StreamResults {

        @Test
        @DisplayName("success")
        void testSuccess() {
            ConcurrentResults<Integer> results = new ConcurrentResults<>(IntStream.range(0, 100)
                    .mapToObj(i -> new ConcurrentResult<>(i)));

            List<Integer> resultList = results.andStreamResults()
                    .collect(Collectors.toList());

            List<Integer> expected = IntStream.range(0, 100)
                    .boxed()
                    .collect(Collectors.toList());

            assertEquals(expected, resultList);
        }

        @Test
        @DisplayName("single exception")
        void testSingleException() {
            Exception exception = new IOException();

            ConcurrentResults<Integer> results = new ConcurrentResults<>(Stream.of(
                    new ConcurrentResult<>(1),
                    new ConcurrentResult<>(exception),
                    new ConcurrentResult<>(3)));

            Stream<Integer> resultStream = results.andStreamResults();

            AssertionFailedError thrown = assertThrows(AssertionFailedError.class, resultStream::toArray);

            assertSame(exception, thrown.getCause());
        }

        @Test
        @DisplayName("multiple exceptions")
        void testMultipleExceptions() {
            Exception exception1 = new IOException();
            Exception exception2 = new IOException();

            ConcurrentResults<Integer> results = new ConcurrentResults<>(Stream.of(
                    new ConcurrentResult<>(1),
                    new ConcurrentResult<>(exception1),
                    new ConcurrentResult<>(3),
                    new ConcurrentResult<>(exception2)));

            Stream<Integer> resultStream = results.andStreamResults();

            AssertionFailedError thrown = assertThrows(AssertionFailedError.class, resultStream::toArray);

            assertSame(exception1, thrown.getCause());
        }
    }

    @Nested
    @DisplayName("andListResults")
    class ListResults {

        @Test
        @DisplayName("success")
        void testSuccess() {
            ConcurrentResults<Integer> results = new ConcurrentResults<>(IntStream.range(0, 100)
                    .mapToObj(i -> new ConcurrentResult<>(i)));

            List<Integer> resultList = results.andListResults();

            List<Integer> expected = IntStream.range(0, 100)
                    .boxed()
                    .collect(Collectors.toList());

            assertEquals(expected, resultList);
        }

        @Test
        @DisplayName("single exception")
        void testSingleException() {
            Exception exception = new IOException();

            ConcurrentResults<Integer> results = new ConcurrentResults<>(Stream.of(
                    new ConcurrentResult<>(1),
                    new ConcurrentResult<>(exception),
                    new ConcurrentResult<>(3)));

            AssertionFailedError thrown = assertThrows(AssertionFailedError.class, results::andListResults);

            assertSame(exception, thrown.getCause());
        }

        @Test
        @DisplayName("multiple exceptions")
        void testMultipleExceptions() {
            Exception exception1 = new IOException();
            Exception exception2 = new IOException();

            ConcurrentResults<Integer> results = new ConcurrentResults<>(Stream.of(
                    new ConcurrentResult<>(1),
                    new ConcurrentResult<>(exception1),
                    new ConcurrentResult<>(3),
                    new ConcurrentResult<>(exception2)));

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class, results::andListResults);

            List<Exception> expected = Arrays.asList(exception1, exception2);

            assertEquals(expected, thrown.getFailures());
        }
    }

    @Nested
    @DisplayName("andAssertNoFailures")
    class AssertNoFailures {

        @Test
        @DisplayName("success")
        void testSuccess() {
            ConcurrentResults<Integer> results = new ConcurrentResults<>(IntStream.range(0, 100)
                    .mapToObj(i -> new ConcurrentResult<>(i)));

            assertDoesNotThrow(results::andAssertNoFailures);
        }

        @Test
        @DisplayName("single exception")
        void testSingleException() {
            Exception exception = new IOException();

            ConcurrentResults<Integer> results = new ConcurrentResults<>(Stream.of(
                    new ConcurrentResult<>(1),
                    new ConcurrentResult<>(exception),
                    new ConcurrentResult<>(3)));

            AssertionFailedError thrown = assertThrows(AssertionFailedError.class, results::andAssertNoFailures);

            assertSame(exception, thrown.getCause());
        }

        @Test
        @DisplayName("multiple exceptions")
        void testMultipleExceptions() {
            Exception exception1 = new IOException();
            Exception exception2 = new IOException();

            ConcurrentResults<Integer> results = new ConcurrentResults<>(Stream.of(
                    new ConcurrentResult<>(1),
                    new ConcurrentResult<>(exception1),
                    new ConcurrentResult<>(3),
                    new ConcurrentResult<>(exception2)));

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class, results::andAssertNoFailures);

            List<Exception> expected = Arrays.asList(exception1, exception2);

            assertEquals(expected, thrown.getFailures());
        }
    }

    @Nested
    @DisplayName("resultCollector")
    class ResultCollector {

        @StreamTest
        @DisplayName("success")
        void testSuccess(boolean parallel) {
            Stream<ConcurrentResult<Integer>> resultStream = parallel(parallel, IntStream.range(0, 100)
                    .mapToObj(i -> new ConcurrentResult<>(i)));

            List<Integer> resultList = resultStream.collect(ConcurrentResults.resultCollector(Collectors.toList()));

            List<Integer> expected = IntStream.range(0, 100)
                    .boxed()
                    .collect(Collectors.toList());

            assertEquals(expected, resultList);
        }

        @StreamTest
        @DisplayName("single exception")
        void testSingleException(boolean parallel) {
            Exception exception = new IOException();

            Stream<ConcurrentResult<Integer>> resultStream = parallel(parallel, Stream.of(
                    new ConcurrentResult<>(1),
                    new ConcurrentResult<>(exception),
                    new ConcurrentResult<>(3)));

            Collector<ConcurrentResult<Integer>, ?, List<Integer>> collector = ConcurrentResults.resultCollector(Collectors.toList());

            AssertionFailedError thrown = assertThrows(AssertionFailedError.class, () -> resultStream.collect(collector));

            assertSame(exception, thrown.getCause());
        }

        @StreamTest
        @DisplayName("multiple exceptions")
        void testMultipleExceptions(boolean parallel) {
            Exception exception1 = new IOException();
            Exception exception2 = new IOException();

            Stream<ConcurrentResult<Integer>> resultStream = parallel(parallel, Stream.of(
                    new ConcurrentResult<>(1),
                    new ConcurrentResult<>(exception1),
                    new ConcurrentResult<>(3),
                    new ConcurrentResult<>(exception2)));

            Collector<ConcurrentResult<Integer>, ?, List<Integer>> collector = ConcurrentResults.resultCollector(Collectors.toList());

            MultipleFailuresError thrown = assertThrows(MultipleFailuresError.class, () -> resultStream.collect(collector));

            List<Exception> expected = Arrays.asList(exception1, exception2);

            assertEquals(expected, thrown.getFailures());
        }

        private <T> Stream<T> parallel(boolean parallel, Stream<T> stream) {
            return parallel ? stream.parallel() : stream.sequential();
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ParameterizedTest(name = "parallel: {0}")
    @ValueSource(booleans = { false, true })
    private @interface StreamTest {
        // No content
    }
}
