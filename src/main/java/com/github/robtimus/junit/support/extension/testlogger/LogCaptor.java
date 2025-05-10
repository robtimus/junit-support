/*
 * LogCaptor.java
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

package com.github.robtimus.junit.support.extension.testlogger;

import java.util.List;
import java.util.function.Supplier;

/**
 * An object that captures logged events or records.
 *
 * @author Rob Spoor
 * @param <T> The type of logged event or record.
 * @since 3.0
 */
public final class LogCaptor<T> {

    private final Supplier<List<T>> loggedGetter;
    private final Runnable resetter;

    LogCaptor(Supplier<List<T>> loggedGetter, Runnable resetter) {
        this.loggedGetter = loggedGetter;
        this.resetter = resetter;
    }

    /**
     * Returns all events or records that were logged.
     *
     * @return All events or records that were logged.
     */
    public List<T> logged() {
        return loggedGetter.get();
    }

    /**
     * Resets the logged events or records. Afterwards {@link #logged()} will return an empty list until more logged events or records occur.
     */
    public void reset() {
        resetter.run();
    }
}
