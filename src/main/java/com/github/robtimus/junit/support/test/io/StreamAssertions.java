/*
 * StreamAssertions.java
 * Copyright 2022 Rob Spoor
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

package com.github.robtimus.junit.support.test.io;

import static com.github.robtimus.junit.support.ThrowableAssertions.assertThrowsOneOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

final class StreamAssertions {

    private StreamAssertions() {
    }

    static void assertNegativeSkip(Reader reader, boolean allowNegativeSkip) throws IOException {
        if (allowNegativeSkip) {
            assertEquals(0, reader.skip(-1));
        } else {
            assertThrowsOneOf(IllegalArgumentException.class, IOException.class, () -> reader.skip(-1));
        }
    }

    static void assertNegativeSkip(InputStream inputStream, boolean allowNegativeSkip) throws IOException {
        if (allowNegativeSkip) {
            assertEquals(0, inputStream.skip(-1));
        } else {
            assertThrowsOneOf(IllegalArgumentException.class, IOException.class, () -> inputStream.skip(-1));
        }
    }
}
