/*
 * TestLoggerTest.java
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

import static com.github.robtimus.junit.support.OptionalAssertions.assertIsPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("nls")
final class TestLoggerTest {

    private static final String LOGGER_NAME = "com.github.robtimus.junit.support.extension.testlogger.TestLogger";
    private static final String DISABLED_LOGGER_NAME = LOGGER_NAME + ".disabled";

    private static final java.util.logging.Logger JDK_LOGGER = java.util.logging.Logger.getLogger(LOGGER_NAME);
    private static final java.util.logging.Logger JDK_DISABLED_LOGGER = java.util.logging.Logger.getLogger(DISABLED_LOGGER_NAME);

    private static final org.apache.logging.log4j.Logger LOG4J_LOGGER = org.apache.logging.log4j.LogManager.getLogger(TestLogger.class);
    private static final org.apache.logging.log4j.Logger LOG4J_DISABLED_LOGGER = org.apache.logging.log4j.LogManager.getLogger(DISABLED_LOGGER_NAME);

    private static final org.slf4j.Logger SLF4J_LOGGER = org.slf4j.LoggerFactory.getLogger(TestLogger.class);
    private static final org.slf4j.Logger SLF4J_DISABLED_LOGGER = org.slf4j.LoggerFactory.getLogger(DISABLED_LOGGER_NAME);

    private static final org.apache.log4j.Logger RELOAD4J_LOGGER = org.apache.log4j.Logger.getLogger(TestLogger.class);
    private static final org.apache.log4j.Logger RELOAD4J_DISABLED_LOGGER = org.apache.log4j.Logger.getLogger(DISABLED_LOGGER_NAME);

    private TestLoggerTest() {
    }

    @BeforeAll
    @AfterAll
    static void validateLoggers() {
        JdkLoggerContextTest.validateLoggers();
        Log4jLoggerContextTest.validateLoggers();
        LogbackLoggerContextTest.validateLoggers();
        Reload4jLoggerContextTest.validateLoggers();
    }

    @Nested
    @DisplayName("JDK")
    class JdkLogging {

        @TestLogger(LOGGER_NAME)
        private JdkLoggerContext contextWithName;

        @TestLogger.ForClass(TestLogger.class)
        private JdkLoggerContext contextWithClass;

        @Nested
        @DisplayName("With logger name")
        class WithLoggerName {

            @Nested
            @DisplayName("Instance per method")
            @TestInstance(Lifecycle.PER_METHOD)
            class InstancePerMethod extends JdkLoggerTests {

                @TestLogger(LOGGER_NAME)
                private JdkLoggerContext logger;

                @TestLogger.Root
                private JdkLoggerContext rootLogger;

                @Override
                JdkLoggerContext logger() {
                    return logger;
                }

                @Override
                JdkLoggerContext rootLogger() {
                    return rootLogger;
                }
            }

            @Nested
            @DisplayName("Instance per class")
            @TestInstance(Lifecycle.PER_CLASS)
            class InstancePerClass extends JdkLoggerTests {

                @TestLogger(LOGGER_NAME)
                private JdkLoggerContext logger;

                @TestLogger.Root
                private JdkLoggerContext rootLogger;

                @Override
                JdkLoggerContext logger() {
                    return logger;
                }

                @Override
                JdkLoggerContext rootLogger() {
                    return rootLogger;
                }
            }
        }

        @Nested
        @DisplayName("With logger class")
        class WithLogger {

            @Nested
            @DisplayName("Instance per method")
            @TestInstance(Lifecycle.PER_METHOD)
            class InstancePerMethod extends JdkLoggerTests {

                @TestLogger.ForClass(TestLogger.class)
                private JdkLoggerContext logger;

                @TestLogger.Root
                private JdkLoggerContext rootLogger;

                @Override
                JdkLoggerContext logger() {
                    return logger;
                }

                @Override
                JdkLoggerContext rootLogger() {
                    return rootLogger;
                }
            }

            @Nested
            @DisplayName("Instance per class")
            @TestInstance(Lifecycle.PER_CLASS)
            class InstancePerClass extends JdkLoggerTests {

                @TestLogger.ForClass(TestLogger.class)
                private JdkLoggerContext logger;

                @TestLogger.Root
                private JdkLoggerContext rootLogger;

                @Override
                JdkLoggerContext logger() {
                    return logger;
                }

                @Override
                JdkLoggerContext rootLogger() {
                    return rootLogger;
                }
            }
        }

        @Test
        @DisplayName("instance reuse")
        void testInstanceReuse(@TestLogger(LOGGER_NAME) JdkLoggerContext contextWithName,
                @TestLogger.ForClass(TestLogger.class) JdkLoggerContext contextWithClass) {

            assertSame(this.contextWithName, this.contextWithClass);
            assertSame(this.contextWithName, contextWithName);
            assertSame(this.contextWithName, contextWithClass);
        }

        @Test
        @DisplayName("disabled logging")
        void testDisabledLogging(@TestLogger(DISABLED_LOGGER_NAME) @DisableLogging JdkLoggerContext logger) {
            JdkTestHandler testHandler = JdkLoggerTests.testHandler(logger);
            testHandler.clearRecords();

            JDK_DISABLED_LOGGER.log(java.util.logging.Level.WARNING, "warning message");
            JDK_DISABLED_LOGGER.log(java.util.logging.Level.INFO, "info message");
            JDK_DISABLED_LOGGER.log(java.util.logging.Level.FINE, "fine message");

            assertEquals(Collections.emptyList(), testHandler.getRecords());
        }
    }

    @Nested
    @DisplayName("Log4j")
    class Log4jLogging {

        @TestLogger(LOGGER_NAME)
        private Log4jLoggerContext contextWithName;

        @TestLogger.ForClass(TestLogger.class)
        private Log4jLoggerContext contextWithClass;

        @Nested
        @DisplayName("With logger name")
        class WithLoggerName {

            @Nested
            @DisplayName("Instance per method")
            @TestInstance(Lifecycle.PER_METHOD)
            class InstancePerMethod extends Log4jLoggerTests {

                @TestLogger(LOGGER_NAME)
                private Log4jLoggerContext logger;

                @TestLogger.Root
                private Log4jLoggerContext rootLogger;

                @Override
                Log4jLoggerContext logger() {
                    return logger;
                }

                @Override
                Log4jLoggerContext rootLogger() {
                    return rootLogger;
                }
            }

            @Nested
            @DisplayName("Instance per class")
            @TestInstance(Lifecycle.PER_CLASS)
            class InstancePerClass extends Log4jLoggerTests {

                @TestLogger(LOGGER_NAME)
                private Log4jLoggerContext logger;

                @TestLogger.Root
                private Log4jLoggerContext rootLogger;

                @Override
                Log4jLoggerContext logger() {
                    return logger;
                }

                @Override
                Log4jLoggerContext rootLogger() {
                    return rootLogger;
                }
            }
        }

        @Nested
        @DisplayName("With logger class")
        class WithLogger {

            @Nested
            @DisplayName("Instance per method")
            @TestInstance(Lifecycle.PER_METHOD)
            class InstancePerMethod extends Log4jLoggerTests {

                @TestLogger.ForClass(TestLogger.class)
                private Log4jLoggerContext logger;

                @TestLogger.Root
                private Log4jLoggerContext rootLogger;

                @Override
                Log4jLoggerContext logger() {
                    return logger;
                }

                @Override
                Log4jLoggerContext rootLogger() {
                    return rootLogger;
                }
            }

            @Nested
            @DisplayName("Instance per class")
            @TestInstance(Lifecycle.PER_CLASS)
            class InstancePerClass extends Log4jLoggerTests {

                @TestLogger.ForClass(TestLogger.class)
                private Log4jLoggerContext logger;

                @TestLogger.Root
                private Log4jLoggerContext rootLogger;

                @Override
                Log4jLoggerContext logger() {
                    return logger;
                }

                @Override
                Log4jLoggerContext rootLogger() {
                    return rootLogger;
                }
            }
        }

        @Test
        @DisplayName("instance reuse")
        void testInstanceReuse(@TestLogger(LOGGER_NAME) Log4jLoggerContext contextWithName,
                @TestLogger.ForClass(TestLogger.class) Log4jLoggerContext contextWithClass) {

            assertSame(this.contextWithName, this.contextWithClass);
            assertSame(this.contextWithName, contextWithName);
            assertSame(this.contextWithName, contextWithClass);
        }

        @Test
        @DisplayName("disabled logging")
        void testDisabledLogging(@TestLogger(DISABLED_LOGGER_NAME) @DisableLogging Log4jLoggerContext logger) {
            Log4jTestAppender testAppender = Log4jLoggerTests.testAppender(logger);
            testAppender.clearEvents();

            LOG4J_DISABLED_LOGGER.warn("warning message");
            LOG4J_DISABLED_LOGGER.info("info message");
            LOG4J_DISABLED_LOGGER.debug("debug message");

            assertEquals(Collections.emptyList(), testAppender.getEvents());
        }
    }

    @Nested
    @DisplayName("Logback")
    class LogbackLogging {

        @TestLogger(LOGGER_NAME)
        private LogbackLoggerContext contextWithName;

        @TestLogger.ForClass(TestLogger.class)
        private LogbackLoggerContext contextWithClass;

        @Nested
        @DisplayName("With logger name")
        class WithLoggerName {

            @Nested
            @DisplayName("Instance per method")
            @TestInstance(Lifecycle.PER_METHOD)
            class InstancePerMethod extends LogbackLoggerTests {

                @TestLogger(LOGGER_NAME)
                private LogbackLoggerContext logger;

                @TestLogger.Root
                private LogbackLoggerContext rootLogger;

                @Override
                LogbackLoggerContext logger() {
                    return logger;
                }

                @Override
                LogbackLoggerContext rootLogger() {
                    return rootLogger;
                }
            }

            @Nested
            @DisplayName("Instance per class")
            @TestInstance(Lifecycle.PER_CLASS)
            class InstancePerClass extends LogbackLoggerTests {

                @TestLogger(LOGGER_NAME)
                private LogbackLoggerContext logger;

                @TestLogger.Root
                private LogbackLoggerContext rootLogger;

                @Override
                LogbackLoggerContext logger() {
                    return logger;
                }

                @Override
                LogbackLoggerContext rootLogger() {
                    return rootLogger;
                }
            }
        }

        @Nested
        @DisplayName("With logger class")
        class WithLogger {

            @Nested
            @DisplayName("Instance per method")
            @TestInstance(Lifecycle.PER_METHOD)
            class InstancePerMethod extends LogbackLoggerTests {

                @TestLogger.ForClass(TestLogger.class)
                private LogbackLoggerContext logger;

                @TestLogger.Root
                private LogbackLoggerContext rootLogger;

                @Override
                LogbackLoggerContext logger() {
                    return logger;
                }

                @Override
                LogbackLoggerContext rootLogger() {
                    return rootLogger;
                }
            }

            @Nested
            @DisplayName("Instance per class")
            @TestInstance(Lifecycle.PER_CLASS)
            class InstancePerClass extends LogbackLoggerTests {

                @TestLogger.ForClass(TestLogger.class)
                private LogbackLoggerContext logger;

                @TestLogger.Root
                private LogbackLoggerContext rootLogger;

                @Override
                LogbackLoggerContext logger() {
                    return logger;
                }

                @Override
                LogbackLoggerContext rootLogger() {
                    return rootLogger;
                }
            }
        }

        @Test
        @DisplayName("instance reuse")
        void testInstanceReuse(@TestLogger(LOGGER_NAME) LogbackLoggerContext contextWithName,
                @TestLogger.ForClass(TestLogger.class) LogbackLoggerContext contextWithClass) {

            assertSame(this.contextWithName, this.contextWithClass);
            assertSame(this.contextWithName, contextWithName);
            assertSame(this.contextWithName, contextWithClass);
        }

        @Test
        @DisplayName("disabled logging")
        void testDisabledLogging(@TestLogger(DISABLED_LOGGER_NAME) @DisableLogging LogbackLoggerContext logger) {
            LogbackTestAppender testAppender = LogbackLoggerTests.testAppender(logger);
            testAppender.clearEvents();

            SLF4J_DISABLED_LOGGER.warn("warning message");
            SLF4J_DISABLED_LOGGER.info("info message");
            SLF4J_DISABLED_LOGGER.debug("debug message");

            assertEquals(Collections.emptyList(), testAppender.getEvents());
        }
    }

    @Nested
    @DisplayName("Reload4j")
    class Reload4jLogging {

        @TestLogger(LOGGER_NAME)
        private Reload4jLoggerContext contextWithName;

        @TestLogger.ForClass(TestLogger.class)
        private Reload4jLoggerContext contextWithClass;

        @Nested
        @DisplayName("With logger name")
        class WithLoggerName {

            @Nested
            @DisplayName("Instance per method")
            @TestInstance(Lifecycle.PER_METHOD)
            class InstancePerMethod extends Reload4jLoggerTests {

                @TestLogger(LOGGER_NAME)
                private Reload4jLoggerContext logger;

                @TestLogger.Root
                private Reload4jLoggerContext rootLogger;

                @Override
                Reload4jLoggerContext logger() {
                    return logger;
                }

                @Override
                Reload4jLoggerContext rootLogger() {
                    return rootLogger;
                }
            }

            @Nested
            @DisplayName("Instance per class")
            @TestInstance(Lifecycle.PER_CLASS)
            class InstancePerClass extends Reload4jLoggerTests {

                @TestLogger(LOGGER_NAME)
                private Reload4jLoggerContext logger;

                @TestLogger.Root
                private Reload4jLoggerContext rootLogger;

                @Override
                Reload4jLoggerContext logger() {
                    return logger;
                }

                @Override
                Reload4jLoggerContext rootLogger() {
                    return rootLogger;
                }
            }
        }

        @Nested
        @DisplayName("With logger class")
        class WithLogger {

            @Nested
            @DisplayName("Instance per method")
            @TestInstance(Lifecycle.PER_METHOD)
            class InstancePerMethod extends Reload4jLoggerTests {

                @TestLogger.ForClass(TestLogger.class)
                private Reload4jLoggerContext logger;

                @TestLogger.Root
                private Reload4jLoggerContext rootLogger;

                @Override
                Reload4jLoggerContext logger() {
                    return logger;
                }

                @Override
                Reload4jLoggerContext rootLogger() {
                    return rootLogger;
                }
            }

            @Nested
            @DisplayName("Instance per class")
            @TestInstance(Lifecycle.PER_CLASS)
            class InstancePerClass extends Reload4jLoggerTests {

                @TestLogger.ForClass(TestLogger.class)
                private Reload4jLoggerContext logger;

                @TestLogger.Root
                private Reload4jLoggerContext rootLogger;

                @Override
                Reload4jLoggerContext logger() {
                    return logger;
                }

                @Override
                Reload4jLoggerContext rootLogger() {
                    return rootLogger;
                }
            }
        }

        @Test
        @DisplayName("instance reuse")
        void testInstanceReuse(@TestLogger(LOGGER_NAME) Reload4jLoggerContext contextWithName,
                @TestLogger.ForClass(TestLogger.class) Reload4jLoggerContext contextWithClass) {

            assertSame(this.contextWithName, this.contextWithClass);
            assertSame(this.contextWithName, contextWithName);
            assertSame(this.contextWithName, contextWithClass);
        }

        @Test
        @DisplayName("disabled logging")
        void testDisabledLogging(@TestLogger(DISABLED_LOGGER_NAME) @DisableLogging Reload4jLoggerContext logger) {
            Reload4jTestAppender testAppender = Reload4jLoggerTests.testAppender(logger);
            testAppender.clearEvents();

            RELOAD4J_DISABLED_LOGGER.warn("warning message");
            RELOAD4J_DISABLED_LOGGER.info("info message");
            RELOAD4J_DISABLED_LOGGER.debug("debug message");

            assertEquals(Collections.emptyList(), testAppender.getEvents());
        }
    }

    @Nested
    @DisplayName("invalid usage")
    class InvalidUsage {

        @Test
        @DisplayName("unsupported context type")
        void testUnsupportedContextType() {
            assertSingleTestFailure(TestLoggerTest.UnsupportedContextType.class, ParameterResolutionException.class,
                    startsWith("No ParameterResolver registered for parameter ["
                            + TestLoggerTest.UnsupportedContextType.UnsupportedContext.class.getName()));
        }

        @Nested
        @DisplayName("multiple annotations")
        class MultipleAnnotations {

            @Test
            @DisplayName("@TestLogger and @TestLogger.ForClass")
            void testNameAndClass() {
                assertSingleTestFailure(TestLoggerTest.MultipleAnnotations.WithNameAndClass.class, ParameterResolutionException.class,
                        endsWith(": Exactly one of @TestLogger, @TestLogger.ForClass and @TestLogger.Root required"));
            }

            @Test
            @DisplayName("@TestLogger and @TestLogger.Root")
            void testNameAndRoot() {
                assertSingleTestFailure(TestLoggerTest.MultipleAnnotations.WithNameAndRoot.class, ParameterResolutionException.class,
                        endsWith(": Exactly one of @TestLogger, @TestLogger.ForClass and @TestLogger.Root required"));
            }

            @Test
            @DisplayName("@TestLogger.ForClass and @TestLogger.Root")
            void testClassAndRoot() {
                assertSingleTestFailure(TestLoggerTest.MultipleAnnotations.WithClassAndRoot.class, ParameterResolutionException.class,
                        endsWith(": Exactly one of @TestLogger, @TestLogger.ForClass and @TestLogger.Root required"));
            }

            @Test
            @DisplayName("@TestLogger and @TestLogger.ForClass and @TestLogger.Root")
            void testNameAndClassAndRoot() {
                assertSingleTestFailure(TestLoggerTest.MultipleAnnotations.WithNameAndClassAndRoot.class, ParameterResolutionException.class,
                        endsWith(": Exactly one of @TestLogger, @TestLogger.ForClass and @TestLogger.Root required"));
            }
        }

        private void assertSingleTestFailure(Class<?> testClass, Class<? extends Throwable> errorType, Matcher<String> messageMatcher) {

            EngineExecutionResults results = runTests(testClass);

            assertEquals(0, results.testEvents().succeeded().count());
            assertEquals(1, results.testEvents().failed().count());

            Throwable throwable = getSingleTestFailure(results);
            assertEquals(errorType, throwable.getClass());
            assertThat(throwable.getMessage(), messageMatcher);
        }

        private EngineExecutionResults runTests(Class<?> testClass) {
            return EngineTestKit.engine(new JupiterTestEngine())
                    .selectors(DiscoverySelectors.selectClass(testClass))
                    .execute();
        }

        private Throwable getSingleTestFailure(EngineExecutionResults results) {
            TestExecutionResult result = assertIsPresent(results.testEvents().failed().stream()
                    .map(event -> event.getPayload(TestExecutionResult.class))
                    .findAny()
                    .orElse(null));

            Throwable throwable = assertIsPresent(result.getThrowable());
            return throwable;
        }
    }

    abstract static class JdkLoggerTests {

        private JdkTestHandler testHandler;
        private JdkTestHandler rootTestHandler;

        abstract JdkLoggerContext logger();

        abstract JdkLoggerContext rootLogger();

        @BeforeEach
        void init() {
            validateLoggers();

            testHandler = testHandler(logger());
            testHandler.clearRecords();

            rootTestHandler = testHandler(rootLogger());
            rootTestHandler.clearRecords();
        }

        private static JdkTestHandler testHandler(JdkLoggerContext logger) {
            return assertIsPresent(logger.streamHandlers()
                    .filter(JdkTestHandler.class::isInstance)
                    .map(JdkTestHandler.class::cast)
                    .findAny());
        }

        @Test
        @DisplayName("default logging")
        void testDefaultLogging() {
            JDK_LOGGER.log(java.util.logging.Level.WARNING, "warning message");
            JDK_LOGGER.log(java.util.logging.Level.INFO, "info message");
            JDK_LOGGER.log(java.util.logging.Level.FINE, "fine message");

            List<java.util.logging.LogRecord> records = testHandler.getRecords();
            assertEquals(2, records.size());
            assertEquals(java.util.logging.Level.WARNING, records.get(0).getLevel());
            assertEquals("warning message", records.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, records.get(1).getLevel());
            assertEquals("info message", records.get(1).getMessage());

            List<java.util.logging.LogRecord> rootRecords = rootTestHandler.getRecords();
            assertEquals(2, rootRecords.size());
            assertEquals(java.util.logging.Level.WARNING, rootRecords.get(0).getLevel());
            assertEquals("warning message", rootRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, records.get(1).getLevel());
            assertEquals("info message", records.get(1).getMessage());
        }

        @Test
        @DisplayName("level changed")
        void testLevelChanged() {
            logger().setLevel(java.util.logging.Level.ALL);

            JDK_LOGGER.log(java.util.logging.Level.WARNING, "warning message");
            JDK_LOGGER.log(java.util.logging.Level.INFO, "info message");
            JDK_LOGGER.log(java.util.logging.Level.FINE, "fine message");

            List<java.util.logging.LogRecord> records = testHandler.getRecords();
            assertEquals(3, records.size());
            assertEquals(java.util.logging.Level.WARNING, records.get(0).getLevel());
            assertEquals("warning message", records.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, records.get(1).getLevel());
            assertEquals("info message", records.get(1).getMessage());
            assertEquals(java.util.logging.Level.FINE, records.get(2).getLevel());
            assertEquals("fine message", records.get(2).getMessage());

            List<java.util.logging.LogRecord> rootRecords = rootTestHandler.getRecords();
            assertEquals(3, rootRecords.size());
            assertEquals(java.util.logging.Level.WARNING, rootRecords.get(0).getLevel());
            assertEquals("warning message", rootRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, rootRecords.get(1).getLevel());
            assertEquals("info message", rootRecords.get(1).getMessage());
            assertEquals(java.util.logging.Level.FINE, rootRecords.get(2).getLevel());
            assertEquals("fine message", rootRecords.get(2).getMessage());
        }

        @Test
        @DisplayName("parent handlers disabled")
        void testParentHandlersDisabled() {
            logger().useParentHandlers(false);

            JDK_LOGGER.log(java.util.logging.Level.WARNING, "warning message");
            JDK_LOGGER.log(java.util.logging.Level.INFO, "info message");
            JDK_LOGGER.log(java.util.logging.Level.FINE, "fine message");

            List<java.util.logging.LogRecord> records = testHandler.getRecords();
            assertEquals(2, records.size());
            assertEquals(java.util.logging.Level.WARNING, records.get(0).getLevel());
            assertEquals("warning message", records.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, records.get(1).getLevel());
            assertEquals("info message", records.get(1).getMessage());

            List<java.util.logging.LogRecord> rootRecords = rootTestHandler.getRecords();
            assertEquals(0, rootRecords.size());
        }

        @Test
        @DisplayName("handler added")
        void testHandlerAdded() {
            java.util.logging.Handler handler = mock(java.util.logging.Handler.class);
            logger().addHandler(handler);

            JDK_LOGGER.log(java.util.logging.Level.WARNING, "warning message");
            JDK_LOGGER.log(java.util.logging.Level.INFO, "info message");
            JDK_LOGGER.log(java.util.logging.Level.FINE, "fine message");

            List<java.util.logging.LogRecord> records = testHandler.getRecords();
            assertEquals(2, records.size());
            assertEquals(java.util.logging.Level.WARNING, records.get(0).getLevel());
            assertEquals("warning message", records.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, records.get(1).getLevel());
            assertEquals("info message", records.get(1).getMessage());

            List<java.util.logging.LogRecord> rootRecords = rootTestHandler.getRecords();
            assertEquals(2, rootRecords.size());
            assertEquals(java.util.logging.Level.WARNING, rootRecords.get(0).getLevel());
            assertEquals("warning message", rootRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, rootRecords.get(1).getLevel());
            assertEquals("info message", rootRecords.get(1).getMessage());

            ArgumentCaptor<java.util.logging.LogRecord> logRecordCaptor = ArgumentCaptor.forClass(java.util.logging.LogRecord.class);

            verify(handler, times(2)).publish(logRecordCaptor.capture());

            List<java.util.logging.LogRecord> capturedRecords = logRecordCaptor.getAllValues();
            assertEquals(2, capturedRecords.size());
            assertEquals(java.util.logging.Level.WARNING, capturedRecords.get(0).getLevel());
            assertEquals("warning message", capturedRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, capturedRecords.get(1).getLevel());
            assertEquals("info message", capturedRecords.get(1).getMessage());
        }

        @Test
        @DisplayName("root handler added")
        void testRootHandlerAdded() {
            java.util.logging.Handler handler = mock(java.util.logging.Handler.class);
            rootLogger().addHandler(handler);

            JDK_LOGGER.log(java.util.logging.Level.WARNING, "warning message");
            JDK_LOGGER.log(java.util.logging.Level.INFO, "info message");
            JDK_LOGGER.log(java.util.logging.Level.FINE, "fine message");

            List<java.util.logging.LogRecord> records = testHandler.getRecords();
            assertEquals(2, records.size());
            assertEquals(java.util.logging.Level.WARNING, records.get(0).getLevel());
            assertEquals("warning message", records.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, records.get(1).getLevel());
            assertEquals("info message", records.get(1).getMessage());

            List<java.util.logging.LogRecord> rootRecords = rootTestHandler.getRecords();
            assertEquals(2, rootRecords.size());
            assertEquals(java.util.logging.Level.WARNING, rootRecords.get(0).getLevel());
            assertEquals("warning message", rootRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, rootRecords.get(1).getLevel());
            assertEquals("info message", rootRecords.get(1).getMessage());

            ArgumentCaptor<java.util.logging.LogRecord> logRecordCaptor = ArgumentCaptor.forClass(java.util.logging.LogRecord.class);

            verify(handler, times(2)).publish(logRecordCaptor.capture());

            List<java.util.logging.LogRecord> capturedRecords = logRecordCaptor.getAllValues();
            assertEquals(2, capturedRecords.size());
            assertEquals(java.util.logging.Level.WARNING, capturedRecords.get(0).getLevel());
            assertEquals("warning message", capturedRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, capturedRecords.get(1).getLevel());
            assertEquals("info message", capturedRecords.get(1).getMessage());
        }

        @Test
        @DisplayName("handler replaced")
        void testHandlerReplaced() {
            java.util.logging.Handler handler = mock(java.util.logging.Handler.class);
            logger().setHandler(handler);

            JDK_LOGGER.log(java.util.logging.Level.WARNING, "warning message");
            JDK_LOGGER.log(java.util.logging.Level.INFO, "info message");
            JDK_LOGGER.log(java.util.logging.Level.FINE, "fine message");

            List<java.util.logging.LogRecord> records = testHandler.getRecords();
            assertEquals(0, records.size());

            List<java.util.logging.LogRecord> rootRecords = rootTestHandler.getRecords();
            assertEquals(2, rootRecords.size());
            assertEquals(java.util.logging.Level.WARNING, rootRecords.get(0).getLevel());
            assertEquals("warning message", rootRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, rootRecords.get(1).getLevel());
            assertEquals("info message", rootRecords.get(1).getMessage());

            ArgumentCaptor<java.util.logging.LogRecord> logRecordCaptor = ArgumentCaptor.forClass(java.util.logging.LogRecord.class);

            verify(handler, times(2)).publish(logRecordCaptor.capture());

            List<java.util.logging.LogRecord> capturedRecords = logRecordCaptor.getAllValues();
            assertEquals(2, capturedRecords.size());
            assertEquals(java.util.logging.Level.WARNING, capturedRecords.get(0).getLevel());
            assertEquals("warning message", capturedRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, capturedRecords.get(1).getLevel());
            assertEquals("info message", capturedRecords.get(1).getMessage());
        }

        @Test
        @DisplayName("root handler replaced")
        void testRootHandlerReplaced() {
            java.util.logging.Handler handler = mock(java.util.logging.Handler.class);
            rootLogger().setHandler(handler);

            JDK_LOGGER.log(java.util.logging.Level.WARNING, "warning message");
            JDK_LOGGER.log(java.util.logging.Level.INFO, "info message");
            JDK_LOGGER.log(java.util.logging.Level.FINE, "fine message");

            List<java.util.logging.LogRecord> records = testHandler.getRecords();
            assertEquals(2, records.size());
            assertEquals(java.util.logging.Level.WARNING, records.get(0).getLevel());
            assertEquals("warning message", records.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, records.get(1).getLevel());
            assertEquals("info message", records.get(1).getMessage());

            List<java.util.logging.LogRecord> rootRecords = rootTestHandler.getRecords();
            assertEquals(0, rootRecords.size());

            ArgumentCaptor<java.util.logging.LogRecord> logRecordCaptor = ArgumentCaptor.forClass(java.util.logging.LogRecord.class);

            verify(handler, times(2)).publish(logRecordCaptor.capture());

            List<java.util.logging.LogRecord> capturedRecords = logRecordCaptor.getAllValues();
            assertEquals(2, capturedRecords.size());
            assertEquals(java.util.logging.Level.WARNING, capturedRecords.get(0).getLevel());
            assertEquals("warning message", capturedRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, capturedRecords.get(1).getLevel());
            assertEquals("info message", capturedRecords.get(1).getMessage());
        }

        @Test
        @DisplayName("parent logger configured")
        void testParentConfigured(@TestLogger("com.github.robtimus.junit.support.extension.testlogger") JdkLoggerContext parentLogger) {
            java.util.logging.Handler handler = mock(java.util.logging.Handler.class);
            parentLogger.setLevel(java.util.logging.Level.ALL)
                    .setHandler(handler)
                    .useParentHandlers(true);

            JDK_LOGGER.log(java.util.logging.Level.WARNING, "warning message");
            JDK_LOGGER.log(java.util.logging.Level.INFO, "info message");
            JDK_LOGGER.log(java.util.logging.Level.FINE, "fine message");

            List<java.util.logging.LogRecord> records = testHandler.getRecords();
            assertEquals(2, records.size());
            assertEquals(java.util.logging.Level.WARNING, records.get(0).getLevel());
            assertEquals("warning message", records.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, records.get(1).getLevel());
            assertEquals("info message", records.get(1).getMessage());

            List<java.util.logging.LogRecord> rootRecords = rootTestHandler.getRecords();
            assertEquals(2, rootRecords.size());
            assertEquals(java.util.logging.Level.WARNING, rootRecords.get(0).getLevel());
            assertEquals("warning message", rootRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, rootRecords.get(1).getLevel());
            assertEquals("info message", rootRecords.get(1).getMessage());

            ArgumentCaptor<java.util.logging.LogRecord> logRecordCaptor = ArgumentCaptor.forClass(java.util.logging.LogRecord.class);

            verify(handler, times(2)).publish(logRecordCaptor.capture());

            List<java.util.logging.LogRecord> capturedRecords = logRecordCaptor.getAllValues();
            assertEquals(2, capturedRecords.size());
            assertEquals(java.util.logging.Level.WARNING, capturedRecords.get(0).getLevel());
            assertEquals("warning message", capturedRecords.get(0).getMessage());
            assertEquals(java.util.logging.Level.INFO, capturedRecords.get(1).getLevel());
            assertEquals("info message", capturedRecords.get(1).getMessage());
        }
    }

    abstract static class Log4jLoggerTests {

        private Log4jTestAppender testAppender;
        private Log4jTestAppender rootTestAppender;

        abstract Log4jLoggerContext logger();

        abstract Log4jLoggerContext rootLogger();

        @BeforeEach
        void init() {
            validateLoggers();

            testAppender = testAppender(logger());
            testAppender.clearEvents();

            rootTestAppender = testAppender(rootLogger());
            rootTestAppender.clearEvents();
        }

        private static Log4jTestAppender testAppender(Log4jLoggerContext logger) {
            return assertIsPresent(logger.streamAppenders()
                    .filter(Log4jTestAppender.class::isInstance)
                    .map(Log4jTestAppender.class::cast)
                    .findAny());
        }

        @Test
        @DisplayName("default logging")
        void testDefaultLogging() {
            LOG4J_LOGGER.warn("warning message");
            LOG4J_LOGGER.info("info message");
            LOG4J_LOGGER.debug("debug message");

            List<org.apache.logging.log4j.core.LogEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage().getFormattedMessage());

            List<org.apache.logging.log4j.core.LogEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage().getFormattedMessage());
        }

        @Test
        @DisplayName("level changed")
        void testLevelChanged() {
            logger().setLevel(org.apache.logging.log4j.Level.ALL);

            LOG4J_LOGGER.warn("warning message");
            LOG4J_LOGGER.info("info message");
            LOG4J_LOGGER.debug("debug message");

            List<org.apache.logging.log4j.core.LogEvent> events = testAppender.getEvents();
            assertEquals(3, events.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.DEBUG, events.get(2).getLevel());
            assertEquals("debug message", events.get(2).getMessage().getFormattedMessage());

            List<org.apache.logging.log4j.core.LogEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(3, rootEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.DEBUG, rootEvents.get(2).getLevel());
            assertEquals("debug message", rootEvents.get(2).getMessage().getFormattedMessage());
        }

        @Test
        @DisplayName("parent appenders disabled")
        void testParentAppendersDisabled() {
            logger().useParentAppenders(false);

            LOG4J_LOGGER.warn("warning message");
            LOG4J_LOGGER.info("info message");
            LOG4J_LOGGER.debug("debug message");

            List<org.apache.logging.log4j.core.LogEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage().getFormattedMessage());

            List<org.apache.logging.log4j.core.LogEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(0, rootEvents.size());
        }

        @Test
        @DisplayName("appender added")
        void testAppenderAdded() {
            Log4jNullAppender appender = spy(Log4jNullAppender.create("MockAppender"));
            logger().addAppender(appender);

            LOG4J_LOGGER.warn("warning message");
            LOG4J_LOGGER.info("info message");
            LOG4J_LOGGER.debug("debug message");

            List<org.apache.logging.log4j.core.LogEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage().getFormattedMessage());

            List<org.apache.logging.log4j.core.LogEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage().getFormattedMessage());

            ArgumentCaptor<org.apache.logging.log4j.core.LogEvent> logEventCaptor = ArgumentCaptor
                    .forClass(org.apache.logging.log4j.core.LogEvent.class);

            verify(appender, times(2)).ignore(logEventCaptor.capture());

            List<org.apache.logging.log4j.core.LogEvent> capturedEvents = logEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage().getFormattedMessage());
        }

        @Test
        @DisplayName("root appender added")
        void testRootAppenderAdded() {
            Log4jNullAppender appender = spy(Log4jNullAppender.create("MockAppender"));
            rootLogger().addAppender(appender);

            LOG4J_LOGGER.warn("warning message");
            LOG4J_LOGGER.info("info message");
            LOG4J_LOGGER.debug("debug message");

            List<org.apache.logging.log4j.core.LogEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage().getFormattedMessage());

            List<org.apache.logging.log4j.core.LogEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage().getFormattedMessage());

            ArgumentCaptor<org.apache.logging.log4j.core.LogEvent> logEventCaptor = ArgumentCaptor
                    .forClass(org.apache.logging.log4j.core.LogEvent.class);

            verify(appender, times(2)).ignore(logEventCaptor.capture());

            List<org.apache.logging.log4j.core.LogEvent> capturedEvents = logEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage().getFormattedMessage());
        }

        @Test
        @DisplayName("appender replaced")
        void testAppenderReplaced() {
            Log4jNullAppender appender = spy(Log4jNullAppender.create("MockAppender"));
            logger().setAppender(appender);

            LOG4J_LOGGER.warn("warning message");
            LOG4J_LOGGER.info("info message");
            LOG4J_LOGGER.debug("debug message");

            List<org.apache.logging.log4j.core.LogEvent> events = testAppender.getEvents();
            assertEquals(0, events.size());

            List<org.apache.logging.log4j.core.LogEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage().getFormattedMessage());

            ArgumentCaptor<org.apache.logging.log4j.core.LogEvent> logEventCaptor = ArgumentCaptor
                    .forClass(org.apache.logging.log4j.core.LogEvent.class);

            verify(appender, times(2)).ignore(logEventCaptor.capture());

            List<org.apache.logging.log4j.core.LogEvent> capturedEvents = logEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage().getFormattedMessage());
        }

        @Test
        @DisplayName("root appender replaced")
        void testRootAppenderReplaced() {
            Log4jNullAppender appender = spy(Log4jNullAppender.create("MockAppender"));
            rootLogger().setAppender(appender);

            LOG4J_LOGGER.warn("warning message");
            LOG4J_LOGGER.info("info message");
            LOG4J_LOGGER.debug("debug message");

            List<org.apache.logging.log4j.core.LogEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage().getFormattedMessage());

            List<org.apache.logging.log4j.core.LogEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(0, rootEvents.size());

            ArgumentCaptor<org.apache.logging.log4j.core.LogEvent> logEventCaptor = ArgumentCaptor
                    .forClass(org.apache.logging.log4j.core.LogEvent.class);

            verify(appender, times(2)).ignore(logEventCaptor.capture());

            List<org.apache.logging.log4j.core.LogEvent> capturedEvents = logEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage().getFormattedMessage());
        }

        @Test
        @DisplayName("parent logger configured")
        void testParentConfigured(@TestLogger("com.github.robtimus.junit.support.extension.testlogger") Log4jLoggerContext parentLogger) {
            Log4jNullAppender appender = spy(Log4jNullAppender.create("MockAppender"));
            parentLogger.setLevel(org.apache.logging.log4j.Level.INFO)
                    .addAppender(appender)
                    .useParentAppenders(true);

            LOG4J_LOGGER.warn("warning message");
            LOG4J_LOGGER.info("info message");
            LOG4J_LOGGER.debug("debug message");

            List<org.apache.logging.log4j.core.LogEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage().getFormattedMessage());

            List<org.apache.logging.log4j.core.LogEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage().getFormattedMessage());

            ArgumentCaptor<org.apache.logging.log4j.core.LogEvent> logEventCaptor = ArgumentCaptor
                    .forClass(org.apache.logging.log4j.core.LogEvent.class);

            verify(appender, times(2)).ignore(logEventCaptor.capture());

            List<org.apache.logging.log4j.core.LogEvent> capturedEvents = logEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(org.apache.logging.log4j.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage().getFormattedMessage());
            assertEquals(org.apache.logging.log4j.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage().getFormattedMessage());
        }
    }

    abstract static class LogbackLoggerTests {

        private LogbackTestAppender testAppender;
        private LogbackTestAppender rootTestAppender;

        abstract LogbackLoggerContext logger();

        abstract LogbackLoggerContext rootLogger();

        @BeforeEach
        void init() {
            validateLoggers();

            testAppender = testAppender(logger());
            testAppender.clearEvents();

            rootTestAppender = testAppender(rootLogger());
            rootTestAppender.clearEvents();
        }

        private static LogbackTestAppender testAppender(LogbackLoggerContext logger) {
            return assertIsPresent(logger.streamAppenders()
                    .filter(LogbackTestAppender.class::isInstance)
                    .map(LogbackTestAppender.class::cast)
                    .findAny());
        }

        @Test
        @DisplayName("default logging")
        void testDefaultLogging() {
            SLF4J_LOGGER.warn("warning message");
            SLF4J_LOGGER.info("info message");
            SLF4J_LOGGER.debug("debug message");

            List<ch.qos.logback.classic.spi.ILoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<ch.qos.logback.classic.spi.ILoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());
        }

        @Test
        @DisplayName("level changed")
        void testLevelChanged() {
            logger().setLevel(ch.qos.logback.classic.Level.ALL);

            SLF4J_LOGGER.warn("warning message");
            SLF4J_LOGGER.info("info message");
            SLF4J_LOGGER.debug("debug message");

            List<ch.qos.logback.classic.spi.ILoggingEvent> events = testAppender.getEvents();
            assertEquals(3, events.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());
            assertEquals(ch.qos.logback.classic.Level.DEBUG, events.get(2).getLevel());
            assertEquals("debug message", events.get(2).getMessage());

            List<ch.qos.logback.classic.spi.ILoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(3, rootEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage());
            assertEquals(ch.qos.logback.classic.Level.DEBUG, rootEvents.get(2).getLevel());
            assertEquals("debug message", rootEvents.get(2).getMessage());
        }

        @Test
        @DisplayName("parent appenders disabled")
        void testParentAppendersDisabled() {
            logger().useParentAppenders(false);

            SLF4J_LOGGER.warn("warning message");
            SLF4J_LOGGER.info("info message");
            SLF4J_LOGGER.debug("debug message");

            List<ch.qos.logback.classic.spi.ILoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<ch.qos.logback.classic.spi.ILoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(0, rootEvents.size());
        }

        @Test
        @DisplayName("appender added")
        void testAppenderAdded() {
            @SuppressWarnings("unchecked")
            ch.qos.logback.core.Appender<ch.qos.logback.classic.spi.ILoggingEvent> appender = mock(ch.qos.logback.core.Appender.class);
            logger().addAppender(appender);

            SLF4J_LOGGER.warn("warning message");
            SLF4J_LOGGER.info("info message");
            SLF4J_LOGGER.debug("debug message");

            List<ch.qos.logback.classic.spi.ILoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<ch.qos.logback.classic.spi.ILoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage());

            ArgumentCaptor<ch.qos.logback.classic.spi.ILoggingEvent> loggingEventCaptor = ArgumentCaptor
                    .forClass(ch.qos.logback.classic.spi.ILoggingEvent.class);

            verify(appender, times(2)).doAppend(loggingEventCaptor.capture());

            List<ch.qos.logback.classic.spi.ILoggingEvent> capturedEvents = loggingEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage());
        }

        @Test
        @DisplayName("root appender added")
        void testRootAppenderAdded() {
            @SuppressWarnings("unchecked")
            ch.qos.logback.core.Appender<ch.qos.logback.classic.spi.ILoggingEvent> appender = mock(ch.qos.logback.core.Appender.class);
            rootLogger().addAppender(appender);

            SLF4J_LOGGER.warn("warning message");
            SLF4J_LOGGER.info("info message");
            SLF4J_LOGGER.debug("debug message");

            List<ch.qos.logback.classic.spi.ILoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<ch.qos.logback.classic.spi.ILoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage());

            ArgumentCaptor<ch.qos.logback.classic.spi.ILoggingEvent> loggingEventCaptor = ArgumentCaptor
                    .forClass(ch.qos.logback.classic.spi.ILoggingEvent.class);

            verify(appender, times(2)).doAppend(loggingEventCaptor.capture());

            List<ch.qos.logback.classic.spi.ILoggingEvent> capturedEvents = loggingEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage());
        }

        @Test
        @DisplayName("appender replaced")
        void testAppenderReplaced() {
            @SuppressWarnings("unchecked")
            ch.qos.logback.core.Appender<ch.qos.logback.classic.spi.ILoggingEvent> appender = mock(ch.qos.logback.core.Appender.class);
            logger().setAppender(appender);

            SLF4J_LOGGER.warn("warning message");
            SLF4J_LOGGER.info("info message");
            SLF4J_LOGGER.debug("debug message");

            List<ch.qos.logback.classic.spi.ILoggingEvent> events = testAppender.getEvents();
            assertEquals(0, events.size());

            List<ch.qos.logback.classic.spi.ILoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage());

            ArgumentCaptor<ch.qos.logback.classic.spi.ILoggingEvent> loggingEventCaptor = ArgumentCaptor
                    .forClass(ch.qos.logback.classic.spi.ILoggingEvent.class);

            verify(appender, times(2)).doAppend(loggingEventCaptor.capture());

            List<ch.qos.logback.classic.spi.ILoggingEvent> capturedEvents = loggingEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage());
        }

        @Test
        @DisplayName("root appender replaced")
        void testRootAppenderReplaced() {
            @SuppressWarnings("unchecked")
            ch.qos.logback.core.Appender<ch.qos.logback.classic.spi.ILoggingEvent> appender = mock(ch.qos.logback.core.Appender.class);
            rootLogger().setAppender(appender);

            SLF4J_LOGGER.warn("warning message");
            SLF4J_LOGGER.info("info message");
            SLF4J_LOGGER.debug("debug message");

            List<ch.qos.logback.classic.spi.ILoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<ch.qos.logback.classic.spi.ILoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(0, rootEvents.size());

            ArgumentCaptor<ch.qos.logback.classic.spi.ILoggingEvent> loggingEventCaptor = ArgumentCaptor
                    .forClass(ch.qos.logback.classic.spi.ILoggingEvent.class);

            verify(appender, times(2)).doAppend(loggingEventCaptor.capture());

            List<ch.qos.logback.classic.spi.ILoggingEvent> capturedEvents = loggingEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage());
        }

        @Test
        @DisplayName("parent logger configured")
        void testParentConfigured(@TestLogger("com.github.robtimus.junit.support.extension.testlogger") LogbackLoggerContext parentLogger) {
            @SuppressWarnings("unchecked")
            ch.qos.logback.core.Appender<ch.qos.logback.classic.spi.ILoggingEvent> appender = mock(ch.qos.logback.core.Appender.class);
            parentLogger.setLevel(ch.qos.logback.classic.Level.INFO)
                    .addAppender(appender)
                    .useParentAppenders(true);

            SLF4J_LOGGER.warn("warning message");
            SLF4J_LOGGER.info("info message");
            SLF4J_LOGGER.debug("debug message");

            List<ch.qos.logback.classic.spi.ILoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<ch.qos.logback.classic.spi.ILoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage());

            ArgumentCaptor<ch.qos.logback.classic.spi.ILoggingEvent> loggingEventCaptor = ArgumentCaptor
                    .forClass(ch.qos.logback.classic.spi.ILoggingEvent.class);

            verify(appender, times(2)).doAppend(loggingEventCaptor.capture());

            List<ch.qos.logback.classic.spi.ILoggingEvent> capturedEvents = loggingEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(ch.qos.logback.classic.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage());
            assertEquals(ch.qos.logback.classic.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage());
        }
    }

    abstract static class Reload4jLoggerTests {

        private Reload4jTestAppender testAppender;
        private Reload4jTestAppender rootTestAppender;

        abstract Reload4jLoggerContext logger();

        abstract Reload4jLoggerContext rootLogger();

        @BeforeEach
        void init() {
            validateLoggers();

            testAppender = testAppender(logger());
            testAppender.clearEvents();

            rootTestAppender = testAppender(rootLogger());
            rootTestAppender.clearEvents();
        }

        private static Reload4jTestAppender testAppender(Reload4jLoggerContext logger) {
            return assertIsPresent(logger.streamAppenders()
                    .filter(Reload4jTestAppender.class::isInstance)
                    .map(Reload4jTestAppender.class::cast)
                    .findAny());
        }

        @Test
        @DisplayName("default logging")
        void testDefaultLogging() {
            RELOAD4J_LOGGER.warn("warning message");
            RELOAD4J_LOGGER.info("info message");
            RELOAD4J_LOGGER.debug("debug message");

            List<org.apache.log4j.spi.LoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<org.apache.log4j.spi.LoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());
        }

        @Test
        @DisplayName("level changed")
        void testLevelChanged() {
            logger().setLevel(org.apache.log4j.Level.ALL);

            RELOAD4J_LOGGER.warn("warning message");
            RELOAD4J_LOGGER.info("info message");
            RELOAD4J_LOGGER.debug("debug message");

            List<org.apache.log4j.spi.LoggingEvent> events = testAppender.getEvents();
            assertEquals(3, events.size());
            assertEquals(org.apache.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());
            assertEquals(org.apache.log4j.Level.DEBUG, events.get(2).getLevel());
            assertEquals("debug message", events.get(2).getMessage());

            List<org.apache.log4j.spi.LoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(3, rootEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage());
            assertEquals(org.apache.log4j.Level.DEBUG, rootEvents.get(2).getLevel());
            assertEquals("debug message", rootEvents.get(2).getMessage());
        }

        @Test
        @DisplayName("parent appenders disabled")
        void testParentAppendersDisabled() {
            logger().useParentAppenders(false);

            RELOAD4J_LOGGER.warn("warning message");
            RELOAD4J_LOGGER.info("info message");
            RELOAD4J_LOGGER.debug("debug message");

            List<org.apache.log4j.spi.LoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<org.apache.log4j.spi.LoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(0, rootEvents.size());
        }

        @Test
        @DisplayName("appender added")
        void testAppenderAdded() {
            org.apache.log4j.Appender appender = mock(org.apache.log4j.Appender.class);
            logger().addAppender(appender);

            RELOAD4J_LOGGER.warn("warning message");
            RELOAD4J_LOGGER.info("info message");
            RELOAD4J_LOGGER.debug("debug message");

            List<org.apache.log4j.spi.LoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<org.apache.log4j.spi.LoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage());

            ArgumentCaptor<org.apache.log4j.spi.LoggingEvent> loggingEventCaptor = ArgumentCaptor.forClass(org.apache.log4j.spi.LoggingEvent.class);

            verify(appender, times(2)).doAppend(loggingEventCaptor.capture());

            List<org.apache.log4j.spi.LoggingEvent> capturedEvents = loggingEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage());
        }

        @Test
        @DisplayName("root appender added")
        void testRootAppenderAdded() {
            org.apache.log4j.Appender appender = mock(org.apache.log4j.Appender.class);
            rootLogger().addAppender(appender);

            RELOAD4J_LOGGER.warn("warning message");
            RELOAD4J_LOGGER.info("info message");
            RELOAD4J_LOGGER.debug("debug message");

            List<org.apache.log4j.spi.LoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<org.apache.log4j.spi.LoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage());

            ArgumentCaptor<org.apache.log4j.spi.LoggingEvent> loggingEventCaptor = ArgumentCaptor.forClass(org.apache.log4j.spi.LoggingEvent.class);

            verify(appender, times(2)).doAppend(loggingEventCaptor.capture());

            List<org.apache.log4j.spi.LoggingEvent> capturedEvents = loggingEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage());
        }

        @Test
        @DisplayName("appender replaced")
        void testAppenderReplaced() {
            org.apache.log4j.Appender appender = mock(org.apache.log4j.Appender.class);
            logger().setAppender(appender);

            RELOAD4J_LOGGER.warn("warning message");
            RELOAD4J_LOGGER.info("info message");
            RELOAD4J_LOGGER.debug("debug message");

            List<org.apache.log4j.spi.LoggingEvent> events = testAppender.getEvents();
            assertEquals(0, events.size());

            List<org.apache.log4j.spi.LoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage());

            ArgumentCaptor<org.apache.log4j.spi.LoggingEvent> loggingEventCaptor = ArgumentCaptor.forClass(org.apache.log4j.spi.LoggingEvent.class);

            verify(appender, times(2)).doAppend(loggingEventCaptor.capture());

            List<org.apache.log4j.spi.LoggingEvent> capturedEvents = loggingEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage());
        }

        @Test
        @DisplayName("root appender replaced")
        void testRootAppenderReplaced() {
            org.apache.log4j.Appender appender = mock(org.apache.log4j.Appender.class);
            rootLogger().setAppender(appender);

            RELOAD4J_LOGGER.warn("warning message");
            RELOAD4J_LOGGER.info("info message");
            RELOAD4J_LOGGER.debug("debug message");

            List<org.apache.log4j.spi.LoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<org.apache.log4j.spi.LoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(0, rootEvents.size());

            ArgumentCaptor<org.apache.log4j.spi.LoggingEvent> loggingEventCaptor = ArgumentCaptor.forClass(org.apache.log4j.spi.LoggingEvent.class);

            verify(appender, times(2)).doAppend(loggingEventCaptor.capture());

            List<org.apache.log4j.spi.LoggingEvent> capturedEvents = loggingEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage());
        }

        @Test
        @DisplayName("parent logger configured")
        void testParentConfigured(@TestLogger("com.github.robtimus.junit.support.extension.testlogger") Reload4jLoggerContext parentLogger) {
            org.apache.log4j.Appender appender = mock(org.apache.log4j.Appender.class);
            parentLogger.setLevel(org.apache.log4j.Level.INFO)
                    .addAppender(appender)
                    .useParentAppenders(true);

            RELOAD4J_LOGGER.warn("warning message");
            RELOAD4J_LOGGER.info("info message");
            RELOAD4J_LOGGER.debug("debug message");

            List<org.apache.log4j.spi.LoggingEvent> events = testAppender.getEvents();
            assertEquals(2, events.size());
            assertEquals(org.apache.log4j.Level.WARN, events.get(0).getLevel());
            assertEquals("warning message", events.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, events.get(1).getLevel());
            assertEquals("info message", events.get(1).getMessage());

            List<org.apache.log4j.spi.LoggingEvent> rootEvents = rootTestAppender.getEvents();
            assertEquals(2, rootEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, rootEvents.get(0).getLevel());
            assertEquals("warning message", rootEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, rootEvents.get(1).getLevel());
            assertEquals("info message", rootEvents.get(1).getMessage());

            ArgumentCaptor<org.apache.log4j.spi.LoggingEvent> loggingEventCaptor = ArgumentCaptor.forClass(org.apache.log4j.spi.LoggingEvent.class);

            verify(appender, times(2)).doAppend(loggingEventCaptor.capture());

            List<org.apache.log4j.spi.LoggingEvent> capturedEvents = loggingEventCaptor.getAllValues();
            assertEquals(2, capturedEvents.size());
            assertEquals(org.apache.log4j.Level.WARN, capturedEvents.get(0).getLevel());
            assertEquals("warning message", capturedEvents.get(0).getMessage());
            assertEquals(org.apache.log4j.Level.INFO, capturedEvents.get(1).getLevel());
            assertEquals("info message", capturedEvents.get(1).getMessage());
        }
    }

    static final class UnsupportedContextType {

        @Test
        void testUnsupportedType(@TestLogger("test") UnsupportedContext context) {
            assertNotNull(context);
        }

        static final class UnsupportedContext extends LoggerContext {

            @Override
            void disable() {
                // does nothing
            }

            @Override
            void saveSettings() {
                // does nothing
            }

            @Override
            public void restore() {
                // does nothing
            }
        }
    }

    static final class MultipleAnnotations {

        static final class WithNameAndClass {

            @Test
            void testMultipleAnnotations(@TestLogger("test") @TestLogger.ForClass(TestLogger.class) JdkLoggerContext context) {
                assertNotNull(context);
            }
        }

        static final class WithNameAndRoot {

            @Test
            void testMultipleAnnotations(@TestLogger("test") @TestLogger.Root JdkLoggerContext context) {
                assertNotNull(context);
            }
        }

        static final class WithClassAndRoot {

            @Test
            void testMultipleAnnotations(@TestLogger.ForClass(TestLogger.class) @TestLogger.Root JdkLoggerContext context) {
                assertNotNull(context);
            }
        }

        static final class WithNameAndClassAndRoot {

            @Test
            void testMultipleAnnotations(@TestLogger("test") @TestLogger.ForClass(TestLogger.class) @TestLogger.Root JdkLoggerContext context) {
                assertNotNull(context);
            }
        }
    }
}
