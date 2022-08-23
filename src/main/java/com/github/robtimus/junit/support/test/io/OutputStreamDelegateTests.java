/*
 * OutputStreamDelegateTests.java
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

import static com.github.robtimus.junit.support.ThrowableAssertions.assertDoesNotThrowCheckedException;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertThrowsOneOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing that {@link OutputStream} implementations correctly delegate to another {@link OutputStream}.
 *
 * @author Rob Spoor
 */
public interface OutputStreamDelegateTests {

    /**
     * Creates the output stream to test.
     * <p>
     * This method will be called only once for each test. This makes it possible to capture the output stream to test and its delegate, and perform
     * additional tests after the pre-defined test has finished.
     *
     * @param delegate The delegate to test against.
     * @return The created output stream.
     */
    OutputStream wrapOutputStream(OutputStream delegate);

    /**
     * Returns the expected content written to a delegate, based on the content that was written to a
     * {@link #wrapOutputStream(OutputStream) created output stream}.
     *
     * @param written The content that was written.
     * @return The expected content.
     */
    byte[] expectedContent(byte[] written);

    /**
     * Returns the content to write for most tests. This default implementation returns a lorem ipsum text.
     *
     * @return The content to write.
     */
    default byte[] contentToWrite() {
        return "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa.".getBytes(); //$NON-NLS-1$
    }

    /**
     * Returns the content to write when testing writing large chunks. this default implementation returns {@link #contentToWrite()}
     * concatenated {@code 1000} times.
     *
     * @return The content to write when testing writing large pieces of text.
     */
    default byte[] longContentToWrite() {
        byte[] content = contentToWrite();
        byte[] result = new byte[1000 * content.length];
        for (int i = 0; i < 1000; i++) {
            System.arraycopy(content, 0, result, i * content.length, content.length);
        }
        return result;
    }

    /**
     * Contains tests for {@link OutputStream#write(int)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("write(int)")
    interface WriteByteTests extends OutputStreamDelegateTests {

        @Test
        @DisplayName("write(int)")
        default void testWriteByte() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] content = contentToWrite();
                byte[] expectedContent = expectedContent(content);

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    for (byte b : content) {
                        outputStream.write(b);
                    }
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }
    }

    /**
     * Contains tests for {@link OutputStream#write(byte[])}.
     *
     * @author Rob Spoor
     */
    @DisplayName("write(byte[])")
    interface WriteByteArrayTests extends OutputStreamDelegateTests {

        @Test
        @DisplayName("write(byte[])")
        default void testWriteByteArray() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] content = contentToWrite();
                byte[] expectedContent = expectedContent(content);

                int bufferSize = 10;

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    for (int i = 0; i < content.length; i += bufferSize) {
                        outputStream.write(Arrays.copyOfRange(content, i, Math.min(i + bufferSize, content.length)));
                    }
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }

        @Test
        @DisplayName("write(byte[]) with a large array")
        default void testWriteByteArrayWithLargeArray() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] content = longContentToWrite();
                byte[] expectedContent = expectedContent(content);

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    outputStream.write(content);
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }

        @Test
        @DisplayName("write(byte[]) with an empty array")
        default void testWriteByteArrayWithEmptyArray() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] content = {};
                byte[] expectedContent = expectedContent(content);

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    outputStream.write(content);
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }

        @Test
        @DisplayName("write(byte[]) with a null array")
        default void testWriteByteArrayWithNullArray() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] expectedContent = {};

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    byte[] buffer = null;
                    assertThrows(NullPointerException.class, () -> outputStream.write(buffer));
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }
    }

    /**
     * Contains tests for {@link OutputStream#write(byte[], int, int)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("write(byte[], int, int)")
    interface WriteByteArrayPortionTests extends OutputStreamDelegateTests {

        @Test
        @DisplayName("write(byte[], int, int)")
        default void testWriteByteArrayPortion() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] content = contentToWrite();
                byte[] expectedContent = expectedContent(content);

                int bufferSize = 10;

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    for (int i = 0; i < content.length; i += bufferSize) {
                        outputStream.write(content, i, Math.min(bufferSize, content.length - i));
                    }
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }

        @Test
        @DisplayName("write(byte[], int, int) with a large array")
        default void testWriteByteArrayPortionWithLargeArray() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] content = longContentToWrite();
                byte[] expectedContent = expectedContent(content);

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    outputStream.write(content, 0, content.length);
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }

        @Test
        @DisplayName("write(byte[], int, int) with a null array")
        default void testWriteByteArrayPortionWithNullArray() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] expectedContent = {};

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    byte[] buffer = null;
                    assertThrows(NullPointerException.class, () -> outputStream.write(buffer, 0, 10));
                }
                // assert that nothing was written
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }

        @Test
        @DisplayName("write(byte[], int, int) with 0 length")
        default void testWriteByteArrayPortionWithZeroLength() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] content = contentToWrite();
                byte[] expectedContent = expectedContent(new byte[0]);

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    outputStream.write(content, 5, 0);
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }

        @Test
        @DisplayName("write(byte[], int, int) with a negative offset")
        default void testWriteByteArrayPortionWithNegativeOffset() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] expectedContent = {};

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    byte[] buffer = new byte[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> outputStream.write(buffer, -1, 10));
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }

        @Test
        @DisplayName("write(byte[], int, int) with an offset that exceeds the array length")
        default void testWriteByteArrayPortionWithTooHighOffset() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] expectedContent = {};

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    byte[] buffer = new byte[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> outputStream.write(buffer, buffer.length + 1, 0));
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }

        @Test
        @DisplayName("write(byte[], int, int) with a negative length")
        default void testWriteByteArrayPortionWithNegativeLength() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] expectedContent = {};

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    byte[] buffer = new byte[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> outputStream.write(buffer, 5, -1));
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }

        @Test
        @DisplayName("write(byte[], int, int) with a length that exceeds the array length")
        default void testWriteByteArrayPortionWithTooHighLength() {
            assertDoesNotThrowCheckedException(() -> {
                byte[] expectedContent = {};

                ByteArrayOutputStream delegate = new ByteArrayOutputStream(expectedContent.length);

                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    byte[] buffer = new byte[10];
                    // don't use 0 and 11, use 1 and 10, so it's not the value of the length that triggers the error but the combination off + len
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> outputStream.write(buffer, 1, buffer.length));
                }
                assertArrayEquals(expectedContent, delegate.toByteArray());
            });
        }
    }

    /**
     * Contains tests for {@link OutputStream#flush()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("flush()")
    interface FlushTests extends OutputStreamDelegateTests {

        @Test
        @DisplayName("flush() delegates")
        @SuppressWarnings("resource")
        default void testFlushDelegates() {
            assertDoesNotThrowCheckedException(() -> {
                OutputStream delegate = mock(OutputStream.class);
                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    outputStream.flush();
                    verify(delegate).flush();
                }
            });
        }
    }

    /**
     * Contains tests for {@link OutputStream#close()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("close()")
    interface CloseTests extends OutputStreamDelegateTests {

        @Test
        @DisplayName("close() delegates")
        default void testCloseDelegates() {
            assertDoesNotThrowCheckedException(() -> {
                @SuppressWarnings("resource")
                OutputStream delegate = mock(OutputStream.class);
                try (OutputStream outputStream = wrapOutputStream(delegate)) {
                    // no code necessary
                }
                verify(delegate).close();
            });
        }
    }
}
