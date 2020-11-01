/*
 * OutputStreamDelegateTestsTest.java
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

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.io.OutputStreamDelegateTests;
import com.github.robtimus.junit.support.io.OutputStreamDelegateTests.CloseTests;
import com.github.robtimus.junit.support.io.OutputStreamDelegateTests.FlushTests;
import com.github.robtimus.junit.support.io.OutputStreamDelegateTests.WriteByteArrayPortionTests;
import com.github.robtimus.junit.support.io.OutputStreamDelegateTests.WriteByteArrayTests;
import com.github.robtimus.junit.support.io.OutputStreamDelegateTests.WriteByteTests;

class OutputStreamDelegateTestsTest {

    @Nested
    class WriteByte extends OutputStreamDelegateTestsBase implements WriteByteTests {
        // no new tests
    }

    @Nested
    class WriteByteArray extends OutputStreamDelegateTestsBase implements WriteByteArrayTests {
        // no new tests
    }

    @Nested
    class WriteByteArrayPortion extends OutputStreamDelegateTestsBase implements WriteByteArrayPortionTests {
        // no new tests
    }

    @Nested
    class Flush extends OutputStreamDelegateTestsBase implements FlushTests {
        // no new tests
    }

    @Nested
    class Close extends OutputStreamDelegateTestsBase implements CloseTests {
        // no new tests
    }

    abstract class OutputStreamDelegateTestsBase implements OutputStreamDelegateTests {

        @Override
        public OutputStream wrapOutputStream(OutputStream delegate) {
            return new BufferedOutputStream(delegate);
        }

        @Override
        public byte[] expectedContent(byte[] written) {
            return written;
        }
    }
}
