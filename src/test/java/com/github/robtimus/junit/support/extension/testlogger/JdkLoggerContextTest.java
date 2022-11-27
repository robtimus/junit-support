/*
 * JdkLoggerContextTest.java
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
final class JdkLoggerContextTest {

    private static final Logger LOGGER = Logger.getLogger(TestLogger.class.getName());
    private static final Logger ROOT_LOGGER = Logger.getLogger("");

    private static final Comparator<Handler> HANDLER_COMPARATOR = Comparator.comparing(Handler::getClass, Comparator.comparing(Class::getName));

    private JdkLoggerContextTest() {
    }

    @BeforeAll
    @AfterAll
    static void validateLoggers() {
        assertEquals(Level.INFO, LOGGER.getLevel());
        assertTrue(LOGGER.getUseParentHandlers());

        List<Handler> handlers = getHandlers(LOGGER);
        assertEquals(1, handlers.size());
        assertInstanceOf(JdkTestHandler.class, handlers.get(0));

        assertEquals(Level.WARNING, ROOT_LOGGER.getLevel());
        assertTrue(ROOT_LOGGER.getUseParentHandlers());

        List<Handler> rootHandlers = getHandlers(ROOT_LOGGER);
        assertEquals(1, rootHandlers.size());
        assertInstanceOf(JdkTestHandler.class, rootHandlers.get(0));
    }

    private static List<Handler> getHandlers(Logger logger) {
        List<Handler> handlers = new ArrayList<>();
        Collections.addAll(handlers, logger.getHandlers());
        handlers.sort(HANDLER_COMPARATOR);
        return handlers;
    }

    @Nested
    @DisplayName("forLogger(String)")
    class ForLoggerByName extends ForLogger {

        ForLoggerByName() {
            super(() -> JdkLoggerContext.forLogger(TestLogger.class.getName()));
        }
    }

    @Nested
    @DisplayName("forLogger(Class)")
    class ForLoggerByClass extends ForLogger {

        ForLoggerByClass() {
            super(() -> JdkLoggerContext.forLogger(TestLogger.class));
        }
    }

    abstract static class ForLogger {

        private final Supplier<JdkLoggerContext> contextFactory;

        private JdkLoggerContext context;

        private ForLogger(Supplier<JdkLoggerContext> contextFactory) {
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

        @Test
        @DisplayName("getName()")
        void testGetName() {
            assertEquals(LOGGER.getName(), context.getName());
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = { "OFF", "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST", "ALL" })
        @DisplayName("setLevel(Level)")
        void testSetLevel(String levelValue) {
            Level level = Level.parse(levelValue);
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
        @DisplayName("addHandler(Handler)")
        void testAddHandler() {
            Handler handler = new JdkTestHandler();
            context.addHandler(handler);

            List<Handler> handlers = getHandlers(LOGGER);
            assertEquals(2, handlers.size());
            assertInstanceOf(JdkTestHandler.class, handlers.get(0));
            assertNotSame(handler, handlers.get(0));
            assertSame(handler, handlers.get(1));
        }

        @Test
        @DisplayName("addHandler(null)")
        void testAddNullHandler() {
            assertThrows(NullPointerException.class, () -> context.addHandler(null));

            validateLoggers();
        }

        @Test
        @DisplayName("setHandler(Handler)")
        void testSetHandler() {
            Handler handler = new JdkTestHandler();
            context.setHandler(handler);

            List<Handler> handlers = getHandlers(LOGGER);
            assertEquals(1, handlers.size());
            assertInstanceOf(JdkTestHandler.class, handlers.get(0));
            assertSame(handler, handlers.get(0));
        }

        @Test
        @DisplayName("setHandler(null)")
        void testSetNullHandler() {
            assertThrows(NullPointerException.class, () -> context.setHandler(null));

            validateLoggers();
        }

        @Test
        @DisplayName("removeHandler(Handler)")
        void testRemoveHandler() {
            // First add one, so we see a difference with removeHandlers()

            Handler handler = new JdkTestHandler();
            context.addHandler(handler);

            List<Handler> handlers = getHandlers(LOGGER);
            assertEquals(2, handlers.size());
            assertInstanceOf(JdkTestHandler.class, handlers.get(0));
            assertNotSame(handler, handlers.get(0));
            assertSame(handler, handlers.get(1));

            context.removeHandler(handlers.get(0));

            handlers = getHandlers(LOGGER);
            assertEquals(1, handlers.size());
            assertSame(handler, handlers.get(0));
        }

        @Test
        @DisplayName("removeHandler(null)")
        void testRemoveNullHandler() {
            assertThrows(NullPointerException.class, () -> context.removeHandler(null));

            validateLoggers();
        }

        @Test
        @DisplayName("removeHandlers()")
        void testRemoveHandlers() {
            context.removeHandlers();

            List<Handler> handlers = getHandlers(LOGGER);
            assertEquals(0, handlers.size());
        }

        @Test
        @DisplayName("removeHandlers(Predicate)")
        void testRemoveHandlersWithFilter() {
            // First add one, so we see a difference with removeHandlers()

            Handler handler = new JdkTestHandler();
            context.addHandler(handler);

            List<Handler> handlers = getHandlers(LOGGER);
            assertEquals(2, handlers.size());
            assertInstanceOf(JdkTestHandler.class, handlers.get(0));
            assertNotSame(handler, handlers.get(0));
            assertSame(handler, handlers.get(1));

            context.removeHandlers(a -> a instanceof JdkTestHandler && a != handler);

            handlers = getHandlers(LOGGER);
            assertEquals(1, handlers.size());
            assertSame(handler, handlers.get(0));
        }

        @Test
        @DisplayName("removeHandlers(null)")
        void testRemoveHandlersWithNullFilter() {
            assertThrows(NullPointerException.class, () -> context.removeHandlers(null));

            validateLoggers();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(booleans = { true, false })
        @DisplayName("useParentHandlers(boolean)")
        void testUseParentHandlers(boolean useParentHandlers) {
            context.useParentHandlers(useParentHandlers);

            assertEquals(useParentHandlers, LOGGER.getUseParentHandlers());
        }
    }

    @Nested
    @DisplayName("forRootLogger()")
    class ForRootLogger {

        private JdkLoggerContext context;

        @BeforeEach
        void initContext() {
            context = JdkLoggerContext.forRootLogger();
            context.saveSettings();
        }

        @AfterEach
        void restoreContext() {
            context.restore();
            validateLoggers();
        }

        @Test
        @DisplayName("getName()")
        void testGetName() {
            assertEquals(ROOT_LOGGER.getName(), context.getName());
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = { "OFF", "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST", "ALL" })
        @DisplayName("setLevel(Level)")
        void testSetLevel(String levelValue) {
            Level level = Level.parse(levelValue);
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
        @DisplayName("addHandler(Handler)")
        void testAddHandler() {
            Handler handler = new JdkTestHandler();
            context.addHandler(handler);

            List<Handler> handlers = getHandlers(ROOT_LOGGER);
            assertEquals(2, handlers.size());
            assertInstanceOf(JdkTestHandler.class, handlers.get(0));
            assertNotSame(handler, handlers.get(0));
            assertSame(handler, handlers.get(1));
        }

        @Test
        @DisplayName("addHandler(null)")
        void testAddNullHandler() {
            assertThrows(NullPointerException.class, () -> context.addHandler(null));

            validateLoggers();
        }

        @Test
        @DisplayName("setHandler(Handler)")
        void testSetHandler() {
            Handler handler = new JdkTestHandler();
            context.setHandler(handler);

            List<Handler> handlers = getHandlers(ROOT_LOGGER);
            assertEquals(1, handlers.size());
            assertSame(handler, handlers.get(0));
        }

        @Test
        @DisplayName("setHandler(null)")
        void testSetNullHandler() {
            assertThrows(NullPointerException.class, () -> context.setHandler(null));

            validateLoggers();
        }

        @Test
        @DisplayName("removeHandler(Handler)")
        void testRemoveHandler() {
            // First add one, so we see a difference with removeHandlers()

            Handler handler = new JdkTestHandler();
            context.addHandler(handler);

            List<Handler> handlers = getHandlers(ROOT_LOGGER);
            assertEquals(2, handlers.size());
            assertInstanceOf(JdkTestHandler.class, handlers.get(0));
            assertNotSame(handler, handlers.get(0));
            assertSame(handler, handlers.get(1));

            context.removeHandler(handlers.get(0));

            handlers = getHandlers(ROOT_LOGGER);
            assertEquals(1, handlers.size());
            assertSame(handler, handlers.get(0));
        }

        @Test
        @DisplayName("removeHandler(null)")
        void testRemoveNullHandler() {
            assertThrows(NullPointerException.class, () -> context.removeHandler(null));

            validateLoggers();
        }

        @Test
        @DisplayName("removeHandlers()")
        void testRemoveHandlers() {
            context.removeHandlers();

            List<Handler> handlers = getHandlers(ROOT_LOGGER);
            assertEquals(0, handlers.size());
        }

        @Test
        @DisplayName("removeHandlers(Predicate)")
        void testRemoveHandlersWithFilter() {
            // First add one, so we see a difference with removeHandlers()

            Handler handler = new JdkTestHandler();
            context.addHandler(handler);

            List<Handler> handlers = getHandlers(ROOT_LOGGER);
            assertEquals(2, handlers.size());
            assertInstanceOf(JdkTestHandler.class, handlers.get(0));
            assertNotSame(handler, handlers.get(0));
            assertSame(handler, handlers.get(1));

            context.removeHandlers(a -> a instanceof JdkTestHandler && a != handler);

            handlers = getHandlers(ROOT_LOGGER);
            assertEquals(1, handlers.size());
            assertSame(handler, handlers.get(0));
        }

        @Test
        @DisplayName("removeHandlers(null)")
        void testRemoveHandlersWithNullFilter() {
            assertThrows(NullPointerException.class, () -> context.removeHandlers(null));

            validateLoggers();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(booleans = { true, false })
        @DisplayName("useParentHandlers(boolean)")
        void testUseParentHandlers(boolean useParentHandlers) {
            context.useParentHandlers(useParentHandlers);

            assertEquals(useParentHandlers, ROOT_LOGGER.getUseParentHandlers());
        }
    }
}
