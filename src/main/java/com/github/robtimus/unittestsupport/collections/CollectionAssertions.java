/*
 * CollectionAssertions.java
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

package com.github.robtimus.unittestsupport.collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.hamcrest.Matcher;

/**
 * A collection of utility methods that support asserting conditions for collections and related types.
 *
 * @author Rob Spoor
 */
public final class CollectionAssertions {

    private CollectionAssertions() {
        throw new IllegalStateException("cannot create instances of " + getClass().getName()); //$NON-NLS-1$
    }

    /**
     * Asserts that a collection contains specific expected elements.
     *
     * @param collection The collection to check.
     * @param expected The expected elements.
     * @param fixedOrder {@code true} if the collection should contain the elements in the order specified by the expected collection,
     *                       or {@code false} if the order is unspecified
     */
    public static void assertHasElements(Collection<?> collection, Collection<?> expected, boolean fixedOrder) {
        if (expected.isEmpty()) {
            assertThat(collection, empty());
        } else if (fixedOrder) {
            assertEquals(asList(expected), asList(collection));
        } else if (collection instanceof Set<?> && expected instanceof Set<?>) {
            assertEquals(expected, collection);
        } else {
            assertThat(collection, containsInAnyOrder(expected.toArray()));
        }
    }

    private static List<?> asList(Collection<?> collection) {
        return collection instanceof List<?> ? (List<?>) collection : new ArrayList<>(collection);
    }

    /**
     * Asserts that an array contains specific expected elements.
     *
     * @param array The collection to check.
     * @param expected   The expected elements.
     * @param fixedOrder {@code true} if the array should contain the elements in the order specified by the expected collection,
     *                       or {@code false} if the order is unspecified
     */
    public static void assertHasElements(Object[] array, Collection<?> expected, boolean fixedOrder) {
        if (expected.isEmpty()) {
            assertThat(array, emptyArray());
        } else if (fixedOrder) {
            assertArrayEquals(expected.toArray(), array);
        } else {
            assertThat(array, arrayContainingInAnyOrder(expected.toArray()));
        }
    }

    /**
     * Asserts that an action either succeeds or throws an {@link UnsupportedOperationException}.
     * This can be used to test operations on unmodifiable collections that will not throw any exception if the collection is not actually updated.
     *
     * @param action The action to run.
     */
    public static void assertOptionallyThrowsUnsupportedOperationException(Runnable action) {
        try {
            action.run();
        } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
            // ignore
        }
    }

    /**
     * Asserts that an action throws an {@link UnsupportedOperationException} or another type of exception.
     * This can be used to test operations on unmodifiable collections that may throw another type of exception, such as {@link NullPointerException}.
     *
     * @param otherExpectedType The other possible expected exception type.
     * @param action The action to run.
     */
    public static void assertThrowsUnsupportedOperationExceptionOr(Class<? extends Throwable> otherExpectedType, Runnable action) {
        assertThrowsUnsupportedOperationExceptionOr(instanceOf(otherExpectedType), action);
    }

    /**
     * Asserts that an action throws an {@link UnsupportedOperationException} or another possible exception.
     * This can be used to test operations on unmodifiable collections that may throw another possible exception.
     *
     * @param otherExpectedMatcher An alternative matcher for the expected exception type.
     * @param action The action to run.
     */
    public static void assertThrowsUnsupportedOperationExceptionOr(Matcher<? super Throwable> otherExpectedMatcher, Runnable action) {
        Exception exception = assertThrows(Exception.class, action::run);
        assertThat(exception, either(otherExpectedMatcher).or(instanceOf(UnsupportedOperationException.class)));
    }
}
