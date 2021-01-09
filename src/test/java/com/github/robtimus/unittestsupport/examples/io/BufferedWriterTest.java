/*
 * BufferedWriterTest.java
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

package com.github.robtimus.unittestsupport.examples.io;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests.AppendCharSequencePortionTests;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests.AppendCharSequenceTests;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests.AppendCharTests;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests.CloseTests;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests.FlushTests;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests.WriteCharArrayPortionTests;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests.WriteCharArrayTests;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests.WriteCharTests;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests.WriteStringPortionTests;
import com.github.robtimus.unittestsupport.io.WriterDelegateTests.WriteStringTests;

class BufferedWriterTest {

    @Nested
    class WriteCharTest extends WriterDelegateTestsBase implements WriteCharTests {
        // no new tests
    }

    @Nested
    class WriteCharArrayTest extends WriterDelegateTestsBase implements WriteCharArrayTests {
        // no new tests
    }

    @Nested
    class WriteCharArrayPortionTest extends WriterDelegateTestsBase implements WriteCharArrayPortionTests {
        // no new tests
    }

    @Nested
    class WriteStringTest extends WriterDelegateTestsBase implements WriteStringTests {
        // no new tests
    }

    @Nested
    class WriteStringPortionTest extends WriterDelegateTestsBase implements WriteStringPortionTests {
        // no new tests
    }

    @Nested
    class AppendCharSequenceTest extends WriterDelegateTestsBase implements AppendCharSequenceTests {
        // no new tests
    }

    @Nested
    class AppendCharSequencePortionTest extends WriterDelegateTestsBase implements AppendCharSequencePortionTests {
        // no new tests
    }

    @Nested
    class AppendCharTest extends WriterDelegateTestsBase implements AppendCharTests {
        // no new tests
    }

    @Nested
    class FlushTest extends WriterDelegateTestsBase implements FlushTests {
        // no new tests
    }

    @Nested
    class CloseTest extends WriterDelegateTestsBase implements CloseTests {
        // no new tests
    }

    @SuppressWarnings("nls")
    abstract static class WriterDelegateTestsBase implements WriterDelegateTests {

        private Set<String> methodsCalled;

        @BeforeEach
        void initializeMethodsCalled() {
            methodsCalled = new HashSet<>();
        }

        @Override
        public Writer wrapWriter(Writer delegate) {
            assertTrue(methodsCalled.add("wrapWriter"), "wrapWriter called multiple times");
            // BufferedWriter does not correctly check the bounds in write(String, int, int);
            // copy the check from BufferedWriter.write(char[], int, int)
            return new BufferedWriter(delegate) {

                @Override
                public void write(String s, int off, int len) throws IOException {
                    if ((off < 0) || (off > s.length()) || (len < 0) || ((off + len) > s.length()) || ((off + len) < 0)) {
                        throw new IndexOutOfBoundsException();
                    }
                    super.write(s, off, len);
                }
            };
        }

        @Override
        public String expectedContent(String written) {
            assertTrue(methodsCalled.add("expectedContent"), "expectedContent called multiple times");
            return written;
        }
    }
}
