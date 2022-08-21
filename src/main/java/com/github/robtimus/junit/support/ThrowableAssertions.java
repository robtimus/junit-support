/*
 * ThrowableAssertions.java
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

import static com.github.robtimus.junit.support.AssertionFailedErrorBuilder.assertionFailedError;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;

/**
 * A collection of utility methods that support asserting conditions in tests for throwables and code that should or should not throw any exceptions,
 * in addition to what is already provided by {@link Assertions}.
 *
 * @author Rob Spoor
 * @since 2.0
 */
@SuppressWarnings("nls")
public final class ThrowableAssertions {

    private static final String CAUSED_BY = "caused by";

    private ThrowableAssertions() {
    }

    /**
     * Asserts that the supplied throwable has a direct cause of the given type, and returns this cause.
     * <p>
     * If the throwable has no cause, or the direct cause has a different type, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the cause instance, ignore the return value.
     *
     * @param <T> The expected cause type.
     * @param expectedType The expected cause type.
     * @param throwable The throwable to check.
     * @return The direct cause of the given exception, cast to the given type.
     */
    public static <T extends Throwable> T assertHasDirectCause(Class<T> expectedType, Throwable throwable) {
        return assertHasDirectCause(expectedType, throwable, (Object) null);
    }

    /**
     * Asserts that the supplied throwable has a cause, directly or indirectly, of the given type, and returns this cause.
     * <p>
     * If the throwable has no cause, or the direct cause has a different type, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the cause instance, ignore the return value.
     *
     * @param <T> The expected cause type.
     * @param expectedType The expected cause type.
     * @param throwable The throwable to check.
     * @param message The failure message to fail with.
     * @return The direct cause of the given exception, cast to the given type.
     */
    public static <T extends Throwable> T assertHasDirectCause(Class<T> expectedType, Throwable throwable, String message) {
        return assertHasDirectCause(expectedType, throwable, (Object) message);
    }

    /**
     * Asserts that the supplied throwable has a cause, directly or indirectly, of the given type, and returns this cause.
     * <p>
     * If the throwable has no cause, or the direct cause has a different type, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the cause instance, ignore the return value.
     *
     * @param <T> The expected cause type.
     * @param expectedType The expected cause type.
     * @param throwable The throwable to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The direct cause of the given exception, cast to the given type.
     */
    public static <T extends Throwable> T assertHasDirectCause(Class<T> expectedType, Throwable throwable, Supplier<String> messageSupplier) {
        return assertHasDirectCause(expectedType, throwable, (Object) messageSupplier);
    }

    private static <T extends Throwable> T assertHasDirectCause(Class<T> expectedType, Throwable throwable, Object messageOrSupplier) {
        Throwable cause = throwable.getCause();
        if (expectedType.isInstance(cause)) {
            return expectedType.cast(cause);
        }

        throw assertionFailedError()
                .message(messageOrSupplier)
                .prefixed(CAUSED_BY).expected(expectedType)
                .actual(cause)
                .build();
    }

    /**
     * Asserts that the supplied throwable has a cause, directly or indirectly, of the given type, and returns this cause.
     * <p>
     * If the throwable has no cause, or has no direct or indirect cause of the given type, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the cause instance, ignore the return value.
     *
     * @param <T> The expected cause type.
     * @param expectedType The expected cause type.
     * @param throwable The throwable to check.
     * @return The nearest cause of the given exception with the given type.
     */
    public static <T extends Throwable> T assertHasCause(Class<T> expectedType, Throwable throwable) {
        return assertHasCause(expectedType, throwable, (Object) null);
    }

    /**
     * Asserts that the supplied throwable has a cause, directly or indirectly, of the given type, and returns this cause.
     * <p>
     * If the throwable has no cause, or has no direct or indirect cause of the given type, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the cause instance, ignore the return value.
     *
     * @param <T> The expected cause type.
     * @param expectedType The expected cause type.
     * @param throwable The throwable to check.
     * @param message The failure message to fail with.
     * @return The nearest cause of the given exception with the given type.
     */
    public static <T extends Throwable> T assertHasCause(Class<T> expectedType, Throwable throwable, String message) {
        return assertHasCause(expectedType, throwable, (Object) message);
    }

