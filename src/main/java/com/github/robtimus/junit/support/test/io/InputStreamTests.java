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

package com.github.robtimus.junit.support.test.io;

import static com.github.robtimus.junit.support.IOAssertions.assertContainsContent;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertDoesNotThrowCheckedException;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertThrowsOneOf;
import static com.github.robtimus.junit.support.test.io.StreamAssertions.assertNegativeSkip;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing separate {@link InputStream} functionalities.
 *
 * @author Rob Spoor
 */
public interface InputStreamTests {

    /**
     * Returns the input stream to test.
     * <p>
     * This method will be called only once for each test. This makes it possible to initialize the input stream in a method annotated with
     * {@link BeforeEach}, and perform additional tests after the pre-defined test has finished.
     *
     * @return The input stream to test.
     */
    InputStream inputStream();

    /**
     * Returns the expected content from {@link #inputStream() created input streams}.
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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expected = expectedContent();
                    int bufferSize = 10;

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expected.length);

                    byte[] buffer = new byte[bufferSize];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        // read must block until data is available, EOF or IOException
                        assertNotEquals(0, len);
                        baos.write(buffer, 0, len);
                    }
                    assertArrayEquals(expected, baos.toByteArray());
                }
            });
        }

        @Test
        @DisplayName("read(byte[]) with an empty array")
        default void testReadIntoByteArrayWithEmptyArray() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expected = expectedContent();
                    int bufferSize = 10;

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expected.length);

                    byte[] buffer = new byte[bufferSize + 10];
                    int off = 5;
                    int len;
                    while ((len = inputStream.read(buffer, off, bufferSize)) != -1) {
                        // read must block until data is available, EOF or IOException
                        assertNotEquals(0, len);
                        baos.write(buffer, off, len);
                    }
                    assertArrayEquals(expected, baos.toByteArray());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with 0 length")
        default void testReadIntoByteArrayPortionWithZeroLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] buffer = new byte[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> inputStream.read(buffer, -1, 10));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with an offset that exceeds the array length")
        default void testReadIntoByteArrayPortionWithTooHighOffset() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] buffer = new byte[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> inputStream.read(buffer, buffer.length + 1, 0));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with a negative length")
        default void testReadIntoByteArrayPortionWithNegativeLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] buffer = new byte[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> inputStream.read(buffer, 5, -1));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with a length that exceeds the array length")
        default void testReadIntoByteArrayPortionWithTooHighLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] buffer = new byte[10];
                    // don't use 0 and 11, use 1 and 10
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> inputStream.read(buffer, 1, buffer.length));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#readAllBytes()}.
     *
     * @author Rob Spoor
     * @since 3.0
     */
    @DisplayName("readAllBytes()")
    interface ReadAllBytesTests extends InputStreamTests {

        @Test
        @DisplayName("readAllBytes() from the start")
        default void testReadAllBytes() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expected = expectedContent();

                    byte[] content = inputStream.readAllBytes();

