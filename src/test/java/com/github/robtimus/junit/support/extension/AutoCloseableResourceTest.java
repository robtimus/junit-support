/*
 * AutoCloseableResourceTest.java
 * Copyright 2025 Rob Spoor
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

package com.github.robtimus.junit.support.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AutoCloseableResourceTest {

    @Nested
    @DisplayName("Wrapper")
    class WrapperTest {

        @Nested
        @DisplayName("forAutoCloseable")
        class ForAutoCloseable {

            @Test
            @DisplayName("null closeable")
            void testNullCloseable() {
                assertThrows(NullPointerException.class, () -> AutoCloseableResource.Wrapper.forAutoCloseable(null));
            }

            @Test
            @DisplayName("non-null closeable")
            @SuppressWarnings("resource")
            void testNonNullCloseable() throws Exception {
                AutoCloseable closeable = mock(AutoCloseable.class);

                AutoCloseableResource.Wrapper<AutoCloseable> wrapper = AutoCloseableResource.Wrapper.forAutoCloseable(closeable);

                assertSame(closeable, wrapper.unwrap());

                wrapper.close();
                wrapper.close();

                verify(closeable, times(2)).close();
            }
        }

        @Nested
        @DisplayName("forObject")
        class ForObject {

            @Test
            @DisplayName("null object")
            @SuppressWarnings("resource")
            void testNullObject() {
                AutoCloseable onClose = mock(AutoCloseable.class);

                assertThrows(NullPointerException.class, () -> AutoCloseableResource.Wrapper.forObject(null, onClose));
            }

            @Test
            @DisplayName("null action")
            void testNullAction() {
                assertThrows(NullPointerException.class, () -> AutoCloseableResource.Wrapper.forObject(1, null));
            }

            @Test
            @DisplayName("non-null object and action")
            @SuppressWarnings("resource")
            void testNonNullObjectAndAction() throws Exception {
                AutoCloseable closeable = mock(AutoCloseable.class);

                AutoCloseableResource.Wrapper<Integer> wrapper = AutoCloseableResource.Wrapper.forObject(1, closeable);

                assertEquals(1, wrapper.unwrap());

                wrapper.close();
                wrapper.close();

                verify(closeable).close();
            }
        }
    }
}
