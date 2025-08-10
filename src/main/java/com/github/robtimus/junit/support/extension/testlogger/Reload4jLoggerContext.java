/*
 * Reload4jLoggerContext.java
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

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import com.github.robtimus.junit.support.extension.logging.capture.CapturingReload4jAppender;
import com.github.robtimus.junit.support.extension.testlogger.TestLoggerExtension.ContextFactory;

/**
 * {@code Reload4jLoggerContext} represents a reload4j {@link Logger}. It can be injected using {@link TestLogger}, {@link TestLogger.ForClass} or
 * {@link TestLogger.Root}, and can be used to configure the logger for test purposes.
 * <p>
 * Note: reloadj4 is based on Log4j 1.x. As a result, this class can most likely also be used with Log4j 1.x. However, no guarantees are made.
 *
 * @author Rob Spoor
 * @since 2.1
 */
public final class Reload4jLoggerContext extends LoggerContext {

    private final Helper helper;

    private Reload4jLoggerContext(Logger logger) {
        helper = new Helper(logger);
    }

    static Reload4jLoggerContext forLogger(String name) {
        Logger logger = Logger.getLogger(name);
        return new Reload4jLoggerContext(logger);
    }

    static Reload4jLoggerContext forLogger(Class<?> c) {
        Logger logger = Logger.getLogger(c);
        return new Reload4jLoggerContext(logger);
    }

    static Reload4jLoggerContext forRootLogger() {
        Logger logger = Logger.getRootLogger();
        return new Reload4jLoggerContext(logger);
    }

    /**
     * Sets the new level for the logger.
     *
     * @param level The new level.
     * @return This object.
     * @throws NullPointerException If the given level is {@code null}.
     */
    public Reload4jLoggerContext setLevel(Level level) {
        Objects.requireNonNull(level);
        helper.setLevel(level);
        return this;
    }

    /**
     * Adds an appender to the logger.
     *
     * @param appender The appender to add.
     * @return This object.
     * @throws NullPointerException If the given appender is {@code null}.
     */
    public Reload4jLoggerContext addAppender(Appender appender) {
        Objects.requireNonNull(appender);
        helper.addAppender(appender);
        return this;
    }

    /**
     * Sets the single appender for the logger. All existing appenders will first be removed.
     *
     * @param appender The appender to set.
     * @return This object.
     * @throws NullPointerException If the given appender is {@code null}.
     */
    public Reload4jLoggerContext setAppender(Appender appender) {
        Objects.requireNonNull(appender);
        helper.setAppender(appender);
        return this;
    }

    /**
     * Removes an appender from the logger.
     *
     * @param appender The appender to remove.
     * @return This object.
     * @throws NullPointerException If the given appender is {@code null}.
     */
    public Reload4jLoggerContext removeAppender(Appender appender) {
        Objects.requireNonNull(appender);
        helper.removeAppender(appender);
        return this;
    }

    /**
     * Removes all appenders from the logger.
     *
     * @return This object.
     */
    public Reload4jLoggerContext removeAppenders() {
        helper.removeAppenders();
        return this;
    }

    /**
     * Removes all appenders from the logger that match a filter.
     *
     * @param filter The filter to use.
     * @return This object.
     * @throws NullPointerException If the given filter is {@code null}.
     */
    public Reload4jLoggerContext removeAppenders(Predicate<? super Appender> filter) {
        Objects.requireNonNull(filter);
        helper.removeAppenders(filter);
        return this;
    }

    /**
     * Sets whether or not to use parent appenders for the logger.
     *
     * @param useParentAppenders {@code true} to use parent appenders, {@code false} otherwise.
     * @return This object.
     */
    public Reload4jLoggerContext useParentAppenders(boolean useParentAppenders) {
        helper.useParentAppenders(useParentAppenders);
        return this;
    }

    /**
     * Returns an object that captures logged events. This can be used instead of having to append a capturing appender manually.
     *
     * @return An object that captures logged events.
     * @since 3.0
     */
    public LogCaptor<LoggingEvent> capture() {
        return helper.logCaptor();
    }

    Stream<Appender> streamAppenders() {
        return helper.streamAppenders();
    }

    @Override
    void saveSettings() {
        helper.saveSettings();
    }

    @Override
    public void restore() {
        helper.restore();
    }

    private static final class Helper extends LoggerContextHelper<Level, LoggingEvent, Appender> {

        private final Logger logger;

        private CapturingReload4jAppender captorAppender;
        private LogCaptor<LoggingEvent> logCaptor;

        private Helper(Logger logger) {
            this.logger = logger;
        }

        @Override
        Level getLevel() {
            return logger.getLevel();
        }

        @Override
        void setLevel(Level level) {
            logger.setLevel(level);
        }

        @Override
        @SuppressWarnings("unchecked")
        Iterable<Appender> appenders() {
            return () -> logger.getAllAppenders().asIterator();
        }

        @Override
        void addAppender(Appender appender) {
            logger.addAppender(appender);
        }

        @Override
        void removeAppender(Appender appender, boolean force) {
            if (force || isNonHelperAppender(appender)) {
                logger.removeAppender(appender);
            }
        }

        private boolean isNonHelperAppender(Appender appender) {
            return appender != captorAppender;
        }

        @Override
        boolean useParentAppenders() {
            return logger.getAdditivity();
        }

        @Override
        void useParentAppenders(boolean useParentAppenders) {
            logger.setAdditivity(useParentAppenders);
        }

        @Override
        LogCaptor<LoggingEvent> logCaptor() {
            if (logCaptor == null) {
                captorAppender = new CapturingReload4jAppender();
                logCaptor = new LogCaptor<>(captorAppender::getEvents, captorAppender::clearEvents);
                addAppender(captorAppender);
            }
            return logCaptor;
        }
    }

    static final class Factory extends ContextFactory<Reload4jLoggerContext> {

        // The root logger in reload4j has name "root", but a logger with name "root" is not the same.
        // Therefore, use something unique instead.
        private static final String ROOT_LOGGER_NAME = UUID.randomUUID().toString();

        @Override
        Reload4jLoggerContext newLoggerContext(String loggerName) {
            return forLogger(loggerName);
        }

        @Override
        Reload4jLoggerContext newLoggerContext(Class<?> loggerClass) {
            return forLogger(loggerClass);
        }

        @Override
        Reload4jLoggerContext newRootLoggerContext() {
            return forRootLogger();
        }

        @Override
        String keyPrefix() {
            return Reload4jLoggerContext.class.getSimpleName();
        }

        @Override
        String loggerName(Class<?> loggerClass) {
            return loggerClass.getName();
        }

        @Override
        String rootLoggerName() {
            return ROOT_LOGGER_NAME;
        }
    }
}
