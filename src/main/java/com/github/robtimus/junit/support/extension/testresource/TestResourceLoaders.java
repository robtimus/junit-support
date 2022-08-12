/*
 * TestResourceLoaders.java
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Objects;
import java.util.Properties;

/**
 * {@link TestResourceLoaders} contains some utility methods that can be used with {@link LoadWith} to load resource into objects.
 *
 * @author Rob Spoor
 * @since 2.0
 */
@SuppressWarnings("nls")
public final class TestResourceLoaders {

    private TestResourceLoaders() {
    }

    /**
     * Loads a resource into a string.
     *
     * @param reader A {@link Reader} containing the contents of the resource.
     * @return The content of the resource as a string.
     * @throws NullPointerException If the given reader is {@code null}.
     * @throws IOException If an error occurred when loading the resource.
     */
    public static String toString(Reader reader) throws IOException {
        return toStringBuilder(reader).toString();
    }

    /**
     * Loads a resource into a string. Any existing line separator will be replaced by the given line separator.
     *
     * @param reader A {@link Reader} containing the contents of the resource.
     * @param lineSeparator The line separator to use.
     * @return The content of the resource as a string.
     * @throws NullPointerException If the given reader or line separator is {@code null}.
     * @throws IOException If an error occurred when loading the resource.
     */
    public static String toString(Reader reader, String lineSeparator) throws IOException {
        Objects.requireNonNull(lineSeparator);

        return toString(reader)
                // first replace any \r\n with \n
                .replace("\r\n", "\n")
                // replace any remaining \r, which was not followed by \r, with \n
                .replace("\r", "\n")
                // now every line separator has been replaced with \n, replace that
                .replace("\n", lineSeparator);
    }

    /**
     * Loads a resource into a {@link StringBuilder}.
     *
     * @param reader A {@link Reader} containing the contents of the resource.
     * @return A {@link StringBuilder} containing the contents of the resource.
     * @throws NullPointerException If the given reader is {@code null}.
     * @throws IOException If an error occurred when loading the resource.
     */
    public static StringBuilder toStringBuilder(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();

        char[] buffer = new char[1024];
        int len;
        while ((len = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, len);
        }
        return sb;
    }

    /**
     * Loads a resource into a {@link StringBuilder}. Any existing line separator will be replaced by the given line separator.
     *
     * @param reader A {@link Reader} containing the contents of the resource.
     * @param lineSeparator The line separator to use.
     * @return A {@link StringBuilder} containing the contents of the resource.
     * @throws NullPointerException If the given reader or line separator is {@code null}.
     * @throws IOException If an error occurred when loading the resource.
     */
    public static StringBuilder toStringBuilder(Reader reader, String lineSeparator) throws IOException {
        return new StringBuilder(toString(reader, lineSeparator));
    }

    /**
     * Loads a resource into a {@link CharSequence}.
     *
     * @param reader A {@link Reader} containing the contents of the resource.
     * @return The content of the resource as a {@link CharSequence}.
     * @throws NullPointerException If the given reader is {@code null}.
     * @throws IOException If an error occurred when loading the resource.
     */
    public static CharSequence toCharSequence(Reader reader) throws IOException {
        return toStringBuilder(reader);
    }

    /**
     * Loads a resource into a {@link CharSequence}. Any existing line separator will be replaced by the given line separator.
     *
     * @param reader A {@link Reader} containing the contents of the resource.
     * @param lineSeparator The line separator to use.
     * @return The content of the resource as a {@link CharSequence}.
     * @throws NullPointerException If the given reader or line separator is {@code null}.
     * @throws IOException If an error occurred when loading the resource.
     */
    public static CharSequence toCharSequence(Reader reader, String lineSeparator) throws IOException {
        return toString(reader, lineSeparator);
    }

    /**
     * Loads a resource into a byte array.
     *
     * @param inputStream An {@link InputStream} containing the contents of the resource.
     * @return The content of the resource as a byte array.
     * @throws NullPointerException If the given input stream is {@code null}.
     * @throws IOException If an error occurred when loading the resource.
     */
    public static byte[] toBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }

    /**
     * Loads a resource into a {@link Properties} object.
     *
     * @param reader A {@link Reader} containing the contents of the resource.
     * @return A {@link Properties} object containing the properties from the resource.
     * @throws NullPointerException If the given reader is {@code null}.
     * @throws IOException If an error occurred when loading the resource.
     * @see Properties#load(Reader)
     */
    public static Properties toProperties(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }
}
