/*
 * Reload4jUtils.java
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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.github.robtimus.junit.support.extension.testlogger.TestLogger;

@SuppressWarnings({ "nls", "javadoc" })
public final class Reload4jUtils {

    public static final Logger LOGGER = Logger.getLogger(TestLogger.class);
    public static final Logger ROOT_LOGGER = Logger.getRootLogger();

    private static final Comparator<Appender> APPENDER_COMPARATOR = Comparator.comparing(Appender::getName)
            .thenComparing(Appender::getClass, Comparator.comparing(Class::getName));

    private Reload4jUtils() {
    }

    public static void validateLoggers() {
        assertEquals(Level.INFO, LOGGER.getLevel());
        assertTrue(LOGGER.getAdditivity());

        List<Appender> appenders = getAppenders(LOGGER);
        assertEquals(1, appenders.size());
        assertInstanceOf(Reload4jTestAppender.class, appenders.get(0));
        assertEquals("A2", appenders.get(0).getName());

        assertEquals(Level.WARN, ROOT_LOGGER.getLevel());
        assertTrue(ROOT_LOGGER.getAdditivity());

        List<Appender> rootAppenders = getAppenders(ROOT_LOGGER);
        assertEquals(1, rootAppenders.size());
        assertInstanceOf(Reload4jTestAppender.class, rootAppenders.get(0));
        assertEquals("A1", rootAppenders.get(0).getName());
    }

    public static List<Appender> getAppenders(Logger logger) {
        List<Appender> appenders = new ArrayList<>();
        for (@SuppressWarnings("unchecked") Enumeration<Appender> e = logger.getAllAppenders(); e.hasMoreElements(); ) {
            appenders.add(e.nextElement());
        }
        appenders.sort(APPENDER_COMPARATOR);
        return appenders;
    }

    public static Reload4jTestAppender getTestAppender() {
        return getTestAppender(LOGGER);
    }

    public static Reload4jTestAppender getRootTestAppender() {
        return getTestAppender(ROOT_LOGGER);
    }

    @SuppressWarnings("unchecked")
    public static Reload4jTestAppender getTestAppender(Logger logger) {
        return getTestAppender(logger.getAllAppenders().asIterator());
    }

    public static Reload4jTestAppender getTestAppender(Iterator<Appender> appenderIterator) {
        return getTestAppender(StreamSupport.stream(Spliterators.spliteratorUnknownSize(appenderIterator, 0), false));
    }

    public static Reload4jTestAppender getTestAppender(Stream<Appender> appenderStream) {
        List<Reload4jTestAppender> appenders = appenderStream
                .filter(Reload4jTestAppender.class::isInstance)
                .map(Reload4jTestAppender.class::cast)
                .collect(Collectors.toList());
        assertEquals(1, appenders.size());
        return appenders.get(0);
    }
}
