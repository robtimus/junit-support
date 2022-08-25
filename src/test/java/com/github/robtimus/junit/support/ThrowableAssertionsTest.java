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
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                        IOException.class.getName(), intermediate, root);
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
                        IOException.class.getName(), intermediate, root);
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

    private void throwException(Exception exception) throws Exception {
        throw exception;
    }

    private void throwError(Error error) {
        throw error;
    }
}
