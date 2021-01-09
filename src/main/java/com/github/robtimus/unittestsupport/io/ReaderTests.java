/*
 * ReaderTests.java
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

package com.github.robtimus.unittestsupport.io;

import static com.github.robtimus.unittestsupport.io.IOAssertions.assertContainsContent;
import static com.github.robtimus.unittestsupport.io.IOAssertions.assertDoesNotThrowIOException;
import static com.github.robtimus.unittestsupport.io.IOAssertions.assertNegativeSkip;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing separate {@link Reader} functionalities.
 *
 * @author Rob Spoor
 */
public interface ReaderTests {

    /**
     * Returns the reader to test.
     * <p>
     * This method will be called only once for each test. This makes it possible to initialize the reader in a method annotated with
     * {@link BeforeEach}, and perform additional tests after the pre-defined test has finished.
     *
     * @return The reader to test.
     */
    Reader reader();

    /**
     * Returns the expected content from {@link #reader() created readers}.
     *
     * @return The expected content.
     */
    String expectedContent();

    /**
     * Contains tests for {@link Reader#read(CharBuffer)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read(CharBuffer)")
    interface ReadIntoCharBufferTests extends ReaderTests {

        @Test
        @DisplayName("read(CharBuffer)")
        default void testReadIntoCharBuffer() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    String expected = expectedContent();
                    int bufferSize = 10;

                    StringBuilder sb = new StringBuilder(expected.length());

                    CharBuffer buffer = CharBuffer.allocate(bufferSize);
                    int len;
                    while ((len = reader.read(buffer)) != -1) {
                        buffer.rewind();
                        sb.append(buffer, 0, len);
                        buffer.clear();
                    }
                    assertEquals(expected, sb.toString());
                }
            });
        }

        @Test
        @DisplayName("read(CharBuffer) with an empty CharBuffer")
        default void testReadIntoCharBufferWithEmptyCharBuffer() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    CharBuffer buffer = CharBuffer.allocate(0);
                    assertEquals(0, reader.read(buffer));

                    // assert that the read did not alter the reader's state
                    assertContainsContent(reader, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(CharBuffer) with a null CharBuffer")
        default void testReadIntoCharBufferWithNullCharBuffer() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    CharBuffer buffer = null;
                    assertThrows(NullPointerException.class, () -> reader.read(buffer));

                    // assert that the read did not alter the reader's state
                    assertContainsContent(reader, expectedContent());
                }
            });
        }
    }

    /**
     * Contains tests for {@link Reader#read()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read()")
    interface ReadCharTests extends ReaderTests {

        @Test
        @DisplayName("read()")
        default void testReadChar() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    String expected = expectedContent();

                    StringBuilder sb = new StringBuilder(expected.length());

                    int c;
                    while ((c = reader.read()) != -1) {
                        sb.append((char) c);
                    }
                    assertEquals(expected, sb.toString());
                }
            });
        }
    }

    /**
     * Contains tests for {@link Reader#read(char[])}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read(char[])")
    interface ReadIntoCharArrayTests extends ReaderTests {

        @Test
        @DisplayName("read(char[])")
        default void testReadIntoCharArray() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    String expected = expectedContent();
                    int bufferSize = 10;

                    StringBuilder sb = new StringBuilder(expected.length());

                    char[] buffer = new char[bufferSize];
                    int len;
                    while ((len = reader.read(buffer)) != -1) {
                        sb.append(buffer, 0, len);
                    }
                    assertEquals(expected, sb.toString());
                }
            });
        }

        @Test
        @DisplayName("read(char[]) with an empty array")
        default void testReadIntoCharArrayWithEmptyArray() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    char[] buffer = {};
                    assertEquals(0, reader.read(buffer));

                    // assert that the read did not alter the reader's state
                    assertContainsContent(reader, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(char[]) with a null array")
        default void testReadIntoCharArrayWithNullArray() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    char[] buffer = null;
                    assertThrows(NullPointerException.class, () -> reader.read(buffer));

                    // assert that the read did not alter the reader's state
                    assertContainsContent(reader, expectedContent());
                }
            });
        }
    }

    /**
     * Contains tests for {@link Reader#read(char[], int, int)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read(char[], int, int)")
    interface ReadIntoCharArrayPortionTests extends ReaderTests {

        @Test
        @DisplayName("read(char[], int, int)")
        default void testReadIntoCharArrayPortion() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    String expected = expectedContent();
                    int bufferSize = 10;

                    StringBuilder sb = new StringBuilder(expected.length());

                    char[] buffer = new char[bufferSize + 10];
                    int off = 5;
                    int len;
                    while ((len = reader.read(buffer, off, bufferSize)) != -1) {
                        sb.append(buffer, off, len);
                    }
                    assertEquals(expected, sb.toString());
                }
            });
        }

        @Test
        @DisplayName("read(char[], int, int) with 0 length")
        default void testReadIntoCharArrayPortionWithZeroLength() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    char[] buffer = new char[10];
                    assertEquals(0, reader.read(buffer, 5, 0));

                    // assert that the read did not alter the reader's state
                    assertContainsContent(reader, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(char[], int, int) with a null array")
        default void testReadIntoCharArrayPortionWithNullArray() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    char[] buffer = null;
                    assertThrows(NullPointerException.class, () -> reader.read(buffer, 0, 10));

                    // assert that the read did not alter the reader's state
                    assertContainsContent(reader, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(char[], int, int) with a negative offset")
        default void testReadIntoCharArrayPortionWithNegativeOffset() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    char[] buffer = new char[10];
                    Exception exception = assertThrows(Exception.class, () -> reader.read(buffer, -1, 10));
                    assertThat(exception, either(instanceOf(IndexOutOfBoundsException.class)).or(instanceOf(IllegalArgumentException.class))
                            .or(instanceOf(IOException.class)));

                    // assert that the read did not alter the reader's state
                    assertContainsContent(reader, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(char[], int, int) with an offset that exceeds the array length")
        default void testReadIntoCharArrayPortionWithTooHighOffset() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    char[] buffer = new char[10];
                    Exception exception = assertThrows(Exception.class, () -> reader.read(buffer, buffer.length + 1, 0));
                    assertThat(exception, either(instanceOf(IndexOutOfBoundsException.class)).or(instanceOf(IllegalArgumentException.class))
                            .or(instanceOf(IOException.class)));

                    // assert that the read did not alter the reader's state
                    assertContainsContent(reader, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(char[], int, int) with a negative length")
        default void testReadIntoCharArrayPortionWithNegativeLength() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    char[] buffer = new char[10];
                    Exception exception = assertThrows(Exception.class, () -> reader.read(buffer, 5, -1));
                    assertThat(exception, either(instanceOf(IndexOutOfBoundsException.class)).or(instanceOf(IllegalArgumentException.class))
                            .or(instanceOf(IOException.class)));

                    // assert that the read did not alter the reader's state
                    assertContainsContent(reader, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(char[], int, int) with a length that exceeds the array length")
        default void testReadIntoCharArrayPortionWithTooHighLength() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    char[] buffer = new char[10];
                    // don't use 0 and 11, use 1 and 10, so it's not the value of the length that triggers the error but the combination off + len
                    Exception exception = assertThrows(Exception.class, () -> reader.read(buffer, 1, buffer.length));
                    assertThat(exception, either(instanceOf(IndexOutOfBoundsException.class)).or(instanceOf(IllegalArgumentException.class))
                            .or(instanceOf(IOException.class)));

                    // assert that the read did not alter the reader's state
                    assertContainsContent(reader, expectedContent());
                }
            });
        }
    }

    /**
     * Contains tests for {@link Reader#skip(long)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("skip(long)")
    interface SkipTests extends ReaderTests {

        boolean allowNegativeSkip();

        @Test
        @DisplayName("skip(long)")
        default void testSkip() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    // skip 5, add 5, repeat
                    final int skipSize = 5;
                    final int readSize = 5;

                    String fullExpectedContent = expectedContent();
                    StringBuilder expectedContent = new StringBuilder(fullExpectedContent.length() / 2);
                    for (int i = skipSize; i < fullExpectedContent.length(); i += skipSize + readSize) {
                        expectedContent.append(fullExpectedContent, i, Math.min(i + skipSize, fullExpectedContent.length()));
                    }

                    StringBuilder sb = new StringBuilder(expectedContent.length());

                    int remaining = fullExpectedContent.length();

                    assertEquals(Math.min(skipSize, remaining), IOUtils.skipAll(reader, skipSize));
                    remaining -= skipSize;

                    char[] buffer = new char[readSize];
                    int len;
                    while ((len = IOUtils.readAll(reader, buffer)) != -1) {
                        sb.append(buffer, 0, len);
                        remaining -= readSize;

                        if (remaining > 0) {
                            assertEquals(Math.min(skipSize, remaining), IOUtils.skipAll(reader, skipSize));
                            remaining -= skipSize;
                        }
                    }
                    assertEquals(expectedContent.toString(), sb.toString());
                }
            });
        }

        @Test
        @DisplayName("skip(long) with a zero index")
        default void testSkipWithZeroIndex() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    String expectedContent = expectedContent();

                    StringBuilder sb = new StringBuilder(expectedContent.length());

                    assertEquals(0, reader.skip(0));

                    char[] buffer = new char[10];
                    int len;
                    while ((len = reader.read(buffer)) != -1) {
                        sb.append(buffer, 0, len);
                        assertEquals(0, reader.skip(0));
                    }
                    assertEquals(0, reader.skip(0));

                    // assert that the skips did not alter the reader's state
                    assertEquals(expectedContent, sb.toString());
                }
            });
        }

        @Test
        @DisplayName("skip(long) with a negative index")
        default void testSkipWithNegativeIndex() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    String expectedContent = expectedContent();

                    StringBuilder sb = new StringBuilder(expectedContent.length());

                    boolean allowNegativeSkip = allowNegativeSkip();

                    assertNegativeSkip(reader, allowNegativeSkip);

                    char[] buffer = new char[10];
                    int len;
                    while ((len = reader.read(buffer)) != -1) {
                        sb.append(buffer, 0, len);
                        assertNegativeSkip(reader, allowNegativeSkip);
                    }
                    assertNegativeSkip(reader, allowNegativeSkip);

                    // assert that the skips did not alter the reader's state
                    assertEquals(expectedContent, sb.toString());
                }
            });
        }
    }

    /**
     * Contains tests for {@link Reader#ready()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("ready()")
    interface ReadyTests extends ReaderTests {

        @Test
        @DisplayName("ready()")
        default void testReady() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    String expectedContent = expectedContent();

                    StringBuilder sb = new StringBuilder(expectedContent.length());

                    assertTrue(reader.ready());
                    char[] buffer = new char[10];
                    int len;
                    while ((len = reader.read(buffer)) != -1) {
                        sb.append(buffer, 0, len);
                        assertEquals(sb.length() < expectedContent.length(), reader.ready());
                    }
                    assertFalse(reader.ready());
                }
            });
        }
    }

    /**
     * Contains tests for {@link Reader#mark(int)} and {@link Reader#reset()}.
     * Note that {@link Reader#markSupported()} must be supported.
     *
     * @author Rob Spoor
     */
    @DisplayName("mark(int) and reset()")
    interface MarkResetTests extends ReaderTests {

