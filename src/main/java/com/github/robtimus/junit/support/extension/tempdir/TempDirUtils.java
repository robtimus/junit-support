/*
 * TempDirUtils.java
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

package com.github.robtimus.junit.support.extension.tempdir;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;

/**
 * A utility class that, combined with {@link TempDir}, provides most of the same functionality as JUnit 4's
 * <a href="https://junit.org/junit4/javadoc/4.13/org/junit/rules/TemporaryFolder.html">TemporaryFolder</a>.
 * <p>
 * All methods throw a {@link NullPointerException} if any argument is {@code null}, unless specified otherwise.
 *
 * @author Rob Spoor
 * @since 2.0
 */
@SuppressWarnings("nls")
public final class TempDirUtils {

    private static final String TMP_PREFIX = "junit"; //$NON-NLS-1$

    private TempDirUtils() {
    }

    /**
     * Returns a new fresh file with the given name under the given temporary folder.
     *
     * @param tempDir The temporary folder.
     * @param fileName The name for the new file.
     * @return A new fresh file as described.
     * @throws IOException If an I/O error occurs.
     */
    public static Path newFile(Path tempDir, String fileName) throws IOException {
        Path result = tempDir.resolve(fileName).normalize();
        if (!result.startsWith(tempDir) || result.equals(tempDir)) {
            throw new IllegalArgumentException(String.format(
                    "file name '%s' is not a relative path, or attempts to escape the temporary folder", fileName));
        }

        if (Files.exists(result)) {
            throw new FileAlreadyExistsException(String.format("a file or folder with the path '%s' already exists", tempDir.relativize(result)));
        }

        return Files.createFile(result);
    }

    /**
     * Returns a new fresh file with the given name under the given temporary folder.
     *
     * @param tempDir The temporary folder.
     * @param fileName The name for the new file.
     * @return A new fresh file as described.
     * @throws IOException If an I/O error occurs.
     */
    public static File newFile(File tempDir, String fileName) throws IOException {
        return newFile(tempDir.toPath(), fileName).toFile();
    }

    /**
     * Returns a new fresh file with a random name under the given temporary folder.
     *
     * @param tempDir The temporary folder.
     * @return A new fresh file as described.
     * @throws IOException If an I/O error occurs.
     */
    public static Path newFile(Path tempDir) throws IOException {
        return Files.createTempFile(tempDir, TMP_PREFIX, null);
    }

    /**
     * Returns a new fresh file with a random name under the given temporary folder.
     *
     * @param tempDir The temporary folder.
     * @return A new fresh file as described.
     * @throws IOException If an I/O error occurs.
     */
    public static File newFile(File tempDir) throws IOException {
        return newFile(tempDir.toPath()).toFile();
    }

    /**
     * Returns a new fresh folder with the given path under the given temporary folder.
     *
     * @param tempDir The temporary folder.
     * @param path The path to the new folder.
     * @return A new fresh folder as described.
     * @throws IOException If an I/O error occurs.
     */
    public static Path newFolder(Path tempDir, String path) throws IOException {
        String[] paths = { path };
        return newFolder(tempDir, paths);
    }

    /**
     * Returns a new fresh folder with the given path under the given temporary folder.
     *
     * @param tempDir The temporary folder.
     * @param path The path to the new folder.
     * @return A new fresh folder as described.
     * @throws IOException If an I/O error occurs.
     */
    public static File newFolder(File tempDir, String path) throws IOException {
        return newFolder(tempDir.toPath(), path).toFile();
    }

    /**
     * Returns a new fresh folder with the given paths under the given temporary folder.
     *
     * @param tempDir The temporary folder.
     * @param paths The paths to the new folder.
     * @return A new fresh folder as described.
     * @throws IOException If an I/O error occurs.
     */
    public static Path newFolder(Path tempDir, String... paths) throws IOException {
        if (paths.length == 0) {
            throw new IllegalArgumentException("must pass at least one path");
        }

        Path result = tempDir;
        for (String path : paths) {
            Path newResult = result.resolve(path).normalize();
            if (!newResult.startsWith(result) || newResult.equals(result)) {
                throw new IllegalArgumentException(String.format(
                        "folder path '%s' is not a relative path, or attempts to escape the temporary folder", path));
            }
            result = newResult;
        }

        if (Files.exists(result)) {
            throw new FileAlreadyExistsException(String.format("a file or folder with the path '%s' already exists", tempDir.relativize(result)));
        }

        return Files.createDirectories(result);
    }

    /**
     * Returns a new fresh folder with the given paths under the given temporary folder.
     *
     * @param tempDir The temporary folder.
     * @param paths The paths to the new folder.
     * @return A new fresh folder as described.
     * @throws IOException If an I/O error occurs.
     */
    public static File newFolder(File tempDir, String... paths) throws IOException {
        return newFolder(tempDir.toPath(), paths).toFile();
    }

    /**
     * Returns a new fresh folder with a random name under the given temporary folder.
     *
     * @param tempDir The temporary folder.
     * @return A new fresh folder as described.
     * @throws IOException If an I/O error occurs.
     */
    public static Path newFolder(Path tempDir) throws IOException {
        return Files.createTempDirectory(tempDir, TMP_PREFIX);
    }

    /**
     * Returns a new fresh folder with a random name under the given temporary folder.
     *
     * @param tempDir The temporary folder.
     * @return A new fresh folder as described.
     * @throws IOException If an I/O error occurs.
     */
    public static File newFolder(File tempDir) throws IOException {
        return newFolder(tempDir.toPath()).toFile();
    }

    // getRoot() not necessary; that's what @TempDir provides

    // delete() explicitly omitted, let @TempDir handle that
}
