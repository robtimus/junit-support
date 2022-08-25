/*
 * AssertionFailedErrorBuilder.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AssertionFailureBuilder;
import org.opentest4j.AssertionFailedError;

/**
 * A builder for {@link AssertionFailedError}s.
 * <p>
 * This class is like an extended version of {@link AssertionFailureBuilder}. It adds the following additional features:
 * <ul>
 * <li>Providing prefixes for both expected and actual values</li>
 * <li>Providing multiple expected and actual values</li>
 * <li>Building reasons with value formatting</li>
 * </ul>
 * <p>
 * Any method argument may be {@code null} unless specified otherwise.
 *
 * @author Rob Spoor
 * @since 2.0
 */
@SuppressWarnings("nls")
public final class AssertionFailedErrorBuilder {

    private static final Expected EXPECTED_NULL = new Expected.Value(null, null);
    private static final Actual ACTUAL_NULL = new Actual.Value(null, null);

    private Object message;
    private Throwable cause;
    private boolean mismatch;
    private Expected expected = EXPECTED_NULL;
    private Actual actual = ACTUAL_NULL;
    private String reason;
    private boolean includeValuesInMessage = true;

    private AssertionFailedErrorBuilder() {
    }

    /**
     * Creates a new {@code AssertionFailedErrorBuilder}.
     *
     * @return The created {@code AssertionFailedErrorBuilder}.
     */
    public static AssertionFailedErrorBuilder assertionFailedError() {
        return new AssertionFailedErrorBuilder();
    }

    /**
     * Sets the user-defined message of the assertion.
     * <p>
     * The message may be passed as a {@link Supplier} or plain string. If any other type is passed, it is converted to string in a {@code null}-safe
     * manner.
     *
     * @param message The user-defined failure message.
     * @return This object.
     */
    public AssertionFailedErrorBuilder message(Object message) {
        this.message = message;
        return this;
    }

    /**
     * Sets the reason why the assertion failed.
     *
     * @param reason The failure reason.
     * @return This object.
     */
    public AssertionFailedErrorBuilder reason(String reason) {
        this.reason = reason;
        return this;
    }

    /**
     * Returns a builder for the reason why the assertion failed.
     * <p>
     * Note that each value added to the reason will be formatted before adding; therefore, each argument should be treated as a string.
     *
     * @param pattern The pattern for the reason, with the same syntax as {@link String#format(String, Object...)}.
     * @return A builder for the reason why the assertion failed.
     * @throws NullPointerException If the given pattern is {@code null}.
     */
    public ReasonBuilder reasonPattern(String pattern) {
        Objects.requireNonNull(pattern);
        return new ReasonBuilder(this, pattern);
    }

    /**
     * Sets the cause of the assertion failure.
     *
     * @param cause The failure cause.
     * @return This object.
     */
    public AssertionFailedErrorBuilder cause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    /**
     * Sets the expected value of the assertion.
     *
     * @param expected The expected value.
     * @return This object.
     */
    public AssertionFailedErrorBuilder expected(Object expected) {
        return expectedWithPrefix("", expected);
    }

    private AssertionFailedErrorBuilder expectedWithPrefix(String prefix, Object expected) {
        this.mismatch = true;
        this.expected = new Expected.Value(prefix, expected);
        return this;
    }

    /**
     * Sets the possible expected values of the assertion. This method will use prefix {@code one of}. To omit the prefix, use
     * {@link #prefixed(String) prefixed("")}.
     * <p>
     * Note that the {@link AssertionFailedError#getExpected() expected value} of the assertion will be a list containing the expected values.
     *
     * @param expected The expected values.
     * @return This object.
     */
    public AssertionFailedErrorBuilder expectedOneOf(Object... expected) {
        return expectedOneOf(Arrays.asList(expected));
    }

    /**
     * Sets the possible expected values of the assertion. This method will use prefix {@code one of}. To omit the prefix, use
     * {@link #prefixed(String) prefixed("")}.
     * <p>
     * Note that the {@link AssertionFailedError#getExpected() expected value} of the assertion will be a list containing the expected values.
     *
     * @param expected The expected values.
     * @return This object.
     * @throws NullPointerException If the given collection is {@code null}.
     */
    public AssertionFailedErrorBuilder expectedOneOf(Collection<?> expected) {
        return expectedOneOfWithPrefix("one of", expected);
    }

