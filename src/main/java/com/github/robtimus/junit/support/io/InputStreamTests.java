/*
 * InputStreamTests.java
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

package com.github.robtimus.junit.support.io;

import static com.github.robtimus.junit.support.io.IOAssertions.assertContainsContent;
import static com.github.robtimus.junit.support.io.IOAssertions.assertDoesNotThrowIOException;
import static com.github.robtimus.junit.support.io.IOAssertions.assertNegativeSkip;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing separate {@link InputStream} functionalities.
 *
 * @author Rob Spoor
 */
public interface InputStreamTests {

    /**
     * Creates the input stream to test.
     *
     * @return The created input stream.
     */
    InputStream createInputStream();

    /**
     * Returns the expected content from {@link #createInputStream() created input streams}.
     *
     * @return The expected content.
     */
    byte[] expectedContent();

    /**
     * Contains tests for {@link InputStream#read()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read()")
    interface ReadByteTests extends InputStreamTests {

        @Test
        @DisplayName("read()")
        default void testReadByte() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] expected = expectedContent();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expected.length);

                    int b;
                    while ((b = inputStream.read()) != -1) {
                        baos.write(b);
                    }
                    assertArrayEquals(expected, baos.toByteArray());
                }
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#read(byte[])}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read(byte[])")
    interface ReadIntoByteArrayTests extends InputStreamTests {

        @Test
        @DisplayName("read(byte[])")
        default void testReadIntoByteArray() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] expected = expectedContent();
                    int bufferSize = 10;

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expected.length);

                    byte[] buffer = new byte[bufferSize];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    assertArrayEquals(expected, baos.toByteArray());
                }
            });
        }

        @Test
        @DisplayName("read(byte[]) with an empty array")
        default void testReadIntoByteArrayWithEmptyArray() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] buffer = {};
                    assertEquals(0, inputStream.read(buffer));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(byte[]) with a null array")
        default void testReadIntoByteArrayWithNullArray() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] buffer = null;
                    assertThrows(NullPointerException.class, () -> inputStream.read(buffer));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#read(byte[], int, int)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read(byte[], int, int)")
    interface ReadIntoByteArrayPortionTests extends InputStreamTests {

        @Test
        @DisplayName("read(byte[], int, int)")
        default void testReadIntoByteArrayPortion() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] expected = expectedContent();
                    int bufferSize = 10;

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expected.length);

                    byte[] buffer = new byte[bufferSize + 10];
                    int off = 5;
                    int len;
                    while ((len = inputStream.read(buffer, off, bufferSize)) != -1) {
                        baos.write(buffer, off, len);
                    }
                    assertArrayEquals(expected, baos.toByteArray());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with 0 length")
        default void testReadIntoByteArrayPortionWithZeroLength() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] buffer = new byte[10];
                    assertEquals(0, inputStream.read(buffer, 5, 0));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with a null array")
        default void testReadIntoByteArrayPortionWithNullArray() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] buffer = null;
                    assertThrows(NullPointerException.class, () -> inputStream.read(buffer, 0, 10));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with a negative offset")
        default void testReadIntoByteArrayPortionWithNegativeOffset() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] buffer = new byte[10];
                    Exception exception = assertThrows(Exception.class, () -> inputStream.read(buffer, -1, 10));
                    assertThat(exception, either(instanceOf(IndexOutOfBoundsException.class)).or(instanceOf(IllegalArgumentException.class))
                            .or(instanceOf(IOException.class)));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with an offset that exceeds the array length")
        default void testReadIntoByteArrayPortionWithTooHighOffset() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] buffer = new byte[10];
                    Exception exception = assertThrows(Exception.class, () -> inputStream.read(buffer, buffer.length + 1, 0));
                    assertThat(exception, either(instanceOf(IndexOutOfBoundsException.class)).or(instanceOf(IllegalArgumentException.class))
                            .or(instanceOf(IOException.class)));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with a negative length")
        default void testReadIntoByteArrayPortionWithNegativeLength() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] buffer = new byte[10];
                    Exception exception = assertThrows(Exception.class, () -> inputStream.read(buffer, 5, -1));
                    assertThat(exception, either(instanceOf(IndexOutOfBoundsException.class)).or(instanceOf(IllegalArgumentException.class))
                            .or(instanceOf(IOException.class)));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with a length that exceeds the array length")
        default void testReadIntoByteArrayPortionWithTooHighLength() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] buffer = new byte[10];
                    // don't use 0 and 11, use 1 and 10
                    Exception exception = assertThrows(Exception.class, () -> inputStream.read(buffer, 1, buffer.length));
                    assertThat(exception, either(instanceOf(IndexOutOfBoundsException.class)).or(instanceOf(IllegalArgumentException.class))
                            .or(instanceOf(IOException.class)));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#skip(long)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("skip(long)")
    interface SkipTests extends InputStreamTests {

        boolean allowNegativeSkip();

        @Test
        @DisplayName("skip(long)")
        default void testSkip() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    // skip 5, add 5, repeat
                    final int skipSize = 5;
                    final int readSize = 5;

                    byte[] fullExpectedContent = expectedContent();
                    ByteArrayOutputStream expectedContent = new ByteArrayOutputStream(fullExpectedContent.length / 2);
                    for (int i = skipSize; i < fullExpectedContent.length; i += skipSize + readSize) {
                        expectedContent.write(fullExpectedContent, i, Math.min(skipSize, fullExpectedContent.length - i));
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedContent.size());

                    int remaining = fullExpectedContent.length;

                    assertEquals(Math.min(skipSize, remaining), IOUtils.skipAll(inputStream, skipSize));
                    remaining -= skipSize;

                    byte[] buffer = new byte[readSize];
                    int len;
                    while ((len = IOUtils.readAll(inputStream, buffer)) != -1) {
                        baos.write(buffer, 0, len);
                        remaining -= readSize;

                        if (remaining > 0) {
                            assertEquals(Math.min(skipSize, remaining), IOUtils.skipAll(inputStream, skipSize));
                            remaining -= skipSize;
                        }
                    }
                    assertArrayEquals(expectedContent.toByteArray(), baos.toByteArray());
                }
            });
        }

        @Test
        @DisplayName("skip(long) with a zero index")
        default void testSkipWithZeroIndex() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] expectedContent = expectedContent();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedContent.length);

                    assertEquals(0, inputStream.skip(0));

                    byte[] buffer = new byte[10];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                        assertEquals(0, inputStream.skip(0));
                    }
                    assertEquals(0, inputStream.skip(0));

                    // assert that the skips did not alter the input stream's state
                    assertArrayEquals(expectedContent, baos.toByteArray());
                }
            });
        }

        @Test
        @DisplayName("skip(long) with a negative index")
        default void testSkipWithNegativeIndex() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] expectedContent = expectedContent();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedContent.length);

                    boolean allowNegativeSkip = allowNegativeSkip();

                    assertNegativeSkip(inputStream, allowNegativeSkip);

                    byte[] buffer = new byte[10];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                        assertNegativeSkip(inputStream, allowNegativeSkip);
                    }
                    assertNegativeSkip(inputStream, allowNegativeSkip);

                    // assert that the skips did not alter the input stream's state
                    assertArrayEquals(expectedContent, baos.toByteArray());
                }
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#available()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("available()")
    interface AvailableTests extends InputStreamTests {

        @Test
        @DisplayName("available()")
        default void testAvailable() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    byte[] expectedContent = expectedContent();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedContent.length);

                    assertThat(inputStream.available(), greaterThan(0));
                    byte[] buffer = new byte[10];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                        if (baos.size() < expectedContent.length) {
                            assertThat(inputStream.available(), greaterThan(0));
                        } else {
                            assertEquals(0, inputStream.available());
                        }
                    }
                    assertEquals(0, inputStream.available());
                }
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#mark(int)} and {@link InputStream#reset()}.
     * Note that {@link InputStream#markSupported()} must be supported.
     *
     * @author Rob Spoor
     */
    @DisplayName("mark(int) and reset()")
    interface MarkResetTests extends InputStreamTests {

