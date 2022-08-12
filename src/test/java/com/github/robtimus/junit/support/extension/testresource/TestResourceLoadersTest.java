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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.Random;
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
    @DisplayName("toProperties(Reader)")
    void testToProperties() throws IOException {
        StringReader reader = new StringReader("key1=value1\nkey2=value2\n");

        Properties properties = TestResourceLoaders.toProperties(reader);

        Properties expected = new Properties();
        expected.put("key1", "value1");
        expected.put("key2", "value2");

        assertEquals(expected, properties);
    }
}
