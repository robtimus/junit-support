/*
 * ArgumentsCombiner.java
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

package com.github.robtimus.junit.support.params;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

/**
 * A class that can combine multiple arguments. Instances of this class can be used in {@link ArgumentsProvider} implementations or provider methods
 * to produce argument combinations.
 * <p>
 * This class has quite some overlapping functionality with
 * <a href="https://junit-pioneer.org/docs/cartesian-product/">JUnit Pioneer's {@literal @CartesianTest}</a>. What this class adds that
 * {@code @CartesianTest} doesn't is filtering out combinations.
 * <p>
 * This class has no special support for enums like {@code @CartesianTest} and {@link EnumSource @EnumSource} do. That's because
 * {@link #with(Collection)} and {@link #crossJoin(Collection)} can be used in combination with {@link EnumSet}. For instance, to exclude some enum
 * constants, use {@link EnumSet#complementOf(EnumSet)} in combination with one of the other {@link EnumSet} factory methods.
 * <p>
 * Note that instances of this class can be considered as builders for {@link Stream}s of {@link Arguments} instances. Like {@link Stream}s they
 * should not be used more than once.
 *
 * @author Rob Spoor
 * @since 2.0
 */
@SuppressWarnings("nls")
public final class ArgumentsCombiner {

    private Stream<? extends Arguments> stream;

