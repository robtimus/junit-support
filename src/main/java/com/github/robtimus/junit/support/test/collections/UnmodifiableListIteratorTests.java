/*
 * UnmodifiableListIteratorTests.java
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

import static com.github.robtimus.junit.support.ThrowableAssertions.assertThrowsOneOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import java.util.ListIterator;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import com.github.robtimus.junit.support.test.collections.annotation.StoreNullNotSupported;

/**
 * Base interface for testing separate {@link ListIterator} functionalities for unmodifiable list iterators.
 *
 * @author Rob Spoor
 * @param <T> The element type of the list iterator to test.
 */
public interface UnmodifiableListIteratorTests<T> extends ListIteratorTests<T> {

    /**
     * Contains tests for {@link ListIterator#remove()} for unmodifiable list iterators.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list iterator to test.
     */
    @DisplayName("remove()")
    interface RemoveTests<T> extends UnmodifiableListIteratorTests<T> {

        @Test
        @DisplayName("remove() using next() throws UnsupportedOperationException")
        default void testRemoveUsingNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            while (iterator.hasNext()) {
                iterator.next();

                assertThrows(UnsupportedOperationException.class, iterator::remove);
            }

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("remove() before next()")
        default void testRemoveBeforeNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            assertThrowsOneOf(UnsupportedOperationException.class, IllegalStateException.class, iterator::remove);

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("remove() using previous() throws UnsupportedOperationException")
        default void testRemoveUsingPrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            while (iterator.hasPrevious()) {
                iterator.previous();

                assertThrows(UnsupportedOperationException.class, iterator::remove);
            }

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("remove() before previous()")
        default void testRemoveBeforePrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            assertThrowsOneOf(UnsupportedOperationException.class, IllegalStateException.class, iterator::remove);

            assertEquals(expectedElements(), list);
        }
    }

    /**
     * Contains tests for {@link ListIterator#set(Object)} for unmodifiable iterators.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list iterator to test.
     */
    @DisplayName("set(Object)")
    interface SetTests<T> extends UnmodifiableListIteratorTests<T> {

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
        @DisplayName("set(Object) using next() throws UnsupportedOperationException")
        default void testSetUsingNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            UnaryOperator<T> operator = replaceElementOperator();

            while (iterator.hasNext()) {
                T element = iterator.next();

                assertThrows(UnsupportedOperationException.class, () -> iterator.set(operator.apply(element)));
            }

            assertEquals(expectedElements(), list);
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
                    assertThrows(UnsupportedOperationException.class, () -> iterator.set(null));
                } else {
                    assertThrowsOneOf(UnsupportedOperationException.class, annotation.expected(), () -> iterator.set(null));
                }
            }

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("set(Object) before next()")
        default void testSetBeforeNext() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            T element = singleElement();

            assertThrowsOneOf(UnsupportedOperationException.class, IllegalStateException.class, () -> iterator.set(element));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("set(Object) using previous() throws UnsupportedOperationException")
        default void testSetUsingPrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            UnaryOperator<T> operator = replaceElementOperator();

            while (iterator.hasPrevious()) {
                T element = iterator.previous();

                assertThrows(UnsupportedOperationException.class, () -> iterator.set(operator.apply(element)));
            }

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("set(Object) with null replacement using previous()")
        default void testSetWithNullReplacementUsingPrevious(TestInfo testInfo) {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            while (iterator.hasPrevious()) {
                iterator.previous();

                if (annotation == null) {
                    assertThrows(UnsupportedOperationException.class, () -> iterator.set(null));
                } else {
                    assertThrowsOneOf(UnsupportedOperationException.class, annotation.expected(), () -> iterator.set(null));
                }
            }

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("set(Object) before previous()")
        default void testSetBeforePrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            T element = singleElement();

            assertThrowsOneOf(UnsupportedOperationException.class, IllegalStateException.class, () -> iterator.set(element));

            assertEquals(expectedElements(), list);
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

            assertThrows(UnsupportedOperationException.class, () -> iterator.add(newElement));

            while (iterator.hasNext()) {
                iterator.next();

                assertThrows(UnsupportedOperationException.class, () -> iterator.add(newElement));
            }

            assertEquals(expectedElements(), list);
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
                assertThrows(UnsupportedOperationException.class, () -> iterator.add(null));
            } else {
                assertThrowsOneOf(UnsupportedOperationException.class, annotation.expected(), () -> iterator.add(null));
            }

            while (iterator.hasNext()) {
                iterator.next();

                if (annotation == null) {
                    assertThrows(UnsupportedOperationException.class, () -> iterator.add(null));
                } else {
                    assertThrowsOneOf(UnsupportedOperationException.class, annotation.expected(), () -> iterator.add(null));
                }
            }

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("add(Object) using previous()")
        default void testAddUsingPrevious() {
            List<T> list = iterable();
            ListIterator<T> iterator = list.listIterator(list.size());

            T newElement = newElement();

            assertThrows(UnsupportedOperationException.class, () -> iterator.add(newElement));

            while (iterator.hasPrevious()) {
                iterator.previous();

                assertThrows(UnsupportedOperationException.class, () -> iterator.add(newElement));
            }

            assertEquals(expectedElements(), list);
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
                assertThrows(UnsupportedOperationException.class, () -> iterator.add(null));
            } else {
                assertThrowsOneOf(UnsupportedOperationException.class, annotation.expected(), () -> iterator.add(null));
            }

            while (iterator.hasPrevious()) {
                iterator.previous();

                if (annotation == null) {
                    assertThrows(UnsupportedOperationException.class, () -> iterator.add(null));
                } else {
                    assertThrowsOneOf(UnsupportedOperationException.class, annotation.expected(), () -> iterator.add(null));
                }
            }

            assertEquals(expectedElements(), list);
        }
    }
}
