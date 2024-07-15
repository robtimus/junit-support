/*
 * Log4jNullAppender.java
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

import java.io.Serializable;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.NullAppender;
import org.apache.logging.log4j.core.config.Property;

/**
 * An appender that ignores events. This is much like {@link NullAppender}, except that {@link #append(LogEvent)} calls {@link #ignore(LogEvent)} with
 * {@linkplain LogEvent#toImmutable() immutable} events.
 *
 * @author Rob Spoor
 * @since 2.1
 */
@SuppressWarnings("exports") // the dependency is static, and therefore needs to be repeated anyway
public class Log4jNullAppender extends AbstractAppender {

    /**
     * Creates a new appender.
     *
     * @param name The appender name.
     * @param filter The filter to associate with the appender.
     * @param layout The layout to use to format the event.
     * @param ignoreExceptions If {@code true}, exceptions will be logged and suppressed.
     *                             If {@code false} errors will be logged and then passed to the application.
     * @param properties An array of properties for the appender.
     */
    protected Log4jNullAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    /**
     * Delegates to {@link #ignore(LogEvent)} with the result of calling {@link LogEvent#toImmutable()} on the given event.
     */
    @Override
    public final void append(LogEvent event) {
        LogEvent immutableEvent = event != null ? event.toImmutable() : null;
        ignore(immutableEvent);
    }

    /**
     * Ignores an event.
     *
     * @param event The event to ignore.
     */
    public void ignore(LogEvent event) {
        // does nothing
    }

    /**
     * Creates a named appender. This method does the same as calling {@code new Builder().setName(name).build()}.
     *
     * @param name The appender name.
     * @return The created appender.
     */
    public static Log4jNullAppender create(String name) {
        return new Builder().setName(name).build();
    }

    /**
     * A builder for {@link Log4jNullAppender} instances.
     *
     * @author Rob Spoor
     * @since 2.1
     */
    public static final class Builder extends AbstractAppender.Builder<Builder> {

        private boolean startAutomatically;

        /**
         * Creates a new builder.
         */
        public Builder() {
            setStartAutomatically(true);
        }

        /**
         * Sets whether or not to start appenders automatically when calling {@link #build()}. The default is {@code true}.
         *
         * @param startAutomatically {@code true} to start appenders automatically, or {@code false} otherwise.
         * @return This builder.
         */
        public Builder setStartAutomatically(boolean startAutomatically) {
            this.startAutomatically = startAutomatically;
            return this;
        }

        /**
         * Creates a new {@link Log4jNullAppender} instance.
         *
         * @return The created {@link Log4jNullAppender} instance.
         */
        public Log4jNullAppender build() {
            String name = getName();
            Filter filter = getFilter();
            Layout<? extends Serializable> layout = getLayout();
            boolean ignoreExceptions = isIgnoreExceptions();
            Property[] propertyArray = getPropertyArray();

            Log4jNullAppender appender = new Log4jNullAppender(name, filter, layout, ignoreExceptions, propertyArray);
            if (startAutomatically) {
                appender.start();
            }
            return appender;
        }
    }
}
