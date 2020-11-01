/*
 * ReaderDelegateTests.java
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
import java.io.Reader;
import java.nio.CharBuffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing that {@link Reader} implementations correctly delegate to another {@link Reader}.
 *
 * @author Rob Spoor
 */
public interface ReaderDelegateTests {

    /**
     * Creates the reader to test.
     *
     * @param delegate The delegate to test against.
     * @return The created reader.
     */
    Reader wrapReader(Reader delegate);

    /**
     * Contains tests for {@link Reader#read(CharBuffer)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read(CharBuffer)")
    interface ReadIntoCharBufferTests extends ReaderDelegateTests {

        @Test
        @DisplayName("read(CharBuffer) delegates")
        @SuppressWarnings("resource")
        default void testReadIntoCharBufferDelegates() {
            assertDoesNotThrowIOException(() -> {
                Reader delegate = mock(Reader.class);
                CharBuffer buffer = CharBuffer.allocate(10);
                int n = 5;
                doReturn(n).when(delegate).read(buffer);
                try (Reader reader = wrapReader(delegate)) {
                    assertEquals(n, reader.read(buffer));
                }
                verify(delegate).read(buffer);
            });
        }
    }

    /**
     * Contains tests for {@link Reader#read()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read()")
    interface ReadCharTests extends ReaderDelegateTests {

        @Test
        @DisplayName("read() delegates")
        @SuppressWarnings("resource")
        default void testReadCharDelegates() {
            assertDoesNotThrowIOException(() -> {
                Reader delegate = mock(Reader.class);
                int c = '\n';
                doReturn(c).when(delegate).read();
                try (Reader reader = wrapReader(delegate)) {
                    assertEquals(c, reader.read());
                }
                verify(delegate).read();
            });
        }
    }

    /**
     * Contains tests for {@link Reader#read(char[])}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read(char[])")
    interface ReadIntoCharArrayTests extends ReaderDelegateTests {

        @Test
        @DisplayName("read(char[]) delegates")
        @SuppressWarnings("resource")
        default void testReadIntoCharArrayDelegates() {
            assertDoesNotThrowIOException(() -> {
                Reader delegate = mock(Reader.class);
                char[] buffer = new char[10];
                int n = 5;
                doReturn(n).when(delegate).read(buffer);
                try (Reader reader = wrapReader(delegate)) {
                    assertEquals(n, reader.read(buffer));
                }
                verify(delegate).read(buffer);
            });
        }
    }

    /**
     * Contains tests for {@link Reader#read(char[], int, int)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("read(char[], int, int)")
    interface ReadIntoCharArrayPortionTests extends ReaderDelegateTests {

        @Test
        @DisplayName("read(char[], int, int) delegates")
        @SuppressWarnings("resource")
        default void testReadIntoCharArrayPortionDelegates() {
            assertDoesNotThrowIOException(() -> {
                Reader delegate = mock(Reader.class);
                char[] buffer = new char[10];
                int n = 5;
                doReturn(n).when(delegate).read(buffer, 0, buffer.length);
                try (Reader reader = wrapReader(delegate)) {
                    assertEquals(n, reader.read(buffer, 0, buffer.length));
                }
                verify(delegate).read(buffer, 0, buffer.length);
            });
        }
    }

    /**
     * Contains tests for {@link Reader#skip(long)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("skip(long)")
    interface SkipTests extends ReaderDelegateTests {

        @Test
        @DisplayName("skip(long) delegates")
        @SuppressWarnings("resource")
        default void testSkipDelegates() {
            assertDoesNotThrowIOException(() -> {
                Reader delegate = mock(Reader.class);
                long n = 5;
                doReturn(n).when(delegate).skip(n);
                try (Reader reader = wrapReader(delegate)) {
                    assertEquals(n, reader.skip(n));
                }
                verify(delegate).skip(n);
            });
        }
    }

    /**
     * Contains tests for {@link Reader#ready()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("ready()")
    interface ReadyTests extends ReaderDelegateTests {

        @Test
        @DisplayName("ready() delegates")
        @SuppressWarnings("resource")
        default void testReadyDelegates() {
            assertDoesNotThrowIOException(() -> {
                Reader delegate = mock(Reader.class);
                doReturn(true).when(delegate).ready();
                try (Reader reader = wrapReader(delegate)) {
                    assertTrue(reader.ready());
                }
                verify(delegate).ready();
            });
        }
    }

    /**
     * Contains tests for {@link Reader#markSupported()}, {@link Reader#mark(int)} and {@link Reader#reset()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("mark(int) and reset()")
    interface MarkResetTests extends ReaderDelegateTests {

        @Test
        @DisplayName("markSupported() delegates")
        @SuppressWarnings("resource")
        default void testMarkSupportedDelegates() {
            assertDoesNotThrowIOException(() -> {
                Reader delegate = mock(Reader.class);
                doReturn(true).when(delegate).markSupported();
                try (Reader reader = wrapReader(delegate)) {
                    assertTrue(reader.markSupported());
                }
                verify(delegate).markSupported();
            });
        }

        @Test
        @DisplayName("mark(int) and reset() delegate")
        @SuppressWarnings("resource")
        default void testMarkAndResetDelegate() {
            assertDoesNotThrowIOException(() -> {
                Reader delegate = mock(Reader.class);
                int readAheadLimit = 10;
                try (Reader reader = wrapReader(delegate)) {
                    reader.mark(readAheadLimit);
                    reader.reset();
                }
                verify(delegate).mark(readAheadLimit);
                verify(delegate).reset();
            });
        }
    }

    /**
     * Contains tests for {@link Reader#close()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("close()")
    interface CloseTests extends ReaderDelegateTests {

        @Test
        @DisplayName("close() delegates")
        default void testCloseDelegates() {
            assertDoesNotThrowIOException(() -> {
                @SuppressWarnings("resource")
                Reader delegate = mock(Reader.class);
                try (Reader reader = wrapReader(delegate)) {
                    // no code necessary
                }
                verify(delegate).close();
            });
        }
    }
}
