/*
 * LogbackUtils.java
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.slf4j.LoggerFactory;
import com.github.robtimus.junit.support.extension.testlogger.TestLogger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

@SuppressWarnings({ "nls", "javadoc" })
public final class LogbackUtils {

    public static final Logger LOGGER = (Logger) LoggerFactory.getLogger(TestLogger.class);
    public static final Logger DISABLED_LOGGER = (Logger) LoggerFactory.getLogger(TestLogger.class.getName() + ".disabled");
    public static final Logger ROOT_LOGGER = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

    private static final Comparator<Appender<ILoggingEvent>> APPENDER_COMPARATOR = Comparator.comparing((Appender<ILoggingEvent> a) -> a.getName())
            .thenComparing(Appender::getClass, Comparator.comparing(Class::getName));

    private LogbackUtils() {
    }

    public static void validateLoggers() {
        assertEquals(Level.INFO, LOGGER.getLevel());
        assertTrue(LOGGER.isAdditive());

        List<Appender<ILoggingEvent>> appenders = getAppenders(LOGGER);
        assertEquals(1, appenders.size());
        assertInstanceOf(LogbackTestAppender.class, appenders.get(0));
        assertEquals("A2", appenders.get(0).getName());

        assertEquals(Level.INFO, DISABLED_LOGGER.getLevel());
        assertTrue(DISABLED_LOGGER.isAdditive());

        List<Appender<ILoggingEvent>> disabledAppenders = getAppenders(DISABLED_LOGGER);
        assertEquals(1, disabledAppenders.size());
        assertInstanceOf(LogbackTestAppender.class, disabledAppenders.get(0));
        assertEquals("A3", disabledAppenders.get(0).getName());

        assertEquals(Level.WARN, ROOT_LOGGER.getLevel());
        assertTrue(ROOT_LOGGER.isAdditive());

        List<Appender<ILoggingEvent>> rootAppenders = getAppenders(ROOT_LOGGER);
        assertEquals(1, rootAppenders.size());
        assertInstanceOf(LogbackTestAppender.class, rootAppenders.get(0));
        assertEquals("A1", rootAppenders.get(0).getName());
    }

    public static List<Appender<ILoggingEvent>> getAppenders(Logger logger) {
        List<Appender<ILoggingEvent>> appenders = new ArrayList<>();
        for (Iterator<Appender<ILoggingEvent>> i = logger.iteratorForAppenders(); i.hasNext(); ) {
            appenders.add(i.next());
        }
        appenders.sort(APPENDER_COMPARATOR);
        return appenders;
    }

    public static LogbackTestAppender getTestAppender() {
        return getTestAppender(LOGGER);
    }

    public static LogbackTestAppender getRootTestAppender() {
        return getTestAppender(ROOT_LOGGER);
    }

    public static LogbackTestAppender getTestAppender(Logger logger) {
        return getTestAppender(logger.iteratorForAppenders());
    }

    public static LogbackTestAppender getTestAppender(Iterator<Appender<ILoggingEvent>> appenderIterator) {
        return getTestAppender(StreamSupport.stream(Spliterators.spliteratorUnknownSize(appenderIterator, 0), false));
    }

    public static LogbackTestAppender getTestAppender(Stream<Appender<ILoggingEvent>> appenderStream) {
        List<LogbackTestAppender> appenders = appenderStream
                .filter(LogbackTestAppender.class::isInstance)
                .map(LogbackTestAppender.class::cast)
                .collect(Collectors.toList());
        assertEquals(1, appenders.size());
        return appenders.get(0);
    }
}
