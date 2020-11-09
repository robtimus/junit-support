/*
 * MapTests.java
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

package com.github.robtimus.unittestsupport.collections;

import static com.github.robtimus.unittestsupport.collections.CollectionAssertions.assertHasElements;
import static com.github.robtimus.unittestsupport.collections.CollectionUtils.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
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
import com.github.robtimus.unittestsupport.collections.annotation.ContainsIncompatibleKeyNotSupported;
import com.github.robtimus.unittestsupport.collections.annotation.ContainsIncompatibleNotSupported;
import com.github.robtimus.unittestsupport.collections.annotation.ContainsNullKeyNotSupported;
import com.github.robtimus.unittestsupport.collections.annotation.ContainsNullNotSupported;
import com.github.robtimus.unittestsupport.collections.annotation.RemoveIncompatibleKeyNotSupported;
import com.github.robtimus.unittestsupport.collections.annotation.RemoveIncompatibleNotSupported;
import com.github.robtimus.unittestsupport.collections.annotation.RemoveNullKeyNotSupported;
import com.github.robtimus.unittestsupport.collections.annotation.RemoveNullNotSupported;
import com.github.robtimus.unittestsupport.collections.annotation.StoreNullKeyNotSupported;
import com.github.robtimus.unittestsupport.collections.annotation.StoreNullNotSupported;

/**
 * Base interface for testing separate {@link Map} functionalities.
 *
 * @author Rob Spoor
 * @param <K> The key type of the map to test.
 * @param <V> The value type of the map to test.
 */
public interface MapTests<K, V> {

    /**
     * Creates the map to test. This should be populated, i.e. not empty, unless the map can only be empty.
     *
     * @return The created map.
     */
    Map<K, V> createMap();

    /**
     * Returns a map with the expected entries contained by the map to test.
     * This should not be of the same type as the map to test, but preferably of some well-known map type like {@link HashMap}.
     *
     * @return A map with the expected entries contained by the map to test.
     */
    Map<K, V> expectedEntries();

    /**
     * Returns some entries that should not be contained by the map to test. For both {@link Map#keySet()} and {@link Map#values()}, the returned map
     * should have no common elements with {@link #expectedEntries()}.
     *
     * @return A map with entries that should not be contained by the entries to test.
     */
    Map<K, V> nonContainedEntries();

    /**
     * Contains tests for {@link Map#containsKey(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Map#containsKey(Object)} with {@code null} or an instance of an incompatible
     * type will simply return {@code false}. If either is not the case, annotate your class with {@link ContainsNullKeyNotSupported} and/or
     * {@link ContainsIncompatibleKeyNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("containsKey(Object)")
    interface ContainsKeyTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("containsKey(Object)")
        default void testContainsKey() {
            Map<K, V> map = createMap();

            for (K o : expectedEntries().keySet()) {
                assertTrue(map.containsKey(o));
            }

            for (K o : nonContainedEntries().keySet()) {
                assertFalse(map.containsKey(o));
            }
        }

        @Test
        @DisplayName("containsKey(Object) with null")
        default void testContainsKeyWithNull(TestInfo testInfo) {
            Map<K, V> map = createMap();

            ContainsNullKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsNullKeyNotSupported.class);

            if (annotation == null) {
                assertFalse(map.containsKey(null));
            } else {
                assertThrows(annotation.expected(), () -> map.containsKey(null));
            }
        }

        @Test
        @DisplayName("containsKey(Object) with an incompatible object")
        default void testContainsKeyWithIncompatibleObject(TestInfo testInfo) {
            Map<K, V> map = createMap();

            ContainsIncompatibleKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsIncompatibleKeyNotSupported.class);

            if (annotation == null) {
                assertFalse(map.containsKey(new IncompatibleObject()));
            } else {
                assertThrows(annotation.expected(), () -> map.containsKey(new IncompatibleObject()));
            }
        }
    }

    /**
     * Contains tests for {@link Map#containsValue(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Map#containsValue(Object)} with {@code null} or an instance of an
     * incompatible type will simply return {@code false}. If either is not the case, annotate your class with {@link ContainsNullNotSupported} and/or
     * {@link ContainsIncompatibleNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("containsValue(Object)")
    interface ContainsValueTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("containsValue(Object)")
        default void testContainsValue() {
            Map<K, V> map = createMap();

            for (V o : expectedEntries().values()) {
                assertTrue(map.containsValue(o));
            }

            for (V o : nonContainedEntries().values()) {
                assertFalse(map.containsValue(o));
            }
        }

        @Test
        @DisplayName("containsValue(Object) with null")
        default void testContainsValueWithNull(TestInfo testInfo) {
            Map<K, V> map = createMap();

            ContainsNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsNullNotSupported.class);

            if (annotation == null) {
                assertFalse(map.containsValue(null));
            } else {
                assertThrows(annotation.expected(), () -> map.containsValue(null));
            }
        }

        @Test
        @DisplayName("containsValue(Object) with an incompatible object")
        default void testContainsValueWithIncompatibleObject(TestInfo testInfo) {
            Map<K, V> map = createMap();

            ContainsIncompatibleNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsIncompatibleNotSupported.class);

            if (annotation == null) {
                assertFalse(map.containsValue(new IncompatibleObject()));
            } else {
                assertThrows(annotation.expected(), () -> map.containsValue(new IncompatibleObject()));
            }
        }
    }

    /**
     * Contains tests for {@link Map#get(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Map#get(Object)} with {@code null} or an instance of an incompatible type
     * will simply return {@code false}. If either is not the case, annotate your class with {@link ContainsNullKeyNotSupported} and/or
     * {@link ContainsIncompatibleKeyNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("get(Object)")
    interface GetTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("get(Object)")
        default void testGet() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : expectedEntries().entrySet()) {
                assertEquals(entry.getValue(), map.get(entry.getKey()));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertNull(map.get(key));
            }
        }

        @Test
        @DisplayName("get(Object) with null")
        default void testGetWithNull(TestInfo testInfo) {
            Map<K, V> map = createMap();

            ContainsNullKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsNullKeyNotSupported.class);

            if (annotation == null) {
                assertNull(map.get(null));
            } else {
                assertThrows(annotation.expected(), () -> map.get(null));
            }
        }

        @Test
        @DisplayName("get(Object) with an incompatible object")
        default void testGetWithIncompatibleObject(TestInfo testInfo) {
            Map<K, V> map = createMap();

            ContainsIncompatibleKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsIncompatibleKeyNotSupported.class);

            if (annotation == null) {
                assertNull(map.get(new IncompatibleObject()));
            } else {
                assertThrows(annotation.expected(), () -> map.get(new IncompatibleObject()));
            }
        }
    }

    /**
     * Contains tests for {@link Map#put(Object, Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Map#put(Object, Object)} with a {@code null} key or value will simply add
     * such an entry. If either is not the case, annotate your class with {@link StoreNullKeyNotSupported} and/or
     * {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("put(Object, Object)")
    interface PutTests<K, V> extends MapTests<K, V> {

        /**
         * Returns a unary operator that can be used to create new values to set with {@link Map#put(Object, Object)}.
         *
         * @return A unary operator that can be used to create new values to set with {@link Map#put(Object, Object)}.
         */
        UnaryOperator<V> replaceValueOperator();

