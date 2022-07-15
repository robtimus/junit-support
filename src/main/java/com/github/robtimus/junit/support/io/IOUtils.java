/*
 * IOUtils.java
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

final class IOUtils {

    private IOUtils() {
    }

    static int readAll(Reader reader, char[] buffer) throws IOException {
        // invariant: index + remaining == buffer.length
        int index = 0;
        int remaining = buffer.length;
        while (remaining > 0) {
            int n = reader.read(buffer, index, remaining);
            if (n == -1) {
                return index == 0 ? -1 : index;
            }
            index += n;
            remaining -= n;
        }
        return buffer.length;
    }

    static int readAll(InputStream inputStream, byte[] buffer) throws IOException {
        // invariant: index + remaining == buffer.length
        int index = 0;
        int remaining = buffer.length;
        while (remaining > 0) {
            int n = inputStream.read(buffer, index, remaining);
            if (n == -1) {
                return index == 0 ? -1 : index;
            }
            index += n;
            remaining -= n;
        }
        return buffer.length;
    }

    static long skipAll(InputStream inputStream, long n) throws IOException {
        long remaining = n;
        while (remaining > 0) {
            long skipped = inputStream.skip(remaining);
            if (skipped == 0) {
                return n - remaining;
            }
            remaining -= skipped;
        }
        return n;
    }

    static long skipAll(Reader reader, long n) throws IOException {
        long remaining = n;
        while (remaining > 0) {
            long skipped = reader.skip(remaining);
            if (skipped == 0) {
                return n - remaining;
            }
            remaining -= skipped;
        }
        return n;
    }

    static StringBuilder readAll(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();

        char[] buffer = new char[1024];
        int len;
        while ((len = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, len);
        }
        return sb;
    }

    static byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }
}
