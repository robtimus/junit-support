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

package com.github.robtimus.junit.support.test.collections;

import static com.github.robtimus.junit.support.ThrowableAsserter.whenThrows;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertOptionallyThrows;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertThrowsOneOf;
import static com.github.robtimus.junit.support.test.collections.CollectionAssertions.assertHasElements;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import com.github.robtimus.junit.support.test.collections.annotation.ContainsIncompatibleNotSupported;
import com.github.robtimus.junit.support.test.collections.annotation.ContainsNullNotSupported;
import com.github.robtimus.junit.support.test.collections.annotation.RemoveIncompatibleNotSupported;
import com.github.robtimus.junit.support.test.collections.annotation.RemoveNullNotSupported;

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
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                V value = entry.getValue();

                assertOptionallyThrows(UnsupportedOperationException.class, () -> assertEquals(value, map.put(entry.getKey(), value)));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("put(Object, Object) with updated entries")
        default void testPutWithUpdatedEntries() {
            Map<K, V> map = map();

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
            Map<K, V> map = map();

            V nonContainedValue = nonContainedEntries().values().iterator().next();

            assertThrows(UnsupportedOperationException.class, () -> map.put(null, nonContainedValue));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("put(Object, Object) with null value")
        default void testPutWithNullValue() {
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.put(entry.getKey(), null));
            }

            assertEquals(expectedEntries, map);
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
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.remove(key));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("remove(Object) with non-contained elements")
        default void testRemoveNonContainedElements() {
            Map<K, V> map = map();

            for (K key : nonContainedEntries().keySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.remove(key)));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object) with null")
        default void testRemoveNull() {
            Map<K, V> map = map();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.remove(null)));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object) with an incompatible object")
        default void testRemoveIncompatibleObject() {
            Map<K, V> map = map();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.remove(new IncompatibleObject())));

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
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                Map<K, V> m = Collections.singletonMap(entry.getKey(), entry.getValue());

                assertOptionallyThrows(UnsupportedOperationException.class, () -> map.putAll(m));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("putAll(Object, Object) with updated entries")
        default void testPutAllWithUpdatedEntries() {
            Map<K, V> map = map();

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
            Map<K, V> map = map();

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                Map<K, V> m = Collections.singletonMap(entry.getKey(), entry.getValue());

                assertThrows(UnsupportedOperationException.class, () -> map.putAll(m));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("putAll(Map) with an empty map")
        default void testPutAllWithEmptyMap() {
            Map<K, V> map = map();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> map.putAll(Collections.emptyMap()));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("putAll(Collection) with null")
        default void testPutAllWithNull() {
            Map<K, V> map = map();

            assertThrowsOneOf(UnsupportedOperationException.class, NullPointerException.class, () -> map.putAll(null));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("putAll(Map) with a map with a null key")
        default void testPutAllWithMapWithNullKey() {
            Map<K, V> map = map();

            V nonContainedValue = nonContainedEntries().values().iterator().next();
            Map<K, V> m = Collections.singletonMap(null, nonContainedValue);

            assertThrows(UnsupportedOperationException.class, () -> map.putAll(m));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("putAll(Object, Object) with a map with a null value")
        default void testPutAllWithMapWithNullValue() {
            Map<K, V> map = map();

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
            Map<K, V> map = map();

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
        default Set<K> iterable() {
            return map().keySet();
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
            default Set<K> iterable() {
                return KeySetTests.super.iterable();
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
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.test.collections.IteratorTests.IterationTests<K> {
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
                    Map<K, V> map = map();
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
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.test.collections.IteratorTests.ForEachRemainingTests<K> {
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
         * Contains tests for {@link Set#toArray(IntFunction)} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         * @since 3.0
         */
        @DisplayName("toArray(IntFunction)")
        interface ToArrayWithGeneratorTests<K, V> extends KeySetTests<K, V>, CollectionTests.ToArrayWithGeneratorTests<K> {
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
        default Collection<V> iterable() {
            return map().values();
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
            default Collection<V> iterable() {
                return ValuesTests.super.iterable();
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
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.test.collections.IteratorTests.IterationTests<V> {
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
                    Map<K, V> map = map();
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
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.test.collections.IteratorTests.ForEachRemainingTests<V> {
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
         * Contains tests for {@link Collection#toArray(IntFunction)} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         * @since 3.0
         */
        @DisplayName("toArray(IntFunction)")
        interface ToArrayWithGeneratorTests<K, V> extends ValuesTests<K, V>, CollectionTests.ToArrayWithGeneratorTests<V> {
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
        default Set<Entry<K, V>> iterable() {
            return map().entrySet();
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
            default Set<Map.Entry<K, V>> iterable() {
                return EntrySetTests.super.iterable();
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
                    extends IteratorTests<K, V>, com.github.robtimus.junit.support.test.collections.IteratorTests.IterationTests<Map.Entry<K, V>> {
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
                    Map<K, V> map = map();
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
            interface ForEachRemainingTests<K, V> extends IteratorTests<K, V>,
                    com.github.robtimus.junit.support.test.collections.IteratorTests.ForEachRemainingTests<Map.Entry<K, V>> {
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
         * Contains tests for {@link Set#toArray(IntFunction)} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         * @since 3.0
         */
        @DisplayName("toArray(IntFunction)")
        interface ToArrayWithGeneratorTests<K, V> extends EntrySetTests<K, V>, CollectionTests.ToArrayWithGeneratorTests<Map.Entry<K, V>> {
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
            Map<K, V> map = map();

            BiFunction<K, V, V> function = replaceValueFunction();

            if (map.isEmpty()) {
                assertOptionallyThrows(UnsupportedOperationException.class, () -> map.replaceAll(function));
            } else {
                assertThrows(UnsupportedOperationException.class, () -> map.replaceAll(function));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replaceAll(BiFunction) with null function")
        default void testReplaceAllWithNullOperator() {
            Map<K, V> map = map();

            assertThrowsOneOf(UnsupportedOperationException.class, NullPointerException.class, () -> map.replaceAll(null));

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
            Map<K, V> map = map();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class,
                        () -> assertEquals(entry.getValue(), map.putIfAbsent(entry.getKey(), nonContained)));
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
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.remove(entry.getKey(), entry.getValue()));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("remove(Object, Object) with non-contained elements")
        default void testRemoveExactValueWithNonContainedElements() {
            Map<K, V> map = map();

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.remove(entry.getKey(), entry.getValue())));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with null key")
        default void testRemoveExactValueWithNullKey() {
            Map<K, V> map = map();

            V nonContained = nonContainedEntries().values().iterator().next();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.remove(null, nonContained)));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with an incompatible key")
        default void testRemoveExactValueWithIncompatibleObjectKey() {
            Map<K, V> map = map();

            V nonContained = nonContainedEntries().values().iterator().next();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.remove(new IncompatibleObject(), nonContained)));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with null value")
        default void testRemoveExactValueWithNullValue() {
            Map<K, V> map = map();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.remove(nonContained, null)));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with an incompatible value")
        default void testRemoveExactValueWithIncompatibleObjectValue() {
            Map<K, V> map = map();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.remove(nonContained, new IncompatibleObject())));

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
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.replace(entry.getKey(), entry.getValue(), nonContained));

                assertOptionallyThrows(UnsupportedOperationException.class,
                        () -> assertTrue(map.replace(entry.getKey(), entry.getValue(), entry.getValue())));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("replace(Object, Object, Object) with non-contained elements")
        default void testReplaceExactValueWithNonContainedElements() {
            Map<K, V> map = map();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class,
                        () -> assertFalse(map.replace(entry.getKey(), entry.getValue(), nonContained)));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replace(Object, Object, Object) with null key")
        default void testReplaceExactValueWithNullKey() {
            Map<K, V> map = map();

            V nonContained = nonContainedEntries().values().iterator().next();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> assertFalse(map.replace(null, nonContained, nonContained)));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replace(Object, Object, Object) with null value")
        default void testReplaceExactValueWithNullValue() {
            Map<K, V> map = map();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> assertFalse(map.replace(nonContained, null, null)));

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
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.replace(entry.getKey(), nonContained));

                assertOptionallyThrows(UnsupportedOperationException.class,
                        () -> assertEquals(entry.getValue(), map.replace(entry.getKey(), entry.getValue())));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("replace(Object, Object) with non-contained elements")
        default void testReplaceWithNonContainedElements() {
            Map<K, V> map = map();

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.replace(entry.getKey(), entry.getValue())));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replace(Object, Object) with null key")
        default void testReplaceWithNullKey() {
            Map<K, V> map = map();

            V nonContained = nonContainedEntries().values().iterator().next();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.replace(null, nonContained)));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replace(Object, Object) with null value")
        default void testReplaceWithNullValue() {
            Map<K, V> map = map();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.replace(nonContained, null)));

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#computeIfAbsent(Object, Function)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("computeIfAbsent(Object, Function)")
    interface ComputeIfAbsentTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("computeIfAbsent(Object, Function)")
        default void testComputeIfAbsent() {
            Map<K, V> map = map();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class,
                        () -> assertEquals(entry.getValue(), map.computeIfAbsent(entry.getKey(), k -> nonContained)));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                V value = entry.getValue();

                assertThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(entry.getKey(), k -> value));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfAbsent(Object, Function) with function returning null")
        default void testComputeIfAbsentWithFunctionReturningNull() {
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(key, k -> null));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.computeIfAbsent(entry.getKey(), k -> null)));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfAbsent(Object, Function) with throwing function")
        default void testComputeIfAbsentWithThrowingFunction() {
            Map<K, V> map = map();

            IllegalArgumentException exception = new IllegalArgumentException();
            Function<K, V> function = k -> {
                throw exception;
            };

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class,
                        () -> assertEquals(entry.getValue(), map.computeIfAbsent(entry.getKey(), function)));
            }

            for (K key : nonContainedEntries().keySet()) {
                whenThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(key, function)).thenAssertNothing()
                        .whenThrows(IllegalArgumentException.class).thenAssert(thrown -> assertSame(exception, thrown))
                        .execute();
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfAbsent(Object, Function) with null function")
        default void testComputeIfAbsentWithNullFunction() {
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrowsOneOf(UnsupportedOperationException.class, NullPointerException.class, () -> map.computeIfAbsent(key, null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertThrowsOneOf(UnsupportedOperationException.class, NullPointerException.class, () -> map.computeIfAbsent(key, null));
            }

            assertEquals(expectedEntries, map);
        }
    }

    /**
     * Contains tests for {@link Map#computeIfPresent(Object, BiFunction)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("computeIfPresent(Object, BiFunction)")
    interface ComputeIfPresentTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("computeIfPresent(Object, BiFunction)")
        default void testComputeIfPresent() {
            Map<K, V> map = map();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.computeIfPresent(key, (k, v) -> nonContained));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                V value = entry.getValue();

                assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.computeIfPresent(entry.getKey(), (k, v) -> value)));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfPresent(Object, BiFunction) with function returning null")
        default void testComputeIfPresentWithFunctionReturningNull() {
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.computeIfPresent(key, (k, v) -> null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.computeIfPresent(key, (k, v) -> null)));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfPresent(Object, BiFunction) with throwing function")
        default void testComputeIfPresentWithThrowingFunction() {
            Map<K, V> map = map();

            IllegalArgumentException exception = new IllegalArgumentException();
            BiFunction<K, V, V> function = (k, v) -> {
                throw exception;
            };

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                whenThrows(UnsupportedOperationException.class, () -> map.computeIfPresent(key, function)).thenAssertNothing()
                        .whenThrows(IllegalArgumentException.class).thenAssert(thrown -> assertSame(exception, thrown))
                        .execute();
            }

            for (K key : nonContainedEntries().keySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class, () -> assertNull(map.computeIfPresent(key, function)));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfPresent(Object, BiFunction) with null function")
        default void testComputeIfPresentWithNullFunction() {
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrowsOneOf(UnsupportedOperationException.class, NullPointerException.class, () -> map.computeIfPresent(key, null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertThrowsOneOf(UnsupportedOperationException.class, NullPointerException.class, () -> map.computeIfPresent(key, null));
            }

            assertEquals(expectedEntries, map);
        }
    }

    /**
     * Contains tests for {@link Map#compute(Object, BiFunction)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("compute(Object, BiFunction)")
    interface ComputeTests<K, V> extends MapTests<K, V> {

        /**
         * Returns a unary operator that can be used to create new values to set with {@link Map#compute(Object, BiFunction)}.
         *
         * @return A unary operator that can be used to create new values to set with {@link Map#compute(Object, BiFunction)}.
         */
        UnaryOperator<V> replaceValueOperator();

        @Test
        @DisplayName("compute(Object, BiFunction)")
        default void testCompute() {
            Map<K, V> map = map();

            UnaryOperator<V> operator = replaceValueOperator();

            Map<K, V> expectedEntries = expectedEntries();
            Map<K, V> nonContainedEntries = nonContainedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.compute(entry.getKey(), (k, v) -> operator.apply(v)));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                V value = entry.getValue();

                assertThrows(UnsupportedOperationException.class, () -> map.compute(entry.getKey(), (k, v) -> value));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("compute(Object, BiFunction) with function returning null")
        default void testComputeWithFunctionReturningNull() {
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.compute(key, (k, v) -> null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.compute(key, (k, v) -> null));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("compute(Object, BiFunction) with throwing function")
        default void testComputeWithThrowingFunction() {
            Map<K, V> map = map();

            IllegalArgumentException exception = new IllegalArgumentException();
            BiFunction<K, V, V> function = (k, v) -> {
                throw exception;
            };

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                whenThrows(UnsupportedOperationException.class, () -> map.compute(key, function)).thenAssertNothing()
                        .whenThrows(IllegalArgumentException.class).thenAssert(thrown -> assertSame(exception, thrown))
                        .execute();
            }

            for (K key : nonContainedEntries().keySet()) {
                whenThrows(UnsupportedOperationException.class, () -> map.compute(key, function)).thenAssertNothing()
                        .whenThrows(IllegalArgumentException.class).thenAssert(thrown -> assertSame(exception, thrown))
                        .execute();
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("compute(Object, BiFunction) with null function")
        default void testComputeWithNullFunction() {
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrowsOneOf(UnsupportedOperationException.class, NullPointerException.class, () -> map.compute(key, null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertThrowsOneOf(UnsupportedOperationException.class, NullPointerException.class, () -> map.compute(key, null));
            }

            assertEquals(expectedEntries, map);
        }
    }

    /**
     * Contains tests for {@link Map#merge(Object, Object, BiFunction)} for unmodifiable maps.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("merge(Object, Object, BiFunction)")
    interface MergeTests<K, V> extends MapTests<K, V> {

        /**
         * Returns a binary operator that can be used to combine values with {@link Map#merge(Object, Object, BiFunction)}.
         *
         * @return A binary operator that can be used to combine values with {@link Map#merge(Object, Object, BiFunction)}.
         */
        BinaryOperator<V> combineValuesOperator();

        @Test
        @DisplayName("merge(Object, Object, BiFunction)")
        default void testMerge() {
            Map<K, V> map = map();

            BinaryOperator<V> operator = combineValuesOperator();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.merge(entry.getKey(), entry.getValue(), operator));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.merge(entry.getKey(), entry.getValue(), operator));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("merge(Object, Object, BiFunction) with function returning null")
        default void testMergeWithFunctionReturningNull() {
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.merge(entry.getKey(), entry.getValue(), (v1, v2) -> null));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                assertOptionallyThrows(UnsupportedOperationException.class, () -> map.merge(entry.getKey(), entry.getValue(), (v1, v2) -> null));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("merge(Object, Object, BiFunction) with throwing function")
        default void testMergeWithThrowingFunction() {
            Map<K, V> map = map();

            IllegalArgumentException exception = new IllegalArgumentException();
            BinaryOperator<V> operator = (v1, v2) -> {
                throw exception;
            };

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                whenThrows(UnsupportedOperationException.class, () -> map.merge(entry.getKey(), entry.getValue(), operator)).thenAssertNothing()
                        .whenThrows(IllegalArgumentException.class).thenAssert(thrown -> assertSame(exception, thrown))
                        .execute();
            }

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                whenThrows(UnsupportedOperationException.class, () -> map.merge(entry.getKey(), entry.getValue(), operator)).thenAssertNothing()
                        .whenThrows(IllegalArgumentException.class).thenAssert(thrown -> assertSame(exception, thrown))
                        .execute();
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("merge(Object, Object, BiFunction) with null function")
        default void testMergeWithNullFunction() {
            Map<K, V> map = map();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrowsOneOf(UnsupportedOperationException.class, NullPointerException.class,
                        () -> map.merge(entry.getKey(), entry.getValue(), null));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                assertThrowsOneOf(UnsupportedOperationException.class, NullPointerException.class,
                        () -> map.merge(entry.getKey(), entry.getValue(), null));
            }

            assertEquals(expectedEntries, map);
        }
    }
}
