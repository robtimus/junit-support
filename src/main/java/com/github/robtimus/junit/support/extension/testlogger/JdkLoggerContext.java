/*
 * JdkLoggerContext.java
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

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import com.github.robtimus.junit.support.extension.testlogger.TestLoggerExtension.ContextFactory;

/**
 * {@code JdkLoggerContext} represents a JDK {@link Logger}. It can be injected using {@link TestLogger}, {@link TestLogger.ForClass} or
 * {@link TestLogger.Root}, and can be used to configure the logger for test purposes.
 *
 * @author Rob Spoor
 * @since 2.1
 */
@SuppressWarnings("exports") // the dependency is static, and therefore needs to be repeated anyway
public final class JdkLoggerContext extends LoggerContext {

    private static final String ROOT_LOGGER_NAME = ""; //$NON-NLS-1$

    private final Helper helper;

    private JdkLoggerContext(Logger logger) {
        helper = new Helper(logger);
    }

    static JdkLoggerContext forLogger(String name) {
        Logger logger = Logger.getLogger(name);
        return new JdkLoggerContext(logger);
    }

    static JdkLoggerContext forLogger(Class<?> c) {
        return forLogger(c.getName());
    }

    static JdkLoggerContext forRootLogger() {
        return forLogger(ROOT_LOGGER_NAME);
    }

    /**
     * Sets the new level for the logger.
     *
     * @param level The new level.
     * @return This object.
     * @throws NullPointerException If the given level is {@code null}.
     */
    public JdkLoggerContext setLevel(Level level) {
        Objects.requireNonNull(level);
        helper.setLevel(level);
        return this;
    }

    /**
     * Adds a handler to the logger.
     *
     * @param handler The handler to add.
     * @return This object.
     * @throws NullPointerException If the given handler is {@code null}.
     */
    public JdkLoggerContext addHandler(Handler handler) {
        Objects.requireNonNull(handler);
        helper.addAppender(handler);
        return this;
    }

    /**
     * Sets the single handler for the logger. All existing handlers will first be removed.
     *
     * @param handler The handler to set.
     * @return This object.
     * @throws NullPointerException If the given handler is {@code null}.
     */
    public JdkLoggerContext setHandler(Handler handler) {
        Objects.requireNonNull(handler);
        helper.setAppender(handler);
        return this;
    }

    /**
     * Removes a handler from the logger.
     *
     * @param handler The handler to remove.
     * @return This object.
     * @throws NullPointerException If the given handler is {@code null}.
     */
    public JdkLoggerContext removeHandler(Handler handler) {
        Objects.requireNonNull(handler);
        helper.removeAppender(handler);
        return this;
    }

    /**
     * Removes all handlers from the logger.
     *
     * @return This object.
     */
    public JdkLoggerContext removeHandlers() {
        helper.removeAppenders();
        return this;
    }

    /**
     * Removes all handlers from the logger that match a filter.
     *
     * @param filter The filter to use.
     * @return This object.
     * @throws NullPointerException If the given filter is {@code null}.
     */
    public JdkLoggerContext removeHandlers(Predicate<? super Handler> filter) {
        Objects.requireNonNull(filter);
        helper.removeAppenders(filter);
        return this;
    }

    /**
     * Sets whether or not to use parent handlers for the logger.
     *
     * @param useParentHandlers {@code true} to use parent handlers, {@code false} otherwise.
     * @return This object.
     */
    public JdkLoggerContext useParentHandlers(boolean useParentHandlers) {
        helper.useParentAppenders(useParentHandlers);
        return this;
    }

    /**
     * Returns an object that captures logged records. This can be used instead of having to append a capturing handler manually.
     *
     * @return An object that captures logged records.
     */
    public LogCaptor<LogRecord> capture() {
        return helper.logCaptor();
    }

    Stream<Handler> streamHandlers() {
        return helper.streamAppenders();
    }

    @Override
    void disable() {
        setLevel(Level.OFF);
    }

    @Override
    void saveSettings() {
        helper.saveSettings();
    }

    @Override
    public void restore() {
        helper.restore();
    }

    private static final class Helper extends LoggerContextHelper<Level, LogRecord, Handler> {

        private final Logger logger;

        private Handler captorHandler;
        private LogCaptor<LogRecord> logCaptor;

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
        Iterable<Handler> appenders() {
            return Arrays.asList(logger.getHandlers());
        }

        @Override
        void addAppender(Handler handler) {
            logger.addHandler(handler);
        }

        @Override
        void removeAppender(Handler handler, boolean force) {
            if (force || handler != captorHandler) {
                logger.removeHandler(handler);
            }
        }

        @Override
        boolean useParentAppenders() {
            return logger.getUseParentHandlers();
        }

        @Override
        void useParentAppenders(boolean useParentHandlers) {
            logger.setUseParentHandlers(useParentHandlers);
        }

        @Override
        LogCaptor<LogRecord> logCaptor() {
            if (logCaptor == null) {
                captorHandler = mock(Handler.class);
                logCaptor = () -> {
                    ArgumentCaptor<LogRecord> recordCaptor = ArgumentCaptor.forClass(LogRecord.class);
                    verify(captorHandler, atLeast(0)).publish(recordCaptor.capture());
                    return recordCaptor.getAllValues();
                };
                addAppender(captorHandler);
            }

            return logCaptor;
        }
    }

    static final class Factory extends ContextFactory<JdkLoggerContext> {

        @Override
        JdkLoggerContext newLoggerContext(String loggerName) {
            return forLogger(loggerName);
        }

        @Override
        JdkLoggerContext newLoggerContext(Class<?> loggerClass) {
            return forLogger(loggerClass);
        }

        @Override
        JdkLoggerContext newRootLoggerContext() {
            return forRootLogger();
        }

        @Override
        String keyPrefix() {
            return JdkLoggerContext.class.getSimpleName();
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
