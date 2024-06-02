/*
 * TestResourceLoadersTest.java
 * Copyright 2022 Rob Spoor
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

package com.github.robtimus.junit.support.extension.testresource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.apache.commons.io.input.BrokenInputStream;
import org.apache.commons.io.input.BrokenReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class TestResourceLoadersTest {

    @Test
    @DisplayName("toString(Reader)")
    void testToString() throws IOException {
        String original = "key1=value1\nkey2=value2\n";

        StringReader reader = new StringReader(original);

        String content = TestResourceLoaders.toString(reader);

        assertEquals(original, content);
    }

    @Nested
    @DisplayName("toString(Reader, String)")
    class ToStringWithLineSeparator {

        @Test
        @DisplayName("ending in with line separator")
        void testEndingWithLineSeparator() throws IOException {
            String original = "line1\r\rline2\r\n\nline3\n";

            StringReader reader = new StringReader(original);

            String content = TestResourceLoaders.toString(reader, "\r\n");

            assertEquals("line1\r\n\r\nline2\r\n\r\nline3\r\n", content);
        }

        @Test
        @DisplayName("not ending in with line separator")
        void testNotEndingWithLineSeparator() throws IOException {
            String original = "line1\r\rline2\r\n\nline3";

            StringReader reader = new StringReader(original);

            String content = TestResourceLoaders.toString(reader, "\r\n");

            assertEquals("line1\r\n\r\nline2\r\n\r\nline3", content);
        }
    }

    @Test
    @DisplayName("toStringBuilder(Reader)")
    void testToStringBuilder() throws IOException {
        String original = "key1=value1\nkey2=value2\n";

        StringReader reader = new StringReader(original);

        StringBuilder content = TestResourceLoaders.toStringBuilder(reader);

        assertEquals(original, content.toString());
    }

    @Nested
    @DisplayName("toStringBuilder(Reader, String)")
    class ToStringBuilderWithLineSeparator {

        @Test
        @DisplayName("ending in with line separator")
        void testEndingWithLineSeparator() throws IOException {
            String original = "line1\r\rline2\r\n\nline3\n";

            StringReader reader = new StringReader(original);

            StringBuilder content = TestResourceLoaders.toStringBuilder(reader, "\r\n");

            assertEquals("line1\r\n\r\nline2\r\n\r\nline3\r\n", content.toString());
        }

        @Test
        @DisplayName("not ending in with line separator")
        void testNotEndingWithLineSeparator() throws IOException {
            String original = "line1\r\rline2\r\n\nline3";

            StringReader reader = new StringReader(original);

            StringBuilder content = TestResourceLoaders.toStringBuilder(reader, "\r\n");

            assertEquals("line1\r\n\r\nline2\r\n\r\nline3", content.toString());
        }
    }

    @Test
    @DisplayName("toCharSequence(Reader)")
    void testToCharSequence() throws IOException {
        String original = "key1=value1\nkey2=value2\n";

        StringReader reader = new StringReader(original);

        CharSequence content = TestResourceLoaders.toCharSequence(reader);

        assertEquals(original, content.toString());
    }

    @Nested
    @DisplayName("toCharSequence(Reader, String)")
    class ToCharSequenceWithLineSeparator {

        @Test
        @DisplayName("ending in with line separator")
        void testEndingWithLineSeparator() throws IOException {
            String original = "line1\r\rline2\r\n\nline3\n";

            StringReader reader = new StringReader(original);

            CharSequence content = TestResourceLoaders.toCharSequence(reader, "\r\n");

            assertEquals("line1\r\n\r\nline2\r\n\r\nline3\r\n", content.toString());
        }

        @Test
        @DisplayName("not ending in with line separator")
        void testNotEndingWithLineSeparator() throws IOException {
            String original = "line1\r\rline2\r\n\nline3";

            StringReader reader = new StringReader(original);

            CharSequence content = TestResourceLoaders.toCharSequence(reader, "\r\n");

            assertEquals("line1\r\n\r\nline2\r\n\r\nline3", content.toString());
        }
    }

    @Test
    @DisplayName("toBytes(InputStream)")
    void testToBytes() throws IOException {
        byte[] original = new byte[1024];
        new Random().nextBytes(original);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(original);

        byte[] content = TestResourceLoaders.toBytes(inputStream);

        assertArrayEquals(original, content);
    }

    @Test
    @DisplayName("toLines(Reader)")
    void testToLines() throws IOException {
        StringReader reader = new StringReader("key1=value1\nkey2=value2\n");

        List<String> lines = TestResourceLoaders.toLines(reader);

        List<String> expected = Arrays.asList("key1=value1", "key2=value2");

        assertEquals(expected, lines);
    }

    @Test
    @DisplayName("toLinesArray(Reader)")
    void testToLinesArray() throws IOException {
        StringReader reader = new StringReader("key1=value1\nkey2=value2\n");

        String[] lines = TestResourceLoaders.toLinesArray(reader);

        String[] expected = { "key1=value1", "key2=value2" };

        assertArrayEquals(expected, lines);
    }

    @Test
    @DisplayName("toProperties(Reader)")
    void testToProperties() throws IOException {
        StringReader reader = new StringReader("key1=value1\nkey2=value2\n");

        Properties properties = TestResourceLoaders.toProperties(reader);

        Properties expected = new Properties();
        expected.put("key1", "value1");
        expected.put("key2", "value2");

        assertEquals(expected, properties);
    }

    @Nested
    @DisplayName("error propagation")
    class ErrorPropagation {

        @Test
        @DisplayName("toString(Reader)")
        void testToString() {
            Exception exception = new IOException();

            @SuppressWarnings("resource")
            Reader reader = new BrokenReader(exception);

            IOException thrown = assertThrows(IOException.class, () -> TestResourceLoaders.toString(reader));

            assertSame(exception, thrown);
        }

        @Test
        @DisplayName("toString(Reader, String)")
        void testToStringWithLineSeparator() {
            Exception exception = new IOException();

            @SuppressWarnings("resource")
            Reader reader = new BrokenReader(exception);

            IOException thrown = assertThrows(IOException.class, () -> TestResourceLoaders.toString(reader, "\n"));

            assertSame(exception, thrown);
        }

        @Test
        @DisplayName("toStringBuilder(Reader)")
        void testToStringBuilder() {
            Exception exception = new IOException();

            @SuppressWarnings("resource")
            Reader reader = new BrokenReader(exception);

            IOException thrown = assertThrows(IOException.class, () -> TestResourceLoaders.toStringBuilder(reader));

            assertSame(exception, thrown);
        }

        @Test
        @DisplayName("toStringBuilder(Reader, String)")
        void testToStringBuilderWithLineSeparator() {
            Exception exception = new IOException();

            @SuppressWarnings("resource")
            Reader reader = new BrokenReader(exception);

            IOException thrown = assertThrows(IOException.class, () -> TestResourceLoaders.toStringBuilder(reader, "\n"));

            assertSame(exception, thrown);
        }

        @Test
        @DisplayName("toCharSequence(Reader)")
        void testToCharSequence() {
            Exception exception = new IOException();

            @SuppressWarnings("resource")
            Reader reader = new BrokenReader(exception);

            IOException thrown = assertThrows(IOException.class, () -> TestResourceLoaders.toCharSequence(reader));

            assertSame(exception, thrown);
        }

        @Test
        @DisplayName("toCharSequence(Reader, String)")
        void testToCharSequenceWithLineSeparator() {
            Exception exception = new IOException();

            @SuppressWarnings("resource")
            Reader reader = new BrokenReader(exception);

            IOException thrown = assertThrows(IOException.class, () -> TestResourceLoaders.toCharSequence(reader, "\n"));

            assertSame(exception, thrown);
        }

        @Test
        @DisplayName("toBytes(InputStream)")
        void testToBytes() {
            Exception exception = new IOException();

            @SuppressWarnings("resource")
            InputStream inputStream = new BrokenInputStream(exception);

            IOException thrown = assertThrows(IOException.class, () -> TestResourceLoaders.toBytes(inputStream));

            assertSame(exception, thrown);
        }

        @Test
        @DisplayName("toLines(Reader)")
        void testToLines() {
            Exception exception = new IOException();

            @SuppressWarnings("resource")
            Reader reader = new BrokenReader(exception);

            IOException thrown = assertThrows(IOException.class, () -> TestResourceLoaders.toLines(reader));

            assertSame(exception, thrown);
        }

        @Test
        @DisplayName("toLinesArray(Reader)")
        void testToLinesArray() {
            Exception exception = new IOException();

            @SuppressWarnings("resource")
            Reader reader = new BrokenReader(exception);

            IOException thrown = assertThrows(IOException.class, () -> TestResourceLoaders.toLinesArray(reader));

            assertSame(exception, thrown);
        }

        @Test
        @DisplayName("toProperties(Reader)")
        void testToProperties() {
            Exception exception = new IOException();

            @SuppressWarnings("resource")
            Reader reader = new BrokenReader(exception);

            IOException thrown = assertThrows(IOException.class, () -> TestResourceLoaders.toProperties(reader));

            assertSame(exception, thrown);
        }
    }
}