    private AssertionFailedErrorBuilder expectedOneOfWithPrefix(String prefix, Collection<?> expected) {
        this.mismatch = true;
        this.expected = new Expected.Values(prefix, expected);
        return this;
    }

    /**
     * Sets a message representing the expected value of the assertion. This message will be added to the final error message without any value
     * indicators.
     *
     * @param expected The expected value message.
     * @return This object.
     */
    public AssertionFailedErrorBuilder expectedMessage(String expected) {
        this.mismatch = true;
        this.expected = new Expected.Message(expected);
        return this;
    }

    /**
     * Sets the actual value of the assertion.
     *
     * @param actual The actual value.
     * @return This object.
     */
    public AssertionFailedErrorBuilder actual(Object actual) {
        return actualWithPrefix("", actual);
    }

    private AssertionFailedErrorBuilder actualWithPrefix(String prefix, Object actual) {
        this.mismatch = true;
        this.actual = new Actual.Value(prefix, actual);
        return this;
    }

    /**
     * Sets the multiple actual values of the assertion.
     * <p>
     * Note that the {@link AssertionFailedError#getActual() actual value} of the assertion will be a list containing the actual values.
     *
     * @param actual The actual values.
     * @return This object.
     */
    public AssertionFailedErrorBuilder actualValues(Object... actual) {
        return actualValues(Arrays.asList(actual));
    }

    /**
     * Sets the multiple actual values of the assertion.
     * <p>
     * Note that the {@link AssertionFailedError#getActual() actual value} of the assertion will be a list containing the actual values.
     *
     * @param actual The actual values.
     * @return This object.
     * @throws NullPointerException If the given collection is {@code null}.
     */
    public AssertionFailedErrorBuilder actualValues(Collection<?> actual) {
        return actualValuesWithPrefix("", actual);
    }

    private AssertionFailedErrorBuilder actualValuesWithPrefix(String prefix, Collection<?> actual) {
        this.mismatch = true;
        this.actual = new Actual.Values(prefix, actual);
        return this;
    }

    /**
     * Returns an object that can be used to set expected or actual values with a prefix.
     *
     * @param prefix The prefix to add before the expected or actual value in the generated failure message.
     * @return An object that can be used to set expected or actual values with a prefix.
     */
    public PrefixedValues prefixed(String prefix) {
        return new PrefixedValues(this, prefix);
    }

    /**
     * Sets whether or not to include the actual and expected values in the generated failure message.
     *
     * @param includeValuesInMessage {@code true} to include the actual and expected values, or {@code false} to omit them.
     * @return This object.
     */
    public AssertionFailedErrorBuilder includeValuesInMessage(boolean includeValuesInMessage) {
        this.includeValuesInMessage = includeValuesInMessage;
        return this;
    }

    /**
     * Builds the {@link AssertionFailedError} and throws it.
     *
     * @throws AssertionFailedError Always.
     */
    public void buildAndThrow() {
        throw build();
    }

    /**
     * Builds the {@link AssertionFailedError} without throwing it.
     *
     * @return The build assertion failure.
     */
    public AssertionFailedError build() {
        String reasonValue = nullSafeGet(reason);
        if (mismatch && includeValuesInMessage) {
            reasonValue = (isNotBlank(reasonValue) ? reasonValue + ", " : "") + formatValues(expected, actual);
        }
        String messageValue = nullSafeGet(message);
        if (reasonValue != null) {
            messageValue = buildPrefix(messageValue) + reasonValue;
        }
        return mismatch
                ? new AssertionFailedError(messageValue, expected.value(), actual.value(), cause)
                : new AssertionFailedError(messageValue, cause);
    }

    static String nullSafeGet(Object messageOrSupplier) {
        if (messageOrSupplier == null) {
            return null;
        }
        if (messageOrSupplier instanceof Supplier<?>) {
            Object message = ((Supplier<?>) messageOrSupplier).get();
            return objectToString(message);
        }
        return objectToString(messageOrSupplier);
    }

