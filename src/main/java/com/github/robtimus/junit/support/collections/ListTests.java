/*
 * ListTests.java
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import com.github.robtimus.junit.support.collections.annotation.ContainsIncompatibleNotSupported;
import com.github.robtimus.junit.support.collections.annotation.ContainsNullNotSupported;
import com.github.robtimus.junit.support.collections.annotation.StoreNullNotSupported;

/**
 * Base interface for testing separate {@link List} functionalities.
 *
 * @author Rob Spoor
 * @param <T> The element type of the list to test.
 */
public interface ListTests<T> extends CollectionTests<T> {

    @Override
    List<T> createIterable();

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
     * Contains tests for {@link List#add(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link List#add(Object)} with {@code null} will simply add the {@code null}.
     * If this is not the case, annotate your class with {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("add(Object)")
    interface AddTests<T> extends ListTests<T> {

        @Test
        @DisplayName("add(Object)")
        default void testAdd() {
            List<T> list = createIterable();

            Collection<T> expectedElements = expectedElements();
            for (T object : expectedElements) {
                assertTrue(list.add(object));
            }

            Collection<T> nonContainedElements = nonContainedElements();
            for (T object : nonContainedElements) {
                assertTrue(list.add(object));
            }

            List<T> expected = new ArrayList<>(expectedElements.size() * 2 + nonContainedElements.size());
            expected.addAll(expectedElements);
            expected.addAll(expectedElements);
            expected.addAll(nonContainedElements);

            assertEquals(expected, list);
        }

        @Test
        @DisplayName("add(Object) with null")
        default void testAddNull(TestInfo testInfo) {
            List<T> list = createIterable();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            Collection<T> expectedElements = expectedElements();

            if (annotation == null) {
                assertTrue(list.add(null));

                expectedElements = new ArrayList<>(expectedElements);
                expectedElements.add(null);
            } else {
                assertThrows(annotation.expected(), () -> list.add(null));
            }

            assertEquals(expectedElements, list);
        }
    }

    /**
     * Contains tests for {@link List#addAll(Collection)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link List#addAll(Collection)} with a collection containing {@code null} will
     * simply add the {@code null}. If this is not the case, annotate your class with {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("addAll(Collection)")
    interface AddAllTests<T> extends ListTests<T> {

        @Test
        @DisplayName("addAll(Collection)")
        default void testAddAll() {
            List<T> list = createIterable();

            Collection<T> nonContainedElements = nonContainedElements();
            assertTrue(list.addAll(nonContainedElements));

            Collection<T> expectedElements = expectedElements();
            assertTrue(list.addAll(expectedElements));

            List<T> expected = new ArrayList<>(expectedElements.size() * 2 + nonContainedElements.size());
            expected.addAll(expectedElements);
            expected.addAll(nonContainedElements);
            expected.addAll(expectedElements);

            assertEquals(expected, list);
        }

        @Test
        @DisplayName("addAll(Collection) with an empty collection")
        default void testAddAllWithEmptyCollection() {
            List<T> list = createIterable();

            assertFalse(list.addAll(Collections.emptyList()));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("addAll(Collection) with a null collection")
        default void testAddAllWithNullCollection() {
            List<T> list = createIterable();

            assertThrows(NullPointerException.class, () -> list.addAll(null));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("addAll(Collection) with a collection with a null")
        default void testAddAllWithCollectionWithNull(TestInfo testInfo) {
            List<T> list = createIterable();

            Collection<T> c = Collections.singleton(null);

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            Collection<T> expectedElements = expectedElements();

            if (annotation == null) {
                assertTrue(list.addAll(c));

                expectedElements = new ArrayList<>(expectedElements);
                expectedElements.addAll(c);
            } else {
                assertThrows(annotation.expected(), () -> list.addAll(c));
            }

            assertEquals(expectedElements, list);
        }
    }

    /**
     * Contains tests for {@link List#replaceAll(UnaryOperator)}.
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

            list.replaceAll(operator);

            List<T> expectedElements = new ArrayList<>(expectedElements());
            expectedElements.replaceAll(operator);

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("replaceAll(UnaryOperator) with null operator")
        default void testReplaceAllWithNullOperator() {
            List<T> list = createIterable();

            assertThrows(NullPointerException.class, () -> list.replaceAll(null));

            assertEquals(expectedElements(), list);
        }
    }

    // No interface for sort - almost nobody is going to implement that any other way that through the default implementation

    /**
     * Contains tests for {@link List#equals(Object)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("equals(Object)")
    interface EqualsTests<T> extends ListTests<T> {

        @ParameterizedTest(name = "{0}: {1}")
        @ArgumentsSource(EqualsArgumentsProvider.class)
        @DisplayName("equals(Object)")
        default void testEquals(List<?> other, boolean expected) {
            List<?> list = createIterable();

            if (expected) {
                assertEquals(other, list);
            } else {
                assertNotEquals(other, list);
            }
        }

        @Test
        @DisplayName("equals(Object) with self")
        default void testEqualsSelf() {
            List<?> list = createIterable();

            assertEquals(list, list);
        }

        @Test
        @DisplayName("equals(Object) with null")
        default void testEqualsNull() {
            List<?> list = createIterable();

            assertNotEquals(null, list);
        }

        @Test
        @DisplayName("equals(Object) with set")
        default void testEqualsSet() {
            List<?> list = createIterable();

            assertNotEquals(new HashSet<>(list), list);
        }
    }

    /**
     * Contains tests for {@link List#hashCode()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("hashCode()")
    interface HashCodeTests<T> extends ListTests<T> {

        @Test
        @DisplayName("hashCode()")
        default void testHashCode() {
            List<?> list = createIterable();

            int expected = expectedElements().stream()
                    .mapToInt(Object::hashCode)
                    .reduce(1, (x, y) -> 31 * x + y);

            assertEquals(expected, list.hashCode());
        }
    }

    /**
     * Contains tests for {@link List#get(int)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("get(int)")
    interface GetTests<T> extends ListTests<T> {

        @Test
        @DisplayName("get(int)")
        default void testGet() {
            List<?> list = createIterable();

            List<?> expectedElements = expectedElements();

            for (ListIterator<?> i = expectedElements.listIterator(); i.hasNext(); ) {
                int index = i.nextIndex();
                Object element = i.next();

                assertEquals(element, list.get(index));
            }
        }

        @Test
        @DisplayName("get(int) with negative index")
        default void testGetWithNegativeIndex() {
            List<?> list = createIterable();

            assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        }

        @Test
        @DisplayName("get(int) with index equal to size")
        default void testGetWithIndexEqualToSize() {
            List<?> list = createIterable();

            assertThrows(IndexOutOfBoundsException.class, () -> list.get(list.size()));
        }
    }

    /**
     * Contains tests for {@link List#set(int, Object)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("set(int, Object)")
    interface SetTests<T> extends ListTests<T> {

        /**
         * Returns a unary operator that can be used to create new values to set with {@link List#set(int, Object)}.
         *
         * @return A unary operator that can be used to create new values to set with {@link List#set(int, Object)}.
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

                assertEquals(element, list.set(index, operator.apply(element)));
            }

            expectedElements.replaceAll(operator);

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("set(int, Object) with null replacement")
        default void testSetWithNullReplacement(TestInfo testInfo) {
            List<T> list = createIterable();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            List<T> expectedElements = expectedElements();

            for (ListIterator<T> i = expectedElements.listIterator(); i.hasNext(); ) {
                int index = i.nextIndex();
                i.next();

                if (annotation == null) {
                    list.set(index, null);
                } else {
                    assertThrows(annotation.expected(), () -> list.set(index, null));
                }
            }

            if (annotation == null) {
                expectedElements = new ArrayList<>(expectedElements);
                Collections.fill(expectedElements, null);

                assertEquals(expectedElements, list);
            } else {
                assertEquals(expectedElements(), list);
            }
        }

        @Test
        @DisplayName("set(int, Object) with negative index")
        default void testSetWithNegativeIndex() {
            List<T> list = createIterable();

            T object = nonContainedElements().iterator().next();

            assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, object));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("set(int, Object) with index equal to size")
        default void testSetWithIndexEqualToSize() {
            List<T> list = createIterable();

            T object = nonContainedElements().iterator().next();

            assertThrows(IndexOutOfBoundsException.class, () -> list.set(list.size(), object));

            assertEquals(expectedElements(), list);
        }
    }

    /**
     * Contains tests for {@link List#add(int, Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link List#add(int, Object)} with {@code null} will simply add the {@code null}.
     * If this is not the case, annotate your class with {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("add(int, Object)")
    interface AddIndexedTests<T> extends ListTests<T> {

        @Test
        @DisplayName("add(int, Object) with index 0")
        default void testAddIndexedWithIndexZero() {
            List<T> list = createIterable();

            Collection<T> expectedElements = expectedElements();
            for (T object : expectedElements) {
                list.add(0, object);
            }

            Collection<T> nonContainedElements = nonContainedElements();
            for (T object : nonContainedElements) {
                list.add(0, object);
            }

            List<T> expected = new ArrayList<>(expectedElements.size() * 2 + nonContainedElements.size());
            expected.addAll(expectedElements);
            expected.addAll(nonContainedElements);
            Collections.reverse(expected);
            expected.addAll(expectedElements);

            assertEquals(expected, list);
        }

        @Test
        @DisplayName("add(int, Object) with index equal to size")
        default void testAddIndexedWithIndexEqualToSize() {
            List<T> list = createIterable();

            Collection<T> expectedElements = expectedElements();
            for (T object : expectedElements) {
                list.add(list.size(), object);
            }

            Collection<T> nonContainedElements = nonContainedElements();
            for (T object : nonContainedElements) {
                list.add(list.size(), object);
            }

            List<T> expected = new ArrayList<>(expectedElements.size() * 2 + nonContainedElements.size());
            expected.addAll(expectedElements);
            expected.addAll(expectedElements);
            expected.addAll(nonContainedElements);

            assertEquals(expected, list);
        }

        @Test
        @DisplayName("add(int, Object) with index equal to size / 2")
        default void testAddIndexedWithIndexEqualToSizeDivTwo() {
            List<T> list = createIterable();

            int index = list.size() / 2;

            List<T> expectedElements = expectedElements();
            for (T object : expectedElements) {
                list.add(index, object);
            }

            Collection<T> nonContainedElements = nonContainedElements();
            for (T object : nonContainedElements) {
                list.add(index, object);
            }

            List<T> expected = new ArrayList<>(expectedElements.size() * 2 + nonContainedElements.size());
            expected.addAll(expectedElements);
            expected.addAll(nonContainedElements);
            Collections.reverse(expected);
            expected.addAll(0, expectedElements.subList(0, index));
            expected.addAll(expectedElements.subList(index, expectedElements.size()));

            assertEquals(expected, list);
        }

        @Test
        @DisplayName("add(int, Object) with null")
        default void testAddIndexedWithNull(TestInfo testInfo) {
            List<T> list = createIterable();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            List<T> expectedElements = expectedElements();

            if (annotation == null) {
                list.add(0, null);

                expectedElements = new ArrayList<>(expectedElements);
                expectedElements.add(0, null);
            } else {
                assertThrows(annotation.expected(), () -> list.add(0, null));
            }

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("add(int, Object) with negative index")
        default void testAddIndexedWithNegativeIndex() {
            List<T> list = createIterable();

            T object = nonContainedElements().iterator().next();

            assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, object));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("add(int, Object) with index larger than size")
        default void testAddIndexedWithIndexLargerThanSize() {
            List<T> list = createIterable();

            T object = nonContainedElements().iterator().next();

            assertThrows(IndexOutOfBoundsException.class, () -> list.set(list.size() + 1, object));

            assertEquals(expectedElements(), list);
        }
    }

    /**
     * Contains tests for {@link List#addAll(int, Collection)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link List#addAll(int, Collection)} with a collection containing {@code null} will
     * simply add the {@code null}. If this is not the case, annotate your class with {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("addAll(int, Collection)")
    interface AddAllIndexedTests<T> extends ListTests<T> {

        @Test
        @DisplayName("addAll(int, Collection) with index 0")
        default void testAddAllIndexedWithIndexZero() {
            List<T> list = createIterable();

            Collection<T> nonContainedElements = nonContainedElements();
            assertTrue(list.addAll(0, nonContainedElements));

            Collection<T> expectedElements = expectedElements();
            assertTrue(list.addAll(0, expectedElements));

            List<T> expected = new ArrayList<>(expectedElements.size() * 2 + nonContainedElements.size());
            expected.addAll(expectedElements);
            expected.addAll(nonContainedElements);
            expected.addAll(expectedElements);

            assertEquals(expected, list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with index equal to size")
        default void testAddAllIndexedWithIndexEqualToSize() {
            List<T> list = createIterable();

            Collection<T> nonContainedElements = nonContainedElements();
            assertEquals(!nonContainedElements.isEmpty(), list.addAll(list.size(), nonContainedElements));

            Collection<T> expectedElements = expectedElements();
            assertEquals(!expectedElements.isEmpty(), list.addAll(list.size(), expectedElements));

            List<T> expected = new ArrayList<>(expectedElements.size() * 2 + nonContainedElements.size());
            expected.addAll(expectedElements);
            expected.addAll(nonContainedElements);
            expected.addAll(expectedElements);

            assertEquals(expected, list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with index equal to size / 2")
        default void testAddAllIndexedWithIndexEqualToSizeDivTwo() {
            List<T> list = createIterable();

            int index = list.size() / 2;

            List<T> expectedElements = expectedElements();
            assertTrue(list.addAll(index, expectedElements));

            Collection<T> nonContainedElements = nonContainedElements();
            assertTrue(list.addAll(index, nonContainedElements));

            List<T> expected = new ArrayList<>(expectedElements.size() * 2 + nonContainedElements.size());
            expected.addAll(nonContainedElements);
            expected.addAll(expectedElements);
            expected.addAll(0, expectedElements.subList(0, index));
            expected.addAll(expectedElements.subList(index, expectedElements.size()));

            assertEquals(expected, list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with an empty collection")
        default void testAddAllIndexedWithEmptyCollection() {
            List<T> list = createIterable();

            assertFalse(list.addAll(0, Collections.emptyList()));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with a null collection")
        default void testAddAllIndexedWithNullCollection() {
            List<T> list = createIterable();

            assertThrows(NullPointerException.class, () -> list.addAll(0, null));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with a collection with a null")
        default void testAddAllIndexedWithCollectionWithNull(TestInfo testInfo) {
            List<T> list = createIterable();

            Collection<T> c = Collections.singleton(null);

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            List<T> expectedElements = expectedElements();

            if (annotation == null) {
                assertTrue(list.addAll(0, c));

                expectedElements = new ArrayList<>(expectedElements);
                expectedElements.addAll(0, c);
            } else {
                assertThrows(annotation.expected(), () -> list.addAll(0, c));
            }

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with negative index")
        default void testAddAllIndexedWithNegativeIndex() {
            List<T> list = createIterable();

            Collection<T> c = Collections.singleton(nonContainedElements().iterator().next());

            assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(-1, c));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("addAll(int, Collection) with index larger than size")
        default void testAddAllIndexedWithIndexLargerThanSize() {
            List<T> list = createIterable();

            Collection<T> c = Collections.singleton(nonContainedElements().iterator().next());

            assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(list.size() + 1, c));

            assertEquals(expectedElements(), list);
        }
    }

    /**
     * Contains tests for {@link List#remove(int)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("remove(int)")
    interface RemoveIndexedTests<T> extends ListTests<T> {

        @Test
        @DisplayName("remove(int) with every index")
        default void testRemoveIndexedWithEveryIndex() {
            List<?> list = createIterable();

            List<?> expectedElements = expectedElements();

            for (int i = list.size() - 1; i >= 0; i--) {
                Object removed = list.remove(i);

                assertEquals(expectedElements.get(i), removed);
            }

            assertEquals(Collections.emptyList(), list);
        }

        @Test
        @DisplayName("remove(int) with even indexes")
        default void testRemoveIndexedWithEvenIndexes() {
            List<T> list = createIterable();

            List<T> expectedElements = new ArrayList<>(expectedElements());

            int start = expectedElements.size() - 1;
            if (start % 2 == 1) {
                start--;
            }

            for (int i = start; i >= 0; i -= 2) {
                T removed = list.remove(i);

                assertEquals(expectedElements.get(i), removed);

                expectedElements.remove(i);
            }

            assertEquals(expectedElements, list);
        }

        @Test
        @DisplayName("remove(int) with negative index")
        default void testRemoveIndexedWithNegativeIndex() {
            List<T> list = createIterable();

            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("remove(int) with index equal to size")
        default void testRemoveIndexedWithIndexEqualToSize() {
            List<T> list = createIterable();

            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(list.size()));

            assertEquals(expectedElements(), list);
        }
    }

    /**
     * Contains tests for {@link List#indexOf(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link List#indexOf(Object)} with {@code null} or an instance of an incompatible
     * type will simply return {@code -1}. If either is not the case, annotate your class with {@link ContainsNullNotSupported} and/or
     * {@link ContainsIncompatibleNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("indexOf(Object)")
    interface IndexOfTests<T> extends ListTests<T> {

        @Test
        @DisplayName("indexOf(Object)")
        default void testIndexOf() {
            List<?> list = createIterable();

            List<?> expectedElements = expectedElements();

            for (Object o : expectedElements) {
                assertEquals(expectedElements.indexOf(o), list.indexOf(o));
            }

            for (Object o : nonContainedElements()) {
                assertEquals(-1, list.indexOf(o));
            }
        }

        @Test
        @DisplayName("indexOf(Object) with null")
        default void testIndexOfWithNull(TestInfo testInfo) {
            List<?> list = createIterable();

            ContainsNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsNullNotSupported.class);

            if (annotation == null) {
                assertEquals(-1, list.indexOf(null));
            } else {
                assertThrows(annotation.expected(), () -> list.indexOf(null));
            }
        }

        @Test
        @DisplayName("indexOf(Object) with an incompatible object")
        default void testIndexOfWithIncompatibleObject(TestInfo testInfo) {
            List<?> list = createIterable();

            ContainsIncompatibleNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsIncompatibleNotSupported.class);

            if (annotation == null) {
                assertEquals(-1, list.indexOf(new IncompatibleObject()));
            } else {
                assertThrows(annotation.expected(), () -> list.indexOf(new IncompatibleObject()));
            }
        }
    }

    /**
     * Contains tests for {@link List#lastIndexOf(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link List#lastIndexOf(Object)} with {@code null} or an instance of an
     * incompatible type will simply return {@code -1}. If either is not the case, annotate your class with {@link ContainsNullNotSupported} and/or
     * {@link ContainsIncompatibleNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("lastIndexOf(Object)")
    interface LastIndexOfTests<T> extends ListTests<T> {

        @Test
        @DisplayName("lastIndexOf(Object)")
        default void testLastIndexOf() {
            List<?> list = createIterable();

            List<?> expectedElements = expectedElements();

            for (Object o : expectedElements) {
                assertEquals(expectedElements.lastIndexOf(o), list.lastIndexOf(o));
            }

            for (Object o : nonContainedElements()) {
                assertEquals(-1, list.lastIndexOf(o));
            }
        }

        @Test
        @DisplayName("lastIndexOf(Object) with null")
        default void testLastIndexOfWithNull(TestInfo testInfo) {
            List<?> list = createIterable();

            ContainsNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsNullNotSupported.class);

            if (annotation == null) {
                assertEquals(-1, list.lastIndexOf(null));
            } else {
                assertThrows(annotation.expected(), () -> list.lastIndexOf(null));
            }
        }

        @Test
        @DisplayName("lastIndexOf(Object) with an incompatible object")
        default void testLastIndexOfWithIncompatibleObject(TestInfo testInfo) {
            List<?> list = createIterable();

            ContainsIncompatibleNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsIncompatibleNotSupported.class);

            if (annotation == null) {
                assertEquals(-1, list.lastIndexOf(new IncompatibleObject()));
            } else {
                assertThrows(annotation.expected(), () -> list.lastIndexOf(new IncompatibleObject()));
            }
        }
    }

    /**
     * Contains tests for {@link List#listIterator(int)}.
     * <p>
     * Note that to test any individual methods of the list iterator, use {@link ListIteratorTests} instead.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("listIterator(int)")
    interface ListIteratorIndexedTests<T> extends ListTests<T> {

        @Test
        @DisplayName("listIterator(int) with index 0")
        default void testListIteratorIndexedWithIndexZero() {
            List<T> list = createIterable();

            ListIterator<T> iterator = list.listIterator(0);

            assertFalse(iterator.hasPrevious());

            List<T> elements = new ArrayList<>(list.size());
            while (iterator.hasNext()) {
                T element = iterator.next();
                elements.add(element);
            }

            assertEquals(expectedElements(), list);
        }

        @Test
        @DisplayName("listIterator(int) with index equal to size")
        default void testListIteratorIndexedWithIndexEqualToSize() {
            List<T> list = createIterable();

            ListIterator<T> iterator = list.listIterator(list.size());

            assertFalse(iterator.hasNext());

            List<T> elements = new ArrayList<>(list.size());
            while (iterator.hasPrevious()) {
                T element = iterator.previous();
                elements.add(element);
            }

            List<T> expectedElements = new ArrayList<>(expectedElements());
            Collections.reverse(expectedElements);

            assertEquals(expectedElements, elements);
        }

        @Test
        @DisplayName("listIterator(int) with index equal to size / 2")
        default void testListIteratorIndexedWithIndexEqualToSizeDivTwo() {
            List<T> list = createIterable();

            ListIterator<T> iterator = list.listIterator(list.size() / 2);

            assertTrue(iterator.hasPrevious());

            List<T> elements = new ArrayList<>(list.size());
            while (iterator.hasNext()) {
                T element = iterator.next();
                elements.add(element);
            }

            assertEquals(expectedElements().subList(list.size() / 2, list.size()), elements);
        }

        @Test
        @DisplayName("listIterator(int) with negative index")
        default void testListIteratorIndexedWithNegativeIndex() {
            List<?> list = createIterable();

            assertThrows(IndexOutOfBoundsException.class, () -> list.listIterator(-1));
        }

        @Test
        @DisplayName("listIterator(int) with index larger than size")
        default void testListIteratorIndexedWithIndexLargerThanSize() {
            List<?> list = createIterable();

            assertThrows(IndexOutOfBoundsException.class, () -> list.listIterator(list.size() + 1));
        }
    }

    /**
     * Contains tests for {@link List#subList(int, int)}.
     * <p>
     * Note that to test any individual methods of the sub list, add one or more {@link Nested} classes to your test class that implement any other
     * interface nested in {@link ListTests}. These sub classes should then implement {@link #createIterable()} to call {@link List#subList(int, int)}
     * on an instance of the list to test.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("subList(int, int)")
    interface SubListTests<T> extends ListTests<T> {

        @Test
        @DisplayName("subList(int, int) with full range")
        default void testSubListWithFullRange() {
            List<?> list = createIterable();

            List<?> subList = list.subList(0, list.size());

            assertEquals(expectedElements(), subList);
        }

        @Test
        @DisplayName("subList(int, int) with partial range")
        default void testSubListWithPartialRange() {
            List<?> list = createIterable();

            List<?> subList = list.subList(1, list.size() - 1);

            assertEquals(expectedElements().subList(1, list.size() - 1), subList);
        }

        @Test
        @DisplayName("subList(int, int) with negative from")
        default void testSubListWithNegativeFrom() {
            List<?> list = createIterable();

            assertThrows(IndexOutOfBoundsException.class, () -> list.subList(-1, list.size()));
        }

        @Test
        @DisplayName("subList(int, int) with to larger than size")
        default void testSubListWithToLargerThanSize() {
            List<?> list = createIterable();

            assertThrows(IndexOutOfBoundsException.class, () -> list.subList(0, list.size() + 1));
        }

        @Test
        @DisplayName("subList(int, int) with to smaller than from")
        default void testSubListWithToSmallerThanFrom() {
            List<?> list = createIterable();

            int from = list.size() / 2;
            int to = from - 1;

            // List.subList specifies that if from > to, an IndexOutOfBoundsException should be thrown.
            // However, AbstractList.subList throws an IllegalArgumentException, and documents that it does.
            Exception exception = assertThrows(Exception.class, () -> list.subList(from, to));
            assertThat(exception, either(instanceOf(IndexOutOfBoundsException.class)).or(instanceOf(IllegalArgumentException.class)));
        }
    }

    /**
     * Contains tests for {@link List#spliterator()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the list to test.
     */
    @DisplayName("spliterator()")
    interface SpliteratorTests<T> extends ListTests<T> {

        @Test
        @DisplayName("spliterator() has ORDERED characteristic")
        default void testSpliteratorHasOrderedCharacteristic() {
            List<?> list = createIterable();

            Spliterator<?> spliterator = list.spliterator();

            assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
        }

        @Test
        @DisplayName("spliterator() has SIZED characteristic")
        default void testSpliteratorHasSizedCharacteristic() {
            List<?> list = createIterable();

            Spliterator<?> spliterator = list.spliterator();

            assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        }
    }

    /**
     * An arguments provider for {@link EqualsTests#testEquals(List, boolean)}.
     *
     * @author Rob Spoor
     */
    final class EqualsArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            EqualsTests<?> instance = (EqualsTests<?>) context.getRequiredTestInstance();

            List<?> expected = new ArrayList<>(instance.expectedElements());
            Object nonContained = instance.nonContainedElements().iterator().next();

            List<Arguments> arguments = new ArrayList<>();
            arguments.add(arguments(new ArrayList<>(expected), true));

            if (!expected.isEmpty()) {
                arguments.add(arguments(new ArrayList<>(expected.subList(0, expected.size() - 1)), false));
            }

            List<Object> withNonContained = new ArrayList<>(expected);
            withNonContained.add(nonContained);
            arguments.add(arguments(withNonContained, false));

            return arguments.stream();
        }
    }
}
