/*
 * CollectionUtilsTest.java
 * Copyright 2020 Rob Spoor
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

package com.github.robtimus.junit.support.collections;

import static com.github.robtimus.junit.support.collections.CollectionUtils.commonType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

@SuppressWarnings("nls")
class CollectionUtilsTest {

    @ParameterizedTest(name = "{0}: {1}")
    @ArgumentsSource(CommonTypeArgumentProvider.class)
    void testCommonType(Collection<?> collection, Class<?> expected) {
        assertEquals(expected, commonType(collection));
    }

    private static final class CommonTypeArgumentProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
                    arguments(Arrays.asList("1", "2", "3"), String.class),
                    arguments(Arrays.asList(1, 1L, 1D), Number.class),
                    arguments(Arrays.asList("1", 1, true, new byte[0]), Serializable.class),
                    arguments(Arrays.asList("1", Collections.emptyIterator()), Object.class)
                    );
        }
    }
}
