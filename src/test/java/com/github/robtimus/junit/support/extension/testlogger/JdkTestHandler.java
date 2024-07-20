/*
 * JdkTestHandler.java
 * Copyright 2022 Rob Spoor
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

@SuppressWarnings({ "javadoc", "exports" })
public class JdkTestHandler extends Handler {

    private final List<LogRecord> records = new ArrayList<>();

    @Override
    public void publish(LogRecord logRecord) {
        if (isLoggable(logRecord)) {
            records.add(logRecord);
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

    List<LogRecord> getRecords() {
        return records;
    }

    void clearRecords() {
        records.clear();
    }
}
