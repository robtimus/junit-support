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

import static com.github.robtimus.junit.support.AssertionFailedErrorBuilder.assertionFailure;
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
import org.opentest4j.AssertionFailedError;

/**
 * An object that asserts that execution of an {@link Executable} throws an error.
 * <p>
 * This class is like several of the throwing assertions of {@link ThrowableAssertions}, especially the "one-of" assertions, but it provides more
 * flexibility. It also removes the need for checking the return type of the returned error in case of the "one-of" assertions; instead, a set of
 * assertions can be configured per expected error type (or none to just specify that the error type is one of the expected error types).
 * <p>
 * This class should be used as follows:
 * <ol>
 * <li>Create an instance using one of the {@code executing} methods.</li>
 * <li>Call {@link #whenThrows(Class)}, {@link #whenThrowsExactly(Class)} and {@link #whenThrowsNothing()} any number of times and in any order,
 *     and specify the assertions using {@code thenAssert} or {@code thenAssertNothing}.
 *     However, you must call {@link #whenThrows(Class)} or {@link #whenThrowsExactly(Class)} at least once. Also, all calls must be unique, i.e. you
 *     cannot call {@link #whenThrows(Class)} twice with the same type, or call {@link #whenThrowsExactly(Class)} twice with the same type, or call
 *     {@link #whenThrowsNothing()} twice.</li>
 * <li>Call {@link #runAssertions()}.</li>
 * <li>Optionally, call {@link Asserted#andReturn()}, {@link Asserted#andReturnAs(Class)}, {@link Asserted#andReturnIfThrown()} or
 *     {@link Asserted#andReturnIfThrownAs(Class)} to retrieve the error that was thrown.</li>
 * </ol>
 * <p>
 * An example:
 * <pre><code>
 * executing(() -&gt; map.computeIfAbsent(key, function))
 *         .whenThrows(UnsupportedOperationException.class).thenAssertNothing()
 *         .whenThrows(IllegalArgumentException.class).thenAssert(thrown -&gt; assertSame(exception, thrown))
 *         .runAssertions();
 * </code></pre>
 * <p>
 * All methods throw a {@link NullPointerException} when provided with {@code null} arguments.<br>
 *
 * @author Rob Spoor
 * @since 2.0
 */
@SuppressWarnings("nls")
public final class ThrowableAsserter {

    private static final Consumer<Object> DO_NOTHING_CONSUMER = o -> {
        // do nothing
    };
    private static final Runnable DO_NOTHING_RUNNABLE = () -> {
        // do nothing
    };

    private final Executable executable;
    private final Object messageOrSupplier;

    private final Map<Class<? extends Throwable>, Consumer<? super Throwable>> errors;
    private final Map<Class<? extends Throwable>, Consumer<? super Throwable>> exactErrors;
    private final Set<Class<? extends Throwable>> expectedErrorTypes;
    private Runnable nothingThrownAsserter;

    private State state;
    // The following are only set if state is CONFIGURING_ERROR_TYPE
    private Class<? extends Throwable> configuringErrorType;
    private boolean configuringExactErrorType;

    private ThrowableAsserter(Executable executable, Object messageOrSupplier) {
        this.executable = executable;
        this.messageOrSupplier = messageOrSupplier;

        errors = new HashMap<>();
        exactErrors = new HashMap<>();
        expectedErrorTypes = new LinkedHashSet<>();

        state = State.INITIALIZED;
    }

    /**
     * Returns an object that asserts that execution of the supplier {@link Executable} throws an error.
     * The possible types of errors can be configured on the returned object using {@link #whenThrows(Class)} and
     * {@link #whenThrowsExactly(Class)}. {@link #whenThrowsNothing()} can be called to indicate that throwing an error is optional.
     *
     * @param executable The {@link Executable} to run.
     * @return An object that asserts that execution of the supplier {@link Executable} throws an error.
     */
    public static ThrowableAsserter executing(Executable executable) {
        return executing(executable, (Object) null);
    }

