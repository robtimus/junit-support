/*
 * BufferedOutputStreamTest.java
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
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.test.io.OutputStreamDelegateTests;
import com.github.robtimus.junit.support.test.io.OutputStreamDelegateTests.CloseTests;
import com.github.robtimus.junit.support.test.io.OutputStreamDelegateTests.FlushTests;
import com.github.robtimus.junit.support.test.io.OutputStreamDelegateTests.WriteByteArrayPortionTests;
import com.github.robtimus.junit.support.test.io.OutputStreamDelegateTests.WriteByteArrayTests;
import com.github.robtimus.junit.support.test.io.OutputStreamDelegateTests.WriteByteTests;

class BufferedOutputStreamTest {

    @Nested
    class WriteByteTest extends OutputStreamTestBase implements WriteByteTests {
        // no new tests
    }

    @Nested
    class WriteByteArrayTest extends OutputStreamTestBase implements WriteByteArrayTests {
        // no new tests
    }

    @Nested
    class WriteByteArrayPortionTest extends OutputStreamTestBase implements WriteByteArrayPortionTests {
        // no new tests
    }

    @Nested
    class FlushTest extends OutputStreamTestBase implements FlushTests {
        // no new tests
    }

    @Nested
    class CloseTest extends OutputStreamTestBase implements CloseTests {
        // no new tests
    }

    @SuppressWarnings("nls")
    abstract class OutputStreamTestBase implements OutputStreamDelegateTests {

        private Set<String> methodsCalled;

        @BeforeEach
        void initializeMethodsCalled() {
            methodsCalled = new HashSet<>();
        }

        @Override
        public OutputStream wrapOutputStream(OutputStream delegate) {
            assertTrue(methodsCalled.add("wrapInputStream"), "wrapInputStream called multiple times");
            return new BufferedOutputStream(delegate);
        }

        @Override
        public byte[] expectedContent(byte[] written) {
            return written;
        }
    }
}
