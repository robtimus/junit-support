/*
 * WriterDelegateTests.java
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Base interface for testing that {@link Writer} implementations correctly delegate to another {@link Writer}.
 *
 * @author Rob Spoor
 */
public interface WriterDelegateTests {

    /**
     * Creates the writer to test.
     * <p>
     * This method will be called only once for each test. This makes it possible to capture the writer to test and its delegate, and perform
     * additional tests after the pre-defined test has finished.
     *
     * @param delegate The delegate to test against.
     * @return The created writer.
     */
    Writer wrapWriter(Writer delegate);

    /**
     * Returns the expected content written to a delegate, based on the content that was written to a {@link #wrapWriter(Writer) created writer}.
     *
     * @param written The content that was written.
     * @return The expected content.
     */
    String expectedContent(String written);

    /**
     * Returns the content to write for most tests. This default implementation returns a lorem ipsum text.
     *
     * @return The content to write.
     */
    default String contentToWrite() {
        return "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa."; //$NON-NLS-1$
    }

    /**
     * Returns the content to write when testing writing large pieces of text. this default implementation returns {@link #contentToWrite()}
     * concatenated {@code 1000} times.
     *
     * @return The content to write when testing writing large pieces of text.
     */
    default String longContentToWrite() {
        String content = contentToWrite();
        StringBuilder sb = new StringBuilder(1000 * content.length());
        for (int i = 0; i < 1000; i++) {
            sb.append(content);
        }
        return sb.toString();
    }

    /**
     * Contains tests for {@link Writer#write(int)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("write(int)")
    interface WriteCharTests extends WriterDelegateTests {

        @Test
        @DisplayName("write(int)")
        default void testWriteChar() {
            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(content);

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    for (int i = 0; i < content.length(); i++) {
                        writer.write(content.charAt(i));
                    }
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }
    }

    /**
     * Contains tests for {@link Writer#write(char[])}.
     *
     * @author Rob Spoor
     */
    @DisplayName("write(char[])")
    interface WriteCharArrayTests extends WriterDelegateTests {

