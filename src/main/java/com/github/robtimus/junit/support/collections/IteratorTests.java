/*
 * IteratorTests.java
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
import static com.github.robtimus.junit.support.collections.CollectionUtils.toList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing separate {@link Iterator} functionalities.
 *
 * @author Rob Spoor
 * @param <T> The element type of the iterator to test.
 */
public interface IteratorTests<T> {

    /**
     * Returns an iterable that returns iterators to test.
     * <p>
     * This method will be called only once for each test. This makes it possible to initialize the iterable in a method annotated with
     * {@link BeforeEach}, and perform additional tests after the pre-defined test has finished.
     *
     * @return An iterable that returns iterators to test.
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
     * Contains tests for {@link Iterator#hasNext()} and {@link Iterator#next()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the iterator to test.
     */
    @DisplayName("iteration")
    interface IterationTests<T> extends IteratorTests<T> {

        @Test
        @DisplayName("iteration")
        default void testIteration() {
            Iterable<T> iterable = iterable();
            Iterator<T> iterator = iterable.iterator();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            while (iterator.hasNext()) {
                // call iterator.hasNext() again to make sure it doesn't have any unexpected side effects
                assertTrue(iterator.hasNext());

                T element = iterator.next();
                elements.add(element);
            }
            assertThrows(NoSuchElementException.class, iterator::next);

            assertHasElements(elements, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("next() without hasNext()")
        default void testNextWithoutHasNext() {
            Iterable<T> iterable = iterable();
            Iterator<T> iterator = iterable.iterator();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            for (int i = 0; i < expectedElements.size(); i++) {
                T element = iterator.next();
                elements.add(element);
            }
            assertThrows(NoSuchElementException.class, iterator::next);

            assertHasElements(elements, expectedElements, fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Iterator#remove()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the iterator to test.
     */
    @DisplayName("remove()")
    interface RemoveTests<T> extends IteratorTests<T> {

        @Test
        @DisplayName("remove() for every element")
        default void testRemoveEveryElement() {
            Iterable<T> iterable = iterable();
            Iterator<T> iterator = iterable.iterator();

            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }

            List<T> remaining = toList(iterable);
            assertHasElements(remaining, Collections.emptyList(), fixedOrder());
        }

        @Test
        @DisplayName("remove() for every even-indexed element")
        default void testRemoveEveryEvenIndexedElement() {
            Iterable<T> iterable = iterable();
            Iterator<T> iterator = iterable.iterator();

            List<T> expectedElements = new ArrayList<>(expectedElements());

            boolean remove = true;
            while (iterator.hasNext()) {
                T element = iterator.next();
                if (remove) {
                    expectedElements.remove(element);
                    iterator.remove();
                }
                remove = !remove;
            }

            List<T> remaining = toList(iterable);
            assertHasElements(remaining, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("remove() before next()")
        default void testRemoveBeforeNext() {
            Iterable<T> iterable = iterable();
            Iterator<T> iterator = iterable.iterator();

            assertThrows(IllegalStateException.class, iterator::remove);

            List<T> remaining = toList(iterable);
            assertHasElements(remaining, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("remove() after remove()")
        default void testRemoveAfterRemove() {
            Iterable<T> iterable = iterable();
            Iterator<T> iterator = iterable.iterator();

            List<T> expectedElements = new ArrayList<>(expectedElements());

            boolean remove = true;
            while (iterator.hasNext()) {
                T element = iterator.next();
                if (remove) {
                    expectedElements.remove(element);
                    iterator.remove();
                    assertThrows(IllegalStateException.class, iterator::remove);
                }
                remove = !remove;
            }

            List<T> remaining = toList(iterable);
            assertHasElements(remaining, expectedElements, fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Iterator#forEachRemaining(Consumer)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the iterator to test.
     */
    @DisplayName("forEachRemaining(Consumer)")
    interface ForEachRemainingTests<T> extends IteratorTests<T> {

        /**
         * Returns whether or not {@link Iterator#forEachRemaining(Consumer)} has a fail-fast {@code null} check.
         * This default implementation returns {@code true}.
         *
         * @return {@code true} if {@link Iterator#forEachRemaining(Consumer)} has a fail-fast {@code null} check, or {@code false} otherwise.
         */
        default boolean hasFailFastNullCheck() {
            return true;
        }

        @Test
        @DisplayName("forEachRemaining(Consumer)")
        default void testForEachRemaining() {
            Iterable<T> iterable = iterable();
            Iterator<T> iterator = iterable.iterator();

            List<T> expectedElements = new ArrayList<>(expectedElements());

            // skip the first 2 elements
            int skip = 2;
            while (skip > 0 && iterator.hasNext()) {
                T element = iterator.next();
                expectedElements.remove(element);
            }

            List<T> remaining = new ArrayList<>();
            iterator.forEachRemaining(remaining::add);

            assertHasElements(remaining, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("forEachRemaining(Consumer) with null consumer")
        default void testForEachRemainingWithNullConsumer() {
            Iterable<T> iterable = iterable();
            Iterator<T> iterator = iterable.iterator();

            boolean hasFailFastNullCheck = hasFailFastNullCheck();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            assertThrows(NullPointerException.class, () -> iterator.forEachRemaining(null));

            while (iterator.hasNext()) {
                T element = iterator.next();
                elements.add(element);

                // If there is no fail-fast null check, this call may succeed if there are no more elements
                if (iterator.hasNext() || hasFailFastNullCheck) {
                    assertThrows(NullPointerException.class, () -> iterator.forEachRemaining(null));
                }
            }

            if (!hasFailFastNullCheck) {
                // The first element, third element, etc. have been consumed by iterator.forEachRemaining(null)
                expectedElements = new ArrayList<>();
                boolean skip = true;
                for (T element : iterable) {
                    if (!skip) {
                        expectedElements.add(element);
                    }
                    skip = !skip;
                }
            }

            assertHasElements(elements, expectedElements, fixedOrder());
        }
    }
}
