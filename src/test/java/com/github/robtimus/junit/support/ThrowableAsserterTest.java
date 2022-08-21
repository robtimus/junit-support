/*
 * ThrowableAsserterTest.java
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
import static com.github.robtimus.junit.support.ThrowableAsserter.executing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import com.github.robtimus.junit.support.ThrowableAsserter.Asserted;
import com.github.robtimus.junit.support.ThrowableAsserter.NoError;
import com.github.robtimus.junit.support.ThrowableAsserter.State;
import com.github.robtimus.junit.support.ThrowableAsserter.ThrownError;

@SuppressWarnings("nls")
class ThrowableAsserterTest {

    @Nested
    @DisplayName("state changes")
    class StateChanges {

        @Nested
        @DisplayName("from initialized")
        class FromInitialized {

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configuring error type")
            void testToConfiguringErrorType(boolean exact) {
                ThrowableAsserter asserter = initialized();

                if (exact) {
                    asserter.whenThrowsExactly(NullPointerException.class);
                } else {
                    asserter.whenThrows(NullPointerException.class);
                }

                assertConfiguringErrorType(asserter, NullPointerException.class, exact);
            }

            @Test
            @DisplayName("to configuring no error")
            void testToConfiguringNoError() {
                ThrowableAsserter asserter = initialized();

                asserter.whenThrowsNothing();

                assertConfiguringNoError(asserter);
            }

            // Impossible to go from initialized to configured without going through configuring error type first

            @Test
            @DisplayName("to asserted")
            void testToAsserted() {
                ThrowableAsserter asserter = initialized();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::runAssertions);
                assertEquals("Cannot run assertions when current state is initialized", exception.getMessage());

                assertInitialized(asserter);
            }
        }

        @Nested
        @DisplayName("from configuring error type")
        class FromConfiguringErrorType {

            // Impossible to go back to initialized

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configuring same error type")
            void testToConfiguringSameErrorType(boolean exact) {
                ThrowableAsserter asserter = configuringErrorType(NullPointerException.class, exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, () -> asserter.whenThrows(NullPointerException.class));
                assertEquals("Cannot configure assertions for an error type when current state is configuring assertions for an error type",
                        exception.getMessage());

                assertConfiguringErrorType(asserter, NullPointerException.class, exact);

                exception = assertThrows(IllegalStateException.class, () -> asserter.whenThrowsExactly(NullPointerException.class));
                assertEquals("Cannot configure assertions for an error type when current state is configuring assertions for an error type",
                        exception.getMessage());

                assertConfiguringErrorType(asserter, NullPointerException.class, exact);
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configuring different error type")
            void testToConfiguringDifferentErrorType(boolean exact) {
                ThrowableAsserter asserter = configuringErrorType(NullPointerException.class, exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, () -> asserter.whenThrows(IOException.class));
                assertEquals("Cannot configure assertions for an error type when current state is configuring assertions for an error type",
                        exception.getMessage());

                assertConfiguringErrorType(asserter, NullPointerException.class, exact);

                exception = assertThrows(IllegalStateException.class, () -> asserter.whenThrowsExactly(IOException.class));
                assertEquals("Cannot configure assertions for an error type when current state is configuring assertions for an error type",
                        exception.getMessage());

                assertConfiguringErrorType(asserter, NullPointerException.class, exact);
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configuring no error")
            void testToConfiguringNoErrorType(boolean exact) {
                ThrowableAsserter asserter = configuringErrorType(NullPointerException.class, exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::whenThrowsNothing);
                assertEquals("Cannot configure assertions for no error when current state is configuring assertions for an error type",
                        exception.getMessage());

                assertConfiguringErrorType(asserter, NullPointerException.class, exact);
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with same error type, same exactness")
            void testToConfiguredWithSameErrorTypeSameExactness(boolean exact) {
                ThrowableAsserter asserter = initialized();

                ThrownError<NullPointerException> thrownError = toConfiguringErrorType(asserter, NullPointerException.class, exact);
                thrownError.thenAssertNothing();

                assertConfigured(asserter);
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with same error type, different exactness")
            void testToConfiguredWithSameErrorTypeDifferentExactness(boolean exact) {
                ThrowableAsserter asserter = initialized();

                ThrownError<NullPointerException> thrownError = toConfiguringErrorType(asserter, NullPointerException.class, exact);
                thrownError.thenAssertNothing();

                assertConfigured(asserter);

                // Ignore the result
                toConfiguringErrorType(asserter, NullPointerException.class, !exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals(String.format("Cannot specify assertions; currently configuring for %s (exact: %b)", NullPointerException.class, !exact),
                        exception.getMessage());
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with different error type, same exactness")
            void testToConfiguredWithDifferentErrorTypeSameExactness(boolean exact) {
                ThrowableAsserter asserter = initialized();

                ThrownError<NullPointerException> thrownError = toConfiguringErrorType(asserter, NullPointerException.class, exact);
                thrownError.thenAssertNothing();

                assertConfigured(asserter);

                // Ignore the result
                toConfiguringErrorType(asserter, IOException.class, exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals(String.format("Cannot specify assertions; currently configuring for %s (exact: %b)", IOException.class, exact),
                        exception.getMessage());
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with different error type, different exactness")
            void testToConfiguredWithDifferentErrorTypeDifferentExactness(boolean exact) {
                ThrowableAsserter asserter = initialized();

                ThrownError<NullPointerException> thrownError = toConfiguringErrorType(asserter, NullPointerException.class, exact);
                thrownError.thenAssertNothing();

                assertConfigured(asserter);

                // Ignore the result
                toConfiguringErrorType(asserter, IOException.class, !exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals(String.format("Cannot specify assertions; currently configuring for %s (exact: %b)", IOException.class, !exact),
                        exception.getMessage());
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with no errors")
            void testToConfiguredWithNoErrors(boolean exact) {
                ThrowableAsserter asserter = initialized();

                NoError noError = toConfiguringNoError(asserter);
                noError.thenAssertNothing();

                assertConfigured(asserter);

                // Ignore the result
                toConfiguringErrorType(asserter, IOException.class, exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, noError::thenAssertNothing);
                assertEquals("Cannot specify assertions for no error when current state is configuring assertions for an error type",
                        exception.getMessage());
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to asserted")
            void testToAsserted(boolean exact) {
                ThrowableAsserter asserter = configuringErrorType(NullPointerException.class, exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::runAssertions);
                assertEquals("Cannot run assertions when current state is configuring assertions for an error type", exception.getMessage());

                assertConfiguringErrorType(asserter, NullPointerException.class, exact);
            }

            private ThrowableAsserter configuringErrorType(Class<? extends Throwable> errorType, boolean exact) {
                ThrowableAsserter asserter = initialized();

                toConfiguringErrorType(asserter, errorType, exact);

                return asserter;
            }
        }

        @Nested
        @DisplayName("from configuring no error")
        class FromConfiguringNoError {

            // Impossible to go back to initialized

            @Test
            @DisplayName("to configuring error type")
            void testToConfiguringErrorType() {
                ThrowableAsserter asserter = configuringNoError();

                IllegalStateException exception = assertThrows(IllegalStateException.class, () -> asserter.whenThrows(IOException.class));
                assertEquals("Cannot configure assertions for an error type when current state is configuring assertions for no error",
                        exception.getMessage());

                assertConfiguringNoError(asserter);

                exception = assertThrows(IllegalStateException.class, () -> asserter.whenThrowsExactly(IOException.class));
                assertEquals("Cannot configure assertions for an error type when current state is configuring assertions for no error",
                        exception.getMessage());

                assertConfiguringNoError(asserter);
            }

            @Test
            @DisplayName("to configuring no error")
            void testToConfiguringNoErrorType() {
                ThrowableAsserter asserter = configuringNoError();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::whenThrowsNothing);
                assertEquals("Cannot configure assertions for no error when current state is configuring assertions for no error",
                        exception.getMessage());

                assertConfiguringNoError(asserter);
            }

            @Test
            @DisplayName("to configured")
            void testToConfigured() {
                ThrowableAsserter asserter = initialized();

                NoError noError = toConfiguringNoError(asserter);
                noError.thenAssertNothing();

                assertConfigured(asserter);
            }

            @Test
            @DisplayName("to asserted")
            void testToAsserted() {
                ThrowableAsserter asserter = configuringNoError();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::runAssertions);
                assertEquals("Cannot run assertions when current state is configuring assertions for no error", exception.getMessage());

                assertConfiguringNoError(asserter);
            }

            private ThrowableAsserter configuringNoError() {
                ThrowableAsserter asserter = initialized();

                toConfiguringNoError(asserter);

                return asserter;
            }
        }

        @Nested
        @DisplayName("from configured with errors")
        class FromConfiguredWithErrors {

            // Impossible to go back to initialized

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configuring error type")
            void testToConfiguringErrorType(boolean exact) {
                ThrowableAsserter asserter = configured(NullPointerException.class, exact);

                toConfiguringErrorType(asserter, IOException.class, exact);

                // toConfiguringNoError already performs the assertions
            }

            @Test
            @DisplayName("to configuring no error")
            void testToConfiguringNoErrorType() {
                ThrowableAsserter asserter = configured(IOException.class, false);

                toConfiguringNoError(asserter);

                // toConfiguringNoError already performs the assertions
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with same error, same exactness")
            void testToConfiguredSameErrorSameExactness(boolean exact) {
                ThrowableAsserter asserter = initialized();

                ThrownError<NullPointerException> thrownError = toConfigured(asserter, NullPointerException.class, exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals("Cannot specify assertions for an error type when current state is configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with same error, different exactness")
            void testToConfiguredSameErrorDifferentExactness(boolean exact) {
                ThrowableAsserter asserter = initialized();

                ThrownError<NullPointerException> thrownError = toConfigured(asserter, NullPointerException.class, exact);

                // Ignore the result
                toConfigured(asserter, NullPointerException.class, !exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals("Cannot specify assertions for an error type when current state is configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with different error, same exactness")
            void testToConfiguredDifferentErrorSameExactness(boolean exact) {
                ThrowableAsserter asserter = initialized();

                ThrownError<NullPointerException> thrownError = toConfigured(asserter, NullPointerException.class, exact);

                // Ignore the result
                toConfigured(asserter, IOException.class, !exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals("Cannot specify assertions for an error type when current state is configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with different error, different exactness")
            void testToConfiguredDifferentErrorDifferentExactness(boolean exact) {
                ThrowableAsserter asserter = initialized();

                ThrownError<NullPointerException> thrownError = toConfigured(asserter, NullPointerException.class, exact);

                // Ignore the result
                toConfigured(asserter, IOException.class, !exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals("Cannot specify assertions for an error type when current state is configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with no error")
            void testToConfiguredNoError(boolean exact) {
                ThrowableAsserter asserter = initialized();

                NoError noError = toConfiguredWithNoError(asserter);

                // Ignore the result
                toConfigured(asserter, IOException.class, !exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, noError::thenAssertNothing);
                assertEquals("Cannot specify assertions for no error when current state is configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @Test
            @DisplayName("to asserted")
            void testToAsserted() {
                ThrowableAsserter asserter = configured(NullPointerException.class, false);

                Asserted asserted = asserter.runAssertions();

                assertAsserted(asserter);

                assertInstanceOf(NullPointerException.class, asserted.andReturn());
                assertInstanceOf(NullPointerException.class, assertIsPresent(asserted.andReturnIfThrown()));
            }

            @Test
            @DisplayName("to asserted failure")
            void testToAssertedFailure() {
                ThrowableAsserter asserter = configured(IOException.class, false);

                assertThrows(AssertionFailedError.class, asserter::runAssertions);

                assertConfigured(asserter);
            }
        }

        @Nested
        @DisplayName("from configured without errors")
        class FromConfiguredWithoutErrors {

            // Impossible to go back to initialized

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configuring error type")
            void testToConfiguringErrorType(boolean exact) {
                ThrowableAsserter asserter = configuredWithoutErrors();

                toConfiguringErrorType(asserter, IOException.class, exact);

                // toConfiguringNoError already performs the assertions
            }

            @Test
            @DisplayName("to configuring no error")
            void testToConfiguringNoErrorType() {
                ThrowableAsserter asserter = configuredWithoutErrors();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::whenThrowsNothing);
                assertEquals("Assertions for no error already configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with error")
            void testToConfiguredWithError(boolean exact) {
                ThrowableAsserter asserter = initialized();

                ThrownError<NullPointerException> thrownError = toConfigured(asserter, NullPointerException.class, exact);

                // Ignore the result
                toConfiguredWithNoError(asserter);

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals("Cannot specify assertions for an error type when current state is configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @Test
            @DisplayName("to configured with no error")
            void testToConfiguredNoError() {
                ThrowableAsserter asserter = initialized();

                NoError noError = toConfiguredWithNoError(asserter);

                IllegalStateException exception = assertThrows(IllegalStateException.class, noError::thenAssertNothing);
                assertEquals("Cannot specify assertions for no error when current state is configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @Test
            @DisplayName("to asserted")
            void testToAsserted() {
                ThrowableAsserter asserter = configuredWithoutErrors();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::runAssertions);
                assertEquals("Cannot run assertions without expected error types", exception.getMessage());

                assertConfigured(asserter);
            }
        }

        @Nested
        @DisplayName("from asserted")
        class FromAsserted {

            // Impossible to go back to initialized

            @Test
            @DisplayName("to configuring error type")
            void testToConfiguringErrorType() {
                ThrowableAsserter asserter = asserted();

                IllegalStateException exception = assertThrows(IllegalStateException.class, () -> asserter.whenThrows(IOException.class));
                assertEquals("Cannot configure assertions for an error type when current state is asserted", exception.getMessage());

                assertAsserted(asserter);

                exception = assertThrows(IllegalStateException.class, () -> asserter.whenThrowsExactly(IOException.class));
                assertEquals("Cannot configure assertions for an error type when current state is asserted", exception.getMessage());

                assertAsserted(asserter);
            }

            @Test
            @DisplayName("to configuring no error")
            void testToConfiguringNoErrorType() {
                ThrowableAsserter asserter = asserted();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::whenThrowsNothing);
                assertEquals("Cannot configure assertions for no error when current state is asserted", exception.getMessage());

                assertAsserted(asserter);
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with error type")
            void testToConfiguredWithErrorType(boolean exact) {
                ThrowableAsserter asserter = initialized();

                ThrownError<NullPointerException> thrownError = toConfigured(asserter, NullPointerException.class, exact);

                Asserted asserted = asserter.runAssertions();

                assertAsserted(asserter);

                assertInstanceOf(NullPointerException.class, asserted.andReturn());
                assertInstanceOf(NullPointerException.class, assertIsPresent(asserted.andReturnIfThrown()));

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals("Cannot specify assertions for an error type when current state is asserted", exception.getMessage());

                assertAsserted(asserter);
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with no error")
            void testToConfiguredWithNoError(boolean exact) {
                ThrowableAsserter asserter = configured(NullPointerException.class, exact);

                NoError noError = toConfiguringNoError(asserter);
                noError.thenAssertNothing();

                assertConfigured(asserter);

                Asserted asserted = asserter.runAssertions();

                assertAsserted(asserter);

                assertInstanceOf(NullPointerException.class, asserted.andReturn());
                assertInstanceOf(NullPointerException.class, assertIsPresent(asserted.andReturnIfThrown()));

                IllegalStateException exception = assertThrows(IllegalStateException.class, noError::thenAssertNothing);
                assertEquals("Cannot specify assertions for no error when current state is asserted", exception.getMessage());

                assertAsserted(asserter);
            }

            @Test
            @DisplayName("to asserted")
            void testToAsserted() {
                ThrowableAsserter asserter = configured(NullPointerException.class, false);

                Asserted asserted = asserter.runAssertions();

                assertAsserted(asserter);

                assertInstanceOf(NullPointerException.class, asserted.andReturn());
                assertInstanceOf(NullPointerException.class, assertIsPresent(asserted.andReturnIfThrown()));

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::runAssertions);
                assertEquals("Cannot run assertions when current state is asserted", exception.getMessage());

                assertAsserted(asserter);
            }

            private ThrowableAsserter asserted() {
                ThrowableAsserter asserter = configured(NullPointerException.class, false);

                Asserted asserted = asserter.runAssertions();

                assertAsserted(asserter);

                assertInstanceOf(NullPointerException.class, asserted.andReturn());
                assertInstanceOf(NullPointerException.class, assertIsPresent(asserted.andReturnIfThrown()));

                return asserter;
            }
        }

        private ThrowableAsserter initialized() {
            ThrowableAsserter asserter = executing(() -> {
                throw new NullPointerException("error");
            });

            assertInitialized(asserter);

            return asserter;
        }

        private ThrowableAsserter configured(Class<? extends Throwable> errorType, boolean exact) {
            ThrowableAsserter asserter = initialized();

            toConfigured(asserter, errorType, exact);

            return asserter;
        }

        private ThrowableAsserter configuredWithoutErrors() {
            ThrowableAsserter asserter = initialized();

            toConfiguredWithNoError(asserter);

            return asserter;
        }

        private <T extends Throwable> ThrownError<T> toConfiguringErrorType(ThrowableAsserter asserter, Class<T> errorType, boolean exact) {
            ThrownError<T> thrownError = exact
                    ? asserter.whenThrowsExactly(errorType)
                    : asserter.whenThrows(errorType);

            assertConfiguringErrorType(asserter, errorType, exact);

            return thrownError;
        }

        private NoError toConfiguringNoError(ThrowableAsserter asserter) {
            NoError noError = asserter.whenThrowsNothing();

            assertConfiguringNoError(asserter);

            return noError;
        }

        private <T extends Throwable> ThrownError<T> toConfigured(ThrowableAsserter asserter, Class<T> errorType, boolean exact) {
            ThrownError<T> thrownError = exact
                    ? asserter.whenThrowsExactly(errorType)
                    : asserter.whenThrows(errorType);

            thrownError.thenAssertNothing();

            assertConfigured(asserter);

            return thrownError;
        }

        private NoError toConfiguredWithNoError(ThrowableAsserter asserter) {
            NoError noError = asserter.whenThrowsNothing();

            noError.thenAssertNothing();

            assertConfigured(asserter);

            return noError;
        }

        private void assertInitialized(ThrowableAsserter asserter) {
            assertEquals(State.INITIALIZED, asserter.state());
            assertNull(asserter.configuringErrorType());
            assertFalse(asserter.configuringExactErrorType());
        }

        private void assertConfiguringErrorType(ThrowableAsserter asserter, Class<? extends Throwable> errorType, boolean exact) {
            assertEquals(State.CONFIGURING_ERROR_TYPE, asserter.state());
            assertEquals(errorType, asserter.configuringErrorType());
            assertEquals(exact, asserter.configuringExactErrorType());
        }

        private void assertConfiguringNoError(ThrowableAsserter asserter) {
            assertEquals(State.CONFIGURING_NO_ERROR, asserter.state());
            assertNull(asserter.configuringErrorType());
            assertFalse(asserter.configuringExactErrorType());
        }

        private void assertConfigured(ThrowableAsserter asserter) {
            assertEquals(State.CONFIGURED, asserter.state());
            assertNull(asserter.configuringErrorType());
            assertFalse(asserter.configuringExactErrorType());
        }

        private void assertAsserted(ThrowableAsserter asserter) {
            assertEquals(State.ASSERTED, asserter.state());
            assertNull(asserter.configuringErrorType());
            assertFalse(asserter.configuringExactErrorType());
        }
    }

    @Nested
    @DisplayName("whenThrows")
    class WhenThrows {

        @Test
        @DisplayName("unique errorType")
        void testUniqueErrorType() {
            ThrowableAsserter asserter = executing(() -> new NullPointerException());

            assertNull(asserter.findAsserter(NullPointerException.class));
            assertNull(asserter.findAsserter(IOException.class));

            asserter.whenThrows(NullPointerException.class).thenAssertNothing();

            assertNotNull(asserter.findAsserter(NullPointerException.class));
            assertNull(asserter.findAsserter(IOException.class));

            asserter.whenThrows(IOException.class).thenAssertNothing();

            assertNotNull(asserter.findAsserter(NullPointerException.class));
            assertNotNull(asserter.findAsserter(IOException.class));
        }

        @Test
        @DisplayName("duplicate errorType")
        void testDuplicateErrorType() {
            ThrowableAsserter asserter = executing(() -> new NullPointerException());

            Consumer<NullPointerException> consumer = NullPointerException::getMessage;

            assertNull(asserter.findAsserter(NullPointerException.class));

            asserter.whenThrows(NullPointerException.class).thenAssert(consumer);

            assertSame(consumer, asserter.findAsserter(NullPointerException.class));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> asserter.whenThrows(NullPointerException.class));
            assertEquals("class java.lang.NullPointerException already configured", exception.getMessage());
        }

        @Test
        @DisplayName("errorType already configured exactly")
        void testErrorTypeConfiguredExactly() {
            ThrowableAsserter asserter = executing(() -> new NullPointerException());

            Consumer<IllegalArgumentException> consumer1 = IllegalArgumentException::getMessage;
            Consumer<IllegalArgumentException> consumer2 = IllegalArgumentException::getLocalizedMessage;
            assertNotSame(consumer1, consumer2);

            assertNull(asserter.findAsserter(IllegalAccessError.class));

            asserter.whenThrowsExactly(IllegalArgumentException.class).thenAssert(consumer1);

            assertSame(consumer1, asserter.findAsserter(IllegalArgumentException.class));
            assertNull(asserter.findAsserter(NumberFormatException.class));

            asserter.whenThrows(IllegalArgumentException.class).thenAssert(consumer2);

            assertSame(consumer1, asserter.findAsserter(IllegalArgumentException.class));
            assertSame(consumer2, asserter.findAsserter(NumberFormatException.class));
        }
    }

    @Nested
    @DisplayName("whenThrowsExactly")
    class WhenThrowsExactly {

        @Test
        @DisplayName("unique errorType")
        void testUniqueErrorType() {
            ThrowableAsserter asserter = executing(() -> new NullPointerException());

            assertNull(asserter.findAsserter(NullPointerException.class));
            assertNull(asserter.findAsserter(IOException.class));

            asserter.whenThrowsExactly(NullPointerException.class).thenAssertNothing();

            assertNotNull(asserter.findAsserter(NullPointerException.class));
            assertNull(asserter.findAsserter(IOException.class));

            asserter.whenThrowsExactly(IOException.class).thenAssertNothing();

            assertNotNull(asserter.findAsserter(NullPointerException.class));
            assertNotNull(asserter.findAsserter(IOException.class));
        }

        @Test
        @DisplayName("duplicate errorType")
        void testDuplicateErrorType() {
            ThrowableAsserter asserter = executing(() -> new NullPointerException());

            Consumer<NullPointerException> consumer = NullPointerException::getMessage;

            assertNull(asserter.findAsserter(NullPointerException.class));

            asserter.whenThrowsExactly(NullPointerException.class).thenAssert(consumer);

            assertSame(consumer, asserter.findAsserter(NullPointerException.class));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> asserter.whenThrowsExactly(NullPointerException.class));
            assertEquals("class java.lang.NullPointerException already configured", exception.getMessage());
        }

        @Test
        @DisplayName("errorType already configured not-exactly")
        void testErrorTypeConfiguredNotExactly() {
            ThrowableAsserter asserter = executing(() -> new NullPointerException());

            Consumer<IllegalArgumentException> consumer1 = IllegalArgumentException::getMessage;
            Consumer<IllegalArgumentException> consumer2 = IllegalArgumentException::getLocalizedMessage;
            assertNotSame(consumer1, consumer2);

            assertNull(asserter.findAsserter(IllegalAccessError.class));

            asserter.whenThrows(IllegalArgumentException.class).thenAssert(consumer1);

            assertSame(consumer1, asserter.findAsserter(IllegalArgumentException.class));
            assertSame(consumer1, asserter.findAsserter(NumberFormatException.class));

            asserter.whenThrowsExactly(IllegalArgumentException.class).thenAssert(consumer2);

            assertSame(consumer2, asserter.findAsserter(IllegalArgumentException.class));
            assertSame(consumer1, asserter.findAsserter(NumberFormatException.class));
        }
    }

    @Nested
    @DisplayName("findAsserter")
    class FindAsserter {

        @Test
        @DisplayName("find configured exactly")
        void testFindConfiguredExactly() {
            Consumer<Throwable> consumer1 = Throwable::printStackTrace;
            Consumer<Throwable> consumer2 = Throwable::getMessage;

            ThrowableAsserter asserter = executing(Object::new)
                    .whenThrows(IllegalArgumentException.class).thenAssert(consumer1)
                    .whenThrowsExactly(IllegalArgumentException.class).thenAssert(consumer2);

            assertSame(consumer2, asserter.findAsserter(IllegalArgumentException.class));
        }

        @Test
        @DisplayName("find configured non-exactly")
        void testFindConfiguredNonExactly() {
            Consumer<Throwable> consumer1 = Throwable::printStackTrace;
            Consumer<Throwable> consumer2 = Throwable::getMessage;

            ThrowableAsserter asserter = executing(Object::new)
                    .whenThrows(IllegalArgumentException.class).thenAssert(consumer1)
                    .whenThrows(NumberFormatException.class).thenAssert(consumer2);

            assertSame(consumer1, asserter.findAsserter(IllegalArgumentException.class));
            assertSame(consumer1, asserter.findAsserter(InvalidPathException.class));
            assertSame(consumer2, asserter.findAsserter(NumberFormatException.class));
        }

        @Test
        @DisplayName("find non-configured non-exactly")
        void testFindNonConfigured() {
            Consumer<Throwable> consumer1 = Throwable::printStackTrace;
            Consumer<Throwable> consumer2 = Throwable::getMessage;

            ThrowableAsserter asserter = executing(Object::new)
                    .whenThrows(IllegalArgumentException.class).thenAssert(consumer1)
                    .whenThrows(NumberFormatException.class).thenAssert(consumer2);

            assertNull(asserter.findAsserter(IOException.class));
            assertNull(asserter.findAsserter(NoSuchFileException.class));
        }
    }

    @Nested
    @DisplayName("runAssertions")
    class RunAssertions {

        @Nested
        @DisplayName("without message or supplier")
        class WithoutMessageOrSupplier {

            @Nested
            @DisplayName("not throwing")
            class NotThrowing {

                @Test
                @DisplayName("whenThrowsNothing not called")
                void whenThrowsNothingNotCalled() {
                    Consumer<Throwable> consumer = mockedConsumer();

                    ThrowableAsserter asserter = executing(Object::new)
                            .whenThrows(NullPointerException.class).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssert(consumer);

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, asserter::runAssertions);
                    assertEquals(String.format("Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                            NullPointerException.class.getName(), IOException.class.getName()),
                            error.getMessage());

                    verifyNoInteractions(consumer);
                }

                @Test
                @DisplayName("whenThrowsNothing called")
                void whenThrowsNothingCalled() {
                    Consumer<Throwable> consumer = mockedConsumer();
                    Runnable runnable = mock(Runnable.class);

                    Asserted asserted = executing(Object::new)
                            .whenThrows(NullPointerException.class).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssert(runnable)
                            .runAssertions();

                    verify(runnable).run();
                    verifyNoMoreInteractions(consumer, runnable);

                    assertIsEmpty(asserted.andReturnIfThrown());

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturn);
                    assertEquals("Nothing was thrown", exception.getMessage());
                }

                @Test
                @DisplayName("whenThrowsNothing called without assertions")
                void whenThrowsNothingCalledWithoutAssertions() {
                    Consumer<Throwable> consumer = mockedConsumer();

                    Asserted asserted = executing(Object::new)
                            .whenThrows(NullPointerException.class).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssertNothing()
                            .runAssertions();

                    verifyNoMoreInteractions(consumer);

                    assertIsEmpty(asserted.andReturnIfThrown());

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturn);
                    assertEquals("Nothing was thrown", exception.getMessage());
                }
            }

            @Test
            @DisplayName("throwing first")
            void testThrowingFirst() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                NullPointerException error = new NullPointerException("error");

                Asserted asserted = executing(throwing(error))
                        .whenThrows(NullPointerException.class).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .runAssertions();

                verify(consumer1).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertSame(error, asserted.andReturn());
                assertSame(error, assertIsPresent(asserted.andReturnIfThrown()));
            }

            @Test
            @DisplayName("throwing second")
            void testThrowingSecond() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IOException error = new IOException("error");

                Asserted asserted = executing(throwing(error))
                        .whenThrows(NullPointerException.class).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .runAssertions();

                verify(consumer2).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertSame(error, asserted.andReturn());
                assertSame(error, assertIsPresent(asserted.andReturnIfThrown()));
            }

            @Test
            @DisplayName("throwing other")
            void testThrowingOther() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IllegalArgumentException error = new IllegalArgumentException("error");

                ThrowableAsserter asserter = executing(throwing(error))
                        .whenThrows(NullPointerException.class).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable);

                AssertionFailedError failure = assertThrows(AssertionFailedError.class, asserter::runAssertions);
                assertEquals(String.format("Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        NullPointerException.class.getName(), IOException.class.getName(), error.getClass().getName()),
                        failure.getMessage());
                assertSame(error, failure.getCause());

                verifyNoInteractions(consumer1, consumer2, runnable);
            }
        }

        @Nested
        @DisplayName("with message")
        class WithMessage {

            @Nested
            @DisplayName("not throwing")
            class NotThrowing {

                @Test
                @DisplayName("whenThrowsNothing not called")
                void whenThrowsNothingNotCalled() {
                    Consumer<Throwable> consumer = mockedConsumer();

                    ThrowableAsserter asserter = executing(Object::new, "error")
                            .whenThrows(NullPointerException.class).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssert(consumer);

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, asserter::runAssertions);
                    assertEquals(String.format("error ==> Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                            NullPointerException.class.getName(), IOException.class.getName()),
                            error.getMessage());

                    verifyNoInteractions(consumer);
                }

                @Test
                @DisplayName("whenThrowsNothing called")
                void whenThrowsNothingCalled() {
                    Consumer<Throwable> consumer = mockedConsumer();
                    Runnable runnable = mock(Runnable.class);

                    Asserted asserted = executing(Object::new, "error")
                            .whenThrows(NullPointerException.class).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssert(runnable)
                            .runAssertions();

                    verify(runnable).run();
                    verifyNoMoreInteractions(consumer, runnable);

                    assertIsEmpty(asserted.andReturnIfThrown());

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturn);
                    assertEquals("Nothing was thrown", exception.getMessage());
                }

                @Test
                @DisplayName("whenThrowsNothing called without assertions")
                void whenThrowsNothingCalledWithoutAssertions() {
                    Consumer<Throwable> consumer = mockedConsumer();

                    Asserted asserted = executing(Object::new, "error")
                            .whenThrows(NullPointerException.class).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssertNothing()
                            .runAssertions();

                    verifyNoMoreInteractions(consumer);

                    assertIsEmpty(asserted.andReturnIfThrown());

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturn);
                    assertEquals("Nothing was thrown", exception.getMessage());
                }
            }

            @Test
            @DisplayName("throwing first")
            void testThrowingFirst() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                NullPointerException error = new NullPointerException("error");

                Asserted asserted = executing(throwing(error), "error")
                        .whenThrows(NullPointerException.class).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .runAssertions();

                verify(consumer1).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertSame(error, asserted.andReturn());
                assertSame(error, assertIsPresent(asserted.andReturnIfThrown()));
            }

            @Test
            @DisplayName("throwing second")
            void testThrowingSecond() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IOException error = new IOException("error");

                Asserted asserted = executing(throwing(error), "error")
                        .whenThrows(NullPointerException.class).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .runAssertions();

                verify(consumer2).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertSame(error, asserted.andReturn());
                assertSame(error, assertIsPresent(asserted.andReturnIfThrown()));
            }

            @Test
            @DisplayName("throwing other")
            void testThrowingOther() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IllegalArgumentException error = new IllegalArgumentException("error");

                ThrowableAsserter asserter = executing(throwing(error), "error")
                        .whenThrows(NullPointerException.class).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable);

                AssertionFailedError failure = assertThrows(AssertionFailedError.class, asserter::runAssertions);
                assertEquals(String.format("error ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        NullPointerException.class.getName(), IOException.class.getName(), error.getClass().getName()),
                        failure.getMessage());
                assertSame(error, failure.getCause());

                verifyNoInteractions(consumer1, consumer2, runnable);
            }
        }

        @Nested
        @DisplayName("with supplier")
        class WithSupplier {

            @Nested
            @DisplayName("not throwing")
            class NotThrowing {

                @Test
                @DisplayName("whenThrowsNothing not called")
                void whenThrowsNothingNotCalled() {
                    Consumer<Throwable> consumer = mockedConsumer();

                    ThrowableAsserter asserter = executing(Object::new, () -> "error")
                            .whenThrows(NullPointerException.class).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssert(consumer);

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, asserter::runAssertions);
                    assertEquals(String.format("error ==> Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                            NullPointerException.class.getName(), IOException.class.getName()),
                            error.getMessage());

                    verifyNoInteractions(consumer);
                }

                @Test
                @DisplayName("whenThrowsNothing called")
                void whenThrowsNothingCalled() {
                    Consumer<Throwable> consumer = mockedConsumer();
                    Runnable runnable = mock(Runnable.class);

                    Asserted asserted = executing(Object::new, () -> "error")
                            .whenThrows(NullPointerException.class).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssert(runnable)
                            .runAssertions();

                    verify(runnable).run();
                    verifyNoMoreInteractions(consumer, runnable);

                    assertIsEmpty(asserted.andReturnIfThrown());

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturn);
                    assertEquals("Nothing was thrown", exception.getMessage());
                }

                @Test
                @DisplayName("whenThrowsNothing called without assertions")
                void whenThrowsNothingCalledWithoutAssertions() {
                    Consumer<Throwable> consumer = mockedConsumer();

                    Asserted asserted = executing(Object::new, () -> "error")
                            .whenThrows(NullPointerException.class).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssertNothing()
                            .runAssertions();

                    verifyNoMoreInteractions(consumer);

                    assertIsEmpty(asserted.andReturnIfThrown());

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturn);
                    assertEquals("Nothing was thrown", exception.getMessage());
                }
            }

            @Test
            @DisplayName("throwing first")
            void testThrowingFirst() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                NullPointerException error = new NullPointerException("error");

                Asserted asserted = executing(throwing(error), () -> "error")
                        .whenThrows(NullPointerException.class).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .runAssertions();

                verify(consumer1).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertSame(error, asserted.andReturn());
                assertSame(error, assertIsPresent(asserted.andReturnIfThrown()));
            }

            @Test
            @DisplayName("throwing second")
            void testThrowingSecond() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IOException error = new IOException("error");

                Asserted asserted = executing(throwing(error), () -> "error")
                        .whenThrows(NullPointerException.class).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .runAssertions();

                verify(consumer2).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertSame(error, asserted.andReturn());
                assertSame(error, assertIsPresent(asserted.andReturnIfThrown()));
            }

            @Test
            @DisplayName("throwing other")
            void testThrowingOther() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IllegalArgumentException error = new IllegalArgumentException("error");

                ThrowableAsserter asserter = executing(throwing(error), () -> "error")
                        .whenThrows(NullPointerException.class).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable);

                AssertionFailedError failure = assertThrows(AssertionFailedError.class, asserter::runAssertions);
                assertEquals(String.format("error ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        NullPointerException.class.getName(), IOException.class.getName(), error.getClass().getName()),
                        failure.getMessage());
                assertSame(error, failure.getCause());

                verifyNoInteractions(consumer1, consumer2, runnable);
            }
        }

        @SuppressWarnings("unchecked")
        private <T extends Throwable> Consumer<T> mockedConsumer() {
            return mock(Consumer.class);
        }

        private Executable throwing(Throwable error) {
            return () -> {
                throw error;
            };
        }
    }
}
