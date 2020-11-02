/*
 * UnmodifiableListTests.java
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

import static com.github.robtimus.junit.support.collections.CollectionAssertions.assertOptionallyThrowsUnsupportedOperationException;
import static com.github.robtimus.junit.support.collections.CollectionAssertions.assertThrowsUnsupportedOperationExceptionOr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing separate {@link List} functionalities for unmodifiable lists.
 *
 * @author Rob Spoor
 * @param <T> The element type of the list to test.
 */
public interface UnmodifiableListTests<T> extends ListTests<T>, UnmodifiableCollectionTests<T> {

    /**
     * Contains tests for {@link List#replaceAll(UnaryOperator)} for unmodifiable lists.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("replaceAll(UnaryOperator)")
    interface ReplaceAllTests<T> extends ListTests<T> {

        /**
         * Returns a unary operator that can be used with {@link List#replaceAll(UnaryOperator)}.
         *
         * @return A unary operator that can be used with {@link List#replaceAll(UnaryOperator)}.
         */
        UnaryOperator<T> replaceElementOperator();

        @Test
        @DisplayName("replaceAll(UnaryOperator)")
        default void testReplaceAll() {
            List<T> list = createIterable();

            UnaryOperator<T> operator = replaceElementOperator();

            if (list.isEmpty()) {
                assertOptionallyThrowsUnsupportedOperationException(() -> list.replaceAll(operator));
            } else {
                assertThrows(UnsupportedOperationException.class, () -> list.replaceAll(operator));
            }

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("replaceAll(UnaryOperator) with null operator")
        default void testReplaceAllWithNullOperator() {
            List<T> list = createIterable();

            assertThrowsUnsupportedOperationExceptionOr(NullPointerException.class, () -> list.replaceAll(null));

            assertEquals(expectedElements(), list);
        }
    }

    /**
     * Contains tests for {@link List#set(int, Object)} for unmodifiable lists.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("set(int, Object)")
    interface SetTests<T> extends ListTests<T> {

        /**
         * Returns a unary operator that can be used with {@link List#replaceAll(UnaryOperator)}.
         *
         * @return A unary operator that can be used with {@link List#replaceAll(UnaryOperator)}.
         */
        UnaryOperator<T> replaceElementOperator();

        @Test
        @DisplayName("set(int, Object)")
        default void testSet() {
            List<T> list = createIterable();

            List<T> expectedElements = expectedElements();

            UnaryOperator<T> operator = replaceElementOperator();

            for (ListIterator<T> i = expectedElements.listIterator(); i.hasNext(); ) {
                int index = i.nextIndex();
                T element = i.next();

                assertThrows(UnsupportedOperationException.class, () -> list.set(index, operator.apply(element)));
            }

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("set(int, Object) with null replacement")
        default void testSetWithNullReplacement() {
            List<T> list = createIterable();

            List<T> expectedElements = expectedElements();

            for (ListIterator<T> i = expectedElements.listIterator(); i.hasNext(); ) {
                int index = i.nextIndex();
                i.next();

                assertThrows(UnsupportedOperationException.class, () -> list.set(index, null));
            }

            assertEquals(expectedElements(), list);
        }
    }

    /**
     * Contains tests for {@link List#add(int, Object)} for unmodifiable lists.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("add(int, Object)")
    interface AddIndexedTests<T> extends UnmodifiableListTests<T> {

        @Test
        @DisplayName("add(int, Object)")
        default void testAddIndexed() {
            List<T> list = createIterable();

            Collection<T> expectedElements = expectedElements();

            for (T element : expectedElements) {
                assertThrows(UnsupportedOperationException.class, () -> list.add(0, element));
            }

            for (T element : nonContainedElements()) {
                assertThrows(UnsupportedOperationException.class, () -> list.add(0, element));
            }

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("add(int, Object) with null")
        default void testAddIndexedWithNull() {
            List<T> list = createIterable();

            assertThrows(UnsupportedOperationException.class, () -> list.add(0, null));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("add(int, Object) with negative index")
        default void testAddIndexedWithNegativeIndex() {
            List<T> list = createIterable();

            T object = nonContainedElements().iterator().next();

            assertThrowsUnsupportedOperationExceptionOr(IndexOutOfBoundsException.class, () -> list.add(-1, object));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("add(int, Object) with index larger than size")
        default void testAddIndexedWithIndexLargerThanSize() {
            List<T> list = createIterable();

            T object = nonContainedElements().iterator().next();

            assertThrowsUnsupportedOperationExceptionOr(IndexOutOfBoundsException.class, () -> list.set(list.size() + 1, object));

            assertEquals(expectedElements(), list);
        }
    }

    /**
     * Contains tests for {@link List#addAll(int, Collection)} for unmodifiable lists.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("addAll(int, Collection)")
    interface AddAllIndexedTests<T> extends UnmodifiableListTests<T> {

        @Test
        @DisplayName("addAll(int, Collection)")
        default void testAddAllIndexed() {
            List<T> list = createIterable();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements);

            for (int i = 1; i <= elements.size(); i++) {
                int to = i;
                assertThrows(UnsupportedOperationException.class, () -> list.addAll(0, elements.subList(0, to)));
            }

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with empty collection")
        default void testAddAllIndexedWithEmptyCollection() {
            List<T> list = createIterable();

            assertOptionallyThrowsUnsupportedOperationException(() -> assertFalse(list.addAll(0, Collections.emptyList())));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with null")
        default void testAddAllIndexedWithNull() {
            List<T> list = createIterable();

            assertThrowsUnsupportedOperationExceptionOr(NullPointerException.class, () -> list.addAll(0, null));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with null element")
        default void testAddAllIndexedWithNullElement() {
            List<T> list = createIterable();

            assertThrows(UnsupportedOperationException.class, () -> list.addAll(0, Collections.singleton(null)));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with negative index")
        default void testAddAllIndexedWithNegativeIndex() {
            List<T> list = createIterable();

            Collection<T> c = Collections.singleton(nonContainedElements().iterator().next());

            assertThrowsUnsupportedOperationExceptionOr(IndexOutOfBoundsException.class, () -> list.addAll(-1, c));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with index larger than size")
        default void testAddAllIndexedWithIndexLargerThanSize() {
            List<T> list = createIterable();

            Collection<T> c = Collections.singleton(nonContainedElements().iterator().next());

            assertThrowsUnsupportedOperationExceptionOr(IndexOutOfBoundsException.class, () -> list.addAll(list.size() + 1, c));

            assertEquals(expectedElements(), list);
        }
    }

    /**
     * Contains tests for {@link List#remove(int)} for unmodifiable lists.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("remove(int)")
    interface RemoveIndexedTests<T> extends ListTests<T> {

        @Test
        @DisplayName("remove(int)")
        default void testRemoveIndexed() {
            List<T> list = createIterable();

            for (int i = list.size() - 1; i >= 0; i--) {
                int index = i;
                assertThrows(UnsupportedOperationException.class, () -> list.remove(index));
            }

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("remove(int) with negative index")
        default void testRemoveIndexedWithNegativeIndex() {
            List<T> list = createIterable();

            assertThrowsUnsupportedOperationExceptionOr(IndexOutOfBoundsException.class, () -> list.remove(-1));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("remove(int) with index equal to size")
        default void testRemoveIndexedWithIndexEqualToSize() {
            List<T> list = createIterable();

            assertThrowsUnsupportedOperationExceptionOr(IndexOutOfBoundsException.class, () -> list.remove(list.size()));

            assertEquals(expectedElements(), list);
        }
    }
}
