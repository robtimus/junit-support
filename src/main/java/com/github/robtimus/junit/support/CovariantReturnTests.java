/*
 * CovariantReturnTests.java
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

package com.github.robtimus.junit.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Base interface for testing that methods return a specific sub type of the overridden method of the super class.
 * This can particularly be useful for testing that chainable method calls remain chainable in the sub class.
 *
 * @author Rob Spoor
 * @param <T> The type of object to test.
 * @since 1.1
 */
public interface CovariantReturnTests<T> {

    /**
     * Returns the type of object to test.
     *
     * @return The type of object to test.
     */
    Class<T> objectType();

    /**
     * Returns the covariant return type to test for. This default implementation returns {@link #objectType()}.
     *
     * @return The covariant return type to test for.
     */
    default Class<?> covariantReturnType() {
        return objectType();
    }

    /**
     * Returns the base return type to test against. This default implementation returns the super class of {@link #covariantReturnType()}.
     *
     * @return The base return type to test against.
     */
    default Class<?> baseReturnType() {
        return covariantReturnType().getSuperclass();
    }

    /**
     * Returns a stream of methods to test. This default implementation returns all public methods of the super class of {@link #objectType()} that
     * have {@link #baseReturnType()} as return type. Note that this includes static methods; those can be excluded by adding additional filtering:
     * <pre><code>
     * &#64;Override
     * public Stream&lt;Method&gt; methods() {
     *     return CovariantReturnTests.super.methods()
     *             .filter(m -&gt; !Modifier.isStatic(m.getModifiers()));
     * }
     * </code></pre>
     *
     * @return A stream of methods to test.
     */
    default Stream<Method> methods() {
        Class<T> objectType = objectType();
        Class<? super T> superType = objectType.getSuperclass();

        Class<?> baseReturnType = baseReturnType();

        return Arrays.stream(superType.getMethods())
                .filter(m -> baseReturnType.equals(m.getReturnType()));
    }

    /**
     * For each method returned by {@link #methods()}, test that the method is overridden by {@link #objectType()} and has
     * {@link #covariantReturnType()} as return type.
     *
     * @return A stream with the tests, one per method.
     */
    @TestFactory
    @DisplayName("covariant return types")
    @SuppressWarnings("nls")
    default Stream<DynamicContainer> testCovariantReturnTypes() {
        Class<T> objectType = objectType();
        Class<?> covariantReturnType = covariantReturnType();

        return methods()
                .map(m -> {
                    String overriddenOrHidden = Modifier.isStatic(m.getModifiers()) ? "hidden" : "overridden";
                    try {
                        Method override = objectType.getDeclaredMethod(m.getName(), m.getParameterTypes());
                        DynamicTest isOverridden = dynamicTest("is " + overriddenOrHidden, () -> { /* no body needed, test succeeds */ });
                        DynamicTest hasCovariantReturnType = dynamicTest("has covariant return type",
                                () -> assertEquals(covariantReturnType, override.getReturnType()));
                        return dynamicContainer(DisplayNameUtils.getMethodDisplayName(m), Arrays.asList(isOverridden, hasCovariantReturnType));

                    } catch (@SuppressWarnings("unused") NoSuchMethodException e) {
                        DynamicTest isOverridden = dynamicTest("is " + overriddenOrHidden, () -> fail("method is not " + overriddenOrHidden));
                        return dynamicContainer(DisplayNameUtils.getMethodDisplayName(m), Arrays.asList(isOverridden));
                    }
                });
    }
}
