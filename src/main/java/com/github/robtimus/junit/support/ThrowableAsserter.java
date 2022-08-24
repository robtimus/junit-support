/*
 * ThrowableAsserter.java
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
import static com.github.robtimus.junit.support.ThrowableAssertions.rethrowIfUnrecoverable;
import static com.github.robtimus.junit.support.ThrowableAssertions.unexpectedExceptionTypeThrown;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.opentest4j.AssertionFailedError;

/**
 * An object that asserts that executing an {@link Executable} or retrieving the value of a {@link ThrowingSupplier} throws an error.
 * <p>
 * This class is like several of the throwing assertions of {@link ThrowableAssertions}, especially the "one-of" assertions, but it provides more
 * flexibility. It also removes the need for checking the return type of the returned error in case of the "one-of" assertions; instead, a set of
 * assertions can be configured per expected error type (or none to just specify that the error type is one of the expected error types).
 * <p>
 * This class should be used as follows:
 * <ol>
 * <li>Call one of the static {@code whenThrows} or {@code whenThrowsExactly} methods to create an instance, and specify the assertions for that error
 *     type using {@code thenAssert} or {@code thenAssertNothing}.</li>
 * <li>Call {@code whenThrows} and {@code whenThrowsExactly} any number of times and in any order, and specify the assertions for that error type
 *     using {@code thenAssert} or {@code thenAssertNothing}.<br>
 *     However, all calls must be unique, i.e. you cannot call {@code whenThrows} twice with the same type, or call {@code whenThrowsExactly} twice
 *     with the same type</li>
 * <li>Call {@code whenThrowsNothing} at most once, and specify the assertions for no error using {@code thenAssert} or {@code thenAssertNothing}.
 *     </li>
 * <li>Call {@code execute}. This will execute the {@link Executable} or retrieve the value of the {@link ThrowingSupplier}, and perform the necessary
 *     assertions.</li>
 * <li>Optionally, use the return value of the {@code execute} method to retrieve the error that was thrown or the {@link ThrowingSupplier}'s value.
 *     </li>
 * </ol>
 * <p>
 * An example:
 * <pre><code>
 * whenThrows(UnsupportedOperationException.class, () -&gt; map.computeIfAbsent(key, function)).thenAssertNothing()
 *         .whenThrows(IllegalArgumentException.class).thenAssert(thrown -&gt; assertSame(exception, thrown))
 *         .execute();
 * </code></pre>
 * <p>
 * All methods throw a {@link NullPointerException} when provided with {@code null} arguments unless specified otherwise.
 *
 * @author Rob Spoor
 * @param <R> The result type of the code to execute.
 * @since 2.0
 */
@SuppressWarnings("nls")
public final class ThrowableAsserter<R> {

    private static final Consumer<Object> DO_NOTHING_CONSUMER = o -> {
        // do nothing
    };

    private final ThrowingSupplier<R> supplier;

    private final Map<Class<? extends Throwable>, Consumer<? super Throwable>> errors;
    private final Map<Class<? extends Throwable>, Consumer<? super Throwable>> exactErrors;
    private final Set<Class<? extends Throwable>> expectedErrorTypes;
    private Consumer<? super R> nothingThrownAsserter;

    private State state;
    // The following are only set if state is CONFIGURING_ERROR_TYPE
    private Class<? extends Throwable> configuringErrorType;
    private boolean configuringExactErrorType;

    private ThrowableAsserter(ThrowingSupplier<R> supplier) {
        this.supplier = supplier;

        errors = new HashMap<>();
        exactErrors = new HashMap<>();
        expectedErrorTypes = new LinkedHashSet<>();

        state = State.INITIALIZED;
    }

    private static ThrowingSupplier<Void> asThrowableSupplier(Executable executable) {
        return () -> {
            executable.execute();
            return null;
        };
    }

    /**
     * Returns an object for configuring the assertions that should be performed when an instance of a specific error type is thrown when an
     * {@link Executable} is run.
     *
     * @param <T> The error type.
     * @param errorType The error type.
     * @param executable The {@link Executable} to run.
     * @return An object for configuring the assertions that should be performed when an instance of a specific error type is thrown.
     * @see Assertions#assertThrows(Class, Executable)
     */
    public static <T extends Throwable> ThrownError<T, Void> whenThrows(Class<T> errorType, Executable executable) {
        return whenThrows(errorType, asThrowableSupplier(executable));
    }

