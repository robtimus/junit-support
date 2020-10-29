/*
 * UnmodifiableCollectionTests.java
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing separate {@link Collection} functionalities for unmodifiable collections.
 *
 * @author Rob Spoor
 * @param <T> The element type of the collection to test.
 */
public interface UnmodifiableCollectionTests<T> extends CollectionTests<T> {

    /**
     * Contains tests for {@link Collection#add(Object)} for unmodifiable collections.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("add(Object)")
    interface AddTests<T> extends UnmodifiableCollectionTests<T> {

        @Test
        @DisplayName("add(Object) with contained elements")
        default void testAddContainedElements() {
            Collection<T> collection = createIterable();

            Collection<T> expectedElements = expectedElements();

            for (T element : expectedElements) {
                assertThrows(UnsupportedOperationException.class, () -> collection.add(element));
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("add(Object) with non-contained elements")
        default void testAddNonContainedElements() {
            Collection<T> collection = createIterable();

            for (T element : nonContainedElements()) {
                assertThrows(UnsupportedOperationException.class, () -> collection.add(element));
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("add(Object) with null")
        default void testAddNull() {
            Collection<T> collection = createIterable();

            assertThrows(UnsupportedOperationException.class, () -> collection.add(null));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Collection#remove(Object)} for unmodifiable collections.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("remove(Object)")
    interface RemoveTests<T> extends UnmodifiableCollectionTests<T> {

        @Test
        @DisplayName("remove(Object) with contained elements")
        default void testRemoveContainedElements() {
            Collection<T> collection = createIterable();

            Collection<T> expectedElements = expectedElements();

            for (T element : expectedElements) {
                assertThrows(UnsupportedOperationException.class, () -> collection.remove(element));
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("remove(Object) with non-contained elements")
        default void testRemoveNonContainedElements() {
            Collection<T> collection = createIterable();

            for (T element : nonContainedElements()) {
                // with a non-contained object, either it does nothing or it throws an exception
                try {
                    assertFalse(collection.remove(element));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("remove(Object) with null")
        default void testRemoveNull() {
            Collection<T> collection = createIterable();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertFalse(collection.remove(null));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("remove(Object) with an incompatible object")
        default void testRemoveIncompatibleObject() {
            Collection<?> collection = createIterable();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertFalse(collection.remove(new IncompatibleObject()));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Collection#addAll(Collection)} for unmodifiable collections.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("addAll(Collection)")
    interface AddAllTests<T> extends UnmodifiableCollectionTests<T> {

        @Test
        @DisplayName("addAll(Collection) with contained elements")
        default void testAddAllWithContainedElements() {
            Collection<T> collection = createIterable();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements);

            for (int i = 1; i <= elements.size(); i++) {
                int to = i;
                assertThrows(UnsupportedOperationException.class, () -> collection.addAll(elements.subList(0, to)));
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("addAll(Collection) with non-contained elements")
        default void testAddAllWithNonContainedElements() {
            Collection<T> collection = createIterable();

            List<T> elements = new ArrayList<>(nonContainedElements());
            for (int i = 1; i <= elements.size(); i++) {
                int to = i;
                assertThrows(UnsupportedOperationException.class, () -> collection.addAll(elements.subList(0, to)));
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("addAll(Collection) with an empty collection")
        default void testAddAllWithEmptyCollection() {
            Collection<T> collection = createIterable();

            // with an empty collection, either it does nothing or it throws an exception
            try {
                assertFalse(collection.addAll(Collections.emptyList()));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("addAll(Collection) with null")
        default void testAddAllWithNull() {
            Collection<T> collection = createIterable();

            Exception exception = assertThrows(Exception.class, () -> collection.addAll(null));
            assertThat(exception, either(instanceOf(UnsupportedOperationException.class)).or(instanceOf(NullPointerException.class)));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("addAll(Collection) with null element")
        default void testAddAllWithNullElement() {
            Collection<T> collection = createIterable();

            assertThrows(UnsupportedOperationException.class, () -> collection.addAll(Collections.singleton(null)));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Collection#removeAll(Collection)} for unmodifiable collections.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("removeAll(Collection)")
    interface RemoveAllTests<T> extends UnmodifiableCollectionTests<T> {

        @Test
        @DisplayName("removeAll(Collection) with contained elements")
        default void testRemoveAllWithContainedElements() {
            Collection<T> collection = createIterable();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements);
            for (int i = 1; i <= elements.size(); i++) {
                int to = i;
                assertThrows(UnsupportedOperationException.class, () -> collection.removeAll(elements.subList(0, to)));
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("removeAll(Collection) with non-contained elements")
        default void testRemoveAllWithNonContainedElements() {
            Collection<T> collection = createIterable();

            List<T> elements = new ArrayList<>(nonContainedElements());
            for (int i = 1; i <= elements.size(); i++) {
                int to = i;
                // with non-matching elements, either it does nothing or it throws an exception
                try {
                    assertFalse(collection.removeAll(elements.subList(0, to)));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("removeAll(Collection) with an empty collection")
        default void testRemoveAllWithEmptyCollection() {
            Collection<T> collection = createIterable();

            // with an empty collection, either it does nothing or it throws an exception
            try {
                assertFalse(collection.removeAll(Collections.emptyList()));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("removeAll(Object) with null")
        default void testRemoveAllWithNull() {
            Collection<T> collection = createIterable();

            Exception exception = assertThrows(Exception.class, () -> collection.removeAll(null));
            assertThat(exception, either(instanceOf(UnsupportedOperationException.class)).or(instanceOf(NullPointerException.class)));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("removeAll(Object) with null element")
        default void testRemoveAllWithNullElement() {
            Collection<T> collection = createIterable();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertFalse(collection.removeAll(Collections.singleton(null)));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("removeAll(Object) with an incompatible object")
        default void testRemoveAllWithIncompatibleObject() {
            Collection<?> collection = createIterable();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertFalse(collection.removeAll(Collections.singleton(new IncompatibleObject())));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Collection#removeIf(Predicate)} for unmodifiable collections.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("removeIf(Predicate)")
    interface RemoveIfTests<T> extends UnmodifiableCollectionTests<T> {

        @Test
        @DisplayName("removeIf(Predicate) with matching predicate")
        default void testRemoveIfWithMatchingPredicate() {
            Collection<T> collection = createIterable();

            assertThrows(UnsupportedOperationException.class, () -> collection.removeIf(e -> true));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("removeIf(Predicate) with non-matching predicate")
        default void testRemoveIfWithNonMatchingPredicate() {
            Collection<T> collection = createIterable();

            // with a non-matching predicate, either it does nothing or it throws an exception
            try {
                assertFalse(collection.removeIf(e -> false));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("removeIf(Predicate) with null predicate")
        default void testRemoveIfWithNullPredicate() {
            Collection<T> collection = createIterable();

            Exception exception = assertThrows(Exception.class, () -> collection.removeIf(null));
            assertThat(exception, either(instanceOf(UnsupportedOperationException.class)).or(instanceOf(NullPointerException.class)));
        }
    }

    /**
     * Contains tests for {@link Collection#retainAll(Collection)} for unmodifiable collections.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("retainAll(Collection)")
    interface RetainAllTests<T> extends UnmodifiableCollectionTests<T> {

        @Test
        @DisplayName("retainAll(Collection) with contained elements")
        default void testRetainAllWithContainedElements() {
            Collection<T> collection = createIterable();

            Collection<T> expectedElements = expectedElements();
            List<T> elements = new ArrayList<>(expectedElements);
            for (int i = 0; i < elements.size(); i++) {
                int to = i;
                assertThrows(UnsupportedOperationException.class, () -> collection.retainAll(elements.subList(0, to)));
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("retainAll(Collection) with non-contained elements")
        default void testRetainAllWithNonContainedElements() {
            Collection<T> collection = createIterable();

            List<T> elements = new ArrayList<>(nonContainedElements());
            for (int i = 0; i <= elements.size(); i++) {
                int to = i;
                assertThrows(UnsupportedOperationException.class, () -> collection.retainAll(elements.subList(0, to)));
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("retainAll(Collection) with all contained elements")
        default void testRetainAllWithAllContainedElements() {
            Collection<T> collection = createIterable();

            Collection<T> expectedElements = expectedElements();

            // with all element, either it does nothing or it throws an exception
            try {
                assertFalse(collection.retainAll(expectedElements));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("retainAll(Object) with null")
        default void testRetainAllWithNull() {
            Collection<T> collection = createIterable();

            Exception exception = assertThrows(Exception.class, () -> collection.retainAll(null));
            assertThat(exception, either(instanceOf(UnsupportedOperationException.class)).or(instanceOf(NullPointerException.class)));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("retainAll(Object) with null element")
        default void testRetainAllWithNullElement() {
            Collection<T> collection = createIterable();

            assertThrows(UnsupportedOperationException.class, () -> collection.retainAll(Collections.singleton(null)));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("retainAll(Object) with an incompatible object")
        default void testRetainAllWithIncompatibleObject() {
            Collection<?> collection = createIterable();

            assertThrows(UnsupportedOperationException.class, () -> collection.retainAll(Collections.singleton(new IncompatibleObject())));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Collection#clear()} for unmodifiable collections.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("clear()")
    interface ClearTests<T> extends UnmodifiableCollectionTests<T> {

        @Test
        @DisplayName("clear()")
        default void testClear() {
            Collection<T> collection = createIterable();

            assertThrows(UnsupportedOperationException.class, collection::clear);

            assertHasElements(collection, expectedElements(), fixedOrder());
        }
    }
}
