/*
 * LogbackLoggerContext.java
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
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * {@code LogbackLoggerContext} represents a logback {@link Logger}. It can be injected using {@link TestLogger}, {@link TestLogger.ForClass} or
 * {@link TestLogger.Root}, and can be used to configure the logger for test purposes.
 *
 * @author Rob Spoor
 */
public final class LogbackLoggerContext extends LoggerContext {

    private final Helper helper;

    private LogbackLoggerContext(Logger logger) {
        helper = new Helper(logger);
    }

    static LogbackLoggerContext forLogger(String name) {
        Logger logger = (Logger) LoggerFactory.getLogger(name);
        return new LogbackLoggerContext(logger);
    }

    static LogbackLoggerContext forLogger(Class<?> c) {
        Logger logger = (Logger) LoggerFactory.getLogger(c);
        return new LogbackLoggerContext(logger);
    }

    static LogbackLoggerContext forRootLogger() {
        return forLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    }

    /**
     * Sets the new level for the logger.
     *
     * @param level The new level.
     * @return This object.
     * @throws NullPointerException If the given level is {@code null}.
     */
    public LogbackLoggerContext setLevel(Level level) {
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
    public LogbackLoggerContext addAppender(Appender<ILoggingEvent> appender) {
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
    public LogbackLoggerContext setAppender(Appender<ILoggingEvent> appender) {
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
    public LogbackLoggerContext removeAppender(Appender<ILoggingEvent> appender) {
        Objects.requireNonNull(appender);
        helper.removeAppender(appender);
        return this;
    }

    /**
     * Removes all appenders from the logger.
     *
     * @return This object.
     */
    public LogbackLoggerContext removeAppenders() {
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
    public LogbackLoggerContext removeAppenders(Predicate<? super Appender<ILoggingEvent>> filter) {
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
    public LogbackLoggerContext useParentAppenders(boolean useParentAppenders) {
        helper.useParentAppenders(useParentAppenders);
        return this;
    }

    Stream<Appender<ILoggingEvent>> streamAppenders() {
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

    private static final class Helper extends LoggerContextHelper<Level, Appender<ILoggingEvent>> {

        private final Logger logger;

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
        Iterable<Appender<ILoggingEvent>> appenders() {
            return logger::iteratorForAppenders;
        }

        @Override
        void addAppender(Appender<ILoggingEvent> appender) {
            logger.addAppender(appender);
        }

        @Override
        void removeAppender(Appender<ILoggingEvent> appender) {
            logger.detachAppender(appender);
        }

        @Override
        boolean useParentAppenders() {
            return logger.isAdditive();
        }

        @Override
        void useParentAppenders(boolean useParentAppenders) {
            logger.setAdditive(useParentAppenders);
        }
    }
}
