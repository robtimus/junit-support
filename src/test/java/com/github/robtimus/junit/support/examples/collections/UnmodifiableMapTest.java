/*
 * UnmodifiableMapTest.java
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

package com.github.robtimus.junit.support.examples.collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.JRE;
import com.github.robtimus.junit.support.test.collections.MapEntryTests;
import com.github.robtimus.junit.support.test.collections.MapTests.ContainsKeyTests;
import com.github.robtimus.junit.support.test.collections.MapTests.ContainsValueTests;
import com.github.robtimus.junit.support.test.collections.MapTests.EqualsTests;
import com.github.robtimus.junit.support.test.collections.MapTests.ForEachTests;
import com.github.robtimus.junit.support.test.collections.MapTests.GetOrDefaultTests;
import com.github.robtimus.junit.support.test.collections.MapTests.GetTests;
import com.github.robtimus.junit.support.test.collections.MapTests.HashCodeTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapEntryTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.ClearTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.ComputeIfAbsentTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.ComputeIfPresentTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.ComputeTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.KeySetTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.MergeTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.PutAllTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.PutIfAbsentTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.PutTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.RemoveExactValueTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.RemoveTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.ReplaceAllTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.ReplaceExactValueTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableMapTests.ReplaceTests;

class UnmodifiableMapTest {

    @Nested
    class ContainsKeyTest extends MapTestBase implements ContainsKeyTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class ContainsValueTest extends MapTestBase implements ContainsValueTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class GetTest extends MapTestBase implements GetTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class PutTest extends MapTestBase implements PutTests<Integer, String> {

        @Override
        public UnaryOperator<String> replaceValueOperator() {
            return s -> s + s;
        }
    }

    @Nested
    class RemoveTest extends MapTestBase implements RemoveTests<Integer, String> {
        // no additional tests
    }

    @Nested
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
    @DisplayName("keySet()")
    class KeySetTest extends KeySetTestBase {

        @Nested
        @DisplayName("iterator()")
        class IteratorTest extends KeySetTestBase implements KeySetTests.IteratorTests<Integer, String> {

            @Nested
            class IterationTest extends KeySetTestBase implements KeySetTests.IteratorTests.IterationTests<Integer, String> {
                // no additional tests
            }

            @Nested
            class RemoveTest extends KeySetTestBase implements KeySetTests.IteratorTests.RemoveTests<Integer, String> {
                // no additional tests
            }

            @Nested
            class ForEachRemainingTest extends KeySetTestBase implements KeySetTests.IteratorTests.ForEachRemainingTests<Integer, String> {
                // no additional tests
            }
        }

        @Nested
        class ForEachTest extends KeySetTestBase implements KeySetTests.ForEachTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ContainsTest extends KeySetTestBase implements KeySetTests.ContainsTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ToObjectArrayTest extends KeySetTestBase implements KeySetTests.ToObjectArrayTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ToArrayTest extends KeySetTestBase implements KeySetTests.ToArrayTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class AddTest extends KeySetTestBase implements KeySetTests.AddTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RemoveTest extends KeySetTestBase implements KeySetTests.RemoveTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ContainsAllTest extends KeySetTestBase implements KeySetTests.ContainsAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class AddAllTest extends KeySetTestBase implements KeySetTests.AddAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RemoveAllTest extends KeySetTestBase implements KeySetTests.RemoveAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RemoveIfTest extends KeySetTestBase implements KeySetTests.RemoveIfTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RetainAllTest extends KeySetTestBase implements KeySetTests.RetainAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ClearTest extends KeySetTestBase implements KeySetTests.ClearTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class EqualsTest extends KeySetTestBase implements KeySetTests.EqualsTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class HashCodeTest extends KeySetTestBase implements KeySetTests.HashCodeTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class SpliteratorTest extends KeySetTestBase implements KeySetTests.SpliteratorTests<Integer, String> {
            // no additional tests
        }
    }

    @Nested
    @DisplayName("values()")
    class ValuesTest extends ValuesTestBase {

        @Nested
        @DisplayName("iterator()")
        class IteratorTest extends ValuesTestBase implements ValuesTests.IteratorTests<Integer, String> {

            @Nested
            class IterationTest extends ValuesTestBase implements ValuesTests.IteratorTests.IterationTests<Integer, String> {
                // no additional tests
            }

            @Nested
            class RemoveTest extends ValuesTestBase implements ValuesTests.IteratorTests.RemoveTests<Integer, String> {
                // no additional tests
            }

            @Nested
            class ForEachRemainingTest extends ValuesTestBase implements ValuesTests.IteratorTests.ForEachRemainingTests<Integer, String> {
                // no additional tests
            }
        }

        @Nested
        class ForEachTest extends ValuesTestBase implements UnmodifiableMapTests.ValuesTests.ForEachTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ContainsTest extends ValuesTestBase implements ValuesTests.ContainsTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ToObjectArrayTest extends ValuesTestBase implements ValuesTests.ToObjectArrayTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ToArrayTest extends ValuesTestBase implements ValuesTests.ToArrayTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class AddTest extends ValuesTestBase implements ValuesTests.AddTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RemoveTest extends ValuesTestBase implements ValuesTests.RemoveTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ContainsAllTest extends ValuesTestBase implements ValuesTests.ContainsAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class AddAllTest extends ValuesTestBase implements ValuesTests.AddAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RemoveAllTest extends ValuesTestBase implements ValuesTests.RemoveAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RemoveIfTest extends ValuesTestBase implements ValuesTests.RemoveIfTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RetainAllTest extends ValuesTestBase implements ValuesTests.RetainAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ClearTest extends ValuesTestBase implements ValuesTests.ClearTests<Integer, String> {
            // no additional tests
        }
    }

    @Nested
    @DisplayName("entrySet()")
    class EntrySetTest extends EntrySetTestBase {

        @Nested
        @DisplayName("iterator()")
        class IteratorTest extends EntrySetTestBase implements EntrySetTests.IteratorTests<Integer, String> {

            @Nested
            class IterationTest extends EntrySetTestBase implements EntrySetTests.IteratorTests.IterationTests<Integer, String> {
                // no additional tests
            }

            @Nested
            class RemoveTest extends EntrySetTestBase implements EntrySetTests.IteratorTests.RemoveTests<Integer, String> {
                // no additional tests
            }

            @Nested
            class ForEachRemainingTest extends EntrySetTestBase implements EntrySetTests.IteratorTests.ForEachRemainingTests<Integer, String> {

                @Override
                public boolean hasFailFastNullCheck() {
                    // Since Java 11, iterator.forEachRemaining does not perform a null check.
                    // JRE.OTHER is larger than any other JRE; when the null check is added and released, the upper bound should be fixed
                    return !EnumSet.range(JRE.JAVA_11, JRE.OTHER).contains(JRE.currentVersion());
                }
            }
        }

        @Nested
        class ForEachTest extends EntrySetTestBase implements EntrySetTests.ForEachTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ContainsTest extends EntrySetTestBase implements EntrySetTests.ContainsTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ToObjectArrayTest extends EntrySetTestBase implements EntrySetTests.ToObjectArrayTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ToArrayTest extends EntrySetTestBase implements EntrySetTests.ToArrayTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class AddTest extends EntrySetTestBase implements EntrySetTests.AddTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RemoveTest extends EntrySetTestBase implements EntrySetTests.RemoveTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ContainsAllTest extends EntrySetTestBase implements EntrySetTests.ContainsAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class AddAllTest extends EntrySetTestBase implements EntrySetTests.AddAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RemoveAllTest extends EntrySetTestBase implements EntrySetTests.RemoveAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RemoveIfTest extends EntrySetTestBase implements EntrySetTests.RemoveIfTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class RetainAllTest extends EntrySetTestBase implements EntrySetTests.RetainAllTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class ClearTest extends EntrySetTestBase implements EntrySetTests.ClearTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class EqualsTest extends EntrySetTestBase implements EntrySetTests.EqualsTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class HashCodeTest extends EntrySetTestBase implements EntrySetTests.HashCodeTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class SpliteratorTest extends EntrySetTestBase implements EntrySetTests.SpliteratorTests<Integer, String> {
            // no additional tests
        }
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
    class RemoveExactValueTest extends MapTestBase implements RemoveExactValueTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class ReplaceExactValueTest extends MapTestBase implements ReplaceExactValueTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class ReplaceTest extends MapTestBase implements ReplaceTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class ComputeIfAbsentTest extends MapTestBase implements ComputeIfAbsentTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class ComputeIfPresentTest extends MapTestBase implements ComputeIfPresentTests<Integer, String> {
        // no additional tests
    }

    @Nested
    class ComputeTest extends MapTestBase implements ComputeTests<Integer, String> {

        @Override
        public UnaryOperator<String> replaceValueOperator() {
            return s -> s + s;
        }
    }

    @Nested
    class MergeTest extends MapTestBase implements MergeTests<Integer, String> {

        @Override
        public BinaryOperator<String> combineValuesOperator() {
            return String::concat;
        }
    }

    @Nested
    @DisplayName("Map.Entry")
    class EntryTest extends MapTestBase implements MapEntryTests<Integer, String> {

        @Nested
        class GetValueTest extends MapTestBase implements MapEntryTests.GetValueTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class SetValueTest extends MapTestBase implements UnmodifiableMapEntryTests.SetValueTests<Integer, String> {

            @Override
            public UnaryOperator<String> replaceValueOperator() {
                return s -> s + s;
            }
        }

        @Nested
        class EqualsTest extends MapTestBase implements MapEntryTests.EqualsTests<Integer, String> {
            // no additional tests
        }

        @Nested
        class HashCodeTest extends MapTestBase implements MapEntryTests.HashCodeTests<Integer, String> {
            // no additional tests
        }
    }

    @SuppressWarnings("nls")
    abstract static class MapTestBase implements UnmodifiableMapTests<Integer, String> {

        private Set<String> methodsCalled;

        @BeforeEach
        void initializeMethodsCalled() {
            methodsCalled = new HashSet<>();
        }

        @Override
        public Map<Integer, String> map() {
            assertTrue(methodsCalled.add("map"), "map called multiple times");
            Map<Integer, String> map = CollectionFactory.createMap(HashMap::new, 0, 10);
            return Collections.unmodifiableMap(map);
        }

        @Override
        public Map<Integer, String> expectedEntries() {
            Map<Integer, String> map = CollectionFactory.createMap(HashMap::new, 0, 10);
            return Collections.unmodifiableMap(map);
        }

        @Override
        public Map<Integer, String> nonContainedEntries() {
            Map<Integer, String> map = CollectionFactory.createMap(HashMap::new, 10, 20);
            return Collections.unmodifiableMap(map);
        }
    }

    abstract static class KeySetTestBase extends MapTestBase implements KeySetTests<Integer, String> {

        @Override
        public boolean fixedOrder() {
            return false;
        }
    }

    abstract static class ValuesTestBase extends MapTestBase implements UnmodifiableMapTests.ValuesTests<Integer, String> {

        @Override
        public boolean fixedOrder() {
            return false;
        }
    }

    abstract static class EntrySetTestBase extends MapTestBase implements UnmodifiableMapTests.EntrySetTests<Integer, String> {

        @Override
        public boolean fixedOrder() {
            return false;
        }
    }
}
