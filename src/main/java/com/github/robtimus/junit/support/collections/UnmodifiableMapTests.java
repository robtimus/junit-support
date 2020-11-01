/*
 * UnmodifiableMapTests.java
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import com.github.robtimus.junit.support.collections.annotation.ContainsIncompatibleNotSupported;
import com.github.robtimus.junit.support.collections.annotation.ContainsNullNotSupported;
import com.github.robtimus.junit.support.collections.annotation.RemoveIncompatibleNotSupported;
import com.github.robtimus.junit.support.collections.annotation.RemoveNullNotSupported;

/**
 * Base interface for testing separate {@link Map} functionalities for unmodifiable maps.
 *
 * @author Rob Spoor
 * @param <K> The key type of the map to test.
 * @param <V> The value type of the map to test.
 */
public interface UnmodifiableMapTests<K, V> extends MapTests<K, V> {

    /**
     * Contains tests for {@link Map#put(Object, Object)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("get(Object)")
    interface PutTests<K, V> extends MapTests<K, V> {

        /**
         * Returns a unary operator that can be used to create new values to set with {@link Map#put(Object, Object)}.
         *
         * @return A unary operator that can be used to create new values to set with {@link Map#put(Object, Object)}.
         */
        UnaryOperator<V> replaceValueOperator();

        @Test
        @DisplayName("put(Object, Object) with contained entries")
        default void testPutWithContainedEntries() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                V value = entry.getValue();

                // if the value stays the same, either it does nothing or it throws an exception
                try {
                    assertEquals(value, map.put(entry.getKey(), value));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("put(Object, Object) with updated entries")
        default void testPutWithUpdatedEntries() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            UnaryOperator<V> operator = replaceValueOperator();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                V value = entry.getValue();

                assertThrows(UnsupportedOperationException.class, () -> map.put(entry.getKey(), operator.apply(value)));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("put(Object, Object) with null key")
        default void testPutWithNullKey() {
            Map<K, V> map = createMap();

            V nonContainedValue = nonContainedEntries().values().iterator().next();

            assertThrows(UnsupportedOperationException.class, () -> map.put(null, nonContainedValue));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("put(Object, Object) with null value")
        default void testPutWithNullValue() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.put(entry.getKey(), null));
            }

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#remove(Object)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("remove(Object)")
    interface RemoveTests<K, V> extends UnmodifiableMapTests<K, V> {

