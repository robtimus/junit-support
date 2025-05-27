/*
 * CapturingLogbackAppender.java
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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;

/**
 * An {@link Appender} that captures the logged events.
 *
 * @author Rob Spoor
 * @since 3.1
 */
public final class CapturingLogbackAppender extends AppenderBase<ILoggingEvent> {

    private final List<ILoggingEvent> events = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent eventObject) {
        synchronized (events) {
            events.add(eventObject);
        }
    }

    /**
     * Returns all events that where passed to {@link #append(ILoggingEvent)}.
     *
     * @return A list with all events that where passed to {@link #append(ILoggingEvent)}.
     */
    public List<ILoggingEvent> getEvents() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    /**
     * Removes all events that where previously passed to {@link #append(ILoggingEvent)}.
     * Afterwards {@link #getEvents()} will return an empty list until more events are published.
     */
    public void clearEvents() {
        synchronized (events) {
            events.clear();
        }
    }
}
