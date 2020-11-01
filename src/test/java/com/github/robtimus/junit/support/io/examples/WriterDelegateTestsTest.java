/*
 * WriterDelegateTestsTest.java
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.io.WriterDelegateTests;
import com.github.robtimus.junit.support.io.WriterDelegateTests.AppendCharSequencePortionTests;
import com.github.robtimus.junit.support.io.WriterDelegateTests.AppendCharSequenceTests;
import com.github.robtimus.junit.support.io.WriterDelegateTests.AppendCharTests;
import com.github.robtimus.junit.support.io.WriterDelegateTests.CloseTests;
import com.github.robtimus.junit.support.io.WriterDelegateTests.FlushTests;
import com.github.robtimus.junit.support.io.WriterDelegateTests.WriteCharArrayPortionTests;
import com.github.robtimus.junit.support.io.WriterDelegateTests.WriteCharArrayTests;
import com.github.robtimus.junit.support.io.WriterDelegateTests.WriteCharTests;
import com.github.robtimus.junit.support.io.WriterDelegateTests.WriteStringPortionTests;
import com.github.robtimus.junit.support.io.WriterDelegateTests.WriteStringTests;

class WriterDelegateTestsTest {

    @Nested
    class WriteChar extends WriterDelegateTestsBase implements WriteCharTests {
        // no new tests
    }

    @Nested
    class WriteCharArray extends WriterDelegateTestsBase implements WriteCharArrayTests {
        // no new tests
    }

    @Nested
    class WriteCharArrayPortion extends WriterDelegateTestsBase implements WriteCharArrayPortionTests {
        // no new tests
    }

    @Nested
    class WriteString extends WriterDelegateTestsBase implements WriteStringTests {
        // no new tests
    }

    @Nested
    class WriteStringPortion extends WriterDelegateTestsBase implements WriteStringPortionTests {
        // no new tests
    }

    @Nested
    class AppendCharSequence extends WriterDelegateTestsBase implements AppendCharSequenceTests {
        // no new tests
    }

    @Nested
    class AppendCharSequencePortion extends WriterDelegateTestsBase implements AppendCharSequencePortionTests {
        // no new tests
    }

    @Nested
    class AppendChar extends WriterDelegateTestsBase implements AppendCharTests {
        // no new tests
    }

    @Nested
    class Flush extends WriterDelegateTestsBase implements FlushTests {
        // no new tests
    }

    @Nested
    class Close extends WriterDelegateTestsBase implements CloseTests {
        // no new tests
    }

    abstract static class WriterDelegateTestsBase implements WriterDelegateTests {

        @Override
        public Writer wrapWriter(Writer delegate) {
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
            return written;
        }
    }
}
