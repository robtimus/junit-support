/*
 * InputStreamDelegateTests.java
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

import static com.github.robtimus.junit.support.io.IOAssertions.assertDoesNotThrowIOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing that {@link InputStream} implementations correctly delegate to another {@link InputStream}.
 *
 * @author Rob Spoor
 */
public interface InputStreamDelegateTests {

    /**
     * Creates the input stream to test.
     *
     * @param delegate The delegate to test against.
     * @return The created input stream.
     */
    InputStream wrapInputStream(InputStream delegate);

    /**
     * Contains tests for {@link InputStream#read()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read()")
    interface ReadByteTests extends InputStreamDelegateTests {

        @Test
        @DisplayName("read() delegates")
        @SuppressWarnings("resource")
        default void testReadByteDelegates() {
            assertDoesNotThrowIOException(() -> {
                InputStream delegate = mock(InputStream.class);
                int b = '\n';
                doReturn(b).when(delegate).read();
                try (InputStream inputStream = wrapInputStream(delegate)) {
                    assertEquals(b, inputStream.read());
                }
                verify(delegate).read();
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#read(byte[])}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read(byte[])")
    interface ReadIntoByteArrayTests extends InputStreamDelegateTests {

        @Test
        @DisplayName("read(byte[]) delegates")
        @SuppressWarnings("resource")
        default void testReadIntoByteArrayDelegates() {
            assertDoesNotThrowIOException(() -> {
                InputStream delegate = mock(InputStream.class);
                byte[] buffer = new byte[10];
                int n = 5;
                doReturn(n).when(delegate).read(buffer);
                try (InputStream inputStream = wrapInputStream(delegate)) {
                    assertEquals(n, inputStream.read(buffer));
                }
                verify(delegate).read(buffer);
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#read(byte[], int, int)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read(byte[], int, int)")
    interface ReadIntoByteArrayPortionTests extends InputStreamDelegateTests {

        @Test
        @DisplayName("read(byte[], int, int) delegates")
        @SuppressWarnings("resource")
        default void testReadIntoByteArrayPortionDelegates() {
            assertDoesNotThrowIOException(() -> {
                InputStream delegate = mock(InputStream.class);
                byte[] buffer = new byte[10];
                int n = 5;
                doReturn(n).when(delegate).read(buffer, 0, buffer.length);
                try (InputStream inputStream = wrapInputStream(delegate)) {
                    assertEquals(n, inputStream.read(buffer, 0, buffer.length));
                }
                verify(delegate).read(buffer, 0, buffer.length);
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#skip(long)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("skip(long)")
    interface SkipTests extends InputStreamDelegateTests {

        @Test
        @DisplayName("skip(long) delegates")
        @SuppressWarnings("resource")
        default void testSkipDelegates() {
            assertDoesNotThrowIOException(() -> {
                InputStream delegate = mock(InputStream.class);
                long n = 5;
                doReturn(n).when(delegate).skip(n);
                try (InputStream inputStream = wrapInputStream(delegate)) {
                    assertEquals(n, inputStream.skip(n));
                }
                verify(delegate).skip(n);
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#available()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("available()")
    interface AvailableTests extends InputStreamDelegateTests {

        @Test
        @DisplayName("available() delegates")
        @SuppressWarnings("resource")
        default void testAvailableDelegates() {
            assertDoesNotThrowIOException(() -> {
                InputStream delegate = mock(InputStream.class);
                int n = 5;
                doReturn(n).when(delegate).available();
                try (InputStream inputStream = wrapInputStream(delegate)) {
                    assertEquals(n, inputStream.available());
                }
                verify(delegate).available();
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#close()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("close()")
    interface CloseTests extends InputStreamDelegateTests {

        @Test
        @DisplayName("close() delegates")
        default void testCloseDelegates() {
            assertDoesNotThrowIOException(() -> {
                @SuppressWarnings("resource")
                InputStream delegate = mock(InputStream.class);
                try (InputStream inputStream = wrapInputStream(delegate)) {
                    // no code necessary
                }
                verify(delegate).close();
            });
        }
    }

    /**
     * Contains tests for {@link InputStream#markSupported()}, {@link InputStream#mark(int)} and {@link InputStream#reset()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("mark(int) and reset()")
    interface MarkResetTests extends InputStreamDelegateTests {

        @Test
        @DisplayName("markSupported() delegates")
        @SuppressWarnings("resource")
        default void testMarkSupportedDelegates() {
            assertDoesNotThrowIOException(() -> {
                InputStream delegate = mock(InputStream.class);
                doReturn(true).when(delegate).markSupported();
                try (InputStream inputStream = wrapInputStream(delegate)) {
                    assertTrue(inputStream.markSupported());
                }
                verify(delegate).markSupported();
            });
        }

        @Test
        @DisplayName("mark(int) and reset() delegate")
        @SuppressWarnings("resource")
        default void testMarkAndResetDelegate() {
            assertDoesNotThrowIOException(() -> {
                InputStream delegate = mock(InputStream.class);
                int readLimit = 10;
                try (InputStream inputStream = wrapInputStream(delegate)) {
                    inputStream.mark(readLimit);
                    inputStream.reset();
                }
                verify(delegate).mark(readLimit);
                verify(delegate).reset();
            });
        }
    }
}
