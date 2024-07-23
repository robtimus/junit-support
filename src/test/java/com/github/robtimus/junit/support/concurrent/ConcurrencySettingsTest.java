/*
 * ConcurrencySettingsTest.java
 * Copyright 2024 Rob Spoor
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

package com.github.robtimus.junit.support.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConcurrencySettingsTest {

    @Nested
    @DisplayName("count")
    class Count {

        @ParameterizedTest
        @ValueSource(ints = { 1, 2, Integer.MAX_VALUE })
        @DisplayName("valid count")
        void testValidCount(int count) {
            ConcurrencySettings settings = ConcurrencySettings.withCount(count);

            assertEquals(count, settings.count());
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, 0 })
        @DisplayName("invalid count")
        void testInvalidCount(int count) {
            assertThrows(IllegalArgumentException.class, () -> ConcurrencySettings.withCount(count));
        }
    }

    @Nested
    @DisplayName("thread count")
    class ThreadCount {

        @Test
        @DisplayName("default thread count")
        void testDefaultThreadCount() {
            ConcurrencySettings settings = ConcurrencySettings.withCount(1);

            assertEquals(Integer.MAX_VALUE, settings.threadCount());
        }

        @ParameterizedTest
        @ValueSource(ints = { 2, 3, Integer.MAX_VALUE })
        @DisplayName("valid thread count")
        void testValidThreadCount(int threadCount) {
            ConcurrencySettings settings = ConcurrencySettings.withCount(1).withThreadCount(threadCount);

            assertEquals(threadCount, settings.threadCount());
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, 0, 1 })
        @DisplayName("invalid thread count")
        void testInvalidThreadCount(int threadCount) {
            ConcurrencySettings settings = ConcurrencySettings.withCount(1);

            assertThrows(IllegalArgumentException.class, () -> settings.withThreadCount(threadCount));
        }
    }
}