    /**
     * Returns an object for configuring the assertions that should be performed when an instance of a specific error type is thrown when the result
     * of a {@link ThrowingSupplier} is retrieved.
     *
     * @param <T> The error type.
     * @param <R> The result type of the {@link ThrowingSupplier}.
     * @param errorType The error type.
     * @param supplier The {@link ThrowingSupplier} with the result to retrieve.
     * @return An object for configuring the assertions that should be performed when an instance of a specific error type is thrown.
     * @see Assertions#assertThrows(Class, Executable)
     */
    public static <T extends Throwable, R> ThrownError<T, R> whenThrows(Class<T> errorType, ThrowingSupplier<R> supplier) {
        Objects.requireNonNull(supplier);
        return new ThrowableAsserter<>(supplier).whenThrows(errorType);
    }

    /**
     * Returns an object for configuring the assertions that should be performed when an instance of a specific error type is thrown.
     *
     * @param <T> The error type.
     * @param errorType The error type.
     * @return An object for configuring the assertions that should be performed when an instance of a specific error type is thrown.
     * @throws IllegalArgumentException If this method has already been called with the given error type.
     * @see Assertions#assertThrows(Class, Executable)
     */
    public <T extends Throwable> ThrownError<T, R> whenThrows(Class<T> errorType) {
        Objects.requireNonNull(errorType);
        return whenThrows(errorType, errors, false);
    }

    /**
     * Returns an object for configuring the assertions that should be performed when an exact instance of a specific error type is thrown when an
     * {@link Executable} is run.
     *
     * @param <T> The error type.
     * @param errorType The error type.
     * @param executable The {@link Executable} to run.
     * @return An object for configuring the assertions that should be performed when an exact instance of a specific error type is thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable)
     */
    public static <T extends Throwable> ThrownError<T, Void> whenThrowsExactly(Class<T> errorType, Executable executable) {
        return whenThrowsExactly(errorType, asThrowableSupplier(executable));
    }

    /**
     * Returns an object for configuring the assertions that should be performed when an exact instance of a specific error type is thrown when the
     * result of a {@link ThrowingSupplier} is retrieved.
     *
     * @param <T> The error type.
     * @param <R> The result type of the {@link ThrowingSupplier}.
     * @param errorType The error type.
     * @param supplier The {@link ThrowingSupplier} with the result to retrieve.
     * @return An object for configuring the assertions that should be performed when an exact instance of a specific error type is thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable)
     */
    public static <T extends Throwable, R> ThrownError<T, R> whenThrowsExactly(Class<T> errorType, ThrowingSupplier<R> supplier) {
        Objects.requireNonNull(supplier);
        return new ThrowableAsserter<>(supplier).whenThrowsExactly(errorType);
    }

    /**
     * Returns an object for configuring the assertions that should be performed when an exact instance of a specific error type is thrown.
     *
     * @param <T> The error type.
     * @param errorType The error type.
     * @return An object for configuring the assertions that should be performed when an exact instance of a specific error type is thrown.
     * @throws IllegalArgumentException If this method has already been called with the given error type.
     * @see Assertions#assertThrowsExactly(Class, Executable)
     */
    public <T extends Throwable> ThrownError<T, R> whenThrowsExactly(Class<T> errorType) {
        Objects.requireNonNull(errorType);
        return whenThrows(errorType, exactErrors, true);
    }

    private <T extends Throwable> ThrownError<T, R> whenThrows(Class<T> errorType,
            Map<Class<? extends Throwable>, Consumer<? super Throwable>> errorMap, boolean exact) {

        if (state != State.INITIALIZED && state != State.CONFIGURED) {
            throw new IllegalStateException("Cannot configure assertions for an error type when current state is " + state);
        }

        errorMap.merge(errorType, DO_NOTHING_CONSUMER, (c1, c2) -> {
            throw new IllegalArgumentException(errorType + " already configured");
        });
        expectedErrorTypes.add(errorType);

        state = State.CONFIGURING_ERROR_TYPE;
        configuringErrorType = errorType;
        configuringExactErrorType = exact;

        return new ThrownError<>(this, errorType, errorMap, exact);
    }

