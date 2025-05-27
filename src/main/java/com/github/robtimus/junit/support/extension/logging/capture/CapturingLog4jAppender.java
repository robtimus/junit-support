/*
 * CapturingLog4jAppender.java
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
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

/**
 * An {@link Appender} that captures the logged events.
 *
 * @author Rob Spoor
 * @since 3.1
 */
public final class CapturingLog4jAppender extends AbstractAppender {

    private final List<LogEvent> events = new ArrayList<>();

    /**
     * Creates a new capturing appender.
     *
     * @param name The appender name.
     */
    public CapturingLog4jAppender(String name) {
        super(name, null, null, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
        LogEvent immutableEvent = event != null ? event.toImmutable() : null;
        synchronized (events) {
            events.add(immutableEvent);
        }
    }

    /**
     * Returns all events that where passed to {@link #append(LogEvent)}.
     *
     * @return A list with all events that where passed to {@link #append(LogEvent)}.
     */
    public List<LogEvent> getEvents() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    /**
     * Removes all events that where previously passed to {@link #append(LogEvent)}.
     * Afterwards {@link #getEvents()} will return an empty list until more events are published.
     */
    public void clearEvents() {
        synchronized (events) {
            events.clear();
        }
    }
}
