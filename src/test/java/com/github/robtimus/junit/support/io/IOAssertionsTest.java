/*
 * IOAssertionsTest.java
 * Copyright 2021 Rob Spoor
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

package com.github.robtimus.junit.support.io;

import static com.github.robtimus.junit.support.io.IOAssertions.assertNotSerializable;
import static com.github.robtimus.junit.support.io.IOAssertions.assertSerializable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IOAssertionsTest {

    @Test
    @DisplayName("assertSerializable(T)")
    void testAssertSerializable() {
        String input = UUID.randomUUID().toString();
        String output = assertSerializable(input);
        assertEquals(input, output);
    }

    @Test
    @DisplayName("assertNotSerializable(Object)")
    void testAssertNotSerializable() {
        Object object = new Object();
        assertNotSerializable(object);
    }
}
