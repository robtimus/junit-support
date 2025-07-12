/*
 * ListIteratorTests.java
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import com.github.robtimus.junit.support.test.collections.annotation.StoreNullNotSupported;

/**
 * Base interface for testing separate {@link ListIterator} functionalities.
 *
 * @author Rob Spoor
 * @param <T> The element type of the list iterator to test.
 */
public interface ListIteratorTests<T> extends IteratorTests<T> {

    @Override
    List<T> iterable();

    @Override
    List<T> expectedElements();

    /**
     * {@inheritDoc}
     * Always returns {@code true}.
     */
    @Override
    default boolean fixedOrder() {
        return true;
    }

    /**
     * Contains tests for {@link ListIterator#hasNext()}, {@link ListIterator#next()}, {@link ListIterator#nextIndex()},
     * {@link ListIterator#hasPrevious()}, {@link ListIterator#previous()} and {@link ListIterator#previousIndex()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list iterator to test.
     */
    @DisplayName("iteration")
    interface IterationTests<T> extends ListIteratorTests<T> {

        @Test
        @DisplayName("iteration using next()")
        default void testIterationUsingNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            int index = 0;
            while (iterator.hasNext()) {
                // call iterator.hasNext() again to make sure it doesn't have any unexpected side effects
                assertTrue(iterator.hasNext());

                assertEquals(index, iterator.nextIndex());

                T element = iterator.next();
                elements.add(element);

                index++;
            }
            assertThrows(NoSuchElementException.class, iterator::next);
            assertEquals(index, iterator.nextIndex());

            assertEquals(expectedElements, elements);
        }

        @Test
        @DisplayName("iteration using previous()")
        default void testIterationUsingPrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            int index = list.size() - 1;
            while (iterator.hasPrevious()) {
                // call iterator.hasPrevous() again to make sure it doesn't have any unexpected side effects
                assertTrue(iterator.hasPrevious());

                assertEquals(index, iterator.previousIndex());

                T element = iterator.previous();
                elements.add(0, element);

                index--;
            }
            assertThrows(NoSuchElementException.class, iterator::previous);
            assertEquals(index, iterator.previousIndex());