    /**
     * Asserts that the supplied throwable has a cause, directly or indirectly, of the given type, and returns this cause.
     * <p>
     * If the throwable has no cause, or has no direct or indirect cause of the given type, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the cause instance, ignore the return value.
     *
     * @param <T> The expected cause type.
     * @param expectedType The expected cause type.
     * @param throwable The throwable to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The nearest cause of the given exception with the given type.
     */
    public static <T extends Throwable> T assertHasCause(Class<T> expectedType, Throwable throwable, Supplier<String> messageSupplier) {
        return assertHasCause(expectedType, throwable, (Object) messageSupplier);
    }

    private static <T extends Throwable> T assertHasCause(Class<T> expectedType, Throwable throwable, Object messageOrSupplier) {
        List<Throwable> causes = new ArrayList<>();

        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (expectedType.isInstance(cause)) {
                return expectedType.cast(cause);
            }
            causes.add(cause);
            cause = cause.getCause();
        }

        AssertionFailedErrorBuilder builder = assertionFailedError()
                .message(messageOrSupplier)
                .prefixed(CAUSED_BY).expected(expectedType);

        throw causes.isEmpty()
                ? builder.prefixed(CAUSED_BY).actual(null).build()
                : builder.prefixed(CAUSED_BY).actualValues(causes).build();
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of the supplied type, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The expected exception type.
     * @param expectedType The expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrows(Class, Executable)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrows(Class<T> expectedType, Executable executable) {
        return assertOptionallyThrows(expectedType, executable, (Object) null);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of the supplied type, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The expected exception type.
     * @param expectedType The expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrows(Class, Executable, String)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrows(Class<T> expectedType, Executable executable, String message) {
        return assertOptionallyThrows(expectedType, executable, (Object) message);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of the supplied type, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The expected exception type.
     * @param expectedType The expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrows(Class, Executable, Supplier)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrows(Class<T> expectedType, Executable executable,
            Supplier<String> messageSupplier) {

        return assertOptionallyThrows(expectedType, executable, (Object) messageSupplier);
    }

    private static <T extends Throwable> Optional<T> assertOptionallyThrows(Class<T> expectedType, Executable executable, Object messageOrSupplier) {
        try {
            executable.execute();
            return Optional.empty();
        } catch (Throwable actualException) {
            if (expectedType.isInstance(actualException)) {
                return Optional.of(expectedType.cast(actualException));
            }
            rethrowIfUnrecoverable(actualException);

            throw unexpectedExceptionTypeThrown()
                    .message(messageOrSupplier)
                    .expected(expectedType)
                    .actual(actualException.getClass())
                    .cause(actualException)
                    .build();
        }
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly the supplied type, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The expected exception type.
     * @param expectedType The expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsExactly(Class<T> expectedType, Executable executable) {
        return assertOptionallyThrowsExactly(expectedType, executable, (Object) null);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly the supplied type, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The expected exception type.
     * @param expectedType The expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable, String)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsExactly(Class<T> expectedType, Executable executable, String message) {
        return assertOptionallyThrowsExactly(expectedType, executable, (Object) message);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly the supplied type, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The expected exception type.
     * @param expectedType The expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable, Supplier)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsExactly(Class<T> expectedType, Executable executable,
            Supplier<String> messageSupplier) {

        return assertOptionallyThrowsExactly(expectedType, executable, (Object) messageSupplier);
    }

    private static <T extends Throwable> Optional<T> assertOptionallyThrowsExactly(Class<T> expectedType, Executable executable,
            Object messageOrSupplier) {

        try {
            executable.execute();
            return Optional.empty();
        } catch (Throwable actualException) {
            if (expectedType.equals(actualException.getClass())) {
                return Optional.of(expectedType.cast(actualException));
            }
            rethrowIfUnrecoverable(actualException);

            throw unexpectedExceptionTypeThrown()
                    .message(messageOrSupplier)
                    .expected(expectedType)
                    .actual(actualException.getClass())
                    .cause(actualException)
                    .build();
        }
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
     * and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @return The exception that was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable)
     */
    public static <T extends Throwable> T assertThrowsExactlyOneOf(Class<? extends T> expectedType1, Class<? extends T> expectedType2,
            Executable executable) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertThrowsExactlyOneOf(expectedTypes, executable);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
     * and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return The exception that was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable, String)
     */
    public static <T extends Throwable> T assertThrowsExactlyOneOf(Class<? extends T> expectedType1, Class<? extends T> expectedType2,
            Executable executable, String message) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertThrowsExactlyOneOf(expectedTypes, executable, message);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
     * and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The exception that was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable, Supplier)
     */
    public static <T extends Throwable> T assertThrowsExactlyOneOf(Class<? extends T> expectedType1, Class<? extends T> expectedType2,
            Executable executable, Supplier<String> messageSupplier) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertThrowsExactlyOneOf(expectedTypes, executable, messageSupplier);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
     * and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @return The exception that was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable)
     */
    public static <T extends Throwable> T assertThrowsExactlyOneOf(Collection<? extends Class<? extends T>> expectedTypes, Executable executable) {
        return assertThrowsExactlyOneOf(expectedTypes, executable, (Object) null);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
     * and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return The exception that was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable, String)
     */
    public static <T extends Throwable> T assertThrowsExactlyOneOf(Collection<? extends Class<? extends T>> expectedTypes, Executable executable,
            String message) {

        return assertThrowsExactlyOneOf(expectedTypes, executable, (Object) message);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
     * and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The exception that was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable, Supplier)
     */
    public static <T extends Throwable> T assertThrowsExactlyOneOf(Collection<? extends Class<? extends T>> expectedTypes, Executable executable,
            Supplier<String> messageSupplier) {

        return assertThrowsExactlyOneOf(expectedTypes, executable, (Object) messageSupplier);
    }

