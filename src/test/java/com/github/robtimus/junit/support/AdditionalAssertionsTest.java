/*
 * AdditionalAssertionsTest.java
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

import static com.github.robtimus.junit.support.AdditionalAssertions.assertHasCause;
import static com.github.robtimus.junit.support.AdditionalAssertions.assertHasDirectCause;
import static com.github.robtimus.junit.support.AdditionalAssertions.assertThrowsExactlyOneOf;
import static com.github.robtimus.junit.support.AdditionalAssertions.assertThrowsOneOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

@SuppressWarnings("nls")
class AdditionalAssertionsTest {

    @Nested
    @DisplayName("hasDirectCause")
    class HasDirectCause {

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

                String expectedMessage = String.format("expected caused by <%s> but was: <%s>",
                        IOException.class.getName(), intermediate);
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("with no cause")
            void testWithNoCause() {
                Exception exception = new IllegalStateException("no cause");

                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class));

                String expectedMessage = String.format("expected caused by <%s> but was: <null>",
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

                String expectedMessage = String.format("%s ==> expected caused by <%s> but was: <%s>",
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

                String expectedMessage = String.format("%s ==> expected caused by <%s> but was: <null>",
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
        class WithMessageOrSupplier {

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

                String expectedMessage = String.format("%s ==> expected caused by <%s> but was: <%s>",
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

                String expectedMessage = String.format("%s ==> expected caused by <%s> but was: <null>",
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
    @DisplayName("hasCause")
    class HasCause {

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

                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, SQLException.class));

                String expectedMessage = String.format("expected caused by <%s> but was: [<%s>, <%s>]",
                        SQLException.class.getName(), intermediate, root);
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("with no cause")
            void testWithNoCause() {
                Exception exception = new IllegalStateException("no cause");

                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class));

                String expectedMessage = String.format("expected caused by <%s> but was: []",
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
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, SQLException.class, message));

                String expectedMessage = String.format("%s ==> expected caused by <%s> but was: [<%s>, <%s>]",
                        message,
                        SQLException.class.getName(), intermediate, root);
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("with no cause")
            void testWithNoCause() {
                Exception exception = new IllegalStateException("no cause");

                String message = "Not caused by IOException";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class, message));

                String expectedMessage = String.format("%s ==> expected caused by <%s> but was: []",
                        message,
                        IOException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            private void throwException(Exception exception, Class<? extends Throwable> expectedCauseType, String message) {
                assertHasCause(expectedCauseType, exception, message);
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageOrSupplier {

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
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, SQLException.class, message));

                String expectedMessage = String.format("%s ==> expected caused by <%s> but was: [<%s>, <%s>]",
                        message,
                        SQLException.class.getName(), intermediate, root);
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("with no cause")
            void testWithNoCause() {
                Exception exception = new IllegalStateException("no cause");

                String message = "Not caused by IOException";
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> throwException(exception, IOException.class, message));

                String expectedMessage = String.format("%s ==> expected caused by <%s> but was: []",
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

                String expectedMessage = String.format("Unexpected exception type thrown ==> expected: one of <%s>, <%s> but was: <%s>",
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, this::throwNothing);

                String expectedMessage = String.format("Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            private void throwOtherException() {
                NumberFormatException exception = new NumberFormatException("other");

                assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception));
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

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown ==> expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
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
            }

            private void throwOtherException(String message) {
                NumberFormatException exception = new NumberFormatException("other");

                assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception), message);
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

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown ==> expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), NumberFormatException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
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
            }

            private void throwOtherException(String message) {
                NumberFormatException exception = new NumberFormatException("other");

                assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception), () -> message);
            }

            private void throwNothing(String message) {
                assertThrowsExactlyOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
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

                String expectedMessage = String.format("Unexpected exception type thrown ==> expected: one of <%s>, <%s> but was: <%s>",
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            @Test
            @DisplayName("nothing is thrown")
            void testNothingThrown() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, this::throwNothing);

                String expectedMessage = String.format("Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
            }

            private void throwOtherException() {
                IllegalStateException exception = new IllegalStateException("other");

                assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception));
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

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown ==> expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
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
            }

            private void throwOtherException(String message) {
                IllegalStateException exception = new IllegalStateException("other");

                assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception), message);
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

                String expectedMessage = String.format("%s ==> Unexpected exception type thrown ==> expected: one of <%s>, <%s> but was: <%s>",
                        message,
                        IllegalArgumentException.class.getName(), NullPointerException.class.getName(), IllegalStateException.class.getName());
                assertEquals(expectedMessage, error.getMessage());
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
            }

            private void throwOtherException(String message) {
                IllegalStateException exception = new IllegalStateException("other");

                assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> throwException(exception), () -> message);
            }

            private void throwNothing(String message) {
                assertThrowsOneOf(IllegalArgumentException.class, NullPointerException.class, () -> {
                    /* do nothing */
                }, () -> message);
            }
        }
    }

    private void throwException(Exception exception) throws Exception {
        throw exception;
    }
}
