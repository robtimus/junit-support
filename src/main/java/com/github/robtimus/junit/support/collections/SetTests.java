/*
 * SetTests.java
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
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
import com.github.robtimus.junit.support.collections.annotation.StoreNullNotSupported;

/**
 * Base interface for testing separate {@link Set} functionalities.
 *
 * @author Rob Spoor
 * @param <T> The element type of the set to test.
 */
public interface SetTests<T> extends CollectionTests<T> {

    @Override
    Set<T> iterable();

    /**
     * Contains tests for {@link Set#add(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Set#add(Object)} with {@code null} will simply add the {@code null}.
     * If this is not the case, annotate your class with {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the set to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("add(Object)")
    interface AddTests<T> extends SetTests<T> {

        /**
         * Adds an element to a list of expected elements. If {@link #fixedOrder()} returns {@code true}, it may be necessary to override this method
         * to ensure the expected elements remain in the correct fixed order.
         *
         * @param expected The existing expected elements.
         * @param element The element to add.
         */
        default void addElementToExpected(List<T> expected, T element) {
            expected.add(element);
        }

        @ParameterizedTest(name = "{0}: {1}")
        @ArgumentsSource(AddArgumentsProvider.class)
        @DisplayName("add(Object)")
        default void testAdd(T o, boolean expected) {
            Set<T> set = iterable();

            assertEquals(expected, set.add(o));

            Collection<T> expectedElements = expectedElements();
            if (expected) {
                expectedElements = new ArrayList<>(expectedElements);
                addElementToExpected((List<T>) expectedElements, o);
            }

            assertHasElements(set, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("add(Object) with null")
        default void testAddNull(TestInfo testInfo) {
            Set<T> set = iterable();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            Collection<T> expectedElements = expectedElements();

            if (annotation == null) {
                assertTrue(set.add(null));

                expectedElements = new ArrayList<>(expectedElements);
                addElementToExpected((List<T>) expectedElements, null);
            } else {
                assertThrows(annotation.expected(), () -> set.add(null));
            }

            assertHasElements(set, expectedElements, fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Set#addAll(Collection)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Set#addAll(Collection)} with a collection containing {@code null} will
     * simply add the {@code null}. If this is not the case, annotate your class with {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the set to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("addAll(Collection)")
    interface AddAllTests<T> extends SetTests<T> {

        /**
         * Adds multiple elements to a list of expected elements. If {@link #fixedOrder()} returns {@code true}, it may be necessary to override this
         * method to ensure the expected elements remain in the correct fixed order.
         * <p>
         * Note that you don't need to worry about duplicates, that will be taken care of.
         *
         * @param expected The existing expected elements.
         * @param elements The elements to add.
         */
        default void addElementsToExpected(List<T> expected, Collection<? extends T> elements) {
            expected.addAll(elements);
        }

        @ParameterizedTest(name = "{0}: {1}")
        @ArgumentsSource(AddAllArgumentsProvider.class)
        @DisplayName("addAll(Collection)")
        default void testAddAll(Collection<? extends T> c, boolean expected) {
            Set<T> set = iterable();

            assertEquals(expected, set.addAll(c));

            Collection<T> expectedElements = expectedElements();
            if (expected) {
                expectedElements = new ArrayList<>(expectedElements);
                addElementsToExpected((List<T>) expectedElements, c);
                expectedElements = new LinkedHashSet<>(expectedElements);
            }

            assertHasElements(set, expectedElements, fixedOrder());
        }

        @Test
        @DisplayName("addAll(Collection) with a null collection")
        default void testAddAllWithNullCollection() {
            Set<T> set = iterable();

            assertThrows(NullPointerException.class, () -> set.addAll(null));

            assertHasElements(set, expectedElements(), fixedOrder());
        }

        @Test
        @DisplayName("addAll(Collection) with a collection with a null")
        default void testAddAllWithCollectionWithNull(TestInfo testInfo) {
            Set<T> set = iterable();

            Collection<T> c = Collections.singleton(null);

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            Collection<T> expectedElements = expectedElements();

            if (annotation == null) {
                assertTrue(set.addAll(c));

                expectedElements = new ArrayList<>(expectedElements);
                addElementsToExpected((List<T>) expectedElements, c);
                expectedElements = new LinkedHashSet<>(expectedElements);
            } else {
                assertThrows(annotation.expected(), () -> set.addAll(c));
            }

            assertHasElements(set, expectedElements, fixedOrder());
        }
    }

    /**
     * Contains tests for {@link Set#equals(Object)}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the set to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("equals(Object)")
    interface EqualsTests<T> extends SetTests<T> {

        @ParameterizedTest(name = "{0}: {1}")
        @ArgumentsSource(EqualsArgumentsProvider.class)
        @DisplayName("equals(Object)")
        default void testEquals(Set<?> other, boolean expected) {
            Set<T> set = iterable();

            if (expected) {
                assertEquals(other, set);
            } else {
                assertNotEquals(other, set);
            }
        }

        @Test
        @DisplayName("equals(Object) with self")
        default void testEqualsSelf() {
            Set<T> set = iterable();

            assertEquals(set, set);
        }

        @Test
        @DisplayName("equals(Object) with null")
        default void testEqualsNull() {
            Set<T> set = iterable();

            assertNotEquals(null, set);
        }

        @Test
        @DisplayName("equals(Object) with list")
        default void testEqualsList() {
            Set<T> set = iterable();

            assertNotEquals(new ArrayList<>(set), set);
        }
    }

    /**
     * Contains tests for {@link Set#hashCode()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the set to test.
     */
    @DisplayName("hashCode()")
    interface HashCodeTests<T> extends SetTests<T> {

        @Test
        @DisplayName("hashCode()")
        default void testHashCode() {
            Set<T> set = iterable();

            int expected = expectedElements().stream()
                    .mapToInt(Object::hashCode)
                    .sum();

            assertEquals(expected, set.hashCode());
        }
    }

    /**
     * Contains tests for {@link Set#spliterator()}.
     *
     * @author Rob Spoor
     * @param <T> The element type of the set to test.
     */
    @DisplayName("spliterator()")
    interface SpliteratorTests<T> extends SetTests<T> {

        @Test
        @DisplayName("spliterator() has DISTINCT characteristic")
        default void testSpliteratorHasDistinctCharacteristic() {
            Set<T> set = iterable();

            Spliterator<T> spliterator = set.spliterator();

            assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT));
        }
    }

    /**
     * An arguments provider for {@link AddTests#testAdd(Object, boolean)}.
     *
     * @author Rob Spoor
     */
    final class AddArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            AddTests<?> instance = (AddTests<?>) context.getRequiredTestInstance();

            Stream<Arguments> expected = instance.expectedElements().stream()
                    .map(e -> arguments(e, false));
            Stream<Arguments> notExpected = instance.nonContainedElements().stream()
                    .map(e -> arguments(e, true));

            return Stream.of(expected, notExpected)
                    .flatMap(Function.identity());
        }
    }

    /**
     * An arguments provider for {@link AddAllTests#testAddAll(Collection, boolean)}.
     *
     * @author Rob Spoor
     */
    final class AddAllArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            AddAllTests<?> instance = (AddAllTests<?>) context.getRequiredTestInstance();

            List<?> expected = new ArrayList<>(instance.expectedElements());
            Object nonContained = instance.nonContainedElements().iterator().next();

            List<Arguments> arguments = new ArrayList<>();
            for (int i = 0; i <= expected.size(); i++) {
                arguments.add(arguments(new ArrayList<>(expected.subList(0, i)), false));

                List<Object> withNonContained = new ArrayList<>(expected.subList(0, i));
                withNonContained.add(nonContained);
                arguments.add(arguments(withNonContained, true));
            }

            return arguments.stream();
        }
    }

    /**
     * An arguments provider for {@link EqualsTests#testEquals(Set, boolean)}.
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
            arguments.add(arguments(new HashSet<>(expected), true));

            if (!expected.isEmpty()) {
                arguments.add(arguments(new HashSet<>(expected.subList(0, expected.size() - 1)), false));
            }

            Set<Object> withNonContained = new HashSet<>(expected);
            withNonContained.add(nonContained);
            arguments.add(arguments(withNonContained, false));

            return arguments.stream();
        }
    }
}
