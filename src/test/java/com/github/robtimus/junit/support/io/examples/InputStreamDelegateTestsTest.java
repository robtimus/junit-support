/*
 * InputStreamDelegateTestsTest.java
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

import java.io.InputStream;
import org.apache.commons.io.input.ProxyInputStream;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.io.InputStreamDelegateTests;
import com.github.robtimus.junit.support.io.InputStreamDelegateTests.AvailableTests;
import com.github.robtimus.junit.support.io.InputStreamDelegateTests.CloseTests;
import com.github.robtimus.junit.support.io.InputStreamDelegateTests.MarkResetTests;
import com.github.robtimus.junit.support.io.InputStreamDelegateTests.ReadByteTests;
import com.github.robtimus.junit.support.io.InputStreamDelegateTests.ReadIntoByteArrayPortionTests;
import com.github.robtimus.junit.support.io.InputStreamDelegateTests.ReadIntoByteArrayTests;
import com.github.robtimus.junit.support.io.InputStreamDelegateTests.SkipTests;

class InputStreamDelegateTestsTest {

    @Nested
    class ReadByte extends InputStreamDelegateTestsBase implements ReadByteTests {
        // no new tests
    }

    @Nested
    class ReadIntoByteArray extends InputStreamDelegateTestsBase implements ReadIntoByteArrayTests {
        // no new tests
    }

    @Nested
    class ReadIntoByteArrayPortion extends InputStreamDelegateTestsBase implements ReadIntoByteArrayPortionTests {
        // no new tests
    }

    @Nested
    class Skip extends InputStreamDelegateTestsBase implements SkipTests {
        // no new tests
    }

    @Nested
    class Available extends InputStreamDelegateTestsBase implements AvailableTests {
        // no new tests
    }

    @Nested
    class Close extends InputStreamDelegateTestsBase implements CloseTests {
        // no new tests
    }

    @Nested
    class MarkReset extends InputStreamDelegateTestsBase implements MarkResetTests {
        // no new tests
    }

    abstract static class InputStreamDelegateTestsBase implements InputStreamDelegateTests {

        @Override
        public InputStream wrapInputStream(InputStream delegate) {
            return new ProxyInputStream(delegate) {
                // no new content
            };
        }
    }
}
