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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.verification.VerificationMode;
import org.slf4j.LoggerFactory;
import com.github.robtimus.junit.support.extension.testlogger.TestLoggerExtension.ContextFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * {@code LogbackLoggerContext} represents a logback {@link Logger}. It can be injected using {@link TestLogger}, {@link TestLogger.ForClass} or
 * {@link TestLogger.Root}, and can be used to configure the logger for test purposes.
 *
 * @author Rob Spoor
 * @since 2.1
 */
@SuppressWarnings("exports") // the dependency is static, and therefore needs to be repeated anyway
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

    /**
     * Returns an object that captures logged events. This can be used instead of having to append a capturing appender manually.
     *
     * @return An object that captures logged events.
     * @since 3.0
     */
    public LogCaptor<ILoggingEvent> capture() {
        return helper.logCaptor();
    }

    Stream<Appender<ILoggingEvent>> streamAppenders() {
        return helper.streamAppenders();
    }

    @Override
    void disable() {
        setLevel(Level.OFF);
    }

    @Override
    void startCapture() {
        helper.startCapture();
    }

    @Override
    void logCaptured() {
        helper.logCaptured();
    }

    @Override
    void saveSettings() {
        helper.saveSettings();
    }

    @Override
    public void restore() {
        helper.restore();
    }

    private static final class Helper extends LoggerContextHelper<Level, ILoggingEvent, Appender<ILoggingEvent>> {

        private final Logger logger;

        private Appender<ILoggingEvent> captorAppender;
        private LogCaptor<ILoggingEvent> logCaptor;

        private Appender<ILoggingEvent> captureAppender;

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
        void removeAppender(Appender<ILoggingEvent> appender, boolean force) {
            if (force || isNonHelperAppender(appender)) {
                logger.detachAppender(appender);
            }
        }

        private boolean isNonHelperAppender(Appender<ILoggingEvent> appender) {
            return appender != captorAppender && appender != captureAppender;
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
        @SuppressWarnings("unchecked")
        LogCaptor<ILoggingEvent> logCaptor() {
            if (logCaptor == null) {
                captorAppender = mock(Appender.class);
                logCaptor = new LogCaptor<>(ILoggingEvent.class,
                        verifier(captorAppender),
                        () -> reset(captorAppender));
                addAppender(captorAppender);
            }
            return logCaptor;
        }

        @Override
        @SuppressWarnings("unchecked")
        void startCapture() {
            captureAppender = mock(Appender.class);
            setAppender(captureAppender);
            useParentAppenders(false);
        }

        @Override
        void logCaptured() {
            if (captureAppender != null) {
                List<ILoggingEvent> events = LogCaptor.captured(ILoggingEvent.class, verifier(captureAppender));
                for (ILoggingEvent event : events) {
                    // logger.log takes a different type of event
                    if (logger.isEnabledFor(event.getLevel())) {
                        logger.callAppenders(event);
                    }
                }
            }
        }

        private BiConsumer<VerificationMode, ArgumentCaptor<ILoggingEvent>> verifier(Appender<ILoggingEvent> appender) {
            return (mode, eventCaptor) -> verify(appender, mode).doAppend(eventCaptor.capture());
        }
    }

    static final class Factory extends ContextFactory<LogbackLoggerContext> {

        @Override
        LogbackLoggerContext newLoggerContext(String loggerName) {
            return forLogger(loggerName);
        }

        @Override
        LogbackLoggerContext newLoggerContext(Class<?> loggerClass) {
            return forLogger(loggerClass);
        }

        @Override
        LogbackLoggerContext newRootLoggerContext() {
            return forRootLogger();
        }

        @Override
        String keyPrefix() {
            return LogbackLoggerContext.class.getSimpleName();
        }

        @Override
        String loggerName(Class<?> loggerClass) {
            return loggerClass.getName();
        }

        @Override
        String rootLoggerName() {
            return org.slf4j.Logger.ROOT_LOGGER_NAME;
        }
    }
}
