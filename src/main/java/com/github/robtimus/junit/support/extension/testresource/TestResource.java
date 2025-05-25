/*
 * TestResource.java
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code TestResource} can be used to annotate a field or a parameter in a lifecycle method or test method that should be resolved into the contents
 * of a (test) resource. The resource will be loaded relative to the class where the field, constructor or method is defined. The following field /
 * parameter types are supported by default:
 * <ul>
 * <li>{@link String}</li>
 * <li>{@link CharSequence}</li>
 * <li>{@link StringBuilder}</li>
 * <li>{@code byte[]}</li>
 * <li>{@link InputStream}</li>
 * <li>{@link BufferedInputStream}</li>
 * <li>{@link Reader}</li>
 * <li>{@link BufferedReader}</li>
 * </ul>
 * When the type is {@link InputStream}, {@link BufferedInputStream}, {@link Reader} or {@link BufferedReader} the contents can only be read once.
 * It is therefore advised to only use this for test method parameters.
 * When the injected stream or reader goes out of scope it will be automatically closed.
 * <p>
 * When the type is not {@code byte[]}, {@link InputStream} or {@link BufferedInputStream}, {@link Encoding} can be used to change the encoding to use
 * (defaults to UTF-8).
 * <p>
 * In addition, {@link LoadWith} can be used to specify a method that is used to load the contents of the resource into an object, or {@link EOL} can
 * be used to define the line separator to use for {@code String}, {@code CharSequence} and {@code StringBuilder}. This can be useful to create tests
 * that work on different operating systems.
 * <p>
 * It is illegal to:
 * <ul>
 * <li>use {@link EOL} for automatic loading to {@code byte[]}, {@link InputStream}, {@link BufferedInputStream}, {@link Reader} or
 *     {@link BufferedReader}</li>
 * <li>use {@link EOL} in combination with {@link LoadWith}</li>
 * <li>use {@link Encoding} in combination with {@link LoadWith} when {@link LoadWith} defines a method that uses an {@link InputStream}</li>
 * <li>use {@link Encoding} for automatic loading to {@code byte[]}, {@link InputStream} or {@link BufferedInputStream}</li>
 * </ul>
 *
 * @author Rob Spoor
 * @since 2.0
 */
@ExtendWith(TestResourceExtension.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface TestResource {

    /**
     * The resource to load.
     */
    String value();
}