        @Test
        @DisplayName("write(char[])")
        default void testWriteCharArray() {
            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(content);

                int bufferSize = 10;
                char[] chars = content.toCharArray();

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    for (int i = 0; i < chars.length; i += bufferSize) {
                        writer.write(Arrays.copyOfRange(chars, i, Math.min(i + bufferSize, chars.length)));
                    }
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(char[]) with a large array")
        default void testWriteCharArrayWithLargeArray() {
            assertDoesNotThrowCheckedException(() -> {
                String content = longContentToWrite();
                String expectedContent = expectedContent(content);

                char[] chars = content.toCharArray();

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    writer.write(chars);
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(char[]) with an empty array")
        default void testWriteCharArrayWithEmptyArray() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = expectedContent(""); //$NON-NLS-1$

                char[] chars = {};

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    writer.write(chars);
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(char[]) with a null array")
        default void testWriteCharArrayWithNullArray() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    char[] buffer = null;
                    assertThrows(NullPointerException.class, () -> writer.write(buffer));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }
    }

    /**
     * Contains tests for {@link Writer#write(char[], int, int)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("write(char[], int, int)")
    interface WriteCharArrayPortionTests extends WriterDelegateTests {

        @Test
        @DisplayName("write(char[], int, int)")
        default void testWriteCharArrayPortion() {
            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(content);

                int bufferSize = 10;
                char[] chars = content.toCharArray();

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    for (int i = 0; i < chars.length; i += bufferSize) {
                        writer.write(chars, i, Math.min(bufferSize, chars.length - i));
                    }
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(char[], int, int) with a large array")
        default void testWriteCharArrayPortionWithLargeArray() {
            assertDoesNotThrowCheckedException(() -> {
                String content = longContentToWrite();
                String expectedContent = expectedContent(content);

                char[] chars = content.toCharArray();

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    writer.write(chars, 0, chars.length);
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(char[], int, int) with a null array")
        default void testWriteCharArrayPortionWithNullArray() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    char[] buffer = null;
                    assertThrows(NullPointerException.class, () -> writer.write(buffer, 0, 10));
                }
                // assert that nothing was written
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(char[], int, int) with 0 length")
        default void testWriteCharArrayPortionWithZeroLength() {
            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(""); //$NON-NLS-1$

                char[] chars = content.toCharArray();

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    writer.write(chars, 5, 0);
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(char[], int, int) with a negative offset")
        default void testWriteCharArrayPortionWithNegativeOffset() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    char[] buffer = new char[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.write(buffer, -1, 10));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(char[], int, int) with an offset that exceeds the array length")
        default void testWriteCharArrayPortionWithTooHighOffset() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    char[] buffer = new char[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.write(buffer, buffer.length + 1, 0));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(char[], int, int) with a negative length")
        default void testWriteCharArrayPortionWithNegativeLength() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    char[] buffer = new char[10];
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.write(buffer, 5, -1));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(char[], int, int) with a length that exceeds the array length")
        default void testWriteCharArrayPortionWithTooHighLength() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    char[] buffer = new char[10];
                    // don't use 0 and 11, use 1 and 10, so it's not the value of the length that triggers the error but the combination off + len
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.write(buffer, 1, buffer.length));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }
    }

    /**
     * Contains tests for {@link Writer#write(String)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("write(String)")
    interface WriteStringTests extends WriterDelegateTests {

        @Test
        @DisplayName("write(String)")
        default void testWriteString() {
            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(content);

                int bufferSize = 10;

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    for (int i = 0; i < content.length(); i += bufferSize) {
                        writer.write(content.substring(i, Math.min(i + bufferSize, content.length())));
                    }
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(String) with a large String")
        default void testWriteStringWithLargeString() {
            assertDoesNotThrowCheckedException(() -> {
                String content = longContentToWrite();
                String expectedContent = expectedContent(content);

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    writer.write(content);
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(String) with an empty String")
        default void testWriteStringWithEmptyString() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = expectedContent(""); //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    writer.write(""); //$NON-NLS-1$
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(String) with a null String")
        default void testWriteStringWithNullString() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = null;
                    assertThrows(NullPointerException.class, () -> writer.write(string));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }
    }

    /**
     * Contains tests for {@link Writer#write(String, int, int)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("write(String, int, int)")
    interface WriteStringPortionTests extends WriterDelegateTests {

        @Test
        @DisplayName("write(String, int, int)")
        default void testWriteStringPortion() {
            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(content);

                int bufferSize = 10;

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    for (int i = 0; i < content.length(); i += bufferSize) {
                        writer.write(content, i, Math.min(bufferSize, content.length() - i));
                    }
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(String, int, int) with a large String")
        default void testWriteStringPortionWithLargeString() {
            assertDoesNotThrowCheckedException(() -> {
                String content = longContentToWrite();
                String expectedContent = expectedContent(content);

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    writer.write(content, 0, content.length());
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(String, int, int) with a null String")
        default void testWriteStringPortionWithNullString() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = null;
                    assertThrows(NullPointerException.class, () -> writer.write(string, 0, 10));
                }
                // assert that nothing was written
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(String, int, int) with 0 length")
        default void testWriteStringPortionWithZeroLength() {
            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(""); //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    writer.write(content, 5, 0);
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(String, int, int) with a negative offset")
        default void testWriteStringPortionWithNegativeOffset() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = "0123456789"; //$NON-NLS-1$
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.write(string, -1, 10));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(String, int, int) with an offset that exceeds the String length")
        default void testWriteStringPortionWithTooHighOffset() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = "0123456789"; //$NON-NLS-1$
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.write(string, string.length() + 1, 0));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(String, int, int) with a negative length")
        default void testWriteStringPortionWithNegativeLength() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = "0123456789"; //$NON-NLS-1$
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.write(string, 5, -1));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("write(String, int, int) with a length that exceeds the String length")
        default void testWriteStringPortionWithTooHighLength() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = "0123456789"; //$NON-NLS-1$
                    // don't use 0 and 11, use 1 and 10, so it's not the value of the length that triggers the error but the combination off + len
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.write(string, 1, string.length()));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }
    }

    /**
     * Contains tests for {@link Writer#append(CharSequence)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("append(CharSequence)")
    interface AppendCharSequenceTests extends WriterDelegateTests {

        @ParameterizedTest(name = "type: {0}")
        @ArgumentsSource(CharSequenceTransformationProvider.class)
        @DisplayName("append(CharSequence)")
        default void testAppendCharSequence(@SuppressWarnings("unused") String type, Function<String, CharSequence> contentTransformation) {
            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(content);

                int bufferSize = 10;

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    for (int i = 0; i < content.length(); i += bufferSize) {
                        String portion = content.substring(i, Math.min(i + bufferSize, content.length()));
                        assertSame(writer, writer.append(contentTransformation.apply(portion)));
                    }
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @ParameterizedTest(name = "type: {0}")
        @ArgumentsSource(CharSequenceTransformationProvider.class)
        @DisplayName("append(CharSequence) with a large CharSequence")
        default void testAppendCharSequenceWithLargeCharSequence(@SuppressWarnings("unused") String type,
                Function<String, CharSequence> contentTransformation) {

            assertDoesNotThrowCheckedException(() -> {
                String content = longContentToWrite();
                String expectedContent = expectedContent(content);

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    assertSame(writer, writer.append(contentTransformation.apply(content)));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @ParameterizedTest(name = "type: {0}")
        @ArgumentsSource(CharSequenceTransformationProvider.class)
        @DisplayName("append(CharSequence) with an empty CharSequence")
        default void testAppendCharSequenceWithEmptyCharSequence(@SuppressWarnings("unused") String type,
                Function<String, CharSequence> contentTransformation) {

            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = expectedContent(""); //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    assertSame(writer, writer.append(contentTransformation.apply(""))); //$NON-NLS-1$
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("append(CharSequence) with a null CharSequence")
        default void testAppendCharSequenceWithNullCharSequence() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = expectedContent("null"); //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    assertSame(writer, writer.append(null));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }
    }

    /**
     * Contains tests for {@link Writer#append(CharSequence, int, int)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("append(CharSequence, int, int)")
    interface AppendCharSequencePortionTests extends WriterDelegateTests {

        @ParameterizedTest(name = "type: {0}")
        @ArgumentsSource(CharSequenceTransformationProvider.class)
        @DisplayName("append(CharSequence, int, int)")
        default void testAppendCharSequencePortion(@SuppressWarnings("unused") String type, Function<String, CharSequence> contentTransformation) {
            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(content);

                int bufferSize = 10;

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    for (int i = 0; i < content.length(); i += bufferSize) {
                        assertSame(writer, writer.append(contentTransformation.apply(content), i, Math.min(i + bufferSize, content.length())));
                    }
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @ParameterizedTest(name = "type: {0}")
        @ArgumentsSource(CharSequenceTransformationProvider.class)
        @DisplayName("append(CharSequence, int, int) with a large CharSequence")
        default void testAppendCharSequencePortionWithLargeCharSequence(@SuppressWarnings("unused") String type,
                Function<String, CharSequence> contentTransformation) {

            assertDoesNotThrowCheckedException(() -> {
                String content = longContentToWrite();
                String expectedContent = expectedContent(content);

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    assertSame(writer, writer.append(contentTransformation.apply(content), 0, content.length()));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("append(CharSequence, int, int) with a null CharSequence")
        default void testAppendCharSequencePortionWithNullCharSequence() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = expectedContent("ul"); //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    assertSame(writer, writer.append(null, 1, 3));
                }
                // assert that nothing was written
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @ParameterizedTest(name = "type: {0}")
        @ArgumentsSource(CharSequenceTransformationProvider.class)
        @DisplayName("append(CharSequence, int, int) with start equal to end")
        default void testAppendCharSequencePortionWithStartEqualToEnd(@SuppressWarnings("unused") String type,
                Function<String, CharSequence> contentTransformation) {

            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(""); //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    assertSame(writer, writer.append(contentTransformation.apply(content), 5, 5));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("append(CharSequence, int, int) with a negative start")
        default void testAppendCharSequencePortionWithNegativeStart() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = "0123456789"; //$NON-NLS-1$
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.append(string, -1, 10));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("append(CharSequence, int, int) with a start that exceeds the CharSequence length")
        default void testAppendCharSequencePortionWithTooHighStart() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = "0123456789"; //$NON-NLS-1$
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.append(string, string.length() + 1, 0));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("append(CharSequence, int, int) with a negative end")
        default void testAppendCharSequencePortionWithNegativeEnd() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = "0123456789"; //$NON-NLS-1$
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.append(string, 0, -1));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("append(CharSequence, int, int) with an end that's smaller than the start")
        default void testAppendCharSequencePortionWithEndSmallerThanStart() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = "0123456789"; //$NON-NLS-1$
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.append(string, 5, 4));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }

        @Test
        @DisplayName("append(CharSequence, int, int) with an end that exceeds the CharSequence length")
        default void testAppendCharSequencePortionWithTooHighEnd() {
            assertDoesNotThrowCheckedException(() -> {
                String expectedContent = ""; //$NON-NLS-1$

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    String string = "0123456789"; //$NON-NLS-1$
                    assertThrowsOneOf(Arrays.asList(IndexOutOfBoundsException.class, IllegalArgumentException.class, IOException.class),
                            () -> writer.append(string, 1, string.length() + 1));
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }
    }

    /**
     * Contains tests for {@link Writer#append(char)}.
     *
     * @author Rob Spoor
     */
    @DisplayName("append(Char)")
    interface AppendCharTests extends WriterDelegateTests {

        @Test
        @DisplayName("append(char)")
        default void testAppendChar() {
            assertDoesNotThrowCheckedException(() -> {
                String content = contentToWrite();
                String expectedContent = expectedContent(content);

                StringWriter delegate = new StringWriter(expectedContent.length());

                try (Writer writer = wrapWriter(delegate)) {
                    for (int i = 0; i < content.length(); i++) {
                        assertSame(writer, writer.append(content.charAt(i)));
                    }
                }
                assertEquals(expectedContent, delegate.toString());
            });
        }
    }

    /**
     * Contains tests for {@link Writer#flush()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("flush()")
    interface FlushTests extends WriterDelegateTests {

        @Test
        @DisplayName("flush() delegates")
        @SuppressWarnings("resource")
        default void testFlushDelegates() {
            assertDoesNotThrowCheckedException(() -> {
                Writer delegate = mock(Writer.class);
                try (Writer writer = wrapWriter(delegate)) {
                    writer.flush();
                    verify(delegate).flush();
                }
            });
        }
    }

    /**
     * Contains tests for {@link Writer#close()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("close()")
    interface CloseTests extends WriterDelegateTests {

        @Test
        @DisplayName("close() delegates")
        default void testCloseDelegates() {
            assertDoesNotThrowCheckedException(() -> {
                @SuppressWarnings("resource")
                Writer delegate = mock(Writer.class);
                try (Writer writer = wrapWriter(delegate)) {
                    // no code necessary
                }
                verify(delegate).close();
            });
        }
    }

    /**
     * An arguments provider for {@link AppendCharSequenceTests} and {@link AppendCharSequencePortionTests}, it returns functions to transform
     * the test instance's {@link WriterDelegateTests#contentToWrite() content to write} or
     * {@link WriterDelegateTests#longContentToWrite() long content to write} into a {@code String} (identity transform), {@code StringBuilder},
     * {@code StringBuffer} and a generic {@code CharSequence}.
     *
     * @author Rob Spoor
     */
    class CharSequenceTransformationProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) { // NOSONAR, keep supporting JUnit versions before 5.13
            return Stream.of(
                    createArguments(String.class, Function.identity()),
                    createArguments(StringBuilder.class, StringBuilder::new),
                    createArguments(StringBuffer.class, StringBuffer::new),
                    createArguments(CharSequence.class, CharBuffer::wrap));
        }

        private <T> Arguments createArguments(Class<T> type, Function<String, T> transformation) {
            return arguments(type.getSimpleName(), transformation);
        }
    }
}
