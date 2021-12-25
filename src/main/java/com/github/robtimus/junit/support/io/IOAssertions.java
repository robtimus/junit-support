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

package com.github.robtimus.junit.support.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import com.github.robtimus.io.function.IORunnable;

/**
 * A collection of utility methods that support asserting conditions related to I/O.
 *
 * @author Rob Spoor
 */
public final class IOAssertions {

    private IOAssertions() {
    }

    /**
     * Asserts that a piece of code does not throw an {@link IOException}.
     * This method works a lot like {@link Assertions#assertDoesNotThrow(Executable)}, except any exception other than {@link IOException} will not
     * be caught.
     *
     * @param runnable The piece
     */
    public static void assertDoesNotThrowIOException(IORunnable runnable) {
        try {
            runnable.run();
        } catch (IOException e) {
            assertDoesNotThrow(() -> {
                throw e;
            });
        }
    }

    /**
     * Asserts that a {@link Reader} contains specific content.
     *
     * @param reader The reader to read from. It will be exhausted at the end of this method call.
     * @param expectedContent The expected content.
     */
    public static void assertContainsContent(Reader reader, String expectedContent) {
        StringBuilder sb = new StringBuilder(expectedContent.length());
        assertDoesNotThrow(() -> {
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
        });
        assertEquals(expectedContent, sb.toString());
    }

    /**
     * Asserts that an {@link InputStream} contains specific content.
     *
     * @param inputStream The input stream to read from. It will be exhausted at the end of this method call.
     * @param expectedContent The expected content.
     */
    public static void assertContainsContent(InputStream inputStream, byte[] expectedContent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedContent.length);
        assertDoesNotThrow(() -> {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        });
        assertArrayEquals(expectedContent, baos.toByteArray());
    }

    static void assertNegativeSkip(Reader reader, boolean allowNegativeSkip) throws IOException {
        if (allowNegativeSkip) {
            assertEquals(0, reader.skip(-1));
        } else {
            Exception exception = assertThrows(Exception.class, () -> reader.skip(-1));
            assertThat(exception, either(instanceOf(IllegalArgumentException.class)).or(instanceOf(IOException.class)));
        }
    }

    static void assertNegativeSkip(InputStream inputStream, boolean allowNegativeSkip) throws IOException {
        if (allowNegativeSkip) {
            assertEquals(0, inputStream.skip(-1));
        } else {
            Exception exception = assertThrows(Exception.class, () -> inputStream.skip(-1));
            assertThat(exception, either(instanceOf(IllegalArgumentException.class)).or(instanceOf(IOException.class)));
        }
    }

    /**
     * Asserts that an object is serializable.
     *
     * @param <T> The type of object to test.
     * @param object The object to test.
     * @return A deserialized copy of the object.
     */
    public static <T> T assertSerializable(T object) {
        return assertDoesNotThrow(() -> {
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
        });
    }

    /**
     * Asserts that an object is not serializable.
     *
     * @param object The object to test.
     */
    public static void assertNotSerializable(Object object) {
        assertThrows(NotSerializableException.class, () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(object);
            }
        });
    }
}
