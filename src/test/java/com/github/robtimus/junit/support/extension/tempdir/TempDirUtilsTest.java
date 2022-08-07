/*
 * TempDirUtilsTest.java
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

import static com.github.robtimus.junit.support.extension.tempdir.TempDirUtils.newFile;
import static com.github.robtimus.junit.support.extension.tempdir.TempDirUtils.newFolder;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aPathNamed;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.anExistingDirectory;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.anExistingRegularFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.io.FileMatchers.aFileNamed;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.hamcrest.io.FileMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("nls")
class TempDirUtilsTest {

    @Nested
    class WithPath {

        @TempDir
        private Path tempDir;

        @Nested
        @DisplayName("newFile(Path, String)")
        class NewFileWithFileName {

            @Test
            @DisplayName("test with absolute path")
            void testWithAbsolutePath() {
                assertThrows(IllegalArgumentException.class, () -> newFile(tempDir, "/absolute-file"));
            }

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "../file", "folder/../../file", "../other/file", "folder/../../other/file" })
            @DisplayName("breaking out of tempDir")
            void testBreakingOutOfTempDir(String fileName) {
                assertThrows(IllegalArgumentException.class, () -> newFile(tempDir, fileName));
            }

            @Test
            @DisplayName("with path to tempDir")
            void testWithPathToTempDir() {
                String fileName = "../" + tempDir.getFileName();
                assertThrows(IllegalArgumentException.class, () -> newFile(tempDir, fileName));
            }

            @Test
            @DisplayName("file in non-existing folder")
            void testFileInNonExistingFolder() {
                assertThrows(NoSuchFileException.class, () -> newFile(tempDir, "folder/file"));
            }

            @Test
            @DisplayName("file already exists")
            void testFileExists() {
                assertDoesNotThrow(() -> Files.createFile(tempDir.resolve("file")));
                assertThrows(FileAlreadyExistsException.class, () -> newFile(tempDir, "file"));
            }

            @Test
            @DisplayName("valid file")
            void testValidFile() {
                Path file = assertDoesNotThrow(() -> newFile(tempDir, "file"));
                assertEquals(tempDir.resolve("file"), file);
                assertThat(file, anExistingRegularFile(LinkOption.NOFOLLOW_LINKS));
            }
        }

        @Test
        @DisplayName("newFile(Path)")
        void testNewFileWithRandomFileName() {
            Path file = assertDoesNotThrow(() -> newFile(tempDir));
            assertEquals(tempDir, file.getParent());
            assertThat(file, aPathNamed(matchesRegex("junit.+")));
            assertThat(file, anExistingRegularFile(LinkOption.NOFOLLOW_LINKS));
        }

        @Nested
        @DisplayName("newFolder(Path, String)")
        class NewFolderWithPath {

            @Test
            @DisplayName("test with absolute path")
            void testWithAbsolutePath() {
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, "/absolute-folder"));
            }

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "../folder", "folder/../../folder", "../other/folder", "folder/../../other/folder" })
            @DisplayName("breaking out of tempDir")
            void testBreakingOutOfTempDir(String path) {
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, path));
            }

            @Test
            @DisplayName("with path to tempDir")
            void testWithPathToTempDir() {
                String fileName = "../" + tempDir.getFileName();
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, fileName));
            }

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "folder", "folder/sub", "folder/sub/subsub" })
            @DisplayName("valid file")
            void testValidPath(String path) {
                Path folder = assertDoesNotThrow(() -> newFolder(tempDir, path));
                assertEquals(tempDir.resolve(path), folder);
                assertThat(folder, anExistingDirectory(LinkOption.NOFOLLOW_LINKS));
            }
        }

        @Nested
        @DisplayName("newFolder(Path, String...)")
        class NewFolderWithPaths {

            @Test
            @DisplayName("with no paths")
            void testWithNoPaths() {
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, new String[0]));
            }

            @Test
            @DisplayName("test with absolute path")
            void testWithAbsolutePath() {
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, "folder", "/absolute-folder"));
            }

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "../folder", "folder/../../folder", "../other/folder", "folder/../../other/folder" })
            @DisplayName("breaking out of tempDir")
            void testBreakingOutOfTempDir(String path) {
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, "folder", path));
            }

            @Test
            @DisplayName("folder already exists")
            void testFolderExists() {
                assertDoesNotThrow(() -> Files.createDirectories(tempDir.resolve("folder/sub/subsub")));
                assertThrows(FileAlreadyExistsException.class, () -> newFolder(tempDir, "folder", "sub", "subsub"));
            }

            @Test
            @DisplayName("intermediate folder already exists")
            void testIntermediateFolderExists() {
                assertDoesNotThrow(() -> Files.createDirectories(tempDir.resolve("folder/sub")));
                Path folder = assertDoesNotThrow(() -> newFolder(tempDir, "folder", "sub", "subsub"));
                assertEquals(tempDir.resolve("folder/sub/subsub"), folder);
                assertThat(folder, anExistingDirectory(LinkOption.NOFOLLOW_LINKS));
            }

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "folder", "folder/sub", "folder/sub/subsub" })
            @DisplayName("valid file")
            void testValidPath(String path) {
                Path folder = assertDoesNotThrow(() -> newFolder(tempDir, "folder", path));
                assertEquals(tempDir.resolve("folder").resolve(path), folder);
                assertThat(folder, anExistingDirectory(LinkOption.NOFOLLOW_LINKS));
            }
        }

        @Test
        @DisplayName("newFolder(Path)")
        void testNewFolderWithRandomName() {
            Path folder = assertDoesNotThrow(() -> newFolder(tempDir));
            assertEquals(tempDir, folder.getParent());
            assertThat(folder, aPathNamed(matchesRegex("junit.+")));
            assertThat(folder, anExistingDirectory(LinkOption.NOFOLLOW_LINKS));
        }
    }

    @Nested
    class WithFile {

        @TempDir
        private File tempDir;

        @Nested
        @DisplayName("newFile(File, String)")
        class NewFileWithFileName {

            @Test
            @DisplayName("test with absolute path")
            void testWithAbsolutePath() {
                assertThrows(IllegalArgumentException.class, () -> newFile(tempDir, "/absolute-file"));
            }

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "../file", "folder/../../file", "../other/file", "folder/../../other/file" })
            @DisplayName("breaking out of tempDir")
            void testBreakingOutOfTempDir(String fileName) {
                assertThrows(IllegalArgumentException.class, () -> newFile(tempDir, fileName));
            }

            @Test
            @DisplayName("with path to tempDir")
            void testWithPathToTempDir() {
                String fileName = "../" + tempDir.getName();
                assertThrows(IllegalArgumentException.class, () -> newFile(tempDir, fileName));
            }

            @Test
            @DisplayName("file in non-existing folder")
            void testFileInNonExistingFolder() {
                assertThrows(NoSuchFileException.class, () -> newFile(tempDir, "folder/file"));
            }

            @Test
            @DisplayName("file already exists")
            void testFileExists() {
                assertDoesNotThrow(() -> new File(tempDir, "file").createNewFile());
                assertThrows(FileAlreadyExistsException.class, () -> newFile(tempDir, "file"));
            }

            @Test
            @DisplayName("valid file")
            void testValidFile() {
                File file = assertDoesNotThrow(() -> newFile(tempDir, "file"));
                assertEquals(new File(tempDir, "file"), file);
                assertThat(file, anExistingFile());
            }
        }

        @Test
        @DisplayName("newFile(File)")
        void testNewFileWithRandomFileName() {
            File file = assertDoesNotThrow(() -> newFile(tempDir));
            assertEquals(tempDir, file.getParentFile());
            assertThat(file, aFileNamed(matchesRegex("junit.+")));
            assertThat(file, anExistingFile());
        }

        @Nested
        @DisplayName("newFolder(File, String)")
        class NewFolderWithPath {

            @Test
            @DisplayName("test with absolute path")
            void testWithAbsolutePath() {
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, "/absolute-folder"));
            }

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "../folder", "folder/../../folder", "../other/folder", "folder/../../other/folder" })
            @DisplayName("breaking out of tempDir")
            void testBreakingOutOfTempDir(String path) {
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, path));
            }

            @Test
            @DisplayName("with path to tempDir")
            void testWithPathToTempDir() {
                String fileName = "../" + tempDir.getName();
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, fileName));
            }

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "folder", "folder/sub", "folder/sub/subsub" })
            @DisplayName("valid file")
            void testValidPath(String path) {
                File folder = assertDoesNotThrow(() -> newFolder(tempDir, path));
                assertEquals(new File(tempDir, path), folder);
                assertThat(folder, FileMatchers.anExistingDirectory());
            }
        }

        @Nested
        @DisplayName("newFolder(File, String...)")
        class NewFolderWithPaths {

            @Test
            @DisplayName("with no paths")
            void testWithNoPaths() {
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, new String[0]));
            }

            @Test
            @DisplayName("test with absolute path")
            void testWithAbsolutePath() {
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, "folder", "/absolute-folder"));
            }

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "../folder", "folder/../../folder", "../other/folder", "folder/../../other/folder" })
            @DisplayName("breaking out of tempDir")
            void testBreakingOutOfTempDir(String path) {
                assertThrows(IllegalArgumentException.class, () -> newFolder(tempDir, "folder", path));
            }

            @Test
            @DisplayName("folder already exists")
            void testFolderExists() {
                assertTrue(new File(tempDir, "folder/sub/subsub").mkdirs());
                assertThrows(FileAlreadyExistsException.class, () -> newFolder(tempDir, "folder", "sub", "subsub"));
            }

            @Test
            @DisplayName("intermediate folder already exists")
            void testIntermediateFolderExists() {
                assertTrue(new File(tempDir, "folder/sub").mkdirs());
                File folder = assertDoesNotThrow(() -> newFolder(tempDir, "folder", "sub", "subsub"));
                assertEquals(new File(tempDir, "folder/sub/subsub"), folder);
                assertThat(folder, FileMatchers.anExistingDirectory());
            }

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "folder", "folder/sub", "folder/sub/subsub" })
            @DisplayName("valid file")
            void testValidPath(String path) {
                File folder = assertDoesNotThrow(() -> newFolder(tempDir, "folder", path));
                assertEquals(new File(new File(tempDir, "folder"), path), folder);
                assertThat(folder, FileMatchers.anExistingDirectory());
            }
        }

        @Test
        @DisplayName("newFolder(File)")
        void testNewFolderWithRandomName() {
            File folder = assertDoesNotThrow(() -> newFolder(tempDir));
            assertEquals(tempDir, folder.getParentFile());
            assertThat(folder, aFileNamed(matchesRegex("junit.+")));
            assertThat(folder, FileMatchers.anExistingDirectory());
        }
    }
}
