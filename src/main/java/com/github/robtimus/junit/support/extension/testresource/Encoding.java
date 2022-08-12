/*
 * Encoding.java
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
import java.nio.charset.Charset;

/**
 * {@code Encoding} can be used in combination with {@link TestResource} to specify a specific encoding or charset to use.
 * <p>
 * The encoding to use is looked up in the following order:
 * <ol>
 * <li>An {@link Encoding} annotation on the field or parameter itself</li>
 * <li>For parameters, an {@link Encoding} annotation on the constructor or method</li>
 * <li>An {@link Encoding} annotation on the class defining the field, constructor or method</li>
 * <li>An {@link Encoding} annotation on any declaring class</li>
 * <li>The <em>default</em> encoding that is defined via the {@value #DEFAULT_ENCODING_PROPERTY_NAME} <em>configuration parameter</em>, which can be
 *     supplied via the JUnit {@code Launcher} API, build tools (e.g., Gradle and Maven), a JVM system property, or the JUnit Platform configuration
 *     file (i.e., a file named {@code junit-platform.properties} in the root of the class path). Consult the JUnit User Guide for further
 *     information.
 *     <p>
 *     This configuration parameter can take a literal value, or one of the following pre-defined values:
 *     <ul>
 *     <li>{@code DEFAULT} for {@link Charset#defaultCharset()}</li>
 *     <li>{@code SYSTEM} for system property {@code file.encoding}</li>
 *     <li>{@code NATIVE} for system property {@code native.encoding} (since Java 17)</li>
 *     </ul>
 *     The default is {@code UTF-8}.
 * </ol>
 *
 * @author Rob Spoor
 * @since 2.0
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SuppressWarnings("nls")
public @interface Encoding {

    /** A marker that indicates the {@linkplain Charset#defaultCharset() default charset} should be used. */
    String DEFAULT = "##default##";

    /** A marker that indicates the value from the {@code file.encoding} system property should be used. */
    String SYSTEM = "##system##";

    /** A marker that indicates the value from the {@code native.encoding} system property should be used. */
    String NATIVE = "##native##";

    /**
     * The encoding to use.
     */
    String value();

    /** The property that can be used to define the default encoding. */
    String DEFAULT_ENCODING_PROPERTY_NAME = "com.github.robtimus.junit.support.extension.testresource.encoding";
}