        /**
         * Returns whether or not the input stream to test has an explicit mark at the start of the stream.
         * If so, then {@link InputStream#reset()} is expected to work without calling {@link InputStream#mark(int)} first.
         * Otherwise, {@link InputStream#reset()} is expected to fail without calling {@link InputStream#mark(int)} first.
         * <p>
         * This default implementation returns {@code false}.
         *
         * @return {@code true} if the input stream to test has an explicit mark at the start of the stream, or {@code false} otherwise.
         */
        default boolean hasDefaultMark() {
            return false;
        }

        @Test
        @DisplayName("markSupported()")
        default void testMarkSupported() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    assertTrue(reader.markSupported());
                }
            });
        }

        @Test
        @DisplayName("mark(int) and reset()")
        default void testMarkAndReset() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    // mark, read 10, reset, read 20, repeat
                    final int readSize = 10;

                    String fullExpectedContent = expectedContent();
                    StringBuilder expectedContent = new StringBuilder(fullExpectedContent.length() * 3 / 2);
                    for (int i = 0; i < fullExpectedContent.length(); i += readSize * 2) {
                        expectedContent.append(fullExpectedContent, i, Math.min(i + readSize, fullExpectedContent.length()));
                        expectedContent.append(fullExpectedContent, i, Math.min(i + readSize * 2, fullExpectedContent.length()));
                    }

                    StringBuilder sb = new StringBuilder(expectedContent.length());

                    char[] markedBuffer = new char[readSize];
                    char[] buffer = new char[readSize * 2];
                    int len;
                    reader.mark(readSize);
                    while ((len = IOUtils.readAll(reader, markedBuffer)) != -1) {
                        sb.append(markedBuffer, 0, len);
                        reader.reset();

                        len = IOUtils.readAll(reader, buffer);
                        if (len != -1) {
                            sb.append(buffer, 0, len);
                            reader.mark(readSize);
                        }
                    }
                    assertEquals(expectedContent.toString(), sb.toString());
                }
            });
        }

        @Test
        @DisplayName("reset() without mark(int)")
        default void testResetWithoutMark() {
            assertDoesNotThrowIOException(() -> {
                try (Reader reader = reader()) {
                    String expectedContent = expectedContent();

                    if (hasDefaultMark()) {
                        int duplicateCount = Math.min(expectedContent.length(), 10);
                        expectedContent = expectedContent.substring(0, duplicateCount) + expectedContent;
                    }

                    StringBuilder sb = new StringBuilder(expectedContent.length());

                    char[] buffer = new char[10];
                    int len = IOUtils.readAll(reader, buffer);
                    if (len != -1) {
                        sb.append(buffer, 0, len);
                    }
                    if (hasDefaultMark()) {
                        assertDoesNotThrow(reader::reset);
                    } else {
                        assertThrows(IOException.class, reader::reset);
                    }

                    while ((len = IOUtils.readAll(reader, buffer)) != -1) {
                        sb.append(buffer, 0, len);
                    }

                    assertEquals(expectedContent, sb.toString());
                }
            });
        }
    }
}
