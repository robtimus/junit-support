/*
 * UnmodifiableMapEntryTests.java
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contains tests for {@link java.util.Map.Entry Map.Entry} for unmodifiable entries.
 *
 * @author Rob Spoor
 * @param <K> The key type of the map to test.
 * @param <V> The value type of the map to test.
 */
public interface UnmodifiableMapEntryTests<K, V> extends MapEntryTests<K, V> {

    /**
     * Contains tests for {@link Entry#setValue(Object)}.
     *
     * @author Rob Spoor
     * @param <K> The key type of the map to test.
     * @param <V> The value type of the map to test.
     */
    @DisplayName("setValue(Object)")
    interface SetValueTests<K, V> extends UnmodifiableMapEntryTests<K, V> {

        /**
         * Returns a unary operator that can be used to create new values to set with {@link Entry#setValue(Object)}.
         *
         * @return A unary operator that can be used to create new values to set with {@link Entry#setValue(Object)}.
         */
        UnaryOperator<V> replaceValueOperator();

        @Test
        @DisplayName("setValue(Object) with same value")
        default void testSetValueWithSameValue() {
            Map<K, V> map = createMap();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                V value = entry.getValue();

                // if the value stays the same, either it does nothing or it throws an exception
                try {
                    assertEquals(value, entry.setValue(value));
                } catch (@SuppressWarnings("unused") UnsupportedOperationException e) {
                    // ignore
                }
            }

            assertEquals(expectedEntries(), map);
        }

        @Test
        @DisplayName("setValue(Object) with updated value")
        default void testSetValueWithUpdatedValue() {
            Map<K, V> map = createMap();

            UnaryOperator<V> operator = replaceValueOperator();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                V value = entry.getValue();

                assertThrows(UnsupportedOperationException.class, () -> entry.setValue(operator.apply(value)));
            }

            assertEquals(expectedEntries(), map);
        }
    }
}
