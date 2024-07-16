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

package com.github.robtimus.junit.support.extension.testlogger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * {@code DisableLogging} can be used in combination with {@link TestLogger} to disable logging for the logger.
 * This is often equivalent to setting the level to {@code OFF}.
 * <p>
 * While this annotation doesn't add much value for logger contexts injected as method parameters, for logger contexts injected as fields this
 * annotation can remove the need for a method annotated with {@link BeforeAll} or {@link BeforeEach}.
 *
 * @author Rob Spoor
 * @since 3.0
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DisableLogging {
    // no content
}
