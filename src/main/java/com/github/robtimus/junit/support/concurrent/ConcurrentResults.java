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
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
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
 * @since 3.0
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
     * Collects the results produced by the {@link ConcurrentRunner} that created this object.
     * <p>
     * This method is similar to calling {@link #andStreamResults()} and then collecting the results using the given collector.
     * The main difference is that this method will report <em>every</em> error and exception that was thrown instead of only the first.
     *
     * @param <R> The result type of the collector.
     * @param collector The collector to use.
     * @return The result of applying the given collector to the results produced by the {@link ConcurrentRunner} that created this object.
     * @throws NullPointerException If the given collector is {@code null}.
     */
    public <R> R andCollectResults(Collector<T, ?, R> collector) {
        Objects.requireNonNull(collector);
        return results.collect(resultCollector(collector));
    }

    /**
     * Returns a list with the results produced by the {@link ConcurrentRunner} that created this object.
     * <p>
     * This method is shorthand for calling {@link #andCollectResults(Collector)} with a collector that returns a list.
     * Whether or not this list is modifiable is unspecified.
     *
     * @return A list with the results produced by the {@link ConcurrentRunner} that created this object.
     */
    public List<T> andListResults() {
        return andCollectResults(Collectors.toList());
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

    static <T, R> Collector<ConcurrentResult<T>, ?, R> resultCollector(Collector<T, ?, R> collector) {
        return Collector.of(
                () -> new ResultCollector<>(collector),
                ResultCollector::accumulate,
                ResultCollector::combine,
                ResultCollector::finish
        );
    }

    static final class ResultCollector<T, A, R> {

        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;

        private final List<Throwable> failures = new ArrayList<>();

        private A state;

        private ResultCollector(Collector<T, A, R> collector) {
            accumulator = collector.accumulator();
            combiner = collector.combiner();
            finisher = collector.finisher();

            state = collector.supplier().get();
        }

        private void accumulate(ConcurrentResult<T> result) {
            Throwable failure = result.failure();
            if (failure != null) {
                failures.add(failure);
            } else {
                accumulator.accept(state, result.result());
            }
        }

        private ResultCollector<T, A, R> combine(ResultCollector<T, A, R> other) {
            failures.addAll(other.failures);
            state = combiner.apply(state, other.state);
            return this;
        }

        private R finish() {
            throwUnchecked(failures);
            return finisher.apply(state);
        }
    }
}
