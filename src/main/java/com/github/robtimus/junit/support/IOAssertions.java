/*
 * IOAssertions.java
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

package com.github.robtimus.junit.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.function.Supplier;
import org.hamcrest.Matcher;

/**
 * A collection of utility methods that support asserting conditions related to I/O.
 *
 * @author Rob Spoor
 */
public final class IOAssertions {

    private IOAssertions() {
    }

    /**
     * Asserts that a {@link Reader} contains specific content.
     *
     * @param reader The reader to read from. It will be exhausted at the end of this method call.
     * @param expectedContent The expected content.
     * @throws NullPointerException If the given reader is null.
     */
    public static void assertContainsContent(Reader reader, String expectedContent) {
        String content = assertDoesNotThrow(() -> readContent(reader, expectedContent));
        assertEquals(expectedContent, content);
    }

    /**
     * Asserts that a {@link Reader} contains specific content.
     *
     * @param reader The reader to read from. It will be exhausted at the end of this method call.
     * @param expectedContent The expected content.
     * @param message The failure message to fail with.
     * @throws NullPointerException If the given reader is null.
     * @since 2.0
     */
    public static void assertContainsContent(Reader reader, String expectedContent, String message) {
        String content = assertDoesNotThrow(() -> readContent(reader, expectedContent), message);
        assertEquals(expectedContent, content, message);
    }

    /**
     * Asserts that a {@link Reader} contains specific content.
     *
     * @param reader The reader to read from. It will be exhausted at the end of this method call.
     * @param expectedContent The expected content.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @throws NullPointerException If the given reader is null.
     * @since 2.0
     */
    public static void assertContainsContent(Reader reader, String expectedContent, Supplier<String> messageSupplier) {
        String content = assertDoesNotThrow(() -> readContent(reader, expectedContent), messageSupplier);
        assertEquals(expectedContent, content, messageSupplier);
    }

    /**
     * Asserts that a {@link Reader} contains specific content.
     *
     * @param reader The reader to read from. It will be exhausted at the end of this method call.
     * @param matcher The matcher for the content.
     * @throws NullPointerException If the given reader or matcher is null.
     * @since 2.0
     */
    public static void assertContainsContent(Reader reader, Matcher<? super String> matcher) {
        String content = assertDoesNotThrow(() -> readContent(reader));
        assertThat(content, matcher);
    }

    private static String readContent(Reader reader, String expectedContent) throws IOException {
        StringBuilder sb = new StringBuilder(expectedContent.length());
        copyContent(reader, sb);
        return sb.toString();
    }

    private static String readContent(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        copyContent(reader, sb);
        return sb.toString();
    }

    private static void copyContent(Reader reader, StringBuilder sb) throws IOException {
        char[] buffer = new char[1024];
        int len;
        while ((len = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, len);
        }
    }

    /**
     * Asserts that an {@link InputStream} contains specific content.
     *
     * @param inputStream The input stream to read from. It will be exhausted at the end of this method call.
     * @param expectedContent The expected content.
     * @throws NullPointerException If the given input stream is null.
     */
    public static void assertContainsContent(InputStream inputStream, byte[] expectedContent) {
        byte[] content = assertDoesNotThrow(() -> readContent(inputStream, expectedContent.length));
        assertArrayEquals(expectedContent, content);
    }

    /**
     * Asserts that an {@link InputStream} contains specific content.
     *
     * @param inputStream The input stream to read from. It will be exhausted at the end of this method call.
     * @param expectedContent The expected content.
     * @param message The failure message to fail with.
     * @throws NullPointerException If the given input stream is null.
     * @since 2.0
     */
    public static void assertContainsContent(InputStream inputStream, byte[] expectedContent, String message) {
        byte[] content = assertDoesNotThrow(() -> readContent(inputStream, expectedContent.length), message);
        assertArrayEquals(expectedContent, content, message);
    }

    /**
     * Asserts that an {@link InputStream} contains specific content.
     *
     * @param inputStream The input stream to read from. It will be exhausted at the end of this method call.
     * @param expectedContent The expected content.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @throws NullPointerException If the given input stream is null.
     * @since 2.0
     */
    public static void assertContainsContent(InputStream inputStream, byte[] expectedContent, Supplier<String> messageSupplier) {
        byte[] content = assertDoesNotThrow(() -> readContent(inputStream, expectedContent.length), messageSupplier);
        assertArrayEquals(expectedContent, content, messageSupplier);
    }

