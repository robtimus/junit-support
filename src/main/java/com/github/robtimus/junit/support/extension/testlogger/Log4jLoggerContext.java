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
import java.util.stream.Stream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import com.github.robtimus.junit.support.extension.logging.capture.CapturingLog4jAppender;
import com.github.robtimus.junit.support.extension.testlogger.TestLoggerExtension.ContextFactory;

/**
 * {@code Log4jLoggerContext} represents a Log4j {@link Logger} (2.x only). It can be injected using {@link TestLogger}, {@link TestLogger.ForClass}
 * or {@link TestLogger.Root}, and can be used to configure the logger for test purposes.
 * <p>
 * Note: using <em>mocks</em> or <em>spies</em> usually don't work out-of-the-box. One of the reasons is that the {@link LogEvent} passed to
 * {@link Appender#append(LogEvent)} may be mutable, and may be reused and altered by Log4j. To overcome this issue, spy on a
 * {@link Log4jNullAppender} and verify calls to its {@link Log4jNullAppender#ignore(LogEvent)} method.
 *
 * @author Rob Spoor
 * @since 2.1
 */
@SuppressWarnings("exports") // the dependency is static, and therefore needs to be repeated anyway
public final class Log4jLoggerContext extends LoggerContext {

    private final Helper helper;

    private Log4jLoggerContext(Logger logger) {
        helper = new Helper(logger);
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

    static String className(Class<?> c) {
        // Unlike the other logging frameworks, Log4j tries to use the canonical name first
        String canonicalName = c.getCanonicalName();
        return canonicalName != null ? canonicalName : c.getName();
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
    public Log4jLoggerContext addAppender(Appender appender) {
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
    public Log4jLoggerContext setAppender(Appender appender) {
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
    public Log4jLoggerContext removeAppender(Appender appender) {
        Objects.requireNonNull(appender);
        helper.removeAppender(appender);
        return this;
    }

    /**
     * Removes all appenders from the logger.
     *
     * @return This object.
     */
    public Log4jLoggerContext removeAppenders() {
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
    public Log4jLoggerContext removeAppenders(Predicate<? super Appender> filter) {
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
    public Log4jLoggerContext useParentAppenders(boolean useParentAppenders) {
        helper.useParentAppenders(useParentAppenders);
        return this;
    }

    /**
     * Returns an object that captures logged events. This can be used instead of having to append a capturing appender manually.
     *
     * @return An object that captures logged events.
     * @since 3.0
     */
    public LogCaptor<LogEvent> capture() {
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

    private static final class Helper extends LoggerContextHelper<Level, LogEvent, Appender> {

        private final Logger logger;

        private CapturingLog4jAppender captorAppender;
        private LogCaptor<LogEvent> logCaptor;

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
        Iterable<Appender> appenders() {
            return logger.getAppenders().values();
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
            return logger.isAdditive();
        }

        @Override
        void useParentAppenders(boolean useParentAppenders) {
            logger.setAdditive(useParentAppenders);
        }

        @Override
        LogCaptor<LogEvent> logCaptor() {
            if (logCaptor == null) {
                captorAppender = new CapturingLog4jAppender("LogCaptor-" + UUID.randomUUID().toString()); //$NON-NLS-1$
                captorAppender.start();
                logCaptor = new LogCaptor<>(captorAppender::getEvents, captorAppender::clearEvents);
                addAppender(captorAppender);
            }
            return logCaptor;
        }

        @Override
        void saveSettings() {
            long originalAppenderCount = super.streamAppenders().count();

            // logger.getAppenders() returns the appenders of the parent, unless an appender has been added first
            Appender dummyAppender = Log4jNullAppender.create(UUID.randomUUID().toString());
            logger.addAppender(dummyAppender);
            logger.removeAppender(dummyAppender);

            long appenderCount = super.streamAppenders().count();
            if (appenderCount != originalAppenderCount) {
                // the appenders were inherited from the parent; explicitly restore the inheritance behavior
                logger.setAdditive(true);
            }

            super.saveSettings();
        }
    }

    static final class Factory extends ContextFactory<Log4jLoggerContext> {

        @Override
        Log4jLoggerContext newLoggerContext(String loggerName) {
            return forLogger(loggerName);
        }

        @Override
        Log4jLoggerContext newLoggerContext(Class<?> loggerClass) {
            return forLogger(loggerClass);
        }

        @Override
        Log4jLoggerContext newRootLoggerContext() {
            return forRootLogger();
        }

        @Override
        String keyPrefix() {
            return Log4jLoggerContext.class.getSimpleName();
        }

        @Override
        String loggerName(Class<?> loggerClass) {
            return className(loggerClass);
        }

        @Override
        String rootLoggerName() {
            return LogManager.ROOT_LOGGER_NAME;
        }
    }
}
