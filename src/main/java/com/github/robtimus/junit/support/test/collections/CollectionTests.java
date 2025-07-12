/*
 * CollectionTests.java
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
import static com.github.robtimus.junit.support.test.collections.CollectionUtils.commonType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import com.github.robtimus.junit.support.test.collections.annotation.ContainsIncompatibleNotSupported;
import com.github.robtimus.junit.support.test.collections.annotation.ContainsNullNotSupported;
import com.github.robtimus.junit.support.test.collections.annotation.RemoveIncompatibleNotSupported;
import com.github.robtimus.junit.support.test.collections.annotation.RemoveNullNotSupported;

/**
 * Base interface for testing separate {@link Collection} functionalities.
 *
 * @author Rob Spoor
 * @param <T> The element type of the collection to test.
 */
public interface CollectionTests<T> extends IterableTests<T> {

    @Override
    Collection<T> iterable();

    /**
     * Returns some elements that should not be contained by the collection to test.
     *
     * @return A collection with elements that should not be contained by the collection to test.
     */
    Collection<T> nonContainedElements();

    /**
     * Contains tests for {@link Collection#contains(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Collection#contains(Object)} with {@code null} or an instance of an
     * incompatible type will simply return {@code false}. If either is not the case, annotate your class with {@link ContainsNullNotSupported} and/or
     * {@link ContainsIncompatibleNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("contains(Object)")
    interface ContainsTests<T> extends CollectionTests<T> {

        @Test
        @DisplayName("contains(Object)")
        default void testContains() {
            Collection<T> collection = iterable();

            for (T o : expectedElements()) {
                assertTrue(collection.contains(o));
            }

            for (T o : nonContainedElements()) {
                assertFalse(collection.contains(o));
            }
        }

        @Test
        @DisplayName("contains(Object) with null")
        default void testContainsWithNull(TestInfo testInfo) {
            Collection<T> collection = iterable();

            ContainsNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsNullNotSupported.class);

            if (annotation == null) {
                assertFalse(collection.contains(null));
            } else {
                assertThrows(annotation.expected(), () -> collection.contains(null));
            }
        }

        @Test
        @DisplayName("contains(Object) with an incompatible object")
        default void testContainsWithIncompatibleObject(TestInfo testInfo) {
            Collection<T> collection = iterable();

            ContainsIncompatibleNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsIncompatibleNotSupported.class);

            if (annotation == null) {
                assertFalse(collection.contains(new IncompatibleObject()));
            } else {
                assertThrows(annotation.expected(), () -> collection.contains(new IncompatibleObject()));
            }
        }
    }

    /**
     * Contains tests for {@link Collection#toArray()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("toArray()")
    interface ToObjectArrayTests<T> extends CollectionTests<T> {

        @Test
        @DisplayName("toArray()")
        default void testToObjectArray() {
            Collection<T> collection = iterable();

            Object[] array = collection.toArray();

            assertHasElements(array, expectedElements(), fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Collection#toArray(Object[])}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("toArray(Object[])")
    interface ToArrayTests<T> extends CollectionTests<T> {

        @Test
        @DisplayName("toArray(Object[]) with same length")
        default void testToArrayWithSameLength() {
            Collection<T> collection = iterable();

            Collection<T> expectedElements = expectedElements();

            Class<?> genericType = commonType(expectedElements);
            Object[] a = (Object[]) Array.newInstance(genericType, collection.size());

            Object[] array = collection.toArray(a);

            assertSame(a, array);
            assertHasElements(array, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("toArray(Object[]) with larger length")
        default void testToArrayWithLargerLength() {
            Collection<T> collection = iterable();

            Object[] a = new Object[collection.size() + 2];
            Object o = new IncompatibleObject();
            Arrays.fill(a, o);

            Object[] array = collection.toArray(a);

            assertSame(a, array);
            assertHasElements(Arrays.copyOfRange(array, 0, collection.size()), expectedElements(), fixedOrder());
            assertHasElements(Arrays.copyOfRange(array, collection.size(), a.length), Arrays.asList(null, o), true);
        }

        @Test
        @DisplayName("toArray(Object[]) with smaller length")
        default void testToArrayWithSmallerLength() {
            Collection<T> collection = iterable();

            Collection<T> expectedElements = expectedElements();

            Class<?> genericType = commonType(expectedElements);
            Object[] a = (Object[]) Array.newInstance(genericType, 0);

            Object[] array = collection.toArray(a);

            assertNotSame(a, array);
            assertInstanceOf(a.getClass(), array);
            assertHasElements(array, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("toArray(Object[]) with null array")
        default void testToArrayWithNullArray() {
            Collection<T> collection = iterable();

            assertThrows(NullPointerException.class, () -> collection.toArray((Object[]) null));
        }
    }

    /**
     * Contains tests for {@link Collection#toArray(IntFunction)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     * @since 3.0
     */
    @DisplayName("toArray(IntFunction)")
    interface ToArrayWithGeneratorTests<T> extends CollectionTests<T> {

