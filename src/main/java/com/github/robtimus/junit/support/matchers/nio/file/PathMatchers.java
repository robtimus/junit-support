/*
 * PathMatchers.java
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

import static org.hamcrest.Matchers.equalTo;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.Objects;
import java.util.function.Predicate;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import com.github.robtimus.io.function.IOPredicate;

/**
 * A collection of {@link Matcher}s for {@link Path}.
 *
 * @author Rob Spoor
 * @since 2.0
 */
@SuppressWarnings("nls")
public final class PathMatchers {

    private PathMatchers() {
    }

    /**
     * Returns a matcher that checks if a {@link Path} is the same file as another {@link Path}.
     * <p>
     * If the given {@link Path} or the {@link Path} to assert does not exist, the {@link Path} to assert will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @param expected The {@link Path} that the {@link Path} to assert should be the same as.
     * @return A matcher that checks if a {@link Path} is the same file as another {@link Path}.
     * @see Files#isSameFile(Path, Path)
     */
    public static Matcher<Path> theSameFileAs(Path expected) {
        Objects.requireNonNull(expected);
        return new PathMatcher() {

            @Override
            boolean matches(Path item, Description mismatchDescription) throws IOException {
                if (Files.isSameFile(item, expected)) {
                    return true;
                }
                mismatchDescription.appendText("was ").appendValue(item.normalize());
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Path that is the same file as ").appendValue(expected);
            }
        };
    }