    /**
     * Returns an object for configuring the assertions that should be performed if no error is thrown.
     * <p>
     * If this method is not called, {@link #execute()}, {@link #execute(String)} and {@link #execute(Supplier)} will fail if no error is thrown.
     *
     * @return An object for configuring the assertions that should be performed if no error is thrown.
     * @throws IllegalStateException If this method is called without configuring the assertions for at least one error type,
     *                                   or If this method has already been called.
     */
    public NoError<R> whenThrowsNothing() {
        if (state != State.CONFIGURED) {
            throw new IllegalStateException("Cannot configure assertions for no error when current state is " + state);
        }

        if (nothingThrownAsserter != null) {
            throw new IllegalStateException("Assertions for no error already configured");
        }
        nothingThrownAsserter = DO_NOTHING_CONSUMER;

        state = State.CONFIGURING_NO_ERROR;

        return new NoError<>(this);
    }

    /**
     * Executes the {@link Executable} or retrieves the value of the {@link ThrowingSupplier} used to create this object, and perform the necessary
     * assertions.
     *
     * @return An object that represents this object in its asserted state.
     * @throws AssertionFailedError If an error is thrown that is not an instance of one of the configured error types,
     *                                  or if no error is thrown and {@link #whenThrowsNothing()} has not been called.
     */
    public Asserted<R> execute() {
        return execute((Object) null);
    }

    /**
     * Executes the {@link Executable} or retrieves the value of the {@link ThrowingSupplier} used to create this object, and perform the necessary
     * assertions.
     *
     * @param message The failure message to fail with; may be {@code null}.
     * @return An object that represents this object in its asserted state.
     * @throws AssertionFailedError If an error is thrown that is not an instance of one of the configured error types,
     *                                  or if no error is thrown and {@link #whenThrowsNothing()} has not been called.
     */
    public Asserted<R> execute(String message) {
        return execute((Object) message);
    }

    /**
     * Executes the {@link Executable} or retrieves the value of the {@link ThrowingSupplier} used to create this object, and perform the necessary
     * assertions.
     *
     * @param messageSupplier The supplier for the failure message to fail with; may be {@code null}.
     * @return An object that represents this object in its asserted state.
     * @throws AssertionFailedError If an error is thrown that is not an instance of one of the configured error types,
     *                                  or if no error is thrown and {@link #whenThrowsNothing()} has not been called.
     */
    public Asserted<R> execute(Supplier<String> messageSupplier) {
        return execute((Object) messageSupplier);
    }

    private Asserted<R> execute(Object messageOrSupplier) {
        if (state != State.CONFIGURED) {
            throw new IllegalStateException("Cannot run assertions when current state is " + state);
        }

        R result;

        try {
            result = supplier.get();

        } catch (Throwable actualError) {
            runAssertionsForError(actualError, messageOrSupplier);

            state = State.ASSERTED;

            return new Asserted<>(actualError);
        }

        runAssertionsWhenNothingThrown(result, messageOrSupplier);

        state = State.ASSERTED;

        return new Asserted<>(result);
    }

    private void runAssertionsWhenNothingThrown(R result, Object messageOrSupplier) throws AssertionFailedError {
        if (nothingThrownAsserter != null) {
            nothingThrownAsserter.accept(result);
            return;
        }

        throw assertionFailedError()
                .message(messageOrSupplier)
                .reasonPattern("Expected one of %s to be thrown, but nothing was thrown.")
                        .withValues(expectedErrorTypes)
                        .format()
                .build();
    }

    private void runAssertionsForError(Throwable actualError, Object messageOrSupplier) throws AssertionFailedError {
        boolean hasRunAssertions = runAllAssertions(actualError);
        if (hasRunAssertions) {
            return;
        }

        rethrowIfUnrecoverable(actualError);

        throw unexpectedExceptionTypeThrown()
                .message(messageOrSupplier)
                .expectedOneOf(expectedErrorTypes)
                .actual(actualError.getClass())
                .cause(actualError)
                .build();
    }