        @Test
        @DisplayName("remove(Object) with contained elements")
        default void testRemoveContainedElements() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.remove(key));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("remove(Object) with non-contained elements")
        default void testRemoveNonContainedElements() {
            Map<K, V> map = createMap();

            for (K key : nonContainedEntries().keySet()) {
                // with a non-contained object, either it does nothing or it throws an exception
                try {
                    assertNull(map.remove(key));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object) with null")
        default void testRemoveNull() {
            Map<K, V> map = createMap();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertNull(map.remove(null));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object) with an incompatible object")
        default void testRemoveIncompatibleObject() {
            Map<K, V> map = createMap();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertNull(map.remove(new IncompatibleObject()));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#putAll(Map)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("putAll(Map)")
    interface PutAllTests<K, V> extends UnmodifiableMapTests<K, V> {

        /**
         * Returns a unary operator that can be used to create new values to set with {@link Map#put(Object, Object)}.
         *
         * @return A unary operator that can be used to create new values to set with {@link Map#put(Object, Object)}.
         */
        UnaryOperator<V> replaceValueOperator();

        @Test
        @DisplayName("putAll(Map) with contained entries")
        default void testPutAllWithContainedEntries() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                Map<K, V> m = Collections.singletonMap(entry.getKey(), entry.getValue());

                // if the value stays the same, either it does nothing or it throws an exception
                try {
                    map.putAll(m);
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("putAll(Object, Object) with updated entries")
        default void testPutAllWithUpdatedEntries() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            UnaryOperator<V> operator = replaceValueOperator();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                V value = entry.getValue();
                Map<K, V> m = Collections.singletonMap(entry.getKey(), operator.apply(value));

                assertThrows(UnsupportedOperationException.class, () -> map.putAll(m));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("putAll(Map) with non-contained entries")
        default void testPutAllWithNonContainedEntries() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                Map<K, V> m = Collections.singletonMap(entry.getKey(), entry.getValue());

                assertThrows(UnsupportedOperationException.class, () -> map.putAll(m));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("putAll(Map) with an empty map")
        default void testPutAllWithEmptyMap() {
            Map<K, V> map = createMap();

            // with an empty map, either it does nothing or it throws an exception
            try {
                map.putAll(Collections.emptyMap());
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("putAll(Collection) with null")
        default void testPutAllWithNull() {
            Map<K, V> map = createMap();

            Exception exception = assertThrows(Exception.class, () -> map.putAll(null));
            assertThat(exception, either(instanceOf(UnsupportedOperationException.class)).or(instanceOf(NullPointerException.class)));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("putAll(Map) with a map with a null key")
        default void testPutAllWithMapWithNullKey() {
            Map<K, V> map = createMap();

            V nonContainedValue = nonContainedEntries().values().iterator().next();
            Map<K, V> m = Collections.singletonMap(null, nonContainedValue);

            assertThrows(UnsupportedOperationException.class, () -> map.putAll(m));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("putAll(Object, Object) with a map with a null value")
        default void testPutAllWithMapWithNullValue() {
            Map<K, V> map = createMap();

            K nonContainedKey = nonContainedEntries().keySet().iterator().next();
            Map<K, V> m = Collections.singletonMap(nonContainedKey, null);

            assertThrows(UnsupportedOperationException.class, () -> map.putAll(m));

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#clear()} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("clear()")
    interface ClearTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("clear()")
        default void testClear() {
            Map<K, V> map = createMap();

            assertThrows(UnsupportedOperationException.class, map::clear);

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#keySet()} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("keySet()")
    interface KeySetTests<K, V> extends UnmodifiableMapTests<K, V>, UnmodifiableSetTests<K> {

        @Override
        default Set<K> createIterable() {
            return createMap().keySet();
        }

        @Override
        default Collection<K> expectedElements() {
            return expectedEntries().keySet();
        }

        @Override
        default Collection<K> nonContainedElements() {
            return nonContainedEntries().keySet();
        }

        /**
         * Contains tests for {@link Set#iterator()} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("forEach(Consumer)")
        interface IteratorTests<K, V> extends KeySetTests<K, V>, UnmodifiableIteratorTests<K> {

            @Override
            default Set<K> createIterable() {
                return KeySetTests.super.createIterable();
            }

            @Override
            default Collection<K> expectedElements() {
                return KeySetTests.super.expectedElements();
            }

            /**
             * Contains tests for {@link Iterator#hasNext()} and {@link Iterator#next()} for key set iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("iteration")
            interface IterationTests<K, V>
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.collections.IteratorTests.IterationTests<K> {
                // no new tests needed
            }

            /**
             * Contains tests for {@link Iterator#remove()} for key set iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("remove()")
            interface RemoveTests<K, V> extends IteratorTests<K, V>, UnmodifiableIteratorTests.RemoveTests<K> {

                @Override
                @Test
                @DisplayName("remove() throws UnsupportedOperationException")
                default void testRemove() {
                    Map<K, V> map = createMap();
                    Set<K> keySet = map.keySet();
                    Iterator<K> iterator = keySet.iterator();

                    while (iterator.hasNext()) {
                        iterator.next();

                        assertThrows(UnsupportedOperationException.class, iterator::remove);
                    }

                    Map<K, V> expectedEntries = expectedEntries();
                    Set<K> expectedElements = expectedEntries.keySet();

                    assertHasElements(keySet, expectedElements, fixedOrder());
                    assertEquals(expectedEntries, map);
                }
            }

            /**
             * Contains tests for {@link Iterator#forEachRemaining(Consumer)} for key set iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("forEachRemaining(Consumer)")
            interface ForEachRemainingTests<K, V>
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.collections.IteratorTests.ForEachRemainingTests<K> {
                // no new tests needed
            }
        }

        /**
         * Contains tests for {@link Iterable#forEach(Consumer)} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("forEach(Consumer)")
        interface ForEachTests<K, V> extends KeySetTests<K, V>, IterableTests.ForEachTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#contains(Object)} for key sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#contains(Object)} with {@code null} or an instance of an
         * incompatible type will simply return {@code false}. If either is not the case, annotate your class with {@link ContainsNullNotSupported}
         * and/or {@link ContainsIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("contains(Object)")
        interface ContainsTests<K, V> extends KeySetTests<K, V>, CollectionTests.ContainsTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#toArray()} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("toArray()")
        interface ToObjectArrayTests<K, V> extends KeySetTests<K, V>, CollectionTests.ToObjectArrayTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#toArray(Object[])} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("toArray(Object[])")
        interface ToArrayTests<K, V> extends KeySetTests<K, V>, CollectionTests.ToArrayTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#add(Object)} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("add(Object)")
        interface AddTests<K, V> extends KeySetTests<K, V>, UnmodifiableCollectionTests.AddTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#remove(Object)} for key sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#remove(Object)} with {@code null} or an instance of an incompatible
         * type will simply return {@code false}. If either is not the case, annotate your class with {@link RemoveNullNotSupported} and/or
         * {@link RemoveIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("remove(Object)")
        interface RemoveTests<K, V> extends KeySetTests<K, V>, UnmodifiableCollectionTests.RemoveTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#containsAll(Collection)} for key sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#containsAll(Collection)} with a collection containing {@code null}
         * or an instance of an incompatible type will simply return {@code false}. If either is not the case, annotate your class with
         * {@link ContainsNullNotSupported} and/or {@link ContainsIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("containsAll(Collection)")
        interface ContainsAllTests<K, V> extends KeySetTests<K, V>, CollectionTests.ContainsAllTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#addAll(Collection)} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("addAll(Collection)")
        interface AddAllTests<K, V> extends KeySetTests<K, V>, UnmodifiableCollectionTests.AddAllTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#removeAll(Collection)} for key sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#removeAll(Collection)} with a collection containing {@code null}
         * or an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the case, annotate
         * your class with {@link RemoveNullNotSupported} and/or {@link RemoveIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("removeAll(Collection)")
        interface RemoveAllTests<K, V> extends KeySetTests<K, V>, UnmodifiableCollectionTests.RemoveAllTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#removeIf(Predicate)} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("removeIf(Predicate)")
        interface RemoveIfTests<K, V> extends KeySetTests<K, V>, UnmodifiableCollectionTests.RemoveIfTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#retainAll(Collection)} for key sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#retainAll(Collection)} with a collection containing {@code null}
         * or an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the case, annotate
         * your class with {@link ContainsNullNotSupported} and/or {@link ContainsIncompatibleNotSupported}
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("retainAll(Collection)")
        interface RetainAllTests<K, V> extends KeySetTests<K, V>, UnmodifiableCollectionTests.RetainAllTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#clear()} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("clear()")
        interface ClearTests<K, V> extends KeySetTests<K, V>, UnmodifiableCollectionTests.ClearTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#equals(Object)} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("equals(Object)")
        interface EqualsTests<K, V> extends KeySetTests<K, V>, SetTests.EqualsTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#hashCode()} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("hashCode()")
        interface HashCodeTests<K, V> extends KeySetTests<K, V>, SetTests.HashCodeTests<K> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#spliterator()} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("spliterator()")
        interface SpliteratorTests<K, V> extends KeySetTests<K, V>, SetTests.SpliteratorTests<K> {
            // no new tests needed
        }
    }

    /**
     * Contains tests for {@link Map#values()}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("values()")
    interface ValuesTests<K, V> extends UnmodifiableMapTests<K, V>, CollectionTests<V> {

        @Override
        default Collection<V> createIterable() {
            return createMap().values();
        }

        @Override
        default Collection<V> expectedElements() {
            return expectedEntries().values();
        }

        @Override
        default Collection<V> nonContainedElements() {
            return nonContainedEntries().values();
        }

        /**
         * Contains tests for {@link Collection#iterator()} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("forEach(Consumer)")
        interface IteratorTests<K, V> extends ValuesTests<K, V>, UnmodifiableIteratorTests<V> {

            @Override
            default Collection<V> createIterable() {
                return ValuesTests.super.createIterable();
            }

            @Override
            default Collection<V> expectedElements() {
                return ValuesTests.super.expectedElements();
            }

            /**
             * Contains tests for {@link Iterator#hasNext()} and {@link Iterator#next()} for values collection iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("iteration")
            interface IterationTests<K, V>
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.collections.IteratorTests.IterationTests<V> {
                // no new tests needed
            }

            /**
             * Contains tests for {@link Iterator#remove()} for values collection iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("remove()")
            interface RemoveTests<K, V> extends IteratorTests<K, V>, UnmodifiableIteratorTests.RemoveTests<V> {

                @Override
                @Test
                @DisplayName("remove() throws UnsupportedOperationException")
                default void testRemove() {
                    Map<K, V> map = createMap();
                    Collection<V> values = map.values();
                    Iterator<V> iterator = values.iterator();

                    while (iterator.hasNext()) {
                        iterator.next();

                        assertThrows(UnsupportedOperationException.class, iterator::remove);
                    }

                    Map<K, V> expectedEntries = expectedEntries();
                    Collection<V> expectedElements = expectedEntries.values();

                    assertHasElements(values, expectedElements, fixedOrder());
                    assertEquals(expectedEntries, map);
                }
            }

            /**
             * Contains tests for {@link Iterator#forEachRemaining(Consumer)} for values collection iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("forEachRemaining(Consumer)")
            interface ForEachRemainingTests<K, V>
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.collections.IteratorTests.ForEachRemainingTests<V> {
                // no new tests needed
            }
        }

        /**
         * Contains tests for {@link Iterable#forEach(Consumer)} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("forEach(Consumer)")
        interface ForEachTests<K, V> extends ValuesTests<K, V>, IterableTests.ForEachTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#contains(Object)} for values collections.
         * <p>
         * By default, the tests in this interface assume that calling {@link Collection#contains(Object)} with {@code null} or an instance of an
         * incompatible type will simply return {@code false}. If either is not the case, annotate your class with {@link ContainsNullNotSupported}
         * and/or {@link ContainsIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("contains(Object)")
        interface ContainsTests<K, V> extends ValuesTests<K, V>, CollectionTests.ContainsTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#toArray()} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("toArray()")
        interface ToObjectArrayTests<K, V> extends ValuesTests<K, V>, CollectionTests.ToObjectArrayTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#toArray(Object[])} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("toArray(Object[])")
        interface ToArrayTests<K, V> extends ValuesTests<K, V>, CollectionTests.ToArrayTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#add(Object)} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("add(Object)")
        interface AddTests<K, V> extends ValuesTests<K, V>, UnmodifiableCollectionTests.AddTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#remove(Object)} for values collections.
         * <p>
         * By default, the tests in this interface assume that calling {@link Collection#remove(Object)} with {@code null} or an instance of an
         * incompatible type will simply return {@code false}. If either is not the case, annotate your class with {@link RemoveNullNotSupported}
         * and/or {@link RemoveIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("remove(Object)")
        interface RemoveTests<K, V> extends ValuesTests<K, V>, UnmodifiableCollectionTests.RemoveTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#containsAll(Collection)} for values collections.
         * <p>
         * By default, the tests in this interface assume that calling {@link Collection#containsAll(Collection)} with a collection containing
         * {@code null} or an instance of an incompatible type will simply return {@code false}. If either is not the case, annotate your class with
         * {@link ContainsNullNotSupported} and/or {@link ContainsIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("containsAll(Collection)")
        interface ContainsAllTests<K, V> extends ValuesTests<K, V>, CollectionTests.ContainsAllTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#addAll(Collection)} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("addAll(Collection)")
        interface AddAllTests<K, V> extends ValuesTests<K, V>, UnmodifiableCollectionTests.AddAllTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#removeAll(Collection)} for values collections.
         * <p>
         * By default, the tests in this interface assume that calling {@link Collection#removeAll(Collection)} with a collection containing
         * {@code null} or an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the
         * case, annotate your class with {@link RemoveNullNotSupported} and/or {@link RemoveIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("removeAll(Collection)")
        interface RemoveAllTests<K, V> extends ValuesTests<K, V>, UnmodifiableCollectionTests.RemoveAllTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#removeIf(Predicate)} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("removeIf(Predicate)")
        interface RemoveIfTests<K, V> extends ValuesTests<K, V>, UnmodifiableCollectionTests.RemoveIfTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#retainAll(Collection)} for values collections.
         * <p>
         * By default, the tests in this interface assume that calling {@link Collection#retainAll(Collection)} with a collection containing
         * {@code null} or an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the
         * case, annotate your class with {@link ContainsNullNotSupported} and/or {@link ContainsIncompatibleNotSupported}
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("retainAll(Collection)")
        interface RetainAllTests<K, V> extends ValuesTests<K, V>, UnmodifiableCollectionTests.RetainAllTests<V> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Collection#clear()} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("clear()")
        interface ClearTests<K, V> extends ValuesTests<K, V>, UnmodifiableCollectionTests.ClearTests<V> {
            // no new tests needed
        }
    }

    /**
     * Contains tests for {@link Map#entrySet()}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("entrySet()")
    interface EntrySetTests<K, V> extends UnmodifiableMapTests<K, V>, SetTests<Map.Entry<K, V>> {

        @Override
        default Set<Entry<K, V>> createIterable() {
            return createMap().entrySet();
        }

        @Override
        default Collection<Entry<K, V>> expectedElements() {
            return expectedEntries().entrySet();
        }

        @Override
        default Collection<Entry<K, V>> nonContainedElements() {
            return nonContainedEntries().entrySet();
        }

        /**
         * Contains tests for {@link Set#iterator()} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("forEach(Consumer)")
        interface IteratorTests<K, V> extends EntrySetTests<K, V>, UnmodifiableIteratorTests<Map.Entry<K, V>> {

            @Override
            default Set<Map.Entry<K, V>> createIterable() {
                return EntrySetTests.super.createIterable();
            }

            @Override
            default Collection<Map.Entry<K, V>> expectedElements() {
                return EntrySetTests.super.expectedElements();
            }

            /**
             * Contains tests for {@link Iterator#hasNext()} and {@link Iterator#next()} for entry set iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("iteration")
            interface IterationTests<K, V>
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.collections.IteratorTests.IterationTests<Map.Entry<K, V>> {
                // no new tests needed
            }

            /**
             * Contains tests for {@link Iterator#remove()} for entry set iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("remove()")
            interface RemoveTests<K, V> extends IteratorTests<K, V>, UnmodifiableIteratorTests.RemoveTests<Map.Entry<K, V>> {

                @Override
                @Test
                @DisplayName("remove() throws UnsupportedOperationException")
                default void testRemove() {
                    Map<K, V> map = createMap();
                    Set<Map.Entry<K, V>> entrySet = map.entrySet();
                    Iterator<Map.Entry<K, V>> iterator = entrySet.iterator();

                    while (iterator.hasNext()) {
                        iterator.next();

                        assertThrows(UnsupportedOperationException.class, iterator::remove);
                    }

                    Map<K, V> expectedEntries = expectedEntries();
                    Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                    assertHasElements(entrySet, expectedElements, fixedOrder());
                    assertEquals(expectedEntries, map);
                }
            }

            /**
             * Contains tests for {@link Iterator#forEachRemaining(Consumer)} for entry set iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("forEachRemaining(Consumer)")
            interface ForEachRemainingTests<K, V>
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.collections.IteratorTests.ForEachRemainingTests<Map.Entry<K, V>> {
                // no new tests needed
            }
        }

        /**
         * Contains tests for {@link Iterable#forEach(Consumer)} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("forEach(Consumer)")
        interface ForEachTests<K, V> extends EntrySetTests<K, V>, IterableTests.ForEachTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#contains(Object)} for entry sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#contains(Object)} with {@code null} or an instance of an
         * incompatible type will simply return {@code false}. If either is not the case, annotate your class with {@link ContainsNullNotSupported}
         * and/or {@link ContainsIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("contains(Object)")
        interface ContainsTests<K, V> extends EntrySetTests<K, V>, CollectionTests.ContainsTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#toArray()} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("toArray()")
        interface ToObjectArrayTests<K, V> extends EntrySetTests<K, V>, CollectionTests.ToObjectArrayTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#toArray(Object[])} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("toArray(Object[])")
        interface ToArrayTests<K, V> extends EntrySetTests<K, V>, CollectionTests.ToArrayTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#add(Object)} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("add(Object)")
        interface AddTests<K, V> extends EntrySetTests<K, V>, UnmodifiableCollectionTests.AddTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#remove(Object)} for entry sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#remove(Object)} with {@code null} or an instance of an incompatible
         * type will simply return {@code false}. If either is not the case, annotate your class with {@link RemoveNullNotSupported} and/or
         * {@link RemoveIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("remove(Object)")
        interface RemoveTests<K, V> extends EntrySetTests<K, V>, UnmodifiableCollectionTests.RemoveTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#containsAll(Collection)} for entry sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#containsAll(Collection)} with a collection containing {@code null}
         * or an instance of an incompatible type will simply return {@code false}. If either is not the case, annotate your class with
         * {@link ContainsNullNotSupported} and/or {@link ContainsIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("containsAll(Collection)")
        interface ContainsAllTests<K, V> extends EntrySetTests<K, V>, CollectionTests.ContainsAllTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#addAll(Collection)} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("addAll(Collection)")
        interface AddAllTests<K, V> extends EntrySetTests<K, V>, UnmodifiableCollectionTests.AddAllTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#removeAll(Collection)} for entry sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#removeAll(Collection)} with a collection containing {@code null}
         * or an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the case, annotate
         * your class with {@link RemoveNullNotSupported} and/or {@link RemoveIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("removeAll(Collection)")
        interface RemoveAllTests<K, V> extends EntrySetTests<K, V>, UnmodifiableCollectionTests.RemoveAllTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#removeIf(Predicate)} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("removeIf(Predicate)")
        interface RemoveIfTests<K, V> extends EntrySetTests<K, V>, UnmodifiableCollectionTests.RemoveIfTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#retainAll(Collection)} for entry sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#retainAll(Collection)} with a collection containing {@code null}
         * or an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the case, annotate
         * your class with {@link ContainsNullNotSupported} and/or {@link ContainsIncompatibleNotSupported}
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("retainAll(Collection)")
        interface RetainAllTests<K, V> extends EntrySetTests<K, V>, UnmodifiableCollectionTests.RetainAllTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#clear()} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("clear()")
        interface ClearTests<K, V> extends EntrySetTests<K, V>, UnmodifiableCollectionTests.ClearTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#equals(Object)} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("equals(Object)")
        interface EqualsTests<K, V> extends EntrySetTests<K, V>, SetTests.EqualsTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#hashCode()} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("hashCode()")
        interface HashCodeTests<K, V> extends EntrySetTests<K, V>, SetTests.HashCodeTests<Map.Entry<K, V>> {
            // no new tests needed
        }

        /**
         * Contains tests for {@link Set#spliterator()} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("spliterator()")
        interface SpliteratorTests<K, V> extends EntrySetTests<K, V>, SetTests.SpliteratorTests<Map.Entry<K, V>> {
            // no new tests needed
        }
    }

    /**
     * Contains tests for {@link Map#replaceAll(BiFunction)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("replaceAll(BiFunction)")
    interface ReplaceAllTests<K, V> extends MapTests<K, V> {

        /**
         * Returns a function that can be used with {@link Map#replaceAll(BiFunction)}.
         *
         * @return A function that can be used with {@link Map#replaceAll(BiFunction)}.
         */
        BiFunction<K, V, V> replaceValueFunction();

        @Test
        @DisplayName("replaceAll(BiFunction)")
        default void testReplaceAll() {
            Map<K, V> map = createMap();

            BiFunction<K, V, V> function = replaceValueFunction();

            if (map.isEmpty()) {
                // with an empty map, either it does nothing or it throws an exception
                try {
                    map.replaceAll(function);
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            } else {
                assertThrows(UnsupportedOperationException.class, () -> map.replaceAll(function));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replaceAll(BiFunction) with null function")
        default void testReplaceAllWithNullOperator() {
            Map<K, V> map = createMap();

            Exception exception = assertThrows(Exception.class, () -> map.replaceAll(null));
            assertThat(exception, either(instanceOf(UnsupportedOperationException.class)).or(instanceOf(NullPointerException.class)));

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#putIfAbsent(Object, Object)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("putIfAbsent(Object, Object)")
    interface PutIfAbsentTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("putIfAbsent(Object, Object)")
        default void testPutIfAbsent() {
            Map<K, V> map = createMap();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                // if the value stays the same, either it does nothing or it throws an exception
                try {
                    assertEquals(entry.getValue(), map.putIfAbsent(entry.getKey(), nonContained));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertEquals(expectedEntries, map);

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent(entry.getKey(), entry.getValue()));
            }

            assertEquals(expectedEntries, map);
        }
    }

    /**
     * Contains tests for {@link Map#remove(Object, Object)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("remove(Object, Object)")
    interface RemoveExactValueTests<K, V> extends UnmodifiableMapTests<K, V> {

        @Test
        @DisplayName("remove(Object, Object) with contained elements")
        default void testRemoveExactValueWithContainedElements() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.remove(entry.getKey(), entry.getValue()));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("remove(Object, Object) with non-contained elements")
        default void testRemoveExactValueWithNonContainedElements() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                // with a non-contained object, either it does nothing or it throws an exception
                try {
                    assertNull(map.remove(entry.getKey(), entry.getValue()));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with null key")
        default void testRemoveExactValueWithNullKey() {
            Map<K, V> map = createMap();

            V nonContained = nonContainedEntries().values().iterator().next();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertNull(map.remove(null, nonContained));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with an incompatible key")
        default void testRemoveExactValueWithIncompatibleObjectKey() {
            Map<K, V> map = createMap();

            V nonContained = nonContainedEntries().values().iterator().next();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertNull(map.remove(new IncompatibleObject(), nonContained));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with null value")
        default void testRemoveExactValueWithNullValue() {
            Map<K, V> map = createMap();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertNull(map.remove(nonContained, null));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with an incompatible value")
        default void testRemoveExactValueWithIncompatibleObjectValue() {
            Map<K, V> map = createMap();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertNull(map.remove(nonContained, new IncompatibleObject()));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#replace(Object, Object, Object)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("replace(Object, Object, Object)")
    interface ReplaceExactValueTests<K, V> extends UnmodifiableMapTests<K, V> {

        @Test
        @DisplayName("replace(Object, Object, Object) with contained elements")
        default void testReplaceExactValueWithContainedElements() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.replace(entry.getKey(), entry.getValue(), nonContained));

                // with the same value, either it does nothing or it throws an exception
                try {
                    assertTrue(map.replace(entry.getKey(), entry.getValue(), entry.getValue()));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("replace(Object, Object, Object) with non-contained elements")
        default void testReplaceExactValueWithNonContainedElements() {
            Map<K, V> map = createMap();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                // with a non-contained object, either it does nothing or it throws an exception
                try {
                    assertFalse(map.replace(entry.getKey(), entry.getValue(), nonContained));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replace(Object, Object, Object) with null key")
        default void testReplaceExactValueWithNullKey() {
            Map<K, V> map = createMap();

            V nonContained = nonContainedEntries().values().iterator().next();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertFalse(map.replace(null, nonContained, nonContained));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replace(Object, Object, Object) with null value")
        default void testReplaceExactValueWithNullValue() {
            Map<K, V> map = createMap();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertFalse(map.replace(nonContained, null, null));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#replace(Object, Object)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("replace(Object, Object)")
    interface ReplaceTests<K, V> extends UnmodifiableMapTests<K, V> {

        @Test
        @DisplayName("replace(Object, Object) with contained elements")
        default void testReplaceWithContainedElements() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.replace(entry.getKey(), nonContained));

                // with the same value, either it does nothing or it throws an exception
                try {
                    assertEquals(entry.getValue(), map.replace(entry.getKey(), entry.getValue()));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("replace(Object, Object) with non-contained elements")
        default void testReplaceWithNonContainedElements() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                // with a non-contained object, either it does nothing or it throws an exception
                try {
                    assertNull(map.replace(entry.getKey(), entry.getValue()));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replace(Object, Object) with null key")
        default void testReplaceWithNullKey() {
            Map<K, V> map = createMap();

            V nonContained = nonContainedEntries().values().iterator().next();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertNull(map.replace(null, nonContained));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replace(Object, Object) with null value")
        default void testReplaceWithNullValue() {
            Map<K, V> map = createMap();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            // with a non-contained object, either it does nothing or it throws an exception
            try {
                assertNull(map.replace(nonContained, null));
            } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                // ignore
            }

            assertEquals(expectedEntries(), map);
        }
    }
}
