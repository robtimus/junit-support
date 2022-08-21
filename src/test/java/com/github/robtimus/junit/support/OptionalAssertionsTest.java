/*
 * OptionalAssertionsTest.java
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

import static com.github.robtimus.junit.support.OptionalAssertions.assertIsEmpty;
import static com.github.robtimus.junit.support.OptionalAssertions.assertIsPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

@SuppressWarnings("nls")
class OptionalAssertionsTest {

    @Nested
    @DisplayName("assertIsPresent")
    class AssertIsPresent {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("Optional is present")
            void testOptionalIsPresent() {
                Optional<String> optional = Optional.of("foo");
                assertEquals("foo", assertIsPresent(optional));
            }

            @Test
            @DisplayName("Optional is empty")
            void testOptionalIsEmpty() {
                Optional<String> optional = Optional.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional));
                assertThat(error.getMessage(), startsWith("expected: not equal but was: <Optional.empty>"));
            }

            @Test
            @DisplayName("OptionalInt is present")
            void testOptionalIntIsPresent() {
                OptionalInt optional = OptionalInt.of(1);
                assertEquals(1, assertIsPresent(optional));
            }

            @Test
            @DisplayName("OptionalInt is empty")
            void testOptionalIntIsEmpty() {
                OptionalInt optional = OptionalInt.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional));
                assertThat(error.getMessage(), startsWith("expected: not equal but was: <OptionalInt.empty>"));
            }

            @Test
            @DisplayName("OptionalLong is present")
            void testOptionalLongIsPresent() {
                OptionalLong optional = OptionalLong.of(1);
                assertEquals(1, assertIsPresent(optional));
            }

            @Test
            @DisplayName("OptionalLong is empty")
            void testOptionalLongIsEmpty() {
                OptionalLong optional = OptionalLong.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional));
                assertThat(error.getMessage(), startsWith("expected: not equal but was: <OptionalLong.empty>"));
            }

            @Test
            @DisplayName("OptionalDouble is present")
            void testOptionalDoubleIsPresent() {
                OptionalDouble optional = OptionalDouble.of(1);
                assertEquals(1, assertIsPresent(optional));
            }

            @Test
            @DisplayName("OptionalDouble is empty")
            void testOptionalDoubleIsEmpty() {
                OptionalDouble optional = OptionalDouble.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional));
                assertThat(error.getMessage(), startsWith("expected: not equal but was: <OptionalDouble.empty>"));
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("Optional is present")
            void testOptionalIsPresent() {
                Optional<String> optional = Optional.of("foo");
                assertEquals("foo", assertIsPresent(optional, "error"));
            }

            @Test
            @DisplayName("Optional is empty")
            void testOptionalIsEmpty() {
                Optional<String> optional = Optional.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional, "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: not equal but was: <Optional.empty>"));
            }

            @Test
            @DisplayName("OptionalInt is present")
            void testOptionalIntIsPresent() {
                OptionalInt optional = OptionalInt.of(1);
                assertEquals(1, assertIsPresent(optional, "error"));
            }

            @Test
            @DisplayName("OptionalInt is empty")
            void testOptionalIntIsEmpty() {
                OptionalInt optional = OptionalInt.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional, "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: not equal but was: <OptionalInt.empty>"));
            }

            @Test
            @DisplayName("OptionalLong is present")
            void testOptionalLongIsPresent() {
                OptionalLong optional = OptionalLong.of(1);
                assertEquals(1, assertIsPresent(optional, "error"));
            }

            @Test
            @DisplayName("OptionalLong is empty")
            void testOptionalLongIsEmpty() {
                OptionalLong optional = OptionalLong.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional, "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: not equal but was: <OptionalLong.empty>"));
            }

            @Test
            @DisplayName("OptionalDouble is present")
            void testOptionalDoubleIsPresent() {
                OptionalDouble optional = OptionalDouble.of(1);
                assertEquals(1, assertIsPresent(optional, "error"));
            }

            @Test
            @DisplayName("OptionalDouble is empty")
            void testOptionalDoubleIsEmpty() {
                OptionalDouble optional = OptionalDouble.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional, "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: not equal but was: <OptionalDouble.empty>"));
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("Optional is present")
            void testOptionalIsPresent() {
                Optional<String> optional = Optional.of("foo");
                assertEquals("foo", assertIsPresent(optional, () -> "error"));
            }

            @Test
            @DisplayName("Optional is empty")
            void testOptionalIsEmpty() {
                Optional<String> optional = Optional.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional, () -> "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: not equal but was: <Optional.empty>"));
            }

            @Test
            @DisplayName("OptionalInt is present")
            void testOptionalIntIsPresent() {
                OptionalInt optional = OptionalInt.of(1);
                assertEquals(1, assertIsPresent(optional, () -> "error"));
            }

            @Test
            @DisplayName("OptionalInt is empty")
            void testOptionalIntIsEmpty() {
                OptionalInt optional = OptionalInt.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional, () -> "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: not equal but was: <OptionalInt.empty>"));
            }

            @Test
            @DisplayName("OptionalLong is present")
            void testOptionalLongIsPresent() {
                OptionalLong optional = OptionalLong.of(1);
                assertEquals(1, assertIsPresent(optional, () -> "error"));
            }

            @Test
            @DisplayName("OptionalLong is empty")
            void testOptionalLongIsEmpty() {
                OptionalLong optional = OptionalLong.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional, () -> "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: not equal but was: <OptionalLong.empty>"));
            }

            @Test
            @DisplayName("OptionalDouble is present")
            void testOptionalDoubleIsPresent() {
                OptionalDouble optional = OptionalDouble.of(1);
                assertEquals(1, assertIsPresent(optional, () -> "error"));
            }

            @Test
            @DisplayName("OptionalDouble is empty")
            void testOptionalDoubleIsEmpty() {
                OptionalDouble optional = OptionalDouble.empty();
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsPresent(optional, () -> "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: not equal but was: <OptionalDouble.empty>"));
            }
        }
    }

    @Nested
    @DisplayName("assertIsEmpty")
    class AssertIsEmpty {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("Optional is present")
            void testOptionalIsPresent() {
                Optional<String> optional = Optional.of("foo");
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional));
                assertThat(error.getMessage(), startsWith("expected: <Optional.empty> but was: <Optional[foo]>"));
            }

            @Test
            @DisplayName("Optional is empty")
            void testOptionalIsEmpty() {
                Optional<String> optional = Optional.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional));
            }

            @Test
            @DisplayName("OptionalInt is present")
            void testOptionalIntIsPresent() {
                OptionalInt optional = OptionalInt.of(1);
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional));
                assertThat(error.getMessage(), startsWith("expected: <OptionalInt.empty> but was: <OptionalInt[1]>"));
            }

            @Test
            @DisplayName("OptionalInt is empty")
            void testOptionalIntIsEmpty() {
                OptionalInt optional = OptionalInt.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional));
            }

            @Test
            @DisplayName("OptionalLong is present")
            void testOptionalLongIsPresent() {
                OptionalLong optional = OptionalLong.of(1);
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional));
                assertThat(error.getMessage(), startsWith("expected: <OptionalLong.empty> but was: <OptionalLong[1]>"));
            }

            @Test
            @DisplayName("OptionalLong is empty")
            void testOptionalLongIsEmpty() {
                OptionalLong optional = OptionalLong.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional));
            }

            @Test
            @DisplayName("OptionalDouble is present")
            void testOptionalDoubleIsPresent() {
                OptionalDouble optional = OptionalDouble.of(1);
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional));
                assertThat(error.getMessage(), startsWith("expected: <OptionalDouble.empty> but was: <OptionalDouble[1.0]>"));
            }

            @Test
            @DisplayName("OptionalDouble is empty")
            void testOptionalDoubleIsEmpty() {
                OptionalDouble optional = OptionalDouble.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional));
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("Optional is present")
            void testOptionalIsPresent() {
                Optional<String> optional = Optional.of("foo");
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional, "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: <Optional.empty> but was: <Optional[foo]>"));
            }

            @Test
            @DisplayName("Optional is empty")
            void testOptionalIsEmpty() {
                Optional<String> optional = Optional.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional, "error"));
            }

            @Test
            @DisplayName("OptionalInt is present")
            void testOptionalIntIsPresent() {
                OptionalInt optional = OptionalInt.of(1);
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional, "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: <OptionalInt.empty> but was: <OptionalInt[1]>"));
            }

            @Test
            @DisplayName("OptionalInt is empty")
            void testOptionalIntIsEmpty() {
                OptionalInt optional = OptionalInt.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional, "error"));
            }

            @Test
            @DisplayName("OptionalLong is present")
            void testOptionalLongIsPresent() {
                OptionalLong optional = OptionalLong.of(1);
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional, "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: <OptionalLong.empty> but was: <OptionalLong[1]>"));
            }

            @Test
            @DisplayName("OptionalLong is empty")
            void testOptionalLongIsEmpty() {
                OptionalLong optional = OptionalLong.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional, "error"));
            }

            @Test
            @DisplayName("OptionalDouble is present")
            void testOptionalDoubleIsPresent() {
                OptionalDouble optional = OptionalDouble.of(1);
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional, "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: <OptionalDouble.empty> but was: <OptionalDouble[1.0]>"));
            }

            @Test
            @DisplayName("OptionalDouble is empty")
            void testOptionalDoubleIsEmpty() {
                OptionalDouble optional = OptionalDouble.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional, "error"));
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("Optional is present")
            void testOptionalIsPresent() {
                Optional<String> optional = Optional.of("foo");
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional, () -> "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: <Optional.empty> but was: <Optional[foo]>"));
            }

            @Test
            @DisplayName("Optional is empty")
            void testOptionalIsEmpty() {
                Optional<String> optional = Optional.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional, () -> "error"));
            }

            @Test
            @DisplayName("OptionalInt is present")
            void testOptionalIntIsPresent() {
                OptionalInt optional = OptionalInt.of(1);
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional, () -> "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: <OptionalInt.empty> but was: <OptionalInt[1]>"));
            }

            @Test
            @DisplayName("OptionalInt is empty")
            void testOptionalIntIsEmpty() {
                OptionalInt optional = OptionalInt.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional, () -> "error"));
            }

            @Test
            @DisplayName("OptionalLong is present")
            void testOptionalLongIsPresent() {
                OptionalLong optional = OptionalLong.of(1);
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional, () -> "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: <OptionalLong.empty> but was: <OptionalLong[1]>"));
            }

            @Test
            @DisplayName("OptionalLong is empty")
            void testOptionalLongIsEmpty() {
                OptionalLong optional = OptionalLong.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional, () -> "error"));
            }

            @Test
            @DisplayName("OptionalDouble is present")
            void testOptionalDoubleIsPresent() {
                OptionalDouble optional = OptionalDouble.of(1);
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIsEmpty(optional, () -> "error"));
                assertThat(error.getMessage(), startsWith("error ==> expected: <OptionalDouble.empty> but was: <OptionalDouble[1.0]>"));
            }

            @Test
            @DisplayName("OptionalDouble is empty")
            void testOptionalDoubleIsEmpty() {
                OptionalDouble optional = OptionalDouble.empty();
                assertDoesNotThrow(() -> assertIsEmpty(optional, () -> "error"));
            }
        }
    }
}
