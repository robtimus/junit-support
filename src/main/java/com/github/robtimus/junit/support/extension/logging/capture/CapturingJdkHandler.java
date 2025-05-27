/*
 * CapturingJdkHandler.java
 * Copyright 2025 Rob Spoor
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

package com.github.robtimus.junit.support.extension.logging.capture;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * A {@link Handler} that captures the logged records.
 *
 * @author Rob Spoor
 * @since 3.1
 */
public final class CapturingJdkHandler extends Handler {

    private final List<LogRecord> records = new ArrayList<>();

    @Override
    public void publish(LogRecord logRecord) {
        synchronized (records) {
            records.add(logRecord);
        }
    }

    /**
     * Returns all records that where passed to {@link #publish(LogRecord)}.
     *
     * @return A list with all records that where passed to {@link #publish(LogRecord)}.
     */
    public List<LogRecord> getRecords() {
        synchronized (records) {
            return new ArrayList<>(records);
        }
    }

    /**
     * Removes all records that where previously passed to {@link #publish(LogRecord)}.
     * Afterwards {@link #getRecords()} will return an empty list until more records are published.
     */
    public void clearRecords() {
        synchronized (records) {
            records.clear();
        }
    }

    @Override
    public void flush() {
        // does nothing
    }

    @Override
    public void close() {
        // does nothing
    }
}