    /**
     * Returns an object that asserts that execution of the supplier {@link Executable} throws an error.
     * The possible types of errors can be configured on the returned object using {@link #whenThrows(Class)} and
     * {@link #whenThrowsExactly(Class)}. {@link #whenThrowsNothing()} can be called to indicate that throwing an error is optional.
     *
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return An object that asserts that execution of the supplier {@link Executable} throws an error.
     */
    public static ThrowableAsserter executing(Executable executable, String message) {
        return executing(executable, (Object) message);
    }

    /**
     * Returns an object that asserts that execution of the supplier {@link Executable} throws an error.
     * The possible types of errors can be configured on the returned object using {@link #whenThrows(Class)} and
     * {@link #whenThrowsExactly(Class)}. {@link #whenThrowsNothing()} can be called to indicate that throwing an error is optional.
     *
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return An object that asserts that execution of the supplier {@link Executable} throws an error.
     */
    public static ThrowableAsserter executing(Executable executable, Supplier<String> messageSupplier) {
        return executing(executable, (Object) messageSupplier);
    }

    private static ThrowableAsserter executing(Executable executable, Object messageOrSupplier) {
        Objects.requireNonNull(executable);

        return new ThrowableAsserter(executable, messageOrSupplier);
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
    public <T extends Throwable> ThrownError<T> whenThrows(Class<T> errorType) {
        Objects.requireNonNull(errorType);
        return whenThrows(errorType, errors, false);
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
    public <T extends Throwable> ThrownError<T> whenThrowsExactly(Class<T> errorType) {
        Objects.requireNonNull(errorType);
        return whenThrows(errorType, exactErrors, true);
    }

    private <T extends Throwable> ThrownError<T> whenThrows(Class<T> errorType,
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

        return new ThrownError<>(errorType, errorMap, exact);
    }

    /**
     * Returns an object for configuring the assertions that should be performed if no error is thrown.
     * <p>
     * If this method is not called, {@link #runAssertions()} will fail if no error is thrown.
     *
     * @return An object for configuring the assertions that should be performed if no error is thrown.
     * @throws IllegalStateException If this method is called without configuring the assertions for at least one error type,
     *                                   or If this method has already been called.
     */
    public NoError whenThrowsNothing() {
        if (state != State.INITIALIZED && state != State.CONFIGURED) {
            throw new IllegalStateException("Cannot configure assertions for no error when current state is " + state);
        }

        if (nothingThrownAsserter != null) {
            throw new IllegalStateException("Assertions for no error already configured");
        }
        nothingThrownAsserter = DO_NOTHING_RUNNABLE;

        state = State.CONFIGURING_NO_ERROR;

        return new NoError();
    }

    /**
     * Runs the executable and the necessary configured assertions.
     *
     * @return This object.
     * @throws IllegalStateException If this method is called without configuring the assertions for at least one error type.
     * @throws AssertionFailedError If an error is thrown that is not an instance of one of the configured error types,
     *                                  or if no error is thrown and {@link #whenThrowsNothing()} has not been called.
     */
    public Asserted runAssertions() {
        if (state != State.CONFIGURED) {
            throw new IllegalStateException("Cannot run assertions when current state is " + state);
        }
        if (expectedErrorTypes.isEmpty()) {
            throw new IllegalStateException("Cannot run assertions without expected error types");
        }

        try {
            executable.execute();

        } catch (Throwable actualError) {
            runAssertionsForError(actualError);

            state = State.ASSERTED;

            return new Asserted(actualError);
        }

        runAssertionsWhenNothingThrown();

        state = State.ASSERTED;

        return new Asserted(null);
    }

    private void runAssertionsWhenNothingThrown() throws AssertionFailedError {
        if (nothingThrownAsserter != null) {
            nothingThrownAsserter.run();
            return;
        }

        throw assertionFailure()
                .message(messageOrSupplier)
                .reasonPattern("Expected one of %s to be thrown, but nothing was thrown.")
                        .withValues(expectedErrorTypes)
                        .format()
                .build();
    }

    private void runAssertionsForError(Throwable actualError) throws AssertionFailedError {
        Consumer<? super Throwable> asserter = findAsserter(actualError.getClass());
        if (asserter != null) {
            asserter.accept(actualError);
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

    Consumer<? super Throwable> findAsserter(Class<? extends Throwable> errorType) {
        Consumer<? super Throwable> asserter = exactErrors.get(errorType);
        if (asserter != null) {
            return asserter;
        }

        Class<?> iterator = errorType;
        while (iterator != Object.class) {
            asserter = errors.get(iterator);
            if (asserter != null) {
                return asserter;
            }
            iterator = iterator.getSuperclass();
        }

        return null;
    }

    /**
     * An object that can be used to configure the assertions that should be performed when an error is thrown.
     *
     * @author Rob Spoor
     * @param <T> The error type.
     * @since 2.0
     */
    public final class ThrownError<T extends Throwable> {

        private final Class<T> errorType;
        private final Map<Class<? extends Throwable>, Consumer<? super Throwable>> errorMap;
        private final boolean exact;

        private ThrownError(Class<T> errorType, Map<Class<? extends Throwable>, Consumer<? super Throwable>> errorMap, boolean exact) {
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
        public ThrowableAsserter thenAssert(Consumer<? super T> asserter) {
            Objects.requireNonNull(asserter);

            configureAssertions((Consumer<? super Throwable>) asserter);

            return ThrowableAsserter.this;
        }

        /**
         * Specifies that no assertions should be performed when an error is thrown.
         *
         * @return The error asserter that returned this object.
         */
        public ThrowableAsserter thenAssertNothing() {
            configureAssertions(DO_NOTHING_CONSUMER);

            return ThrowableAsserter.this;
        }

        private void configureAssertions(Consumer<? super Throwable> asserter) {
            if (state != State.CONFIGURING_ERROR_TYPE) {
                throw new IllegalStateException("Cannot specify assertions for an error type when current state is " + state);
            }
            if (errorType != configuringErrorType || exact != configuringExactErrorType) {
                throw new IllegalStateException(String.format("Cannot specify assertions; currently configuring for %s (exact: %b)",
                        configuringErrorType, configuringExactErrorType));
            }

            errorMap.put(errorType, asserter);

            state = State.CONFIGURED;
            configuringErrorType = null;
            configuringExactErrorType = false;
        }
    }

    /**
     * An object that can be used to configure the assertions that should be performed when no error is thrown.
     *
     * @author Rob Spoor
     * @since 2.0
     */
    public final class NoError {

        private NoError() {
        }

        /**
         * Specifies the assertions that should be performed when no error is thrown.
         *
         * @param asserter A runnable with the assertions that should be performed.
         * @return The error asserter that returned this object.
         * @throws NullPointerException If the given runnable is {@code null}.
         */
        public ThrowableAsserter thenAssert(Runnable asserter) {
            Objects.requireNonNull(asserter);

            configureAssertions(asserter);

            return ThrowableAsserter.this;
        }

        /**
         * Specifies that no assertions should be performed when no error is thrown.
         *
         * @return The error asserter that returned this object.
         */
        public ThrowableAsserter thenAssertNothing() {
            configureAssertions(DO_NOTHING_RUNNABLE);

            return ThrowableAsserter.this;
        }

        private void configureAssertions(Runnable asserter) {
            if (state != State.CONFIGURING_NO_ERROR) {
                throw new IllegalStateException("Cannot specify assertions for no error when current state is " + state);
            }

            nothingThrownAsserter = asserter;

            state = State.CONFIGURED;
        }
    }

    /**
     * An object that represents a {@link ThrowableAsserter} in its asserted state. It can be used to query the assertion results.
     *
     * @author Rob Spoor
     * @since 2.0
     */
    public final class Asserted {

        private final Throwable thrown;

        private Asserted(Throwable thrown) {
            this.thrown = thrown;
        }

        /**
         * Returns the error that was thrown.
         *
         * @return The error that was thrown.
         * @throws IllegalStateException If no error was thrown.
         */
        public Throwable andReturn() {
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
        public <T extends Throwable> T andReturnAs(Class<T> errorType) {
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
        public Optional<Throwable> andReturnIfThrown() {
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
        public <T extends Throwable> Optional<T> andReturnIfThrownAs(Class<T> errorType) {
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
