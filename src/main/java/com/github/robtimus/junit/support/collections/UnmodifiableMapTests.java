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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
            Map<?, ?> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (K key : expectedEntries.keySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.remove(key));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("remove(Object) with non-contained elements")
        default void testRemoveNonContainedElements() {
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

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
            Map<?, ?> map = createMap();

            Map<?, ?> expectedEntries = expectedEntries();

            for (Map.Entry<?, ?> entry : expectedEntries.entrySet()) {
                assertThrows(UnsupportedOperationException.class, () -> map.remove(entry.getKey(), entry.getValue()));
            }

            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("remove(Object, Object) with non-contained elements")
        default void testRemoveExactValueWithNonContainedElements() {
            Map<?, ?> map = createMap();

            for (Map.Entry<?, ?> entry : nonContainedEntries().entrySet()) {
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
            Map<?, ?> map = createMap();

            Object nonContained = nonContainedEntries().values().iterator().next();

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
            Map<?, ?> map = createMap();

            Object nonContained = nonContainedEntries().values().iterator().next();

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
            Map<?, ?> map = createMap();

            Object nonContained = nonContainedEntries().keySet().iterator().next();

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
            Map<?, ?> map = createMap();

            Object nonContained = nonContainedEntries().keySet().iterator().next();

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
