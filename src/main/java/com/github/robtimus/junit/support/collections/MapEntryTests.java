/*
 * MapEntryTests.java
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import com.github.robtimus.junit.support.collections.annotation.StoreNullNotSupported;

/**
 * Contains tests for {@link java.util.Map.Entry Map.Entry}.
 *
 * @author Rob Spoor
 * @param <K> The key type of the map to test.
 * @param <V> The value type of the map to test.
 */
public interface MapEntryTests<K, V> extends MapTests<K, V> {

    /**
     * Contains tests for {@link Entry#getValue()}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("getValue()")
    interface GetValueTests<K, V> extends MapEntryTests<K, V> {

        @Test
        @DisplayName("getValue()")
        default void testGetValue() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                assertEquals(expectedEntries.get(entry.getKey()), entry.getValue());
            }
        }
    }

    /**
     * Contains tests for {@link Entry#setValue(Object)}.
     * <p>
     * By default, the tests in this interface assume that calling {@link Entry#setValue(Object)} with {@code null} value will simply add the
     * such an entry. If this is not the case, annotate your class with {@link StoreNullNotSupported}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("setValue(Object)")
    interface SetValueTests<K, V> extends MapEntryTests<K, V> {

        /**
         * Returns a unary operator that can be used to create new values to set with {@link Entry#setValue(Object)}.
         *
         * @return A unary operator that can be used to create new values to set with {@link Entry#setValue(Object)}.
         */
        UnaryOperator<V> replaceValueOperator();

        @Test
        @DisplayName("setValue(Object)")
        default void testSetValue() {
            Map<K, V> map = createMap();

            Map<K, V> expectedEntries = new HashMap<>(expectedEntries());

            UnaryOperator<V> operator = replaceValueOperator();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                V value = entry.getValue();

                assertEquals(expectedEntries.get(entry.getKey()), entry.setValue(operator.apply(value)));
            }

            expectedEntries.replaceAll((k, v) -> operator.apply(v));
            assertEquals(expectedEntries, map);
        }

        @Test
        @DisplayName("setValue(Object) with null")
        default void testSetValueWithNull(TestInfo testInfo) {
            Map<K, V> map = createMap();

            StoreNullNotSupported annotation = testInfo.getTestClass()
                    .orElseThrow(() -> new IllegalStateException("test class should be available")) //$NON-NLS-1$
                    .getAnnotation(StoreNullNotSupported.class);

            Map<K, V> expectedEntries = expectedEntries();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (annotation == null) {
                    assertEquals(expectedEntries.get(entry.getKey()), entry.setValue(null));
                } else {
                    assertThrows(annotation.expected(), () -> entry.setValue(null));
                }
            }

            if (annotation == null) {
                expectedEntries = new HashMap<>(expectedEntries);
                expectedEntries.replaceAll((k, v) -> null);

                assertEquals(expectedEntries, map);
            } else {
                assertEquals(expectedEntries, map);
            }
        }
    }

    /**
     * Contains tests for {@link Entry#equals(Object)}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("equals(Object)")
    interface EqualsTests<K, V> extends MapEntryTests<K, V> {

        @Test
        @DisplayName("equals(Object)")
        default void testEquals() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                Map.Entry<K, V> other = new SimpleEntry<>(entry);

                assertEquals(other, entry);
            }
        }

        @Test
        @DisplayName("equals(Object) with self")
        default void testEqualsSelf() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                assertEquals(entry, entry);
            }
        }

        @Test
        @DisplayName("equals(Object) with other key")
        default void testEqualsWithOtherKey() {
            Map<K, V> map = createMap();

            K otherKey = nonContainedEntries().keySet().iterator().next();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                Map.Entry<K, V> other = new SimpleEntry<>(otherKey, entry.getValue());

                assertNotEquals(other, entry);
            }
        }

        @Test
        @DisplayName("equals(Object) with other value")
        default void testEqualsWithOtherValue() {
            Map<K, V> map = createMap();

            V otherValue = nonContainedEntries().values().iterator().next();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                Map.Entry<K, V> other = new SimpleEntry<>(entry.getKey(), otherValue);

                assertNotEquals(other, entry);
            }
        }

        @Test
        @DisplayName("equals(Object) with null key")
        default void testEqualsWithNullKey() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                Map.Entry<K, V> other = new SimpleEntry<>(null, entry.getValue());

                assertNotEquals(other, entry);
            }
        }

        @Test
        @DisplayName("equals(Object) with null value")
        default void testEqualsWithNullValue() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                Map.Entry<K, V> other = new SimpleEntry<>(entry.getKey(), null);

                assertNotEquals(other, entry);
            }
        }

        @Test
        @DisplayName("equals(Object) with null")
        default void testEqualsNull() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                assertNotEquals(null, entry);
            }
        }

        @Test
        @DisplayName("equals(Object) with incompatible object")
        default void testEqualsIncompatibleObject() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                assertNotEquals(1, entry);
            }
        }
    }

    /**
     * Contains tests for {@link Entry#hashCode()}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("equals(Object)")
    interface HashCodeTests<K, V> extends MapEntryTests<K, V> {

        @Test
        @DisplayName("hashCode()")
        default void testHashCode() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();

                int expected = (key != null ? key.hashCode() : 0) ^ (value != null ? value.hashCode() : 0);

                assertEquals(expected, entry.hashCode());
            }
        }
    }
}