    boolean runAllAssertions(Throwable actualError) {
        Class<? extends Throwable> errorType = actualError.getClass();
        boolean hasRunAssertions = false;

        Consumer<? super Throwable> asserter = exactErrors.get(errorType);
        if (asserter != null) {
            asserter.accept(actualError);
            hasRunAssertions = true;
        }

        Class<?> iterator = errorType;
        while (iterator != Object.class) {
            asserter = errors.get(iterator);
            if (asserter != null) {
                asserter.accept(actualError);
                hasRunAssertions = true;
            }
            iterator = iterator.getSuperclass();
        }

        return hasRunAssertions;
    }

    /**
     * An object that can be used to configure the assertions that should be performed when an error is thrown.
     *
     * @author Rob Spoor
     * @param <T> The error type.
     * @param <R> The result type of the code to execute.
     * @since 2.0
     */
    public static final class ThrownError<T extends Throwable, R> {

        private final ThrowableAsserter<R> throwableAsserter;
        private final Class<T> errorType;
        private final Map<Class<? extends Throwable>, Consumer<? super Throwable>> errorMap;
        private final boolean exact;

        private ThrownError(ThrowableAsserter<R> throwableAsserter, Class<T> errorType,
                Map<Class<? extends Throwable>, Consumer<? super Throwable>> errorMap, boolean exact) {

            this.throwableAsserter = throwableAsserter;
            this.errorType = errorType;
            this.errorMap = errorMap;
            this.exact = exact;
        }

        /**
         * Specifies the assertions that should be performed when an error is thrown.
         *
         * @param asserter An operation with the assertions that should be performed. The thrown error will be the operation's input.
         * @return The error asserter that returned this object.
         * @throws NullPointerException If the given operation is {@code null}.
         */
        @SuppressWarnings("unchecked")
        public ThrowableAsserter<R> thenAssert(Consumer<? super T> asserter) {
            Objects.requireNonNull(asserter);

            return configureAssertions((Consumer<? super Throwable>) asserter);
        }

        /**
         * Specifies that no assertions should be performed when an error is thrown.
         *
         * @return The error asserter that returned this object.
         */
        public ThrowableAsserter<R> thenAssertNothing() {
            return configureAssertions(DO_NOTHING_CONSUMER);
        }

        private ThrowableAsserter<R> configureAssertions(Consumer<? super Throwable> asserter) {
            if (throwableAsserter.state != State.CONFIGURING_ERROR_TYPE) {
                throw new IllegalStateException("Cannot specify assertions for an error type when current state is " + throwableAsserter.state);
            }
            if (errorType != throwableAsserter.configuringErrorType || exact != throwableAsserter.configuringExactErrorType) {
                throw new IllegalStateException(String.format("Cannot specify assertions; currently configuring for %s (exact: %b)",
                        throwableAsserter.configuringErrorType, throwableAsserter.configuringExactErrorType));
            }

            errorMap.put(errorType, asserter);

            throwableAsserter.state = State.CONFIGURED;
            throwableAsserter.configuringErrorType = null;
            throwableAsserter.configuringExactErrorType = false;

            return throwableAsserter;
        }

        ThrowableAsserter<R> throwableAsserter() {
            return throwableAsserter;
        }
    }

    /**
     * An object that can be used to configure the assertions that should be performed when no error is thrown.
     *
     * @author Rob Spoor
     * @param <R> The result type of the code to execute.
     * @since 2.0
     */
    public static final class NoError<R> {

        private final ThrowableAsserter<R> throwableAsserter;

        private NoError(ThrowableAsserter<R> throwableAsserter) {
            this.throwableAsserter = throwableAsserter;
        }

        /**
         * Specifies the assertions that should be performed when no error is thrown.
         *
         * @param asserter A runnable with the assertions that should be performed.
         * @return The error asserter that returned this object.
         * @throws NullPointerException If the given runnable is {@code null}.
         */
        public ThrowableAsserter<R> thenAssert(Runnable asserter) {
            Objects.requireNonNull(asserter);

            return configureAssertions(r -> asserter.run());
        }

