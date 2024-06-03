/*
 * ConcurrentResults.java
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

import static com.github.robtimus.junit.support.concurrent.ConcurrentResult.throwUnchecked;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;

/**
 * A class that represents the results produced by a {@link ConcurrentRunner}.
 * <p>
 * For each instance of this class, only one method should be called. Calling more than one method will cause an exception to be thrown.
 *
 * @author Rob Spoor
 * @param <T> The type of result.
 * @since 2.3
 */
public final class ConcurrentResults<T> {

    private final Stream<ConcurrentResult<T>> results;

    ConcurrentResults(Stream<ConcurrentResult<T>> results) {
        this.results = results;
    }

    /**
     * Returns a stream with the results produced by the {@link ConcurrentRunner} that created this object.
     * <p>
     * If any {@link Executable} or {@link ThrowingSupplier} threw an error or exception, the returned stream will throw an error or exception when
     * a terminal operator is executed.
     *
     * @return A stream with the results produced by the {@link ConcurrentRunner} that created this object.
     */
    public Stream<T> andStreamResults() {
        return results.map(ConcurrentResult::getOrThrow);
    }

    /**
     * Returns a stream with the results produced by the {@link ConcurrentRunner} that created this object.
     * <p>
     * This method is similar to calling {@link #andStreamResults()} and the collecting the results to a list. The main difference is that this method
     * will report <em>every</em> error and exception that was thrown instead of only the first.
     *
     * @return A list with the results produced by the {@link ConcurrentRunner} that created this object.
     */
    public List<T> andListResults() {
        return results.collect(toList());
    }

    /**
     * Asserts that no {@link Executable} or {@link ThrowingSupplier} threw an error or exception.
     * If any {@link Executable} or {@link ThrowingSupplier} threw an error or exception, this method will throw an error or exception.
     */
    public void andAssertNoFailures() {
        List<Throwable> failures = results
                .map(ConcurrentResult::failure)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        throwUnchecked(failures);
    }

    static <T> Collector<ConcurrentResult<T>, ?, List<T>> toList() {
        return Collector.of(
                () -> new ResultCollector<T>(),
                ResultCollector::accumulate,
                ResultCollector::combine,
                ResultCollector::finish
        );
    }

    static final class ResultCollector<T> {

        private final List<T> results = new ArrayList<>();
        private final List<Throwable> failures = new ArrayList<>();

        private void accumulate(ConcurrentResult<T> result) {
            Throwable failure = result.failure();
            if (failure != null) {
                failures.add(failure);
            } else {
                results.add(result.result());
            }
        }

        private ResultCollector<T> combine(ResultCollector<T> other) {
            results.addAll(other.results);
            failures.addAll(other.failures);
            return this;
        }

        private List<T> finish() {
            throwUnchecked(failures);
            return results;
        }
    }
}