                    assertArrayEquals(expected, content);
                }
            });
        }

        @Test
        @DisplayName("readAllBytes() after having read 10 bytes")
        default void testReadAllBytesAfterReading10Bytes() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expected = expectedContent();
                    expected = expected.length < 10 ? new byte[0] : Arrays.copyOfRange(expected, 10, expected.length);

                    assertEquals(Math.min(10, expected.length), inputStream.read(new byte[10]));

                    byte[] content = inputStream.readAllBytes();

                    assertArrayEquals(expected, content);
                }
            });
        }

        @Test
        @DisplayName("readAllBytes() after everything has already been consumed")
        default void testReadAllBytesAfterConsumingStream() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    assertContainsContent(inputStream, expectedContent());

                    byte[] content = inputStream.readAllBytes();

                    assertArrayEquals(new byte[0], content);
                }
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#readNBytes(int)}.
     *
     * @author Rob Spoor
     * @since 3.0
     */
    @DisplayName("readNBytes(int)")
    interface ReadNBytesTests extends InputStreamTests {

        @Test
        @DisplayName("readNBytes(int) with expected length")
        default void testReadNBytesWithExpectedLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expected = expectedContent();

                    byte[] content = inputStream.readNBytes(expected.length);

                    assertArrayEquals(expected, content);

                    // assert everything was read
                    assertEquals(-1, inputStream.read());
                }
            });
        }

        @Test
        @DisplayName("readNBytes(int) with smaller length")
        default void testReadNBytesWithSmallerLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expected = expectedContent();

                    byte[] content = inputStream.readNBytes(expected.length / 2);

                    assertArrayEquals(Arrays.copyOfRange(expected, 0, expected.length / 2), content);

                    // assert that the read didn't read more then expected
                    assertContainsContent(inputStream, Arrays.copyOfRange(expected, expected.length / 2, expected.length));
                }
            });
        }

        @Test
        @DisplayName("readNBytes(int) with 0 length")
        default void testReadNBytesWithZeroLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] content = inputStream.readNBytes(0);

                    assertArrayEquals(new byte[0], content);

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("readNBytes(byte[], int, int) with a negative length")
        default void testReadNBytesWithNegativeLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> inputStream.readNBytes(-1));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("readNBytes(int) with a length that exceeds the content length")
        default void testReadNBytesWithLengthExceedingContentLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expected = expectedContent();

                    byte[] content = inputStream.readNBytes(expected.length);

                    assertArrayEquals(expected, content);

                    // assert everything was read
                    assertEquals(-1, inputStream.read());
                }
            });
        }

        @Test
        @DisplayName("readNBytes(int) after everything has already been consumed")
        default void testReadNBytesAfterConsumingStream() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    assertContainsContent(inputStream, expectedContent());

                    byte[] content = inputStream.readNBytes(1);

                    assertArrayEquals(new byte[0], content);
                }
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#readNBytes(byte[], int, int)}.
     *
     * @author Rob Spoor
     * @since 3.0
     */
    @DisplayName("readNBytes(byte[], int, int)")
    interface ReadNBytesIntoByteArrayPortionTests extends InputStreamTests {

        @Test
        @DisplayName("readNBytes(byte[], int, int)")
        default void testReadNBytesIntoByteArrayPortion() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expected = expectedContent();
                    int bufferSize = 10;

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expected.length);

                    byte[] buffer = new byte[bufferSize + 10];
                    int off = 5;
                    int len;
                    int remaining = expected.length;
                    while ((len = inputStream.readNBytes(buffer, off, bufferSize)) != 0) {
                        assertEquals(remaining < bufferSize ? remaining : bufferSize, len);
                        remaining -= len;
                        baos.write(buffer, off, len);
                    }
                    assertEquals(-1, inputStream.read());
                    assertArrayEquals(expected, baos.toByteArray());
                }
            });
        }

        @Test
        @DisplayName("readNBytes(byte[], int, int) with 0 length")
        default void testReadNBytesIntoByteArrayPortionWithZeroLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] buffer = new byte[10];
                    assertEquals(0, inputStream.readNBytes(buffer, 5, 0));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("readNBytes(byte[], int, int) with a null array")
        default void testReadNBytesIntoByteArrayPortionWithNullArray() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] buffer = null;
                    assertThrows(NullPointerException.class, () -> inputStream.readNBytes(buffer, 0, 10));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("readNBytes(byte[], int, int) with a negative offset")
        default void testReadNBytesIntoByteArrayPortionWithNegativeOffset() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] buffer = new byte[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> inputStream.readNBytes(buffer, -1, 10));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("readNBytes(byte[], int, int) with an offset that exceeds the array length")
        default void testReadNBytesIntoByteArrayPortionWithTooHighOffset() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] buffer = new byte[10];
                    assertThrows(IndexOutOfBoundsException.class, () -> inputStream.readNBytes(buffer, buffer.length + 1, 0));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("readNBytes(byte[], int, int) with a negative length")
        default void testReadNBytesIntoByteArrayPortionWithNegativeLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] buffer = new byte[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> inputStream.readNBytes(buffer, 5, -1));

                    // assert that the read did not alter the input stream's state
                    assertContainsContent(inputStream, expectedContent());
                }
            });
        }

        @Test
        @DisplayName("read(byte[], int, int) with a length that exceeds the array length")
        default void testReadNBytesIntoByteArrayPortionWithTooHighLength() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] buffer = new byte[10];
                    // don't use 0 and 11, use 1 and 10
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> inputStream.readNBytes(buffer, 1, buffer.length));

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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
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
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    assertTrue(inputStream.markSupported());
                }
            });
        }

        @Test
        @DisplayName("mark(int) and reset()")
        default void testMarkAndReset() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    // mark, read 10, reset, read 20, repeat
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

        @Test
        @DisplayName("reset() without mark(int)")
        default void testResetWithoutMark() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expectedContent = expectedContent();

                    if (hasDefaultMark()) {
                        int duplicateCount = Math.min(expectedContent.length, 10);
                        byte[] expectedContentWithDuplicateBytes = new byte[duplicateCount + expectedContent.length];
                        System.arraycopy(expectedContent, 0, expectedContentWithDuplicateBytes, 0, duplicateCount);
                        System.arraycopy(expectedContent, 0, expectedContentWithDuplicateBytes, duplicateCount, expectedContent.length);
                        expectedContent = expectedContentWithDuplicateBytes;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedContent.length);

                    byte[] buffer = new byte[10];
                    int len = IOUtils.readAll(inputStream, buffer);
                    if (len != -1) {
                        baos.write(buffer, 0, len);
                    }
                    if (hasDefaultMark()) {
                        assertDoesNotThrow(inputStream::reset);
                    } else {
                        assertThrows(IOException.class, inputStream::reset);
                    }

                    while ((len = IOUtils.readAll(inputStream, buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }

                    assertArrayEquals(expectedContent, baos.toByteArray());
                }
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#transferTo(OutputStream)}.
     *
     * @author Rob Spoor
     * @since 3.0
     */
    @DisplayName("transferTo(OutputStream)")
    interface TransferToTests extends InputStreamTests {

        @Test
        @DisplayName("transferTo(OutputStream) from the start")
        default void testTransferToReadAllBytes() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expected = expectedContent();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    assertEquals(expected.length, inputStream.transferTo(baos));

                    assertArrayEquals(expected, baos.toByteArray());
                }
            });
        }

        @Test
        @DisplayName("transferTo(OutputStream) after having read 10 bytes")
        default void testTransferToAfterReading10Bytes() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    byte[] expected = expectedContent();
                    expected = expected.length < 10 ? new byte[0] : Arrays.copyOfRange(expected, 10, expected.length);

                    assertEquals(Math.min(10, expected.length), inputStream.read(new byte[10]));

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    assertEquals(expected.length, inputStream.transferTo(baos));

                    assertArrayEquals(expected, baos.toByteArray());
                }
            });
        }

        @Test
        @DisplayName("transferTo(OutputStream) after everything has already been consumed")
        default void testTransferToAfterConsumingStream() {
            assertDoesNotThrowCheckedException(() -> {
                try (InputStream inputStream = inputStream()) {
                    assertContainsContent(inputStream, expectedContent());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    assertEquals(0, inputStream.transferTo(baos));

                    assertArrayEquals(new byte[0], baos.toByteArray());
                }
            });
        }
    }
}
