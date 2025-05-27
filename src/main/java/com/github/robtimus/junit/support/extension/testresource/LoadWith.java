/*
 * LoadWith.java
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

import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import com.github.robtimus.junit.support.extension.InjectionTarget;

/**
 * {@code LoadWith} can be used in combination with {@link TestResource} to specify a factory method to load the contents of a resource into an
 * object.
 * <p>
 * By default, any {@link AutoCloseable} or {@link CloseableResource} return value of the factory method is automatically closed once it goes out of
 * scope. To turn this off for {@link AutoCloseable}, set the {@value #CLOSE_AUTO_CLOSEABLE} <em>configuration parameter</em> to {@code false}.
 * This can be done via the JUnit {@code Launcher} API, build tools (e.g., Gradle and Maven), a JVM system property, or the JUnit Platform
 * configuration file (i.e., a file named {@code junit-platform.properties} in the root of the class path). Consult the JUnit User Guide for further
 * information.
 *
 * @author Rob Spoor
 * @since 2.0
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface LoadWith {

    /**
     * The name of the factory method within the test class or in an external class to use to load the contents of the resource into an object.
     * The method must take a single {@link InputStream} or {@link Reader} argument, and an optional {@link InjectionTarget} or {@link Class} argument
     * that represents the injection target or the target type respectively.
     * <p>
     * If no arguments are specified in the factory method name, the following parameter types are tried:
     * <ol>
     * <li>{@link Reader} and {@link InjectionTarget}</li>
     * <li>{@link Reader} and {@link Class}</li>
     * <li>{@link Reader}</li>
     * <li>{@link InputStream} and {@link InjectionTarget}</li>
     * <li>{@link InputStream} and {@link Class}</li>
     * <li>{@link InputStream}</li>
     * </ol>
     * <p>
     * A factory method within the test class must be static when used for static field or constructor parameter injection. It may be non-static when
     * used for instance field or method parameter injection.
     * <p>
     * A factory method in an external class must always be static, and must be referenced by <em>fully qualified method name</em>.
     * <p>
     * Examples:
     * <ul>
     * <li>{@code loadResource} for a method in the test class; the same as
     *     {@code loadResource(java.io.Reader, com.github.robtimus.junit.support.extension.InjectionTarget)} if it exists, otherwise
     *     {@code loadResource(java.io.Reader, java.lang.Class)} if it exists, otherwise {@code loadResource(java.io.Reader)} if it exists, otherwise
     *     {@code loadResource(java.io.InputStream, com.github.robtimus.junit.support.extension.InjectionTarget)} if it exists, otherwise
     *     {@code loadResource(java.io.InputStream, java.lang.Class)} if it exists, otherwise {@code loadResource(java.io.InputStream)}</li>
     * <li>{@code loadResource(java.io.Reader, com.github.robtimus.junit.support.extension.InjectionTarget)} for a method in the test class that takes
     *     {@link Reader} and {@link InjectionTarget} arguments</li>
     * <li>{@code loadResource(java.io.Reader, java.lang.Class)} for a method in the test class that takes {@link Reader} and {@link Class}
     *     arguments</li>
     * <li>{@code loadResource(java.io.Reader)} for a method in the test class that takes a single {@link Reader} argument</li>
     * <li>{@code loadResource(java.io.InputStream)} for a method in the test class that takes a single {@link Reader} argument</li>
     * <li>{@code com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toString(java.io.Reader)} for a method in an external
     *     class that takes a single {@link Reader} class</li>
     * <li>{@code com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toBytes(java.io.InputStream)} for a method in an
     *     external class that takes a single {@link InputStream} class</li>
     * </ul>
     */
    String value();

    /**
     * The property that can be used to enable or disable automatically closing {@link AutoCloseable} return values of factory methods.
     *
     * @since 3.1
     */
    @SuppressWarnings("nls")
    String CLOSE_AUTO_CLOSEABLE = "com.github.robtimus.junit.support.extension.testresource.closeAutoCloseable";
}
