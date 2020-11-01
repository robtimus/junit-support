/*
 * ReaderTestsTest.java
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

import java.io.Reader;
import org.apache.commons.io.input.CharSequenceReader;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.io.ReaderTests;
import com.github.robtimus.junit.support.io.ReaderTests.MarkResetTests;
import com.github.robtimus.junit.support.io.ReaderTests.ReadCharTests;
import com.github.robtimus.junit.support.io.ReaderTests.ReadIntoCharArrayPortionTests;
import com.github.robtimus.junit.support.io.ReaderTests.ReadIntoCharArrayTests;
import com.github.robtimus.junit.support.io.ReaderTests.ReadIntoCharBufferTests;
import com.github.robtimus.junit.support.io.ReaderTests.ReadyTests;
import com.github.robtimus.junit.support.io.ReaderTests.SkipTests;

@SuppressWarnings("nls")
class ReaderTestsTest {

    private static final String INPUT = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa.";

    @Nested
    class ReadIntoCharBuffer extends ReaderTestsBase implements ReadIntoCharBufferTests {
        // no new tests
    }

    @Nested
    class ReadChar extends ReaderTestsBase implements ReadCharTests {
        // no new tests
    }

    @Nested
    class ReadIntoCharArray extends ReaderTestsBase implements ReadIntoCharArrayTests {
        // no new tests
    }

    @Nested
    class ReadIntoCharArrayPortion extends ReaderTestsBase implements ReadIntoCharArrayPortionTests {
        // no new tests
    }

    @Nested
    class Skip extends ReaderTestsBase implements SkipTests {

        @Override
        public boolean allowNegativeSkip() {
            return false;
        }
    }

    @Nested
    class Ready extends ReaderTestsBase implements ReadyTests {
        // no new tests
    }

    @Nested
    class MarkReset extends ReaderTestsBase implements MarkResetTests {
        // no new tests
    }

    abstract static class ReaderTestsBase implements ReaderTests {

        @Override
        public Reader createReader() {
            // use CharSequenceReader, as StringReader will decrease its index when negative indexes are used
            return new CharSequenceReader(INPUT);
        }

        @Override
        public String expectedContent() {
            return INPUT;
        }
    }
}
