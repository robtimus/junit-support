/*
 * Reload4jTestAppender.java
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

package com.github.robtimus.junit.support.extension.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

@SuppressWarnings("javadoc")
public class Reload4jTestAppender extends AppenderSkeleton {

    private final List<LoggingEvent> events = new ArrayList<>();

    @Override
    protected void append(LoggingEvent event) {
        events.add(event);
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public void close() {
        // does nothing
    }

    public List<LoggingEvent> getEvents() {
        return events;
    }

    public void clearEvents() {
        events.clear();
    }
}
