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
import static com.github.robtimus.junit.support.ThrowableAsserter.whenThrows;
import static com.github.robtimus.junit.support.ThrowableAsserter.whenThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.text.ParseException;
import java.util.function.Consumer;
import java.util.function.Supplier;
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

        // from initialized is not possible; the first returned value is linked to a configuring object

        @Nested
        @DisplayName("from configuring error type")
        class FromConfiguringErrorType {

            // Impossible to go back to initialized

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configuring same error type")
            void testToConfiguringSameErrorType(boolean exact) {
                ThrowableAsserter<?> asserter = configuringErrorType(NullPointerException.class, exact).throwableAsserter();

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
                ThrowableAsserter<?> asserter = configuringErrorType(NullPointerException.class, exact).throwableAsserter();

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
                ThrowableAsserter<?> asserter = configuringErrorType(NullPointerException.class, exact).throwableAsserter();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::whenThrowsNothing);
                assertEquals("Cannot configure assertions for no error when current state is configuring assertions for an error type",
                        exception.getMessage());

                assertConfiguringErrorType(asserter, NullPointerException.class, exact);
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with same error type, same exactness")
            void testToConfiguredWithSameErrorTypeSameExactness(boolean exact) {
                ThrownError<NullPointerException, ?> thrownError = configuringErrorType(NullPointerException.class, exact);

                ThrowableAsserter<?> asserter = thrownError.throwableAsserter();

                thrownError.thenAssertNothing();

                assertConfigured(asserter);
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with same error type, different exactness")
            void testToConfiguredWithSameErrorTypeDifferentExactness(boolean exact) {
                ThrownError<NullPointerException, ?> thrownError = configuringErrorType(NullPointerException.class, exact);

                ThrowableAsserter<?> asserter = thrownError.throwableAsserter();

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
                ThrownError<NullPointerException, ?> thrownError = configuringErrorType(NullPointerException.class, exact);

                ThrowableAsserter<?> asserter = thrownError.throwableAsserter();

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
                ThrownError<NullPointerException, ?> thrownError = configuringErrorType(NullPointerException.class, exact);

                ThrowableAsserter<?> asserter = thrownError.throwableAsserter();

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
                ThrowableAsserter<?> asserter = configured(NullPointerException.class, exact);

                NoError<?> noError = toConfiguringNoError(asserter);
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
                ThrowableAsserter<?> asserter = configuringErrorType(NullPointerException.class, exact).throwableAsserter();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::execute);
                assertEquals("Cannot run assertions when current state is configuring assertions for an error type", exception.getMessage());

                assertConfiguringErrorType(asserter, NullPointerException.class, exact);
            }
        }

        @Nested
        @DisplayName("from configuring no error")
        class FromConfiguringNoError {

            // Impossible to go back to initialized

            @Test
            @DisplayName("to configuring error type")
            void testToConfiguringErrorType() {
                ThrowableAsserter<?> asserter = configuringNoError(NullPointerException.class, false).throwableAsserter();

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
                ThrowableAsserter<?> asserter = configuringNoError(NullPointerException.class, false).throwableAsserter();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::whenThrowsNothing);
                assertEquals("Cannot configure assertions for no error when current state is configuring assertions for no error",
                        exception.getMessage());

                assertConfiguringNoError(asserter);
            }

            @Test
            @DisplayName("to configured")
            void testToConfigured() {
                NoError<?> noError = configuringNoError(NullPointerException.class, false);

                ThrowableAsserter<?> asserter = noError.throwableAsserter();

                noError.thenAssertNothing();

                assertConfigured(asserter);
            }

            @Test
            @DisplayName("to asserted")
            void testToAsserted() {
                ThrowableAsserter<?> asserter = configuringNoError(NullPointerException.class, false).throwableAsserter();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::execute);
                assertEquals("Cannot run assertions when current state is configuring assertions for no error", exception.getMessage());

                assertConfiguringNoError(asserter);
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
                ThrowableAsserter<?> asserter = configured(NullPointerException.class, exact);

                toConfiguringErrorType(asserter, IOException.class, exact);

                // toConfiguringNoError already performs the assertions
            }

            @Test
            @DisplayName("to configuring no error")
            void testToConfiguringNoErrorType() {
                ThrowableAsserter<?> asserter = configured(IOException.class, false);

                toConfiguringNoError(asserter);

                // toConfiguringNoError already performs the assertions
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with same error, same exactness")
            void testToConfiguredSameErrorSameExactness(boolean exact) {
                ThrownError<NullPointerException, ?> thrownError = configuringErrorType(NullPointerException.class, exact);

                ThrowableAsserter<?> asserter = thrownError.throwableAsserter();

                thrownError.thenAssertNothing();

                assertConfigured(asserter);

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals("Cannot specify assertions for an error type when current state is configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with same error, different exactness")
            void testToConfiguredSameErrorDifferentExactness(boolean exact) {
                ThrownError<NullPointerException, ?> thrownError = configuringErrorType(NullPointerException.class, exact);

                ThrowableAsserter<?> asserter = thrownError.throwableAsserter();

                thrownError.thenAssertNothing();

                assertConfigured(asserter);

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
                ThrownError<NullPointerException, ?> thrownError = configuringErrorType(NullPointerException.class, exact);

                ThrowableAsserter<?> asserter = thrownError.throwableAsserter();

                thrownError.thenAssertNothing();

                assertConfigured(asserter);

                // Ignore the result
                toConfigured(asserter, IOException.class, exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals("Cannot specify assertions for an error type when current state is configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with different error, different exactness")
            void testToConfiguredDifferentErrorDifferentExactness(boolean exact) {
                ThrownError<NullPointerException, ?> thrownError = configuringErrorType(NullPointerException.class, exact);

                ThrowableAsserter<?> asserter = thrownError.throwableAsserter();

                thrownError.thenAssertNothing();

                assertConfigured(asserter);

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
                ThrowableAsserter<?> asserter = configured(NullPointerException.class, exact);

                NoError<?> noError = toConfiguredWithNoError(asserter);

                // Ignore the result
                toConfigured(asserter, IOException.class, exact);

                IllegalStateException exception = assertThrows(IllegalStateException.class, noError::thenAssertNothing);
                assertEquals("Cannot specify assertions for no error when current state is configured", exception.getMessage());

                assertConfigured(asserter);
            }

            @Test
            @DisplayName("to asserted")
            void testToAsserted() {
                ThrowableAsserter<?> asserter = configured(NullPointerException.class, false);

                Asserted<?> asserted = asserter.execute();

                assertAsserted(asserter);

                assertIsEmpty(asserted.andReturnResult());
                assertInstanceOf(NullPointerException.class, asserted.andReturnError());
                assertInstanceOf(NullPointerException.class, assertIsPresent(asserted.andReturnErrorIfThrown()));
            }

            @Test
            @DisplayName("to asserted failure")
            void testToAssertedFailure() {
                ThrowableAsserter<?> asserter = configured(IOException.class, false);

                assertThrows(AssertionFailedError.class, asserter::execute);

                assertConfigured(asserter);
            }
        }

        @Nested
        @DisplayName("from configured with no error")
        class FromConfiguredWithNoError {

            // configured without error is not possible; the first returned value is linked to an error type configuring object

            @Test
            @DisplayName("to configuring no error")
            void testToConfiguringNoErrorType() {
                ThrowableAsserter<?> asserter = configured(NullPointerException.class, false)
                        .whenThrowsNothing().thenAssertNothing();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::whenThrowsNothing);
                assertEquals("Assertions for no error already configured", exception.getMessage());

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
                ThrowableAsserter<?> asserter = asserted();

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
                ThrowableAsserter<?> asserter = asserted();

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::whenThrowsNothing);
                assertEquals("Cannot configure assertions for no error when current state is asserted", exception.getMessage());

                assertAsserted(asserter);
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with error type")
            void testToConfiguredWithErrorType(boolean exact) {
                ThrownError<NullPointerException, ?> thrownError = configuringErrorType(NullPointerException.class, exact);

                ThrowableAsserter<?> asserter = thrownError.throwableAsserter();

                thrownError.thenAssertNothing();

                assertConfigured(asserter);

                Asserted<?> asserted = asserter.execute();

                assertAsserted(asserter);

                assertIsEmpty(asserted.andReturnResult());
                assertInstanceOf(NullPointerException.class, asserted.andReturnError());
                assertInstanceOf(NullPointerException.class, assertIsPresent(asserted.andReturnErrorIfThrown()));

                IllegalStateException exception = assertThrows(IllegalStateException.class, thrownError::thenAssertNothing);
                assertEquals("Cannot specify assertions for an error type when current state is asserted", exception.getMessage());

                assertAsserted(asserter);
            }

            @ParameterizedTest(name = "exact: {0}")
            @ValueSource(booleans = { true, false })
            @DisplayName("to configured with no error")
            void testToConfiguredWithNoError(boolean exact) {
                ThrowableAsserter<?> asserter = configured(NullPointerException.class, exact);

                NoError<?> noError = toConfiguringNoError(asserter);
                noError.thenAssertNothing();

                assertConfigured(asserter);

                Asserted<?> asserted = asserter.execute();

                assertAsserted(asserter);

                assertIsEmpty(asserted.andReturnResult());
                assertInstanceOf(NullPointerException.class, asserted.andReturnError());
                assertInstanceOf(NullPointerException.class, assertIsPresent(asserted.andReturnErrorIfThrown()));

                IllegalStateException exception = assertThrows(IllegalStateException.class, noError::thenAssertNothing);
                assertEquals("Cannot specify assertions for no error when current state is asserted", exception.getMessage());

                assertAsserted(asserter);
            }

            @Test
            @DisplayName("to asserted")
            void testToAsserted() {
                ThrowableAsserter<?> asserter = configured(NullPointerException.class, false);

                Asserted<?> asserted = asserter.execute();

                assertAsserted(asserter);

                assertIsEmpty(asserted.andReturnResult());
                assertInstanceOf(NullPointerException.class, asserted.andReturnError());
                assertInstanceOf(NullPointerException.class, assertIsPresent(asserted.andReturnErrorIfThrown()));

                IllegalStateException exception = assertThrows(IllegalStateException.class, asserter::execute);
                assertEquals("Cannot run assertions when current state is asserted", exception.getMessage());

                assertAsserted(asserter);
            }
        }

        private <T extends Throwable> ThrownError<T, ?> configuringErrorType(Class<T> errorType, boolean exact) {
            Executable executable = throwing(new NullPointerException("error"));

            ThrownError<T, ?> thrownError = exact
                    ? whenThrowsExactly(errorType, executable)
                    : whenThrows(errorType, executable);

            assertConfiguringErrorType(thrownError.throwableAsserter(), errorType, exact);

            return thrownError;
        }

        private NoError<?> configuringNoError(Class<? extends Throwable> initialErrorType, boolean exact) {
            ThrowableAsserter<?> asserter = configured(initialErrorType, exact);

            NoError<?> noError = asserter.whenThrowsNothing();

            assertConfiguringNoError(asserter);

            return noError;
        }

        private ThrowableAsserter<?> configured(Class<? extends Throwable> errorType, boolean exact) {
            ThrowableAsserter<?> asserter = configuringErrorType(errorType, exact).thenAssertNothing();

            assertConfigured(asserter);

            return asserter;
        }

        private ThrowableAsserter<?> asserted() {
            ThrowableAsserter<?> asserter = configured(NullPointerException.class, false);

            Asserted<?> asserted = asserter.execute();

            assertAsserted(asserter);

            assertIsEmpty(asserted.andReturnResult());
            assertInstanceOf(NullPointerException.class, asserted.andReturnError());
            assertInstanceOf(NullPointerException.class, assertIsPresent(asserted.andReturnErrorIfThrown()));

            return asserter;
        }

        private <T extends Throwable, R> ThrownError<T, R> toConfiguringErrorType(ThrowableAsserter<R> asserter, Class<T> errorType, boolean exact) {
            ThrownError<T, R> thrownError = exact
                    ? asserter.whenThrowsExactly(errorType)
                    : asserter.whenThrows(errorType);

            assertConfiguringErrorType(asserter, errorType, exact);

            return thrownError;
        }

        private <R> NoError<R> toConfiguringNoError(ThrowableAsserter<R> asserter) {
            NoError<R> noError = asserter.whenThrowsNothing();

            assertConfiguringNoError(asserter);

            return noError;
        }

        private <T extends Throwable, R> ThrownError<T, R> toConfigured(ThrowableAsserter<R> asserter, Class<T> errorType, boolean exact) {
            ThrownError<T, R> thrownError = exact
                    ? asserter.whenThrowsExactly(errorType)
                    : asserter.whenThrows(errorType);

            thrownError.thenAssertNothing();

            assertConfigured(asserter);

            return thrownError;
        }

        private <R> NoError<R> toConfiguredWithNoError(ThrowableAsserter<R> asserter) {
            NoError<R> noError = asserter.whenThrowsNothing();

            noError.thenAssertNothing();

            assertConfigured(asserter);

            return noError;
        }

        private void assertConfiguringErrorType(ThrowableAsserter<?> asserter, Class<? extends Throwable> errorType, boolean exact) {
            assertEquals(State.CONFIGURING_ERROR_TYPE, asserter.state());
            assertEquals(errorType, asserter.configuringErrorType());
            assertEquals(exact, asserter.configuringExactErrorType());
        }

        private void assertConfiguringNoError(ThrowableAsserter<?> asserter) {
            assertEquals(State.CONFIGURING_NO_ERROR, asserter.state());
            assertNull(asserter.configuringErrorType());
            assertFalse(asserter.configuringExactErrorType());
        }

        private void assertConfigured(ThrowableAsserter<?> asserter) {
            assertEquals(State.CONFIGURED, asserter.state());
            assertNull(asserter.configuringErrorType());
            assertFalse(asserter.configuringExactErrorType());
        }

        private void assertAsserted(ThrowableAsserter<?> asserter) {
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
            ThrowableAsserter<?> asserter = newAsserter();

            assertFalse(asserter.runAllAssertions(new NullPointerException()));
            assertFalse(asserter.runAllAssertions(new IOException()));

            asserter.whenThrows(NullPointerException.class).thenAssertNothing();

            assertTrue(asserter.runAllAssertions(new NullPointerException()));
            assertFalse(asserter.runAllAssertions(new IOException()));

            asserter.whenThrows(IOException.class).thenAssertNothing();

            assertTrue(asserter.runAllAssertions(new NullPointerException()));
            assertTrue(asserter.runAllAssertions(new IOException()));
        }

        @Test
        @DisplayName("duplicate errorType")
        void testDuplicateErrorType() {
            ThrowableAsserter<?> asserter = newAsserter();

            Consumer<NullPointerException> consumer = mockedConsumer();

            assertFalse(asserter.runAllAssertions(new NullPointerException()));

            verifyNoInteractions(consumer);

            asserter.whenThrows(NullPointerException.class).thenAssert(consumer);

            NullPointerException error = new NullPointerException();

            assertTrue(asserter.runAllAssertions(error));

            verify(consumer).accept(error);
            verifyNoMoreInteractions(consumer);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> asserter.whenThrows(NullPointerException.class));
            assertEquals("class java.lang.NullPointerException already configured", exception.getMessage());
        }

        @Test
        @DisplayName("errorType already configured exactly")
        void testErrorTypeConfiguredExactly() {
            ThrowableAsserter<?> asserter = newAsserter();

            Consumer<IllegalArgumentException> consumer1 = mockedConsumer();
            Consumer<IllegalArgumentException> consumer2 = mockedConsumer();

            IllegalArgumentException error1 = new IllegalArgumentException();
            NumberFormatException error2 = new NumberFormatException();

            assertFalse(asserter.runAllAssertions(error1));
            assertFalse(asserter.runAllAssertions(error2));

            verifyNoInteractions(consumer1, consumer2);

            asserter.whenThrowsExactly(IllegalArgumentException.class).thenAssert(consumer1);

            assertTrue(asserter.runAllAssertions(error1));
            assertFalse(asserter.runAllAssertions(error2));

            verify(consumer1).accept(error1);
            verifyNoMoreInteractions(consumer1, consumer2);

            asserter.whenThrows(IllegalArgumentException.class).thenAssert(consumer2);

            assertTrue(asserter.runAllAssertions(error1));
            assertTrue(asserter.runAllAssertions(error2));

            // consumer1.accept(error1) has occurred twice, because asserter.runAllAssertions(error1) has run it twice
            // consumer1 is not used for error2 because it was configured exactly
            verify(consumer1, times(2)).accept(error1);
            // consumer2 is used for both errors
            verify(consumer2).accept(error1);
            verify(consumer2).accept(error2);
            verifyNoMoreInteractions(consumer1, consumer2);
        }

        private ThrowableAsserter<?> newAsserter() {
            return whenThrows(ParseException.class, Object::new).thenAssertNothing();
        }
    }

    @Nested
    @DisplayName("whenThrowsExactly")
    class WhenThrowsExactly {

        @Test
        @DisplayName("unique errorType")
        void testUniqueErrorType() {
            ThrowableAsserter<?> asserter = newAsserter();

            assertFalse(asserter.runAllAssertions(new NullPointerException()));
            assertFalse(asserter.runAllAssertions(new IOException()));

            asserter.whenThrowsExactly(NullPointerException.class).thenAssertNothing();

            assertTrue(asserter.runAllAssertions(new NullPointerException()));
            assertFalse(asserter.runAllAssertions(new IOException()));

            asserter.whenThrowsExactly(IOException.class).thenAssertNothing();

            assertTrue(asserter.runAllAssertions(new NullPointerException()));
            assertTrue(asserter.runAllAssertions(new IOException()));
        }

        @Test
        @DisplayName("duplicate errorType")
        void testDuplicateErrorType() {
            ThrowableAsserter<?> asserter = newAsserter();

            Consumer<NullPointerException> consumer = mockedConsumer();

            assertFalse(asserter.runAllAssertions(new NullPointerException()));

            verifyNoInteractions(consumer);

            asserter.whenThrowsExactly(NullPointerException.class).thenAssert(consumer);

            NullPointerException error = new NullPointerException();

            assertTrue(asserter.runAllAssertions(error));

            verify(consumer).accept(error);
            verifyNoMoreInteractions(consumer);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> asserter.whenThrowsExactly(NullPointerException.class));
            assertEquals("class java.lang.NullPointerException already configured", exception.getMessage());
        }

        @Test
        @DisplayName("errorType already configured not-exactly")
        void testErrorTypeConfiguredNotExactly() {
            ThrowableAsserter<?> asserter = newAsserter();

            Consumer<IllegalArgumentException> consumer1 = mockedConsumer();
            Consumer<IllegalArgumentException> consumer2 = mockedConsumer();

            IllegalArgumentException error1 = new IllegalArgumentException();
            NumberFormatException error2 = new NumberFormatException();

            assertFalse(asserter.runAllAssertions(error1));

            verifyNoInteractions(consumer1, consumer2);

            asserter.whenThrows(IllegalArgumentException.class).thenAssert(consumer1);

            assertTrue(asserter.runAllAssertions(error1));
            assertTrue(asserter.runAllAssertions(error2));

            verify(consumer1).accept(error1);
            verify(consumer1).accept(error2);
            verifyNoMoreInteractions(consumer1, consumer2);

            asserter.whenThrowsExactly(IllegalArgumentException.class).thenAssert(consumer2);

            assertTrue(asserter.runAllAssertions(error1));
            assertTrue(asserter.runAllAssertions(error2));

            // consumer1.accept(error1) has occurred twice, because asserter.runAllAssertions(error1) has run it twice
            verify(consumer1, times(2)).accept(error1);
            // consumer1.accept(error2) has occurred twice, because asserter.runAllAssertions(error2) has run it twice
            verify(consumer1, times(2)).accept(error2);
            // consumer2 is not used for error2 because it was configured exactly
            verify(consumer2).accept(error1);
            verifyNoMoreInteractions(consumer1, consumer2);
        }

        private ThrowableAsserter<?> newAsserter() {
            return whenThrows(ParseException.class, Object::new).thenAssertNothing();
        }
    }

    @Nested
    @DisplayName("runAllAssertions")
    class RunAllAssertions {

        @Test
        @DisplayName("find configured exactly")
        void testFindConfiguredExactly() {
            Consumer<Throwable> consumer1 = mockedConsumer();
            Consumer<Throwable> consumer2 = mockedConsumer();

            ThrowableAsserter<?> asserter = whenThrows(IllegalArgumentException.class, Object::new).thenAssert(consumer1)
                    .whenThrowsExactly(IllegalArgumentException.class).thenAssert(consumer2);

            IllegalArgumentException error = new IllegalArgumentException();

            assertTrue(asserter.runAllAssertions(error));

            verify(consumer1).accept(error);
            verify(consumer2).accept(error);
            verifyNoMoreInteractions(consumer1, consumer2);
        }

        @Test
        @DisplayName("find configured non-exactly")
        void testFindConfiguredNonExactly() {
            Consumer<Throwable> consumer1 = mockedConsumer();
            Consumer<Throwable> consumer2 = mockedConsumer();

            ThrowableAsserter<?> asserter = whenThrows(IllegalArgumentException.class, Object::new).thenAssert(consumer1)
                    .whenThrows(NumberFormatException.class).thenAssert(consumer2);

            IllegalArgumentException error1 = new IllegalArgumentException();
            InvalidPathException error2 = new InvalidPathException("path", "error");
            NumberFormatException error3 = new NumberFormatException();

            assertTrue(asserter.runAllAssertions(error1));
            assertTrue(asserter.runAllAssertions(error2));
            assertTrue(asserter.runAllAssertions(error3));

            verify(consumer1).accept(error1);
            verify(consumer1).accept(error2);
            verify(consumer1).accept(error3);
            verify(consumer2).accept(error3);
            verifyNoMoreInteractions(consumer1, consumer2);
        }

        @Test
        @DisplayName("find non-configured non-exactly")
        void testFindNonConfigured() {
            Consumer<Throwable> consumer1 = mockedConsumer();
            Consumer<Throwable> consumer2 = mockedConsumer();

            ThrowableAsserter<?> asserter = whenThrows(IllegalArgumentException.class, Object::new).thenAssert(consumer1)
                    .whenThrows(NumberFormatException.class).thenAssert(consumer2);

            IOException error1 = new IOException();
            NoSuchFileException error2 = new NoSuchFileException("file");

            assertFalse(asserter.runAllAssertions(error1));
            assertFalse(asserter.runAllAssertions(error2));

            verifyNoInteractions(consumer1, consumer2);
        }
    }

    @Nested
    @DisplayName("execute")
    class Execute {

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

                    ThrowableAsserter<?> asserter = whenThrows(NullPointerException.class, Object::new).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssert(consumer);

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, asserter::execute);
                    assertEquals(String.format("Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                            NullPointerException.class.getName(), IOException.class.getName()),
                            error.getMessage());

                    verifyNoInteractions(consumer);
                }

                @Test
                @DisplayName("whenThrowsNothing called with Runnable")
                void whenThrowsNothingCalledWithRunnable() {
                    Consumer<Throwable> consumer = mockedConsumer();
                    Runnable runnable = mock(Runnable.class);

                    Asserted<?> asserted = whenThrows(NullPointerException.class, notThrowing()).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssert(runnable)
                            .execute();

                    verify(runnable).run();
                    verifyNoMoreInteractions(consumer, runnable);

                    assertIsEmpty(asserted.andReturnResult());
                    assertIsEmpty(asserted.andReturnErrorIfThrown());
                    assertIsEmpty(asserted.andReturnErrorIfThrownAs(Exception.class));

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturnError);
                    assertEquals("Nothing was thrown", exception.getMessage());

                    exception = assertThrows(IllegalStateException.class, () -> asserted.andReturnErrorAs(Exception.class));
                    assertEquals("Nothing was thrown", exception.getMessage());
                }

                @Test
                @DisplayName("whenThrowsNothing called with Consumer")
                void whenThrowsNothingCalled() {
                    Consumer<Throwable> consumer1 = mockedConsumer();
                    Consumer<String> consumer2 = mockedConsumer();

                    Asserted<?> asserted = whenThrows(NullPointerException.class, () -> "foo").thenAssert(consumer1)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssert(consumer2)
                            .execute();

                    verify(consumer2).accept("foo");
                    verifyNoMoreInteractions(consumer1, consumer2);

                    assertEquals("foo", assertIsPresent(asserted.andReturnResult()));
                    assertIsEmpty(asserted.andReturnErrorIfThrown());
                    assertIsEmpty(asserted.andReturnErrorIfThrownAs(Exception.class));

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturnError);
                    assertEquals("Nothing was thrown", exception.getMessage());

                    exception = assertThrows(IllegalStateException.class, () -> asserted.andReturnErrorAs(Exception.class));
                    assertEquals("Nothing was thrown", exception.getMessage());
                }

                @Test
                @DisplayName("whenThrowsNothing called without assertions")
                void whenThrowsNothingCalledWithoutAssertions() {
                    Consumer<Throwable> consumer = mockedConsumer();

                    Asserted<?> asserted = whenThrows(NullPointerException.class, () -> "foo").thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssertNothing()
                            .execute();

                    verifyNoMoreInteractions(consumer);

                    assertEquals("foo", assertIsPresent(asserted.andReturnResult()));
                    assertIsEmpty(asserted.andReturnErrorIfThrown());
                    assertIsEmpty(asserted.andReturnErrorIfThrownAs(Exception.class));

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturnError);
                    assertEquals("Nothing was thrown", exception.getMessage());

                    exception = assertThrows(IllegalStateException.class, () -> asserted.andReturnErrorAs(Exception.class));
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

                Asserted<?> asserted = whenThrows(NullPointerException.class, throwing(error)).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .execute();

                verify(consumer1).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertIsEmpty(asserted.andReturnResult());
                assertSame(error, asserted.andReturnError());
                assertSame(error, asserted.andReturnErrorAs(Exception.class));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrown()));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrownAs(Exception.class)));

                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorAs(IllegalArgumentException.class));
                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorIfThrownAs(IllegalArgumentException.class));
            }

            @Test
            @DisplayName("throwing second")
            void testThrowingSecond() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IOException error = new IOException("error");

                Asserted<?> asserted = whenThrows(NullPointerException.class, throwing(error)).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .execute();

                verify(consumer2).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertIsEmpty(asserted.andReturnResult());
                assertSame(error, asserted.andReturnError());
                assertSame(error, asserted.andReturnErrorAs(Exception.class));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrown()));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrownAs(Exception.class)));

                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorAs(IllegalArgumentException.class));
                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorIfThrownAs(IllegalArgumentException.class));
            }

            @Test
            @DisplayName("throwing other")
            void testThrowingOther() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IllegalArgumentException error = new IllegalArgumentException("error");

                ThrowableAsserter<?> asserter = whenThrows(NullPointerException.class, throwing(error)).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable);

                AssertionFailedError failure = assertThrows(AssertionFailedError.class, asserter::execute);
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

                    ThrowableAsserter<?> asserter = whenThrows(NullPointerException.class, Object::new).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssert(consumer);

                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> asserter.execute("error"));
                    assertEquals(String.format("error ==> Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                            NullPointerException.class.getName(), IOException.class.getName()),
                            error.getMessage());

                    verifyNoInteractions(consumer);
                }

                @Test
                @DisplayName("whenThrowsNothing called with Runnable")
                void whenThrowsNothingCalledWithRunnable() {
                    Consumer<Throwable> consumer = mockedConsumer();
                    Runnable runnable = mock(Runnable.class);

                    Asserted<?> asserted = whenThrows(NullPointerException.class, notThrowing()).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssert(runnable)
                            .execute("error");

                    verify(runnable).run();
                    verifyNoMoreInteractions(consumer, runnable);

                    assertIsEmpty(asserted.andReturnResult());
                    assertIsEmpty(asserted.andReturnErrorIfThrown());
                    assertIsEmpty(asserted.andReturnErrorIfThrownAs(Exception.class));

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturnError);
                    assertEquals("Nothing was thrown", exception.getMessage());

                    exception = assertThrows(IllegalStateException.class, () -> asserted.andReturnErrorAs(Exception.class));
                    assertEquals("Nothing was thrown", exception.getMessage());
                }

                @Test
                @DisplayName("whenThrowsNothing called with Consumer")
                void whenThrowsNothingCalled() {
                    Consumer<Throwable> consumer1 = mockedConsumer();
                    Consumer<String> consumer2 = mockedConsumer();

                    Asserted<?> asserted = whenThrows(NullPointerException.class, () -> "foo").thenAssert(consumer1)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssert(consumer2)
                            .execute("error");

                    verify(consumer2).accept("foo");
                    verifyNoMoreInteractions(consumer1, consumer2);

                    assertEquals("foo", assertIsPresent(asserted.andReturnResult()));
                    assertIsEmpty(asserted.andReturnErrorIfThrown());
                    assertIsEmpty(asserted.andReturnErrorIfThrownAs(Exception.class));

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturnError);
                    assertEquals("Nothing was thrown", exception.getMessage());

                    exception = assertThrows(IllegalStateException.class, () -> asserted.andReturnErrorAs(Exception.class));
                    assertEquals("Nothing was thrown", exception.getMessage());
                }

                @Test
                @DisplayName("whenThrowsNothing called without assertions")
                void whenThrowsNothingCalledWithoutAssertions() {
                    Consumer<Throwable> consumer = mockedConsumer();

                    Asserted<?> asserted = whenThrows(NullPointerException.class, () -> "foo").thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssertNothing()
                            .execute("error");

                    verifyNoMoreInteractions(consumer);

                    assertEquals("foo", assertIsPresent(asserted.andReturnResult()));
                    assertIsEmpty(asserted.andReturnErrorIfThrown());
                    assertIsEmpty(asserted.andReturnErrorIfThrownAs(Exception.class));

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturnError);
                    assertEquals("Nothing was thrown", exception.getMessage());

                    exception = assertThrows(IllegalStateException.class, () -> asserted.andReturnErrorAs(Exception.class));
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

                Asserted<?> asserted = whenThrows(NullPointerException.class, throwing(error)).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .execute("error");

                verify(consumer1).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertIsEmpty(asserted.andReturnResult());
                assertSame(error, asserted.andReturnError());
                assertSame(error, asserted.andReturnErrorAs(Exception.class));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrown()));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrownAs(Exception.class)));

                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorAs(IllegalArgumentException.class));
                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorIfThrownAs(IllegalArgumentException.class));
            }

            @Test
            @DisplayName("throwing second")
            void testThrowingSecond() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IOException error = new IOException("error");

                Asserted<?> asserted = whenThrows(NullPointerException.class, throwing(error)).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .execute("error");

                verify(consumer2).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertIsEmpty(asserted.andReturnResult());
                assertSame(error, asserted.andReturnError());
                assertSame(error, asserted.andReturnErrorAs(Exception.class));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrown()));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrownAs(Exception.class)));

                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorAs(IllegalArgumentException.class));
                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorIfThrownAs(IllegalArgumentException.class));
            }

            @Test
            @DisplayName("throwing other")
            void testThrowingOther() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IllegalArgumentException error = new IllegalArgumentException("error");

                ThrowableAsserter<?> asserter = whenThrows(NullPointerException.class, throwing(error)).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable);

                AssertionFailedError failure = assertThrows(AssertionFailedError.class, () -> asserter.execute("error"));
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

                    ThrowableAsserter<?> asserter = whenThrows(NullPointerException.class, Object::new).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssert(consumer);

                    Supplier<String> messageSupplier = () -> "error";
                    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> asserter.execute(messageSupplier));
                    assertEquals(String.format("error ==> Expected one of <%s>, <%s> to be thrown, but nothing was thrown.",
                            NullPointerException.class.getName(), IOException.class.getName()),
                            error.getMessage());

                    verifyNoInteractions(consumer);
                }

                @Test
                @DisplayName("whenThrowsNothing called with Runnable")
                void whenThrowsNothingCalledWithRunnable() {
                    Consumer<Throwable> consumer = mockedConsumer();
                    Runnable runnable = mock(Runnable.class);

                    Asserted<?> asserted = whenThrows(NullPointerException.class, notThrowing()).thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssert(runnable)
                            .execute(() -> "error");

                    verify(runnable).run();
                    verifyNoMoreInteractions(consumer, runnable);

                    assertIsEmpty(asserted.andReturnResult());
                    assertIsEmpty(asserted.andReturnErrorIfThrown());
                    assertIsEmpty(asserted.andReturnErrorIfThrownAs(Exception.class));

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturnError);
                    assertEquals("Nothing was thrown", exception.getMessage());

                    exception = assertThrows(IllegalStateException.class, () -> asserted.andReturnErrorAs(Exception.class));
                    assertEquals("Nothing was thrown", exception.getMessage());
                }

                @Test
                @DisplayName("whenThrowsNothing called with Consumer")
                void whenThrowsNothingCalled() {
                    Consumer<Throwable> consumer1 = mockedConsumer();
                    Consumer<String> consumer2 = mockedConsumer();

                    Asserted<?> asserted = whenThrows(NullPointerException.class, () -> "foo").thenAssert(consumer1)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssert(consumer2)
                            .execute(() -> "error");

                    verify(consumer2).accept("foo");
                    verifyNoMoreInteractions(consumer1, consumer2);

                    assertEquals("foo", assertIsPresent(asserted.andReturnResult()));
                    assertIsEmpty(asserted.andReturnErrorIfThrown());
                    assertIsEmpty(asserted.andReturnErrorIfThrownAs(Exception.class));

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturnError);
                    assertEquals("Nothing was thrown", exception.getMessage());

                    exception = assertThrows(IllegalStateException.class, () -> asserted.andReturnErrorAs(Exception.class));
                    assertEquals("Nothing was thrown", exception.getMessage());
                }

                @Test
                @DisplayName("whenThrowsNothing called without assertions")
                void whenThrowsNothingCalledWithoutAssertions() {
                    Consumer<Throwable> consumer = mockedConsumer();

                    Asserted<?> asserted = whenThrows(NullPointerException.class, () -> "foo").thenAssert(consumer)
                            .whenThrows(IOException.class).thenAssertNothing()
                            .whenThrowsNothing().thenAssertNothing()
                            .execute(() -> "error");

                    verifyNoMoreInteractions(consumer);

                    assertEquals("foo", assertIsPresent(asserted.andReturnResult()));
                    assertIsEmpty(asserted.andReturnErrorIfThrown());
                    assertIsEmpty(asserted.andReturnErrorIfThrownAs(Exception.class));

                    IllegalStateException exception = assertThrows(IllegalStateException.class, asserted::andReturnError);
                    assertEquals("Nothing was thrown", exception.getMessage());

                    exception = assertThrows(IllegalStateException.class, () -> asserted.andReturnErrorAs(Exception.class));
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

                Asserted<?> asserted = whenThrows(NullPointerException.class, throwing(error)).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .execute(() -> "error");

                verify(consumer1).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertIsEmpty(asserted.andReturnResult());
                assertSame(error, asserted.andReturnError());
                assertSame(error, asserted.andReturnErrorAs(Exception.class));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrown()));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrownAs(Exception.class)));

                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorAs(IllegalArgumentException.class));
                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorIfThrownAs(IllegalArgumentException.class));
            }

            @Test
            @DisplayName("throwing second")
            void testThrowingSecond() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IOException error = new IOException("error");

                Asserted<?> asserted = whenThrows(NullPointerException.class, throwing(error)).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable)
                        .execute(() -> "error");

                verify(consumer2).accept(error);
                verifyNoMoreInteractions(consumer1, consumer2, runnable);

                assertIsEmpty(asserted.andReturnResult());
                assertSame(error, asserted.andReturnError());
                assertSame(error, asserted.andReturnErrorAs(Exception.class));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrown()));
                assertSame(error, assertIsPresent(asserted.andReturnErrorIfThrownAs(Exception.class)));

                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorAs(IllegalArgumentException.class));
                assertThrows(ClassCastException.class, () -> asserted.andReturnErrorIfThrownAs(IllegalArgumentException.class));
            }

            @Test
            @DisplayName("throwing other")
            void testThrowingOther() {
                Consumer<NullPointerException> consumer1 = mockedConsumer();
                Consumer<IOException> consumer2 = mockedConsumer();
                Runnable runnable = mock(Runnable.class);

                IllegalArgumentException error = new IllegalArgumentException("error");

                ThrowableAsserter<?> asserter = whenThrows(NullPointerException.class, throwing(error)).thenAssert(consumer1)
                        .whenThrows(IOException.class).thenAssert(consumer2)
                        .whenThrowsNothing().thenAssert(runnable);

                Supplier<String> messageSupplier = () -> "error";
                AssertionFailedError failure = assertThrows(AssertionFailedError.class, () -> asserter.execute(messageSupplier));
                assertEquals(String.format("error ==> Unexpected exception type thrown, expected: one of <%s>, <%s> but was: <%s>",
                        NullPointerException.class.getName(), IOException.class.getName(), error.getClass().getName()),
                        failure.getMessage());
                assertSame(error, failure.getCause());

                verifyNoInteractions(consumer1, consumer2, runnable);
            }
        }
    }

    private Executable throwing(Throwable error) {
        return () -> {
            throw error;
        };
    }

    private Executable notThrowing() {
        return () -> {
            // do nothing
        };
    }

    @SuppressWarnings("unchecked")
    private <T> Consumer<T> mockedConsumer() {
        return mock(Consumer.class);
    }
}
