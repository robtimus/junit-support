/*
 * DisableLogging.java
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

package com.github.robtimus.junit.support.extension.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code DisableLogging} can be used to disable logging for the duration of a test.
 * <p>
 * To disable logging, add a logger field (static or not) to your test class and annotate it with {@code DisableLogging}.
 * The following types of loggers are supported:
 * <ul>
 * <li>{@link java.util.logging.Logger}, usually created using {@link java.util.logging.LogManager#getLogger(String)}.</li>
 * <li>{@link org.apache.logging.log4j.core.Logger} (from <a href="https://logging.apache.org/log4j/2.x/">Log4j 2</a>), usually created using
 *     {@link org.apache.logging.log4j.LogManager#getLogger(String)} or {@link org.apache.logging.log4j.LogManager#getLogger(Class)}.
 *     Note that this requires {@code log4j-core} as implementation.</li>
 * <li>{@link org.apache.log4j.Logger} (from <a href="https://reload4j.qos.ch/">reload4j</a> or Log4j 1.x), usually created using
 *     {@link org.apache.log4j.Logger#getLogger(String)} or {@link org.apache.log4j.Logger#getLogger(Class)}.</li>
 * <li>{@link ch.qos.logback.classic.Logger} (from <a href="https://logback.qos.ch/">logback</a>), usually created using
 *     {@link org.slf4j.LoggerFactory#getLogger(String)} or {@link org.slf4j.LoggerFactory#getLogger(Class)}.
 *     Note that this requires {@code logback} as single SLF4J implementation.</li>
 * </ul>
 *
 * @author Rob Spoor
 * @since 3.0
 */
@ExtendWith(DisableLoggingExtension.class)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DisableLogging {
    // no content
}
