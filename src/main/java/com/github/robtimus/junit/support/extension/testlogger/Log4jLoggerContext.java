/*
 * Log4jLoggerContext.java
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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;

/**
 * {@code Log4jLoggerContext} represents a Log4j {@link Logger} (2.x only). It can be injected using {@link TestLogger}, {@link TestLogger.ForClass}
 * or {@link TestLogger.Root}, and can be used to configure the logger for test purposes.
 * <p>
 * Note: using <em>mocks</em> or <em>spies</em> usually don't work out-of-the-box. One of the reasons is that the {@link LogEvent} passed to
 * {@link Appender#append(LogEvent)} may be mutable, and may be reused and altered by Log4j. To overcome this issue, spy on a
 * {@link Log4jNullAppender} and verify calls to its {@link Log4jNullAppender#ignore(LogEvent)} method.
 *
 * @author Rob Spoor
 */
public final class Log4jLoggerContext extends LoggerContext<Level, Appender> {

    private final Logger logger;

    private Log4jLoggerContext(Logger logger) {
        this.logger = logger;
    }

    static Log4jLoggerContext forLogger(String name) {
        Logger logger = (Logger) LogManager.getLogger(name);
        return new Log4jLoggerContext(logger);
    }

    static Log4jLoggerContext forLogger(Class<?> c) {
        Logger logger = (Logger) LogManager.getLogger(c);
        return new Log4jLoggerContext(logger);
    }

    static Log4jLoggerContext forRootLogger() {
        Logger logger = (Logger) LogManager.getRootLogger();
        return new Log4jLoggerContext(logger);
    }

    /**
     * Sets the new level for the logger.
     *
     * @param level The new level.
     * @return This object.
     * @throws NullPointerException If the given level is {@code null}.
     */
    public Log4jLoggerContext setLevel(Level level) {
        Objects.requireNonNull(level);
        doSetLevel(level);
        return this;
    }

    @Override
    Level doGetLevel() {
        return logger.getLevel();
    }

    @Override
    void doSetLevel(Level level) {
        logger.setLevel(level);
    }

    /**
     * Adds an appender to the logger.
     *
     * @param appender The appender to add.
     * @return This object.
     * @throws NullPointerException If the given appender is {@code null}.
     */
    public Log4jLoggerContext addAppender(Appender appender) {
        Objects.requireNonNull(appender);
        doAddAppender(appender);
        return this;
    }

    /**
     * Sets the single appender for the logger. All existing appenders will first be removed.
     *
     * @param appender The appender to set.
     * @return This object.
     * @throws NullPointerException If the given appender is {@code null}.
     */
    public Log4jLoggerContext setAppender(Appender appender) {
        Objects.requireNonNull(appender);
        doSetAppender(appender);
        return this;
    }

    /**
     * Removes an appender from the logger.
     *
     * @param appender The appender to remove.
     * @return This object.
     * @throws NullPointerException If the given appender is {@code null}.
     */
    public Log4jLoggerContext removeAppender(Appender appender) {
        Objects.requireNonNull(appender);
        doRemoveAppender(appender);
        return this;
    }

    /**
     * Removes all appenders from the logger.
     *
     * @return This object.
     */
    public Log4jLoggerContext removeAppenders() {
        doRemoveAppenders();
        return this;
    }

    /**
     * Removes all appenders from the logger that match a filter.
     *
     * @param filter The filter to use.
     * @return This object.
     * @throws NullPointerException If the given filter is {@code null}.
     */
    public Log4jLoggerContext removeAppenders(Predicate<? super Appender> filter) {
        Objects.requireNonNull(filter);
        doRemoveAppenders(filter);
        return this;
    }

    @Override
    Iterable<Appender> doListAppenders() {
        return logger.getAppenders().values();
    }

    @Override
    void doAddAppender(Appender appender) {
        logger.addAppender(appender);
    }

    @Override
    void doRemoveAppender(Appender appender) {
        logger.removeAppender(appender);
    }

    /**
     * Sets whether or not to use parent appenders for the logger.
     *
     * @param useParentAppenders {@code true} to use parent appenders, {@code false} otherwise.
     * @return This object.
     */
    public Log4jLoggerContext useParentAppenders(boolean useParentAppenders) {
        doSetUseParentAppenders(useParentAppenders);
        return this;
    }

    @Override
    boolean doGetUseParentAppenders() {
        return logger.isAdditive();
    }

    @Override
    void doSetUseParentAppenders(boolean useParentAppenders) {
        logger.setAdditive(useParentAppenders);
    }

    @Override
    void saveSettings() {
        long originalAppenderCount = streamAppenders().count();

        // logger.getAppenders() returns the appenders of the parent, unless an appender has been added first
        Appender dummyAppender = Log4jNullAppender.create(UUID.randomUUID().toString());
        logger.addAppender(dummyAppender);
        logger.removeAppender(dummyAppender);

        long appenderCount = streamAppenders().count();
        if (appenderCount != originalAppenderCount) {
            // the appenders were inherited from the parent; explicitly restore the inheritance behavior
            logger.setAdditive(true);
        }

        super.saveSettings();
    }
}
