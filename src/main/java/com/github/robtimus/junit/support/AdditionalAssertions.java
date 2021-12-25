/*
 * AdditionalAssertions.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.opentest4j.AssertionFailedError;

/**
 * A collection of utility methods that support asserting conditions in tests, in addition to what is already provided by {@link Assertions}.
 *
 * @author Rob Spoor
 */
@SuppressWarnings("nls")
public final class AdditionalAssertions {

    private AdditionalAssertions() {
    }

    /**
     * <em>Assert</em> that the supplied throwable has a direct cause of the given type, and returns this cause.
     * <p>
     * If the throwable has no cause, or the direct cause has a different type, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the cause instance, ignore the return value.
     *
     * @param <T> The expected cause type.
     * @param expectedType The expected cause type.
     * @param throwable The throwable to check.
     * @return The nearest cause of the given exception with the given type.
     */
    public static <T extends Throwable> T assertHasDirectCause(Class<T> expectedType, Throwable throwable) {
        return assertHasDirectCause(expectedType, throwable, (Object) null);
    }

    /**
     * <em>Assert</em> that the supplied throwable has a cause, directly or indirectly, of the given type, and returns this cause.
     * <p>
     * If the throwable has no cause, or the direct cause has a different type, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the cause instance, ignore the return value.
     *
     * @param <T> The expected cause type.
     * @param expectedType The expected cause type.
     * @param throwable The throwable to check.
     * @param message The failure message to fail with.
     * @return The nearest cause of the given exception with the given type.
     */
    public static <T extends Throwable> T assertHasDirectCause(Class<T> expectedType, Throwable throwable, String message) {
        return assertHasDirectCause(expectedType, throwable, (Object) message);
    }

    /**
     * <em>Assert</em> that the supplied throwable has a cause, directly or indirectly, of the given type, and returns this cause.
     * <p>
     * If the throwable has no cause, or the direct cause has a different type, this method will fail.
     * <p>
     * If you do not want to perform additional checks on the cause instance, ignore the return value.
     *
     * @param <T> The expected cause type.
     * @param expectedType The expected cause type.
     * @param throwable The throwable to check.
     * @param messageSupplier The supplier for the failure message to fail with.
     * @return The nearest cause of the given exception with the given type.
     */
    public static <T extends Throwable> T assertHasDirectCause(Class<T> expectedType, Throwable throwable, Supplier<String> messageSupplier) {
        return assertHasDirectCause(expectedType, throwable, (Object) messageSupplier);
    }

    private static <T extends Throwable> T assertHasDirectCause(Class<T> expectedType, Throwable throwable, Object messageOrSupplier) {
        Throwable cause = throwable.getCause();
        if (expectedType.isInstance(cause)) {
            return expectedType.cast(cause);
        }

        String message = String.format("%sexpected caused by %s but was: <%s>",
                buildPrefix(nullSafeGet(messageOrSupplier)),
                formatClass(expectedType),
                cause);
        throw new AssertionFailedError(message);
    }

    /**
     * <em>Assert</em> that the supplied throwable has a cause, directly or indirectly, of the given type, and returns this cause.
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
     * <em>Assert</em> that the supplied throwable has a cause, directly or indirectly, of the given type, and returns this cause.
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
     * <em>Assert</em> that the supplied throwable has a cause, directly or indirectly, of the given type, and returns this cause.
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
        List<String> causes = new ArrayList<>();

        Throwable cause = throwable.getCause();
        while (cause != null && !expectedType.isInstance(cause)) {
            causes.add("<" + cause + ">");
            cause = cause.getCause();
        }
        if (cause != null) {
            // expectedType.isInstance(cause)
            return expectedType.cast(cause);
        }

        String message = String.format("%sexpected caused by %s but was: %s",
                buildPrefix(nullSafeGet(messageOrSupplier)),
                formatClass(expectedType),
                causes);
        throw new AssertionFailedError(message);
    }

    /**
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
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
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
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
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
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
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
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
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
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
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of exactly one of the supplied types,
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

        try {
            executable.execute();
        } catch (Throwable actualException) {
            for (Class<? extends T> expectedType : expectedTypes) {
                if (expectedType.equals(actualException.getClass())) {
                    return expectedType.cast(actualException);
                }
            }
            UnrecoverableExceptions.rethrowIfUnrecoverable(actualException);

            String message = String.format("%s%sexpected: one of %s but was: %s",
                    buildPrefix(nullSafeGet(messageOrSupplier)),
                    buildPrefix("Unexpected exception type thrown"),
                    formatClasses(expectedTypes),
                    formatClass(actualException.getClass()));
            throw new AssertionFailedError(message, actualException);
        }

        String message = String.format("%sExpected one of %s to be thrown, but nothing was thrown.",
                buildPrefix(nullSafeGet(messageOrSupplier)),
                formatClasses(expectedTypes));
        throw new AssertionFailedError(message);
    }

    /**
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
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
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
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
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
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
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
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
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
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
     * <em>Assert</em> that execution of the supplied {@link Executable} throws an exception of one of the supplied types, and returns the exception.
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

        try {
            executable.execute();
        } catch (Throwable actualException) {
            for (Class<? extends T> expectedType : expectedTypes) {
                if (expectedType.isInstance(actualException)) {
                    return expectedType.cast(actualException);
                }
            }
            UnrecoverableExceptions.rethrowIfUnrecoverable(actualException);

            String message = String.format("%s%sexpected: one of %s but was: %s",
                    buildPrefix(nullSafeGet(messageOrSupplier)),
                    buildPrefix("Unexpected exception type thrown"),
                    formatClasses(expectedTypes),
                    formatClass(actualException.getClass()));
            throw new AssertionFailedError(message, actualException);
        }

        String message = String.format("%sExpected one of %s to be thrown, but nothing was thrown.",
                buildPrefix(nullSafeGet(messageOrSupplier)),
                formatClasses(expectedTypes));
        throw new AssertionFailedError(message);
    }

    private static String formatClasses(Collection<? extends Class<?>> classes) {
        return classes.stream()
                .map(AdditionalAssertions::formatClass)
                .collect(Collectors.joining(", "));
    }

    private static String formatClass(Class<?> clazz) {
        String canonicalName = clazz.getCanonicalName();
        return "<" + (canonicalName != null ? canonicalName : clazz.getName()) + ">";
    }

    // Copy of JUnit's own methods

    private static String nullSafeGet(Object messageOrSupplier) {
        if (messageOrSupplier instanceof String) {
            return (String) messageOrSupplier;
        }
        if (messageOrSupplier instanceof Supplier<?>) {
            Object message = ((Supplier<?>) messageOrSupplier).get();
            if (message != null) {
                return message.toString();
            }
        }
        return null;
    }

    private static String buildPrefix(String message) {
        return StringUtils.isNotBlank(message) ? message + " ==> " : "";
    }
}
