/*
 * Log4jLoggerContextTest.java
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("nls")
final class Log4jLoggerContextTest {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(TestLogger.class);
    private static final Logger ROOT_LOGGER = (Logger) LogManager.getRootLogger();

    private static final Comparator<Appender> APPENDER_COMPARATOR = Comparator.comparing(Appender::getName)
            .thenComparing(Appender::getClass, Comparator.comparing(Class::getName));

    private Log4jLoggerContextTest() {
    }

    @BeforeAll
    @AfterAll
    static void validateLoggers() {
        assertEquals(Level.INFO, LOGGER.getLevel());
        assertTrue(LOGGER.isAdditive());

        List<Appender> appenders = getAppenders(LOGGER);
        assertEquals(1, appenders.size());
        assertInstanceOf(Log4jTestAppender.class, appenders.get(0));
        assertEquals("A2", appenders.get(0).getName());

        assertEquals(Level.WARN, ROOT_LOGGER.getLevel());
        assertFalse(ROOT_LOGGER.isAdditive());

        List<Appender> rootAppenders = getAppenders(ROOT_LOGGER);
        assertEquals(1, rootAppenders.size());
        assertInstanceOf(Log4jTestAppender.class, rootAppenders.get(0));
        assertEquals("A1", rootAppenders.get(0).getName());
    }

    private static List<Appender> getAppenders(Logger logger) {
        List<Appender> appenders = new ArrayList<>();
        for (Appender appender : logger.getAppenders().values()) {
            appenders.add(appender);
        }
        appenders.sort(APPENDER_COMPARATOR);
        return appenders;
    }

    @Nested
    @DisplayName("forLogger(String)")
    class ForLoggerByName extends ForLogger {

        ForLoggerByName() {
            super(() -> Log4jLoggerContext.forLogger(TestLogger.class.getName()));
        }
    }

    @Nested
    @DisplayName("forLogger(Class)")
    class ForLoggerByClass extends ForLogger {

        ForLoggerByClass() {
            super(() -> Log4jLoggerContext.forLogger(TestLogger.class));
        }
    }

    abstract static class ForLogger {

        private final Supplier<Log4jLoggerContext> contextFactory;

        private Log4jLoggerContext context;

        private ForLogger(Supplier<Log4jLoggerContext> contextFactory) {
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
            Appender appender = new Log4jTestAppender("TestAppender");
            context.addAppender(appender);

            List<Appender> appenders = getAppenders(LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(Log4jTestAppender.class, appenders.get(0));
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
            Appender appender = new Log4jTestAppender("TestAppender");
            context.setAppender(appender);

            List<Appender> appenders = getAppenders(LOGGER);
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

            Appender appender = new Log4jTestAppender("TestAppender");
            context.addAppender(appender);

            List<Appender> appenders = getAppenders(LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(Log4jTestAppender.class, appenders.get(0));
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

            List<Appender> appenders = getAppenders(LOGGER);
            assertEquals(0, appenders.size());
        }

        @Test
        @DisplayName("removeAppenders(Predicate)")
        void testRemoveAppendersWithFilter() {
            // First add one, so we see a difference with removeAppenders()

            Appender appender = new Log4jTestAppender("TestAppender");
            context.addAppender(appender);

            List<Appender> appenders = getAppenders(LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(Log4jTestAppender.class, appenders.get(0));
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
    }

    @Nested
    @DisplayName("forRootLogger()")
    class ForRootLogger {

        private Log4jLoggerContext context;

        @BeforeEach
        void initContext() {
            context = Log4jLoggerContext.forRootLogger();
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
            Appender appender = new Log4jTestAppender("TestAppender");
            context.addAppender(appender);

            List<Appender> appenders = getAppenders(ROOT_LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(Log4jTestAppender.class, appenders.get(0));
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
            Appender appender = new Log4jTestAppender("TestAppender");
            context.setAppender(appender);

            List<Appender> appenders = getAppenders(ROOT_LOGGER);
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

            Appender appender = new Log4jTestAppender("TestAppender");
            context.addAppender(appender);

            List<Appender> appenders = getAppenders(ROOT_LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(Log4jTestAppender.class, appenders.get(0));
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

            List<Appender> appenders = getAppenders(ROOT_LOGGER);
            assertEquals(0, appenders.size());
        }

        @Test
        @DisplayName("removeAppenders(Predicate)")
        void testRemoveAppendersWithFilter() {
            // First add one, so we see a difference with removeAppenders()

            Appender appender = new Log4jTestAppender("TestAppender");
            context.addAppender(appender);

            List<Appender> appenders = getAppenders(ROOT_LOGGER);
            assertEquals(2, appenders.size());
            assertInstanceOf(Log4jTestAppender.class, appenders.get(0));
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
