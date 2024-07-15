/*
 * CharSequenceReaderTest.java
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
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.input.CharSequenceReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.test.io.ReaderTests;
import com.github.robtimus.junit.support.test.io.ReaderTests.MarkResetTests;
import com.github.robtimus.junit.support.test.io.ReaderTests.ReadCharTests;
import com.github.robtimus.junit.support.test.io.ReaderTests.ReadIntoCharArrayPortionTests;
import com.github.robtimus.junit.support.test.io.ReaderTests.ReadIntoCharArrayTests;
import com.github.robtimus.junit.support.test.io.ReaderTests.ReadIntoCharBufferTests;
import com.github.robtimus.junit.support.test.io.ReaderTests.ReadyTests;
import com.github.robtimus.junit.support.test.io.ReaderTests.SkipTests;
import com.github.robtimus.junit.support.test.io.ReaderTests.TransferToTests;

@SuppressWarnings("nls")
class CharSequenceReaderTest {

    private static final String INPUT = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa.";

    @Nested
    class ReadIntoCharBufferTest extends ReaderTestBase implements ReadIntoCharBufferTests {
        // no new tests
    }

    @Nested
    class ReadCharTest extends ReaderTestBase implements ReadCharTests {
        // no new tests
    }

    @Nested
    class ReadIntoCharArrayTest extends ReaderTestBase implements ReadIntoCharArrayTests {
        // no new tests
    }

    @Nested
    class ReadIntoCharArrayPortionTest extends ReaderTestBase implements ReadIntoCharArrayPortionTests {
        // no new tests
    }

    @Nested
    class SkipTest extends ReaderTestBase implements SkipTests {

        @Override
        public boolean allowNegativeSkip() {
            return false;
        }
    }

    @Nested
    class ReadyTest extends ReaderTestBase implements ReadyTests {
        // no new tests
    }

    @Nested
    class MarkResetTest extends ReaderTestBase implements MarkResetTests {

        @Override
        public boolean hasDefaultMark() {
            return true;
        }
    }

    @Nested
    class TransferToTest extends ReaderTestBase implements TransferToTests {
        // no new tests
    }

    abstract static class ReaderTestBase implements ReaderTests {

        private Set<String> methodsCalled;

        @BeforeEach
        void initializeMethodsCalled() {
            methodsCalled = new HashSet<>();
        }

        @Override
        public Reader reader() {
            assertTrue(methodsCalled.add("reader"), "reader called multiple times");
            // use CharSequenceReader, as StringReader will decrease its index when negative indexes are used
            return new CharSequenceReader(INPUT);
        }

        @Override
        public String expectedContent() {
            assertTrue(methodsCalled.add("expectedContent"), "expectedContent called multiple times");
            return INPUT;
        }
    }
}
