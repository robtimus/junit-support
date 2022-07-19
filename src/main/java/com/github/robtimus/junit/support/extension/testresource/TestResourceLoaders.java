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
import java.util.Properties;

/**
 * {@link TestResourceLoaders} contains some utility methods that can be used with {@link LoadWith} to load resource into objects.
 *
 * @author Rob Spoor
 * @since 2.0
 */
public final class TestResourceLoaders {

    private TestResourceLoaders() {
    }

    /**
     * Loads a resource into a string.
     *
     * @param reader A {@link Reader} containing the contents of the resource.
     * @return The content of the resource as a string.
     * @throws IOException If an error occurred when loading the resource.
     */
    public static String toString(Reader reader) throws IOException {
        return toStringBuilder(reader).toString();
    }

    /**
     * Loads a resource into a {@link StringBuilder}.
     *
     * @param reader A {@link Reader} containing the contents of the resource.
     * @return A {@link StringBuilder} containing the contents of the resource.
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
     * Loads a resource into a byte array.
     *
     * @param inputStream An {@link InputStream} containing the contents of the resource.
     * @return The content of the resource as a byte array.
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
     * @throws IOException If an error occurred when loading the resource.
     * @see Properties#load(Reader)
     */
    public static Properties toProperties(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }
}
