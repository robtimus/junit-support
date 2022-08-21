/*
 * OptionalAssertions.java
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Supplier;

/**
 * A collection of utility methods that support asserting conditions in tests for {@link Optional}, {@link OptionalInt}, {@link OptionalLong} and
 * {@link OptionalDouble}.
 *
 * @author Rob Spoor
 * @since 2.0
 */
public final class OptionalAssertions {

    private OptionalAssertions() {
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param <T> The optional value type.
     * @param optional The optional to check.
     * @return The optional's value.
     */
    public static <T> T assertIsPresent(Optional<T> optional) {
        assertNotEquals(Optional.empty(), optional);
        return optional.get(); // NOSONAR
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param <T> The optional value type.
     * @param optional The optional to check.
     * @param message The failure message to fail with.
     * @return The optional's value.
     */
    public static <T> T assertIsPresent(Optional<T> optional, String message) {
        assertNotEquals(Optional.empty(), optional, message);
        return optional.get(); // NOSONAR
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param <T> The optional value type.
     * @param optional The optional to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The optional's value.
     */
    public static <T> T assertIsPresent(Optional<T> optional, Supplier<String> messageSupplier) {
        assertNotEquals(Optional.empty(), optional, messageSupplier);
        return optional.get(); // NOSONAR
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param optional The optional to check.
     * @return The optional's value.
     */
    public static int assertIsPresent(OptionalInt optional) {
        assertNotEquals(OptionalInt.empty(), optional);
        return optional.getAsInt();
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param optional The optional to check.
     * @param message The failure message to fail with.
     * @return The optional's value.
     */
    public static int assertIsPresent(OptionalInt optional, String message) {
        assertNotEquals(OptionalInt.empty(), optional, message);
        return optional.getAsInt();
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param optional The optional to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The optional's value.
     */
    public static int assertIsPresent(OptionalInt optional, Supplier<String> messageSupplier) {
        assertNotEquals(OptionalInt.empty(), optional, messageSupplier);
        return optional.getAsInt();
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param optional The optional to check.
     * @return The optional's value.
     */
    public static long assertIsPresent(OptionalLong optional) {
        assertNotEquals(OptionalLong.empty(), optional);
        return optional.getAsLong();
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param optional The optional to check.
     * @param message The failure message to fail with.
     * @return The optional's value.
     */
    public static long assertIsPresent(OptionalLong optional, String message) {
        assertNotEquals(OptionalLong.empty(), optional, message);
        return optional.getAsLong();
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param optional The optional to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The optional's value.
     */
    public static long assertIsPresent(OptionalLong optional, Supplier<String> messageSupplier) {
        assertNotEquals(OptionalLong.empty(), optional, messageSupplier);
        return optional.getAsLong();
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param optional The optional to check.
     * @return The optional's value.
     */
    public static double assertIsPresent(OptionalDouble optional) {
        assertNotEquals(OptionalDouble.empty(), optional);
        return optional.getAsDouble();
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param optional The optional to check.
     * @param message The failure message to fail with.
     * @return The optional's value.
     */
    public static double assertIsPresent(OptionalDouble optional, String message) {
        assertNotEquals(OptionalDouble.empty(), optional, message);
        return optional.getAsDouble();
    }

    /**
     * Asserts that the supplied optional is not empty.
     *
     * @param optional The optional to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The optional's value.
     */
    public static double assertIsPresent(OptionalDouble optional, Supplier<String> messageSupplier) {
        assertNotEquals(OptionalDouble.empty(), optional, messageSupplier);
        return optional.getAsDouble();
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     */
    public static void assertIsEmpty(Optional<?> optional) {
        assertEquals(Optional.empty(), optional);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     * @param message The failure message to fail with.
     */
    public static void assertIsEmpty(Optional<?> optional, String message) {
        assertEquals(Optional.empty(), optional, message);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertIsEmpty(Optional<?> optional, Supplier<String> messageSupplier) {
        assertEquals(Optional.empty(), optional, messageSupplier);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     */
    public static void assertIsEmpty(OptionalInt optional) {
        assertEquals(OptionalInt.empty(), optional);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     * @param message The failure message to fail with.
     */
    public static void assertIsEmpty(OptionalInt optional, String message) {
        assertEquals(OptionalInt.empty(), optional, message);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertIsEmpty(OptionalInt optional, Supplier<String> messageSupplier) {
        assertEquals(OptionalInt.empty(), optional, messageSupplier);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     */
    public static void assertIsEmpty(OptionalLong optional) {
        assertEquals(OptionalLong.empty(), optional);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     * @param message The failure message to fail with.
     */
    public static void assertIsEmpty(OptionalLong optional, String message) {
        assertEquals(OptionalLong.empty(), optional, message);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertIsEmpty(OptionalLong optional, Supplier<String> messageSupplier) {
        assertEquals(OptionalLong.empty(), optional, messageSupplier);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     */
    public static void assertIsEmpty(OptionalDouble optional) {
        assertEquals(OptionalDouble.empty(), optional);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     * @param message The failure message to fail with.
     */
    public static void assertIsEmpty(OptionalDouble optional, String message) {
        assertEquals(OptionalDouble.empty(), optional, message);
    }

    /**
     * Asserts that the supplied optional is empty.
     *
     * @param optional The optional to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertIsEmpty(OptionalDouble optional, Supplier<String> messageSupplier) {
        assertEquals(OptionalDouble.empty(), optional, messageSupplier);
    }
}