        @Test
        @DisplayName("toArray(IntFunction)")
        default void testToArrayWithGenerator() {
            Collection<T> collection = iterable();

            Collection<T> expectedElements = expectedElements();

            Class<?> genericType = commonType(expectedElements);
            IntFunction<Object[]> generator = length -> (Object[]) Array.newInstance(genericType, length);

            Object[] array = collection.toArray(generator);

            assertHasElements(array, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("toArray(IntFunction) with null generator")
        default void testToArrayWithNullGenerator() {
            Collection<T> collection = iterable();

            assertThrows(NullPointerException.class, () -> collection.toArray((IntFunction<Object[]>) null));
        }
    }

    // No interface for add - the behaviour is too unspecified to write proper tests. Create for List and Set separately.

    /**
     * Contains tests for {@link Collection#remove(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Collection#remove(Object)} with {@code null} or an instance of an
     * incompatible type will simply return {@code false}. If either is not the case, annotate your class with {@link RemoveNullNotSupported} and/or
     * {@link RemoveIncompatibleNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("remove(Object)")
    interface RemoveTests<T> extends CollectionTests<T> {

        @ParameterizedTest(name = "{0}: {1}")
        @ArgumentsSource(RemoveArgumentsProvider.class)
        @DisplayName("remove(Object)")
        default void testRemove(Object o, boolean expected) {
            Collection<T> collection = iterable();

            assertEquals(expected, collection.remove(o));

            Collection<T> expectedElements = expectedElements();
            if (expected) {
                expectedElements = new ArrayList<>(expectedElements);
                expectedElements.remove(o);
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("remove(Object) with null")
        default void testRemoveNull(TestInfo testInfo) {
            Collection<T> collection = iterable();

            RemoveNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(RemoveNullNotSupported.class);

            if (annotation == null) {
                assertFalse(collection.remove(null));
            } else {
                assertThrows(annotation.expected(), () -> collection.remove(null));
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("remove(Object) with incompatible object")
        default void testRemoveIncompatibleObject(TestInfo testInfo) {
            Collection<T> collection = iterable();

            RemoveIncompatibleNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(RemoveIncompatibleNotSupported.class);

            if (annotation == null) {
                assertFalse(collection.remove(new IncompatibleObject()));
            } else {
                assertThrows(annotation.expected(), () -> collection.remove(new IncompatibleObject()));
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Collection#containsAll(Collection)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Collection#containsAll(Collection)} with a collection containing
     * {@code null} or an instance of an incompatible type will simply return {@code false}. If either is not the case, annotate your class with
     * {@link ContainsNullNotSupported} and/or {@link ContainsIncompatibleNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("containsAll(Collection)")
    interface ContainsAllTests<T> extends CollectionTests<T> {

        @Test
        @DisplayName("containsAll(Collection)")
        default void testContainsAll() {
            Collection<T> collection = iterable();

            List<T> expected = new ArrayList<>(expectedElements());
            T nonContained = nonContainedElements().iterator().next();

            for (int i = 0; i <= expected.size(); i++) {
                assertTrue(collection.containsAll(expected.subList(0, i)));

                List<T> withNonContained = new ArrayList<>(expected.subList(0, i));
                withNonContained.add(nonContained);

                assertFalse(collection.containsAll(withNonContained));
            }
        }

        @Test
        @DisplayName("containsAll(Collection) with a null collection")
        default void testContainsAllWithNullCollection() {
            Collection<T> collection = iterable();

            assertThrows(NullPointerException.class, () -> collection.containsAll(null));
        }

        @Test
        @DisplayName("containsAll(Collection) with a collection with a null")
        default void testContainsAllWithCollectionWithNull(TestInfo testInfo) {
            Collection<T> collection = iterable();

            Collection<T> c = new ArrayList<>(expectedElements());
            c.add(null);

            ContainsNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsNullNotSupported.class);

            if (annotation == null) {
                assertFalse(collection.containsAll(c));
            } else {
                assertThrows(annotation.expected(), () -> collection.containsAll(c));
            }
        }

        @Test
        @DisplayName("containsAll(Collection) with a collection with an incompatible object")
        default void testContainsAllWithCollectionWithIncompatibleObject(TestInfo testInfo) {
            Collection<T> collection = iterable();

            Collection<Object> c = new ArrayList<>(expectedElements());
            c.add(new IncompatibleObject());

            ContainsIncompatibleNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsIncompatibleNotSupported.class);

            if (annotation == null) {
                assertFalse(collection.containsAll(c));
            } else {
                assertThrows(annotation.expected(), () -> collection.containsAll(c));
            }
        }
    }

    /**
     * Contains tests for {@link Collection#removeAll(Collection)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Collection#removeAll(Collection)} with a collection containing {@code null}
     * or an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the case, annotate your
     * class with {@link RemoveNullNotSupported} and/or {@link RemoveIncompatibleNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("removeAll(Collection)")
    interface RemoveAllTests<T> extends CollectionTests<T> {

        @ParameterizedTest(name = "{0}: {1}")
        @ArgumentsSource(RemoveAllArgumentsProvider.class)
        @DisplayName("removeAll(Collection)")
        default void testRemoveAll(Collection<?> c, boolean expected) {
            Collection<T> collection = iterable();

            assertEquals(expected, collection.removeAll(c));

            Collection<T> expectedElements = expectedElements();
            if (expected) {
                expectedElements = new ArrayList<>(expectedElements);
                expectedElements.removeAll(c);
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("removeAll(Collection) with a null collection")
        default void testRemoveAllWithNullCollection() {
            Collection<T> collection = iterable();

            assertThrows(NullPointerException.class, () -> collection.removeAll(null));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("removeAll(Collection) with a collection with a null")
        default void testRemoveAllWithCollectionWithNull(TestInfo testInfo) {
            Collection<T> collection = iterable();

            Collection<?> c = Collections.singleton(null);

            RemoveNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(RemoveNullNotSupported.class);

            if (annotation == null) {
                assertFalse(collection.removeAll(c));
            } else {
                assertThrows(annotation.expected(), () -> collection.removeAll(c));
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("removeAll(Collection) with a collection with an incompatible object")
        default void testRemoveAllWithCollectionWithIncompatibleObject(TestInfo testInfo) {
            Collection<T> collection = iterable();

            Collection<?> c = Collections.singleton(new IncompatibleObject());

            RemoveIncompatibleNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(RemoveIncompatibleNotSupported.class);

            if (annotation == null) {
                assertFalse(collection.removeAll(c));
            } else {
                assertThrows(annotation.expected(), () -> collection.removeAll(c));
            }

            assertHasElements(collection, expectedElements(), fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Collection#removeIf(Predicate)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("removeIf(Predicate)")
    interface RemoveIfTests<T> extends CollectionTests<T> {

        @Test
        @DisplayName("removeIf(Predicate) with matching predicate")
        default void testRemoveIfWithMatchingPredicate() {
            Collection<T> collection = iterable();

            boolean isEmpty = collection.isEmpty();
            assertEquals(!isEmpty, collection.removeIf(e -> true));

            assertHasElements(collection, Collections.emptyList(), fixedOrder());
        }

        @Test
        @DisplayName("removeIf(Predicate) with non-matching predicate")
        default void testRemoveIfWithNonMatchingPredicate() {
            Collection<T> collection = iterable();

            assertFalse(collection.removeIf(e -> false));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("removeIf(Predicate) with null predicate")
        default void testRemoveIfWithNullPredicate() {
            Collection<T> collection = iterable();

            assertThrows(NullPointerException.class, () -> collection.removeIf(null));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Collection#retainAll(Collection)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Collection#retainAll(Collection)} with a collection containing {@code null}
     * or an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the case, annotate your
     * class with {@link ContainsNullNotSupported} and/or {@link ContainsIncompatibleNotSupported}
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("retainAll(Collection)")
    interface RetainAllTests<T> extends CollectionTests<T> {

        @ParameterizedTest(name = "{0}: {1}")
        @ArgumentsSource(RetainAllArgumentsProvider.class)
        @DisplayName("retainAll(Collection)")
        default void testRetainAll(Collection<?> c, boolean expected) {
            Collection<T> collection = iterable();

            assertEquals(expected, collection.retainAll(c));

            Collection<T> expectedElements = expectedElements();
            if (expected) {
                expectedElements = new ArrayList<>(expectedElements);
                expectedElements.retainAll(c);
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("retainAll(Collection) with a null collection")
        default void testRetainAllWithNullCollection() {
            Collection<T> collection = iterable();

            assertThrows(NullPointerException.class, () -> collection.retainAll(null));

            assertHasElements(collection, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("retainAll(Collection) with a collection with a null")
        default void testRetainAllWithCollectionWithNull(TestInfo testInfo) {
            Collection<T> collection = iterable();

            Collection<T> expectedElements = expectedElements();

            Collection<Object> c = new ArrayList<>(expectedElements);
            c.add(null);

            ContainsNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsNullNotSupported.class);

            if (annotation == null) {
                assertFalse(collection.retainAll(c));
            } else {
                assertThrows(annotation.expected(), () -> collection.retainAll(c));
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("retainAll(Collection) with a collection with an incompatible object")
        default void testRetainAllWithCollectionWithIncompatibleObject(TestInfo testInfo) {
            Collection<T> collection = iterable();

            Collection<T> expectedElements = expectedElements();

            Collection<Object> c = new ArrayList<>(expectedElements);
            c.add(new IncompatibleObject());

            ContainsIncompatibleNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsIncompatibleNotSupported.class);

            if (annotation == null) {
                assertFalse(collection.retainAll(c));
            } else {
                assertThrows(annotation.expected(), () -> collection.retainAll(c));
            }

            assertHasElements(collection, expectedElements, fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Collection#clear()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the collection to test.
     */
    @DisplayName("clear()")
    interface ClearTests<T> extends CollectionTests<T> {

        @Test
        @DisplayName("clear()")
        default void testClear() {
            Collection<T> collection = iterable();

            collection.clear();

            assertHasElements(collection, Collections.emptyList(), fixedOrder());
        }
    }

    /**
     * An arguments provider for {@link RemoveTests#testRemove(Object, boolean)}.
     *
     * @author Rob Spoor
     */
    final class RemoveArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) { // NOSONAR, keep supporting JUnit versions before 5.13
            RemoveTests<?> instance = (RemoveTests<?>) context.getRequiredTestInstance();

