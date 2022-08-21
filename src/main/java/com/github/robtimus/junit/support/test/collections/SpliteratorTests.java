/*
 * SpliteratorTests.java
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

package com.github.robtimus.junit.support.test.collections;

import static com.github.robtimus.junit.support.test.collections.CollectionAssertions.assertHasElements;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing separate {@link Spliterator} functionalities.
 *
 * @author Rob Spoor
 * @param <T> The element type of the spliterator to test.
 */
public interface SpliteratorTests<T> {

    /**
     * Returns an iterable that returns spliterators to test.
     * <p>
     * This method will be called only once for each test. This makes it possible to initialize the iterable in a method annotated with
     * {@link BeforeEach}, and perform additional tests after the pre-defined test has finished.
     *
     * @return An iterable that returns spliterators to test.
     */
    Iterable<T> iterable();

    /**
     * Returns the expected elements returned by the iterator to test.
     *
     * @return The expected elements returned by the iterator to test.
     */
    Collection<T> expectedElements();

    /**
     * Returns whether or not the iterator to test has a fixed order.
     *
     * @return {@code true} if the iterator to test has a fixed order, or {@code false} if the order is unspecified.
     */
    boolean fixedOrder();

    /**
     * Contains tests for {@link Spliterator#tryAdvance(Consumer)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the spliterator to test.
     */
    @DisplayName("tryAdvance(Consumer)")
    interface TryAdvanceTests<T> extends SpliteratorTests<T> {

        @Test
        @DisplayName("tryAdvance(Consumer)")
        default void testTryAdvance() {
            Iterable<T> iterable = iterable();
            Spliterator<T> spliterator = iterable.spliterator();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            for (int i = 0; i < expectedElements.size(); i++) {
                assertTrue(spliterator.tryAdvance(elements::add));
            }

            assertFalse(spliterator.tryAdvance(elements::add));

            assertHasElements(elements, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("tryAdvance(Consumer) with null consumer")
        default void testTryAdvanceWithNullConsumer() {
            Iterable<T> iterable = iterable();
            Spliterator<T> spliterator = iterable.spliterator();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            for (int i = 0; i < expectedElements.size(); i++) {
                assertThrows(NullPointerException.class, () -> spliterator.tryAdvance(null));
            }

            assertThrows(NullPointerException.class, () -> spliterator.tryAdvance(null));

            assertHasElements(elements, Collections.emptyList(), fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Spliterator#forEachRemaining(Consumer)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the iterator to test.
     */
    @DisplayName("forEachRemaining(Consumer)")
    interface ForEachRemainingTests<T> extends SpliteratorTests<T> {

        @Test
        @DisplayName("forEachRemaining(Consumer)")
        default void testForEachRemaining() {
            Iterable<T> iterable = iterable();
            Spliterator<T> spliterator = iterable.spliterator();

            List<T> expectedElements = new ArrayList<>(expectedElements());

            // skip the first 2 elements
            int skip = 2;
            while (skip > 0 && spliterator.tryAdvance(expectedElements::remove)) {
                // do nothing
            }

            List<T> remaining = new ArrayList<>();
            spliterator.forEachRemaining(remaining::add);

            assertHasElements(remaining, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("forEachRemaining(Consumer) with null consumer")
        default void testForEachRemainingWithNullConsumer() {
            Iterable<T> iterable = iterable();
            Spliterator<T> spliterator = iterable.spliterator();

            assertThrows(NullPointerException.class, () -> spliterator.forEachRemaining(null));

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            for (int i = 0; i < expectedElements.size(); i++) {
                assertTrue(spliterator.tryAdvance(elements::add));

                assertThrows(NullPointerException.class, () -> spliterator.forEachRemaining(null));
            }

            assertFalse(spliterator.tryAdvance(elements::add));

            assertThrows(NullPointerException.class, () -> spliterator.forEachRemaining(null));

            assertHasElements(elements, expectedElements, fixedOrder());
        }
    }
}
