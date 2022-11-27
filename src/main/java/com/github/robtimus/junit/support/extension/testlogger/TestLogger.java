/*
 * TestLogger.java
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Logger;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code TestLogger} can be used to annotate a field or a parameter in a lifecycle method or test method that should be resolved into a logging
 * context. This logging context can be used to easily set log levels and add or replace appenders.
 * <p>
 * The following logging contexts are supported:
 * <ul>
 * <li>{@link Reload4jLoggerContext} for <a href="https://reload4j.qos.ch/">reload4j</a> loggers</li>
 * <li>{@link LogbackLoggerContext} for <a href="https://logback.qos.ch/">logback</a> loggers</li>
 * <li>{@link Log4jLoggerContext} for <a href="https://logging.apache.org/log4j/2.x/">Log4j 2</a> loggers</li>
 * <li>{@link JdkLoggerContext} for {@link Logger} instances</li>
 * </ul>
 * <p>
 * Logger contexts will restore the original settings once they go out of scope. For logger contexts injected in instance fields or method parameter,
 * that will occur as soon as a test method ends. For logger contexts injected in static fields or constructor parameters, that may not occur as
 * often. If needed, the {@code restore} method of a logger context can be used to explicitly restore the original settings.
 *
 * @author Rob Spoor
 * @since 2.1
 */
@ExtendWith(TestLoggerExtension.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface TestLogger {

    /**
     * The name of the logger.
     */
    String value();

    /**
     * {@code TestLogger.ForClass} can be used instead of {@link TestLogger} to provide the logger name as a class.
     * This matches with the way loggers are often retrieved.
     *
     * @author Rob Spoor
     * @since 2.1
     */
    @ExtendWith(TestLoggerExtension.class)
    @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ForClass {

        /**
         * The class that acts as the name of the logger.
         */
        Class<?> value();
    }

    /**
     * {@code TestLogger.Root} can be used instead of {@link TestLogger} to inject a logging context for the root logger.
     *
     * @author Rob Spoor
     * @since 2.1
     */
    @ExtendWith(TestLoggerExtension.class)
    @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Root {

        // no properties
    }
}
