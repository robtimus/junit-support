/*
 * JdkLoggingUtils.java
 * Copyright 2024 Rob Spoor
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.github.robtimus.junit.support.extension.testlogger.TestLogger;

@SuppressWarnings({ "nls", "javadoc" })
public final class JdkLoggingUtils {

    public static final Logger LOGGER = Logger.getLogger(TestLogger.class.getName());
    public static final Logger DISABLED_LOGGER = Logger.getLogger(TestLogger.class.getName() + ".disabled");
    public static final Logger ROOT_LOGGER = Logger.getLogger("");

    private static final Comparator<Handler> HANDLER_COMPARATOR = Comparator.comparing(Handler::getClass, Comparator.comparing(Class::getName));

    private JdkLoggingUtils() {
    }

    public static void validateLoggers() {
        assertEquals(Level.INFO, LOGGER.getLevel());
        assertTrue(LOGGER.getUseParentHandlers());

        List<Handler> handlers = getHandlers(LOGGER);
        assertEquals(1, handlers.size());
        assertInstanceOf(JdkTestHandler.class, handlers.get(0));

        assertEquals(Level.INFO, DISABLED_LOGGER.getLevel());
        assertTrue(DISABLED_LOGGER.getUseParentHandlers());

        List<Handler> disabledHandlers = getHandlers(DISABLED_LOGGER);
        assertEquals(1, disabledHandlers.size());
        assertInstanceOf(JdkTestHandler.class, disabledHandlers.get(0));

        assertEquals(Level.WARNING, ROOT_LOGGER.getLevel());
        assertTrue(ROOT_LOGGER.getUseParentHandlers());

        List<Handler> rootHandlers = getHandlers(ROOT_LOGGER);
        assertEquals(1, rootHandlers.size());
        assertInstanceOf(JdkTestHandler.class, rootHandlers.get(0));
    }

    public static List<Handler> getHandlers(Logger logger) {
        List<Handler> handlers = new ArrayList<>();
        Collections.addAll(handlers, logger.getHandlers());
        handlers.sort(HANDLER_COMPARATOR);
        return handlers;
    }

    public static JdkTestHandler getTestHandler() {
        return getTestHandler(LOGGER);
    }

    public static JdkTestHandler getRootTestHandler() {
        return getTestHandler(ROOT_LOGGER);
    }

    public static JdkTestHandler getTestHandler(Logger logger) {
        return getTestHandler(Arrays.stream(logger.getHandlers()));
    }

    public static JdkTestHandler getTestHandler(Stream<Handler> handlerStream) {
        List<JdkTestHandler> handlers = handlerStream
                .filter(JdkTestHandler.class::isInstance)
                .map(JdkTestHandler.class::cast)
                .collect(Collectors.toList());
        assertEquals(1, handlers.size());
        return handlers.get(0);
    }
}