    /**
     * Returns a matcher that checks if a {@link Path} is an existing symbolic link.
     * <p>
     * If the {@link Path} to assert does not exist, it will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @return A matcher that checks if a {@link Path} is an existing symbolic link.
     */
    public static Matcher<Path> anExistingSymbolicLink() {
        return new FileTypeMatcher("a Path that is an existing symbolic link", BasicFileAttributes::isSymbolicLink, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * Returns a matcher that checks if a {@link Path} is an existing directory.
     * <p>
     * If the {@link Path} to assert does not exist, it will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @param options The link options to use.
     * @return A matcher that checks if a {@link Path} is an existing directory.
     * @see Files#isDirectory(Path, LinkOption...)
     */
    public static Matcher<Path> anExistingDirectory(LinkOption... options) {
        return new FileTypeMatcher("a Path that is an existing directory", BasicFileAttributes::isDirectory, options);
    }

    /**
     * Returns a matcher that checks if a {@link Path} is an existing regular file.
     * <p>
     * If the {@link Path} to assert does not exist, it will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @param options The link options to use.
     * @return A matcher that checks if a {@link Path} is an existing regular file.
     * @see Files#isRegularFile(Path, LinkOption...)
     */
    public static Matcher<Path> anExistingRegularFile(LinkOption... options) {
        return new FileTypeMatcher("a Path that is an existing regular file", BasicFileAttributes::isRegularFile, options);
    }

    /**
     * Returns a matcher that checks if a {@link Path} exists.
     *
     * @param options The link options to use.
     * @return A matcher that checks if a {@link Path} exists.
     * @see Files#exists(Path, LinkOption...)
     */
    public static Matcher<Path> anExistingPath(LinkOption... options) {
        return PathMatcher.create(item -> Files.exists(item, options), "an existing Path", "did not exist: ");
    }

    /**
     * Returns a matcher that checks if a {@link Path} does not exist.
     *
     * @param options The link options to use.
     * @return A matcher that checks if a {@link Path} does not exist.
     * @see Files#notExists(Path, LinkOption...)
     */
    public static Matcher<Path> aNonExistingPath(LinkOption... options) {
        return PathMatcher.create(item -> Files.notExists(item, options), "a non-existing Path", "existed: ");
    }

    /**
     * Returns a matcher that checks if a {@link Path} is absolute.
     * <p>
     * The existence of the {@link Path} to assert is not checked.
     *
     * @return A matcher that checks if a {@link Path} is absolute.
     * @see Path#isAbsolute()
     */
    public static Matcher<Path> anAbsolutePath() {
        return PathMatcher.create(Path::isAbsolute, "an absolute Path", "was ");
    }

    /**
     * Returns a matcher that checks if a {@link Path} is hidden.
     * <p>
     * If the {@link Path} to assert does not exist, it will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @return A matcher that checks if a {@link Path} is hidden.
     * @see Files#isHidden(Path)
     */
    public static Matcher<Path> aHiddenPath() {
        return PathMatcher.create(Files::isHidden, "a hidden Path", "was not hidden: ");
    }

    /**
     * Returns a matcher that checks if a {@link Path} is readable.
     * <p>
     * If the {@link Path} to assert does not exist or is not readable, it will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @return A matcher that checks if a {@link Path} is readable.
     * @see Files#isReadable(Path)
     * @see FileSystemProvider#checkAccess(Path, AccessMode...)
     */
    public static Matcher<Path> aReadablePath() {
        return PathMatcher.create(item -> checkAccess(item, AccessMode.READ), "a readable Path", "was not readable: ");
    }

    /**
     * Returns a matcher that checks if a {@link Path} is writable.
     * <p>
     * If the {@link Path} to assert does not exist or is not writable, it will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @return A matcher that checks if a {@link Path} is writable.
     * @see Files#isWritable(Path)
     * @see FileSystemProvider#checkAccess(Path, AccessMode...)
     */
    public static Matcher<Path> aWritablePath() {
        return PathMatcher.create(item -> checkAccess(item, AccessMode.WRITE), "a writable Path", "was not writable: ");
    }

    /**
     * Returns a matcher that checks if a {@link Path} is executable.
     * <p>
     * If the {@link Path} to assert does not exist or is not executable, it will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @return A matcher that checks if a {@link Path} is executable.
     * @see Files#isExecutable(Path)
     * @see FileSystemProvider#checkAccess(Path, AccessMode...)
     */
    public static Matcher<Path> anExecutablePath() {
        return PathMatcher.create(item -> checkAccess(item, AccessMode.EXECUTE), "an executable Path", "was not executable: ");
    }

    @SuppressWarnings("resource")
    private static boolean checkAccess(Path path, AccessMode accessMode) throws IOException {
        try {
            path.getFileSystem().provider().checkAccess(path, accessMode);
            return true;
        } catch (@SuppressWarnings("unused") AccessDeniedException e) {
            return false;
        }
    }

    /**
     * Returns a matcher that checks the size of a {@link Path}.
     * <p>
     * If the {@link Path} to assert does not exist or is not executable, it will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @param expected The expected file size.
     * @return A matcher that checks the size of a {@link Path}.
     * @see Files#size(Path)
     */
    public static Matcher<Path> aPathWithSize(long expected) {
        return aPathWithSize(equalTo(expected));
    }

    /**
     * Returns a matcher that checks the size of a {@link Path}.
     * <p>
     * If the {@link Path} to assert does not exist or is not executable, it will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @param expected A matcher for the file size.
     * @return A matcher that checks the size of a {@link Path}.
     * @see Files#size(Path)
     */
    public static Matcher<Path> aPathWithSize(Matcher<? super Long> expected) {
        return new BasicFileAttributesMatcher() {

            @Override
            boolean matches(BasicFileAttributes attributes, Path item, Description mismatchDescription) {
                long size = attributes.size();
                if (expected.matches(size)) {
                    return true;
                }
                expected.describeMismatch(size, mismatchDescription);
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Path with size ").appendDescriptionOf(expected);
            }
        };
    }

    // Don't add aPathLastModified with a FileTime argument - the exact last modified time will not often be exactly known

    /**
     * Returns a matcher that checks the last modified time of a {@link Path}.
     * <p>
     * If the {@link Path} to assert does not exist, it will not match the matcher.
     * <p>
     * Any other {@link IOException} will be thrown by the matcher wrapped in an {@link UncheckedIOException}.
     *
     * @param expected A matcher for the last modified time.
     * @param options The link options to use.
     * @return A matcher that checks the size of a {@link Path}.
     * @see Files#getLastModifiedTime(Path, LinkOption...)
     */
    public static Matcher<Path> aPathLastModified(Matcher<? super FileTime> expected, LinkOption... options) {
        return new BasicFileAttributesMatcher(options) {

            @Override
            boolean matches(BasicFileAttributes attributes, Path item, Description mismatchDescription) {
                FileTime lastModifiedTime = attributes.lastModifiedTime();
                if (expected.matches(lastModifiedTime)) {
                    return true;
                }
                expected.describeMismatch(lastModifiedTime, mismatchDescription);
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Path last modified ").appendDescriptionOf(expected);
            }
        };
    }

    /**
     * Returns a matcher that checks the file name of a {@link Path}.
     * <p>
     * The existence of the {@link Path} to assert is not checked.
     *
     * @param expected A matcher for the file name.
     * @return A matcher that checks the file name of a {@link Path}.
     * @see Path#getFileName()
     */
    public static Matcher<Path> aPathNamed(Matcher<? super String> expected) {
        return new FeatureMatcher<Path, String>(expected, "a Path with file name", "file name") {

            @Override
            protected String featureValueOf(Path actual) {
                Path fileName = actual.getFileName();
                return fileName != null ? fileName.toString() : null;
            }
        };
    }

    /**
     * Returns a matcher that checks the normalized path of a {@link Path}.
     * <p>
     * The existence of the {@link Path} to assert is not checked.
     *
     * @param expected A matcher for the normalized path.
     * @return A matcher that checks the normalized path of a {@link Path}.
     * @see Path#normalize()
     */
    public static Matcher<Path> aPathWithNormalizedPath(Matcher<? super String> expected) {
        return new FeatureMatcher<Path, String>(expected, "a Path with normalized path", "normalized path") {

            @Override
            protected String featureValueOf(Path actual) {
                return actual.normalize().toString();
            }
        };
    }

    /**
     * Returns a matcher that checks the absolute path of a {@link Path}.
     * <p>
     * The existence of the {@link Path} to assert is not checked.
     *
     * @param expected A matcher for the absolute path.
     * @return A matcher that checks the absolute path of a {@link Path}.
     * @see Path#toAbsolutePath()
     */
    public static Matcher<Path> aPathWithAbsolutePath(Matcher<? super String> expected) {
        return new FeatureMatcher<Path, String>(expected, "a Path with absolute path", "absolute path") {

            @Override
            protected String featureValueOf(Path actual) {
                return actual.toAbsolutePath().toString();
            }
        };
    }

    /**
     * Returns a matcher that checks if a {@link Path} starts with a path.
     * <p>
     * The existence of the {@link Path} to assert is not checked.
     *
     * @param expected The path that the {@link Path} to assert should start with.
     * @return A matcher that checks if a {@link Path} starts with a path.
     * @see Path#startsWith(String)
     */
    public static Matcher<Path> aPathStartingWith(String expected) {
        return new TypeSafeDiagnosingMatcher<Path>(Path.class) {

            @Override
            protected boolean matchesSafely(Path item, Description mismatchDescription) {
                if (item.startsWith(expected)) {
                    return true;
                }
                mismatchDescription.appendText("was ").appendValue(item);
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Path starting with ").appendValue(expected);
            }
        };
    }

    /**
     * Returns a matcher that checks if a {@link Path} starts with a path.
     * <p>
     * The existence of the {@link Path} to assert is not checked.
     *
     * @param expected The path that the {@link Path} to assert should start with.
     * @return A matcher that checks if a {@link Path} starts with a path.
     * @see Path#startsWith(String)
     */
    public static Matcher<Path> aPathStartingWith(Path expected) {
        return new TypeSafeDiagnosingMatcher<Path>(Path.class) {

            @Override
            protected boolean matchesSafely(Path item, Description mismatchDescription) {
                if (item.startsWith(expected)) {
                    return true;
                }
                mismatchDescription.appendText("was ").appendValue(item);
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Path starting with ").appendValue(expected);
            }
        };
    }

    /**
     * Returns a matcher that checks if a {@link Path} ends with a path.
     * <p>
     * The existence of the {@link Path} to assert is not checked.
     *
     * @param expected The path that the {@link Path} to assert should end with.
     * @return A matcher that checks if a {@link Path} ends with a path.
     * @see Path#endsWith(String)
     */
    public static Matcher<Path> aPathEndingWith(String expected) {
        return new TypeSafeDiagnosingMatcher<Path>(Path.class) {

            @Override
            protected boolean matchesSafely(Path item, Description mismatchDescription) {
                if (item.endsWith(expected)) {
                    return true;
                }
                mismatchDescription.appendText("was ").appendValue(item);
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Path ending with ").appendValue(expected);
            }
        };
    }

    /**
     * Returns a matcher that checks if a {@link Path} ends with a path.
     * <p>
     * The existence of the {@link Path} to assert is not checked.
     *
     * @param expected The path that the {@link Path} to assert should end with.
     * @return A matcher that checks if a {@link Path} ends with a path.
     * @see Path#endsWith(String)
     */
    public static Matcher<Path> aPathEndingWith(Path expected) {
        return new TypeSafeDiagnosingMatcher<Path>(Path.class) {

            @Override
            protected boolean matchesSafely(Path item, Description mismatchDescription) {
                if (item.endsWith(expected)) {
                    return true;
                }
                mismatchDescription.appendText("was ").appendValue(item);
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Path ending with ").appendValue(expected);
            }
        };
    }

    private abstract static class PathMatcher extends TypeSafeDiagnosingMatcher<Path> {

        @Override
        protected final boolean matchesSafely(Path item, Description mismatchDescription) {
            try {
                return matches(item, mismatchDescription);
            } catch (NoSuchFileException e) {
                mismatchDescription.appendText("did not exist: ").appendText("<" + e.getFile() + ">");
                return false;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        abstract boolean matches(Path item, Description mismatchDescription) throws IOException;

        private static PathMatcher create(IOPredicate<Path> predicate, String descriptionValue, String mismatchDescriptionValue) {
            return new PathMatcher() {

                @Override
                boolean matches(Path item, Description mismatchDescription) throws IOException {
                    if (predicate.test(item)) {
                        return true;
                    }
                    mismatchDescription.appendText(mismatchDescriptionValue).appendValue(item);
                    return false;
                }

                @Override
                public void describeTo(Description description) {
                    description.appendText(descriptionValue);
                }
            };
        }
    }

    private abstract static class BasicFileAttributesMatcher extends PathMatcher {

        private final LinkOption[] options;

        private BasicFileAttributesMatcher(LinkOption... options) {
            this.options = options;
        }

        @Override
        final boolean matches(Path item, Description mismatchDescription) throws IOException {
            BasicFileAttributes attributes = Files.readAttributes(item, BasicFileAttributes.class, options);
            return matches(attributes, item, mismatchDescription);
        }

        abstract boolean matches(BasicFileAttributes attributes, Path item, Description mismatchDescription);
    }

    private static final class FileTypeMatcher extends BasicFileAttributesMatcher {

        private final String descriptionValue;
        private final Predicate<BasicFileAttributes> predicate;

        private FileTypeMatcher(String descriptionValue, Predicate<BasicFileAttributes> predicate, LinkOption... options) {
            super(options);
            this.descriptionValue = descriptionValue;
            this.predicate = predicate;
        }

        @Override
        boolean matches(BasicFileAttributes attributes, Path item, Description mismatchDescription) {
            if (predicate.test(attributes)) {
                return true;
            }
            if (attributes.isDirectory()) {
                mismatchDescription.appendText("was a directory: ").appendValue(item);
                return false;
            }
            if (attributes.isRegularFile()) {
                mismatchDescription.appendText("was a regular file: ").appendValue(item);
                return false;
            }
            if (attributes.isSymbolicLink()) {
                mismatchDescription.appendText("was a symbolic link: ").appendValue(item);
                return false;
            }
            mismatchDescription.appendText("was another type of file: ").appendValue(item);
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(descriptionValue);
        }
    }
}
