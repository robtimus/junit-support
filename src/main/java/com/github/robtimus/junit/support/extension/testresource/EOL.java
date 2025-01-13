/*
 * EOL.java
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

package com.github.robtimus.junit.support.extension.testresource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code EOL} can be used in combination with {@link TestResource} to specify a specific line separator to use.
 * This can be useful to create tests that work on different operating systems.
 * <p>
 * The line separator to use is looked up in the following order:
 * <ol>
 * <li>An {@link EOL} annotation on the field or parameter itself</li>
 * <li>For parameters, an {@link EOL} annotation on the constructor or method</li>
 * <li>An {@link EOL} annotation on the class defining the field, constructor or method</li>
 * <li>An {@link EOL} annotation on any declaring class</li>
 * <li>The <em>default</em> line separator that is defined via the {@value #DEFAULT_EOL_PROPERTY_NAME} <em>configuration parameter</em>, which can be
 *     supplied via the JUnit {@code Launcher} API, build tools (e.g., Gradle and Maven), a JVM system property, or the JUnit Platform configuration
 *     file (i.e., a file named {@code junit-platform.properties} in the root of the class path). Consult the JUnit User Guide for further
 *     information.
 *     <p>
 *     This configuration parameter can take a literal value, or one of the following pre-defined values:
 *     <ul>
 *     <li>{@code LF} for {@code \n}</li>
 *     <li>{@code CR} for {@code \r}</li>
 *     <li>{@code CRLF} for {@code \r\n}</li>
 *     <li>{@code SYSTEM} for {@link System#lineSeparator()}</li>
 *     <li>{@code ORIGINAL} for line separators from the original file; this is the default setting</li>
 *     <li>{@code NONE} for no line separators</li>
 *     </ul>
 * </ol>
 *
 * @author Rob Spoor
 * @since 2.0
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SuppressWarnings("nls")
public @interface EOL {

    /** A single line feed character ({@code \n}). */
    String LF = "\n";

    /** A single carriage return character ({@code \r\n}). */
    String CR = "\r";

    /** A carriage return followed by a line feed character ({@code \r\n}). */
    String CRLF = "\r\n";

    /** A marker that indicates the {@linkplain System#lineSeparator() system line separator} should be used. */
    String SYSTEM = "##system##";

    /**
     * A marker that indicates the line separators from the original file should be used.
     * This can be useful in case an enclosing member or class defines a different line separator,
     * or if a different line separator is defined via the {@value #DEFAULT_EOL_PROPERTY_NAME} configuration parameter.
     */
    String ORIGINAL = "##original##";

    /** No line separator. In other words, all lines are combined into one large line. */
    String NONE = "";

    /**
     * The line separator to use.
     */
    String value();

    /** The property that can be used to define the default line separator. */
    String DEFAULT_EOL_PROPERTY_NAME = "com.github.robtimus.junit.support.extension.testresource.lineSeparator";
}
