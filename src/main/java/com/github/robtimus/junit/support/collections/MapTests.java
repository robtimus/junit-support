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

package com.github.robtimus.junit.support.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
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
import com.github.robtimus.junit.support.collections.annotation.ContainsIncompatibleKeyNotSupported;
import com.github.robtimus.junit.support.collections.annotation.ContainsIncompatibleNotSupported;
import com.github.robtimus.junit.support.collections.annotation.ContainsNullKeyNotSupported;
import com.github.robtimus.junit.support.collections.annotation.ContainsNullNotSupported;
import com.github.robtimus.junit.support.collections.annotation.RemoveIncompatibleKeyNotSupported;
import com.github.robtimus.junit.support.collections.annotation.RemoveIncompatibleNotSupported;
import com.github.robtimus.junit.support.collections.annotation.RemoveNullKeyNotSupported;
import com.github.robtimus.junit.support.collections.annotation.RemoveNullNotSupported;
import com.github.robtimus.junit.support.collections.annotation.StoreNullKeyNotSupported;
import com.github.robtimus.junit.support.collections.annotation.StoreNullNotSupported;

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
     * By default, the tests in this interface assume that calling {@link Map#containsKey(Object)} with {@code null} or an instance of an
     * incompatible type will simply return {@code false}. If either is not the case, annotate your class with {@link ContainsNullKeyNotSupported}
     * and/or {@link ContainsIncompatibleKeyNotSupported}.
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
            Map<?, ?> map = createMap();

            for (Object o : expectedEntries().keySet()) {
                assertTrue(map.containsKey(o));
            }

            for (Object o : nonContainedEntries().keySet()) {
                assertFalse(map.containsKey(o));
            }
        }

        @Test
        @DisplayName("containsKey(Object) with null")
        default void testContainsKeyWithNull(TestInfo testInfo) {
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

            for (Object o : expectedEntries().values()) {
                assertTrue(map.containsValue(o));
            }

            for (Object o : nonContainedEntries().values()) {
                assertFalse(map.containsValue(o));
            }
        }

        @Test
        @DisplayName("containsValue(Object) with null")
        default void testContainsValueWithNull(TestInfo testInfo) {
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

            for (Map.Entry<?, ?> entry : expectedEntries().entrySet()) {
                assertEquals(entry.getValue(), map.get(entry.getKey()));
            }

            for (Object o : nonContainedEntries().keySet()) {
                assertNull(map.get(o));
            }
        }

        @Test
        @DisplayName("get(Object) with null")
        default void testGetWithNull(TestInfo testInfo) {
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

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
     * the such an entry. If either is not the case, annotate your class with {@link ContainsNullKeyNotSupported} and/or
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
     * By default, the tests in this interface assume that calling {@link Map#remove(Object)} with {@code null} or an instance of an
     * incompatible type will simply return {@code false}. If either is not the case, annotate your class with {@link RemoveNullKeyNotSupported}
     * and/or {@link RemoveIncompatibleKeyNotSupported}.
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
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

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

    // TODO: add test interfaces for keySet(), values() and entrySet()

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

            for (Map.Entry<?, ?> entry : expectedEntries().entrySet()) {
                assertEquals(entry.getValue(), map.getOrDefault(entry.getKey(), firstValue));
                assertEquals(entry.getValue(), map.getOrDefault(entry.getKey(), lastValue));
                assertEquals(entry.getValue(), map.getOrDefault(entry.getKey(), null));
            }

            for (Object o : nonContainedEntries().keySet()) {
                assertEquals(firstValue, map.getOrDefault(o, firstValue));
                assertEquals(lastValue, map.getOrDefault(o, lastValue));
                assertNull(map.getOrDefault(o, null));
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
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

            Object nonContained = nonContainedEntries().values().iterator().next();

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
            Map<?, ?> map = createMap();

            Object nonContained = nonContainedEntries().values().iterator().next();

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
            Map<?, ?> map = createMap();

            Object nonContained = nonContainedEntries().keySet().iterator().next();

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
            Map<?, ?> map = createMap();

            Object nonContained = nonContainedEntries().keySet().iterator().next();

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

            return Stream.of(expected, notExpected)
                    .flatMap(Function.identity());
        }
    }

    /**
     * An arguments provider for {@link EqualsTests#testEquals(Map, boolean)}.
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

            return Stream.of(expected, containedKeysWithDifferentValue, notExpected)
                    .flatMap(Function.identity());
        }
    }
}
