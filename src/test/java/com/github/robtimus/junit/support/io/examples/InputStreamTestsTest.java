/*
 * InputStreamTestsTest.java
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

package com.github.robtimus.junit.support.io.examples;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.io.InputStreamTests;
import com.github.robtimus.junit.support.io.InputStreamTests.AvailableTests;
import com.github.robtimus.junit.support.io.InputStreamTests.MarkResetTests;
import com.github.robtimus.junit.support.io.InputStreamTests.ReadByteTests;
import com.github.robtimus.junit.support.io.InputStreamTests.ReadIntoByteArrayPortionTests;
import com.github.robtimus.junit.support.io.InputStreamTests.ReadIntoByteArrayTests;
import com.github.robtimus.junit.support.io.InputStreamTests.SkipTests;

@SuppressWarnings("nls")
class InputStreamTestsTest {

    private static final String INPUT = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa.";

    @Nested
    class ReadByte extends InputStreamTestsBase implements ReadByteTests {
        // no new tests
    }

    @Nested
    class ReadIntoByteArray extends InputStreamTestsBase implements ReadIntoByteArrayTests {
        // no new tests
    }

    @Nested
    class ReadIntoByteArrayPortion extends InputStreamTestsBase implements ReadIntoByteArrayPortionTests {
        // no new tests
    }

    @Nested
    class Skip extends InputStreamTestsBase implements SkipTests {

        @Override
        public boolean allowNegativeSkip() {
            return true;
        }
    }

    @Nested
    class Available extends InputStreamTestsBase implements AvailableTests {
        // no new tests
    }

    @Nested
    class MarkReset extends InputStreamTestsBase implements MarkResetTests {
        // no new tests
    }

    abstract class InputStreamTestsBase implements InputStreamTests {

        @Override
        public InputStream createInputStream() {
            return new ByteArrayInputStream(INPUT.getBytes());
        }

        @Override
        public byte[] expectedContent() {
            return INPUT.getBytes();
        }
    }
}
