/*
 * BufferedReaderTest.java
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

import java.io.BufferedReader;
import java.io.Reader;
import org.apache.commons.io.input.CharSequenceReader;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.io.ReaderDelegateTests;
import com.github.robtimus.junit.support.io.ReaderDelegateTests.CloseTests;
import com.github.robtimus.junit.support.io.ReaderTests;
import com.github.robtimus.junit.support.io.ReaderTests.MarkResetTests;
import com.github.robtimus.junit.support.io.ReaderTests.ReadCharTests;
import com.github.robtimus.junit.support.io.ReaderTests.ReadIntoCharArrayPortionTests;
import com.github.robtimus.junit.support.io.ReaderTests.ReadIntoCharArrayTests;
import com.github.robtimus.junit.support.io.ReaderTests.ReadIntoCharBufferTests;
import com.github.robtimus.junit.support.io.ReaderTests.ReadyTests;
import com.github.robtimus.junit.support.io.ReaderTests.SkipTests;

@SuppressWarnings("nls")
class BufferedReaderTest {

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
        // no new tests
    }

    @Nested
    class CloseTest extends ReaderTestBase implements CloseTests {
        // no new tests
    }

    abstract static class ReaderTestBase implements ReaderTests, ReaderDelegateTests {

        @Override
        public Reader createReader() {
            // use CharSequenceReader, as StringReader will decrease its index when negative indexes are used
            return new BufferedReader(new CharSequenceReader(INPUT));
        }

        @Override
        public Reader wrapReader(Reader delegate) {
            return new BufferedReader(delegate);
        }

        @Override
        public String expectedContent() {
            return INPUT;
        }
    }
}