    private ArgumentsCombiner(Stream<? extends Arguments> stream) {
        this.stream = stream;
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #with(Object...)} for that.
     *
     * @param arguments The initial set of arguments.
     * @return The created arguments combiner.
     */
    public static ArgumentsCombiner withBooleans(boolean[] arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = IntStream.range(0, arguments.length)
                .mapToObj(i -> arguments(arguments[i]));

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #with(Object...)} for that.
     *
     * @param arguments The initial set of arguments.
     * @return The created arguments combiner.
     */
    public static ArgumentsCombiner withChars(char[] arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = IntStream.range(0, arguments.length)
                .mapToObj(i -> arguments(arguments[i]));

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #with(Object...)} for that.
     *
     * @param arguments The initial set of arguments.
     * @return The created arguments combiner.
     */
    public static ArgumentsCombiner withBytes(byte[] arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = IntStream.range(0, arguments.length)
                .mapToObj(i -> arguments(arguments[i]));

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #with(Object...)} for that.
     *
     * @param arguments The initial set of arguments.
     * @return The created arguments combiner.
     */
    public static ArgumentsCombiner withShorts(short[] arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = IntStream.range(0, arguments.length)
                .mapToObj(i -> arguments(arguments[i]));

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #with(Object...)} for that.
     *
     * @param arguments The initial set of arguments.
     * @return The created arguments combiner.
     */
    public static ArgumentsCombiner withInts(int[] arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = Arrays.stream(arguments)
                .mapToObj(Arguments::arguments);

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #with(Object...)} for that.
     *
     * @param arguments The initial set of arguments.
     * @return The created arguments combiner.
     */
    public static ArgumentsCombiner withLongs(long[] arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = Arrays.stream(arguments)
                .mapToObj(Arguments::arguments);

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #with(Object...)} for that.
     *
     * @param arguments The initial set of arguments.
     * @return The created arguments combiner.
     */
    public static ArgumentsCombiner withFloats(float[] arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = IntStream.range(0, arguments.length)
                .mapToObj(i -> arguments(arguments[i]));

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #with(Object...)} for that.
     *
     * @param arguments The initial set of arguments.
     * @return The created arguments combiner.
     */
    public static ArgumentsCombiner withDoubles(double[] arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = Arrays.stream(arguments)
                .mapToObj(Arguments::arguments);

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     *
     * @param arguments The initial set of arguments.
     * @return The created arguments combiner.
     */
    public static ArgumentsCombiner with(Object... arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = Arrays.stream(arguments)
                .map(Arguments::arguments);

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     *
     * @param arguments A collection with the initial set of arguments.
     * @return The created arguments combiner.
     * @throws NullPointerException If the given collection is {@code null}.
     */
    public static ArgumentsCombiner with(Collection<?> arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = arguments.stream()
                .map(Arguments::arguments);

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     *
     * @param arguments A stream with the initial set of arguments. Note that this stream should not be used outside the returned instance anymore.
     * @return The created arguments combiner.
     * @throws NullPointerException If the given stream is {@code null}.
     */
    public static ArgumentsCombiner with(Stream<?> arguments) {
        Objects.requireNonNull(arguments);

        Stream<Arguments> stream = arguments.map(ArgumentsCombiner::asArguments);

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     * <p>
     * Note that if the arguments provider throws an exception from its {@link ArgumentsProvider#provideArguments(ExtensionContext)} method,
     * that exception will be re-thrown <em>as is</em> but as an unchecked exception.
     *
     * @param argumentsProvider The provider initial set of arguments.
     * @param context The current extension context.
     * @return The created arguments combiner.
     * @throws NullPointerException If any of the given arguments is {@code null}.
     * @deprecated This method delegates to the deprecated {@link ArgumentsProvider#provideArguments(ExtensionContext)} method.
     *             Use {@link #with(ArgumentsProvider, ParameterDeclarations, ExtensionContext)} instead.
     */
    @Deprecated(since = "3.2", forRemoval = true)
    public static ArgumentsCombiner with(ArgumentsProvider argumentsProvider, ExtensionContext context) {
        Objects.requireNonNull(argumentsProvider);
        Objects.requireNonNull(context);

        Stream<? extends Arguments> stream = argumentsStream(argumentsProvider, context);

        return new ArgumentsCombiner(stream);
    }

    /**
     * Creates an arguments combiner with an initial set of arguments.
     * <p>
     * Note that if the arguments provider throws an exception from its
     * {@link ArgumentsProvider#provideArguments(ParameterDeclarations, ExtensionContext)} method, that exception will be re-thrown <em>as is</em> but
     * as an unchecked exception.
     *
     * @param argumentsProvider The provider initial set of arguments.
     * @param parameters The parameter declarations to pass to the given provider.
     * @param context The current extension context.
     * @return The created arguments combiner.
     * @throws NullPointerException If any of the given arguments is {@code null}.
     * @since 3.2
     */
    public static ArgumentsCombiner with(ArgumentsProvider argumentsProvider, ParameterDeclarations parameters, ExtensionContext context) {
        Objects.requireNonNull(argumentsProvider);
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(context);

        Stream<? extends Arguments> stream = argumentsStream(argumentsProvider, parameters, context);

        return new ArgumentsCombiner(stream);
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #crossJoin(Object...)} for that.
     *
     * @param arguments The arguments to add.
     * @return This object.
     */
    public ArgumentsCombiner crossJoinBooleans(boolean[] arguments) {
        Objects.requireNonNull(arguments);

        stream = stream.flatMap(args -> IntStream.range(0, arguments.length)
                .mapToObj(i -> combineArguments(args, arguments[i])));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #crossJoin(Object...)} for that.
     *
     * @param arguments The arguments to add.
     * @return This object.
     */
    public ArgumentsCombiner crossJoinChars(char[] arguments) {
        Objects.requireNonNull(arguments);

        stream = stream.flatMap(args -> IntStream.range(0, arguments.length)
                .mapToObj(i -> combineArguments(args, arguments[i])));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #crossJoin(Object...)} for that.
     *
     * @param arguments The arguments to add.
     * @return This object.
     */
    public ArgumentsCombiner crossJoinBytes(byte[] arguments) {
        Objects.requireNonNull(arguments);

        stream = stream.flatMap(args -> IntStream.range(0, arguments.length)
                .mapToObj(i -> combineArguments(args, arguments[i])));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #crossJoin(Object...)} for that.
     *
     * @param arguments The arguments to add.
     * @return This object.
     */
    public ArgumentsCombiner crossJoinShorts(short[] arguments) {
        Objects.requireNonNull(arguments);

        stream = stream.flatMap(args -> IntStream.range(0, arguments.length)
                .mapToObj(i -> combineArguments(args, arguments[i])));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #crossJoin(Object...)} for that.
     *
     * @param arguments The arguments to add.
     * @return This object.
     */
    public ArgumentsCombiner crossJoinInts(int[] arguments) {
        Objects.requireNonNull(arguments);

        stream = stream.flatMap(args -> Arrays.stream(arguments)
                .mapToObj(arg -> combineArguments(args, arg)));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #crossJoin(Object...)} for that.
     *
     * @param arguments The arguments to add.
     * @return This object.
     */
    public ArgumentsCombiner crossJoinLongs(long[] arguments) {
        Objects.requireNonNull(arguments);

        stream = stream.flatMap(args -> Arrays.stream(arguments)
                .mapToObj(arg -> combineArguments(args, arg)));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #crossJoin(Object...)} for that.
     *
     * @param arguments The arguments to add.
     * @return This object.
     */
    public ArgumentsCombiner crossJoinFloats(float[] arguments) {
        Objects.requireNonNull(arguments);

        stream = stream.flatMap(args -> IntStream.range(0, arguments.length)
                .mapToObj(i -> combineArguments(args, arguments[i])));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     * <p>
     * Note that this method does not support variable arguments; use {@link #crossJoin(Object...)} for that.
     *
     * @param arguments The arguments to add.
     * @return This object.
     */
    public ArgumentsCombiner crossJoinDoubles(double[] arguments) {
        Objects.requireNonNull(arguments);

        stream = stream.flatMap(args -> Arrays.stream(arguments)
                .mapToObj(arg -> combineArguments(args, arg)));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     *
     * @param arguments The arguments to add.
     * @return This object.
     */
    public ArgumentsCombiner crossJoin(Object... arguments) {
        Objects.requireNonNull(arguments);

        stream = stream.flatMap(args -> Arrays.stream(arguments)
                .map(arg -> combineArguments(args, arg)));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     *
     * @param arguments A collection with the arguments to add.
     * @return This object.
     * @throws NullPointerException If the given collection is {@code null}.
     */
    public ArgumentsCombiner crossJoin(Collection<?> arguments) {
        Objects.requireNonNull(arguments);

        stream = stream.flatMap(args -> arguments.stream()
                .map(arg -> combineArguments(args, arg)));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     *
     * @param argumentsProvider A supplier for a stream with the arguments to add.
     *                              Each invocation of the supplier should return a fresh new stream that is not used for any other purposes.
     * @return This object.
     * @throws NullPointerException If the given supplier is {@code null}.
     */
    public ArgumentsCombiner crossJoin(Supplier<? extends Stream<?>> argumentsProvider) {
        Objects.requireNonNull(argumentsProvider);

        stream = stream.flatMap(args -> argumentsProvider.get()
                .map(arg -> combineArguments(args, arg)));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     * <p>
     * Note that if the arguments provider throws an exception from its {@link ArgumentsProvider#provideArguments(ExtensionContext)} method,
     * that exception will be re-thrown <em>as is</em> but as an unchecked exception.
     *
     * @param argumentsProvider The provider for the arguments to add.
     * @param context The current extension context.
     * @return This object.
     * @throws NullPointerException If any of the given arguments is {@code null}.
     * @deprecated This method delegates to the deprecated {@link ArgumentsProvider#provideArguments(ExtensionContext)} method.
     *             Use {@link #crossJoin(ArgumentsProvider, ParameterDeclarations, ExtensionContext)} instead.
     */
    @Deprecated(since = "3.2", forRemoval = true)
    public ArgumentsCombiner crossJoin(ArgumentsProvider argumentsProvider, ExtensionContext context) {
        Objects.requireNonNull(argumentsProvider);
        Objects.requireNonNull(context);

        stream = stream.flatMap(args -> argumentsStream(argumentsProvider, context)
                .map(args2 -> combineArguments(args, args2)));

        return this;
    }

    /**
     * Adds a set of arguments. The resulting arguments will be a cross join or Cartesian product of the current arguments and the given arguments.
     * <p>
     * Note that if the arguments provider throws an exception from its
     * {@link ArgumentsProvider#provideArguments(ParameterDeclarations, ExtensionContext)} method, that exception will be re-thrown <em>as is</em> but
     * as an unchecked exception.
     *
     * @param argumentsProvider The provider for the arguments to add.
     * @param parameters The parameter declarations to pass to the given provider.
     * @param context The current extension context.
     * @return This object.
     * @throws NullPointerException If any of the given arguments is {@code null}.
     * @since 3.2
     */
    public ArgumentsCombiner crossJoin(ArgumentsProvider argumentsProvider, ParameterDeclarations parameters, ExtensionContext context) {
        Objects.requireNonNull(argumentsProvider);
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(context);

        stream = stream.flatMap(args -> argumentsStream(argumentsProvider, parameters, context)
                .map(args2 -> combineArguments(args, args2)));

        return this;
    }

    /**
     * Excludes a combination of values from the current combinations of arguments. If the combination occurs multiple times, all occurrences will be
     * removed.
     * <p>
     * Note that the number of values given should be equal to the number of arguments in each current combination. Otherwise, an exception will be
     * thrown when the stream returned by {@link #stream()} is consumed.
     *
     * @param values The combination of values to exclude.
     * @return This object.
     */
    public ArgumentsCombiner excludeCombination(Object... values) {
        Objects.requireNonNull(values);

        Predicate<Object[]> filter = arguments -> {
            if (arguments.length != values.length) {
                throw new IllegalStateException(String.format(
                        "Incorrect number of values given. Expected %d, was %d: %s", arguments.length, values.length, Arrays.toString(values)));
            }
            return Arrays.equals(values, arguments);
        };
        return excludeCombinations(filter);
    }

    /**
     * Excludes all combinations of arguments that match a filter.
     *
     * @param filter The filter to use.
     *                   Every {@link Arguments} instance for which {@link Predicate#test(Object) filter.test} returns {@code true} will be excluded.
     * @return This object.
     * @throws NullPointerException If the given filter is {@code null}.
     */
    public ArgumentsCombiner excludeCombinations(Predicate<? super Object[]> filter) {
        Objects.requireNonNull(filter);

        stream = stream.filter(arguments -> !filter.test(arguments.get()));

        return this;
    }

    /**
     * Returns a stream with all combinations of arguments.
     * This stream should be consumed at most once, and afterwards this {@code ArgumentsCombiner} should no longer be used.
     *
     * @return A stream with all combinations of arguments.
     */
    public Stream<? extends Arguments> stream() {
        return stream;
    }

    @Deprecated(since = "3.2", forRemoval = true)
    private static Stream<? extends Arguments> argumentsStream(ArgumentsProvider argumentsProvider, ExtensionContext context) {
        try {
            return argumentsProvider.provideArguments(context);
        } catch (Exception e) {
            return throwAsUncheckedException(e);
        }
    }

    private static Stream<? extends Arguments> argumentsStream(ArgumentsProvider argumentsProvider, ParameterDeclarations parameters,
                                                               ExtensionContext context) {
        try {
            return argumentsProvider.provideArguments(parameters, context);
        } catch (Exception e) {
            return throwAsUncheckedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R throwAsUncheckedException(Throwable t) throws T {
        throw (T) t;
    }

    static Arguments asArguments(Object argument) {
        return argument instanceof Arguments ? (Arguments) argument : arguments(argument);
    }

    /**
     * Combines two (sets of) arguments into one {@link Arguments} object. If either argument is an instance of {@link Arguments}, its elements will
     * be used as separate arguments in the result. Otherwise, the argument is used as a single argument.
     *
     * @param o1 The first argument or set of arguments.
     * @param o2 The second argument or set of arguments.
     * @return The result of combining the two (sets of) arguments.
     */
    public static Arguments combineArguments(Object o1, Object o2) {
        if (o1 instanceof Arguments && o2 instanceof Arguments) {
            return combineArguments((Arguments) o1, (Arguments) o2);
        }
        if (o1 instanceof Arguments) {
            return combineArguments((Arguments) o1, o2);
        }
        if (o2 instanceof Arguments) {
            return combineArguments(o1, (Arguments) o2);
        }
        return arguments(o1, o2);
    }

    private static Arguments combineArguments(Arguments arguments1, Arguments arguments2) {
        Object[] args1 = arguments1.get();
        Object[] args2 = arguments2.get();
        Object[] result = new Object[args1.length + args2.length];
        System.arraycopy(args1, 0, result, 0, args1.length);
        System.arraycopy(args2, 0, result, args1.length, args2.length);
        return arguments(result);
    }

    private static Arguments combineArguments(Arguments arguments, Object argument) {
        Object[] args = arguments.get();
        Object[] result = new Object[args.length + 1];
        System.arraycopy(args, 0, result, 0, args.length);
        result[args.length] = argument;
        return arguments(result);
    }

    private static Arguments combineArguments(Object argument, Arguments arguments) {
        Object[] args = arguments.get();
        Object[] result = new Object[args.length + 1];
        result[0] = argument;
        System.arraycopy(args, 0, result, 1, args.length);
        return arguments(result);
    }
}