            assertEquals(expectedElements, elements);
        }

        @Test
        @DisplayName("next() without hasNext()")
        default void testNextWithoutHasNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            for (int i = 0; i < expectedElements.size(); i++) {
                T element = iterator.next();
                elements.add(element);
            }
            assertThrows(NoSuchElementException.class, iterator::next);

            assertEquals(expectedElements, elements);
        }

        @Test
        @DisplayName("previous() without hasPrevious()")
        default void testPreviousWithoutHasPrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements.size());

            for (int i = 0; i < expectedElements.size(); i++) {
                T element = iterator.previous();
                elements.add(0, element);
            }
            assertThrows(NoSuchElementException.class, iterator::previous);

            assertEquals(expectedElements, elements);
        }
    }

    /**
     * Contains tests for {@link ListIterator#remove()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list iterator to test.
     */
    @DisplayName("remove()")
    interface RemoveTests<T> extends ListIteratorTests<T> {

        @Test
        @DisplayName("remove() for every element using next()")
        default void testRemoveEveryElementUsingNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }

            assertEquals(Collections.emptyList(), list);
        }

        @Test
        @DisplayName("remove() for every even-indexed element using next()")
        default void testRemoveEveryEvenIndexedElementUsingNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

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

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("remove() before next()")
        default void testRemoveBeforeNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            assertThrows(IllegalStateException.class, iterator::remove);

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("remove() after remove() using next()")
        default void testRemoveAfterRemoveUsingNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

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

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("remove() for every element using previous()")
        default void testRemoveEveryElementUsingPrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            while (iterator.hasPrevious()) {
                iterator.previous();
                iterator.remove();
            }

            assertEquals(Collections.emptyList(), list);
        }

        @Test
        @DisplayName("remove() for every even-indexed element using previous()")
        default void testRemoveEveryEvenIndexedElementUsingPrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            List<T> expectedElements = new ArrayList<>(expectedElements());

            boolean remove = true;
            while (iterator.hasPrevious()) {
                T element = iterator.previous();
                if (remove) {
                    expectedElements.remove(element);
                    iterator.remove();
                }
                remove = !remove;
            }

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("remove() before previous()")
        default void testRemoveBeforePrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            assertThrows(IllegalStateException.class, iterator::remove);

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("remove() after remove() using previous()")
        default void testRemoveAfterRemoveUsingPrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            List<T> expectedElements = new ArrayList<>(expectedElements());

            boolean remove = true;
            while (iterator.hasPrevious()) {
                T element = iterator.previous();
                if (remove) {
                    expectedElements.remove(element);
                    iterator.remove();
                    assertThrows(IllegalStateException.class, iterator::remove);
                }
                remove = !remove;
            }

            assertEquals(expectedElements, list);
        }
    }

    /**
     * Contains tests for {@link ListIterator#set(Object)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list iterator to test.
     */
    @DisplayName("set(Object)")
    interface SetTests<T> extends ListIteratorTests<T> {

        /**
         * Returns a unary operator that can be used to create new values to set with {@link ListIterator#set(Object)}.
         *
         * @return A unary operator that can be used to create new values to set with {@link ListIterator#set(Object)}.
         */
        UnaryOperator<T> replaceElementOperator();

        /**
         * Returns a single element. This is used by {@link #testSetBeforeNext()} and {@link #testSetBeforePrevious()}.
         * This default implementation returns the middle element of {@link #expectedElements()}.
         *
         * @return A single element to set.
         */
        default T singleElement() {
            List<T> expectedElements = expectedElements();
            return expectedElements.get(expectedElements.size() / 2);
        }

        @Test
        @DisplayName("set(Object) using next()")
        default void testSetUsingNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            UnaryOperator<T> operator = replaceElementOperator();

            while (iterator.hasNext()) {
                T element = iterator.next();

                iterator.set(operator.apply(element));
            }

            List<T> expectedElements = new ArrayList<>(expectedElements());
            expectedElements.replaceAll(operator);

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("set(Object) with null replacement using next()")
        default void testSetWithNullReplacementUsingNext(TestInfo testInfo) {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            while (iterator.hasNext()) {
                iterator.next();

                if (annotation == null) {
                    iterator.set(null);
                } else {
                    assertThrows(annotation.expected(), () -> iterator.set(null));
                }
            }

            if (annotation == null) {
                List<T> expectedElements = new ArrayList<>(expectedElements());
                Collections.fill(expectedElements, null);

                assertEquals(expectedElements, list);
            } else {
                assertEquals(expectedElements(), list);
            }
        }

        @Test
        @DisplayName("set(Object) before next()")
        default void testSetBeforeNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            T element = singleElement();

            assertThrows(IllegalStateException.class, () -> iterator.set(element));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("set(Object) after set(Object) using next()")
        default void testSetAfterSetUsingNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            UnaryOperator<T> operator = replaceElementOperator();

            while (iterator.hasNext()) {
                T element = iterator.next();

                iterator.set(operator.apply(element));
                iterator.set(operator.apply(operator.apply(element)));
            }

            List<T> expectedElements = new ArrayList<>(expectedElements());
            expectedElements.replaceAll(operator);
            expectedElements.replaceAll(operator);

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("set(Object) using previous()")
        default void testSetUsingPrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            UnaryOperator<T> operator = replaceElementOperator();

            while (iterator.hasPrevious()) {
                T element = iterator.previous();

                iterator.set(operator.apply(element));
            }

            List<T> expectedElements = new ArrayList<>(expectedElements());
            expectedElements.replaceAll(operator);

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("set(Object) with null replacement using previous()")
        default void testSetWithNullReplacementUsingPrevious(TestInfo testInfo) {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            while (iterator.hasPrevious()) {
                iterator.previous();

                if (annotation == null) {
                    iterator.set(null);
                } else {
                    assertThrows(annotation.expected(), () -> iterator.set(null));
                }
            }

            if (annotation == null) {
                List<T> expectedElements = new ArrayList<>(expectedElements());
                Collections.fill(expectedElements, null);

                assertEquals(expectedElements, list);
            } else {
                assertEquals(expectedElements(), list);
            }
        }

        @Test
        @DisplayName("set(Object) before previous()")
        default void testSetBeforePrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            T element = singleElement();

            assertThrows(IllegalStateException.class, () -> iterator.set(element));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("set(Object) after set(Object) using previous()")
        default void testSetAfterSetUsingPrevous() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            UnaryOperator<T> operator = replaceElementOperator();

            while (iterator.hasPrevious()) {
                T element = iterator.previous();

                iterator.set(operator.apply(element));
                iterator.set(operator.apply(operator.apply(element)));
            }

            List<T> expectedElements = new ArrayList<>(expectedElements());
            expectedElements.replaceAll(operator);
            expectedElements.replaceAll(operator);

            assertEquals(expectedElements, list);
        }
    }

    /**
     * Contains tests for {@link ListIterator#add(Object)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list iterator to test.
     */
    @DisplayName("add(Object)")
    interface AddTests<T> extends ListIteratorTests<T> {

        /**
         * Returns a new element to be added to the list. This element should not be an element of {@link #expectedElements()}.
         *
         * @return A new element to be added to the list.
         */
        T newElement();

        @Test
        @DisplayName("add(Object) using next()")
        default void testAddUsingNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            T newElement = newElement();

            iterator.add(newElement);

            while (iterator.hasNext()) {
                iterator.next();
                iterator.add(newElement);
            }

            List<T> expectedElements = new ArrayList<>(expectedElements());
            for (int i = expectedElements.size(); i >= 0; i--) {
                expectedElements.add(i, newElement);
            }

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("add(Object) with null using next()")
        default void testAddNullUsingNext(TestInfo testInfo) {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            if (annotation == null) {
                iterator.add(null);
            } else {
                assertThrows(annotation.expected(), () -> iterator.add(null));
            }

            while (iterator.hasNext()) {
                iterator.next();

                if (annotation == null) {
                    iterator.add(null);
                } else {
                    assertThrows(annotation.expected(), () -> iterator.add(null));
                }
            }

            if (annotation == null) {
                List<T> expectedElements = new ArrayList<>(expectedElements());
                for (int i = expectedElements.size(); i >= 0; i--) {
                    expectedElements.add(i, null);
                }

                assertEquals(expectedElements, list);
            } else {
                assertEquals(expectedElements(), list);
            }
        }

        @Test
        @DisplayName("add(Object) using previous()")
        default void testAddUsingPrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            T newElement = newElement();

            iterator.add(newElement);

            // previous must return the newly added element
            assertTrue(iterator.hasPrevious());
            assertEquals(newElement, iterator.previous());

            while (iterator.hasPrevious()) {
                iterator.previous();

                iterator.add(newElement);

                // previous must return the newly added element
                assertTrue(iterator.hasPrevious());
                assertEquals(newElement, iterator.previous());
            }

            List<T> expectedElements = new ArrayList<>(expectedElements());
            for (int i = expectedElements.size(); i >= 0; i--) {
                expectedElements.add(i, newElement);
            }

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("add(Object) with null using previous()")
        default void testAddNullUsingPrevious(TestInfo testInfo) {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            if (annotation == null) {
                iterator.add(null);

                // previous must return the newly added element
                assertTrue(iterator.hasPrevious());
                assertNull(iterator.previous());
            } else {
                assertThrows(annotation.expected(), () -> iterator.add(null));
            }

            while (iterator.hasPrevious()) {
                iterator.previous();

                if (annotation == null) {
                    iterator.add(null);

                    // previous must return the newly added element
                    assertTrue(iterator.hasPrevious());
                    assertNull(iterator.previous());
                } else {
                    assertThrows(annotation.expected(), () -> iterator.add(null));
                }
            }

            if (annotation == null) {
                List<T> expectedElements = new ArrayList<>(expectedElements());
                for (int i = expectedElements.size(); i >= 0; i--) {
                    expectedElements.add(i, null);
                }

                assertEquals(expectedElements, list);
            } else {
                assertEquals(expectedElements(), list);
            }
        }
    }
}
