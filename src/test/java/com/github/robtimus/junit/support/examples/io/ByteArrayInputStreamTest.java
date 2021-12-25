/*
 * ByteArrayInputStreamTest.java
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

package com.github.robtimus.junit.support.examples.io;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.io.InputStreamTests;
import com.github.robtimus.junit.support.io.InputStreamTests.AvailableTests;
import com.github.robtimus.junit.support.io.InputStreamTests.MarkResetTests;
import com.github.robtimus.junit.support.io.InputStreamTests.ReadByteTests;
import com.github.robtimus.junit.support.io.InputStreamTests.ReadIntoByteArrayPortionTests;
import com.github.robtimus.junit.support.io.InputStreamTests.ReadIntoByteArrayTests;
import com.github.robtimus.junit.support.io.InputStreamTests.SkipTests;

@SuppressWarnings("nls")
class ByteArrayInputStreamTest {

    private static final String INPUT = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa.";

    @Nested
    class ReadByteTest extends InputStreamTestBase implements ReadByteTests {
        // no new tests
    }

    @Nested
    class ReadIntoByteArrayTest extends InputStreamTestBase implements ReadIntoByteArrayTests {
        // no new tests
    }

    @Nested
    class ReadIntoByteArrayPortionTest extends InputStreamTestBase implements ReadIntoByteArrayPortionTests {
        // no new tests
    }

    @Nested
    class SkipTest extends InputStreamTestBase implements SkipTests {

        @Override
        public boolean allowNegativeSkip() {
            return true;
        }
    }

    @Nested
    class AvailableTest extends InputStreamTestBase implements AvailableTests {
        // no new tests
    }

    @Nested
    class MarkResetTest extends InputStreamTestBase implements MarkResetTests {

        @Override
        public boolean hasDefaultMark() {
            return true;
        }
    }

    abstract class InputStreamTestBase implements InputStreamTests {

        private Set<String> methodsCalled;

        @BeforeEach
        void initializeMethodsCalled() {
            methodsCalled = new HashSet<>();
        }

        @Override
        public InputStream inputStream() {
            assertTrue(methodsCalled.add("inputStream"), "inputStream called multiple times");
            return new ByteArrayInputStream(INPUT.getBytes());
        }

        @Override
        public byte[] expectedContent() {
            assertTrue(methodsCalled.add("expectedContent"), "expectedContent called multiple times");
            return INPUT.getBytes();
        }
    }
}
