/*
 * AssertionFailedErrorBuilderTest.java
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

package com.github.robtimus.junit.support;

import static com.github.robtimus.junit.support.AssertionFailedErrorBuilder.assertionFailedError;
import static com.github.robtimus.junit.support.AssertionFailedErrorBuilder.formatClassAndValue;
import static com.github.robtimus.junit.support.AssertionFailedErrorBuilder.isNotBlank;
import static com.github.robtimus.junit.support.AssertionFailedErrorBuilder.nullSafeGet;
import static com.github.robtimus.junit.support.AssertionFailedErrorBuilder.objectToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.function.Supplier;
import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;
import com.github.robtimus.junit.support.AssertionFailedErrorBuilder.ReasonBuilder;

@SuppressWarnings("nls")
class AssertionFailedErrorBuilderTest {

    @Nested
    @DisplayName("build")
    class Build {

        @Nested
        @DisplayName("without expected or actual")
        class WithoutExpectedOrActual {

            @Test
            @DisplayName("without reason, message or cause")
            void testWithoutReasonOrMessageOrCause() {
                AssertionFailedErrorBuilder builder = assertionFailedError();

                AssertionFailedError error = builder
                        .build();

                assertEquals("", error.getMessage());
                assertNull(error.getExpected());
                assertNull(error.getActual());
                assertNull(error.getCause());

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, builder::buildAndThrow);

                assertEqualErrors(error, thrown);

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with cause, without reason or message")
            void testWithCauseWithoutReasonOrMessage() {
                IOException cause = new IOException();

                AssertionFailedErrorBuilder builder = assertionFailedError()
                        .cause(cause);

                AssertionFailedError error = builder.build();

                assertEquals("", error.getMessage());
                assertNull(error.getExpected());
                assertNull(error.getActual());
                assertSame(cause, error.getCause());

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, builder::buildAndThrow);

                assertEqualErrors(error, thrown);

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message, without reason or cause")
            void testWithMessageWithoutReasonOrCause() {
                AssertionFailedErrorBuilder builder = assertionFailedError()
                        .message("some message");

                AssertionFailedError error = builder.build();

                assertEquals("some message", error.getMessage());
                assertNull(error.getExpected());
                assertNull(error.getActual());
                assertNull(error.getCause());

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, builder::buildAndThrow);

                assertEqualErrors(error, thrown);

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message and cause, without reason")
            void testWithMessageAndCauseWithoutReason() {
                IOException cause = new IOException();

                AssertionFailedErrorBuilder builder = assertionFailedError()
                        .message("some message")
                        .cause(cause);

                AssertionFailedError error = builder.build();

                assertEquals("some message", error.getMessage());
                assertNull(error.getExpected());
                assertNull(error.getActual());
                assertSame(cause, error.getCause());

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, builder::buildAndThrow);

                assertEqualErrors(error, thrown);

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason, without message or cause")
            void testWithReasonWithoutMessageOrCause() {
                AssertionFailedErrorBuilder builder = assertionFailedError()
                        .reason("some reason");

                AssertionFailedError error = builder.build();

                assertEquals("some reason", error.getMessage());
                assertNull(error.getExpected());
                assertNull(error.getActual());
                assertNull(error.getCause());

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, builder::buildAndThrow);

                assertEqualErrors(error, thrown);

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .reason("some reason")
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason and cause, without message")
            void testWithReasonAndCauseWithoutMessage() {
                IOException cause = new IOException();

                AssertionFailedErrorBuilder builder = assertionFailedError()
                        .reason("some reason")
                        .cause(cause);

                AssertionFailedError error = builder.build();

                assertEquals("some reason", error.getMessage());
                assertNull(error.getExpected());
                assertNull(error.getActual());
                assertSame(cause, error.getCause());

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, builder::buildAndThrow);

                assertEqualErrors(error, thrown);

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .reason("some reason")
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason and message, without cause")
            void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                AssertionFailedErrorBuilder builder = assertionFailedError()
                        .message("some message")
                        .reason("some reason");

                AssertionFailedError error = builder.build();

                assertEquals("some message ==> some reason", error.getMessage());
                assertNull(error.getExpected());
                assertNull(error.getActual());
                assertNull(error.getCause());

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, builder::buildAndThrow);

                assertEqualErrors(error, thrown);

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .reason("some reason")
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message, reason and cause")
            void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                IOException cause = new IOException();

                AssertionFailedErrorBuilder builder = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .cause(cause);

                AssertionFailedError error = builder.build();

                assertEquals("some message ==> some reason", error.getMessage());
                assertNull(error.getExpected());
                assertNull(error.getActual());
                assertSame(cause, error.getCause());

                AssertionFailedError thrown = assertThrows(AssertionFailedError.class, builder::buildAndThrow);

                assertEqualErrors(error, thrown);

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .reason("some reason")
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }
        }

        @Nested
        @DisplayName("with expected, without actual")
        class WithExpectedWithoutActual {

            @Nested
            @DisplayName("single value without prefix")
            class SingleValueWithoutPrefix {

                @Test
                @DisplayName("without reason, message or cause")
                void testWithoutReasonOrMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .expected("expected value")
                            .build();

                    assertEquals("expected: <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertNull(error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .expected("expected value")
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with cause, without reason or message")
                void testWithCauseWithoutReasonOrMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .expected("expected value")
                            .cause(cause)
                            .build();

                    assertEquals("expected: <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .expected("expected value")
                            .cause(cause)
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with message, without reason or cause")
                void testWithMessageWithoutReasonOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .expected("expected value")
                            .build();

                    assertEquals("some message ==> expected: <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertNull(error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .message("some message")
                            .expected("expected value")
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with message and cause, without reason")
                void testWithMessageAndCauseWithoutReason() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .expected("expected value")
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> expected: <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .message("some message")
                            .expected("expected value")
                            .cause(cause)
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with reason, without message or cause")
                void testWithReasonWithoutMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .expected("expected value")
                            .build();

                    assertEquals("some reason, expected: <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertNull(error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .reason("some reason")
                            .expected("expected value")
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with reason and cause, without message")
                void testWithReasonAndCauseWithoutMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .expected("expected value")
                            .cause(cause)
                            .build();

                    assertEquals("some reason, expected: <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .reason("some reason")
                            .expected("expected value")
                            .cause(cause)
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with reason and message, without cause")
                void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .expected("expected value")
                            .build();

                    assertEquals("some message ==> some reason, expected: <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertNull(error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .message("some message")
                            .reason("some reason")
                            .expected("expected value")
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with message, reason and cause")
                void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .expected("expected value")
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> some reason, expected: <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .message("some message")
                            .reason("some reason")
                            .expected("expected value")
                            .cause(cause)
                            .build();

                    assertEqualErrors(junitError, error);
                }
            }

            @Nested
            @DisplayName("single value with prefix")
            class SingleValueWithPrefix {

                @Test
                @DisplayName("without reason, message or cause")
                void testWithoutReasonOrMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .prefixed("like").expected("expected value")
                            .build();

                    assertEquals("expected: like <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with cause, without reason or message")
                void testWithCauseWithoutReasonOrMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .prefixed("like").expected("expected value")
                            .cause(cause)
                            .build();

                    assertEquals("expected: like <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, without reason or cause")
                void testWithMessageWithoutReasonOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .prefixed("like").expected("expected value")
                            .build();

                    assertEquals("some message ==> expected: like <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message and cause, without reason")
                void testWithMessageAndCauseWithoutReason() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .prefixed("like").expected("expected value")
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> expected: like <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason, without message or cause")
                void testWithReasonWithoutMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .prefixed("like").expected("expected value")
                            .build();

                    assertEquals("some reason, expected: like <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and cause, without message")
                void testWithReasonAndCauseWithoutMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .prefixed("like").expected("expected value")
                            .cause(cause)
                            .build();

                    assertEquals("some reason, expected: like <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and message, without cause")
                void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .prefixed("like").expected("expected value")
                            .build();

                    assertEquals("some message ==> some reason, expected: like <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, reason and cause")
                void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .prefixed("like").expected("expected value")
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> some reason, expected: like <expected value> but was: <null>", error.getMessage());
                    assertExpected(error, "expected value");
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }
            }

            @Nested
            @DisplayName("multiple values without prefix")
            class MultipleValuesWithoutPrefix {

                @Test
                @DisplayName("without reason, message or cause")
                void testWithoutReasonOrMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .prefixed("").expectedOneOf(1, 2, 3)
                            .build();

                    assertEquals("expected: <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with cause, without reason or message")
                void testWithCauseWithoutReasonOrMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .prefixed("").expectedOneOf(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("expected: <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, without reason or cause")
                void testWithMessageWithoutReasonOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .prefixed("").expectedOneOf(1, 2, 3)
                            .build();

                    assertEquals("some message ==> expected: <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message and cause, without reason")
                void testWithMessageAndCauseWithoutReason() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .prefixed("").expectedOneOf(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> expected: <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason, without message or cause")
                void testWithReasonWithoutMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .prefixed("").expectedOneOf(1, 2, 3)
                            .build();

                    assertEquals("some reason, expected: <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and cause, without message")
                void testWithReasonAndCauseWithoutMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .prefixed("").expectedOneOf(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some reason, expected: <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and message, without cause")
                void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .prefixed("").expectedOneOf(1, 2, 3)
                            .build();

                    assertEquals("some message ==> some reason, expected: <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, reason and cause")
                void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .prefixed("").expectedOneOf(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> some reason, expected: <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }
            }

            @Nested
            @DisplayName("multiple values with prefix")
            class MultipleValuesWithPrefix {

                // Note: use the default prefix

                @Test
                @DisplayName("without reason, message or cause")
                void testWithoutReasonOrMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .expectedOneOf(1, 2, 3)
                            .build();

                    assertEquals("expected: one of <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with cause, without reason or message")
                void testWithCauseWithoutReasonOrMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .expectedOneOf(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("expected: one of <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, without reason or cause")
                void testWithMessageWithoutReasonOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .expectedOneOf(1, 2, 3)
                            .build();

                    assertEquals("some message ==> expected: one of <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message and cause, without reason")
                void testWithMessageAndCauseWithoutReason() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .expectedOneOf(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> expected: one of <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason, without message or cause")
                void testWithReasonWithoutMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .expectedOneOf(1, 2, 3)
                            .build();

                    assertEquals("some reason, expected: one of <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and cause, without message")
                void testWithReasonAndCauseWithoutMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .expectedOneOf(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some reason, expected: one of <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and message, without cause")
                void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .expectedOneOf(1, 2, 3)
                            .build();

                    assertEquals("some message ==> some reason, expected: one of <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, reason and cause")
                void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .expectedOneOf(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> some reason, expected: one of <1>, <2>, <3> but was: <null>", error.getMessage());
                    assertExpected(error, Arrays.asList(1, 2, 3));
                    assertActual(error, null);
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }
            }
        }

        @Nested
        @DisplayName("with actual, without expected")
        class WithActualWithoutExpected {

            @Nested
            @DisplayName("single value without prefix")
            class SingleValueWithoutPrefix {

                @Test
                @DisplayName("without reason, message or cause")
                void testWithoutReasonOrMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .actual("actual value")
                            .build();

                    assertEquals("expected: <null> but was: <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertNull(error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .actual("actual value")
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with cause, without reason or message")
                void testWithCauseWithoutReasonOrMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .actual("actual value")
                            .cause(cause)
                            .build();

                    assertEquals("expected: <null> but was: <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertSame(cause, error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .actual("actual value")
                            .cause(cause)
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with message, without reason or cause")
                void testWithMessageWithoutReasonOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .actual("actual value")
                            .build();

                    assertEquals("some message ==> expected: <null> but was: <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertNull(error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .message("some message")
                            .actual("actual value")
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with message and cause, without reason")
                void testWithMessageAndCauseWithoutReason() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .actual("actual value")
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> expected: <null> but was: <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertSame(cause, error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .message("some message")
                            .actual("actual value")
                            .cause(cause)
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with reason, without message or cause")
                void testWithReasonWithoutMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .actual("actual value")
                            .build();

                    assertEquals("some reason, expected: <null> but was: <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertNull(error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .reason("some reason")
                            .actual("actual value")
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with reason and cause, without message")
                void testWithReasonAndCauseWithoutMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .actual("actual value")
                            .cause(cause)
                            .build();

                    assertEquals("some reason, expected: <null> but was: <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertSame(cause, error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .reason("some reason")
                            .actual("actual value")
                            .cause(cause)
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with reason and message, without cause")
                void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .actual("actual value")
                            .build();

                    assertEquals("some message ==> some reason, expected: <null> but was: <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertNull(error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .message("some message")
                            .reason("some reason")
                            .actual("actual value")
                            .build();

                    assertEqualErrors(junitError, error);
                }

                @Test
                @DisplayName("with message, reason and cause")
                void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .actual("actual value")
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> some reason, expected: <null> but was: <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertSame(cause, error.getCause());

                    AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                            .message("some message")
                            .reason("some reason")
                            .actual("actual value")
                            .cause(cause)
                            .build();

                    assertEqualErrors(junitError, error);
                }
            }

            @Nested
            @DisplayName("single value with prefix")
            class SingleValueWithPrefix {

                @Test
                @DisplayName("without reason, message or cause")
                void testWithoutReasonOrMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .prefixed("like").actual("actual value")
                            .build();

                    assertEquals("expected: <null> but was: like <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with cause, without reason or message")
                void testWithCauseWithoutReasonOrMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .prefixed("like").actual("actual value")
                            .cause(cause)
                            .build();

                    assertEquals("expected: <null> but was: like <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, without reason or cause")
                void testWithMessageWithoutReasonOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .prefixed("like").actual("actual value")
                            .build();

                    assertEquals("some message ==> expected: <null> but was: like <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message and cause, without reason")
                void testWithMessageAndCauseWithoutReason() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .prefixed("like").actual("actual value")
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> expected: <null> but was: like <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason, without message or cause")
                void testWithReasonWithoutMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .prefixed("like").actual("actual value")
                            .build();

                    assertEquals("some reason, expected: <null> but was: like <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and cause, without message")
                void testWithReasonAndCauseWithoutMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .prefixed("like").actual("actual value")
                            .cause(cause)
                            .build();

                    assertEquals("some reason, expected: <null> but was: like <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and message, without cause")
                void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .prefixed("like").actual("actual value")
                            .build();

                    assertEquals("some message ==> some reason, expected: <null> but was: like <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, reason and cause")
                void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .prefixed("like").actual("actual value")
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> some reason, expected: <null> but was: like <actual value>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, "actual value");
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }
            }

            @Nested
            @DisplayName("multiple values without prefix")
            class MultipleValuesWithoutPrefix {

                @Test
                @DisplayName("without reason, message or cause")
                void testWithoutReasonOrMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .actualValues(1, 2, 3)
                            .build();

                    assertEquals("expected: <null> but was: <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with cause, without reason or message")
                void testWithCauseWithoutReasonOrMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .actualValues(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("expected: <null> but was: <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, without reason or cause")
                void testWithMessageWithoutReasonOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .actualValues(1, 2, 3)
                            .build();

                    assertEquals("some message ==> expected: <null> but was: <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message and cause, without reason")
                void testWithMessageAndCauseWithoutReason() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .actualValues(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> expected: <null> but was: <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason, without message or cause")
                void testWithReasonWithoutMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .actualValues(1, 2, 3)
                            .build();

                    assertEquals("some reason, expected: <null> but was: <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and cause, without message")
                void testWithReasonAndCauseWithoutMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .actualValues(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some reason, expected: <null> but was: <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and message, without cause")
                void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .actualValues(1, 2, 3)
                            .build();

                    assertEquals("some message ==> some reason, expected: <null> but was: <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, reason and cause")
                void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .actualValues(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> some reason, expected: <null> but was: <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }
            }

            @Nested
            @DisplayName("multiple values with prefix")
            class MultipleValuesWithPrefix {

                @Test
                @DisplayName("without reason, message or cause")
                void testWithoutReasonOrMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .prefixed("in order").actualValues(1, 2, 3)
                            .build();

                    assertEquals("expected: <null> but was: in order <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with cause, without reason or message")
                void testWithCauseWithoutReasonOrMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .prefixed("in order").actualValues(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("expected: <null> but was: in order <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, without reason or cause")
                void testWithMessageWithoutReasonOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .prefixed("in order").actualValues(1, 2, 3)
                            .build();

                    assertEquals("some message ==> expected: <null> but was: in order <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message and cause, without reason")
                void testWithMessageAndCauseWithoutReason() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .prefixed("in order").actualValues(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> expected: <null> but was: in order <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason, without message or cause")
                void testWithReasonWithoutMessageOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .prefixed("in order").actualValues(1, 2, 3)
                            .build();

                    assertEquals("some reason, expected: <null> but was: in order <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and cause, without message")
                void testWithReasonAndCauseWithoutMessage() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .reason("some reason")
                            .prefixed("in order").actualValues(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some reason, expected: <null> but was: in order <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with reason and message, without cause")
                void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .prefixed("in order").actualValues(1, 2, 3)
                            .build();

                    assertEquals("some message ==> some reason, expected: <null> but was: in order <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertNull(error.getCause());

                    // No JUnit equivalent
                }

                @Test
                @DisplayName("with message, reason and cause")
                void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                    IOException cause = new IOException();

                    AssertionFailedError error = assertionFailedError()
                            .message("some message")
                            .reason("some reason")
                            .prefixed("in order").actualValues(1, 2, 3)
                            .cause(cause)
                            .build();

                    assertEquals("some message ==> some reason, expected: <null> but was: in order <1>, <2>, <3>", error.getMessage());
                    assertExpected(error, null);
                    assertActual(error, Arrays.asList(1, 2, 3));
                    assertSame(cause, error.getCause());

                    // No JUnit equivalent
                }
            }
        }

        @Nested
        @DisplayName("with expected and actual")
        class WithExpectedAndActual {

            // No need to test single vs multiple again

            @Test
            @DisplayName("without reason, message or cause")
            void testWithoutReasonOrMessageOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .expected("expected value")
                        .actual("actual value")
                        .build();

                assertEquals("expected: <expected value> but was: <actual value>", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .expected("expected value")
                        .actual("actual value")
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with cause, without reason or message")
            void testWithCauseWithoutReasonOrMessage() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .expected("expected value")
                        .actual("actual value")
                        .cause(cause)
                        .build();

                assertEquals("expected: <expected value> but was: <actual value>", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .expected("expected value")
                        .actual("actual value")
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message, without reason or cause")
            void testWithMessageWithoutReasonOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expected("expected value")
                        .actual("actual value")
                        .build();

                assertEquals("some message ==> expected: <expected value> but was: <actual value>", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .expected("expected value")
                        .actual("actual value")
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message and cause, without reason")
            void testWithMessageAndCauseWithoutReason() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expected("expected value")
                        .actual("actual value")
                        .cause(cause)
                        .build();

                assertEquals("some message ==> expected: <expected value> but was: <actual value>", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .expected("expected value")
                        .actual("actual value")
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason, without message or cause")
            void testWithReasonWithoutMessageOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .build();

                assertEquals("some reason, expected: <expected value> but was: <actual value>", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason and cause, without message")
            void testWithReasonAndCauseWithoutMessage() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .cause(cause)
                        .build();

                assertEquals("some reason, expected: <expected value> but was: <actual value>", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason and message, without cause")
            void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .build();

                assertEquals("some message ==> some reason, expected: <expected value> but was: <actual value>", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message, reason and cause")
            void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .cause(cause)
                        .build();

                assertEquals("some message ==> some reason, expected: <expected value> but was: <actual value>", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }
        }

        @Nested
        @DisplayName("with values excluded in message")
        class WithValuesExcludedInMessage {

            // No need to test single vs multiple again

            @Test
            @DisplayName("without reason, message or cause")
            void testWithoutReasonOrMessageOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .build();

                assertEquals("", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with cause, without reason or message")
            void testWithCauseWithoutReasonOrMessage() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .cause(cause)
                        .build();

                assertEquals("", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message, without reason or cause")
            void testWithMessageWithoutReasonOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .build();

                assertEquals("some message", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message and cause, without reason")
            void testWithMessageAndCauseWithoutReason() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .cause(cause)
                        .build();

                assertEquals("some message", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason, without message or cause")
            void testWithReasonWithoutMessageOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .build();

                assertEquals("some reason", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason and cause, without message")
            void testWithReasonAndCauseWithoutMessage() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .cause(cause)
                        .build();

                assertEquals("some reason", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason and message, without cause")
            void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .build();

                assertEquals("some message ==> some reason", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message, reason and cause")
            void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .cause(cause)
                        .build();

                assertEquals("some message ==> some reason", error.getMessage());
                assertExpected(error, "expected value");
                assertActual(error, "actual value");
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .reason("some reason")
                        .expected("expected value")
                        .actual("actual value")
                        .includeValuesInMessage(false)
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }
        }

        @Nested
        @DisplayName("with expected and actual with same toString")
        class WithExpectedAndActualWithSameToString {

            @Test
            @DisplayName("without reason, message or cause")
            void testWithoutReasonOrMessageOrCause() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);

                AssertionFailedError error = assertionFailedError()
                        .expected(expected)
                        .actual(actual)
                        .build();

                assertEquals(String.format("expected: java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, expected);
                assertActual(error, actual);
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .expected(expected)
                        .actual(actual)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with cause, without reason or message")
            void testWithCauseWithoutReasonOrMessage() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .expected(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEquals(String.format("expected: java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, expected);
                assertActual(error, actual);
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .expected(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message, without reason or cause")
            void testWithMessageWithoutReasonOrCause() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expected(expected)
                        .actual(actual)
                        .build();

                assertEquals(String.format("some message ==> expected: java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, expected);
                assertActual(error, actual);
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .expected(expected)
                        .actual(actual)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message and cause, without reason")
            void testWithMessageAndCauseWithoutReason() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expected(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEquals(String.format("some message ==> expected: java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, expected);
                assertActual(error, actual);
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .expected(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason, without message or cause")
            void testWithReasonWithoutMessageOrCause() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);

                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expected(expected)
                        .actual(actual)
                        .build();

                assertEquals(String.format("some reason, expected: java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, expected);
                assertActual(error, actual);
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .reason("some reason")
                        .expected(expected)
                        .actual(actual)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason and cause, without message")
            void testWithReasonAndCauseWithoutMessage() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expected(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEquals(String.format("some reason, expected: java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, expected);
                assertActual(error, actual);
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .reason("some reason")
                        .expected(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with reason and message, without cause")
            void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expected(expected)
                        .actual(actual)
                        .build();

                assertEquals(String.format(
                        "some message ==> some reason, expected: java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, expected);
                assertActual(error, actual);
                assertNull(error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .reason("some reason")
                        .expected(expected)
                        .actual(actual)
                        .build();

                assertEqualErrors(junitError, error);
            }

            @Test
            @DisplayName("with message, reason and cause")
            void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expected(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEquals(String.format(
                        "some message ==> some reason, expected: java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, expected);
                assertActual(error, actual);
                assertSame(cause, error.getCause());

                AssertionFailedError junitError = AssertionFailureBuilder.assertionFailure()
                        .message("some message")
                        .reason("some reason")
                        .expected(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEqualErrors(junitError, error);
            }
        }

        @Nested
        @DisplayName("with actual toString in multiple expected toStrings")
        class WithActualToStringInMultipleExpectedToString {

            @Test
            @DisplayName("without reason, message or cause")
            void testWithoutReasonOrMessageOrCause() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);

                AssertionFailedError error = assertionFailedError()
                        .expectedOneOf(expected)
                        .actual(actual)
                        .build();

                assertEquals(String.format("expected: one of java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected));
                assertActual(error, actual);
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with cause, without reason or message")
            void testWithCauseWithoutReasonOrMessage() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .expectedOneOf(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEquals(String.format("expected: one of java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected));
                assertActual(error, actual);
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with message, without reason or cause")
            void testWithMessageWithoutReasonOrCause() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expectedOneOf(expected)
                        .actual(actual)
                        .build();

                assertEquals(String.format(
                        "some message ==> expected: one of java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected));
                assertActual(error, actual);
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with message and cause, without reason")
            void testWithMessageAndCauseWithoutReason() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expectedOneOf(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEquals(String.format(
                        "some message ==> expected: one of java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected));
                assertActual(error, actual);
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with reason, without message or cause")
            void testWithReasonWithoutMessageOrCause() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);

                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expectedOneOf(expected)
                        .actual(actual)
                        .build();

                assertEquals(String.format(
                        "some reason, expected: one of java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected));
                assertActual(error, actual);
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with reason and cause, without message")
            void testWithReasonAndCauseWithoutMessage() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expectedOneOf(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEquals(String.format(
                        "some reason, expected: one of java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected));
                assertActual(error, actual);
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with reason and message, without cause")
            void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expectedOneOf(expected)
                        .actual(actual)
                        .build();

                assertEquals(String.format(
                        "some message ==> some reason, expected: one of java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected));
                assertActual(error, actual);
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with message, reason and cause")
            void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                String expected = "2020-01-01";
                LocalDate actual = LocalDate.of(2020, 1, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expectedOneOf(expected)
                        .actual(actual)
                        .cause(cause)
                        .build();

                assertEquals(String.format(
                        "some message ==> some reason, expected: one of java.lang.String@%x<2020-01-01> but was: java.time.LocalDate@%x<2020-01-01>",
                        System.identityHashCode(expected), System.identityHashCode(actual)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected));
                assertActual(error, actual);
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }
        }

        @Nested
        @DisplayName("with multiple expected and actual with same toString")
        class WithMultipleExpectedAndActualWithSameToString {

            @Test
            @DisplayName("without reason, message or cause")
            void testWithoutReasonOrMessageOrCause() {
                String expected1 = "2020-01-01";
                String expected2 = "2020-02-01";
                LocalDate actual1 = LocalDate.of(2020, 1, 1);
                LocalDate actual2 = LocalDate.of(2020, 2, 1);

                AssertionFailedError error = assertionFailedError()
                        .expectedOneOf(expected1, expected2)
                        .actualValues(actual1, actual2)
                        .build();

                assertEquals(String.format("expected: one of java.lang.String@%x<2020-01-01>, java.lang.String@%x<2020-02-01> "
                        + "but was: java.time.LocalDate@%x<2020-01-01>, java.time.LocalDate@%x<2020-02-01>",
                        System.identityHashCode(expected1), System.identityHashCode(expected2),
                        System.identityHashCode(actual1), System.identityHashCode(actual2)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected1, expected2));
                assertActual(error, Arrays.asList(actual1, actual2));
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with cause, without reason or message")
            void testWithCauseWithoutReasonOrMessage() {
                String expected1 = "2020-01-01";
                String expected2 = "2020-02-01";
                LocalDate actual1 = LocalDate.of(2020, 1, 1);
                LocalDate actual2 = LocalDate.of(2020, 2, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .expectedOneOf(expected1, expected2)
                        .actualValues(actual1, actual2)
                        .cause(cause)
                        .build();

                assertEquals(String.format("expected: one of java.lang.String@%x<2020-01-01>, java.lang.String@%x<2020-02-01> "
                        + "but was: java.time.LocalDate@%x<2020-01-01>, java.time.LocalDate@%x<2020-02-01>",
                        System.identityHashCode(expected1), System.identityHashCode(expected2),
                        System.identityHashCode(actual1), System.identityHashCode(actual2)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected1, expected2));
                assertActual(error, Arrays.asList(actual1, actual2));
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with message, without reason or cause")
            void testWithMessageWithoutReasonOrCause() {
                String expected1 = "2020-01-01";
                String expected2 = "2020-02-01";
                LocalDate actual1 = LocalDate.of(2020, 1, 1);
                LocalDate actual2 = LocalDate.of(2020, 2, 1);

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expectedOneOf(expected1, expected2)
                        .actualValues(actual1, actual2)
                        .build();

                assertEquals(String.format("some message ==> expected: one of java.lang.String@%x<2020-01-01>, java.lang.String@%x<2020-02-01> "
                        + "but was: java.time.LocalDate@%x<2020-01-01>, java.time.LocalDate@%x<2020-02-01>",
                        System.identityHashCode(expected1), System.identityHashCode(expected2),
                        System.identityHashCode(actual1), System.identityHashCode(actual2)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected1, expected2));
                assertActual(error, Arrays.asList(actual1, actual2));
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with message and cause, without reason")
            void testWithMessageAndCauseWithoutReason() {
                String expected1 = "2020-01-01";
                String expected2 = "2020-02-01";
                LocalDate actual1 = LocalDate.of(2020, 1, 1);
                LocalDate actual2 = LocalDate.of(2020, 2, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expectedOneOf(expected1, expected2)
                        .actualValues(actual1, actual2)
                        .cause(cause)
                        .build();

                assertEquals(String.format("some message ==> expected: one of java.lang.String@%x<2020-01-01>, java.lang.String@%x<2020-02-01> "
                        + "but was: java.time.LocalDate@%x<2020-01-01>, java.time.LocalDate@%x<2020-02-01>",
                        System.identityHashCode(expected1), System.identityHashCode(expected2),
                        System.identityHashCode(actual1), System.identityHashCode(actual2)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected1, expected2));
                assertActual(error, Arrays.asList(actual1, actual2));
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with reason, without message or cause")
            void testWithReasonWithoutMessageOrCause() {
                String expected1 = "2020-01-01";
                String expected2 = "2020-02-01";
                LocalDate actual1 = LocalDate.of(2020, 1, 1);
                LocalDate actual2 = LocalDate.of(2020, 2, 1);

                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expectedOneOf(expected1, expected2)
                        .actualValues(actual1, actual2)
                        .build();

                assertEquals(String.format("some reason, expected: one of java.lang.String@%x<2020-01-01>, java.lang.String@%x<2020-02-01> "
                        + "but was: java.time.LocalDate@%x<2020-01-01>, java.time.LocalDate@%x<2020-02-01>",
                        System.identityHashCode(expected1), System.identityHashCode(expected2),
                        System.identityHashCode(actual1), System.identityHashCode(actual2)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected1, expected2));
                assertActual(error, Arrays.asList(actual1, actual2));
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with reason and cause, without message")
            void testWithReasonAndCauseWithoutMessage() {
                String expected1 = "2020-01-01";
                String expected2 = "2020-02-01";
                LocalDate actual1 = LocalDate.of(2020, 1, 1);
                LocalDate actual2 = LocalDate.of(2020, 2, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expectedOneOf(expected1, expected2)
                        .actualValues(actual1, actual2)
                        .cause(cause)
                        .build();

                assertEquals(String.format("some reason, expected: one of java.lang.String@%x<2020-01-01>, java.lang.String@%x<2020-02-01> "
                        + "but was: java.time.LocalDate@%x<2020-01-01>, java.time.LocalDate@%x<2020-02-01>",
                        System.identityHashCode(expected1), System.identityHashCode(expected2),
                        System.identityHashCode(actual1), System.identityHashCode(actual2)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected1, expected2));
                assertActual(error, Arrays.asList(actual1, actual2));
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with reason and message, without cause")
            void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                String expected1 = "2020-01-01";
                String expected2 = "2020-02-01";
                LocalDate actual1 = LocalDate.of(2020, 1, 1);
                LocalDate actual2 = LocalDate.of(2020, 2, 1);

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expectedOneOf(expected1, expected2)
                        .actualValues(actual1, actual2)
                        .build();

                assertEquals(String.format(
                        "some message ==> some reason, expected: one of java.lang.String@%x<2020-01-01>, java.lang.String@%x<2020-02-01> "
                        + "but was: java.time.LocalDate@%x<2020-01-01>, java.time.LocalDate@%x<2020-02-01>",
                        System.identityHashCode(expected1), System.identityHashCode(expected2),
                        System.identityHashCode(actual1), System.identityHashCode(actual2)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected1, expected2));
                assertActual(error, Arrays.asList(actual1, actual2));
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with message, reason and cause")
            void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                String expected1 = "2020-01-01";
                String expected2 = "2020-02-01";
                LocalDate actual1 = LocalDate.of(2020, 1, 1);
                LocalDate actual2 = LocalDate.of(2020, 2, 1);
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expectedOneOf(expected1, expected2)
                        .actualValues(actual1, actual2)
                        .cause(cause)
                        .build();

                assertEquals(String.format(
                        "some message ==> some reason, expected: one of java.lang.String@%x<2020-01-01>, java.lang.String@%x<2020-02-01> "
                        + "but was: java.time.LocalDate@%x<2020-01-01>, java.time.LocalDate@%x<2020-02-01>",
                        System.identityHashCode(expected1), System.identityHashCode(expected2),
                        System.identityHashCode(actual1), System.identityHashCode(actual2)),
                        error.getMessage());
                assertExpected(error, Arrays.asList(expected1, expected2));
                assertActual(error, Arrays.asList(actual1, actual2));
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }
        }

        @Nested
        @DisplayName("with expected message")
        class WithExpectedMessage {

            @Test
            @DisplayName("without reason, message or cause")
            void testWithoutReasonOrMessageOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .expectedMessage("matching predicate")
                        .actual("foo")
                        .build();

                assertEquals("expected: matching predicate but was: <foo>", error.getMessage());
                assertExpected(error, "matching predicate");
                assertActual(error, "foo");
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with cause, without reason or message")
            void testWithCauseWithoutReasonOrMessage() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .expectedMessage("matching predicate")
                        .actual("foo")
                        .cause(cause)
                        .build();

                assertEquals("expected: matching predicate but was: <foo>", error.getMessage());
                assertExpected(error, "matching predicate");
                assertActual(error, "foo");
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with message, without reason or cause")
            void testWithMessageWithoutReasonOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expectedMessage("matching predicate")
                        .actual("foo")
                        .build();

                assertEquals("some message ==> expected: matching predicate but was: <foo>", error.getMessage());
                assertExpected(error, "matching predicate");
                assertActual(error, "foo");
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with message and cause, without reason")
            void testWithMessageAndCauseWithoutReason() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .expectedMessage("matching predicate")
                        .actual("foo")
                        .cause(cause)
                        .build();

                assertEquals("some message ==> expected: matching predicate but was: <foo>", error.getMessage());
                assertExpected(error, "matching predicate");
                assertActual(error, "foo");
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with reason, without message or cause")
            void testWithReasonWithoutMessageOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expectedMessage("matching predicate")
                        .actual("foo")
                        .build();

                assertEquals("some reason, expected: matching predicate but was: <foo>", error.getMessage());
                assertExpected(error, "matching predicate");
                assertActual(error, "foo");
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with reason and cause, without message")
            void testWithReasonAndCauseWithoutMessage() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .reason("some reason")
                        .expectedMessage("matching predicate")
                        .actual("foo")
                        .cause(cause)
                        .build();

                assertEquals("some reason, expected: matching predicate but was: <foo>", error.getMessage());
                assertExpected(error, "matching predicate");
                assertActual(error, "foo");
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with reason and message, without cause")
            void testWithReasonWithoutMessageOrExpectedOrActualOrCause() {
                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expectedMessage("matching predicate")
                        .actual("foo")
                        .build();

                assertEquals("some message ==> some reason, expected: matching predicate but was: <foo>", error.getMessage());
                assertExpected(error, "matching predicate");
                assertActual(error, "foo");
                assertNull(error.getCause());

                // No JUnit equivalent
            }

            @Test
            @DisplayName("with message, reason and cause")
            void testWithReasonAndCauseWithoutMessageOrExpectedOrActual() {
                IOException cause = new IOException();

                AssertionFailedError error = assertionFailedError()
                        .message("some message")
                        .reason("some reason")
                        .expectedMessage("matching predicate")
                        .actual("foo")
                        .cause(cause)
                        .build();

                assertEquals("some message ==> some reason, expected: matching predicate but was: <foo>", error.getMessage());
                assertExpected(error, "matching predicate");
                assertActual(error, "foo");
                assertSame(cause, error.getCause());

                // No JUnit equivalent
            }
        }

        @Nested
        @DisplayName("reasonPattern")
        class ReasonPattern {

            @Test
            @DisplayName("null pattern")
            void testNullPattern() {
                AssertionFailedErrorBuilder builder = assertionFailedError();

                assertThrows(NullPointerException.class, () -> builder.reasonPattern(null));
            }

            @Test
            @DisplayName("pattern and value mismatch")
            void testPatternAndValueMismatch() {
                ReasonBuilder builder = assertionFailedError()
                        .reasonPattern("some text and %s");

                assertThrows(IllegalFormatException.class, builder::format);

                builder = assertionFailedError()
                        .reasonPattern("some text and %d")
                        .withValue(1);

                assertThrows(IllegalFormatException.class, builder::format);
            }

            @Test
            @DisplayName("format")
            void testFormat() {
                AssertionFailedError error = assertionFailedError()
                        .reasonPattern("some text and single %s and multiple %s values")
                                .withValue("foo")
                                .withValues(1, 2, 3)
                                .format()
                        .build();

                assertEquals("some text and single <foo> and multiple <1>, <2>, <3> values", error.getMessage());
            }
        }

        private void assertEqualErrors(AssertionFailedError expected, AssertionFailedError actual) {
            assertEquals(expected.getMessage(), actual.getMessage());
            assertEqualValueWrappers(expected.getExpected(), actual.getExpected());
            assertEqualValueWrappers(expected.getActual(), actual.getActual());
            assertSame(expected.getCause(), actual.getCause());
        }

        private void assertEqualValueWrappers(ValueWrapper expected, ValueWrapper actual) {
            if (expected == null) {
                assertNull(actual);
            } else {
                assertEquals(expected.getEphemeralValue(), actual.getEphemeralValue());
                assertEquals(expected.getValue(), actual.getValue());
            }
        }

        private void assertExpected(AssertionFailedError error, Object expectedValue) {
            assertNotNull(error.getExpected());
            assertEquals(expectedValue, error.getExpected().getEphemeralValue());
        }

        private void assertActual(AssertionFailedError error, Object expectedValue) {
            assertNotNull(error.getActual());
            assertEquals(expectedValue, error.getActual().getEphemeralValue());
        }
    }

    @Nested
    @DisplayName("nullSafeGet")
    class NullSafeGet {

        @Test
        @DisplayName("null")
        void testNull() {
            assertEquals(null, nullSafeGet(null));
        }

        @Test
        @DisplayName("String")
        void testString() {
            assertEquals("foo", nullSafeGet("foo"));
        }

        @Test
        @DisplayName("Supplier supplying non-null")
        void testSupplierSupplyingNonNull() {
            Supplier<?> supplier = () -> "foo".toCharArray();
            assertEquals("[f, o, o]", nullSafeGet(supplier));
        }

        @Test
        @DisplayName("Supplier supplying null")
        void testSupplierSupplyingNull() {
            Supplier<String> supplier = () -> null;
            assertEquals("null", nullSafeGet(supplier));
        }

        @Test
        @DisplayName("Object")
        void testObject() {
            int[] array = { 1, 2, 3 };
            assertEquals("[1, 2, 3]", nullSafeGet(array));
        }
    }

    @Nested
    @DisplayName("isNotBlank")
    class IsNotBlank {

        @Test
        @DisplayName("null")
        void testNull() {
            assertFalse(isNotBlank(null));
        }

        @Test
        @DisplayName("empty")
        void testEmpty() {
            assertFalse(isNotBlank(""));
        }

        @Test
        @DisplayName("all whitespace")
        void testAllWhitespace() {
            assertFalse(isNotBlank(" \t\r\n"));
        }

        @Test
        @DisplayName("mostly whitespace")
        void testMostlyWhitespace() {
            assertTrue(isNotBlank("a \t\r\n"));
            assertTrue(isNotBlank(" a\t\r\n"));
            assertTrue(isNotBlank(" \ta\r\n"));
            assertTrue(isNotBlank(" \t\ra\n"));
            assertTrue(isNotBlank(" \t\r\na"));
        }

        @Test
        @DisplayName("no whitespace")
        void testNoWhitespace() {
            assertTrue(isNotBlank("a"));
            assertTrue(isNotBlank(getClass().getName()));
        }
    }

    @Nested
    @DisplayName("formatClassAndValue")
    class FormatClassAndValue {

        @Test
        @DisplayName("null")
        void testNull() {
            assertEquals("<null>", formatClassAndValue(null, "unused"));
        }

        @Test
        @DisplayName("Class")
        void testClass() {
            String expected = String.format("<%s@%x>", getClass().getCanonicalName(), getClass().hashCode());
            assertEquals(expected, formatClassAndValue(getClass(), "unused"));
        }

        @Test
        @DisplayName("other")
        void testOther() {
            String expected = String.format("%s@%x<%s>", Integer.class.getName(), System.identityHashCode(1), 1);
            assertEquals(expected, formatClassAndValue(1, "1"));
        }
    }

    @Nested
    @DisplayName("objectToString")
    class ObjectToString {

        @Test
        @DisplayName("null")
        void testNull() {
            assertEquals("null", objectToString(null));
        }

        @Test
        @DisplayName("Class")
        void testClass() {
            assertEquals(getClass().getCanonicalName(), objectToString(getClass()));
        }

        @Test
        @DisplayName("boolean[]")
        void testBooleanArray() {
            boolean[] array = { true, false };
            assertEquals("[true, false]", objectToString(array));
        }

        @Test
        @DisplayName("char[]")
        void testCharArray() {
            char[] array = { 'A', 'B', 'C' };
            assertEquals("[A, B, C]", objectToString(array));
        }

        @Test
        @DisplayName("byte[]")
        void testByteArray() {
            byte[] array = { 1, 2, 3 };
            assertEquals("[1, 2, 3]", objectToString(array));
        }

        @Test
        @DisplayName("short[]")
        void testShortArray() {
            short[] array = { 1, 2, 3 };
            assertEquals("[1, 2, 3]", objectToString(array));
        }

        @Test
        @DisplayName("int[]")
        void testIntArray() {
            int[] array = { 1, 2, 3 };
            assertEquals("[1, 2, 3]", objectToString(array));
        }

        @Test
        @DisplayName("long[]")
        void testLongArray() {
            long[] array = { 1, 2, 3 };
            assertEquals("[1, 2, 3]", objectToString(array));
        }

        @Test
        @DisplayName("float[]")
        void testFloatArray() {
            float[] array = { 1, 2, 3 };
            assertEquals("[1.0, 2.0, 3.0]", objectToString(array));
        }

        @Test
        @DisplayName("double[]")
        void testDoubleArray() {
            double[] array = { 1, 2, 3 };
            assertEquals("[1.0, 2.0, 3.0]", objectToString(array));
        }

        @Test
        @DisplayName("Object[]")
        void testObjectArray() {
            Object[][] array = {
                    { "foo", "bar" },
                    { 1, 2, 3 },
            };
            assertEquals("[[foo, bar], [1, 2, 3]]", objectToString(array));
        }

        @Test
        @DisplayName("Object")
        void testObject() {
            assertEquals("1234.567", objectToString(new BigDecimal("1234.567")));

            assertEquals("null", objectToString(new Object() {
                @Override
                public String toString() {
                    return null;
                }
            }));
        }
    }
}
