/*
 * ArgumentsCombinerTest.java
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

import static com.github.robtimus.junit.support.params.ArgumentsCombiner.combineArguments;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.time.Month;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

@SuppressWarnings("nls")
class ArgumentsCombinerTest {

    @Nested
    @DisplayName("withBooleans(boolean...)")
    class WithBooleanArguments {

        @Test
        @DisplayName("null array")
        void testNullArray() {
            boolean[] arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.withBooleans(arguments));
        }

        @Test
        @DisplayName("empty array")
        void testEmptyArray() {
            boolean[] arguments = {};

            ArgumentsCombiner combiner = ArgumentsCombiner.withBooleans(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty array")
        void testNonEmptyArray() {
            boolean[] arguments = { true, false, true, };

            ArgumentsCombiner combiner = ArgumentsCombiner.withBooleans(arguments);

            assertArguments(combiner, arguments(true), arguments(false), arguments(true));
        }
    }

    @Nested
    @DisplayName("withChars(char...)")
    class WithCharArguments {

        @Test
        @DisplayName("null array")
        void testNullArray() {
            char[] arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.withChars(arguments));
        }

        @Test
        @DisplayName("empty array")
        void testEmptyArray() {
            char[] arguments = {};

            ArgumentsCombiner combiner = ArgumentsCombiner.withChars(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty array")
        void testNonEmptyArray() {
            char[] arguments = { 'A', 'B', 'C', };

            ArgumentsCombiner combiner = ArgumentsCombiner.withChars(arguments);

            assertArguments(combiner, arguments('A'), arguments('B'), arguments('C'));
        }
    }

    @Nested
    @DisplayName("withBytes(byte...)")
    class WithByteArguments {

        @Test
        @DisplayName("null array")
        void testNullArray() {
            byte[] arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.withBytes(arguments));
        }

        @Test
        @DisplayName("empty array")
        void testEmptyArray() {
            byte[] arguments = {};

            ArgumentsCombiner combiner = ArgumentsCombiner.withBytes(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty array")
        void testNonEmptyArray() {
            byte[] arguments = { 1, 2, 3, };

            ArgumentsCombiner combiner = ArgumentsCombiner.withBytes(arguments);

            assertArguments(combiner, arguments((byte) 1), arguments((byte) 2), arguments((byte) 3));
        }
    }

    @Nested
    @DisplayName("withShorts(short...)")
    class WithShortArguments {

        @Test
        @DisplayName("null array")
        void testNullArray() {
            short[] arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.withShorts(arguments));
        }

        @Test
        @DisplayName("empty array")
        void testEmptyArray() {
            short[] arguments = {};

            ArgumentsCombiner combiner = ArgumentsCombiner.withShorts(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty array")
        void testNonEmptyArray() {
            short[] arguments = { 1, 2, 3, };

            ArgumentsCombiner combiner = ArgumentsCombiner.withShorts(arguments);

            assertArguments(combiner, arguments((short) 1), arguments((short) 2), arguments((short) 3));
        }
    }

    @Nested
    @DisplayName("withInts(int...)")
    class WithIntArguments {

        @Test
        @DisplayName("null array")
        void testNullArray() {
            int[] arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.withInts(arguments));
        }

        @Test
        @DisplayName("empty array")
        void testEmptyArray() {
            int[] arguments = {};

            ArgumentsCombiner combiner = ArgumentsCombiner.withInts(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty array")
        void testNonEmptyArray() {
            int[] arguments = { 1, 2, 3, };

            ArgumentsCombiner combiner = ArgumentsCombiner.withInts(arguments);

            assertArguments(combiner, arguments(1), arguments(2), arguments(3));
        }
    }

    @Nested
    @DisplayName("withLongs(long...)")
    class WithLongArguments {

        @Test
        @DisplayName("null array")
        void testNullArray() {
            long[] arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.withLongs(arguments));
        }

        @Test
        @DisplayName("empty array")
        void testEmptyArray() {
            long[] arguments = {};

            ArgumentsCombiner combiner = ArgumentsCombiner.withLongs(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty array")
        void testNonEmptyArray() {
            long[] arguments = { 1L, 2L, 3L, };

            ArgumentsCombiner combiner = ArgumentsCombiner.withLongs(arguments);

            assertArguments(combiner, arguments(1L), arguments(2L), arguments(3L));
        }
    }

    @Nested
    @DisplayName("withFloats(float...)")
    class WithFloatArguments {

        @Test
        @DisplayName("null array")
        void testNullArray() {
            float[] arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.withFloats(arguments));
        }

        @Test
        @DisplayName("empty array")
        void testEmptyArray() {
            float[] arguments = {};

            ArgumentsCombiner combiner = ArgumentsCombiner.withFloats(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty array")
        void testNonEmptyArray() {
            float[] arguments = { 1F, 2F, 3F, };

            ArgumentsCombiner combiner = ArgumentsCombiner.withFloats(arguments);

            assertArguments(combiner, arguments(1F), arguments(2F), arguments(3F));
        }
    }

    @Nested
    @DisplayName("withDoubles(double...)")
    class WithDoubleArguments {

        @Test
        @DisplayName("null array")
        void testNullArray() {
            double[] arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.withDoubles(arguments));
        }

        @Test
        @DisplayName("empty array")
        void testEmptyArray() {
            double[] arguments = {};

            ArgumentsCombiner combiner = ArgumentsCombiner.withDoubles(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty array")
        void testNonEmptyArray() {
            double[] arguments = { 1D, 2D, 3D, };

            ArgumentsCombiner combiner = ArgumentsCombiner.withDoubles(arguments);

            assertArguments(combiner, arguments(1D), arguments(2D), arguments(3D));
        }
    }

    @Nested
    @DisplayName("with(Object...)")
    class WithObjectArguments {

        @Test
        @DisplayName("null array")
        void testNullArray() {
            Object[] arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.with(arguments));
        }

        @Test
        @DisplayName("empty array")
        void testEmptyArray() {
            Object[] arguments = {};

            ArgumentsCombiner combiner = ArgumentsCombiner.with(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty array")
        void testNonEmptyArray() {
            Object[] arguments = { "foo", "bar", 0, };

            ArgumentsCombiner combiner = ArgumentsCombiner.with(arguments);

            assertArguments(combiner, arguments("foo"), arguments("bar"), arguments(0));
        }
    }

    @Nested
    @DisplayName("with(Collection)")
    class WithArgumentsCollection {

        @Test
        @DisplayName("null collection")
        void testNullCollection() {
            Collection<?> arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.with(arguments));
        }

        @Test
        @DisplayName("empty collection")
        void testEmptyArray() {
            Collection<?> arguments = Collections.emptyList();

            ArgumentsCombiner combiner = ArgumentsCombiner.with(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty collection")
        void testNonEmptyArray() {
            Collection<?> arguments = Arrays.asList("foo", "bar", 0);

            ArgumentsCombiner combiner = ArgumentsCombiner.with(arguments);

            assertArguments(combiner, arguments("foo"), arguments("bar"), arguments(0));
        }
    }

    @Nested
    @DisplayName("with(Stream)")
    class WithArgumentsStream {

        @Test
        @DisplayName("null stream")
        void testNullStream() {
            Stream<?> arguments = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.with(arguments));
        }

        @Test
        @DisplayName("empty stream")
        void testEmptyStream() {
            Stream<?> arguments = Stream.empty();

            ArgumentsCombiner combiner = ArgumentsCombiner.with(arguments);

            assertArguments(combiner);
        }

        @Test
        @DisplayName("non-empty stream")
        void testNonEmptyStream() {
            Stream<?> arguments = Stream.of("foo", "bar", 0);

            ArgumentsCombiner combiner = ArgumentsCombiner.with(arguments);

            assertArguments(combiner, arguments("foo"), arguments("bar"), arguments(0));
        }

        @Test
        @DisplayName("arguments stream")
        void testArgumentsStream() {
            Stream<?> arguments = Stream.of(arguments("foo"), arguments("bar"), arguments(0));

            ArgumentsCombiner combiner = ArgumentsCombiner.with(arguments);

            assertArguments(combiner, arguments("foo"), arguments("bar"), arguments(0));
        }
    }

    @Nested
    @DisplayName("with(ArgumentsProvider, ExtensionContext)")
    class WithArgumentsProvider {

        @Test
        @DisplayName("null arguments provider")
        void testNullArgumentsProvider() {
            ArgumentsProvider argumentsProvider = null;
            ExtensionContext context = mock(ExtensionContext.class);

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.with(argumentsProvider, context));

            verifyNoInteractions(context);
        }

        @Test
        @DisplayName("null context")
        void testNullContext() {
            ArgumentsProvider argumentsProvider = mock(ArgumentsProvider.class);
            ExtensionContext context = null;

            assertThrows(NullPointerException.class, () -> ArgumentsCombiner.with(argumentsProvider, context));

            verifyNoInteractions(argumentsProvider);
        }

        @Test
        @DisplayName("arguments provider providing empty stream")
        void testProvidingEmptyStream() throws Exception {
            ArgumentsProvider argumentsProvider = mock(ArgumentsProvider.class);
            ExtensionContext context = mock(ExtensionContext.class);

            when(argumentsProvider.provideArguments(context)).thenAnswer(i -> Stream.empty());

            ArgumentsCombiner combiner = ArgumentsCombiner.with(argumentsProvider, context);

            assertArguments(combiner);

            verify(argumentsProvider).provideArguments(context);
            verifyNoMoreInteractions(argumentsProvider, context);
        }

        @Test
        @DisplayName("arguments provider providing non-empty stream")
        void testProvidingNonEmptyStream() throws Exception {
            ArgumentsProvider argumentsProvider = mock(ArgumentsProvider.class);
            ExtensionContext context = mock(ExtensionContext.class);

            when(argumentsProvider.provideArguments(context)).thenAnswer(i -> Stream.of(arguments("foo"), arguments("bar"), arguments(0)));

            ArgumentsCombiner combiner = ArgumentsCombiner.with(argumentsProvider, context);

            assertArguments(combiner, arguments("foo"), arguments("bar"), arguments(0));

            verify(argumentsProvider).provideArguments(context);
            verifyNoMoreInteractions(argumentsProvider, context);
        }

        @Test
        @DisplayName("arguments provider throwing exception")
        void testThrowingException() throws Exception {
            ArgumentsProvider argumentsProvider = mock(ArgumentsProvider.class);
            ExtensionContext context = mock(ExtensionContext.class);
            IOException exception = new IOException();

            when(argumentsProvider.provideArguments(context)).thenThrow(exception);

            IOException thrown = assertThrows(IOException.class, () -> ArgumentsCombiner.with(argumentsProvider, context));
            assertSame(exception, thrown);

            verify(argumentsProvider).provideArguments(context);
            verifyNoMoreInteractions(argumentsProvider, context);
        }
    }

    @Nested
    @DisplayName("crossJoin")
    class CrossJoin {

        private ArgumentsCombiner combiner;

        @BeforeEach
        void initCombiner() {
            combiner = ArgumentsCombiner.with("hello", "world");
        }

        @Nested
        @DisplayName("crossJoinBooleans(boolean...)")
        class WithBooleanArguments {

            @Test
            @DisplayName("null array")
            void testNullArray() {
                boolean[] arguments = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoinBooleans(arguments));
            }

            @Test
            @DisplayName("empty array")
            void testEmptyArray() {
                boolean[] arguments = {};

                assertSame(combiner, combiner.crossJoinBooleans(arguments));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("non-empty array")
            void testNonEmptyArray() {
                boolean[] arguments = { true, false, true, };

                assertSame(combiner, combiner.crossJoinBooleans(arguments));

                assertArguments(combiner,
                        arguments("hello", true), arguments("hello", false), arguments("hello", true),
                        arguments("world", true), arguments("world", false), arguments("world", true));
            }
        }

        @Nested
        @DisplayName("crossJoinChars(char...)")
        class WithCharArguments {

            @Test
            @DisplayName("null array")
            void testNullArray() {
                char[] arguments = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoinChars(arguments));
            }

            @Test
            @DisplayName("empty array")
            void testEmptyArray() {
                char[] arguments = {};

                assertSame(combiner, combiner.crossJoinChars(arguments));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("non-empty array")
            void testNonEmptyArray() {
                char[] arguments = { 'A', 'B', 'C', };

                assertSame(combiner, combiner.crossJoinChars(arguments));

                assertArguments(combiner,
                        arguments("hello", 'A'), arguments("hello", 'B'), arguments("hello", 'C'),
                        arguments("world", 'A'), arguments("world", 'B'), arguments("world", 'C'));
            }
        }

        @Nested
        @DisplayName("crossJoinBytes(byte...)")
        class WithByteArguments {

            @Test
            @DisplayName("null array")
            void testNullArray() {
                byte[] arguments = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoinBytes(arguments));
            }

            @Test
            @DisplayName("empty array")
            void testEmptyArray() {
                byte[] arguments = {};

                assertSame(combiner, combiner.crossJoinBytes(arguments));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("non-empty array")
            void testNonEmptyArray() {
                byte[] arguments = { 1, 2, 3, };

                assertSame(combiner, combiner.crossJoinBytes(arguments));

                assertArguments(combiner,
                        arguments("hello", (byte) 1), arguments("hello", (byte) 2), arguments("hello", (byte) 3),
                        arguments("world", (byte) 1), arguments("world", (byte) 2), arguments("world", (byte) 3));
            }
        }

        @Nested
        @DisplayName("crossJoinShorts(short...)")
        class WithShortArguments {

            @Test
            @DisplayName("null array")
            void testNullArray() {
                short[] arguments = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoinShorts(arguments));
            }

            @Test
            @DisplayName("empty array")
            void testEmptyArray() {
                short[] arguments = {};

                assertSame(combiner, combiner.crossJoinShorts(arguments));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("non-empty array")
            void testNonEmptyArray() {
                short[] arguments = { 1, 2, 3, };

                assertSame(combiner, combiner.crossJoinShorts(arguments));

                assertArguments(combiner,
                        arguments("hello", (short) 1), arguments("hello", (short) 2), arguments("hello", (short) 3),
                        arguments("world", (short) 1), arguments("world", (short) 2), arguments("world", (short) 3));
            }
        }

        @Nested
        @DisplayName("crossJoinInts(int...)")
        class WithIntArguments {

            @Test
            @DisplayName("null array")
            void testNullArray() {
                int[] arguments = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoinInts(arguments));
            }

            @Test
            @DisplayName("empty array")
            void testEmptyArray() {
                int[] arguments = {};

                assertSame(combiner, combiner.crossJoinInts(arguments));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("non-empty array")
            void testNonEmptyArray() {
                int[] arguments = { 1, 2, 3, };

                assertSame(combiner, combiner.crossJoinInts(arguments));

                assertArguments(combiner,
                        arguments("hello", 1), arguments("hello", 2), arguments("hello", 3),
                        arguments("world", 1), arguments("world", 2), arguments("world", 3));
            }
        }

        @Nested
        @DisplayName("crossJoinLongs(long...)")
        class WithLongArguments {

            @Test
            @DisplayName("null array")
            void testNullArray() {
                long[] arguments = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoinLongs(arguments));
            }

            @Test
            @DisplayName("empty array")
            void testEmptyArray() {
                long[] arguments = {};

                assertSame(combiner, combiner.crossJoinLongs(arguments));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("non-empty array")
            void testNonEmptyArray() {
                long[] arguments = { 1L, 2L, 3L, };

                assertSame(combiner, combiner.crossJoinLongs(arguments));

                assertArguments(combiner,
                        arguments("hello", 1L), arguments("hello", 2L), arguments("hello", 3L),
                        arguments("world", 1L), arguments("world", 2L), arguments("world", 3L));
            }
        }

        @Nested
        @DisplayName("crossJoinFloats(float...)")
        class WithFloatArguments {

            @Test
            @DisplayName("null array")
            void testNullArray() {
                float[] arguments = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoinFloats(arguments));
            }

            @Test
            @DisplayName("empty array")
            void testEmptyArray() {
                float[] arguments = {};

                assertSame(combiner, combiner.crossJoinFloats(arguments));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("non-empty array")
            void testNonEmptyArray() {
                float[] arguments = { 1F, 2F, 3F, };

                assertSame(combiner, combiner.crossJoinFloats(arguments));

                assertArguments(combiner,
                        arguments("hello", 1F), arguments("hello", 2F), arguments("hello", 3F),
                        arguments("world", 1F), arguments("world", 2F), arguments("world", 3F));
            }
        }

        @Nested
        @DisplayName("crossJoinDoubles(double...)")
        class WithDoubleArguments {

            @Test
            @DisplayName("null array")
            void testNullArray() {
                double[] arguments = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoinDoubles(arguments));
            }

            @Test
            @DisplayName("empty array")
            void testEmptyArray() {
                double[] arguments = {};

                assertSame(combiner, combiner.crossJoinDoubles(arguments));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("non-empty array")
            void testNonEmptyArray() {
                double[] arguments = { 1D, 2D, 3D, };

                assertSame(combiner, combiner.crossJoinDoubles(arguments));

                assertArguments(combiner,
                        arguments("hello", 1D), arguments("hello", 2D), arguments("hello", 3D),
                        arguments("world", 1D), arguments("world", 2D), arguments("world", 3D));
            }
        }

        @Nested
        @DisplayName("crossJoin(Object...)")
        class WithObjectArguments {

            @Test
            @DisplayName("null array")
            void testNullArray() {
                Object[] arguments = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoin(arguments));
            }

            @Test
            @DisplayName("empty array")
            void testEmptyArray() {
                Object[] arguments = {};

                assertSame(combiner, combiner.crossJoin(arguments));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("non-empty array")
            void testNonEmptyArray() {
                Object[] arguments = { "foo", "bar", 0, };

                assertSame(combiner, combiner.crossJoin(arguments));

                assertArguments(combiner,
                        arguments("hello", "foo"), arguments("hello", "bar"), arguments("hello", 0),
                        arguments("world", "foo"), arguments("world", "bar"), arguments("world", 0));
            }
        }

        @Nested
        @DisplayName("crossJoin(Collection)")
        class WithArgumentsCollection {

            @Test
            @DisplayName("null collection")
            void testNullCollection() {
                Collection<?> arguments = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoin(arguments));
            }

            @Test
            @DisplayName("empty collection")
            void testEmptyArray() {
                Collection<?> arguments = Collections.emptyList();

                assertSame(combiner, combiner.crossJoin(arguments));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("non-empty collection")
            void testNonEmptyArray() {
                Collection<?> arguments = Arrays.asList("foo", "bar", 0);

                assertSame(combiner, combiner.crossJoin(arguments));

                assertArguments(combiner,
                        arguments("hello", "foo"), arguments("hello", "bar"), arguments("hello", 0),
                        arguments("world", "foo"), arguments("world", "bar"), arguments("world", 0));
            }
        }

        @Nested
        @DisplayName("crossJoin(Supplier<Stream>)")
        class WithArgumentsStreamSupplier {

            @Test
            @DisplayName("null supplier")
            void testNullSupplier() {
                Supplier<Stream<?>> argumentsProvider = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoin(argumentsProvider));
            }

            @Test
            @DisplayName("providing null stream")
            void testProvidingNullStream() {
                Supplier<Stream<?>> argumentsProvider = () -> null;

                assertSame(combiner, combiner.crossJoin(argumentsProvider));

                Stream<? extends Arguments> arguments = combiner.stream();

                assertThrows(NullPointerException.class, arguments::count);
            }

            @Test
            @DisplayName("providing empty stream")
            void testEmptyStream() {
                Supplier<Stream<?>> argumentsProvider = () -> Stream.empty();

                assertSame(combiner, combiner.crossJoin(argumentsProvider));

                assertArguments(combiner);
            }

            @Test
            @DisplayName("providing non-empty stream")
            void testNonEmptyStream() {
                Supplier<Stream<?>> argumentsProvider = () -> Stream.of("foo", "bar", 0);

                assertSame(combiner, combiner.crossJoin(argumentsProvider));

                assertArguments(combiner,
                        arguments("hello", "foo"), arguments("hello", "bar"), arguments("hello", 0),
                        arguments("world", "foo"), arguments("world", "bar"), arguments("world", 0));
            }
        }

        @Nested
        @DisplayName("crossJoin(ArgumentsProvider, ExtensionContext)")
        class WithArgumentsProvider {

            @Test
            @DisplayName("null arguments provider")
            void testNullArgumentsProvider() {
                ArgumentsProvider argumentsProvider = null;
                ExtensionContext context = mock(ExtensionContext.class);

                assertThrows(NullPointerException.class, () -> combiner.crossJoin(argumentsProvider, context));

                verifyNoInteractions(context);
            }

            @Test
            @DisplayName("null context")
            void testNullContext() {
                ArgumentsProvider argumentsProvider = mock(ArgumentsProvider.class);
                ExtensionContext context = null;

                assertThrows(NullPointerException.class, () -> combiner.crossJoin(argumentsProvider, context));

                verifyNoInteractions(argumentsProvider);
            }

            @Test
            @DisplayName("arguments provider providing empty stream")
            void testProvidingEmptyStream() throws Exception {
                ArgumentsProvider argumentsProvider = mock(ArgumentsProvider.class);
                ExtensionContext context = mock(ExtensionContext.class);

                when(argumentsProvider.provideArguments(context)).thenAnswer(i -> Stream.empty());

                assertSame(combiner, combiner.crossJoin(argumentsProvider, context));

                assertArguments(combiner);

                verify(argumentsProvider, times(2)).provideArguments(context);
                verifyNoMoreInteractions(argumentsProvider, context);
            }

            @Test
            @DisplayName("arguments provider providing non-empty stream")
            void testProvidingNonEmptyStream() throws Exception {
                ArgumentsProvider argumentsProvider = mock(ArgumentsProvider.class);
                ExtensionContext context = mock(ExtensionContext.class);

                when(argumentsProvider.provideArguments(context)).thenAnswer(i -> Stream.of(arguments("foo"), arguments("bar"), arguments(0)));

                assertSame(combiner, combiner.crossJoin(argumentsProvider, context));

                assertArguments(combiner,
                        arguments("hello", "foo"), arguments("hello", "bar"), arguments("hello", 0),
                        arguments("world", "foo"), arguments("world", "bar"), arguments("world", 0));

                verify(argumentsProvider, times(2)).provideArguments(context);
                verifyNoMoreInteractions(argumentsProvider, context);
            }

            @Test
            @DisplayName("arguments provider throwing exception")
            void testThrowingException() throws Exception {
                ArgumentsProvider argumentsProvider = mock(ArgumentsProvider.class);
                ExtensionContext context = mock(ExtensionContext.class);
                IOException exception = new IOException();

                when(argumentsProvider.provideArguments(context)).thenThrow(exception);

                assertSame(combiner, combiner.crossJoin(argumentsProvider, context));

                Stream<? extends Arguments> arguments = combiner.stream();

                IOException thrown = assertThrows(IOException.class, arguments::count);
                assertSame(exception, thrown);

                verify(argumentsProvider).provideArguments(context);
                verifyNoMoreInteractions(argumentsProvider, context);
            }
        }
    }

    @Nested
    @DisplayName("excludeCombination(Object...)")
    class ExcludeCombination {

        private ArgumentsCombiner combiner;

        @BeforeEach
        void initCombiner() {
            combiner = ArgumentsCombiner.with(true, false)
                    .crossJoin(1, 2, 3)
                    .crossJoin("foo", "bar");
        }

        @Test
        @DisplayName("null array")
        void testNullArray() {
            Object[] array = null;

            assertThrows(NullPointerException.class, () -> combiner.excludeCombination(array));
        }

        @Test
        @DisplayName("no match")
        void testNoMatch() {
            // Note that the combination exists - just in a different order
            assertSame(combiner, combiner.excludeCombination(true, "bar", 1));

            assertArguments(combiner,
                    arguments(true, 1, "foo"), arguments(true, 1, "bar"),
                    arguments(true, 2, "foo"), arguments(true, 2, "bar"),
                    arguments(true, 3, "foo"), arguments(true, 3, "bar"),
                    arguments(false, 1, "foo"), arguments(false, 1, "bar"),
                    arguments(false, 2, "foo"), arguments(false, 2, "bar"),
                    arguments(false, 3, "foo"), arguments(false, 3, "bar"));
        }

        @Test
        @DisplayName("no match - all nulls")
        void testNoMatchWithAllNulls() {
            assertSame(combiner, combiner.excludeCombination(null, null, null));

            assertArguments(combiner,
                    arguments(true, 1, "foo"), arguments(true, 1, "bar"),
                    arguments(true, 2, "foo"), arguments(true, 2, "bar"),
                    arguments(true, 3, "foo"), arguments(true, 3, "bar"),
                    arguments(false, 1, "foo"), arguments(false, 1, "bar"),
                    arguments(false, 2, "foo"), arguments(false, 2, "bar"),
                    arguments(false, 3, "foo"), arguments(false, 3, "bar"));
        }

        @Test
        @DisplayName("match")
        void testMatch() {
            assertSame(combiner, combiner.excludeCombination(true, 1, "bar"));

            assertArguments(combiner,
                    arguments(true, 1, "foo"),
                    arguments(true, 2, "foo"), arguments(true, 2, "bar"),
                    arguments(true, 3, "foo"), arguments(true, 3, "bar"),
                    arguments(false, 1, "foo"), arguments(false, 1, "bar"),
                    arguments(false, 2, "foo"), arguments(false, 2, "bar"),
                    arguments(false, 3, "foo"), arguments(false, 3, "bar"));
        }

        @Test
        @DisplayName("match with different lengths")
        void testMatchWithDifferentLengths() {
            assertSame(combiner, combiner.excludeCombination(true, 1, "bar"));

            combiner = combiner.crossJoin(1L, 2L, 3L)
                    .excludeCombination(false, 2, "foo", 2L);

            assertArguments(combiner,
                    arguments(true, 1, "foo", 1L), arguments(true, 1, "foo", 2L), arguments(true, 1, "foo", 3L),
                    // all true, 1, "bar" combinations are excluded
                    arguments(true, 2, "foo", 1L), arguments(true, 2, "foo", 2L), arguments(true, 2, "foo", 3L),
                    arguments(true, 2, "bar", 1L), arguments(true, 2, "bar", 2L), arguments(true, 2, "bar", 3L),
                    arguments(true, 3, "foo", 1L), arguments(true, 3, "foo", 2L), arguments(true, 3, "foo", 3L),
                    arguments(true, 3, "bar", 1L), arguments(true, 3, "bar", 2L), arguments(true, 3, "bar", 3L),
                    arguments(false, 1, "foo", 1L), arguments(false, 1, "foo", 2L), arguments(false, 1, "foo", 3L),
                    arguments(false, 1, "bar", 1L), arguments(false, 1, "bar", 2L), arguments(false, 1, "bar", 3L),
                    arguments(false, 2, "foo", 1L), arguments(false, 2, "foo", 3L),
                    arguments(false, 2, "bar", 1L), arguments(false, 2, "bar", 2L), arguments(false, 2, "bar", 3L),
                    arguments(false, 3, "foo", 1L), arguments(false, 3, "foo", 2L), arguments(false, 3, "foo", 3L),
                    arguments(false, 3, "bar", 1L), arguments(false, 3, "bar", 2L), arguments(false, 3, "bar", 3L));
        }

        @Test
        @DisplayName("too few values")
        void testTooFewValues() {
            assertSame(combiner, combiner.excludeCombination(true, 1));

            Stream<? extends Arguments> stream = combiner.stream();

            IllegalStateException thrown = assertThrows(IllegalStateException.class, stream::count);
            assertEquals("Incorrect number of values given. Expected 3, was 2: [true, 1]", thrown.getMessage());
        }

        @Test
        @DisplayName("too many values")
        void testTooManyValues() {
            assertSame(combiner, combiner.excludeCombination(true, 1, "bar", 0));

            Stream<? extends Arguments> stream = combiner.stream();

            IllegalStateException thrown = assertThrows(IllegalStateException.class, stream::count);
            assertEquals("Incorrect number of values given. Expected 3, was 4: [true, 1, bar, 0]", thrown.getMessage());
        }
    }

    @Nested
    @DisplayName("excludeCombinations(Predicate)")
    class ExcludeCombinations {

        private ArgumentsCombiner combiner;

        @BeforeEach
        void initCombiner() {
            combiner = ArgumentsCombiner.with(true, false)
                    .crossJoin(1, 2, 3)
                    .crossJoin("foo", "bar");
        }

        @Test
        @DisplayName("null filter")
        void testNullFilter() {
            assertThrows(NullPointerException.class, () -> combiner.excludeCombinations(null));
        }

        @Test
        @DisplayName("no match")
        void testNoMatch() {
            // Note that the combination exists - just in a different order
            assertSame(combiner, combiner.excludeCombinations(arguments -> false));

            assertArguments(combiner,
                    arguments(true, 1, "foo"), arguments(true, 1, "bar"),
                    arguments(true, 2, "foo"), arguments(true, 2, "bar"),
                    arguments(true, 3, "foo"), arguments(true, 3, "bar"),
                    arguments(false, 1, "foo"), arguments(false, 1, "bar"),
                    arguments(false, 2, "foo"), arguments(false, 2, "bar"),
                    arguments(false, 3, "foo"), arguments(false, 3, "bar"));
        }

        @Test
        @DisplayName("match")
        void testMatch() {
            assertSame(combiner, combiner.excludeCombinations(arguments -> Boolean.TRUE.equals(arguments[0]) && "bar".equals(arguments[2])));

            assertArguments(combiner,
                    arguments(true, 1, "foo"),
                    arguments(true, 2, "foo"),
                    arguments(true, 3, "foo"),
                    arguments(false, 1, "foo"), arguments(false, 1, "bar"),
                    arguments(false, 2, "foo"), arguments(false, 2, "bar"),
                    arguments(false, 3, "foo"), arguments(false, 3, "bar"));
        }
    }

    @Test
    @DisplayName("stream twice")
    void testStreamTwice() {
        ArgumentsCombiner combiner = ArgumentsCombiner.with(true, false)
                .crossJoin(1, 2, 3)
                .crossJoin("foo", "bar");

        assertEquals(12, combiner.stream().count());

        Stream<? extends Arguments> stream = combiner.stream();

        assertThrows(IllegalStateException.class, stream::count);
    }

    @Nested
    @DisplayName("combineArguments")
    class CombineArguments {

        @Test
        @DisplayName("Arguments and Arguments")
        void testCombineArgumentsWithArguments() {
            Object o1 = arguments("a", "b", "c");
            Object o2 = arguments(1, 2, 3);
            Arguments combined = combineArguments(o1, o2);

            assertThat(combined, hasArguments("a", "b", "c", 1, 2, 3));
        }

        @Test
        @DisplayName("Arguments and null")
        void testCombineArgumentsWithNull() {
            Object o1 = arguments("a", "b", "c");
            Object o2 = null;
            Arguments combined = combineArguments(o1, o2);

            assertThat(combined, hasArguments("a", "b", "c", null));
        }

        @Test
        @DisplayName("Arguments and non-null")
        void testCombineArgumentsWithNonNull() {
            Object o1 = arguments("a", "b", "c");
            Object o2 = "foo";
            Arguments combined = combineArguments(o1, o2);

            assertThat(combined, hasArguments("a", "b", "c", "foo"));
        }

        @Test
        @DisplayName("null and Arguments")
        void testCombineNullWithArguments() {
            Object o1 = null;
            Object o2 = arguments("a", "b", "c");
            Arguments combined = combineArguments(o1, o2);

            assertThat(combined, hasArguments(null, "a", "b", "c"));
        }

        @Test
        @DisplayName("null and null")
        void testCombineNullWithNull() {
            Object o1 = null;
            Object o2 = null;
            Arguments combined = combineArguments(o1, o2);

            assertThat(combined, hasArguments(null, null));
        }

        @Test
        @DisplayName("null and non-null")
        void testCombineNullWithNonNull() {
            Object o1 = null;
            Object o2 = "foo";
            Arguments combined = combineArguments(o1, o2);

            assertThat(combined, hasArguments(null, "foo"));
        }

        @Test
        @DisplayName("non-null and Arguments")
        void testCombineNonNullWithArguments() {
            Object o1 = "foo";
            Object o2 = arguments("a", "b", "c");
            Arguments combined = combineArguments(o1, o2);

            assertThat(combined, hasArguments("foo", "a", "b", "c"));
        }

        @Test
        @DisplayName("non-null and null")
        void testCombineNonNullWithNull() {
            Object o1 = "foo";
            Object o2 = null;
            Arguments combined = combineArguments(o1, o2);

            assertThat(combined, hasArguments("foo", null));
        }

        @Test
        @DisplayName("non-null and non-null")
        void testCombineNonNullWithNonNull() {
            Object o1 = "foo";
            Object o2 = "bar";
            Arguments combined = combineArguments(o1, o2);

            assertThat(combined, hasArguments("foo", "bar"));
        }
    }

    private static void assertArguments(ArgumentsCombiner combiner, Arguments... expected) {
        List<Arguments> arguments = combiner.stream()
                .collect(Collectors.toList());

        if (expected.length == 0) {
            assertThat(arguments, empty());
        } else {
            List<Matcher<? super Arguments>> argumentsMatchers = Arrays.stream(expected)
                    .map(Arguments::get)
                    .map(ArgumentsCombinerTest::hasArguments)
                    .collect(Collectors.toList());
            assertThat(arguments, contains(argumentsMatchers));
        }
    }

    private static Matcher<Arguments> hasArguments(Object... arguments) {
        Matcher<Object[]> valuesMatcher = arrayContaining(arguments);

        return new TypeSafeMatcher<Arguments>(Arguments.class) {

            @Override
            protected boolean matchesSafely(Arguments item) {
                return valuesMatcher.matches(item.get());
            }

            @Override
            public void describeTo(Description description) {
                valuesMatcher.describeTo(description);
            }

            @Override
            protected void describeMismatchSafely(Arguments item, Description mismatchDescription) {
                valuesMatcher.describeMismatch(item.get(), mismatchDescription);
            }
        };
    }

    @ParameterizedTest(name = "month = {0}, day = {1}")
    @ArgumentsSource(MonthDayArgumentsProvider.class)
    @DisplayName("site example")
    void testSiteExample(Month month, int day) {
        assertDoesNotThrow(() -> MonthDay.of(month, day));
    }

    private static final class MonthDayArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return ArgumentsCombiner.with(EnumSet.allOf(Month.class))
                    .crossJoin(() -> IntStream.rangeClosed(1, 31).boxed())
                    .excludeCombinations(arguments -> Month.FEBRUARY.equals(arguments[0]) && (int) arguments[1] > 28)
                    .excludeCombination(Month.APRIL, 31)
                    .excludeCombination(Month.JUNE, 31)
                    .excludeCombination(Month.SEPTEMBER, 31)
                    .excludeCombination(Month.NOVEMBER, 31)
                    .stream();
        }
    }
}
