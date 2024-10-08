/*
 * EnumerationTests.java
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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing separate {@link Enumeration} functionalities.
 *
 * @author Rob Spoor
 * @param <T> The element type of the enumeration to test.
 */
public interface EnumerationTests<T> {

    /**
     * Returns the enumeration to test.
     * <p>
     * This method will be called only once for each test. This makes it possible to initialize the enumeration in a method annotated with
     * {@link BeforeEach}, and perform additional tests after the pre-defined test has finished.
     *
     * @return The enumeration to test.
     */
    Enumeration<T> enumeration();

    /**
     * Returns the expected elements returned by the enumeration to test.
     *
     * @return The expected elements returned by the enumeration to test.
     */
    Collection<T> expectedElements();

    /**
     * Returns whether or not the enumeration to test has a fixed order.
     *
     * @return {@code true} if the enumeration to test has a fixed order, or {@code false} if the order is unspecified.
     */
    boolean fixedOrder();

    /**
     * Contains tests for {@link Enumeration#hasMoreElements()} and {@link Enumeration#nextElement()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the enumeration to test.
     */
    @DisplayName("iteration")
    interface IterationTests<T> extends EnumerationTests<T> {

        /**
         * Tests iteration using {@link Enumeration#hasMoreElements()} and {@link Enumeration#nextElement()}.
         */
        @Test
        @DisplayName("iteration")
        default void testIteration() {
            Enumeration<T> enumeration = enumeration();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            while (enumeration.hasMoreElements()) {
                // call enumeration.hasMoreElements() again to make sure it doesn't have any unexpected side effects
                assertTrue(enumeration.hasMoreElements());

                T element = enumeration.nextElement();
                elements.add(element);
            }
            assertThrows(NoSuchElementException.class, enumeration::nextElement);

            assertHasElements(elements, expectedElements, fixedOrder());
        }

        /**
         * Tests calling {@link Enumeration#nextElement()} without {@link Enumeration#hasMoreElements()}.
         * It calls {@link Enumeration#nextElement()} once for each element in {@link #expectedElements()}.
         */
        @Test
        @DisplayName("nextElement() without hasMoreElements()")
        default void testNextElementWithoutHasMoreElements() {
            Enumeration<T> enumeration = enumeration();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            for (int i = 0; i < expectedElements.size(); i++) {
                T element = enumeration.nextElement();
                elements.add(element);
            }
            assertThrows(NoSuchElementException.class, enumeration::nextElement);

            assertHasElements(elements, expectedElements, fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Enumeration#asIterator()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the enumeration to test.
     * @since 3.0
     */
    @DisplayName("asIterator()")
    interface AsIteratorTests<T> extends EnumerationTests<T> {

        /**
         * Tests iteration using {@link Enumeration#asIterator()}.
         */
        @Test
        @DisplayName("asIterator()")
        default void testAsIterator() {
            Enumeration<T> enumeration = enumeration();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            Iterator<T> iterator = enumeration.asIterator();

            while (iterator.hasNext()) {
                // call enumeration.hasMoreElements() to make sure iterator.hasNext() doesn't have any unexpected side effects
                assertTrue(enumeration.hasMoreElements());

                T element = iterator.next();
                elements.add(element);
            }
            assertFalse(enumeration.hasMoreElements());
            assertThrows(NoSuchElementException.class, iterator::next);

            assertHasElements(elements, expectedElements, fixedOrder());
        }

        /**
         * Tests calling {@link Iterator#next()} without {@link Iterator#hasNext()}.
         * It calls {@link Iterator#next()} once for each element in {@link #expectedElements()}.
         */
        @Test
        @DisplayName("next() without hasNext()")
        default void testNextWithoutHasNext() {
            Enumeration<T> enumeration = enumeration();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            Iterator<T> iterator = enumeration.asIterator();

            for (int i = 0; i < expectedElements.size(); i++) {
                T element = iterator.next();
                elements.add(element);
            }
            assertThrows(NoSuchElementException.class, iterator::next);

            assertHasElements(elements, expectedElements, fixedOrder());
        }
    }
}
