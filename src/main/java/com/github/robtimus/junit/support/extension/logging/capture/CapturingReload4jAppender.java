/*
 * CapturingReload4jAppender.java
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
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * An {@link Appender} that captures the logged events.
 *
 * @author Rob Spoor
 */
public final class CapturingReload4jAppender extends AppenderSkeleton {

    private final List<LoggingEvent> events = new ArrayList<>();

    @Override
    protected void append(LoggingEvent event) {
        synchronized (events) {
            events.add(event);
        }
    }

    /**
     * Returns all events that where passed to {@link #append(LoggingEvent)}.
     *
     * @return A list with all events that where passed to {@link #append(LoggingEvent)}.
     */
    public List<LoggingEvent> getEvents() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    /**
     * Removes all events that where previously passed to {@link #append(LoggingEvent)}.
     * Afterwards {@link #getEvents()} will return an empty list until more events are published.
     */
    public void clearEvents() {
        synchronized (events) {
            events.clear();
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public void close() {
        // does nothing
    }
}
