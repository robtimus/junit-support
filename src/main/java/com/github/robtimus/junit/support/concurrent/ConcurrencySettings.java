/*
 * ConcurrencySettings.java
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

import static com.github.robtimus.junit.support.concurrent.ConcurrentRunner.validateCount;
import static com.github.robtimus.junit.support.concurrent.ConcurrentRunner.validateThreadCount;
import org.junit.jupiter.api.function.Executable;

/**
 * Settings to apply when calling {@link ConcurrentRunner#runConcurrently(Executable, ConcurrencySettings)}.
 * This allows shared settings to be applied to multiple concurrent calls without having to create {@link ConcurrentRunner} instances yourself.
 *
 * @author Rob Spoor
 * @since 3.0
 */
public final class ConcurrencySettings {

    private final int count;

    private int threadCount;

    private ConcurrencySettings(int count) {
        this.count = count;
        this.threadCount = Integer.MAX_VALUE;
    }

    /**
     * Creates a new concurrency settings object.
     *
     * @param count The number of times to run blocks of code.
     * @return The created concurrency settings object.
     * @throws IllegalArgumentException If the given count is not positive.
     */
    public static ConcurrencySettings withCount(int count) {
        validateCount(count);
        return new ConcurrencySettings(count);
    }

    /**
     * Sets the number of threads to use.
     *
     * @param threadCount The number of threads to use.
     * @return This object.
     * @throws IllegalArgumentException If the given thread count is not at least 2.
     * @see ConcurrentRunner#withThreadCount(int)
     */
    public ConcurrencySettings withThreadCount(int threadCount) {
        validateThreadCount(threadCount);
        this.threadCount = threadCount;
        return this;
    }

    /**
     * Returns the number of times to run blocks of code.
     *
     * @return The number of times to run blocks of code.
     */
    public int count() {
        return count;
    }

    /**
     * Returns the number of threads to use.
     *
     * @return The number of threads to use.
     */
    public int threadCount() {
        return threadCount;
    }
}
