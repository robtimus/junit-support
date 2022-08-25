/*
 * PredicateAssertions.java
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

import static com.github.robtimus.junit.support.AssertionFailedErrorBuilder.assertionFailedError;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;

/**
 * A collection of utility methods that support asserting conditions on objects using predicates. These methods are like
 * {@link Assertions#assertTrue(boolean)} and {@link Assertions#assertFalse(boolean)}, but use an <em>actual</em> object that's not always
 * {@code true} or {@code false}. As a result, the assertions in this class provide more information about <em>why</em> assertions fail.
 *
 * @author Rob Spoor
 * @since 2.0
 */
@SuppressWarnings("nls")
public final class PredicateAssertions {

    private PredicateAssertions() {
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param <T> The value type.
     * @param matcher The predicate to use.
     * @param actual The value to check.
     */
    public static <T> void assertMatches(Predicate<? super T> matcher, T actual) {
        assertMatches(matcher, actual, (Object) null);
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param <T> The value type.
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param message The failure message to fail with.
     */
    public static <T> void assertMatches(Predicate<? super T> matcher, T actual, String message) {
        assertMatches(matcher, actual, (Object) message);
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param <T> The value type.
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static <T> void assertMatches(Predicate<? super T> matcher, T actual, Supplier<String> messageSupplier) {
        assertMatches(matcher, actual, (Object) messageSupplier);
    }

    private static <T> void assertMatches(Predicate<? super T> matcher, T actual, Object messageOrSupplier) {
        if (!matcher.test(actual)) {
            notMatching(actual, messageOrSupplier).buildAndThrow();
        }
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     */
    public static void assertIntMatches(IntPredicate matcher, int actual) {
        assertIntMatches(matcher, actual, (Object) null);
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param message The failure message to fail with.
     */
    public static void assertIntMatches(IntPredicate matcher, int actual, String message) {
        assertIntMatches(matcher, actual, (Object) message);
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertIntMatches(IntPredicate matcher, int actual, Supplier<String> messageSupplier) {
        assertIntMatches(matcher, actual, (Object) messageSupplier);
    }

    private static void assertIntMatches(IntPredicate matcher, int actual, Object messageOrSupplier) {
        if (!matcher.test(actual)) {
            notMatching(actual, messageOrSupplier).buildAndThrow();
        }
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     */
    public static void assertLongMatches(LongPredicate matcher, long actual) {
        assertLongMatches(matcher, actual, (Object) null);
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param message The failure message to fail with.
     */
    public static void assertLongMatches(LongPredicate matcher, long actual, String message) {
        assertLongMatches(matcher, actual, (Object) message);
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertLongMatches(LongPredicate matcher, long actual, Supplier<String> messageSupplier) {
        assertLongMatches(matcher, actual, (Object) messageSupplier);
    }

    private static void assertLongMatches(LongPredicate matcher, long actual, Object messageOrSupplier) {
        if (!matcher.test(actual)) {
            notMatching(actual, messageOrSupplier).buildAndThrow();
        }
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     */
    public static void assertDoubleMatches(DoublePredicate matcher, double actual) {
        assertDoubleMatches(matcher, actual, (Object) null);
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param message The failure message to fail with.
     */
    public static void assertDoubleMatches(DoublePredicate matcher, double actual, String message) {
        assertDoubleMatches(matcher, actual, (Object) message);
    }

    /**
     * Asserts that the supplied value matches the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertDoubleMatches(DoublePredicate matcher, double actual, Supplier<String> messageSupplier) {
        assertDoubleMatches(matcher, actual, (Object) messageSupplier);
    }

    private static void assertDoubleMatches(DoublePredicate matcher, double actual, Object messageOrSupplier) {
        if (!matcher.test(actual)) {
            notMatching(actual, messageOrSupplier).buildAndThrow();
        }
    }

    private static AssertionFailedErrorBuilder notMatching(Object actual, Object messageOrSupplier) {
        return assertionFailedError()
                .message(messageOrSupplier)
                .expectedMessage("matching predicate")
                .actual(actual);
    }

    /**
     * Asserts that the supplied value does not match the supplied predicate.
     *
     * @param <T> The value type.
     * @param matcher The predicate to use.
     * @param actual The value to check.
     */
    public static <T> void assertDoesNotMatch(Predicate<? super T> matcher, T actual) {
        assertDoesNotMatch(matcher, actual, (Object) null);
    }

    /**
     * Asserts that the supplied value does not match  the supplied predicate.
     *
     * @param <T> The value type.
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param message The failure message to fail with.
     */
    public static <T> void assertDoesNotMatch(Predicate<? super T> matcher, T actual, String message) {
        assertDoesNotMatch(matcher, actual, (Object) message);
    }

    /**
     * Asserts that the supplied value does not match  the supplied predicate.
     *
     * @param <T> The value type.
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static <T> void assertDoesNotMatch(Predicate<? super T> matcher, T actual, Supplier<String> messageSupplier) {
        assertDoesNotMatch(matcher, actual, (Object) messageSupplier);
    }

    private static <T> void assertDoesNotMatch(Predicate<? super T> matcher, T actual, Object messageOrSupplier) {
        if (matcher.test(actual)) {
            matching(actual, messageOrSupplier).buildAndThrow();
        }
    }

    /**
     * Asserts that the supplied value does not match the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     */
    public static void assertIntDoesNotMatch(IntPredicate matcher, int actual) {
        assertIntDoesNotMatch(matcher, actual, (Object) null);
    }

    /**
     * Asserts that the supplied value does not match  the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param message The failure message to fail with.
     */
    public static void assertIntDoesNotMatch(IntPredicate matcher, int actual, String message) {
        assertIntDoesNotMatch(matcher, actual, (Object) message);
    }

    /**
     * Asserts that the supplied value does not match  the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertIntDoesNotMatch(IntPredicate matcher, int actual, Supplier<String> messageSupplier) {
        assertIntDoesNotMatch(matcher, actual, (Object) messageSupplier);
    }

    private static void assertIntDoesNotMatch(IntPredicate matcher, int actual, Object messageOrSupplier) {
        if (matcher.test(actual)) {
            matching(actual, messageOrSupplier).buildAndThrow();
        }
    }

    /**
     * Asserts that the supplied value does not match the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     */
    public static void assertLongDoesNotMatch(LongPredicate matcher, long actual) {
        assertLongDoesNotMatch(matcher, actual, (Object) null);
    }

    /**
     * Asserts that the supplied value does not match  the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param message The failure message to fail with.
     */
    public static void assertLongDoesNotMatch(LongPredicate matcher, long actual, String message) {
        assertLongDoesNotMatch(matcher, actual, (Object) message);
    }

    /**
     * Asserts that the supplied value does not match  the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertLongDoesNotMatch(LongPredicate matcher, long actual, Supplier<String> messageSupplier) {
        assertLongDoesNotMatch(matcher, actual, (Object) messageSupplier);
    }

    private static void assertLongDoesNotMatch(LongPredicate matcher, long actual, Object messageOrSupplier) {
        if (matcher.test(actual)) {
            matching(actual, messageOrSupplier).buildAndThrow();
        }
    }

    /**
     * Asserts that the supplied value does not match the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     */
    public static void assertDoubleDoesNotMatch(DoublePredicate matcher, double actual) {
        assertDoubleDoesNotMatch(matcher, actual, (Object) null);
    }

    /**
     * Asserts that the supplied value does not match  the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param message The failure message to fail with.
     */
    public static void assertDoubleDoesNotMatch(DoublePredicate matcher, double actual, String message) {
        assertDoubleDoesNotMatch(matcher, actual, (Object) message);
    }

    /**
     * Asserts that the supplied value does not match  the supplied predicate.
     *
     * @param matcher The predicate to use.
     * @param actual The value to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertDoubleDoesNotMatch(DoublePredicate matcher, double actual, Supplier<String> messageSupplier) {
        assertDoubleDoesNotMatch(matcher, actual, (Object) messageSupplier);
    }

    private static void assertDoubleDoesNotMatch(DoublePredicate matcher, double actual, Object messageOrSupplier) {
        if (matcher.test(actual)) {
            matching(actual, messageOrSupplier).buildAndThrow();
        }
    }

    private static AssertionFailedErrorBuilder matching(Object actual, Object messageOrSupplier) {
        return assertionFailedError()
                .message(messageOrSupplier)
                .expectedMessage("not matching predicate")
                .actual(actual);
    }
}