    private static String buildPrefix(String message) {
        return isNotBlank(message) ? message + " ==> " : "";
    }

    static boolean isNotBlank(String message) {
        return message != null && message.chars().anyMatch(c -> !Character.isWhitespace(c));
    }

    private static String formatValues(Expected expected, Actual actual) {
        String actualString = actual.valueString();
        if (expected.isFormattedAs(actualString)) {
            return String.format("expected: %s but was: %s", expected.formatWithClass(), actual.formatWithClass());
        }
        return String.format("expected: %s but was: %s", expected.format(), actual.format());
    }

    static String formatClassAndValue(Object value, String valueString) {
        // If the value is null, return <null> instead of null<null>.
        if (value == null) {
            return "<null>";
        }
        String classAndHash = getClassName(value) + toHash(value);
        // if it's a class, there's no need to repeat the class name contained in the valueString.
        return value instanceof Class ? "<" + classAndHash + ">" : classAndHash + "<" + valueString + ">";
    }

    private static String toHash(Object obj) {
        return "@" + Integer.toHexString(System.identityHashCode(obj));
    }

    private static String getClassName(Object obj) {
        if (obj instanceof Class<?>) {
            return getCanonicalName((Class<?>) obj);
        }
        return obj.getClass().getName();
    }

    private static String getCanonicalName(Class<?> clazz) {
        String canonicalName = clazz.getCanonicalName();
        return canonicalName != null ? canonicalName : clazz.getName();
    }

