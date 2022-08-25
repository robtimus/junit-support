/*
 * PredicateAssertionsTest.java
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

import static com.github.robtimus.junit.support.PredicateAssertions.assertDoesNotMatch;
import static com.github.robtimus.junit.support.PredicateAssertions.assertDoubleDoesNotMatch;
import static com.github.robtimus.junit.support.PredicateAssertions.assertDoubleMatches;
import static com.github.robtimus.junit.support.PredicateAssertions.assertIntDoesNotMatch;
import static com.github.robtimus.junit.support.PredicateAssertions.assertIntMatches;
import static com.github.robtimus.junit.support.PredicateAssertions.assertLongDoesNotMatch;
import static com.github.robtimus.junit.support.PredicateAssertions.assertLongMatches;
import static com.github.robtimus.junit.support.PredicateAssertions.assertMatches;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

@SuppressWarnings("nls")
class PredicateAssertionsTest {

    @Nested
    @DisplayName("assertMatches")
    class AssertMatches {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertMatches(String::isEmpty, ""));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertMatches(String::isEmpty, "foo"));

                assertEquals("expected: matching predicate but was: <foo>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertMatches(String::isEmpty, "", "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertMatches(String::isEmpty, "foo", "error"));

                assertEquals("error ==> expected: matching predicate but was: <foo>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertMatches(String::isEmpty, "", () -> "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertMatches(String::isEmpty, "foo", () -> "error"));

                assertEquals("error ==> expected: matching predicate but was: <foo>", error.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("assertIntMatches")
    class AssertIntMatchesInt {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertIntMatches(i -> i > 0, 1));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIntMatches(i -> i > 0, 0));

                assertEquals("expected: matching predicate but was: <0>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertIntMatches(i -> i > 0, 1, "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIntMatches(i -> i > 0, 0, "error"));

                assertEquals("error ==> expected: matching predicate but was: <0>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertIntMatches(i -> i > 0, 1, () -> "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIntMatches(i -> i > 0, 0, () -> "error"));

                assertEquals("error ==> expected: matching predicate but was: <0>", error.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("assertLongMatches")
    class AssertLongMatches {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertLongMatches(i -> i > 0L, 1L));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertLongMatches(i -> i > 0L, 0L));

                assertEquals("expected: matching predicate but was: <0>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertLongMatches(i -> i > 0L, 1L, "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertLongMatches(i -> i > 0L, 0L, "error"));

                assertEquals("error ==> expected: matching predicate but was: <0>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertLongMatches(i -> i > 0L, 1L, () -> "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertLongMatches(i -> i > 0L, 0L, () -> "error"));

                assertEquals("error ==> expected: matching predicate but was: <0>", error.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("assertDoubleMatches")
    class AssertDoubleMatches {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertDoubleMatches(Double::isNaN, Double.NaN));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoubleMatches(Double::isNaN, 1.0));

                assertEquals("expected: matching predicate but was: <1.0>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertDoubleMatches(Double::isNaN, Double.NaN, "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoubleMatches(Double::isNaN, 1.0, "error"));

                assertEquals("error ==> expected: matching predicate but was: <1.0>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertDoubleMatches(Double::isNaN, Double.NaN, () -> "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoubleMatches(Double::isNaN, 1.0, () -> "error"));

                assertEquals("error ==> expected: matching predicate but was: <1.0>", error.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("assertDoesNotMatch")
    class AssertDoesNotMatch {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertDoesNotMatch(String::isEmpty, "foo"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoesNotMatch(String::isEmpty, ""));

                assertEquals("expected: not matching predicate but was: <>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertDoesNotMatch(String::isEmpty, "foo", "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoesNotMatch(String::isEmpty, "", "error"));

                assertEquals("error ==> expected: not matching predicate but was: <>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertDoesNotMatch(String::isEmpty, "foo", () -> "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoesNotMatch(String::isEmpty, "", () -> "error"));

                assertEquals("error ==> expected: not matching predicate but was: <>", error.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("assertIntDoesNotMatch")
    class AssertIntDoesNotMatchInt {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertIntDoesNotMatch(i -> i > 0, 0));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIntDoesNotMatch(i -> i > 0, 1));

                assertEquals("expected: not matching predicate but was: <1>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertIntDoesNotMatch(i -> i > 0, 0, "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIntDoesNotMatch(i -> i > 0, 1, "error"));

                assertEquals("error ==> expected: not matching predicate but was: <1>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertIntDoesNotMatch(i -> i > 0, 0, () -> "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertIntDoesNotMatch(i -> i > 0, 1, () -> "error"));

                assertEquals("error ==> expected: not matching predicate but was: <1>", error.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("assertLongDoesNotMatch")
    class AssertLongDoesNotMatch {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertLongDoesNotMatch(i -> i > 0L, 0L));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertLongDoesNotMatch(i -> i > 0L, 1L));

                assertEquals("expected: not matching predicate but was: <1>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertLongDoesNotMatch(i -> i > 0L, 0L, "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertLongDoesNotMatch(i -> i > 0L, 1L, "error"));

                assertEquals("error ==> expected: not matching predicate but was: <1>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertLongDoesNotMatch(i -> i > 0L, 0L, () -> "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertLongDoesNotMatch(i -> i > 0L, 1L, () -> "error"));

                assertEquals("error ==> expected: not matching predicate but was: <1>", error.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("assertDoubleDoesNotMatch")
    class AssertDoubleDoesNotMatch {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertDoubleDoesNotMatch(Double::isNaN, 1.0));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertDoubleDoesNotMatch(Double::isNaN, Double.NaN));

                assertEquals("expected: not matching predicate but was: <NaN>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertDoubleDoesNotMatch(Double::isNaN, 1.0, "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertDoubleDoesNotMatch(Double::isNaN, Double.NaN, "error"));

                assertEquals("error ==> expected: not matching predicate but was: <NaN>", error.getMessage());
            }
        }

        @Nested
        @DisplayName("with message supplier")
        class WithMessageSupplier {

            @Test
            @DisplayName("matches")
            void testMatches() {
                assertDoesNotThrow(() -> assertDoubleDoesNotMatch(Double::isNaN, 1.0, () -> "error"));
            }

            @Test
            @DisplayName("does not match")
            void testDoesNotMatch() {
                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertDoubleDoesNotMatch(Double::isNaN, Double.NaN, () -> "error"));

                assertEquals("error ==> expected: not matching predicate but was: <NaN>", error.getMessage());
            }
        }
    }
}
