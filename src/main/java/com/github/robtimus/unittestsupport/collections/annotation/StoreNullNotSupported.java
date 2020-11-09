/*
 * StoreNullNotSupported.java
 * Copyright 2020 Rob Spoor
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

package com.github.robtimus.unittestsupport.collections.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;

/**
 * Indicates that {@link Collection#add(Object)}, {@link List#add(int, Object)}, {@link List#set(int, Object)} and similar methods throw an exception
 * when called with {@code null}.
 *
 * @author Rob Spoor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StoreNullNotSupported {

    /**
     * The expected exception type thrown by {@link Collection#add(Object)}, {@link List#add(int, Object)}, {@link List#set(int, Object)} or similar
     * methods.
     */
    Class<? extends RuntimeException> expected() default NullPointerException.class;
}