    static String objectToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof Class<?>) {
            return getCanonicalName((Class<?>) obj);
        }
        if (obj instanceof boolean[]) {
            return Arrays.toString((boolean[]) obj);
        }
        if (obj instanceof char[]) {
            return Arrays.toString((char[]) obj);
        }
        if (obj instanceof byte[]) {
            return Arrays.toString((byte[]) obj);
        }
        if (obj instanceof short[]) {
            return Arrays.toString((short[]) obj);
        }
        if (obj instanceof int[]) {
            return Arrays.toString((int[]) obj);
        }
        if (obj instanceof long[]) {
            return Arrays.toString((long[]) obj);
        }
        if (obj instanceof float[]) {
            return Arrays.toString((float[]) obj);
        }
        if (obj instanceof double[]) {
            return Arrays.toString((double[]) obj);
        }
        if (obj instanceof Object[]) {
            return Arrays.deepToString((Object[]) obj);
        }
        String result = obj.toString();
        return result != null ? result : "null";
    }

    private static String addOptionalPrefix(String prefix, String message) {
        return isNotBlank(prefix)
                ? String.format("%s %s", prefix, message)
                : message;
    }

    /**
     * A builder for the reason why the assertion failed.
     *
     * @author Rob Spoor
     * @since 2.0
     */
    public static final class ReasonBuilder {

        private final AssertionFailedErrorBuilder builder;
        private final String pattern;
        private final List<Object> arguments;

        private ReasonBuilder(AssertionFailedErrorBuilder builder, String pattern) {
            this.builder = builder;
            this.pattern = pattern;
            this.arguments = new ArrayList<>();
        }

        /**
         * Adds a single formatted value.
         * This value should match a single string place holder in the pattern used to create this object.
         *
         * @param value The value to add.
         * @return This object.
         */
        public ReasonBuilder withValue(Object value) {
            arguments.add(String.format("<%s>", objectToString(value)));
            return this;
        }

        /**
         * Adds multiple formatted values, separated by commas.
         * These values should match a single string place holder in the pattern used to create this object.
         *
         * @param values The values to add.
         * @return This object.
         */
        public ReasonBuilder withValues(Object... values) {
            return withValues(Arrays.asList(values));
        }

        /**
         * Adds multiple formatted values, separated by commas.
         * These values should match a single string place holder in the pattern used to create this object.
         *
         * @param values The values to add.
         * @return This object.
         * @throws NullPointerException If the given collection is {@code null}.
         */
        public ReasonBuilder withValues(Collection<?> values) {
            arguments.add(values.stream()
                    .map(value -> String.format("<%s>", objectToString(value)))
                    .collect(Collectors.joining(", ")));
            return this;
        }

        /**
         * Sets the reason on the {@link AssertionFailedErrorBuilder} that returned this object.
         * The reason will be a formatted string using the format used to create this object and arguments added to this object.
         *
         * @return The {@link AssertionFailedErrorBuilder} that returned this object.
         * @throws IllegalFormatException If the formatted string could not be created.
         * @see String#format(String, Object...)
         */
        public AssertionFailedErrorBuilder format() {
            String reason = String.format(pattern, arguments.toArray());
            return builder.reason(reason);
        }
    }

    /**
     * An object that can set the possible expected value or actual value of an assertion with a prefix.
     *
     * @author Rob Spoor
     * @since 2.0
     */
    public static final class PrefixedValues {

        private final AssertionFailedErrorBuilder builder;
        private final String prefix;

        private PrefixedValues(AssertionFailedErrorBuilder builder, String prefix) {
            this.builder = builder;
            this.prefix = prefix;
        }

        /**
         * Sets the expected value of the assertion.
         *
         * @param expected The expected value.
         * @return The {@link AssertionFailedErrorBuilder} that returned this object.
         */
        public AssertionFailedErrorBuilder expected(Object expected) {
            return builder.expectedWithPrefix(prefix, expected);
        }

        /**
         * Sets the possible expected values of the assertion.
         * <p>
         * Note that the {@link AssertionFailedError#getExpected() expected value} of the assertion will be a list containing the expected values.
         *
         * @param expected The expected values.
         * @return The {@link AssertionFailedErrorBuilder} that returned this object.
         */
        public AssertionFailedErrorBuilder expectedOneOf(Object... expected) {
            return expectedOneOf(Arrays.asList(expected));
        }

        /**
         * Sets the possible expected values of the assertion.
         * <p>
         * Note that the {@link AssertionFailedError#getExpected() expected value} of the assertion will be a list containing the expected values.
         *
         * @param expected The expected values.
         * @return The {@link AssertionFailedErrorBuilder} that returned this object.
         * @throws NullPointerException If the given collection is {@code null}.
         */
        public AssertionFailedErrorBuilder expectedOneOf(Collection<?> expected) {
            return builder.expectedOneOfWithPrefix(prefix, expected);
        }

        /**
         * Sets the actual value of the assertion.
         *
         * @param actual The actual value.
         * @return The {@link AssertionFailedErrorBuilder} that returned this object.
         */
        public AssertionFailedErrorBuilder actual(Object actual) {
            return builder.actualWithPrefix(prefix, actual);
        }

        /**
         * Sets the multiple actual values of the assertion.
         * <p>
         * Note that the {@link AssertionFailedError#getActual() actual value} of the assertion will be a list containing the actual values.
         *
         * @param actual The actual values.
         * @return The {@link AssertionFailedErrorBuilder} that returned this object.
         */
        public AssertionFailedErrorBuilder actualValues(Object... actual) {
            return actualValues(Arrays.asList(actual));
        }

        /**
         * Sets the multiple actual values of the assertion.
         * <p>
         * Note that the {@link AssertionFailedError#getActual() actual value} of the assertion will be a list containing the actual values.
         *
         * @param actual The actual values.
         * @return The {@link AssertionFailedErrorBuilder} that returned this object.
         * @throws NullPointerException If the given collection is {@code null}.
         */
        public AssertionFailedErrorBuilder actualValues(Collection<?> actual) {
            return builder.actualValuesWithPrefix(prefix, actual);
        }
    }

    private abstract static class Expected {

        private final String prefix;

        Expected(String prefix) {
            this.prefix = prefix;
        }

        abstract boolean isFormattedAs(String actualString);

        private String format() {
            return addOptionalPrefix(prefix, doFormat());
        }

        abstract String doFormat();

        private String formatWithClass() {
            return addOptionalPrefix(prefix, doFormatWithClass());
        }

        abstract String doFormatWithClass();

        abstract Object value();

        private static final class Value extends Expected {

            private final Object value;
            private final String valueString;

            private Value(String prefix, Object value) {
                super(prefix);
                this.value = value;
                this.valueString = objectToString(value);
            }

            @Override
            boolean isFormattedAs(String actualString) {
                return valueString.equals(actualString);
            }

            @Override
            String doFormat() {
                return String.format("<%s>", valueString);
            }

            @Override
            String doFormatWithClass() {
                return formatClassAndValue(value, valueString);
            }

            @Override
            Object value() {
                return value;
            }
        }

        private static final class Values extends Expected {

            private final List<?> values;
            private final List<String> valueStrings;
            private final String valuesString;

            private Values(String prefix, Collection<?> values) {
                super(prefix);
                this.values = new ArrayList<>(values);
                this.valueStrings = values.stream()
                        .map(AssertionFailedErrorBuilder::objectToString)
                        .collect(Collectors.toList());
                this.valuesString = valueStrings.stream()
                        .collect(Collectors.joining(", "));
            }

            @Override
            boolean isFormattedAs(String actualString) {
                return valueStrings.contains(actualString) || valuesString.equals(actualString);
            }

            @Override
            String doFormat() {
                return valueStrings.stream()
                        .map(valueString -> String.format("<%s>", valueString))
                        .collect(Collectors.joining(", "));
            }

            @Override
            String doFormatWithClass() {
                Iterator<?> valueIterator = values.iterator();
                Iterator<String> valueStringIterator = valueStrings.iterator();
                StringJoiner stringJoiner = new StringJoiner(", ");

                while (valueIterator.hasNext()) {
                    Object value = valueIterator.next();
                    String valueString = valueStringIterator.next();
                    stringJoiner.add(formatClassAndValue(value, valueString));
                }
                return stringJoiner.toString();
            }

            @Override
            Object value() {
                return values;
            }
        }

        private static final class Message extends Expected {

            private final String message;

            private Message(String message) {
                super("");
                this.message = message;
            }

            @Override
            boolean isFormattedAs(String actualString) {
                return false;
            }

            @Override
            String doFormat() {
                return message;
            }

            @Override
            String doFormatWithClass() {
                return message;
            }

            @Override
            Object value() {
                return message;
            }
        }
    }

    private abstract static class Actual {

        private final String prefix;

        Actual(String prefix) {
            this.prefix = prefix;
        }

        private String format() {
            return addOptionalPrefix(prefix, doFormat());
        }

        abstract String doFormat();

        private String formatWithClass() {
            return addOptionalPrefix(prefix, doFormatWithClass());
        }

        abstract String doFormatWithClass();

        abstract Object value();

        abstract String valueString();

        private static final class Value extends Actual {

            private final Object value;
            private final String valueString;

            private Value(String prefix, Object value) {
                super(prefix);
                this.value = value;
                this.valueString = objectToString(value);
            }

            @Override
            String doFormat() {
                return String.format("<%s>", valueString);
            }

            @Override
            String doFormatWithClass() {
                return formatClassAndValue(value, valueString);
            }

            @Override
            Object value() {
                return value;
            }

            @Override
            String valueString() {
                return valueString;
            }
        }

        private static final class Values extends Actual {

            private final List<?> values;
            private final List<String> valueStrings;

            private Values(String prefix, Collection<?> values) {
                super(prefix);
                this.values = new ArrayList<>(values);
                this.valueStrings = values.stream()
                        .map(AssertionFailedErrorBuilder::objectToString)
                        .collect(Collectors.toList());
            }

            @Override
            String doFormat() {
                return valueStrings.stream()
                        .map(valueString -> String.format("<%s>", valueString))
                        .collect(Collectors.joining(", "));
            }

            @Override
            String doFormatWithClass() {
                Iterator<?> valueIterator = values.iterator();
                Iterator<String> valueStringIterator = valueStrings.iterator();
                StringJoiner stringJoiner = new StringJoiner(", ");

                while (valueIterator.hasNext()) {
                    Object value = valueIterator.next();
                    String valueString = valueStringIterator.next();
                    stringJoiner.add(formatClassAndValue(value, valueString));
                }
                return stringJoiner.toString();
            }

            @Override
            Object value() {
                return values;
            }

            @Override
            public String valueString() {
                return valueStrings.stream()
                        .collect(Collectors.joining(", "));
            }
        }
    }
}