        /**
         * Specifies the assertions that should be performed when no error is thrown.
         *
         * @param asserter An operation with the assertions that should be performed. The result of the executed code will be the operation's input.
         * @return The error asserter that returned this object.
         * @throws NullPointerException If the given operation is {@code null}.
         */
        public ThrowableAsserter<R> thenAssert(Consumer<? super R> asserter) {
            Objects.requireNonNull(asserter);

            return configureAssertions(asserter);
        }

        /**
         * Specifies that no assertions should be performed when no error is thrown.
         *
         * @return The error asserter that returned this object.
         */
        public ThrowableAsserter<R> thenAssertNothing() {
            return configureAssertions(DO_NOTHING_CONSUMER);
        }

        private ThrowableAsserter<R> configureAssertions(Consumer<? super R> asserter) {
            if (throwableAsserter.state != State.CONFIGURING_NO_ERROR) {
                throw new IllegalStateException("Cannot specify assertions for no error when current state is " + throwableAsserter.state);
            }

            throwableAsserter.nothingThrownAsserter = asserter;

            throwableAsserter.state = State.CONFIGURED;

            return throwableAsserter;
        }

        ThrowableAsserter<R> throwableAsserter() {
            return throwableAsserter;
        }
    }

    /**
     * An object that represents a {@link ThrowableAsserter} in its asserted state. It can be used to query the assertion results.
     *
     * @author Rob Spoor
     * @param <R> The result type.
     * @since 2.0
     */
    public static final class Asserted<R> {

        private final R result;
        private final Throwable thrown;

        private Asserted(R result) {
            this.result = result;
            this.thrown = null;
        }

        private Asserted(Throwable thrown) {
            this.result = null;
            this.thrown = thrown;
        }

        /**
         * Returns the result of the executed code.
         *
         * @return An {@link Optional} describing the result, or {@link Optional#empty()} if an error was thrown or the result was {@code null}.
         */
        public Optional<R> andReturnResult() {
            return Optional.ofNullable(result);
        }

        /**
         * Returns the error that was thrown.
         *
         * @return The error that was thrown.
         * @throws IllegalStateException If no error was thrown.
         */
        public Throwable andReturnError() {
            if (thrown == null) {
                throw new IllegalStateException("Nothing was thrown");
            }
            return thrown;
        }

        /**
         * Returns the error that was thrown.
         *
         * @param <T> The expected type of error.
         * @param errorType The expected type of error.
         *                      This should be a common super type of all configured error types to prevent any {@link ClassCastException}s.
         * @return The error that was thrown.
         * @throws IllegalStateException If no error was thrown.
         * @throws ClassCastException If the error that was thrown is not an instance of the given error type.
         */
        public <T extends Throwable> T andReturnErrorAs(Class<T> errorType) {
            if (thrown == null) {
                throw new IllegalStateException("Nothing was thrown");
            }
            return errorType.cast(thrown);
        }

        /**
         * Returns the error that was thrown.
         *
         * @return An {@link Optional} describing the error that was thrown, or {@link Optional#empty()} if no error was thrown.
         */
        public Optional<Throwable> andReturnErrorIfThrown() {
            return Optional.ofNullable(thrown);
        }

        /**
         * Returns the error that was thrown.
         *
         * @param <T> The expected type of error.
         * @param errorType The expected type of error.
         *                      This should be a common super type of all configured error types to prevent any {@link ClassCastException}s.
         * @return An {@link Optional} describing the error that was thrown, or {@link Optional#empty()} if no error was thrown.
         * @throws ClassCastException If an error was thrown that is not an instance of the given error type.
         */
        public <T extends Throwable> Optional<T> andReturnErrorIfThrownAs(Class<T> errorType) {
            return thrown == null
                    ? Optional.empty()
                    : Optional.of(errorType.cast(thrown));
        }
    }

    State state() {
        return state;
    }

    Class<? extends Throwable> configuringErrorType() {
        return configuringErrorType;
    }

    boolean configuringExactErrorType() {
        return configuringExactErrorType;
    }

    enum State {
        INITIALIZED("initialized"),
        CONFIGURING_ERROR_TYPE("configuring assertions for an error type"),
        CONFIGURING_NO_ERROR("configuring assertions for no error"),
        CONFIGURED("configured"),
        ASSERTED("asserted"),
        ;

        private final String stringValue;

        State(String stringValue) {
            this.stringValue = stringValue;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }
}