        @Test
        @DisplayName("put(Object, Object)")
        default void testPut() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());

            UnaryOperator<V> operator = replaceValueOperator();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                V value = entry.getValue();

                assertEquals(value, map.put(entry.getKey(), operator.apply(value)));
            }

            Map<K, V> nonContainedEntries = nonContainedEntries();

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                assertNull(map.put(entry.getKey(), entry.getValue()));
            }

            expectedEntries.replaceAll((k, v) -> operator.apply(v));
            expectedEntries.putAll(nonContainedEntries);

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("put(Object, Object) with null key")
        default void testPutWithNullKey(TestInfo testInfo) {
            Map<K, V> map = createMap();

            StoreNullKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullKeyNotSupported.class);

            V nonContainedValue = nonContainedEntries().values().iterator().next();

            if (annotation == null) {
                assertNull(map.put(null, nonContainedValue));
            } else {
                assertThrows(annotation.expected(), () -> map.put(null, nonContainedValue));
            }

            if (annotation == null) {
                Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
                expectedEntries.put(null, nonContainedValue);

                assertEquals(expectedEntries, map);
            } else {
                assertEquals(expectedEntries(), map);
            }
        }

        @Test
        @DisplayName("put(Object, Object) with null value")
        default void testPutWithNullValue(TestInfo testInfo) {
            Map<K, V> map = createMap();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                if (annotation == null) {
                    assertEquals(entry.getValue(), map.put(entry.getKey(), null));
                } else {
                    assertThrows(annotation.expected(), () -> map.put(entry.getKey(), null));
                }
            }

            if (annotation == null) {
                expectedEntries = new HashMap<>(expectedEntries);
                expectedEntries.replaceAll((k, v) -> null);

                assertEquals(expectedEntries, map);
            } else {
                assertEquals(expectedEntries(), map);
            }
        }
    }

    /**
     * Contains tests for {@link Map#remove(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Map#remove(Object)} with {@code null} or an instance of an incompatible type
     * will simply return {@code false}. If either is not the case, annotate your class with {@link RemoveNullKeyNotSupported} and/or
     * {@link RemoveIncompatibleKeyNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("remove(Object)")
    interface RemoveTests<K, V> extends MapTests<K, V> {

        @ParameterizedTest(name = "{0}: {1}")
        @ArgumentsSource(RemoveArgumentsProvider.class)
        @DisplayName("remove(Object)")
        default void testRemove(Object key, Object expectedValue, boolean expected) {
            Map<K, V> map = createMap();

            assertEquals(expectedValue, map.remove(key));

            Map<K, V> expectedEntries = expectedEntries();
            if (expected) {
                expectedEntries = new HashMap<>(expectedEntries);
                expectedEntries.remove(key);
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("remove(Object) with null")
        default void testRemoveNull(TestInfo testInfo) {
            Map<K, V> map = createMap();

            RemoveNullKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(RemoveNullKeyNotSupported.class);

            if (annotation == null) {
                assertNull(map.remove(null));
            } else {
                assertThrows(annotation.expected(), () -> map.remove(null));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object) with incompatible object")
        default void testRemoveIncompatibleObject(TestInfo testInfo) {
            Map<K, V> map = createMap();

            RemoveIncompatibleKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(RemoveIncompatibleKeyNotSupported.class);

            if (annotation == null) {
                assertNull(map.remove(new IncompatibleObject()));
            } else {
                assertThrows(annotation.expected(), () -> map.remove(new IncompatibleObject()));
            }

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#putAll(Map)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Map#putAll(Map)} with a map containing {@code null} keys or values will
     * simply add the entry with the {@code null} key or value. If this is not the case, annotate your class with {@link StoreNullKeyNotSupported}
     * and/or {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("putAll(Map)")
    interface PutAllTests<K, V> extends MapTests<K, V> {

        /**
         * Returns a unary operator that can be used to create new values to set with {@link Map#putAll(Map)}.
         *
         * @return A unary operator that can be used to create new values to set with {@link Map#putAll(Map)}.
         */
        UnaryOperator<V> replaceValueOperator();

        @Test
        @DisplayName("putAll(Map)")
        default void testPutAll() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());

            map.putAll(Collections.emptyMap());

            assertEquals(expectedEntries, map);

            UnaryOperator<V> operator = replaceValueOperator();

            Map<K, V> m = new HashMap<>();
            for (Iterator<Map.Entry<K, V>> i = expectedEntries.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<K, V> entry = i.next();
                K key = entry.getKey();
                V value = entry.getValue();
                V newValue = operator.apply(value);

                m.put(key, operator.apply(value));
                expectedEntries.put(key, newValue);

                if (i.hasNext()) {
                    i.next();
                }
            }

            map.putAll(m);

            assertEquals(expectedEntries, map);

            Map<K, V> nonContainedEntries = nonContainedEntries();

            map.putAll(nonContainedEntries);
            expectedEntries.putAll(nonContainedEntries);

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("putAll(Map) with a null map")
        default void testPutAllWithNullMap() {
            Map<K, V> map = createMap();

            assertThrows(NullPointerException.class, () -> map.putAll(null));

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("putAll(Map) with a map with a null key")
        default void testPutAllWithMapWithNullKey(TestInfo testInfo) {
            Map<K, V> map = createMap();

            V nonContainedValue = nonContainedEntries().values().iterator().next();
            Map<K, V> m = Collections.singletonMap(null, nonContainedValue);

            StoreNullKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullKeyNotSupported.class);

            Map<K, V> expectedEntries = expectedEntries();

            if (annotation == null) {
                map.putAll(m);

                expectedEntries = new HashMap<>(expectedEntries);
                expectedEntries.put(null, nonContainedValue);
            } else {
                assertThrows(annotation.expected(), () -> map.putAll(m));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("putAll(Map) with a map with a null value")
        default void testPutAllWithMapWithNullValue(TestInfo testInfo) {
            Map<K, V> map = createMap();

            Map<K, V> m = new HashMap<>(expectedEntries());
            m.replaceAll((k, v) -> null);

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            Map<K, V> expectedEntries = expectedEntries();

            if (annotation == null) {
                map.putAll(m);

                expectedEntries = new HashMap<>(expectedEntries);
                expectedEntries.replaceAll((k, v) -> null);
            } else {
                assertThrows(annotation.expected(), () -> map.putAll(m));
            }

            assertEquals(expectedEntries, map);
        }
    }

    /**
     * Contains tests for {@link Map#clear()}.
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

            map.clear();

            assertEquals(Collections.emptyMap(), map);
        }
    }

    /**
     * Contains tests for {@link Map#keySet()}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("keySet()")
    interface KeySetTests<K, V> extends MapTests<K, V>, SetTests<K> {

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
        interface IteratorTests<K, V> extends KeySetTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests<K> {

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
                    extends IteratorTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests.IterationTests<K> {
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
            interface RemoveTests<K, V> extends IteratorTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests.RemoveTests<K> {

                @Override
                @Test
                @DisplayName("remove() for every element")
                default void testRemoveEveryElement() {
                    Map<K, V> map = createMap();
                    Set<K> keySet = map.keySet();
                    Iterator<K> iterator = keySet.iterator();

                    while (iterator.hasNext()) {
                        iterator.next();
                        iterator.remove();
                    }

                    List<K> remaining = toList(keySet);
                    assertHasElements(remaining, Collections.emptyList(), fixedOrder());
                    assertEquals(Collections.emptyMap(), map);
                }

                @Override
                @Test
                @DisplayName("remove() for every even-indexed element")
                default void testRemoveEveryEvenIndexedElement() {
                    Map<K, V> map = createMap();
                    Set<K> keySet = map.keySet();
                    Iterator<K> iterator = keySet.iterator();

                    Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
                    List<K> expectedElements = new ArrayList<>(expectedEntries.keySet());

                    boolean remove = true;
                    while (iterator.hasNext()) {
                        K element = iterator.next();
                        if (remove) {
                            expectedElements.remove(element);
                            expectedEntries.remove(element);
                            iterator.remove();
                        }
                        remove = !remove;
                    }

                    List<K> remaining = toList(keySet);
                    assertHasElements(remaining, expectedElements, fixedOrder());
                    assertEquals(expectedEntries, map);
                }

                @Override
                @Test
                @DisplayName("remove() before next()")
                default void testRemoveBeforeNext() {
                    Map<K, V> map = createMap();
                    Set<K> keySet = map.keySet();
                    Iterator<K> iterator = keySet.iterator();

                    assertThrows(IllegalStateException.class, iterator::remove);

                    Map<K, V> expectedEntries = expectedEntries();
                    Set<K> expectedElements = expectedEntries.keySet();

                    List<K> remaining = toList(keySet);
                    assertHasElements(remaining, expectedElements, fixedOrder());
                    assertEquals(expectedEntries, map);
                }

                @Override
                @Test
                @DisplayName("remove() after remove()")
                default void testRemoveAfterRemove() {
                    Map<K, V> map = createMap();
                    Set<K> keySet = map.keySet();
                    Iterator<K> iterator = keySet.iterator();

                    Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
                    List<K> expectedElements = new ArrayList<>(expectedEntries.keySet());

                    boolean remove = true;
                    while (iterator.hasNext()) {
                        K element = iterator.next();
                        if (remove) {
                            expectedElements.remove(element);
                            expectedEntries.remove(element);
                            iterator.remove();
                            assertThrows(IllegalStateException.class, iterator::remove);
                        }
                        remove = !remove;
                    }

                    List<K> remaining = toList(keySet);
                    assertHasElements(remaining, expectedElements, fixedOrder());
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
                    extends IteratorTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests.ForEachRemainingTests<K> {
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
        interface RemoveTests<K, V> extends KeySetTests<K, V>, CollectionTests.RemoveTests<K> {

            @Override
            @ParameterizedTest(name = "{0}: {1}")
            @ArgumentsSource(CollectionTests.RemoveArgumentsProvider.class)
            @DisplayName("remove(Object)")
            default void testRemove(Object o, boolean expected) {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                assertEquals(expected, keySet.remove(o));

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();
                if (expected) {
                    expectedEntries = new HashMap<>(expectedEntries);
                    expectedElements = expectedEntries.keySet();
                    expectedElements.remove(o);
                }

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("remove(Object) with null")
            default void testRemoveNull(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                RemoveNullNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveNullNotSupported.class);

                if (annotation == null) {
                    assertFalse(keySet.remove(null));
                } else {
                    assertThrows(annotation.expected(), () -> keySet.remove(null));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("remove(Object) with incompatible object")
            default void testRemoveIncompatibleObject(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                RemoveIncompatibleNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveIncompatibleNotSupported.class);

                if (annotation == null) {
                    assertFalse(keySet.remove(new IncompatibleObject()));
                } else {
                    assertThrows(annotation.expected(), () -> keySet.remove(new IncompatibleObject()));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
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
         * By default, the tests in this interface assume that calling {@link Set#removeAll(Collection)} with a collection containing {@code null} or
         * an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the case, annotate your
         * class with {@link RemoveNullNotSupported} and/or {@link RemoveIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("removeAll(Collection)")
        interface RemoveAllTests<K, V> extends KeySetTests<K, V>, CollectionTests.RemoveAllTests<K> {

            @Override
            @ParameterizedTest(name = "{0}: {1}")
            @ArgumentsSource(RemoveAllArgumentsProvider.class)
            @DisplayName("removeAll(Collection)")
            default void testRemoveAll(Collection<?> c, boolean expected) {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                assertEquals(expected, keySet.removeAll(c));

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();
                if (expected) {
                    expectedEntries = new HashMap<>(expectedEntries);
                    expectedElements = expectedEntries.keySet();
                    expectedElements.removeAll(c);
                }

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeAll(Collection) with a null collection")
            default void testRemoveAllWithNullCollection() {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                assertThrows(NullPointerException.class, () -> keySet.removeAll(null));

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeAll(Collection) with a collection with a null")
            default void testRemoveAllWithCollectionWithNull(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                Collection<?> c = Collections.singleton(null);

                RemoveNullNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveNullNotSupported.class);

                if (annotation == null) {
                    assertFalse(keySet.removeAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> keySet.removeAll(c));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeAll(Collection) with a collection with an incompatible object")
            default void testRemoveAllWithCollectionWithIncompatibleObject(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                Collection<?> c = Collections.singleton(new IncompatibleObject());

                RemoveIncompatibleNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveIncompatibleNotSupported.class);

                if (annotation == null) {
                    assertFalse(keySet.removeAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> keySet.removeAll(c));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
        }

        /**
         * Contains tests for {@link Set#removeIf(Predicate)} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("removeIf(Predicate)")
        interface RemoveIfTests<K, V> extends KeySetTests<K, V>, CollectionTests.RemoveIfTests<K> {

            @Override
            @Test
            @DisplayName("removeIf(Predicate) with matching predicate")
            default void testRemoveIfWithMatchingPredicate() {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                boolean isEmpty = keySet.isEmpty();
                assertEquals(!isEmpty, keySet.removeIf(e -> true));

                assertHasElements(keySet, Collections.emptyList(), fixedOrder());
                assertEquals(Collections.emptyMap(), map);
            }

            @Override
            @Test
            @DisplayName("removeIf(Predicate) with non-matching predicate")
            default void testRemoveIfWithNonMatchingPredicate() {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                assertFalse(keySet.removeIf(e -> false));

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeIf(Predicate) with null predicate")
            default void testRemoveIfWithNullPredicate() {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                assertThrows(NullPointerException.class, () -> keySet.removeIf(null));

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
        }

        /**
         * Contains tests for {@link Set#retainAll(Collection)} for key sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#retainAll(Collection)} with a collection containing {@code null} or
         * an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the case, annotate your
         * class with {@link ContainsNullNotSupported} and/or {@link ContainsIncompatibleNotSupported}
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("retainAll(Collection)")
        interface RetainAllTests<K, V> extends KeySetTests<K, V>, CollectionTests.RetainAllTests<K> {

            @Override
            @ParameterizedTest(name = "{0}: {1}")
            @ArgumentsSource(RetainAllArgumentsProvider.class)
            @DisplayName("retainAll(Collection)")
            default void testRetainAll(Collection<?> c, boolean expected) {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                assertEquals(expected, keySet.retainAll(c));

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();
                if (expected) {
                    expectedEntries = new HashMap<>(expectedEntries);
                    expectedElements = expectedEntries.keySet();
                    expectedElements.retainAll(c);
                }

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("retainAll(Collection) with a null collection")
            default void testRetainAllWithNullCollection() {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                assertThrows(NullPointerException.class, () -> keySet.retainAll(null));

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("retainAll(Collection) with a collection with a null")
            default void testRetainAllWithCollectionWithNull(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();

                Collection<Object> c = new ArrayList<>(expectedElements);
                c.add(null);

                ContainsNullNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(ContainsNullNotSupported.class);

                if (annotation == null) {
                    assertFalse(keySet.retainAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> keySet.retainAll(c));
                }

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("retainAll(Collection) with a collection with an incompatible object")
            default void testRetainAllWithCollectionWithIncompatibleObject(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                Map<K, V> expectedEntries = expectedEntries();
                Set<K> expectedElements = expectedEntries.keySet();

                Collection<Object> c = new ArrayList<>(expectedElements);
                c.add(new IncompatibleObject());

                ContainsIncompatibleNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(ContainsIncompatibleNotSupported.class);

                if (annotation == null) {
                    assertFalse(keySet.retainAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> keySet.retainAll(c));
                }

                assertHasElements(keySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
        }

        /**
         * Contains tests for {@link Set#clear()} for key sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("clear()")
        interface ClearTests<K, V> extends KeySetTests<K, V>, CollectionTests.ClearTests<K> {

            @Override
            @Test
            @DisplayName("clear()")
            default void testClear() {
                Map<K, V> map = createMap();
                Set<K> keySet = map.keySet();

                keySet.clear();

                assertHasElements(keySet, Collections.emptyList(), fixedOrder());
                assertEquals(Collections.emptyMap(), map);
            }
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
    interface ValuesTests<K, V> extends MapTests<K, V>, CollectionTests<V> {

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
         * Contains tests for {@link Collection#iterator()} for value collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("forEach(Consumer)")
        interface IteratorTests<K, V> extends ValuesTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests<V> {

            @Override
            default Collection<V> createIterable() {
                return ValuesTests.super.createIterable();
            }

            @Override
            default Collection<V> expectedElements() {
                return ValuesTests.super.expectedElements();
            }

            /**
             * Contains tests for {@link Iterator#hasNext()} and {@link Iterator#next()} for value collection iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("iteration")
            interface IterationTests<K, V>
                    extends IteratorTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests.IterationTests<V> {
                // no new tests needed
            }

            /**
             * Contains tests for {@link Iterator#remove()} for value collection iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("remove()")
            interface RemoveTests<K, V> extends IteratorTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests.RemoveTests<V> {

                @Override
                @Test
                @DisplayName("remove() for every element")
                default void testRemoveEveryElement() {
                    Map<K, V> map = createMap();
                    Collection<V> values = map.values();
                    Iterator<V> iterator = values.iterator();

                    while (iterator.hasNext()) {
                        iterator.next();
                        iterator.remove();
                    }

                    List<V> remaining = toList(values);
                    assertHasElements(remaining, Collections.emptyList(), fixedOrder());
                    assertEquals(Collections.emptyMap(), map);
                }

                @Override
                @Test
                @DisplayName("remove() for every even-indexed element")
                default void testRemoveEveryEvenIndexedElement() {
                    Map<K, V> map = createMap();
                    Collection<V> values = map.values();
                    Iterator<V> iterator = values.iterator();

                    Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
                    Collection<V> expectedElements = expectedEntries.values();
                    Iterator<V> expectedIterator = expectedElements.iterator();

                    boolean remove = true;
                    while (iterator.hasNext()) {
                        iterator.next();
                        expectedIterator.next();
                        if (remove) {
                            expectedIterator.remove();
                            iterator.remove();
                        }
                        remove = !remove;
                    }

                    List<V> remaining = toList(values);
                    assertHasElements(remaining, expectedElements, fixedOrder());
                    assertEquals(expectedEntries, map);
                }

                @Override
                @Test
                @DisplayName("remove() before next()")
                default void testRemoveBeforeNext() {
                    Map<K, V> map = createMap();
                    Collection<V> values = map.values();
                    Iterator<V> iterator = values.iterator();

                    assertThrows(IllegalStateException.class, iterator::remove);

                    Map<K, V> expectedEntries = expectedEntries();
                    Collection<V> expectedElements = expectedEntries.values();

                    List<V> remaining = toList(values);
                    assertHasElements(remaining, expectedElements, fixedOrder());
                    assertEquals(expectedEntries, map);
                }

                @Override
                @Test
                @DisplayName("remove() after remove()")
                default void testRemoveAfterRemove() {
                    Map<K, V> map = createMap();
                    Collection<V> values = map.values();
                    Iterator<V> iterator = values.iterator();

                    Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
                    Collection<V> expectedElements = expectedEntries.values();
                    Iterator<V> expectedIterator = expectedElements.iterator();

                    boolean remove = true;
                    while (iterator.hasNext()) {
                        iterator.next();
                        expectedIterator.next();
                        if (remove) {
                            expectedIterator.remove();
                            iterator.remove();
                            assertThrows(IllegalStateException.class, iterator::remove);
                        }
                        remove = !remove;
                    }

                    List<V> remaining = toList(values);
                    assertHasElements(remaining, expectedElements, fixedOrder());
                }
            }

            /**
             * Contains tests for {@link Iterator#forEachRemaining(Consumer)} for value collection iterators.
             *
             * @author Rob Spoor
             * @param <K> The key type of the map to test.
             * @param <V> The value type of the map to test.
             */
            @DisplayName("forEachRemaining(Consumer)")
            interface ForEachRemainingTests<K, V>
                    extends IteratorTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests.ForEachRemainingTests<V> {
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
        interface RemoveTests<K, V> extends ValuesTests<K, V>, CollectionTests.RemoveTests<V> {

            @Override
            @ParameterizedTest(name = "{0}: {1}")
            @ArgumentsSource(CollectionTests.RemoveArgumentsProvider.class)
            @DisplayName("remove(Object)")
            default void testRemove(Object o, boolean expected) {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                assertEquals(expected, values.remove(o));

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();
                if (expected) {
                    expectedEntries = new HashMap<>(expectedEntries);
                    expectedElements = expectedEntries.values();
                    expectedElements.remove(o);
                }

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("remove(Object) with null")
            default void testRemoveNull(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                RemoveNullNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveNullNotSupported.class);

                if (annotation == null) {
                    assertFalse(values.remove(null));
                } else {
                    assertThrows(annotation.expected(), () -> values.remove(null));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("remove(Object) with incompatible object")
            default void testRemoveIncompatibleObject(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                RemoveIncompatibleNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveIncompatibleNotSupported.class);

                if (annotation == null) {
                    assertFalse(values.remove(new IncompatibleObject()));
                } else {
                    assertThrows(annotation.expected(), () -> values.remove(new IncompatibleObject()));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
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
        interface RemoveAllTests<K, V> extends ValuesTests<K, V>, CollectionTests.RemoveAllTests<V> {

            @Override
            @ParameterizedTest(name = "{0}: {1}")
            @ArgumentsSource(RemoveAllArgumentsProvider.class)
            @DisplayName("removeAll(Collection)")
            default void testRemoveAll(Collection<?> c, boolean expected) {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                assertEquals(expected, values.removeAll(c));

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();
                if (expected) {
                    expectedEntries = new HashMap<>(expectedEntries);
                    expectedElements = expectedEntries.values();
                    expectedElements.removeAll(c);
                }

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeAll(Collection) with a null collection")
            default void testRemoveAllWithNullCollection() {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                assertThrows(NullPointerException.class, () -> values.removeAll(null));

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeAll(Collection) with a collection with a null")
            default void testRemoveAllWithCollectionWithNull(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                Collection<?> c = Collections.singleton(null);

                RemoveNullNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveNullNotSupported.class);

                if (annotation == null) {
                    assertFalse(values.removeAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> values.removeAll(c));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeAll(Collection) with a collection with an incompatible object")
            default void testRemoveAllWithCollectionWithIncompatibleObject(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                Collection<?> c = Collections.singleton(new IncompatibleObject());

                RemoveIncompatibleNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveIncompatibleNotSupported.class);

                if (annotation == null) {
                    assertFalse(values.removeAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> values.removeAll(c));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
        }

        /**
         * Contains tests for {@link Collection#removeIf(Predicate)} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("removeIf(Predicate)")
        interface RemoveIfTests<K, V> extends ValuesTests<K, V>, CollectionTests.RemoveIfTests<V> {

            @Override
            @Test
            @DisplayName("removeIf(Predicate) with matching predicate")
            default void testRemoveIfWithMatchingPredicate() {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                boolean isEmpty = values.isEmpty();
                assertEquals(!isEmpty, values.removeIf(e -> true));

                assertHasElements(values, Collections.emptyList(), fixedOrder());
                assertEquals(Collections.emptyMap(), map);
            }

            @Override
            @Test
            @DisplayName("removeIf(Predicate) with non-matching predicate")
            default void testRemoveIfWithNonMatchingPredicate() {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                assertFalse(values.removeIf(e -> false));

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeIf(Predicate) with null predicate")
            default void testRemoveIfWithNullPredicate() {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                assertThrows(NullPointerException.class, () -> values.removeIf(null));

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
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
        interface RetainAllTests<K, V> extends ValuesTests<K, V>, CollectionTests.RetainAllTests<V> {

            @Override
            @ParameterizedTest(name = "{0}: {1}")
            @ArgumentsSource(RetainAllArgumentsProvider.class)
            @DisplayName("retainAll(Collection)")
            default void testRetainAll(Collection<?> c, boolean expected) {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                assertEquals(expected, values.retainAll(c));

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();
                if (expected) {
                    expectedEntries = new HashMap<>(expectedEntries);
                    expectedElements = expectedEntries.values();
                    expectedElements.retainAll(c);
                }

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("retainAll(Collection) with a null collection")
            default void testRetainAllWithNullCollection() {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                assertThrows(NullPointerException.class, () -> values.retainAll(null));

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("retainAll(Collection) with a collection with a null")
            default void testRetainAllWithCollectionWithNull(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();

                Collection<Object> c = new ArrayList<>(expectedElements);
                c.add(null);

                ContainsNullNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(ContainsNullNotSupported.class);

                if (annotation == null) {
                    assertFalse(values.retainAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> values.retainAll(c));
                }

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("retainAll(Collection) with a collection with an incompatible object")
            default void testRetainAllWithCollectionWithIncompatibleObject(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                Map<K, V> expectedEntries = expectedEntries();
                Collection<V> expectedElements = expectedEntries.values();

                Collection<Object> c = new ArrayList<>(expectedElements);
                c.add(new IncompatibleObject());

                ContainsIncompatibleNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(ContainsIncompatibleNotSupported.class);

                if (annotation == null) {
                    assertFalse(values.retainAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> values.retainAll(c));
                }

                assertHasElements(values, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
        }

        /**
         * Contains tests for {@link Collection#clear()} for values collections.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("clear()")
        interface ClearTests<K, V> extends ValuesTests<K, V>, CollectionTests.ClearTests<V> {

            @Override
            @Test
            @DisplayName("clear()")
            default void testClear() {
                Map<K, V> map = createMap();
                Collection<V> values = map.values();

                values.clear();

                assertHasElements(values, Collections.emptyList(), fixedOrder());
                assertEquals(Collections.emptyMap(), map);
            }
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
    interface EntrySetTests<K, V> extends MapTests<K, V>, SetTests<Map.Entry<K, V>> {

        @Override
        default Set<Map.Entry<K, V>> createIterable() {
            return createMap().entrySet();
        }

        @Override
        default Collection<Map.Entry<K, V>> expectedElements() {
            return expectedEntries().entrySet();
        }

        @Override
        default Collection<Map.Entry<K, V>> nonContainedElements() {
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
        interface IteratorTests<K, V> extends EntrySetTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests<Map.Entry<K, V>> {

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
                    extends IteratorTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests.IterationTests<Map.Entry<K, V>> {
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
            interface RemoveTests<K, V>
                    extends IteratorTests<K, V>, com.github.robtimus.unittestsupport.collections.IteratorTests.RemoveTests<Map.Entry<K, V>> {

                @Override
                @Test
                @DisplayName("remove() for every element")
                default void testRemoveEveryElement() {
                    Map<K, V> map = createMap();
                    Set<Map.Entry<K, V>> entrySet = map.entrySet();
                    Iterator<Map.Entry<K, V>> iterator = entrySet.iterator();

                    while (iterator.hasNext()) {
                        iterator.next();
                        iterator.remove();
                    }

                    List<Map.Entry<K, V>> remaining = toList(entrySet);
                    assertHasElements(remaining, Collections.emptyList(), fixedOrder());
                    assertEquals(Collections.emptyMap(), map);
                }

                @Override
                @Test
                @DisplayName("remove() for every even-indexed element")
                default void testRemoveEveryEvenIndexedElement() {
                    Map<K, V> map = createMap();
                    Set<Map.Entry<K, V>> entrySet = map.entrySet();
                    Iterator<Map.Entry<K, V>> iterator = entrySet.iterator();

                    Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
                    // create a copy of each entry, to prevent volatile entries from being updated just by iterating
                    List<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet().stream()
                            .map(SimpleImmutableEntry::new)
                            .collect(Collectors.toList());

                    boolean remove = true;
                    while (iterator.hasNext()) {
                        Map.Entry<K, V> element = iterator.next();
                        if (remove) {
                            expectedElements.remove(element);
                            expectedEntries.remove(element.getKey(), element.getValue());
                            iterator.remove();
                        }
                        remove = !remove;
                    }

                    List<Map.Entry<K, V>> remaining = toList(entrySet);
                    assertHasElements(remaining, expectedElements, fixedOrder());
                    assertEquals(expectedEntries, map);
                }

                @Override
                @Test
                @DisplayName("remove() before next()")
                default void testRemoveBeforeNext() {
                    Map<K, V> map = createMap();
                    Set<Map.Entry<K, V>> entrySet = map.entrySet();
                    Iterator<Map.Entry<K, V>> iterator = entrySet.iterator();

                    assertThrows(IllegalStateException.class, iterator::remove);

                    Map<K, V> expectedEntries = expectedEntries();
                    Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                    List<Map.Entry<K, V>> remaining = toList(entrySet);
                    assertHasElements(remaining, expectedElements, fixedOrder());
                    assertEquals(expectedEntries, map);
                }

                @Override
                @Test
                @DisplayName("remove() after remove()")
                default void testRemoveAfterRemove() {
                    Map<K, V> map = createMap();
                    Set<Map.Entry<K, V>> entrySet = map.entrySet();
                    Iterator<Map.Entry<K, V>> iterator = entrySet.iterator();

                    Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
                    // create a copy of each entry, to prevent volatile entries from being updated just by iterating
                    List<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet().stream()
                            .map(SimpleImmutableEntry::new)
                            .collect(Collectors.toList());

                    boolean remove = true;
                    while (iterator.hasNext()) {
                        Map.Entry<K, V> element = iterator.next();
                        if (remove) {
                            expectedElements.remove(element);
                            expectedEntries.remove(element.getKey(), element.getValue());
                            iterator.remove();
                            assertThrows(IllegalStateException.class, iterator::remove);
                        }
                        remove = !remove;
                    }

                    List<Map.Entry<K, V>> remaining = toList(entrySet);
                    assertHasElements(remaining, expectedElements, fixedOrder());
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
                    com.github.robtimus.unittestsupport.collections.IteratorTests.ForEachRemainingTests<Map.Entry<K, V>> {
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
        interface RemoveTests<K, V> extends EntrySetTests<K, V>, CollectionTests.RemoveTests<Map.Entry<K, V>> {

            @Override
            @ParameterizedTest(name = "{0}: {1}")
            @ArgumentsSource(CollectionTests.RemoveArgumentsProvider.class)
            @DisplayName("remove(Object)")
            default void testRemove(Object o, boolean expected) {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                assertEquals(expected, entrySet.remove(o));

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();
                if (expected) {
                    expectedEntries = new HashMap<>(expectedEntries);
                    expectedElements = expectedEntries.entrySet();
                    expectedElements.remove(o);
                }

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("remove(Object) with null")
            default void testRemoveNull(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                RemoveNullNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveNullNotSupported.class);

                if (annotation == null) {
                    assertFalse(entrySet.remove(null));
                } else {
                    assertThrows(annotation.expected(), () -> entrySet.remove(null));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("remove(Object) with incompatible object")
            default void testRemoveIncompatibleObject(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<?> entrySet = map.entrySet();

                RemoveIncompatibleNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveIncompatibleNotSupported.class);

                if (annotation == null) {
                    assertFalse(entrySet.remove(new IncompatibleObject()));
                } else {
                    assertThrows(annotation.expected(), () -> entrySet.remove(new IncompatibleObject()));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
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
         * By default, the tests in this interface assume that calling {@link Set#removeAll(Collection)} with a collection containing {@code null} or
         * an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the case, annotate your
         * class with {@link RemoveNullNotSupported} and/or {@link RemoveIncompatibleNotSupported}.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("removeAll(Collection)")
        interface RemoveAllTests<K, V> extends EntrySetTests<K, V>, CollectionTests.RemoveAllTests<Map.Entry<K, V>> {

            @Override
            @ParameterizedTest(name = "{0}: {1}")
            @ArgumentsSource(RemoveAllArgumentsProvider.class)
            @DisplayName("removeAll(Collection)")
            default void testRemoveAll(Collection<?> c, boolean expected) {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                assertEquals(expected, entrySet.removeAll(c));

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();
                if (expected) {
                    expectedEntries = new HashMap<>(expectedEntries);
                    expectedElements = expectedEntries.entrySet();
                    expectedElements.removeAll(c);
                }

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeAll(Collection) with a null collection")
            default void testRemoveAllWithNullCollection() {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                assertThrows(NullPointerException.class, () -> entrySet.removeAll(null));

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeAll(Collection) with a collection with a null")
            default void testRemoveAllWithCollectionWithNull(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                Collection<?> c = Collections.singleton(null);

                RemoveNullNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveNullNotSupported.class);

                if (annotation == null) {
                    assertFalse(entrySet.removeAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> entrySet.removeAll(c));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeAll(Collection) with a collection with an incompatible object")
            default void testRemoveAllWithCollectionWithIncompatibleObject(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                Collection<?> c = Collections.singleton(new IncompatibleObject());

                RemoveIncompatibleNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(RemoveIncompatibleNotSupported.class);

                if (annotation == null) {
                    assertFalse(entrySet.removeAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> entrySet.removeAll(c));
                }

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
        }

        /**
         * Contains tests for {@link Set#removeIf(Predicate)} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("removeIf(Predicate)")
        interface RemoveIfTests<K, V> extends EntrySetTests<K, V>, CollectionTests.RemoveIfTests<Map.Entry<K, V>> {

            @Override
            @Test
            @DisplayName("removeIf(Predicate) with matching predicate")
            default void testRemoveIfWithMatchingPredicate() {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                boolean isEmpty = entrySet.isEmpty();
                assertEquals(!isEmpty, entrySet.removeIf(e -> true));

                assertHasElements(entrySet, Collections.emptyList(), fixedOrder());
                assertEquals(Collections.emptyMap(), map);
            }

            @Override
            @Test
            @DisplayName("removeIf(Predicate) with non-matching predicate")
            default void testRemoveIfWithNonMatchingPredicate() {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                assertFalse(entrySet.removeIf(e -> false));

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("removeIf(Predicate) with null predicate")
            default void testRemoveIfWithNullPredicate() {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                assertThrows(NullPointerException.class, () -> entrySet.removeIf(null));

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
        }

        /**
         * Contains tests for {@link Set#retainAll(Collection)} for entry sets.
         * <p>
         * By default, the tests in this interface assume that calling {@link Set#retainAll(Collection)} with a collection containing {@code null} or
         * an instance of an incompatible type will simply ignore the {@code null} and incompatible element. If either is not the case, annotate your
         * class with {@link ContainsNullNotSupported} and/or {@link ContainsIncompatibleNotSupported}
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("retainAll(Collection)")
        interface RetainAllTests<K, V> extends EntrySetTests<K, V>, CollectionTests.RetainAllTests<Map.Entry<K, V>> {

            @Override
            @ParameterizedTest(name = "{0}: {1}")
            @ArgumentsSource(RetainAllArgumentsProvider.class)
            @DisplayName("retainAll(Collection)")
            default void testRetainAll(Collection<?> c, boolean expected) {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                assertEquals(expected, entrySet.retainAll(c));

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();
                if (expected) {
                    expectedEntries = new HashMap<>(expectedEntries);
                    expectedElements = expectedEntries.entrySet();
                    expectedElements.retainAll(c);
                }

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("retainAll(Collection) with a null collection")
            default void testRetainAllWithNullCollection() {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                assertThrows(NullPointerException.class, () -> entrySet.retainAll(null));

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("retainAll(Collection) with a collection with a null")
            default void testRetainAllWithCollectionWithNull(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                Collection<Object> c = new ArrayList<>(expectedElements);
                c.add(null);

                ContainsNullNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(ContainsNullNotSupported.class);

                if (annotation == null) {
                    assertFalse(entrySet.retainAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> entrySet.retainAll(c));
                }

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }

            @Override
            @Test
            @DisplayName("retainAll(Collection) with a collection with an incompatible object")
            default void testRetainAllWithCollectionWithIncompatibleObject(TestInfo testInfo) {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                Map<K, V> expectedEntries = expectedEntries();
                Set<Map.Entry<K, V>> expectedElements = expectedEntries.entrySet();

                Collection<Object> c = new ArrayList<>(expectedElements);
                c.add(new IncompatibleObject());

                ContainsIncompatibleNotSupported annotation = testInfo.getTestClass()
                        .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                        .getAnnotation(ContainsIncompatibleNotSupported.class);

                if (annotation == null) {
                    assertFalse(entrySet.retainAll(c));
                } else {
                    assertThrows(annotation.expected(), () -> entrySet.retainAll(c));
                }

                assertHasElements(entrySet, expectedElements, fixedOrder());
                assertEquals(expectedEntries, map);
            }
        }

        /**
         * Contains tests for {@link Set#clear()} for entry sets.
         *
         * @author Rob Spoor
         * @param <K> The key type of the map to test.
         * @param <V> The value type of the map to test.
         */
        @DisplayName("clear()")
        interface ClearTests<K, V> extends EntrySetTests<K, V>, CollectionTests.ClearTests<Map.Entry<K, V>> {

            @Override
            @Test
            @DisplayName("clear()")
            default void testClear() {
                Map<K, V> map = createMap();
                Set<Map.Entry<K, V>> entrySet = map.entrySet();

                entrySet.clear();

                assertHasElements(entrySet, Collections.emptyList(), fixedOrder());
                assertEquals(Collections.emptyMap(), map);
            }
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
     * Contains tests for {@link Map#equals(Object)}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("equals(Object)")
    interface EqualsTests<K, V> extends MapTests<K, V> {

        @ParameterizedTest(name = "{0}: {1}")
        @ArgumentsSource(EqualsArgumentsProvider.class)
        @DisplayName("equals(Object)")
        default void testEquals(Map<?, ?> other, boolean expected) {
            Map<K, V> map = createMap();

            if (expected) {
                assertEquals(other, map);
            } else {
                assertNotEquals(other, map);
            }
        }

        @Test
        @DisplayName("equals(Object) with self")
        default void testEqualsSelf() {
            Map<K, V> map = createMap();

            assertEquals(map, map);
        }

        @Test
        @DisplayName("equals(Object) with null")
        default void testEqualsNull() {
            Map<K, V> map = createMap();

            assertNotEquals(null, map);
        }

        @Test
        @DisplayName("equals(Object) with set")
        default void testEqualsList() {
            Map<K, V> map = createMap();

            assertNotEquals(map.entrySet(), map);
        }
    }

    /**
     * Contains tests for {@link Map#hashCode()}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("hashCode()")
    interface HashCodeTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("hashCode()")
        default void testHashCode() {
            Map<K, V> map = createMap();

            int expected = expectedEntries().entrySet().stream()
                    .mapToInt(Map.Entry::hashCode)
                    .sum();

            assertEquals(expected, map.hashCode());
        }
    }

    /**
     * Contains tests for {@link Map#getOrDefault(Object, Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Map#getOrDefault(Object, Object)} with {@code null} or an instance of an
     * incompatible type will simply return the default value. If either is not the case, annotate your class with {@link ContainsNullKeyNotSupported}
     * and/or {@link ContainsIncompatibleKeyNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("getOrDefault(Object, Object)")
    interface GetOrDefaultTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("getOrDefault(Object, Object)")
        default void testGetOrDefault() {
            Map<K, V> map = createMap();

            List<Map.Entry<K, V>> nonContained = new ArrayList<>(nonContainedEntries().entrySet());
            V firstValue = nonContained.get(0).getValue();
            V lastValue = nonContained.get(nonContained.size() - 1).getValue();

            for (Map.Entry<K, V> entry : expectedEntries().entrySet()) {
                assertEquals(entry.getValue(), map.getOrDefault(entry.getKey(), firstValue));
                assertEquals(entry.getValue(), map.getOrDefault(entry.getKey(), lastValue));
                assertEquals(entry.getValue(), map.getOrDefault(entry.getKey(), null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertEquals(firstValue, map.getOrDefault(key, firstValue));
                assertEquals(lastValue, map.getOrDefault(key, lastValue));
                assertNull(map.getOrDefault(key, null));
            }
        }

        @Test
        @DisplayName("getOrDefault(Object, Object) with null")
        default void testGetOrDefaultWithNull(TestInfo testInfo) {
            Map<K, V> map = createMap();

            ContainsNullKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsNullKeyNotSupported.class);

            List<Map.Entry<K, V>> nonContained = new ArrayList<>(nonContainedEntries().entrySet());
            V firstValue = nonContained.get(0).getValue();
            V lastValue = nonContained.get(nonContained.size() - 1).getValue();

            if (annotation == null) {
                assertEquals(firstValue, map.getOrDefault(null, firstValue));
                assertEquals(lastValue, map.getOrDefault(null, lastValue));
                assertNull(map.getOrDefault(null, null));
            } else {
                assertThrows(annotation.expected(), () -> map.getOrDefault(null, firstValue));
                assertThrows(annotation.expected(), () -> map.getOrDefault(null, lastValue));
                assertThrows(annotation.expected(), () -> map.getOrDefault(null, null));
            }
        }

        @Test
        @DisplayName("getOrDefault(Object, Object) with an incompatible object")
        default void testGetOrDefaultWithIncompatibleObject(TestInfo testInfo) {
            Map<K, V> map = createMap();

            ContainsIncompatibleKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(ContainsIncompatibleKeyNotSupported.class);

            List<Map.Entry<K, V>> nonContained = new ArrayList<>(nonContainedEntries().entrySet());
            V firstValue = nonContained.get(0).getValue();
            V lastValue = nonContained.get(nonContained.size() - 1).getValue();

            if (annotation == null) {
                assertEquals(firstValue, map.getOrDefault(new IncompatibleObject(), firstValue));
                assertEquals(lastValue, map.getOrDefault(new IncompatibleObject(), lastValue));
                assertNull(map.getOrDefault(new IncompatibleObject(), null));
            } else {
                assertThrows(annotation.expected(), () -> map.getOrDefault(new IncompatibleObject(), firstValue));
                assertThrows(annotation.expected(), () -> map.getOrDefault(new IncompatibleObject(), lastValue));
                assertThrows(annotation.expected(), () -> map.getOrDefault(new IncompatibleObject(), null));
            }
        }
    }

    /**
     * Contains tests for {@link Map#forEach(BiConsumer)}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("forEach(BiConsumer)")
    interface ForEachTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("forEach(BiConsumer)")
        default void testForEach() {
            Map<K, V> map = createMap();

            Map<K, V> m = new HashMap<>();

            map.forEach(m::put);

            assertEquals(expectedEntries(), m);
        }

        @Test
        @DisplayName("forEach(BiConsumer) with null consumer")
        default void testForEachWithNullConsumer() {
            Map<K, V> map = createMap();

            assertThrows(NullPointerException.class, () -> map.forEach(null));
        }
    }

    /**
     * Contains tests for {@link Map#replaceAll(BiFunction)}.
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

            map.replaceAll(function);

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
            expectedEntries.replaceAll(function);

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("replaceAll(BiFunction) with null function")
        default void testReplaceAllWithNullOperator() {
            Map<K, V> map = createMap();

            assertThrows(NullPointerException.class, () -> map.replaceAll(null));

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#putIfAbsent(Object, Object)}.
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

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertEquals(entry.getValue(), map.putIfAbsent(entry.getKey(), nonContained));
            }

            assertEquals(expectedEntries, map);

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                assertNull(map.putIfAbsent(entry.getKey(), entry.getValue()));
            }

            expectedEntries.putAll(nonContainedEntries);
            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("putIfAbsent(Object, Object) with existing null value")
        default void testPutIfAbsentWithExistingNullValue() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());

            Map<K, V> nonContainedEntries = nonContainedEntries();
            K nonContainedKey = nonContainedEntries.keySet().iterator().next();
            V nonContainedValue = nonContainedEntries.values().iterator().next();

            map.put(nonContainedKey, null);

            expectedEntries.put(nonContainedKey, null);
            assertEquals(expectedEntries, map);

            assertTrue(map.containsKey(nonContainedKey));
            assertNull(map.get(nonContainedKey));

            assertNull(map.putIfAbsent(nonContainedKey, nonContainedValue));

            expectedEntries.put(nonContainedKey, nonContainedValue);
            assertEquals(expectedEntries, map);
        }
    }

    /**
     * Contains tests for {@link Map#remove(Object, Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Map#remove(Object, Object)} with a key or value that is {@code null} or an
     * instance of an incompatible type will simply return {@code false}. If any of these is not the case, annotate your class with
     * {@link RemoveNullKeyNotSupported}, {@link RemoveNullNotSupported}, {@link RemoveIncompatibleKeyNotSupported} and/or
     * {@link RemoveIncompatibleNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("remove(Object, Object)")
    interface RemoveExactValueTests<K, V> extends MapTests<K, V> {

        @ParameterizedTest(name = "{0}, {1}: {2}")
        @ArgumentsSource(RemoveExactValueArgumentsProvider.class)
        @DisplayName("remove(Object, Object)")
        default void testRemoveExactValue(Object key, Object value, boolean expected) {
            Map<K, V> map = createMap();

            assertEquals(expected, map.remove(key, value));

            Map<K, V> expectedEntries = expectedEntries();
            if (expected) {
                expectedEntries = new HashMap<>(expectedEntries);
                expectedEntries.remove(key);
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("remove(Object, Object) with null key")
        default void testRemoveExactValueWithNullKey(TestInfo testInfo) {
            Map<K, V> map = createMap();

            V nonContained = nonContainedEntries().values().iterator().next();

            RemoveNullKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(RemoveNullKeyNotSupported.class);

            if (annotation == null) {
                assertFalse(map.remove(null, nonContained));
            } else {
                assertThrows(annotation.expected(), () -> map.remove(null, nonContained));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with incompatible key")
        default void testRemoveExactValueWithIncompatibleKey(TestInfo testInfo) {
            Map<K, V> map = createMap();

            V nonContained = nonContainedEntries().values().iterator().next();

            RemoveIncompatibleKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(RemoveIncompatibleKeyNotSupported.class);

            if (annotation == null) {
                assertFalse(map.remove(new IncompatibleObject(), nonContained));
            } else {
                assertThrows(annotation.expected(), () -> map.remove(new IncompatibleObject(), nonContained));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with null value")
        default void testRemoveExactValueWithNullValue(TestInfo testInfo) {
            Map<K, V> map = createMap();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            RemoveNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(RemoveNullNotSupported.class);

            if (annotation == null) {
                assertFalse(map.remove(nonContained, null));
            } else {
                assertThrows(annotation.expected(), () -> map.remove(nonContained, null));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("remove(Object, Object) with incompatible value")
        default void testRemoveExactValueWithIncompatibleValue(TestInfo testInfo) {
            Map<K, V> map = createMap();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            RemoveIncompatibleNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(RemoveIncompatibleNotSupported.class);

            if (annotation == null) {
                assertFalse(map.remove(nonContained, new IncompatibleObject()));
            } else {
                assertThrows(annotation.expected(), () -> map.remove(nonContained, new IncompatibleObject()));
            }

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#replace(Object, Object, Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Map#replace(Object, Object, Object)} with a key or value that is
     * {@code null} will simply return {@code false}. If either is not the case, annotate your class with {@link StoreNullKeyNotSupported} and/or
     * {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("replace(Object, Object, Object)")
    interface ReplaceExactValueTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("replace(Object, Object, Object)")
        default void testReplaceExactValue() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                assertFalse(map.replace(entry.getKey(), entry.getValue(), nonContained));
            }

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertFalse(map.replace(entry.getKey(), nonContained, entry.getValue()));
            }

            assertEquals(expectedEntries, map);

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertTrue(map.replace(entry.getKey(), entry.getValue(), nonContained));
            }

            expectedEntries.replaceAll((k, v) -> nonContained);
            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("replace(Object, Object, Object) with null key")
        default void testReplaceExactValueWithNullKey(TestInfo testInfo) {
            Map<K, V> map = createMap();

            V nonContained = nonContainedEntries().values().iterator().next();

            StoreNullKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullKeyNotSupported.class);

            if (annotation == null) {
                assertFalse(map.replace(null, nonContained, nonContained));
            } else {
                assertThrows(annotation.expected(), () -> map.replace(null, nonContained, nonContained));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replace(Object, Object, Object) with null value")
        default void testReplaceExactValueWithNullValue(TestInfo testInfo) {
            Map<K, V> map = createMap();

            K nonContained = nonContainedEntries().keySet().iterator().next();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            if (annotation == null) {
                assertFalse(map.replace(nonContained, null, null));
            } else {
                assertThrows(annotation.expected(), () -> map.replace(nonContained, null, null));
            }

            assertEquals(expectedEntries(), map);
        }
    }

    /**
     * Contains tests for {@link Map#replace(Object, Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Map#replace(Object, Object)} with a key that is {@code null} will simply
     * return {@code null}. If this is not the case, annotate your class with {@link StoreNullKeyNotSupported}.
     * <p>
     * The tests also assume that replacing values with {@code null} will be allowed. If this is not the case, annotate your class with
     * {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("replace(Object, Object)")
    interface ReplaceTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("replace(Object, Object)")
        default void testReplace() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                assertNull(map.replace(entry.getKey(), entry.getValue()));
            }

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertEquals(entry.getValue(), map.replace(entry.getKey(), nonContained));
            }

            expectedEntries.replaceAll((k, v) -> nonContained);
            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("replace(Object, Object) with null key")
        default void testReplaceExactValueWithNullKey(TestInfo testInfo) {
            Map<K, V> map = createMap();

            V nonContained = nonContainedEntries().values().iterator().next();

            StoreNullKeyNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullKeyNotSupported.class);

            if (annotation == null) {
                assertNull(map.replace(null, nonContained));
            } else {
                assertThrows(annotation.expected(), () -> map.replace(null, nonContained));
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("replace(Object, Object) with null value")
        default void testReplaceExactValueWithNullValue(TestInfo testInfo) {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            if (annotation == null) {
                for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                    assertEquals(entry.getValue(), map.replace(entry.getKey(), null));
                }

                expectedEntries = new HashMap<>(expectedEntries);
                expectedEntries.replaceAll((k, v) -> null);
            } else {
                for (K key : expectedEntries.keySet()) {
                    assertThrows(annotation.expected(), () -> map.replace(key, null));
                }
            }

            assertEquals(expectedEntries, map);
        }
    }

    /**
     * Contains tests for {@link Map#computeIfAbsent(Object, Function)}.
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
            Map<K, V> map = createMap();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertEquals(entry.getValue(), map.computeIfAbsent(entry.getKey(), k -> nonContained));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                V value = entry.getValue();

                assertEquals(entry.getValue(), map.computeIfAbsent(entry.getKey(), k -> value));
            }

            expectedEntries.putAll(nonContainedEntries);

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfAbsent(Object, Function) with function returning null")
        default void testComputeIfAbsentWithFunctionReturningNull() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertEquals(entry.getValue(), map.computeIfAbsent(entry.getKey(), k -> null));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                assertNull(map.computeIfAbsent(entry.getKey(), k -> null));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfAbsent(Object, Function) with throwing function")
        default void testComputeIfAbsentWithThrowingFunction() {
            Map<K, V> map = createMap();

            RuntimeException exception = new RuntimeException();
            Function<K, V> function = k -> {
                throw exception;
            };

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertEquals(entry.getValue(), map.computeIfAbsent(entry.getKey(), function));
            }

            for (K key : nonContainedEntries().keySet()) {
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> map.computeIfAbsent(key, function));
                assertSame(exception, thrown);
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfAbsent(Object, Function) with null function")
        default void testComputeIfAbsentWithNullFunction() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrows(NullPointerException.class, () -> map.computeIfAbsent(key, null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertThrows(NullPointerException.class, () -> map.computeIfAbsent(key, null));
            }

            assertEquals(expectedEntries, map);
        }
    }

    /**
     * Contains tests for {@link Map#computeIfPresent(Object, BiFunction)}.
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
            Map<K, V> map = createMap();

            Map<K, V> nonContainedEntries = nonContainedEntries();
            V nonContained = nonContainedEntries.values().iterator().next();

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());

            for (K key : expectedEntries.keySet()) {
                assertEquals(nonContained, map.computeIfPresent(key, (k, v) -> nonContained));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                V value = entry.getValue();

                assertNull(map.computeIfPresent(entry.getKey(), (k, v) -> value));
            }

            expectedEntries.replaceAll((k, v) -> nonContained);

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfPresent(Object, BiFunction) with function returning null")
        default void testComputeIfPresentWithFunctionReturningNull() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertNull(map.computeIfPresent(key, (k, v) -> null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertNull(map.computeIfPresent(key, (k, v) -> null));
            }

            assertEquals(Collections.emptyMap(), map);
        }

        @Test
        @DisplayName("computeIfPresent(Object, BiFunction) with throwing function")
        default void testComputeIfPresentWithThrowingFunction() {
            Map<K, V> map = createMap();

            RuntimeException exception = new RuntimeException();
            BiFunction<K, V, V> function = (k, v) -> {
                throw exception;
            };

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> map.computeIfPresent(key, function));
                assertSame(exception, thrown);
            }

            for (K key : nonContainedEntries().keySet()) {
                assertNull(map.computeIfPresent(key, function));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("computeIfPresent(Object, BiFunction) with null function")
        default void testComputeIfPresentWithNullFunction() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrows(NullPointerException.class, () -> map.computeIfPresent(key, null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertThrows(NullPointerException.class, () -> map.computeIfPresent(key, null));
            }

            assertEquals(expectedEntries, map);
        }
    }

    /**
     * Contains tests for {@link Map#compute(Object, BiFunction)}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("compute(Object, BiFunction)")
    interface ComputeTests<K, V> extends MapTests<K, V> {

        @Test
        @DisplayName("compute(Object, BiFunction)")
        default void testCompute() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
            Map<K, V> nonContainedEntries = nonContainedEntries();

            V nonContained = nonContainedEntries.values().iterator().next();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertEquals(nonContained, map.compute(entry.getKey(), (k, v) -> nonContained));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                V value = entry.getValue();

                assertEquals(value, map.compute(entry.getKey(), (k, v) -> value));
            }

            expectedEntries.replaceAll((k, v) -> nonContained);
            expectedEntries.putAll(nonContainedEntries);

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("compute(Object, BiFunction) with function returning null")
        default void testComputeWithFunctionReturningNull() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertNull(map.compute(key, (k, v) -> null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertNull(map.compute(key, (k, v) -> null));
            }

            assertEquals(Collections.emptyMap(), map);
        }

        @Test
        @DisplayName("compute(Object, BiFunction) with throwing function")
        default void testComputeWithThrowingFunction() {
            Map<K, V> map = createMap();

            RuntimeException exception = new RuntimeException();
            BiFunction<K, V, V> function = (k, v) -> {
                throw exception;
            };

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> map.compute(key, function));
                assertSame(exception, thrown);
            }

            for (K key : nonContainedEntries().keySet()) {
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> map.compute(key, function));
                assertSame(exception, thrown);
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("compute(Object, BiFunction) with null function")
        default void testComputeWithNullFunction() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrows(NullPointerException.class, () -> map.compute(key, null));
            }

            for (K key : nonContainedEntries().keySet()) {
                assertThrows(NullPointerException.class, () -> map.compute(key, null));
            }

            assertEquals(expectedEntries, map);
        }
    }

    /**
     * Contains tests for {@link Map#merge(Object, Object, BiFunction)}.
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
            Map<K, V> map = createMap();

            BinaryOperator<V> operator = combineValuesOperator();

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
            Map<K, V> nonContainedEntries = nonContainedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                V value = entry.getValue();
                V newValue = operator.apply(value, value);

                assertEquals(newValue, map.merge(entry.getKey(), value, operator));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                V value = entry.getValue();

                assertEquals(value, map.merge(entry.getKey(), value, operator));
            }

            expectedEntries.replaceAll((k, v) -> operator.apply(v, v));
            expectedEntries.putAll(nonContainedEntries);

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("merge(Object, Object, BiFunction) with function returning null")
        default void testMergeWithFunctionReturningNull() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();
            Map<K, V> nonContainedEntries = nonContainedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertNull(map.merge(entry.getKey(), entry.getValue(), (v1, v2) -> null));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                V value = entry.getValue();

                assertEquals(value, map.merge(entry.getKey(), value, (v1, v2) -> null));
            }

            assertEquals(nonContainedEntries, map);
        }

        @Test
        @DisplayName("merge(Object, Object, BiFunction) with throwing function")
        default void testMergeWithThrowingFunction() {
            Map<K, V> map = createMap();

            RuntimeException exception = new RuntimeException();
            BinaryOperator<V> operator = (v1, v2) -> {
                throw exception;
            };

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());
            Map<K, V> nonContainedEntries = nonContainedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> map.merge(entry.getKey(), entry.getValue(), operator));
                assertSame(exception, thrown);
            }

            for (Map.Entry<K, V> entry : nonContainedEntries.entrySet()) {
                V value = entry.getValue();

                assertEquals(value, map.merge(entry.getKey(), value, operator));
            }

            expectedEntries.putAll(nonContainedEntries);

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("merge(Object, Object, BiFunction) with null function")
        default void testMergeWithNullFunction() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : expectedEntries.entrySet()) {
                assertThrows(NullPointerException.class, () -> map.merge(entry.getKey(), entry.getValue(), null));
            }

            for (Map.Entry<K, V> entry : nonContainedEntries().entrySet()) {
                assertThrows(NullPointerException.class, () -> map.merge(entry.getKey(), entry.getValue(), null));
            }

            assertEquals(expectedEntries, map);
        }
    }

    /**
     * An arguments provider for {@link RemoveTests#testRemove(Object, Object, boolean)}.
     *
     * @author Rob Spoor
     */
    final class RemoveArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            RemoveTests<?, ?> instance = (RemoveTests<?, ?>) context.getRequiredTestInstance();

            Stream<Arguments> expected = instance.expectedEntries().entrySet().stream()
                    .map(e -> arguments(e.getKey(), e.getValue(), true));
            Stream<Arguments> notExpected = instance.nonContainedEntries().keySet().stream()
                    .map(e -> arguments(e, null, false));

            return Stream.of(expected, notExpected).flatMap(Function.identity());
        }
    }

    /**
     * An arguments provider for {@link MapTests.EqualsTests#testEquals(Map, boolean)}.
     *
     * @author Rob Spoor
     */
    final class EqualsArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            EqualsTests<?, ?> instance = (EqualsTests<?, ?>) context.getRequiredTestInstance();

            Map<?, ?> expected = instance.expectedEntries();
            Map.Entry<?, ?> nonContained = instance.nonContainedEntries().entrySet().iterator().next();

            List<Arguments> arguments = new ArrayList<>();
            arguments.add(arguments(new HashMap<>(expected), true));

            if (!expected.isEmpty()) {
                Map<Object, Object> map = new HashMap<>(expected);
                Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();
                iterator.next();
                iterator.remove();
                arguments.add(arguments(map, false));

                Map<Object, Object> withDifferentValue = new HashMap<>(expected);
                iterator = withDifferentValue.entrySet().iterator();
                Map.Entry<Object, Object> entry = iterator.next();
                entry.setValue(nonContained);
                arguments.add(arguments(map, false));
            }

            Map<Object, Object> withNonContained = new HashMap<>(expected);
            withNonContained.put(nonContained.getKey(), nonContained.getValue());
            arguments.add(arguments(withNonContained, false));

            return arguments.stream();
        }
    }

    /**
     * An arguments provider for {@link RemoveExactValueTests#testRemoveExactValue(Object, Object, boolean)}.
     *
     * @author Rob Spoor
     */
    final class RemoveExactValueArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            RemoveExactValueTests<?, ?> instance = (RemoveExactValueTests<?, ?>) context.getRequiredTestInstance();

            Object nonContained = instance.nonContainedEntries().values().iterator().next();

            Stream<Arguments> expected = instance.expectedEntries().entrySet().stream()
                    .map(e -> arguments(e.getKey(), e.getValue(), true));
            Stream<Arguments> containedKeysWithDifferentValue = instance.expectedEntries().keySet().stream()
                    .map(e -> arguments(e, nonContained, false));
            Stream<Arguments> notExpected = instance.nonContainedEntries().keySet().stream()
                    .map(e -> arguments(e, null, false));

            return Stream.of(expected, containedKeysWithDifferentValue, notExpected).flatMap(Function.identity());
        }
    }
}