    private static <T extends Throwable> T assertThrowsExactlyOneOf(Collection<? extends Class<? extends T>> expectedTypes, Executable executable,
            Object messageOrSupplier) {

        return assertOptionallyThrowsExactlyOneOf(expectedTypes, executable, messageOrSupplier)
                .orElseThrow(() -> assertionFailedError()
                        .message(messageOrSupplier)
                        .reasonPattern("Expected one of %s to be thrown, but nothing was thrown.")
                                .withValues(expectedTypes)
                                .format()
                        .build());
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsExactlyOneOf(Class<? extends T> expectedType1,
            Class<? extends T> expectedType2, Executable executable) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertOptionallyThrowsExactlyOneOf(expectedTypes, executable);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable, String)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsExactlyOneOf(Class<? extends T> expectedType1,
            Class<? extends T> expectedType2, Executable executable, String message) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertOptionallyThrowsExactlyOneOf(expectedTypes, executable, message);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable, Supplier)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsExactlyOneOf(Class<? extends T> expectedType1,
            Class<? extends T> expectedType2, Executable executable, Supplier<String> messageSupplier) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertOptionallyThrowsExactlyOneOf(expectedTypes, executable, messageSupplier);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsExactlyOneOf(Collection<? extends Class<? extends T>> expectedTypes,
            Executable executable) {

        return assertOptionallyThrowsExactlyOneOf(expectedTypes, executable, (Object) null);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable, String)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsExactlyOneOf(Collection<? extends Class<? extends T>> expectedTypes,
            Executable executable, String message) {

        return assertOptionallyThrowsExactlyOneOf(expectedTypes, executable, (Object) message);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrowsExactly(Class, Executable, Supplier)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsExactlyOneOf(Collection<? extends Class<? extends T>> expectedTypes,
            Executable executable, Supplier<String> messageSupplier) {

        return assertOptionallyThrowsExactlyOneOf(expectedTypes, executable, (Object) messageSupplier);
    }

    private static <T extends Throwable> Optional<T> assertOptionallyThrowsExactlyOneOf(Collection<? extends Class<? extends T>> expectedTypes,
            Executable executable, Object messageOrSupplier) {

        try {
            executable.execute();
            return Optional.empty();
        } catch (Throwable actualException) {
            for (Class<? extends T> expectedType : expectedTypes) {
                if (expectedType.equals(actualException.getClass())) {
                    return Optional.of(expectedType.cast(actualException));
                }
            }
            rethrowIfUnrecoverable(actualException);

            throw unexpectedExceptionTypeThrown()
                    .message(messageOrSupplier)
                    .expectedOneOf(expectedTypes)
                    .actual(actualException.getClass())
                    .cause(actualException)
                    .build();
        }
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @return The exception that was thrown.
     * @see Assertions#assertThrows(Class, Executable)
     */
    public static <T extends Throwable> T assertThrowsOneOf(Class<? extends T> expectedType1, Class<? extends T> expectedType2,
            Executable executable) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertThrowsOneOf(expectedTypes, executable);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return The exception that was thrown.
     * @see Assertions#assertThrows(Class, Executable, String)
     */
    public static <T extends Throwable> T assertThrowsOneOf(Class<? extends T> expectedType1, Class<? extends T> expectedType2,
            Executable executable, String message) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertThrowsOneOf(expectedTypes, executable, message);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The exception that was thrown.
     * @see Assertions#assertThrows(Class, Executable, Supplier)
     */
    public static <T extends Throwable> T assertThrowsOneOf(Class<? extends T> expectedType1, Class<? extends T> expectedType2,
            Executable executable, Supplier<String> messageSupplier) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertThrowsOneOf(expectedTypes, executable, messageSupplier);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @return The exception that was thrown.
     * @see Assertions#assertThrows(Class, Executable)
     */
    public static <T extends Throwable> T assertThrowsOneOf(Collection<? extends Class<? extends T>> expectedTypes, Executable executable) {
        return assertThrowsOneOf(expectedTypes, executable, (Object) null);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return The exception that was thrown.
     * @see Assertions#assertThrows(Class, Executable, String)
     */
    public static <T extends Throwable> T assertThrowsOneOf(Collection<? extends Class<? extends T>> expectedTypes, Executable executable,
            String message) {

        return assertThrowsOneOf(expectedTypes, executable, (Object) message);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
     * <p>
     * If no exception is thrown, or if an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The exception that was thrown.
     * @see Assertions#assertThrows(Class, Executable, Supplier)
     */
    public static <T extends Throwable> T assertThrowsOneOf(Collection<? extends Class<? extends T>> expectedTypes, Executable executable,
            Supplier<String> messageSupplier) {

        return assertThrowsOneOf(expectedTypes, executable, (Object) messageSupplier);
    }

    private static <T extends Throwable> T assertThrowsOneOf(Collection<? extends Class<? extends T>> expectedTypes, Executable executable,
            Object messageOrSupplier) {

        return assertOptionallyThrowsOneOf(expectedTypes, executable, messageOrSupplier)
                .orElseThrow(() -> assertionFailedError()
                        .message(messageOrSupplier)
                        .reasonPattern("Expected one of %s to be thrown, but nothing was thrown.")
                                .withValues(expectedTypes)
                                .format()
                        .build());
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrows(Class, Executable)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsOneOf(Class<? extends T> expectedType1, Class<? extends T> expectedType2,
            Executable executable) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertOptionallyThrowsOneOf(expectedTypes, executable);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrows(Class, Executable, String)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsOneOf(Class<? extends T> expectedType1, Class<? extends T> expectedType2,
            Executable executable, String message) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertOptionallyThrowsOneOf(expectedTypes, executable, message);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedType1 The first expected exception type to check for.
     * @param expectedType2 The second expected exception type to check for.
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrows(Class, Executable, Supplier)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsOneOf(Class<? extends T> expectedType1, Class<? extends T> expectedType2,
            Executable executable, Supplier<String> messageSupplier) {

        List<Class<? extends T>> expectedTypes = Arrays.asList(expectedType1, expectedType2);
        return assertOptionallyThrowsOneOf(expectedTypes, executable, messageSupplier);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrows(Class, Executable)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsOneOf(Collection<? extends Class<? extends T>> expectedTypes,
            Executable executable) {

        return assertOptionallyThrowsOneOf(expectedTypes, executable, (Object) null);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @param message The failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrows(Class, Executable, String)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsOneOf(Collection<? extends Class<? extends T>> expectedTypes,
            Executable executable, String message) {

        return assertOptionallyThrowsOneOf(expectedTypes, executable, (Object) message);
    }

    /**
     * Asserts that execution of the supplied {@link Executable} throws an exception of one of the supplied types, or throws nothing at all.
     * If an exception was thrown, it is returned.
     * <p>
     * If an exception of a different type is thrown, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the exception instance, ignore the return value.
     *
     * @param <T> The common super type of the expected exception types.
     * @param expectedTypes The expected exception types to check for.
     * @param executable The {@link Executable} to run.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return An {@link Optional} describing the exception that was thrown, or {@link Optional#empty()} if no exception was thrown.
     * @see Assertions#assertThrows(Class, Executable, Supplier)
     */
    public static <T extends Throwable> Optional<T> assertOptionallyThrowsOneOf(Collection<? extends Class<? extends T>> expectedTypes,
            Executable executable, Supplier<String> messageSupplier) {

        return assertOptionallyThrowsOneOf(expectedTypes, executable, (Object) messageSupplier);
    }

    private static <T extends Throwable> Optional<T> assertOptionallyThrowsOneOf(Collection<? extends Class<? extends T>> expectedTypes,
            Executable executable, Object messageOrSupplier) {

        try {
            executable.execute();
            return Optional.empty();
        } catch (Throwable actualException) {
            for (Class<? extends T> expectedType : expectedTypes) {
                if (expectedType.isInstance(actualException)) {
                    return Optional.of(expectedType.cast(actualException));
                }
            }
            rethrowIfUnrecoverable(actualException);

            throw unexpectedExceptionTypeThrown()
                    .message(messageOrSupplier)
                    .expectedOneOf(expectedTypes)
                    .actual(actualException.getClass())
                    .cause(actualException)
                    .build();
        }
    }

    /**
     * Asserts that a piece of code does not throw a checked exception.
     * This method works a lot like {@link Assertions#assertDoesNotThrow(Executable)}, except any error or unchecked exception will not be caught.
     * It allows failed assertion errors to pass through.
     *
     * @param executable The piece of code that should not throw a checked exception.
     */
    public static void assertDoesNotThrowCheckedException(Executable executable) {
        try {
            executable.execute();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            assertDoesNotThrow(() -> {
                throw t;
            });
        }
    }

    /**
     * Asserts that a piece of code does not throw a checked exception.
     * This method works a lot like {@link Assertions#assertDoesNotThrow(Executable, String)}, except any error or unchecked exception will not be
     * caught. It allows failed assertion errors to pass through.
     *
     * @param executable The piece of code that should not throw a checked exception.
     * @param message The failure message to fail with.
     */
    public static void assertDoesNotThrowCheckedException(Executable executable, String message) {
        try {
            executable.execute();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            assertDoesNotThrow(() -> {
                throw t;
            }, message);
        }
    }

    /**
     * Asserts that a piece of code does not throw a checked exception.
     * This method works a lot like {@link Assertions#assertDoesNotThrow(Executable, Supplier)}, except any error or unchecked exception will not be
     * caught. It allows failed assertion errors to pass through.
     *
     * @param executable The piece of code that should not throw a checked exception.
     * @param messageSupplier The supplier for the failure message to fail with.
     */
    public static void assertDoesNotThrowCheckedException(Executable executable, Supplier<String> messageSupplier) {
        try {
            executable.execute();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            assertDoesNotThrow(() -> {
                throw t;
            }, messageSupplier);
        }
    }

    /**
     * Asserts that a piece of code does not throw a checked exception.
     * This method works a lot like {@link Assertions#assertDoesNotThrow(ThrowingSupplier)}, except any error or unchecked exception will not be
     * caught. It allows failed assertion errors to pass through.
     *
     * @param <T> The type of results supplied by the given supplier.
     * @param supplier The piece of code that should not throw a checked exception.
     * @return A result supplied by the given supplier.
     */
    public static <T> T assertDoesNotThrowCheckedException(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            return assertDoesNotThrow(() -> {
                throw t;
            });
        }
    }

    /**
     * Asserts that a piece of code does not throw a checked exception.
     * This method works a lot like {@link Assertions#assertDoesNotThrow(ThrowingSupplier, String)}, except any error or unchecked exception will not
     * be caught. It allows failed assertion errors to pass through.
     *
     * @param <T> The type of results supplied by the given supplier.
     * @param supplier The piece of code that should not throw a checked exception.
     * @param message The failure message to fail with.
     * @return A result supplied by the given supplier.
     */
    public static <T> T assertDoesNotThrowCheckedException(ThrowingSupplier<T> supplier, String message) {
        try {
            return supplier.get();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            return assertDoesNotThrow(() -> {
                throw t;
            }, message);
        }
    }

    /**
     * Asserts that a piece of code does not throw a checked exception.
     * This method works a lot like {@link Assertions#assertDoesNotThrow(ThrowingSupplier, Supplier)}, except any error or unchecked exception will
     * not be caught. It allows failed assertion errors to pass through.
     *
     * @param <T> The type of results supplied by the given supplier.
     * @param supplier The piece of code that should not throw a checked exception.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return A result supplied by the given supplier.
     */
    public static <T> T assertDoesNotThrowCheckedException(ThrowingSupplier<T> supplier, Supplier<String> messageSupplier) {
        try {
            return supplier.get();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            return assertDoesNotThrow(() -> {
                throw t;
            }, messageSupplier);
        }
    }

    static AssertionFailedErrorBuilder unexpectedExceptionTypeThrown() {
        return assertionFailedError().reason("Unexpected exception type thrown");
    }

    static void rethrowIfUnrecoverable(Throwable exception) {
        if (exception instanceof OutOfMemoryError) {
            throw (OutOfMemoryError) exception;
        }
    }
}
