/*
 * LogbackLoggerContextTest.java
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

import static com.github.robtimus.junit.support.extension.util.LogbackUtils.LOGGER;
import static com.github.robtimus.junit.support.extension.util.LogbackUtils.ROOT_LOGGER;
import static com.github.robtimus.junit.support.extension.util.LogbackUtils.getAppenders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.github.robtimus.junit.support.extension.util.LogbackTestAppender;
import com.github.robtimus.junit.support.extension.util.LogbackUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

@SuppressWarnings("nls")
final class LogbackLoggerContextTest {

    private LogbackLoggerContextTest() {
    }

    @BeforeAll
    @AfterAll
    static void validateLoggers() {
        LogbackUtils.validateLoggers();
    }

    @Nested
    @DisplayName("forLogger(String)")
    class ForLoggerByName extends ForLogger {

        ForLoggerByName() {
            super(() -> LogbackLoggerContext.forLogger(TestLogger.class.getName()));
        }
    }

    @Nested
    @DisplayName("forLogger(Class)")
    class ForLoggerByClass extends ForLogger {

        ForLoggerByClass() {
            super(() -> LogbackLoggerContext.forLogger(TestLogger.class));
        }
    }

    abstract static class ForLogger {

        private final Supplier<LogbackLoggerContext> contextFactory;

        private LogbackLoggerContext context;

        private ForLogger(Supplier<LogbackLoggerContext> contextFactory) {
            this.contextFactory = contextFactory;
        }

        @BeforeEach
        void initContext() {
            context = contextFactory.get();
            context.saveSettings();
        }

        @AfterEach
        void restoreContext() {
            context.restore();
            validateLoggers();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = { "OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE" })
        @DisplayName("setLevel(Level)")
        void testSetLevel(String levelValue) {
            Level level = Level.toLevel(levelValue);
            context.setLevel(level);

            assertEquals(level, LOGGER.getLevel());
        }

        @Test
        @DisplayName("setLevel(null)")
        void testSetNullLevel() {
            assertThrows(NullPointerException.class, () -> context.setLevel(null));

            validateLoggers();
        }

        @Test
        @DisplayName("addAppender(Appender)")
        void testAddAppender() {
            Appender<ILoggingEvent> appender = new LogbackTestAppender();
            appender.setName("TestAppender");
            context.addAppender(appender);

            List<Appender<ILoggingEvent>> appenders = getAppenders(LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(LogbackTestAppender.class, appenders.get(0));
            assertEquals("A2", appenders.get(0).getName());
            assertSame(appender, appenders.get(1));
        }

        @Test
        @DisplayName("addAppender(null)")
        void testAddNullAppender() {
            assertThrows(NullPointerException.class, () -> context.addAppender(null));

            validateLoggers();
        }

        @Test
        @DisplayName("setAppender(Appender)")
        void testSetAppender() {
            Appender<ILoggingEvent> appender = new LogbackTestAppender();
            appender.setName("TestAppender");
            context.setAppender(appender);

            List<Appender<ILoggingEvent>> appenders = getAppenders(LOGGER);
            assertEquals(1, appenders.size());
            assertSame(appender, appenders.get(0));
        }

        @Test
        @DisplayName("setAppender(null)")
        void testSetNullAppender() {
            assertThrows(NullPointerException.class, () -> context.setAppender(null));

            validateLoggers();
        }

        @Test
        @DisplayName("removeAppender(Appender)")
        void testRemoveAppender() {
            // First add one, so we see a difference with removeAppenders()

            Appender<ILoggingEvent> appender = new LogbackTestAppender();
            appender.setName("TestAppender");
            context.addAppender(appender);

            List<Appender<ILoggingEvent>> appenders = getAppenders(LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(LogbackTestAppender.class, appenders.get(0));
            assertEquals("A2", appenders.get(0).getName());
            assertSame(appender, appenders.get(1));

            context.removeAppender(appenders.get(0));

            appenders = getAppenders(LOGGER);
            assertEquals(1, appenders.size());
            assertSame(appender, appenders.get(0));
        }

        @Test
        @DisplayName("removeAppender(null)")
        void testRemoveNullAppender() {
            assertThrows(NullPointerException.class, () -> context.removeAppender(null));

            validateLoggers();
        }

        @Test
        @DisplayName("removeAppenders()")
        void testRemoveAppenders() {
            context.removeAppenders();

            List<Appender<ILoggingEvent>> appenders = getAppenders(LOGGER);
            assertEquals(0, appenders.size());
        }

        @Test
        @DisplayName("removeAppenders(Predicate)")
        void testRemoveAppendersWithFilter() {
            // First add one, so we see a difference with removeAppenders()

            Appender<ILoggingEvent> appender = new LogbackTestAppender();
            appender.setName("TestAppender");
            context.addAppender(appender);

            List<Appender<ILoggingEvent>> appenders = getAppenders(LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(LogbackTestAppender.class, appenders.get(0));
            assertEquals("A2", appenders.get(0).getName());
            assertSame(appender, appenders.get(1));

            context.removeAppenders(a -> a.getName().startsWith("A"));

            appenders = getAppenders(LOGGER);
            assertEquals(1, appenders.size());
            assertSame(appender, appenders.get(0));
        }

        @Test
        @DisplayName("removeAppenders(null)")
        void testRemoveAppendersWithNullFilter() {
            assertThrows(NullPointerException.class, () -> context.removeAppenders(null));

            validateLoggers();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(booleans = { true, false })
        @DisplayName("useParentAppenders(boolean)")
        void testUseParentAppenders(boolean useParentAppenders) {
            context.useParentAppenders(useParentAppenders);

            assertEquals(useParentAppenders, LOGGER.isAdditive());
        }

        @Test
        @DisplayName("capture()")
        void testCapture() {
            LogCaptor<ILoggingEvent> logCaptor = context.capture();
            assertLoggedMessages(logCaptor);

            assertSame(logCaptor, context.capture());

            LOGGER.info("first log");
            assertLoggedMessages(logCaptor, "first log");

            context.removeAppenders();

            LOGGER.info("second log");
            assertLoggedMessages(logCaptor, "first log", "second log");

            context.removeAppenders(appender -> true);

            LOGGER.info("third log");
            assertLoggedMessages(logCaptor, "first log", "second log", "third log");

            @SuppressWarnings("unchecked")
            Appender<ILoggingEvent> appender = mock(Appender.class);
            context.setAppender(appender);

            LOGGER.info("fourth log");
            assertLoggedMessages(logCaptor, "first log", "second log", "third log", "fourth log");

            logCaptor.reset();
            assertLoggedMessages(logCaptor);

            LOGGER.info("fifth log");
            assertLoggedMessages(logCaptor, "fifth log");
        }

        private void assertLoggedMessages(LogCaptor<ILoggingEvent> logCaptor, String... messages) {
            List<ILoggingEvent> logged = logCaptor.logged();
            assertEquals(messages.length, logged.size());

            for (int i = 0; i < messages.length; i++) {
                assertEquals(messages[i], logged.get(i).getMessage());
            }
        }
    }

    @Nested
    @DisplayName("forRootLogger()")
    class ForRootLogger {

        private LogbackLoggerContext context;

        @BeforeEach
        void initContext() {
            context = LogbackLoggerContext.forRootLogger();
            context.saveSettings();
        }

        @AfterEach
        void restoreContext() {
            context.restore();
            validateLoggers();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = { "OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE" })
        @DisplayName("setLevel(Level)")
        void testSetLevel(String levelValue) {
            Level level = Level.toLevel(levelValue);
            context.setLevel(level);

            assertEquals(level, ROOT_LOGGER.getLevel());
        }

        @Test
        @DisplayName("setLevel(null)")
        void testSetNullLevel() {
            assertThrows(NullPointerException.class, () -> context.setLevel(null));

            validateLoggers();
        }

        @Test
        @DisplayName("addAppender(Appender)")
        void testAddAppender() {
            Appender<ILoggingEvent> appender = new LogbackTestAppender();
            appender.setName("TestAppender");
            context.addAppender(appender);

            List<Appender<ILoggingEvent>> appenders = getAppenders(ROOT_LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(LogbackTestAppender.class, appenders.get(0));
            assertEquals("A1", appenders.get(0).getName());
            assertSame(appender, appenders.get(1));
        }

        @Test
        @DisplayName("addAppender(null)")
        void testAddNullAppender() {
            assertThrows(NullPointerException.class, () -> context.addAppender(null));

            validateLoggers();
        }

        @Test
        @DisplayName("setAppender(Appender)")
        void testSetAppender() {
            Appender<ILoggingEvent> appender = new LogbackTestAppender();
            appender.setName("TestAppender");
            context.setAppender(appender);

            List<Appender<ILoggingEvent>> appenders = getAppenders(ROOT_LOGGER);
            assertEquals(1, appenders.size());
            assertSame(appender, appenders.get(0));
        }

        @Test
        @DisplayName("setAppender(null)")
        void testSetNullAppender() {
            assertThrows(NullPointerException.class, () -> context.setAppender(null));

            validateLoggers();
        }

        @Test
        @DisplayName("removeAppender(Appender)")
        void testRemoveAppender() {
            // First add one, so we see a difference with removeAppenders()

            Appender<ILoggingEvent> appender = new LogbackTestAppender();
            appender.setName("TestAppender");
            context.addAppender(appender);

            List<Appender<ILoggingEvent>> appenders = getAppenders(ROOT_LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(LogbackTestAppender.class, appenders.get(0));
            assertEquals("A1", appenders.get(0).getName());
            assertSame(appender, appenders.get(1));

            context.removeAppender(appenders.get(0));

            appenders = getAppenders(ROOT_LOGGER);
            assertEquals(1, appenders.size());
            assertSame(appender, appenders.get(0));
        }

        @Test
        @DisplayName("removeAppender(null)")
        void testRemoveNullAppender() {
            assertThrows(NullPointerException.class, () -> context.removeAppender(null));

            validateLoggers();
        }

        @Test
        @DisplayName("removeAppenders()")
        void testRemoveAppenders() {
            context.removeAppenders();

            List<Appender<ILoggingEvent>> appenders = getAppenders(ROOT_LOGGER);
            assertEquals(0, appenders.size());
        }

        @Test
        @DisplayName("removeAppenders(Predicate)")
        void testRemoveAppendersWithFilter() {
            // First add one, so we see a difference with removeAppenders()

            Appender<ILoggingEvent> appender = new LogbackTestAppender();
            appender.setName("TestAppender");
            context.addAppender(appender);

            List<Appender<ILoggingEvent>> appenders = getAppenders(ROOT_LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(LogbackTestAppender.class, appenders.get(0));
            assertEquals("A1", appenders.get(0).getName());
            assertSame(appender, appenders.get(1));

            context.removeAppenders(a -> a.getName().startsWith("A"));

            appenders = getAppenders(ROOT_LOGGER);
            assertEquals(1, appenders.size());
            assertSame(appender, appenders.get(0));
        }

        @Test
        @DisplayName("removeAppenders(null)")
        void testRemoveAppendersWithNullFilter() {
            assertThrows(NullPointerException.class, () -> context.removeAppenders(null));

            validateLoggers();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(booleans = { true, false })
        @DisplayName("useParentAppenders(boolean)")
        void testUseParentAppenders(boolean useParentAppenders) {
            context.useParentAppenders(useParentAppenders);

            assertEquals(useParentAppenders, ROOT_LOGGER.isAdditive());
        }
    }
}