    private static byte[] readContent(InputStream inputStream, int expectedContentLength) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedContentLength);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

    /**
     * Asserts that an {@link InputStream} contains specific content.
     *
     * @param inputStream The input stream to read from. It will be exhausted at the end of this method call.
     * @param charset The charset to use.
     * @param expectedContent The expected content.
     * @throws NullPointerException If the given input stream or charset is null.
     * @since 2.0
     */
    public static void assertContainsContent(InputStream inputStream, Charset charset, String expectedContent) {
        Reader reader = new InputStreamReader(inputStream, charset);
        assertContainsContent(reader, expectedContent);
    }

    /**
     * Asserts that an {@link InputStream} contains specific content.
     *
     * @param inputStream The input stream to read from. It will be exhausted at the end of this method call.
     * @param charset The charset to use.
     * @param expectedContent The expected content.
     * @param message The failure message to fail with.
     * @throws NullPointerException If the given input stream or charset is null.
     * @since 2.0
     */
    public static void assertContainsContent(InputStream inputStream, Charset charset, String expectedContent, String message) {
        Reader reader = new InputStreamReader(inputStream, charset);
        assertContainsContent(reader, expectedContent, message);
    }

    /**
     * Asserts that an {@link InputStream} contains specific content.
     *
     * @param inputStream The input stream to read from. It will be exhausted at the end of this method call.
     * @param charset The charset to use.
     * @param expectedContent The expected content.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @throws NullPointerException If the given input stream or charset is null.
     * @since 2.0
     */
    public static void assertContainsContent(InputStream inputStream, Charset charset, String expectedContent, Supplier<String> messageSupplier) {
        Reader reader = new InputStreamReader(inputStream, charset);
        assertContainsContent(reader, expectedContent, messageSupplier);
    }

    /**
     * Asserts that an {@link InputStream} contains specific content.
     *
     * @param inputStream The input stream to read from. It will be exhausted at the end of this method call.
     * @param charset The charset to use.
     * @param matcher The matcher for the content.
     * @throws NullPointerException If the given reader, charset or matcher is null.
     * @since 2.0
     */
    public static void assertContainsContent(InputStream inputStream, Charset charset, Matcher<? super String> matcher) {
        Reader reader = new InputStreamReader(inputStream, charset);
        assertContainsContent(reader, matcher);
    }

    /**
     * Asserts that an object is serializable.
     *
     * @param <T> The type of object to test.
     * @param object The object to test.
     * @return A deserialized copy of the object.
     */
    public static <T> T assertSerializable(T object) {
        return assertDoesNotThrow(() -> serializeAndDeserialize(object));
    }

    /**
     * Asserts that an object is serializable.
     *
     * @param <T> The type of object to test.
     * @param object The object to test.
     * @param message The failure message to fail with.
     * @return A deserialized copy of the object.
     * @since 2.0
     */
    public static <T> T assertSerializable(T object, String message) {
        return assertDoesNotThrow(() -> serializeAndDeserialize(object), message);
    }

    /**
     * Asserts that an object is serializable.
     *
     * @param <T> The type of object to test.
     * @param object The object to test.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return A deserialized copy of the object.
     * @since 2.0
     */
    public static <T> T assertSerializable(T object, Supplier<String> messageSupplier) {
        return assertDoesNotThrow(() -> serializeAndDeserialize(object), messageSupplier);
    }

    private static <T> T serializeAndDeserialize(T object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
        }
        byte[] bytes = baos.toByteArray();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            @SuppressWarnings("unchecked")
            T deserialized = (T) ois.readObject();
            return deserialized;
        }
    }

    /**
     * Asserts that an object is not serializable.
     *
     * @param object The object to test.
     */
    public static void assertNotSerializable(Object object) {
        assertThrows(NotSerializableException.class, () -> serializeOnly(object));
    }

    /**
     * Asserts that an object is not serializable.
     *
     * @param object The object to test.
     * @param message The failure message to fail with.
     * @since 2.0
     */
    public static void assertNotSerializable(Object object, String message) {
        assertThrows(NotSerializableException.class, () -> serializeOnly(object), message);
    }

    /**
     * Asserts that an object is not serializable.
     *
     * @param object The object to test.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @since 2.0
     */
    public static void assertNotSerializable(Object object, Supplier<String> messageSupplier) {
        assertThrows(NotSerializableException.class, () -> serializeOnly(object), messageSupplier);
    }

    private static void serializeOnly(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
        }
    }
}
