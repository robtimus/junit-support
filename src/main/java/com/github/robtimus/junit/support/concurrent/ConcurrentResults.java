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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.opentest4j.MultipleFailuresError;

/**
 * A class that represents the results produced by a {@link ConcurrentRunner}.
 * <p>
 * For each instance of this class, only one method should be called. Calling more than one method will cause an exception to be thrown.
 *
 * @author Rob Spoor
 * @param <T> The type of result.
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
     * Asserts that no {@link Executable} or {@link ThrowingSupplier} threw an error or exception.
     * If any {@link Executable} or {@link ThrowingSupplier} threw an error or exception, this method will throw an error or exception.
     */
    public void andAssertNoFailures() {
        List<Throwable> failures = results
                .map(ConcurrentResult::failure)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (failures.isEmpty()) {
            return;
        }
        if (failures.size() > 1) {
            MultipleFailuresError error = new MultipleFailuresError(null, failures);
            failures.forEach(error::addSuppressed);
            throw error;
        }
        Throwable singleFailure = failures.get(0);
        throwUnchecked(singleFailure);
    }
}
