/*
 * TreeMapTest.java
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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.collections.MapTests.ClearTests;
import com.github.robtimus.junit.support.collections.MapTests.ContainsKeyTests;
import com.github.robtimus.junit.support.collections.MapTests.ContainsValueTests;
import com.github.robtimus.junit.support.collections.MapTests.EqualsTests;
import com.github.robtimus.junit.support.collections.MapTests.ForEachTests;
import com.github.robtimus.junit.support.collections.MapTests.GetOrDefaultTests;
import com.github.robtimus.junit.support.collections.MapTests.GetTests;
import com.github.robtimus.junit.support.collections.MapTests.HashCodeTests;
import com.github.robtimus.junit.support.collections.MapTests.PutAllTests;
import com.github.robtimus.junit.support.collections.MapTests.PutIfAbsentTests;
import com.github.robtimus.junit.support.collections.MapTests.PutTests;
import com.github.robtimus.junit.support.collections.MapTests.RemoveExactValueTests;
import com.github.robtimus.junit.support.collections.MapTests.RemoveTests;
import com.github.robtimus.junit.support.collections.MapTests.ReplaceAllTests;
import com.github.robtimus.junit.support.collections.MapTests.ReplaceExactValueTests;
import com.github.robtimus.junit.support.collections.MapTests.ReplaceTests;
import com.github.robtimus.junit.support.collections.annotation.ContainsIncompatibleKeyNotSupported;
import com.github.robtimus.junit.support.collections.annotation.ContainsNullKeyNotSupported;
import com.github.robtimus.junit.support.collections.annotation.RemoveIncompatibleKeyNotSupported;
import com.github.robtimus.junit.support.collections.annotation.RemoveNullKeyNotSupported;
import com.github.robtimus.junit.support.collections.annotation.StoreNullKeyNotSupported;

class TreeMapTest {

    @Nested
    @ContainsNullKeyNotSupported
    @ContainsIncompatibleKeyNotSupported(expected = ClassCastException.class)
    class ContainsKeyTest extends MapTestBase implements ContainsKeyTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class ContainsValueTest extends MapTestBase implements ContainsValueTests<Integer, String> {
        // no additional tests
    }

    @Nested
    @ContainsNullKeyNotSupported
    @ContainsIncompatibleKeyNotSupported(expected = ClassCastException.class)
    class GetTest extends MapTestBase implements GetTests<Integer, String> {
        // no additional tests
    }

    @Nested
    @StoreNullKeyNotSupported
    class PutTest extends MapTestBase implements PutTests<Integer, String> {

        @Override
        public UnaryOperator<String> replaceValueOperator() {
            return s -> s + s;
        }
    }

    @Nested
    @RemoveNullKeyNotSupported
    @RemoveIncompatibleKeyNotSupported(expected = ClassCastException.class)
    class RemoveTest extends MapTestBase implements RemoveTests<Integer, String> {
        // no additional tests
    }

    @Nested
    @StoreNullKeyNotSupported
    class PutAllTest extends MapTestBase implements PutAllTests<Integer, String> {

        @Override
        public UnaryOperator<String> replaceValueOperator() {
            return s -> s + s;
        }
    }

    @Nested
    class ClearTest extends MapTestBase implements ClearTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class EqualsTest extends MapTestBase implements EqualsTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class HashCodeTest extends MapTestBase implements HashCodeTests<Integer, String> {
        // no additional tests
    }

    @Nested
    @ContainsNullKeyNotSupported
    @ContainsIncompatibleKeyNotSupported(expected = ClassCastException.class)
    class GetOrDefaultTest extends MapTestBase implements GetOrDefaultTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class ForEachTest extends MapTestBase implements ForEachTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class ReplaceAllTest extends MapTestBase implements ReplaceAllTests<Integer, String> {

        @Override
        public BiFunction<Integer, String, String> replaceValueFunction() {
            return (k, v) -> k + v;
        }
    }

    @Nested
    class PutIfAbsentTest extends MapTestBase implements PutIfAbsentTests<Integer, String> {
        // no additional tests
    }

    @Nested
    @RemoveNullKeyNotSupported
    @RemoveIncompatibleKeyNotSupported(expected = ClassCastException.class)
    class RemoveExactValueTest extends MapTestBase implements RemoveExactValueTests<Integer, String> {
        // no additional tests
    }

    @Nested
    @StoreNullKeyNotSupported
    class ReplaceExactValueTest extends MapTestBase implements ReplaceExactValueTests<Integer, String> {
        // no additional tests
    }

    @Nested
    @StoreNullKeyNotSupported
    class ReplaceTest extends MapTestBase implements ReplaceTests<Integer, String> {
        // no additional tests
    }

    abstract static class MapTestBase implements MapTests<Integer, String> {

        @Override
        public Map<Integer, String> createMap() {
            return CollectionFactory.createMap(TreeMap::new, 0, 10);
        }

        @Override
        public Map<Integer, String> expectedEntries() {
            return CollectionFactory.createMap(HashMap::new, 0, 10);
        }

        @Override
        public Map<Integer, String> nonContainedEntries() {
            return CollectionFactory.createMap(HashMap::new, 10, 20);
        }
    }
}
