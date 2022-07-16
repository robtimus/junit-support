/*
 * IOAssertionsTest.java
 * Copyright 2021 Rob Spoor
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
import static com.github.robtimus.junit.support.io.IOAssertions.assertNotSerializable;
import static com.github.robtimus.junit.support.io.IOAssertions.assertSerializable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.Reader;
import java.io.StringReader;
import java.util.UUID;
import java.util.function.Supplier;
import org.apache.commons.io.input.BrokenInputStream;
import org.apache.commons.io.input.BrokenReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

@SuppressWarnings("nls")
class IOAssertionsTest {

    @Nested
    @DisplayName("assertContainsContent")
    class AssertContainsContent {

        @Nested
        @DisplayName("Reader contains content")
        class ReaderContainsContent {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                String content = "foo";
                StringReader reader = new StringReader(content);
                assertContainsContent(reader, content);
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                String content = "foo";
                StringReader reader = new StringReader(content);
                assertContainsContent(reader, content, "error");
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                String content = "foo";
                StringReader reader = new StringReader(content);
                Supplier<String> messageSupplier = () -> "error";
                assertContainsContent(reader, content, messageSupplier);
            }
        }

        @Nested
        @DisplayName("Reader does not contain content")
        class ReaderDoesNotContainContent {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                String content = "foo";
                StringReader reader = new StringReader("bar");
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(reader, content));
                assertThat(error.getMessage(), startsWith("expected: <foo>"));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                String content = "foo";
                StringReader reader = new StringReader("bar");
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(reader, content, "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: <foo>"));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                String content = "foo";
                StringReader reader = new StringReader("bar");
                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(reader, content, messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> expected: <foo>"));
            }
        }

        @Nested
        @DisplayName("Reader throws exception")
        class ReaderThrowsException {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                @SuppressWarnings("resource")
                Reader reader = new BrokenReader();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(reader, "foo"));
                assertThat(error.getMessage(), startsWith("Unexpected exception thrown: " + IOException.class.getName()));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                @SuppressWarnings("resource")
                Reader reader = new BrokenReader();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(reader, "foo", "error"));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + IOException.class.getName()));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                @SuppressWarnings("resource")
                Reader reader = new BrokenReader();
                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(reader, "foo", messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + IOException.class.getName()));
            }
        }

        @Nested
        @DisplayName("InputStream contains content")
        class InputStreamContainsContent {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                byte[] content = "foo".getBytes();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
                assertContainsContent(inputStream, content);
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                byte[] content = "foo".getBytes();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
                assertContainsContent(inputStream, content, "error");
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                byte[] content = "foo".getBytes();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
                Supplier<String> messageSupplier = () -> "error";
                assertContainsContent(inputStream, content, messageSupplier);
            }
        }

        @Nested
        @DisplayName("InputStream does not contain content")
        class InputStreamDoesNotContainContent {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                byte[] content = "foo".getBytes();
                ByteArrayInputStream inputStream = new ByteArrayInputStream("bar".getBytes());
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(inputStream, content));
                assertThat(error.getMessage(), startsWith("array contents differ at index [0]"));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                byte[] content = "foo".getBytes();
                ByteArrayInputStream inputStream = new ByteArrayInputStream("bar".getBytes());
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(inputStream, content, "error"));
                assertThat(error.getMessage(), startsWith("error ==> array contents differ at index [0]"));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                byte[] content = "foo".getBytes();
                ByteArrayInputStream inputStream = new ByteArrayInputStream("bar".getBytes());
                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertContainsContent(inputStream, content, messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> array contents differ at index [0]"));
            }
        }

        @Nested
        @DisplayName("InputStream throws exception")
        class InputStreamThrowsException {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                byte[] content = "foo".getBytes();
                @SuppressWarnings("resource")
                InputStream inputStream = new BrokenInputStream();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(inputStream, content));
                assertThat(error.getMessage(), startsWith("Unexpected exception thrown: " + IOException.class.getName()));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                byte[] content = "foo".getBytes();
                @SuppressWarnings("resource")
                InputStream inputStream = new BrokenInputStream();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(inputStream, content, "error"));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + IOException.class.getName()));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                byte[] content = "foo".getBytes();
                @SuppressWarnings("resource")
                InputStream inputStream = new BrokenInputStream();
                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertContainsContent(inputStream, content, messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + IOException.class.getName()));
            }
        }
    }

    @Nested
    @DisplayName("assertSerializable(T)")
    class AssertSerializable {

        @Nested
        @DisplayName("with serializable object")
        class WithSerializableObject {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                String input = UUID.randomUUID().toString();
                String output = assertSerializable(input);
                assertEquals(input, output);
                assertNotSame(input, output);
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                String input = UUID.randomUUID().toString();
                String output = assertSerializable(input, "error");
                assertEquals(input, output);
                assertNotSame(input, output);
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                String input = UUID.randomUUID().toString();
                Supplier<String> messageSupplier = () -> "error";
                String output = assertSerializable(input, messageSupplier);
                assertEquals(input, output);
                assertNotSame(input, output);
            }
        }

        @Nested
        @DisplayName("with non-serializable object")
        class WithNonSerializableObject {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                Object input = new Object();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertSerializable(input));
                assertThat(error.getMessage(), startsWith("Unexpected exception thrown: " + NotSerializableException.class.getName()));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                Object input = new Object();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertSerializable(input, "error"));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + NotSerializableException.class.getName()));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                Supplier<String> messageSupplier = () -> "error";
                Object input = new Object();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertSerializable(input, messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + NotSerializableException.class.getName()));
            }
        }
    }

    @Nested
    @DisplayName("assertNotSerializable(T)")
    class AssertNotSerializable {

        @Nested
        @DisplayName("with serializable object")
        class WithSerializableObject {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                String input = UUID.randomUUID().toString();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertNotSerializable(input));
                assertThat(error.getMessage(), startsWith("Expected " + NotSerializableException.class.getName() + " to be thrown"));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                String input = UUID.randomUUID().toString();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertNotSerializable(input, "error"));
                assertThat(error.getMessage(), startsWith("error ==> Expected " + NotSerializableException.class.getName() + " to be thrown"));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                String input = UUID.randomUUID().toString();
                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertNotSerializable(input, messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> Expected " + NotSerializableException.class.getName() + " to be thrown"));
            }
        }

        @Nested
        @DisplayName("with non-serializable object")
        class WithNonSerializableObject {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                Object input = new Object();
                assertNotSerializable(input);
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                Object input = new Object();
                assertNotSerializable(input, "error");
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                Object input = new Object();
                Supplier<String> messageSupplier = () -> "error";
                assertNotSerializable(input, messageSupplier);
            }
        }
    }
}
