/*
 * Log4jNullAppenderTest.java
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import java.util.Collections;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("nls")
class Log4jNullAppenderTest {

    @Nested
    @DisplayName("append")
    class Append {

        @Test
        @DisplayName("null event")
        void testNullEvent() {
            Log4jNullAppender appender = spy(Log4jNullAppender.create("MockAppender"));

            appender.append(null);

            verify(appender).ignore(null);
        }

        @Test
        @DisplayName("mutable event")
        void testMutableEvent() {
            Log4jNullAppender appender = spy(Log4jNullAppender.create("MockAppender"));

            MutableLogEvent event = new MutableLogEvent();
            event.setLoggerName("logger");
            event.setLevel(Level.INFO);
            event.setMessage(new SimpleMessage("test message"));

            appender.append(event);

            ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);

            verify(appender).append(event);
            verify(appender, never()).ignore(event);
            verify(appender).ignore(eventCaptor.capture());

            LogEvent capturedEvent = eventCaptor.getValue();
            assertEquals(event.getLevel(), capturedEvent.getLevel());
            assertEquals(event.getMessage().getFormattedMessage(), capturedEvent.getMessage().getFormattedMessage());
        }

        @Test
        @DisplayName("immutable event")
        void testImmutableEvent() {
            Log4jNullAppender appender = spy(Log4jNullAppender.create("MockAppender"));

            SimpleMessage message = new SimpleMessage("test message");
            LogEvent event = new Log4jLogEvent("logger", null, "logger", Level.INFO, message, Collections.emptyList(), null);

            appender.append(event);

            verify(appender).append(event);
            verify(appender).ignore(event);
        }
    }

    @Test
    @DisplayName("create")
    void testCreate() {
        Log4jNullAppender appender = Log4jNullAppender.create("appender");

        assertEquals("appender", appender.getName());
        assertTrue(appender.isStarted());
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTest {

        @ParameterizedTest(name = "{0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("setStartAutomatically")
        void testSetStartAutomatically(boolean startAutomatically) {
            Log4jNullAppender appender = new Log4jNullAppender.Builder()
                    .setName("appender")
                    .setStartAutomatically(startAutomatically)
                    .build();

            assertEquals("appender", appender.getName());
            assertEquals(startAutomatically, appender.isStarted());
        }
    }
}
