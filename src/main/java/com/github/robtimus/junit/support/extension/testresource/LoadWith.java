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

/**
 * {@code LoadWith} can be used in combination with {@link TestResource} to specify a factory method to load the contents of a resource into an
 * object.
 *
 * @author Rob Spoor
 * @since 2.0
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface LoadWith {

    /**
     * The name of the factory method within the test class or in an external class to use to load the contents of the resource into an object.
     * The method must take a single {@link InputStream} or {@link Reader} argument. If no arguments are specified in the factory method name,
     * a method with a {@link Reader} parameter is tried first, then a method with an {@link InputStream} parameter.
     * <p>
     * A factory method within the test class must be static when used for static field or constructor parameter injection. It may be non-static when
     * used for instance field or method parameter injection.
     * <p>
     * A factory method in an external class must always be static, and must be referenced by <em>fully qualified method name</em>.
     * <p>
     * Examples:
     * <ul>
     * <li>{@code loadResource} for a method in the test class; the same as {@code loadResource(Reader)} if it exists, otherwise
     *     {@code loadResource(InputStream)}</li>
     * <li>{@code loadResource(Reader)} for a method in the test class that takes a single {@link Reader} argument</li>
     * <li>{@code loadResource(InputStream)} for a method in the test class that takes a single {@link Reader} argument</li>
     * <li>{@code com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toString(Reader)} for a method in an external class
     *     that takes a single {@link Reader} class</li>
     * <li>{@code com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toBytes(InputStream)} for a method in an external class
     *     that takes a single {@link InputStream} class</li>
     * </ul>
     */
    String value();
}