            Stream<Arguments> expected = instance.expectedElements().stream()
                    .map(e -> arguments(e, true));
            Stream<Arguments> notExpected = instance.nonContainedElements().stream()
                    .map(e -> arguments(e, false));

            return Stream.of(expected, notExpected)
                    .flatMap(Function.identity());
        }
    }

    /**
     * An arguments provider for {@link RemoveAllTests#testRemoveAll(Collection, boolean)}.
     *
     * @author Rob Spoor
     */
    final class RemoveAllArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) { // NOSONAR, keep supporting JUnit versions before 5.13
            RemoveAllTests<?> instance = (RemoveAllTests<?>) context.getRequiredTestInstance();

            List<?> expected = new ArrayList<>(instance.expectedElements());
            Object nonContained = instance.nonContainedElements().iterator().next();

            List<Arguments> arguments = new ArrayList<>();
            for (int i = 0; i <= expected.size(); i++) {
                arguments.add(arguments(new ArrayList<>(expected.subList(0, i)), i > 0));

                List<Object> withNonContained = new ArrayList<>(expected.subList(0, i));
                withNonContained.add(nonContained);
                arguments.add(arguments(withNonContained, i > 0));
            }

            return arguments.stream();
        }
    }

    /**
     * An arguments provider for {@link RetainAllTests#testRetainAll(Collection, boolean)}.
     *
     * @author Rob Spoor
     */
    final class RetainAllArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) { // NOSONAR, keep supporting JUnit versions before 5.13
            RetainAllTests<?> instance = (RetainAllTests<?>) context.getRequiredTestInstance();

            List<?> expected = new ArrayList<>(instance.expectedElements());
            Object nonContained = instance.nonContainedElements().iterator().next();

            List<Arguments> arguments = new ArrayList<>();
            for (int i = 0; i <= expected.size(); i++) {
                arguments.add(arguments(new ArrayList<>(expected.subList(0, i)), i < expected.size()));

                List<Object> withNonContained = new ArrayList<>(expected.subList(0, i));
                withNonContained.add(nonContained);
                arguments.add(arguments(withNonContained, i < expected.size()));
            }

            return arguments.stream();
        }
    }
}
