/*
 * LogOnFailureTest.java
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

package com.github.robtimus.junit.support.extension.logonfailure;

import static com.github.robtimus.junit.support.extension.util.TestUtils.assertSingleTestFailure;
import static com.github.robtimus.junit.support.extension.util.TestUtils.getSingleTestFailure;
import static com.github.robtimus.junit.support.extension.util.TestUtils.runTests;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.opentest4j.AssertionFailedError;
import com.github.robtimus.junit.support.extension.testlogger.TestLogger;
import com.github.robtimus.junit.support.extension.util.JdkLoggingUtils;
import com.github.robtimus.junit.support.extension.util.Log4jUtils;
import com.github.robtimus.junit.support.extension.util.LogbackUtils;
import com.github.robtimus.junit.support.extension.util.Reload4jUtils;

@SuppressWarnings("nls")
final class LogOnFailureTest {

    private LogOnFailureTest() {
    }

    @BeforeAll
    @AfterAll
    static void validateLoggers() {
        JdkLoggingUtils.validateLoggers();
        Log4jUtils.validateLoggers();
        LogbackUtils.validateLoggers();
        Reload4jUtils.validateLoggers();
    }

    @Nested
    @DisplayName("JDK")
    class JdkLogging {

        @BeforeEach
        void clearRecords() {
            JdkLoggingUtils.getTestHandler().clearRecords();
            JdkLoggingUtils.getRootTestHandler().clearRecords();
        }

        @Test
        @DisplayName("without failures")
        void testWithoutFailures() {
            EngineExecutionResults results = runTests(LogOnFailureTest.LogsAndFailures.WithoutFailures.class);

            assertEquals(2, results.testEvents().succeeded().count());
            assertEquals(0, results.testEvents().failed().count());

            List<java.util.logging.LogRecord> records = JdkLoggingUtils.getTestHandler().getRecords();
            assertEquals(0, records.size());

            List<java.util.logging.LogRecord> rootRecords = JdkLoggingUtils.getRootTestHandler().getRecords();
            assertEquals(0, rootRecords.size());
        }

        @Test
        @DisplayName("with failures")
        void testWithFailures() {
            EngineExecutionResults results = runTests(LogOnFailureTest.LogsAndFailures.WithFailures.class);

            assertEquals(1, results.testEvents().succeeded().count());
            assertEquals(1, results.testEvents().failed().count());

            Throwable throwable = getSingleTestFailure(results);
            assertEquals(AssertionFailedError.class, throwable.getClass());
            assertEquals("expected: <false> but was: <true>", throwable.getMessage());

            List<java.util.logging.LogRecord> records = JdkLoggingUtils.getTestHandler().getRecords();
            assertEquals(2, records.size());
            assertEquals(java.util.logging.Level.INFO, records.get(0).getLevel());
            assertEquals("info before failure", records.get(0).getMessage());
            assertEquals(java.util.logging.Level.SEVERE, records.get(1).getLevel());
            assertEquals("severe before failure", records.get(1).getMessage());

            List<java.util.logging.LogRecord> rootRecords = JdkLoggingUtils.getRootTestHandler().getRecords();
            assertEquals(2, rootRecords.size());
            assertEquals(java.util.logging.Level.INFO, rootRecords.get(0).getLevel());
            assertEquals("info before failure", rootRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.SEVERE, records.get(1).getLevel());
            assertEquals("severe before failure", rootRecords.get(1).getMessage());
        }
    }

    @Nested
    @DisplayName("Log4j")
    class Log4jLogging {

        @BeforeEach
        void clearEvents() {
            Log4jUtils.getTestAppender().clearEvents();
            Log4jUtils.getRootTestAppender().clearEvents();
        }

        @Test
        @DisplayName("without failures")
        void testWithoutFailures() {
            EngineExecutionResults results = runTests(LogOnFailureTest.LogsAndFailures.WithoutFailures.class);

            assertEquals(2, results.testEvents().succeeded().count());
            assertEquals(0, results.testEvents().failed().count());

            List<org.apache.logging.log4j.core.LogEvent> events = Log4jUtils.getTestAppender().getEvents();
            assertEquals(0, events.size());

            List<org.apache.logging.log4j.core.LogEvent> rootEvents = Log4jUtils.getRootTestAppender().getEvents();
            assertEquals(0, rootEvents.size());
        }

        @Test
        @DisplayName("with failures")
        void testWithFailures() {
            EngineExecutionResults results = runTests(LogOnFailureTest.LogsAndFailures.WithFailures.class);

            assertEquals(1, results.testEvents().succeeded().count());
            assertEquals(1, results.testEvents().failed().count());

            Throwable throwable = getSingleTestFailure(results);
            assertEquals(AssertionFailedError.class, throwable.getClass());
            assertEquals("expected: <false> but was: <true>", throwable.getMessage());

            List<org.apache.logging.log4j.core.LogEvent> events = Log4jUtils.getTestAppender().getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.logging.log4j.Level.INFO, events.get(0).getLevel());
            assertEquals("info before failure", events.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.ERROR, events.get(1).getLevel());
            assertEquals("error before failure", events.get(1).getMessage().getFormattedMessage());

            List<org.apache.logging.log4j.core.LogEvent> rootEvents = Log4jUtils.getRootTestAppender().getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.logging.log4j.Level.INFO, rootEvents.get(0).getLevel());
            assertEquals("info before failure", rootEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.ERROR, events.get(1).getLevel());
            assertEquals("error before failure", rootEvents.get(1).getMessage().getFormattedMessage());
        }
    }

    @Nested
    @DisplayName("Logback")
    class LogbackLogging {

        @BeforeEach
        void clearEvents() {
            LogbackUtils.getTestAppender().clearEvents();
            LogbackUtils.getRootTestAppender().clearEvents();
        }

        @Test
        @DisplayName("without failures")
        void testWithoutFailures() {
            EngineExecutionResults results = runTests(LogOnFailureTest.LogsAndFailures.WithoutFailures.class);

            assertEquals(2, results.testEvents().succeeded().count());
            assertEquals(0, results.testEvents().failed().count());

            List<ch.qos.logback.classic.spi.ILoggingEvent> events = LogbackUtils.getTestAppender().getEvents();
            assertEquals(0, events.size());

            List<ch.qos.logback.classic.spi.ILoggingEvent> rootEvents = LogbackUtils.getRootTestAppender().getEvents();
            assertEquals(0, rootEvents.size());
        }

        @Test
        @DisplayName("with failures")
        void testWithFailures() {
            EngineExecutionResults results = runTests(LogOnFailureTest.LogsAndFailures.WithFailures.class);

            assertEquals(1, results.testEvents().succeeded().count());
            assertEquals(1, results.testEvents().failed().count());

            Throwable throwable = getSingleTestFailure(results);
            assertEquals(AssertionFailedError.class, throwable.getClass());
            assertEquals("expected: <false> but was: <true>", throwable.getMessage());

            List<ch.qos.logback.classic.spi.ILoggingEvent> events = LogbackUtils.getTestAppender().getEvents();
            assertEquals(2, events.size());
            assertEquals(ch.qos.logback.classic.Level.INFO, events.get(0).getLevel());
            assertEquals("info before failure", events.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.ERROR, events.get(1).getLevel());
            assertEquals("error before failure", events.get(1).getMessage());

            List<ch.qos.logback.classic.spi.ILoggingEvent> rootEvents = LogbackUtils.getRootTestAppender().getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(ch.qos.logback.classic.Level.INFO, rootEvents.get(0).getLevel());
            assertEquals("info before failure", rootEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.ERROR, events.get(1).getLevel());
            assertEquals("error before failure", rootEvents.get(1).getMessage());
        }
    }

    @Nested
    @DisplayName("Reload4j")
    class Reload4jLogging {

        @BeforeEach
        void clearEvents() {
            Reload4jUtils.getTestAppender().clearEvents();
            Reload4jUtils.getRootTestAppender().clearEvents();
        }

        @Test
        @DisplayName("without failures")
        void testWithoutFailures() {
            EngineExecutionResults results = runTests(LogOnFailureTest.LogsAndFailures.WithoutFailures.class);

            assertEquals(2, results.testEvents().succeeded().count());
            assertEquals(0, results.testEvents().failed().count());

            List<org.apache.log4j.spi.LoggingEvent> events = Reload4jUtils.getTestAppender().getEvents();
            assertEquals(0, events.size());

            List<org.apache.log4j.spi.LoggingEvent> rootEvents = Reload4jUtils.getRootTestAppender().getEvents();
            assertEquals(0, rootEvents.size());
        }

        @Test
        @DisplayName("with failures")
        void testWithFailures() {
            EngineExecutionResults results = runTests(LogOnFailureTest.LogsAndFailures.WithFailures.class);

            assertEquals(1, results.testEvents().succeeded().count());
            assertEquals(1, results.testEvents().failed().count());

            Throwable throwable = getSingleTestFailure(results);
            assertEquals(AssertionFailedError.class, throwable.getClass());
            assertEquals("expected: <false> but was: <true>", throwable.getMessage());

            List<org.apache.log4j.spi.LoggingEvent> events = Reload4jUtils.getTestAppender().getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.log4j.Level.INFO, events.get(0).getLevel());
            assertEquals("info before failure", events.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.ERROR, events.get(1).getLevel());
            assertEquals("error before failure", events.get(1).getMessage());

            List<org.apache.log4j.spi.LoggingEvent> rootEvents = Reload4jUtils.getRootTestAppender().getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.log4j.Level.INFO, rootEvents.get(0).getLevel());
            assertEquals("info before failure", rootEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.ERROR, events.get(1).getLevel());
            assertEquals("error before failure", rootEvents.get(1).getMessage());
        }
    }

    @Nested
    @DisplayName("invalid usage")
    class InvalidUsage {

        @Test
        @DisplayName("unsupported logger type")
        void testUnsupportedLoggerType() {
            assertSingleTestFailure(LogOnFailureTest.UnsupportedLoggerType.class, PreconditionViolationException.class,
                    equalTo("Object type not supported: java.lang.String"));
        }

        @Test
        @DisplayName("null logger")
        void testNullLogger() {
            assertSingleTestFailure(LogOnFailureTest.NullLogger.class, PreconditionViolationException.class,
                    equalTo("null not supported"));
        }
    }

    static final class UnsupportedLoggerType {

        @LogOnFailure
        private static final String TEST = "test";

        @Test
        void testUnsupportedType() {
            assertTrue(true);
        }
    }

    static final class NullLogger {

        @LogOnFailure
        private static final java.util.logging.Logger LOGGER = null;

        @Test
        void testUnsupportedType() {
            assertTrue(true);
        }
    }

    static final class LogsAndFailures {

        static final class WithoutFailures {

            @LogOnFailure
            private static final java.util.logging.Logger JDK_LOGGER = java.util.logging.Logger.getLogger(TestLogger.class.getName());

            @LogOnFailure
            public static final org.apache.logging.log4j.Logger LOG4J_LOGGER = org.apache.logging.log4j.LogManager.getLogger(TestLogger.class);

            @LogOnFailure
            private final org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(TestLogger.class);

            @LogOnFailure
            public final org.apache.log4j.Logger reload4jLogger = org.apache.log4j.Logger.getLogger(TestLogger.class);

            @Test
            void testSuccess1() {
                JDK_LOGGER.info("info1");

                LOG4J_LOGGER.info("info1");

                slf4jLogger.info("info1");

                reload4jLogger.info("info1");

                assertTrue(true);
            }

            @Test
            void testSuccess2() {
                JDK_LOGGER.info("info2");

                LOG4J_LOGGER.info("info2");

                slf4jLogger.info("info2");

                reload4jLogger.info("info2");

                assertTrue(true);
            }
        }

        static final class WithFailures {

            @LogOnFailure
            private static final java.util.logging.Logger JDK_LOGGER = java.util.logging.Logger.getLogger(TestLogger.class.getName());

            @LogOnFailure
            public static final org.apache.logging.log4j.Logger LOG4J_LOGGER = org.apache.logging.log4j.LogManager.getLogger(TestLogger.class);

            @LogOnFailure
            private final org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(TestLogger.class);

            @LogOnFailure
            public final org.apache.log4j.Logger reload4jLogger = org.apache.log4j.Logger.getLogger(TestLogger.class);

            @Test
            void testSuccess() {
                JDK_LOGGER.info("info");

                LOG4J_LOGGER.info("info");

                slf4jLogger.info("info");

                reload4jLogger.info("info");

                assertTrue(true);
            }

            @Test
            void testFailure() {
                JDK_LOGGER.fine("fine before failure");
                JDK_LOGGER.info("info before failure");
                JDK_LOGGER.severe("severe before failure");

                LOG4J_LOGGER.debug("debug before failure");
                LOG4J_LOGGER.info("info before failure");
                LOG4J_LOGGER.error("error before failure");

                slf4jLogger.debug("debug before failure");
                slf4jLogger.info("info before failure");
                slf4jLogger.error("error before failure");

                reload4jLogger.debug("debug before failure");
                reload4jLogger.info("info before failure");
                reload4jLogger.error("error before failure");

                assertFalse(true);
            }
        }
    }
}
