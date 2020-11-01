/*
 * ReaderDelegateTestsTest.java
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
import org.apache.commons.io.input.ProxyReader;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.io.ReaderDelegateTests;
import com.github.robtimus.junit.support.io.ReaderDelegateTests.CloseTests;
import com.github.robtimus.junit.support.io.ReaderDelegateTests.MarkResetTests;
import com.github.robtimus.junit.support.io.ReaderDelegateTests.ReadCharTests;
import com.github.robtimus.junit.support.io.ReaderDelegateTests.ReadIntoCharArrayPortionTests;
import com.github.robtimus.junit.support.io.ReaderDelegateTests.ReadIntoCharArrayTests;
import com.github.robtimus.junit.support.io.ReaderDelegateTests.ReadIntoCharBufferTests;
import com.github.robtimus.junit.support.io.ReaderDelegateTests.ReadyTests;
import com.github.robtimus.junit.support.io.ReaderDelegateTests.SkipTests;

class ReaderDelegateTestsTest {

    @Nested
    class ReadIntoCharBuffer extends ReaderDelegateTestsBase implements ReadIntoCharBufferTests {
        // no new tests
    }

    @Nested
    class ReadChar extends ReaderDelegateTestsBase implements ReadCharTests {
        // no new tests
    }

    @Nested
    class ReadIntoCharArray extends ReaderDelegateTestsBase implements ReadIntoCharArrayTests {
        // no new tests
    }

    @Nested
    class ReadIntoCharArrayPortion extends ReaderDelegateTestsBase implements ReadIntoCharArrayPortionTests {
        // no new tests
    }

    @Nested
    class Skip extends ReaderDelegateTestsBase implements SkipTests {
        // no new tests
    }

    @Nested
    class Ready extends ReaderDelegateTestsBase implements ReadyTests {
        // no new tests
    }

    @Nested
    class MarkReset extends ReaderDelegateTestsBase implements MarkResetTests {
        // no new tests
    }

    @Nested
    class Close extends ReaderDelegateTestsBase implements CloseTests {
        // no new tests
    }

    abstract static class ReaderDelegateTestsBase implements ReaderDelegateTests {

        @Override
        public Reader wrapReader(Reader delegate) {
            return new ProxyReader(delegate) {
                // no new content
            };
        }
    }
}
