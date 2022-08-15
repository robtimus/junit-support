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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;
import org.apache.commons.io.input.BrokenInputStream;
import org.apache.commons.io.input.BrokenReader;
import org.hamcrest.Matcher;
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

            @Test
            @DisplayName("with matcher")
            void testWithMatcher() {
                String content = "foobar";
                StringReader reader = new StringReader(content);
                Matcher<String> matcher = startsWith("foo");
                assertContainsContent(reader, matcher);
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

            @Test
            @DisplayName("with matcher")
            void testWithMatcher() {
                String content = "foobar";
                StringReader reader = new StringReader(content);
                Matcher<String> matcher = startsWith("bar");
                AssertionError error = assertThrows(AssertionError.class, () -> assertContainsContent(reader, matcher));
                assertThat(error.getMessage(), containsString("Expected: a string starting with \"bar\""));
                assertThat(error.getMessage(), containsString("but: was \"foobar\""));
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

            @Test
            @DisplayName("with matcher")
            void testWithMatcher() {
                @SuppressWarnings("resource")
                Reader reader = new BrokenReader();
                Matcher<String> matcher = startsWith("foo");
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(reader, matcher));
                assertThat(error.getMessage(), startsWith("Unexpected exception thrown: " + IOException.class.getName()));
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

        @Nested
        @DisplayName("InputStream contains string content")
        class InputStreamContainsStringContent {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                String content = "foo";
                Charset charset = StandardCharsets.UTF_8;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(charset));
                assertContainsContent(inputStream, charset, content);
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                String content = "foo";
                Charset charset = StandardCharsets.UTF_8;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(charset));
                assertContainsContent(inputStream, charset, content, "error");
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                String content = "foo";
                Charset charset = StandardCharsets.UTF_8;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(charset));
                Supplier<String> messageSupplier = () -> "error";
                assertContainsContent(inputStream, charset, content, messageSupplier);
            }

            @Test
            @DisplayName("with matcher")
            void testWithMatcher() {
                String content = "foobar";
                Charset charset = StandardCharsets.UTF_8;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(charset));
                Matcher<String> matcher = startsWith("foo");
                assertContainsContent(inputStream, charset, matcher);
            }
        }

        @Nested
        @DisplayName("InputStream does not contain string content")
        class InputStreamDoesNotContainStringContent {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                String content = "foo";
                Charset charset = StandardCharsets.UTF_8;
                ByteArrayInputStream inputStream = new ByteArrayInputStream("bar".getBytes(charset));
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(inputStream, charset, content));
                assertThat(error.getMessage(), startsWith("expected: <foo> but was: <bar>"));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                String content = "foo";
                Charset charset = StandardCharsets.UTF_8;
                ByteArrayInputStream inputStream = new ByteArrayInputStream("bar".getBytes(charset));
                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertContainsContent(inputStream, charset, content, "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: <foo> but was: <bar>"));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                String content = "foo";
                Charset charset = StandardCharsets.UTF_8;
                ByteArrayInputStream inputStream = new ByteArrayInputStream("bar".getBytes(charset));
                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertContainsContent(inputStream, charset, content, messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> expected: <foo> but was: <bar>"));
            }

            @Test
            @DisplayName("with matcher")
            void testWithMatcher() {
                Charset charset = StandardCharsets.UTF_8;
                ByteArrayInputStream inputStream = new ByteArrayInputStream("foobar".getBytes(charset));
                Matcher<String> matcher = startsWith("bar");
                AssertionError error = assertThrows(AssertionError.class, () -> assertContainsContent(inputStream, charset, matcher));
                assertThat(error.getMessage(), containsString("Expected: a string starting with \"bar\""));
                assertThat(error.getMessage(), containsString("but: was \"foobar\""));
            }
        }

        @Nested
        @DisplayName("InputStream throws exception for string")
        class InputStreamThrowsExceptionForString {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                String content = "foo";
                Charset charset = StandardCharsets.UTF_8;
                @SuppressWarnings("resource")
                InputStream inputStream = new BrokenInputStream();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(inputStream, charset, content));
                assertThat(error.getMessage(), startsWith("Unexpected exception thrown: " + IOException.class.getName()));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                String content = "foo";
                Charset charset = StandardCharsets.UTF_8;
                @SuppressWarnings("resource")
                InputStream inputStream = new BrokenInputStream();
                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertContainsContent(inputStream, charset, content, "error"));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + IOException.class.getName()));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                String content = "foo";
                Charset charset = StandardCharsets.UTF_8;
                @SuppressWarnings("resource")
                InputStream inputStream = new BrokenInputStream();
                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertContainsContent(inputStream, charset, content, messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + IOException.class.getName()));
            }

            @Test
            @DisplayName("with matcher")
            void testWithMatcher() {
                Charset charset = StandardCharsets.UTF_8;
                @SuppressWarnings("resource")
                InputStream inputStream = new BrokenInputStream();
                Matcher<String> matcher = startsWith("foo");
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertContainsContent(inputStream, charset, matcher));
                assertThat(error.getMessage(), startsWith("Unexpected exception thrown: " + IOException.class.getName()));
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

        @Nested
        @DisplayName("with null")
        class WithNull {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                assertNull(assertSerializable(null));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                assertNull(assertSerializable(null, "error"));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                Supplier<String> messageSupplier = () -> "error";
                assertNull(assertSerializable(null, messageSupplier));
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

        @Nested
        @DisplayName("with null")
        class WithNull {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertNotSerializable(null));
                assertThat(error.getMessage(), startsWith("Expected " + NotSerializableException.class.getName() + " to be thrown"));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertNotSerializable(null, "error"));
                assertThat(error.getMessage(), startsWith("error ==> Expected " + NotSerializableException.class.getName() + " to be thrown"));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertNotSerializable(null, messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> Expected " + NotSerializableException.class.getName() + " to be thrown"));
            }
        }
    }
}
