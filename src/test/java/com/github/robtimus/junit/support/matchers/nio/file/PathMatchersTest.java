/*
 * PathMatchersTest.java
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

package com.github.robtimus.junit.support.matchers.nio.file;

import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aHiddenPath;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aNonExistingPath;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aPathEndingWith;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aPathLastModified;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aPathNamed;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aPathStartingWith;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aPathWithAbsolutePath;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aPathWithNormalizedPath;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aPathWithSize;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aReadablePath;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.aWritablePath;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.anAbsolutePath;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.anExecutablePath;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.anExistingDirectory;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.anExistingPath;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.anExistingRegularFile;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.anExistingSymbolicLink;
import static com.github.robtimus.junit.support.matchers.nio.file.PathMatchers.theSameFileAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.github.robtimus.filesystems.memory.MemoryFileAttributeView;
import com.github.robtimus.filesystems.memory.MemoryFileSystemProvider;

@SuppressWarnings("nls")
class PathMatchersTest {

    @BeforeEach
    void setupMemoryFileSystem() throws IOException {
        MemoryFileSystemProvider.clear();
        Files.createDirectory(Paths.get(URI.create("memory:/test")));
        MemoryFileSystemProvider.setContent("/test/file", "foobar".getBytes());
    }

    @Nested
    @DisplayName("theSameFileAs")
    class TheSameFileAs {

        @Test
        @DisplayName("matches")
        void testMatches() {
            Path path = Paths.get(URI.create("memory:/test/sub/../file"));
            Path expected = Paths.get(URI.create("memory:/test/file"));

            assertThat(path, theSameFileAs(expected));
        }

        @Test
        @DisplayName("doesn't match")
        void testDoesNotMatch() throws IOException {
            MemoryFileSystemProvider.setContent("/test/file2", "foobar".getBytes());

            Path path = Paths.get(URI.create("memory:/test/sub/../file"));
            Path expected = Paths.get(URI.create("memory:/test/file2"));
            Matcher<Path> matcher = theSameFileAs(expected);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path that is the same file as <%s>%n     but: was <%s>", expected, path.normalize()),
                    error.getMessage());
        }

        @Test
        @DisplayName("file doesn't exist")
        void testFileDoesNotExist() {
            Path path = Paths.get(URI.create("memory:/test/sub/../file3"));
            Path expected = Paths.get(URI.create("memory:/test/file2"));
            Matcher<Path> matcher = theSameFileAs(expected);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path that is the same file as <%s>%n     but: did not exist: <%s>", expected, path.normalize()),
                    error.getMessage());
        }

        @Test
        @DisplayName("other file doesn't exist")
        void testOtherFileDoesNotExist() {
            Path path = Paths.get(URI.create("memory:/test/sub/../file"));
            Path expected = Paths.get(URI.create("memory:/test/file3"));
            Matcher<Path> matcher = theSameFileAs(expected);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path that is the same file as <%s>%n     but: did not exist: <%s>", expected, expected),
                    error.getMessage());
        }
    }

    @Nested
    @DisplayName("anExistingSymbolicLink")
    class AnExistingSymbolicLink {

        @Test
        @DisplayName("matches")
        void testMatches() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path target = Paths.get(URI.create("memory:/test/target"));
            Files.createSymbolicLink(link, target);

            assertThat(link, anExistingSymbolicLink());
        }

        @Test
        @DisplayName("doesn't match with directory")
        void testDoesNotMatchWithDirectory() {
            Path path = Paths.get(URI.create("memory:/test"));
            Matcher<Path> matcher = anExistingSymbolicLink();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing symbolic link%n     but: was a directory: <%s>", path),
                    error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with regular file")
        void testDoesNotMatchWithRegularFile() {
            Path path = Paths.get(URI.create("memory:/test/file"));
            Matcher<Path> matcher = anExistingSymbolicLink();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing symbolic link%n     but: was a regular file: <%s>", path),
                    error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with non-existing file")
        void testDoesNotMatchWithNonExistingFile() {
            Path path = Paths.get(URI.create("memory:/test/link"));
            Matcher<Path> matcher = anExistingSymbolicLink();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing symbolic link%n     but: did not exist: <%s>", path),
                    error.getMessage());
        }
    }

    @Nested
    @DisplayName("anExistingDirectory")
    class AnExistingDirectory {

        @Test
        @DisplayName("matches with directory")
        void testMatchesWithDirectory() {
            Path path = Paths.get(URI.create("memory:/test"));

            assertThat(path, anExistingDirectory());
        }

        @Test
        @DisplayName("matches with symbolic link followed")
        void testMatchesWithSymbolicLinkFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/link"));
            Path path = Paths.get(URI.create("memory:/test"));
            Files.createSymbolicLink(link, path);

            assertThat(link, anExistingDirectory());
        }

        @Test
        @DisplayName("doesn't match with regular file")
        void testDoesNotMatchWithRegularFile() {
            Path path = Paths.get(URI.create("memory:/test/file"));
            Matcher<Path> matcher = anExistingDirectory();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing directory%n     but: was a regular file: <%s>", path),
                    error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with symbolic link not followed")
        void testDoesNotMatchWithSymbolicLinkNotFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/link"));
            Path path = Paths.get(URI.create("memory:/test"));
            Files.createSymbolicLink(link, path);
            Matcher<Path> matcher = anExistingDirectory(LinkOption.NOFOLLOW_LINKS);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(link, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing directory%n     but: was a symbolic link: <%s>", link),
                    error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with symbolic link followed to regular file")
        void testDoesNotMatchWithSymbolicLinkFollowedToRegularFile() throws IOException {
            Path link = Paths.get(URI.create("memory:/link"));
            Path path = Paths.get(URI.create("memory:/test/file"));
            Files.createSymbolicLink(link, path);
            Matcher<Path> matcher = anExistingDirectory(LinkOption.NOFOLLOW_LINKS);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(link, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing directory%n     but: was a symbolic link: <%s>", link),
                    error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with non-existing file")
        void testDoesNotMatchWithNonExistingFile() {
            Path path = Paths.get(URI.create("memory:/test/dir"));
            Matcher<Path> matcher = anExistingDirectory();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing directory%n     but: did not exist: <%s>", path),
                    error.getMessage());
        }
    }

    @Nested
    @DisplayName("anExistingRegularFile")
    class AnExistingRegularFile {

        @Test
        @DisplayName("matches with regular file")
        void testMatchesWithRegularFile() {
            Path path = Paths.get(URI.create("memory:/test/file"));

            assertThat(path, anExistingRegularFile());
        }

        @Test
        @DisplayName("matches with symbolic link followed")
        void testMatchesWithSymbolicLinkFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path path = Paths.get(URI.create("memory:/test/file"));
            Files.createSymbolicLink(link, path);

            assertThat(link, anExistingRegularFile());
        }

        @Test
        @DisplayName("doesn't match with directory")
        void testDoesNotMatchWithDirectory() {
            Path path = Paths.get(URI.create("memory:/test"));
            Matcher<Path> matcher = anExistingRegularFile();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing regular file%n     but: was a directory: <%s>", path),
                    error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with symbolic link not followed")
        void testDoesNotMatchWithSymbolicLinkNotFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path path = Paths.get(URI.create("memory:/test/file"));
            Files.createSymbolicLink(link, path);
            Matcher<Path> matcher = anExistingRegularFile(LinkOption.NOFOLLOW_LINKS);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(link, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing regular file%n     but: was a symbolic link: <%s>", link),
                    error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with symbolic link followed to directory")
        void testDoesNotMatchWithSymbolicLinkFollowedToDirectory() throws IOException {
            Path link = Paths.get(URI.create("memory:/link"));
            Path path = Paths.get(URI.create("memory:/test"));
            Files.createSymbolicLink(link, path);
            Matcher<Path> matcher = anExistingRegularFile(LinkOption.NOFOLLOW_LINKS);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(link, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing regular file%n     but: was a symbolic link: <%s>", link),
                    error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with non-existing file")
        void testDoesNotMatchWithNonExistingFile() {
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Matcher<Path> matcher = anExistingRegularFile();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path that is an existing regular file%n     but: did not exist: <%s>", path),
                    error.getMessage());
        }
    }

    @Nested
    @DisplayName("anExistingPath")
    class AnExistingPath {

        @Test
        @DisplayName("matches with regular file")
        void testMatchesWithRegularFile() {
            Path path = Paths.get(URI.create("memory:/test/file"));

            assertThat(path, anExistingPath());
        }

        @Test
        @DisplayName("matches with directory")
        void testMatchesWithDirectory() {
            Path path = Paths.get(URI.create("memory:/test"));

            assertThat(path, anExistingPath());
        }

        @Test
        @DisplayName("matches with link followed")
        void testMatchesWithLinkFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path path = Paths.get(URI.create("memory:/test/file"));
            Files.createSymbolicLink(link, path);

            assertThat(link, anExistingPath());
        }

        @Test
        @DisplayName("matches with broken link not followed")
        void testMatchesWithBrokenLinkNotFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Files.createSymbolicLink(link, path);

            assertThat(link, anExistingPath(LinkOption.NOFOLLOW_LINKS));
        }

        @Test
        @DisplayName("doesn't match with broken link")
        void testDoesNotMatchWithBrokenLinkFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Files.createSymbolicLink(link, path);
            Matcher<Path> matcher = anExistingPath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(link, matcher));
            assertEquals(String.format("%nExpected: an existing Path%n     but: did not exist: <%s>", link), error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with non-existing file")
        void testDoesNotMatchWithNonExistingFile() {
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Matcher<Path> matcher = anExistingPath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: an existing Path%n     but: did not exist: <%s>", path), error.getMessage());
        }
    }

    @Nested
    @DisplayName("aNonExistingPath")
    class ANonExistingPath {

        @Test
        @DisplayName("doesn't match with regular file")
        void testDoesNotMatchWithRegularFile() {
            Path path = Paths.get(URI.create("memory:/test/file"));
            Matcher<Path> matcher = aNonExistingPath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a non-existing Path%n     but: existed: <%s>", path), error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with directory")
        void testDoesNotMatchWithDirectory() {
            Path path = Paths.get(URI.create("memory:/test"));
            Matcher<Path> matcher = aNonExistingPath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a non-existing Path%n     but: existed: <%s>", path), error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with link followed")
        void testMatchesWithLinkFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path path = Paths.get(URI.create("memory:/test/file"));
            Files.createSymbolicLink(link, path);
            Matcher<Path> matcher = aNonExistingPath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(link, matcher));
            assertEquals(String.format("%nExpected: a non-existing Path%n     but: existed: <%s>", link), error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with broken link not followed")
        void testDoesNotMatchWithBrokenLinkNotFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Files.createSymbolicLink(link, path);
            Matcher<Path> matcher = aNonExistingPath(LinkOption.NOFOLLOW_LINKS);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(link, matcher));
            assertEquals(String.format("%nExpected: a non-existing Path%n     but: existed: <%s>", link), error.getMessage());
        }

        @Test
        @DisplayName("matches with broken link followed")
        void testMatchesWithBrokenLinkFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Files.createSymbolicLink(link, path);

            assertThat(link, aNonExistingPath());
        }

        @Test
        @DisplayName("matches with non-existing file")
        void testMatchesWithNonExistingFile() {
            Path path = Paths.get(URI.create("memory:/test/file2"));

            assertThat(path, aNonExistingPath());
        }
    }

    @Nested
    @DisplayName("anAbsolutePath")
    class AnAbsolutePath {

        @Test
        @DisplayName("matches with absolute path")
        void testMatchesWithAbsolutePath() {
            Path path = Paths.get(URI.create("memory:/test/file2"));

            assertThat(path, anAbsolutePath());
        }

        @Test
        @DisplayName("doesn't match with relative path")
        void testDoesNotMatchWithRelativePath() {
            Path path = Paths.get(URI.create("memory:file2"));
            Matcher<Path> matcher = anAbsolutePath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: an absolute Path%n     but: was <%s>", path), error.getMessage());
        }
    }

    @Nested
    @DisplayName("aHiddenPath")
    class AHiddenPath {

        @Test
        @DisplayName("matches with hidden path")
        void testMatchesWithHiddenPath() throws IOException {
            Path path = Paths.get(URI.create("memory:/test/file"));
            Files.getFileAttributeView(path, MemoryFileAttributeView.class).setHidden(true);

            assertThat(path, aHiddenPath());
        }

        @Test
        @DisplayName("doesn't match with non-hidden path")
        void testDoesNotMatchWithNonHiddenPath() {
            Path path = Paths.get(URI.create("memory:/test/file"));
            Matcher<Path> matcher = aHiddenPath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a hidden Path%n     but: was not hidden: <%s>", path), error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with non-existing path")
        void testDoesNotMatchWithNonExistingPath() {
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Matcher<Path> matcher = aHiddenPath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a hidden Path%n     but: did not exist: <%s>", path), error.getMessage());
        }
    }

    @Nested
    @DisplayName("aReadablePath")
    class AReadablePath {

        @Test
        @DisplayName("matches with readable path")
        void testMatchesWithHiddenPath() {
            Path path = Paths.get(URI.create("memory:/test/file"));

            assertThat(path, aReadablePath());
        }

        // cannot test a non-readable path with the memory file system

        @Test
        @DisplayName("doesn't match with non-existing path")
        void testDoesNotMatchWithNonExistingPath() {
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Matcher<Path> matcher = aReadablePath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a readable Path%n     but: did not exist: <%s>", path), error.getMessage());
        }
    }

    @Nested
    @DisplayName("aWritablePath")
    class AWritablePath {

        @Test
        @DisplayName("matches with writable path")
        void testMatchesWithWritablePath() {
            Path path = Paths.get(URI.create("memory:/test/file"));

            assertThat(path, aWritablePath());
        }

        @Test
        @DisplayName("doesn't match with read-only path")
        void testDoesNotMatchWithReadOnlyPath() throws IOException {
            Path path = Paths.get(URI.create("memory:/test/file"));
            Files.getFileAttributeView(path, MemoryFileAttributeView.class).setReadOnly(true);
            Matcher<Path> matcher = aWritablePath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a writable Path%n     but: was not writable: <%s>", path), error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with non-existing path")
        void testDoesNotMatchWithNonExistingPath() {
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Matcher<Path> matcher = aWritablePath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a writable Path%n     but: did not exist: <%s>", path), error.getMessage());
        }
    }

    @Nested
    @DisplayName("anExecutablePath")
    class AnExecutablePath {

        @Test
        @DisplayName("matches with directory")
        void testMatchesWithDirectory() {
            Path path = Paths.get(URI.create("memory:/test"));

            assertThat(path, anExecutablePath());
        }

        @Test
        @DisplayName("doesn't match with non-executable file")
        void testDoesNotMatchWithNonExecutableFile() {
            Path path = Paths.get(URI.create("memory:/test/file"));
            Matcher<Path> matcher = anExecutablePath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: an executable Path%n     but: was not executable: <%s>", path), error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with non-existing path")
        void testDoesNotMatchWithNonExistingPath() {
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Matcher<Path> matcher = anExecutablePath();

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: an executable Path%n     but: did not exist: <%s>", path), error.getMessage());
        }
    }

    @Nested
    @DisplayName("aPathWithSize")
    class APathWithSize {

        @Test
        @DisplayName("matches with matching size")
        void testMatchesWithMatchingSize() {
            Path path = Paths.get(URI.create("memory:/test/file"));

            assertThat(path, aPathWithSize("foobar".getBytes().length));
        }

        @Test
        @DisplayName("doesn't match with mismatching size")
        void testDoesNotMatchWithMismatchingSize() {
            Path path = Paths.get(URI.create("memory:/test/file"));
            Matcher<Path> matcher = aPathWithSize(1000);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path with size <1000L>%n     but: was <6L>"), error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with non-existing path")
        void testDoesNotMatchWithNonExistingPath() {
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Matcher<Path> matcher = aPathWithSize(3L);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path with size <3L>%n     but: did not exist: <%s>", path), error.getMessage());
        }
    }

    @Nested
    @DisplayName("aPathLastModified")
    class APathLastModified {

        @Test
        @DisplayName("matches with matching last modified")
        void testMatchesWithMatchingSize() {
            Path path = Paths.get(URI.create("memory:/test/file"));
            Matcher<FileTime> lastModified = both(greaterThan(FileTime.fromMillis(System.currentTimeMillis() - 5000L)))
                    .and(lessThan(FileTime.fromMillis(System.currentTimeMillis() + 5000)));

            assertThat(path, aPathLastModified(lastModified));
        }

        @Test
        @DisplayName("matches with broken link not followed")
        void testMatchesWithBrokenLinkNotFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Files.createSymbolicLink(link, path);
            Matcher<FileTime> lastModified = both(greaterThan(FileTime.fromMillis(System.currentTimeMillis() - 5000L)))
                    .and(lessThan(FileTime.fromMillis(System.currentTimeMillis() + 5000)));

            assertThat(link, aPathLastModified(lastModified, LinkOption.NOFOLLOW_LINKS));
        }

        @Test
        @DisplayName("doesn't match with mismatching last modified")
        void testDoesNotMatchWithInMismatchingSize() throws IOException {
            Path path = Paths.get(URI.create("memory:/test/file"));
            FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis() + 5000);
            Matcher<FileTime> lastModified = greaterThan(fileTime);
            Matcher<Path> matcher = aPathLastModified(lastModified);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path last modified a value greater than <%s>%n     but: <%s> was less than <%1$s>",
                    fileTime, Files.getLastModifiedTime(path)), error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with broken link followed")
        void testDoesNotMatchWithBrokenLinkFollowed() throws IOException {
            Path link = Paths.get(URI.create("memory:/test/link"));
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Files.createSymbolicLink(link, path);
            FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis() - 5000);
            Matcher<FileTime> lastModified = greaterThan(fileTime);
            Matcher<Path> matcher = aPathLastModified(lastModified);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(link, matcher));
            assertEquals(String.format("%nExpected: a Path last modified a value greater than <%s>%n     but: did not exist: <%s>", fileTime, path),
                    error.getMessage());
        }

        @Test
        @DisplayName("doesn't match with non-existing path")
        void testDoesNotMatchWithNonExistingPath() {
            Path path = Paths.get(URI.create("memory:/test/file2"));
            FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis() + 5000);
            Matcher<FileTime> lastModified = greaterThan(fileTime);
            Matcher<Path> matcher = aPathLastModified(lastModified);

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path last modified a value greater than <%s>%n     but: did not exist: <%s>", fileTime, path),
                    error.getMessage());
        }
    }

    @Nested
    @DisplayName("aPathNamed")
    class APathNamed {

        @Test
        @DisplayName("matches with matching file name")
        void testMatchesWithMatchingFileName() {
            Path path = Paths.get(URI.create("memory:/test/file2"));

            assertThat(path, aPathNamed(endsWith("2")));
        }

        @Test
        @DisplayName("matches with matching null file name")
        void testMatchesWithMatchingNullFileName() {
            Path path = Paths.get(URI.create("memory:/"));
            assertEquals(0, path.getNameCount());

            assertThat(path, aPathNamed(nullValue()));
        }

        @Test
        @DisplayName("does not match with mismatching file name")
        void testDoesNotMatchWithMismatchingFileName() {
            Path path = Paths.get(URI.create("memory:/test/file2"));
            Matcher<Path> matcher = aPathNamed(endsWith("1"));

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path with file name a string ending with \"1\"%n     but: file name was \"file2\""),
                    error.getMessage());
        }

        @Test
        @DisplayName("does not match with mismatching null file name")
        void testDoesNotMatchWithMismatchingNullFileName() {
            Path path = Paths.get(URI.create("memory:/"));
            assertEquals(0, path.getNameCount());
            Matcher<Path> matcher = aPathNamed(endsWith("1"));

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path with file name a string ending with \"1\"%n     but: file name was null"),
                    error.getMessage());
        }
    }

    @Nested
    @DisplayName("aPathWithNormalizedPath")
    class APathWithNormalizedPath {

        @Test
        @DisplayName("matches with matching normalized path")
        void testMatchesWithMatchingFileName() {
            Path path = Paths.get(URI.create("memory:/test/../sub/./../test/file2"));

            assertThat(path, aPathWithNormalizedPath(equalTo("/test/file2")));
        }

        @Test
        @DisplayName("does not match with mismatching normalized path")
        void testDoesNotMatchWithMismatchingFileName() {
            Path path = Paths.get(URI.create("memory:/test/../sub/./../test/file2"));
            Matcher<Path> matcher = aPathWithNormalizedPath(containsString("."));

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(
                    String.format("%nExpected: a Path with normalized path a string containing \".\"%n     but: normalized path was \"/test/file2\""),
                    error.getMessage());
        }
    }

    @Nested
    @DisplayName("aPathWithAbsolutePath")
    class APathWithAbsolutePath {

        @Test
        @DisplayName("matches with matching absolute path")
        void testMatchesWithMatchingFileName() {
            Path path = Paths.get(URI.create("memory:file2"));

            assertThat(path, aPathWithAbsolutePath(equalTo("/file2")));
        }

        @Test
        @DisplayName("does not match with mismatching absolute path")
        void testDoesNotMatchWithMismatchingFileName() {
            Path path = Paths.get(URI.create("memory:file2"));
            Matcher<Path> matcher = aPathWithAbsolutePath(equalTo("/test/file2"));

            AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
            assertEquals(String.format("%nExpected: a Path with absolute path \"/test/file2\"%n     but: absolute path was \"/file2\""),
                    error.getMessage());
        }
    }

    @Nested
    @DisplayName("aPathStartingWith")
    class APathStartingWith {

        @Nested
        @DisplayName("expected as String")
        class ExpectedAsString {

            @ParameterizedTest(name = "{0} starting with {1}")
            @CsvSource({
                    "/test/file2, /test",
                    "test/file2, test"
            })
            @DisplayName("matches starting with expected")
            void testMatchesStartingWithExpected(String pathValue, String expected) {
                Path path = Paths.get(URI.create("memory:" + pathValue));

                assertThat(path, aPathStartingWith(expected));
            }

            @Test
            @DisplayName("does not match not starting with expected")
            void testDoesNotMatchNotStartingWithExpected() {
                Path path = Paths.get(URI.create("memory:/test/file2"));
                Matcher<Path> matcher = aPathStartingWith("file2");

                AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
                assertEquals(String.format("%nExpected: a Path starting with \"file2\"%n     but: was <%s>", path), error.getMessage());
            }
        }

        @Nested
        @DisplayName("expected as Path")
        class ExpectedAsPath {

            @ParameterizedTest(name = "{0} starting with {1}")
            @CsvSource({
                    "/test/file2, /test",
                    "test/file2, test"
            })
            @DisplayName("matches starting with expected")
            void testMatchesStartingWithExpected(String pathValue, String expectedValue) {
                Path path = Paths.get(URI.create("memory:" + pathValue));
                Path expected = Paths.get(URI.create("memory:" + expectedValue));

                assertThat(path, aPathStartingWith(expected));
            }

            @Test
            @DisplayName("does not match not starting with expected")
            void testDoesNotMatchNotStartingWithExpected() {
                Path path = Paths.get(URI.create("memory:/test/file2"));
                Path expected = Paths.get(URI.create("memory:file2"));
                Matcher<Path> matcher = aPathStartingWith(expected);

                AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
                assertEquals(String.format("%nExpected: a Path starting with <%s>%n     but: was <%s>", expected, path), error.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("aPathEndingWith")
    class APathEndingWith {

        @Nested
        @DisplayName("expected as String")
        class ExpectedAsString {

            @Test
            @DisplayName("matches ending with expected")
            void testMatchesEndingWithExpected() {
                Path path = Paths.get(URI.create("memory:/test/file2"));

                assertThat(path, aPathEndingWith("file2"));
            }

            @ParameterizedTest(name = "{0} ending with {1}")
            @CsvSource({
                    "/test/file2, /test",
                    "test/file2, /file2"
            })
            @DisplayName("does not match not ending with expected")
            void testDoesNotMatchNotEndingWithExpected(String pathValue, String expected) {
                Path path = Paths.get(URI.create("memory:" + pathValue));
                Matcher<Path> matcher = aPathEndingWith(expected);

                AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
                assertEquals(String.format("%nExpected: a Path ending with \"%s\"%n     but: was <%s>", expected, path), error.getMessage());
            }
        }

        @Nested
        @DisplayName("expected as Path")
        class ExpectedAsPath {

            @Test
            @DisplayName("matches ending with expected")
            void testMatchesEndingWithExpected() {
                Path path = Paths.get(URI.create("memory:/test/file2"));
                Path expected = Paths.get(URI.create("memory:file2"));

                assertThat(path, aPathEndingWith(expected));
            }

            @ParameterizedTest(name = "{0} ending with {1}")
            @CsvSource({
                    "/test/file2, /test",
                    "test/file2, /file2"
            })
            @DisplayName("does not match not ending with expected")
            void testDoesNotMatchNotEndingWithExpected(String pathValue, String expectedValue) {
                Path path = Paths.get(URI.create("memory:" + pathValue));
                Path expected = Paths.get(URI.create("memory:" + expectedValue));
                Matcher<Path> matcher = aPathEndingWith(expected);

                AssertionError error = assertThrows(AssertionError.class, () -> assertThat(path, matcher));
                assertEquals(String.format("%nExpected: a Path ending with <%s>%n     but: was <%s>", expected, path), error.getMessage());
            }
        }
    }
}
