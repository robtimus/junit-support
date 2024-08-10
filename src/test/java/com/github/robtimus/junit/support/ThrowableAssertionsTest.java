/*
 * ThrowableAssertionsTest.java
 * Copyright 2021 Rob Spoor
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

import static com.github.robtimus.junit.support.OptionalAssertions.assertIsEmpty;
import static com.github.robtimus.junit.support.OptionalAssertions.assertIsPresent;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertChainEquals;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertDoesNotThrowCheckedException;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertHasCause;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertHasDirectCause;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertOptionallyThrows;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertOptionallyThrowsExactly;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertOptionallyThrowsExactlyOneOf;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertOptionallyThrowsOneOf;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertThrowsExactlyOneOf;
import static com.github.robtimus.junit.support.ThrowableAssertions.assertThrowsOneOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.ParseException;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.opentest4j.AssertionFailedError;

@SuppressWarnings("nls")
class ThrowableAssertionsTest {

    @Nested
    @DisplayName("assertHasDirectCause")
    class AssertHasDirectCause {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("with matching cause")
            void testWithMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                UncheckedIOException foundCause = assertHasDirectCause(UncheckedIOException.class, exception);
                assertSame(intermediate, foundCause);
            }

            @Test
            @DisplayName("with non-matching cause")
            void testWithNonMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class));

                String expectedMessage = String.format("expected: caused by <%s> but was: <%s>",
                        IOException.class.getName(), intermediate);
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("with no cause")
            void testWithNoCause() {
                Exception exception = new IllegalStateException("no cause");

                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class));

                String expectedMessage = String.format("expected: caused by <%s> but was: <null>",
                        IOException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            private void throwException(Exception exception, Class<? extends Throwable> expectedCauseType) {
                assertHasDirectCause(expectedCauseType, exception);
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("with matching cause")
            void testWithMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                UncheckedIOException foundCause = assertHasDirectCause(UncheckedIOException.class, exception, "Not caused by UncheckedIOException");
                assertSame(intermediate, foundCause);
            }

            @Test
            @DisplayName("with non-matching cause")
            void testWithNonMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                String message = "Not caused by IOException";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class, message));

                String expectedMessage = String.format("%s ==> expected: caused by <%s> but was: <%s>",
                        message,
                        IOException.class.getName(), intermediate);
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("with no cause")
            void testWithNoCause() {
                Exception exception = new IllegalStateException("no cause");

                String message = "Not caused by IOException";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class, message));

                String expectedMessage = String.format("%s ==> expected: caused by <%s> but was: <null>",
                        message,
                        IOException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            private void throwException(Exception exception, Class<? extends Throwable> expectedCauseType, String message) {
                assertHasDirectCause(expectedCauseType, exception, message);
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("with matching cause")
            void testWithMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                UncheckedIOException foundCause = assertHasDirectCause(UncheckedIOException.class, exception,
                        () -> "Not caused by UncheckedIOException");
                assertSame(intermediate, foundCause);
            }

            @Test
            @DisplayName("with non-matching cause")
            void testWithNonMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                String message = "Not caused by IOException";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class, message));

                String expectedMessage = String.format("%s ==> expected: caused by <%s> but was: <%s>",
                        message,
                        IOException.class.getName(), intermediate);
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("with no cause")
            void testWithNoCause() {
                Exception exception = new IllegalStateException("no cause");

                String message = "Not caused by IOException";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class, message));

                String expectedMessage = String.format("%s ==> expected: caused by <%s> but was: <null>",
                        message,
                        IOException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            private void throwException(Exception exception, Class<? extends Throwable> expectedCauseType, String message) {
                assertHasDirectCause(expectedCauseType, exception, () -> message);
            }
        }
    }

    @Nested
    @DisplayName("assertHasCause")
    class AssertHasCause {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("with matching cause")
            void testWithMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                IOException foundCause = assertHasCause(IOException.class, exception);
                assertSame(root, foundCause);
            }

            @Test
            @DisplayName("with non-matching cause")
            void testWithNonMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, ParseException.class));

                String expectedMessage = String.format("expected: caused by <%s> but was: caused by <%s>, <%s>",
                        ParseException.class.getName(), intermediate, root);
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("with no cause")
            void testWithNoCause() {
                Exception exception = new IllegalStateException("no cause");

                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class));

                String expectedMessage = String.format("expected: caused by <%s> but was: caused by <null>",
                        IOException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            private void throwException(Exception exception, Class<? extends Throwable> expectedCauseType) {
                assertHasCause(expectedCauseType, exception);
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("with matching cause")
            void testWithMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                IOException foundCause = assertHasCause(IOException.class, exception, "Not caused by IOException");
                assertSame(root, foundCause);
            }

            @Test
            @DisplayName("with non-matching cause")
            void testWithNonMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                String message = "Not caused by IOException";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, ParseException.class, message));

                String expectedMessage = String.format("%s ==> expected: caused by <%s> but was: caused by <%s>, <%s>",
                        message, ParseException.class.getName(), intermediate, root);
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("with no cause")
            void testWithNoCause() {
                Exception exception = new IllegalStateException("no cause");

                String message = "Not caused by IOException";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class, message));

                String expectedMessage = String.format("%s ==> expected: caused by <%s> but was: caused by <null>",
                        message, IOException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            private void throwException(Exception exception, Class<? extends Throwable> expectedCauseType, String message) {
                assertHasCause(expectedCauseType, exception, message);
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("with matching cause")
            void testWithMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                IOException foundCause = assertHasCause(IOException.class, exception, () -> "Not caused by IOException");
                assertSame(root, foundCause);
            }

            @Test
            @DisplayName("with non-matching cause")
            void testWithNonMatchingCause() {
                IOException root = new IOException("cause");
                Exception intermediate = new UncheckedIOException(root);
                Exception exception = new IllegalStateException(intermediate);

                String message = "Not caused by IOException";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, ParseException.class, message));

                String expectedMessage = String.format("%s ==> expected: caused by <%s> but was: caused by <%s>, <%s>",
                        message, ParseException.class.getName(), intermediate, root);
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("with no cause")
            void testWithNoCause() {
                Exception exception = new IllegalStateException("no cause");

                String message = "Not caused by IOException";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class, message));

                String expectedMessage = String.format("%s ==> expected: caused by <%s> but was: caused by <null>",
                        message,
                        IOException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            private void throwException(Exception exception, Class<? extends Throwable> expectedCauseType, String message) {
                assertHasCause(expectedCauseType, exception, () -> message);
            }
        }

        @Test
        @DisplayName("has root cause")
        void testHasRootCause() {
            IOException root = new IOException("root");
            IOException intermediate1 = new IOException(root);
            IOException intermediate2 = new IOException(intermediate1);
            IOException intermediate3 = new IOException(intermediate2);
            IOException exception = new IOException(intermediate3);

            IOException cause = assertHasCause(IOException.class, exception);
            while (cause.getCause() != null) {
                cause = assertHasCause(IOException.class, cause);
            }
            assertSame(root, cause);
        }
    }

    @Nested
    @DisplayName("assertOptionallyThrows")
    class AssertOptionallyThrows {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("expected exception is thrown")
            void testExpectedThrown() {
                IllegalArgumentException exception = new NumberFormatException("first");

                Optional<IllegalArgumentException> thrown = assertOptionallyThrows(IllegalArgumentException.class, () -> throwException(exception));

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, this::throwOtherException);

                String expectedMessage = String.format("Unexpected exception type thrown, expected: <%s> but was: <%s>",
                        IllegalArgumentException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(IllegalStateException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, this::throwOutOfMemoryError);

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<IllegalArgumentException> thrown = throwNothing();
                assertIsEmpty(thrown);
            }

            private void throwOtherException() {
                IllegalStateException exception = new IllegalStateException("other");

                assertOptionallyThrows(IllegalArgumentException.class, () -> throwException(exception));
            }

            private void throwOutOfMemoryError() {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrows(IllegalAccessError.class, () -> throwError(error));
            }

            private Optional<IllegalArgumentException> throwNothing() {
                return assertOptionallyThrows(IllegalArgumentException.class, () -> {
                    /* do nothing */
                });
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("expected exception is thrown")
            void testExpectedThrown() {
                IllegalArgumentException exception = new NumberFormatException("first");

                Optional<IllegalArgumentException> thrown = assertOptionallyThrows(IllegalArgumentException.class, () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: <%s> but was: <%s>",
                        message, IllegalArgumentException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(IllegalStateException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<IllegalArgumentException> thrown = throwNothing("Wrong type of exception thrown");
                assertIsEmpty(thrown);
            }

            private void throwOtherException(String message) {
                IllegalStateException exception = new IllegalStateException("other");

                assertOptionallyThrows(IllegalArgumentException.class, () -> throwException(exception), message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrows(IllegalAccessError.class, () -> throwError(error), message);
            }

            private Optional<IllegalArgumentException> throwNothing(String message) {
                return assertOptionallyThrows(IllegalArgumentException.class, () -> {
                    /* do nothing */
                }, message);
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("expected exception is thrown")
            void testExpectedThrown() {
                IllegalArgumentException exception = new NumberFormatException("first");

                Optional<IllegalArgumentException> thrown = assertOptionallyThrows(IllegalArgumentException.class, () -> throwException(exception),
                        () -> "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: <%s> but was: <%s>",
                        message, IllegalArgumentException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(IllegalStateException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<IllegalArgumentException> thrown = throwNothing("Wrong type of exception thrown");
                assertIsEmpty(thrown);
            }

            private void throwOtherException(String message) {
                IllegalStateException exception = new IllegalStateException("other");

                assertOptionallyThrows(IllegalArgumentException.class, () -> throwException(exception), () -> message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrows(IllegalAccessError.class, () -> throwError(error), () -> message);
            }

            private Optional<IllegalArgumentException> throwNothing(String message) {
                return assertOptionallyThrows(IllegalArgumentException.class, () -> {
                    /* do nothing */
                }, () -> message);
            }
        }
    }

    @Nested
    @DisplayName("assertOptionallyThrowsExactly")
    class AssertOptionallyThrowsExactly {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("expected exception is thrown")
            void testExpectedThrown() {
                IllegalArgumentException exception = new IllegalArgumentException("first");

                Optional<IllegalArgumentException> thrown = assertOptionallyThrowsExactly(IllegalArgumentException.class,
                        () -> throwException(exception));

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, this::throwOtherException);

                String expectedMessage = String.format("Unexpected exception type thrown, expected: <%s> but was: <%s>",
                        IllegalArgumentException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(NumberFormatException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, this::throwOutOfMemoryError);

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<IllegalArgumentException> thrown = throwNothing();
                assertIsEmpty(thrown);
            }

            private void throwOtherException() {
                NumberFormatException exception = new NumberFormatException("other");

                assertOptionallyThrowsExactly(IllegalArgumentException.class, () -> throwException(exception));
            }

            private void throwOutOfMemoryError() {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrowsExactly(IllegalAccessError.class, () -> throwError(error));
            }

            private Optional<IllegalArgumentException> throwNothing() {
                return assertOptionallyThrowsExactly(IllegalArgumentException.class, () -> {
                    /* do nothing */
                });
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("expected exception is thrown")
            void testExpectedThrown() {
                IllegalArgumentException exception = new IllegalArgumentException("first");

                Optional<IllegalArgumentException> thrown = assertOptionallyThrowsExactly(IllegalArgumentException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: <%s> but was: <%s>",
                        message, IllegalArgumentException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(NumberFormatException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<IllegalArgumentException> thrown = throwNothing("Wrong type of exception thrown");
                assertIsEmpty(thrown);
            }

            private void throwOtherException(String message) {
                NumberFormatException exception = new NumberFormatException("other");

                assertOptionallyThrowsExactly(IllegalArgumentException.class, () -> throwException(exception), message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrowsExactly(IllegalAccessError.class, () -> throwError(error), message);
            }

            private Optional<IllegalArgumentException> throwNothing(String message) {
                return assertOptionallyThrowsExactly(IllegalArgumentException.class, () -> {
                    /* do nothing */
                }, message);
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("expected exception is thrown")
            void testExpectedThrown() {
                IllegalArgumentException exception = new IllegalArgumentException("first");

                Optional<IllegalArgumentException> thrown = assertOptionallyThrowsExactly(IllegalArgumentException.class,
                        () -> throwException(exception),
                        () -> "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: <%s> but was: <%s>",
                        message, IllegalArgumentException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(NumberFormatException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<IllegalArgumentException> thrown = throwNothing("Wrong type of exception thrown");
                assertIsEmpty(thrown);
            }

            private void throwOtherException(String message) {
                NumberFormatException exception = new NumberFormatException("other");

                assertOptionallyThrowsExactly(IllegalArgumentException.class, () -> throwException(exception), () -> message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrowsExactly(IllegalAccessError.class, () -> throwError(error), () -> message);
            }

            private Optional<IllegalArgumentException> throwNothing(String message) {
                return assertOptionallyThrowsExactly(IllegalArgumentException.class, () -> {
                    /* do nothing */
                }, () -> message);
            }
        }
    }

    @Nested
    @DisplayName("assertThrowsExactlyOneOf")
    class AssertThrowsExactlyOneOf {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new IllegalArgumentException("first");

                RuntimeException thrown = assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception));

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                RuntimeException thrown = assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception));

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, this::throwOtherException);

                String expectedMessage = String.format("Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(NumberFormatException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, this::throwOutOfMemoryError);

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, this::throwNothing);

                String expectedMessage = String.format("Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertNull(error.getCause());
            }

            private void throwOtherException() {
                NumberFormatException exception = new NumberFormatException("other");

                assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception));
            }

            private void throwOutOfMemoryError() {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertThrowsExactlyOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error));
            }

            private void throwNothing() {
                assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                });
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new IllegalArgumentException("first");

                RuntimeException thrown = assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                RuntimeException thrown = assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(NumberFormatException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwNothing(message));

                String expectedMessage = String.format("%s ==> Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertNull(error.getCause());
            }

            private void throwOtherException(String message) {
                NumberFormatException exception = new NumberFormatException("other");

                assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception), message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertThrowsExactlyOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error), message);
            }

            private void throwNothing(String message) {
                assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                }, message);
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new IllegalArgumentException("first");

                RuntimeException thrown = assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        () -> "Wrong type of exception thrown");

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                RuntimeException thrown = assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(NumberFormatException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwNothing(message));

                String expectedMessage = String.format("%s ==> Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertNull(error.getCause());
            }

            private void throwOtherException(String message) {
                NumberFormatException exception = new NumberFormatException("other");

                assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception), () -> message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertThrowsExactlyOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error), () -> message);
            }

            private void throwNothing(String message) {
                assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                }, () -> message);
            }
        }
    }

    @Nested
    @DisplayName("assertOptionallyThrowsExactlyOneOf")
    class AssertOptionallyThrowsExactlyOneOf {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new IllegalArgumentException("first");

                Optional<RuntimeException> thrown = assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception));

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                Optional<RuntimeException> thrown = assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception));

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, this::throwOtherException);

                String expectedMessage = String.format("Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(NumberFormatException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, this::throwOutOfMemoryError);

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<RuntimeException> thrown = throwNothing();
                assertIsEmpty(thrown);
            }

            private void throwOtherException() {
                NumberFormatException exception = new NumberFormatException("other");

                assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception));
            }

            private void throwOutOfMemoryError() {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrowsExactlyOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error));
            }

            private Optional<RuntimeException> throwNothing() {
                return assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                });
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new IllegalArgumentException("first");

                Optional<RuntimeException> thrown = assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                Optional<RuntimeException> thrown = assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(NumberFormatException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<RuntimeException> thrown = throwNothing("Wrong type of exception thrown");
                assertIsEmpty(thrown);
            }

            private void throwOtherException(String message) {
                NumberFormatException exception = new NumberFormatException("other");

                assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception),
                        message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrowsExactlyOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error), message);
            }

            private Optional<RuntimeException> throwNothing(String message) {
                return assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                }, message);
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new IllegalArgumentException("first");

                Optional<RuntimeException> thrown = assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        () -> "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                Optional<RuntimeException> thrown = assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(NumberFormatException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<RuntimeException> thrown = throwNothing("Wrong type of exception thrown");
                assertIsEmpty(thrown);
            }

            private void throwOtherException(String message) {
                NumberFormatException exception = new NumberFormatException("other");

                assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception),
                        () -> message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrowsExactlyOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error), () -> message);
            }

            private Optional<RuntimeException> throwNothing(String message) {
                return assertOptionallyThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                }, () -> message);
            }
        }
    }

    @Nested
    @DisplayName("assertThrowsOneOf")
    class AssertThrowsOneOf {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new NumberFormatException("first");

                RuntimeException thrown = assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception));

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                RuntimeException thrown = assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception));

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, this::throwOtherException);

                String expectedMessage = String.format("Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(IllegalStateException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, this::throwOutOfMemoryError);

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, this::throwNothing);

                String expectedMessage = String.format("Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertNull(error.getCause());
            }

            private void throwOtherException() {
                IllegalStateException exception = new IllegalStateException("other");

                assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception));
            }

            private void throwOutOfMemoryError() {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertThrowsOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error));
            }

            private void throwNothing() {
                assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                });
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new NumberFormatException("first");

                RuntimeException thrown = assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                RuntimeException thrown = assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(IllegalStateException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwNothing(message));

                String expectedMessage = String.format("%s ==> Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertNull(error.getCause());
            }

            private void throwOtherException(String message) {
                IllegalStateException exception = new IllegalStateException("other");

                assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception), message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertThrowsOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error), message);
            }

            private void throwNothing(String message) {
                assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                }, message);
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new NumberFormatException("first");

                RuntimeException thrown = assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        () -> "Wrong type of exception thrown");

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                RuntimeException thrown = assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(IllegalStateException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwNothing(message));

                String expectedMessage = String.format("%s ==> Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertNull(error.getCause());
            }

            private void throwOtherException(String message) {
                IllegalStateException exception = new IllegalStateException("other");

                assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception), () -> message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertThrowsOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error), () -> message);
            }

            private void throwNothing(String message) {
                assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                }, () -> message);
            }
        }
    }

    @Nested
    @DisplayName("assertOptionallyThrowsOneOf")
    class AssertOptionallyThrowsOneOf {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new NumberFormatException("first");

                Optional<RuntimeException> thrown = assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception));

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                Optional<RuntimeException> thrown = assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception));

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, this::throwOtherException);

                String expectedMessage = String.format("Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(IllegalStateException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, this::throwOutOfMemoryError);

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<RuntimeException> thrown = throwNothing();
                assertIsEmpty(thrown);
            }

            private void throwOtherException() {
                IllegalStateException exception = new IllegalStateException("other");

                assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception));
            }

            private void throwOutOfMemoryError() {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrowsOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error));
            }

            private Optional<RuntimeException> throwNothing() {
                return assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                });
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new NumberFormatException("first");

                Optional<RuntimeException> thrown = assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                Optional<RuntimeException> thrown = assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(IllegalStateException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<RuntimeException> thrown = throwNothing("Wrong type of exception thrown");
                assertIsEmpty(thrown);
            }

            private void throwOtherException(String message) {
                IllegalStateException exception = new IllegalStateException("other");

                assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception), message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrowsOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error), message);
            }

            private Optional<RuntimeException> throwNothing(String message) {
                return assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                }, message);
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("first expected exception is thrown")
            void testFirstThrown() {
                IllegalArgumentException exception = new NumberFormatException("first");

                Optional<RuntimeException> thrown = assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        () -> "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("second expected exception is thrown")
            void testSecondThrown() {
                NullPointerException exception = new NullPointerException("second");

                Optional<RuntimeException> thrown = assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class,
                        () -> throwException(exception),
                        "Wrong type of exception thrown");

                assertSame(exception, assertIsPresent(thrown));
            }

            @Test
            @DisplayName("different exception is thrown")
            void testDifferentThrown() {
                String message = "Wrong type of exception thrown";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwOtherException(message));

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
                assertInstanceOf(IllegalStateException.class, error.getCause());
                assertEquals("other", error.getCause().getMessage());
            }

            @Test
            @DisplayName("unrecoverable error is thrown")
            void testUnrecoverableErrorIsThrown() {
                String message = "Wrong type of exception thrown";
                OutOfMemoryError error = assertThrows(OutOfMemoryError.class, () -> throwOutOfMemoryError(message));

                assertEquals("error", error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                Optional<RuntimeException> thrown = throwNothing("Wrong type of exception thrown");
                assertIsEmpty(thrown);
            }

            private void throwOtherException(String message) {
                IllegalStateException exception = new IllegalStateException("other");

                assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception),
                        () -> message);
            }

            private void throwOutOfMemoryError(String message) {
                OutOfMemoryError error = new OutOfMemoryError("error");

                assertOptionallyThrowsOneOf(IllegalAccessError.class, NullPointerException.class, () -> throwError(error), () -> message);
            }

            private Optional<RuntimeException> throwNothing(String message) {
                return assertOptionallyThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                }, () -> message);
            }
        }
    }

    @Nested
    @DisplayName("assertDoesNotThrowCheckedException")
    class AssertDoesNotThrowCheckedException {

        @Nested
        @DisplayName("Executable does not throw anything")
        class ExecutableThrowsNothing {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                Executable executable = () -> { /* nothing */ };
                assertDoesNotThrowCheckedException(executable);
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                Executable executable = () -> { /* nothing */ };
                assertDoesNotThrowCheckedException(executable, "error");
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                Executable executable = () -> { /* nothing */ };
                Supplier<String> messageSupplier = () -> "error";
                assertDoesNotThrowCheckedException(executable, messageSupplier);
            }
        }

        @Nested
        @DisplayName("Executable throws checked exception")
        class ExecutableThrowsCheckedException {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                Executable executable = () -> {
                    throw new IOException();
                };
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoesNotThrowCheckedException(executable));
                assertThat(error.getMessage(), startsWith("Unexpected exception thrown: " + IOException.class.getName()));
                assertInstanceOf(IOException.class, error.getCause());
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                Executable executable = () -> {
                    throw new IOException();
                };
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoesNotThrowCheckedException(executable, "error"));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + IOException.class.getName()));
                assertInstanceOf(IOException.class, error.getCause());
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                Executable executable = () -> {
                    throw new IOException();
                };
                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertDoesNotThrowCheckedException(executable, messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + IOException.class.getName()));
                assertInstanceOf(IOException.class, error.getCause());
            }
        }

        @Nested
        @DisplayName("Executable throws unchecked exception")
        class ExecutableThrowsUncheckedException {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                RuntimeException exception = new IllegalStateException();
                Executable executable = () -> {
                    throw exception;
                };
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> assertDoesNotThrowCheckedException(executable));
                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                RuntimeException exception = new IllegalStateException();
                Executable executable = () -> {
                    throw exception;
                };
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> assertDoesNotThrowCheckedException(executable, "error"));
                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                RuntimeException exception = new IllegalStateException();
                Executable executable = () -> {
                    throw exception;
                };
                Supplier<String> messageSupplier = () -> "error";
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> assertDoesNotThrowCheckedException(executable, messageSupplier));
                assertSame(exception, thrown);
            }
        }

        @Nested
        @DisplayName("ThrowingSupplier does not throw anything")
        class ThrowingSupplierThrowsNothing {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                ThrowingSupplier<String> supplier = () -> "foo";
                assertEquals("foo", assertDoesNotThrowCheckedException(supplier));
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                ThrowingSupplier<String> supplier = () -> "foo";
                assertEquals("foo", assertDoesNotThrowCheckedException(supplier, "error"));
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                ThrowingSupplier<String> supplier = () -> "foo";
                Supplier<String> messageSupplier = () -> "error";
                assertEquals("foo", assertDoesNotThrowCheckedException(supplier, messageSupplier));
            }
        }

        @Nested
        @DisplayName("ThrowingSupplier throws checked exception")
        class ThrowingSupplierThrowsCheckedException {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                ThrowingSupplier<String> supplier = () -> {
                    throw new IOException();
                };
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoesNotThrowCheckedException(supplier));
                assertThat(error.getMessage(), startsWith("Unexpected exception thrown: " + IOException.class.getName()));
                assertInstanceOf(IOException.class, error.getCause());
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                ThrowingSupplier<String> supplier = () -> {
                    throw new IOException();
                };
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoesNotThrowCheckedException(supplier, "error"));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + IOException.class.getName()));
                assertInstanceOf(IOException.class, error.getCause());
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                ThrowingSupplier<String> supplier = () -> {
                    throw new IOException();
                };
                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertDoesNotThrowCheckedException(supplier, messageSupplier));
                assertThat(error.getMessage(), startsWith("error ==> Unexpected exception thrown: " + IOException.class.getName()));
                assertInstanceOf(IOException.class, error.getCause());
            }
        }

        @Nested
        @DisplayName("ThrowingSupplier throws unchecked exception")
        class ThrowingSupplierThrowsUncheckedException {

            @Test
            @DisplayName("without message or message supplier")
            void testWithoutMessageOrMessageSupplier() {
                RuntimeException exception = new IllegalStateException();
                ThrowingSupplier<String> supplier = () -> {
                    throw exception;
                };
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> assertDoesNotThrowCheckedException(supplier));
                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("with message")
            void testWithMessage() {
                RuntimeException exception = new IllegalStateException();
                ThrowingSupplier<String> supplier = () -> {
                    throw exception;
                };
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> assertDoesNotThrowCheckedException(supplier, "error"));
                assertSame(exception, thrown);
            }

            @Test
            @DisplayName("with message supplier")
            void testWithMessageSupplier() {
                RuntimeException exception = new IllegalStateException();
                ThrowingSupplier<String> supplier = () -> {
                    throw exception;
                };
                Supplier<String> messageSupplier = () -> "error";
                RuntimeException thrown = assertThrows(RuntimeException.class, () -> assertDoesNotThrowCheckedException(supplier, messageSupplier));
                assertSame(exception, thrown);
            }
        }
    }

    @Nested
    @DisplayName("assertChainEquals")
    class AssertChainEquals {

        @Nested
        @DisplayName("default depth")
        class DefaultDepth {

            @Nested
            @DisplayName("reached")
            class Reached {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = createException(19, new IOException("error"));
                    Throwable actual = createException(19, new IOException("error2"));

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual));
                    assertThat(error.getMessage(), startsWith("expected: "));
                    assertThat(error.getMessage(), containsString(" caused by java.io.IOException(error)>"));
                    assertThat(error.getMessage(), containsString(" caused by java.io.IOException(error2)>"));
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = createException(19, new IOException("error"));
                    Throwable actual = createException(19, new IOException("error2"));

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, "error"));
                    assertThat(error.getMessage(), startsWith("error ==> expected: "));
                    assertThat(error.getMessage(), containsString(" caused by java.io.IOException(error)>"));
                    assertThat(error.getMessage(), containsString(" caused by java.io.IOException(error2)>"));
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = createException(19, new IOException("error"));
                    Throwable actual = createException(19, new IOException("error2"));

                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, messageSupplier));
                    assertThat(error.getMessage(), startsWith("error ==> expected: "));
                    assertThat(error.getMessage(), containsString(" caused by java.io.IOException(error)>"));
                    assertThat(error.getMessage(), containsString(" caused by java.io.IOException(error2)>"));
                }
            }

            @Nested
            @DisplayName("exceeded")
            class Exceeded {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = createException(20, new IOException("error"));
                    Throwable rootCause = new IOException("error2");
                    Throwable actual = createException(20, rootCause);

                    Throwable result = assertChainEquals(expected, actual);
                    assertEquals(rootCause, result);
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = createException(20, new IOException("error"));
                    Throwable rootCause = new IOException("error2");
                    Throwable actual = createException(20, rootCause);

                    Throwable result = assertChainEquals(expected, actual, "error");
                    assertEquals(rootCause, result);
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = createException(20, new IOException("error"));
                    Throwable rootCause = new IOException("error2");
                    Throwable actual = createException(20, rootCause);

                    Throwable result = assertChainEquals(expected, actual, () -> "error");
                    assertEquals(rootCause, result);
                }
            }
        }

        @Nested
        @DisplayName("zero depth")
        class ZeroDepth {

            @Nested
            @DisplayName("equal type and message")
            class EqualTypeAndMessage {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new IOException("error");

                    Throwable result = assertChainEquals(expected, actual, 0);
                    assertNull(result);
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new IOException("error");

                    Throwable result = assertChainEquals(expected, actual, 0, "error");
                    assertNull(result);
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new IOException("error");

                    Throwable result = assertChainEquals(expected, actual, 0, () -> "error");
                    assertNull(result);
                }
            }

            @Nested
            @DisplayName("different type")
            class DifferentType {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new FileNotFoundException("error");

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 0));
                    assertEquals("expected: <java.io.IOException(error)> but was: <java.io.FileNotFoundException(error)>", error.getMessage());
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new FileNotFoundException("error");

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 0, "error"));
                    assertEquals("error ==> expected: <java.io.IOException(error)> but was: <java.io.FileNotFoundException(error)>",
                            error.getMessage());
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new FileNotFoundException("error");

                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class,
                            () -> assertChainEquals(expected, actual, 0, messageSupplier));
                    assertEquals("error ==> expected: <java.io.IOException(error)> but was: <java.io.FileNotFoundException(error)>",
                            error.getMessage());
                }
            }

            @Nested
            @DisplayName("different message")
            class DifferentMessage {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new IOException("error2");

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 0));
                    assertEquals("expected: <java.io.IOException(error)> but was: <java.io.IOException(error2)>", error.getMessage());
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new IOException("error2");

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 0, "error"));
                    assertEquals("error ==> expected: <java.io.IOException(error)> but was: <java.io.IOException(error2)>", error.getMessage());
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new IOException("error2");

                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class,
                            () -> assertChainEquals(expected, actual, 0, messageSupplier));
                    assertEquals("error ==> expected: <java.io.IOException(error)> but was: <java.io.IOException(error2)>", error.getMessage());
                }
            }

            @Nested
            @DisplayName("different cause")
            class DifferentCause {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new IOException("error", expected);

                    Throwable result = assertChainEquals(expected, actual, 0);
                    assertSame(expected, result);
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new IOException("error", expected);

                    Throwable result = assertChainEquals(expected, actual, 0, "message");
                    assertSame(expected, result);
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = new IOException("error");
                    Throwable actual = new IOException("error", expected);

                    Throwable result = assertChainEquals(expected, actual, 0, () -> "error");
                    assertSame(expected, result);
                }
            }

            @Nested
            @DisplayName("expected is null")
            class ExpectedIsNull {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable actual = new IOException("error");

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(null, actual, 0));
                    assertEquals("expected: <null> but was: <java.io.IOException(error)>", error.getMessage());
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable actual = new IOException("error");

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(null, actual, 0, "error"));
                    assertEquals("error ==> expected: <null> but was: <java.io.IOException(error)>", error.getMessage());
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable actual = new IOException("error");

                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(null, actual, 0, messageSupplier));
                    assertEquals("error ==> expected: <null> but was: <java.io.IOException(error)>", error.getMessage());
                }
            }

            @Nested
            @DisplayName("actual is null")
            class ActualIsNull {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = new IOException("error");

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, null, 0));
                    assertEquals("expected: <java.io.IOException(error)> but was: <null>", error.getMessage());
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = new IOException("error");

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, null, 0, "error"));
                    assertEquals("error ==> expected: <java.io.IOException(error)> but was: <null>", error.getMessage());
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = new IOException("error");

                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class,
                            () -> assertChainEquals(expected, null, 0, messageSupplier));
                    assertEquals("error ==> expected: <java.io.IOException(error)> but was: <null>", error.getMessage());
                }
            }

            @Nested
            @DisplayName("expected and actual are null")
            class ExpectedAndActualAreNull {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable result = assertChainEquals(null, null, 0);
                    assertNull(result);
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable result = assertChainEquals(null, null, 0, "error");
                    assertNull(result);
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable result = assertChainEquals(null, null, 0, () -> "error");
                    assertNull(result);
                }
            }
        }

        @Nested
        @DisplayName("non-zero depth")
        class NonZeroDepth {

            @Nested
            @DisplayName("negative maxDepth")
            class NegativeMaxDepth {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(null, null, -1));
                    assertEquals("maxDepth must not be negative ==> expected: <true> but was: <false>", error.getMessage());
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(null, null, -1, "error"));
                    assertEquals("maxDepth must not be negative ==> expected: <true> but was: <false>", error.getMessage());
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class,
                            () -> assertChainEquals(null, null, -1, messageSupplier));
                    assertEquals("maxDepth must not be negative ==> expected: <true> but was: <false>", error.getMessage());
                }
            }

            @Nested
            @DisplayName("equal type and message")
            class EqualTypeAndMessage {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable result = testNoException(3, 3);
                    assertNull(result);
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable result = testNoException(3, 3, "error");
                    assertNull(result);
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable result = testNoException(3, 3, () -> "error");
                    assertNull(result);
                }
            }

            @Nested
            @DisplayName("different type at included depth")
            class DifferentTypeAtIncludedDepth {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = createException(2, new IOException("error"));
                    Throwable actual = createException(2, new FileNotFoundException("error"));

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 3));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "error");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            FileNotFoundException.class.getName(), "error");
                    String expectedMessage = String.format("expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = createException(2, new IOException("error"));
                    Throwable actual = createException(2, new FileNotFoundException("error"));

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 3, "error"));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "error");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            FileNotFoundException.class.getName(), "error");
                    String expectedMessage = String.format("error ==> expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = createException(2, new IOException("error"));
                    Throwable actual = createException(2, new FileNotFoundException("error"));

                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class,
                            () -> assertChainEquals(expected, actual, 3, messageSupplier));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "error");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            FileNotFoundException.class.getName(), "error");
                    String expectedMessage = String.format("error ==> expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }
            }

            @Nested
            @DisplayName("different type at excluded depth")
            class DifferentTypeAtExcludedDepth {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = createException(3, new IOException("error"));
                    Throwable rootCause = new FileNotFoundException("error");
                    Throwable actual = createException(3, rootCause);

                    Throwable result = assertChainEquals(expected, actual, 3);
                    assertSame(rootCause, result);
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = createException(3, new IOException("error"));
                    Throwable rootCause = new FileNotFoundException("error");
                    Throwable actual = createException(3, rootCause);

                    Throwable result = assertChainEquals(expected, actual, 3, "error");
                    assertSame(rootCause, result);
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = createException(3, new IOException("error"));
                    Throwable rootCause = new FileNotFoundException("error");
                    Throwable actual = createException(3, rootCause);

                    Throwable result = assertChainEquals(expected, actual, 3, () -> "error");
                    assertSame(rootCause, result);
                }
            }

            @Nested
            @DisplayName("different message at included depth")
            class DifferentMessageAtIncludedDepth {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = createException(2, new IOException("error"));
                    Throwable actual = createException(2, new IOException("error2"));

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 3));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "error");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "error2");
                    String expectedMessage = String.format("expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = createException(2, new IOException("error"));
                    Throwable actual = createException(2, new IOException("error2"));

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 3, "error"));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "error");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "error2");
                    String expectedMessage = String.format("error ==> expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = createException(2, new IOException("error"));
                    Throwable actual = createException(2, new IOException("error2"));

                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class,
                            () -> assertChainEquals(expected, actual, 3, messageSupplier));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "error");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "error2");
                    String expectedMessage = String.format("error ==> expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }
            }

            @Nested
            @DisplayName("different message at excluded depth")
            class DifferentMessageAtExcludedDepth {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = createException(3, new IOException("error"));
                    Throwable rootCause = new IOException("error2");
                    Throwable actual = createException(3, rootCause);

                    Throwable result = assertChainEquals(expected, actual, 3);
                    assertSame(rootCause, result);
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = createException(3, new IOException("error"));
                    Throwable rootCause = new IOException("error2");
                    Throwable actual = createException(3, rootCause);

                    Throwable result = assertChainEquals(expected, actual, 3, "error");
                    assertSame(rootCause, result);
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = createException(3, new IOException("error"));
                    Throwable rootCause = new IOException("error2");
                    Throwable actual = createException(3, rootCause);

                    Throwable result = assertChainEquals(expected, actual, 3, () -> "error");
                    assertSame(rootCause, result);
                }
            }

            @Nested
            @DisplayName("expected has fewer causes at included depth")
            class ExpectedHasFewerCausesAtIncludedDepth {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = createException(2);
                    Throwable actual = createException(2, new IOException("root"));

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 3));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "root");
                    String expectedMessage = String.format("expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = createException(2);
                    Throwable actual = createException(2, new IOException("root"));

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 3, "error"));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "root");
                    String expectedMessage = String.format("error ==> expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = createException(2);
                    Throwable actual = createException(2, new IOException("root"));

                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class,
                            () -> assertChainEquals(expected, actual, 3, messageSupplier));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "root");
                    String expectedMessage = String.format("error ==> expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }
            }

            @Nested
            @DisplayName("expected has fewer causes at excluded depth")
            class ExpectedHasFewerCausesAtExcludedDepth {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable result = testNoException(3, 4);
                    assertInstanceOf(IOException.class, result);
                    assertEquals("depth: 4", result.getMessage());
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable result = testNoException(3, 4, "error");
                    assertInstanceOf(IOException.class, result);
                    assertEquals("depth: 4", result.getMessage());
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable result = testNoException(3, 4, () -> "error");
                    assertInstanceOf(IOException.class, result);
                    assertEquals("depth: 4", result.getMessage());
                }
            }

            @Nested
            @DisplayName("expected has more causes at included depth")
            class ExpectedHasMoreCausesAtIncludedDepth {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable expected = createException(3);
                    Throwable actual = createException(2);

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 3));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "depth: 3");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2");
                    String expectedMessage = String.format("expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable expected = createException(3);
                    Throwable actual = createException(2);

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertChainEquals(expected, actual, 3, "error"));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "depth: 3");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2");
                    String expectedMessage = String.format("error ==> expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable expected = createException(3);
                    Throwable actual = createException(2);

                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class,
                            () -> assertChainEquals(expected, actual, 3, messageSupplier));
                    String expectedChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2",
                            IOException.class.getName(), "depth: 3");
                    String actualChainString = String.format("%s(%s) caused by %s(%s) caused by %s(%s)",
                            IOException.class.getName(), "depth: 0",
                            IOException.class.getName(), "depth: 1",
                            IOException.class.getName(), "depth: 2");
                    String expectedMessage = String.format("error ==> expected: <%s> but was: <%s>", expectedChainString, actualChainString);
                    assertEquals(expectedMessage, error.getMessage());
                }
            }

            @Nested
            @DisplayName("expected has more causes at excluded depth")
            class ExpectedHasMoreCausesAtExcludedDepth {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable result = testNoException(4, 3);
                    assertNull(result);
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable result = testNoException(4, 3, "error");
                    assertNull(result);
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable result = testNoException(4, 3, () -> "error");
                    assertNull(result);
                }
            }

            @Nested
            @DisplayName("expected and actual have fewer causes than depth")
            class ExpectedAndActualHaveFewerCausesThanDepth {

                @Test
                @DisplayName("without message or message supplier")
                void testWithoutMessageOrMessageSupplier() {
                    Throwable result = testNoException(2, 2);
                    assertNull(result);
                }

                @Test
                @DisplayName("with message")
                void testWithMessage() {
                    Throwable result = testNoException(2, 2, "error");
                    assertNull(result);
                }

                @Test
                @DisplayName("with message supplier")
                void testWithMessageSupplier() {
                    Throwable result = testNoException(2, 2, () -> "error");
                    assertNull(result);
                }
            }

            private Throwable testNoException(int expectedCauses, int actualCauses) {
                Throwable expected = createException(expectedCauses);
                Throwable actual = createException(actualCauses);

                return assertChainEquals(expected, actual, 3);
            }

            private Throwable testNoException(int expectedCauses, int actualCauses, String message) {
                Throwable expected = createException(expectedCauses);
                Throwable actual = createException(actualCauses);

                return assertChainEquals(expected, actual, 3, message);
            }

            private Throwable testNoException(int expectedCauses, int actualCauses, Supplier<String> messageSupplier) {
                Throwable expected = createException(expectedCauses);
                Throwable actual = createException(actualCauses);

                return assertChainEquals(expected, actual, 3, messageSupplier);
            }
        }

        private Throwable createException(int causes) {
            return createException(causes, null);
        }

        private Throwable createException(int causes, Throwable rootCause) {
            return createException(0, causes, rootCause);
        }

        private Throwable createException(int depth, int causes, Throwable rootCause) {
            Throwable cause = causes == 0 ? rootCause : createException(depth + 1, causes - 1, rootCause);
            return new IOException("depth: " + depth, cause);
        }
    }

    private void throwException(Exception exception) throws Exception {
        throw exception;
    }

    private void throwError(Error error) {
        throw error;
    }
}
