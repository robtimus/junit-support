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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code JdkLoggerContext} represents a JDK {@link Logger}. It can be injected using {@link TestLogger}, {@link TestLogger.ForClass} or
 * {@link TestLogger.Root}, and can be used to configure the logger for test purposes.
 *
 * @author Rob Spoor
 */
public final class JdkLoggerContext extends LoggerContext<Level, Handler> {

    private final Logger logger;

    private JdkLoggerContext(Logger logger) {
        this.logger = logger;
    }

    static JdkLoggerContext forLogger(String name) {
        Logger logger = Logger.getLogger(name);
        return new JdkLoggerContext(logger);
    }

    static JdkLoggerContext forLogger(Class<?> c) {
        return forLogger(c.getName());
    }

    static JdkLoggerContext forRootLogger() {
        return forLogger(""); //$NON-NLS-1$
    }

    /**
     * Returns the name of the logger.
     *
     * @return The name of the logger.
     */
    public String getName() {
        return logger.getName();
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
     * Adds a handler to the logger.
     *
     * @param handler The handler to add.
     * @return This object.
     * @throws NullPointerException If the given handler is {@code null}.
     */
    public JdkLoggerContext addHandler(Handler handler) {
        Objects.requireNonNull(handler);
        doAddAppender(handler);
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
        doSetAppender(handler);
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
        doRemoveAppender(handler);
        return this;
    }

    /**
     * Removes all handlers from the logger.
     *
     * @return This object.
     */
    public JdkLoggerContext removeHandlers() {
        doRemoveAppenders();
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
        doRemoveAppenders(filter);
        return this;
    }

    @Override
    Iterable<Handler> doListAppenders() {
        return Arrays.asList(logger.getHandlers());
    }

    @Override
    void doAddAppender(Handler handler) {
        logger.addHandler(handler);
    }

    @Override
    void doRemoveAppender(Handler handler) {
        logger.removeHandler(handler);
    }

    /**
     * Sets whether or not to use parent handlers for the logger.
     *
     * @param useParentHandlers {@code true} to use parent handlers, {@code false} otherwise.
     * @return This object.
     */
    public JdkLoggerContext useParentHandlers(boolean useParentHandlers) {
        doSetUseParentAppenders(useParentHandlers);
        return this;
    }

    @Override
    boolean doGetUseParentAppenders() {
        return logger.getUseParentHandlers();
    }

    @Override
    void doSetUseParentAppenders(boolean useParentHandlers) {
        logger.setUseParentHandlers(useParentHandlers);
    }
}