        @Test
        @DisplayName("markSupported()")
        default void testMarkSupported() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    assertTrue(inputStream.markSupported());
                }
            });
        }

        @Test
        @DisplayName("mark(int) and reset()")
        default void testMarkAndReset() {
            assertDoesNotThrowIOException(() -> {
                try (InputStream inputStream = createInputStream()) {
                    // mark, read 10, reset, read 10, repeat
                    final int readSize = 10;

                    byte[] fullExpectedContent = expectedContent();
                    ByteArrayOutputStream expectedContent = new ByteArrayOutputStream(fullExpectedContent.length * 3 / 2);
                    for (int i = 0; i < fullExpectedContent.length; i += readSize * 2) {
                        expectedContent.write(fullExpectedContent, i, Math.min(readSize, fullExpectedContent.length - i));
                        expectedContent.write(fullExpectedContent, i, Math.min(readSize * 2, fullExpectedContent.length - i));
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedContent.size());

                    byte[] markedBuffer = new byte[readSize];
                    byte[] buffer = new byte[readSize * 2];
                    int len;
                    inputStream.mark(readSize);
                    while ((len = IOUtils.readAll(inputStream, markedBuffer)) != -1) {
                        baos.write(markedBuffer, 0, len);
                        inputStream.reset();

                        len = IOUtils.readAll(inputStream, buffer);
                        if (len != -1) {
                            baos.write(buffer, 0, len);
                            inputStream.mark(readSize);
                        }
                    }
                    assertArrayEquals(expectedContent.toByteArray(), baos.toByteArray());
                }
            });
        }
    }
}
