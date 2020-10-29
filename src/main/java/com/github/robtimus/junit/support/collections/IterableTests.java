/*
 * IterableTests.java
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

package com.github.robtimus.junit.support.collections;

import static com.github.robtimus.junit.support.collections.CollectionAssertions.assertHasElements;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing separate {@link Iterable} functionalities.
 *
 * @author Rob Spoor
 * @param <T> The element type of the iterable to test.
 */
public interface IterableTests<T> {

    /**
     * Creates the iterable to test. This should be populated, i.e. not empty, unless the iterable can only be empty.
     *
     * @return The created iterable.
     */
    Iterable<T> createIterable();

    /**
     * Returns the expected elements contained by the iterable to test.
     * This should not be of the same type as the iterable to test, but preferably of some well-known iterable type like {@link ArrayList} or
     * {@link HashSet}.
     *
     * @return The expected elements contained by the iterable to test.
     */
    Collection<T> expectedElements();

    /**
     * Returns whether or not the iterable to test has a fixed order.
     *
     * @return {@code true} if the iterable to test has a fixed order, or {@code false} if the order is unspecified.
     */
    boolean fixedOrder();

    /**
     * Contains tests for {@link Iterable#forEach(Consumer)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the iterable to test.
     */
    @DisplayName("forEach(Consumer)")
    interface ForEachTests<T> extends IterableTests<T> {

        @Test
        @DisplayName("forEach(Consumer)")
        default void testForEach() {
            Iterable<T> iterable = createIterable();

            List<T> elements = new ArrayList<>();
            iterable.forEach(elements::add);

            assertHasElements(elements, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("forEach(Consumer) with null consumer")
        default void testForEachWithNullConsumer() {
            Iterable<T> iterable = createIterable();

            assertThrows(NullPointerException.class, () -> iterable.forEach(null));
        }
    }
}
