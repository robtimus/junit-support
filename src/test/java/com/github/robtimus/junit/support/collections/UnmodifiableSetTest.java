/*
 * UnmodifiableSetTest.java
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

import static com.github.robtimus.junit.support.collections.CollectionFactory.createCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.collections.CollectionTests.ContainsAllTests;
import com.github.robtimus.junit.support.collections.CollectionTests.ContainsTests;
import com.github.robtimus.junit.support.collections.CollectionTests.ToArrayTests;
import com.github.robtimus.junit.support.collections.CollectionTests.ToObjectArrayTests;
import com.github.robtimus.junit.support.collections.IterableTests.ForEachTests;
import com.github.robtimus.junit.support.collections.SetTests.EqualsTests;
import com.github.robtimus.junit.support.collections.SetTests.HashCodeTests;
import com.github.robtimus.junit.support.collections.SetTests.SpliteratorTests;
import com.github.robtimus.junit.support.collections.UnmodifiableCollectionTests.AddAllTests;
import com.github.robtimus.junit.support.collections.UnmodifiableCollectionTests.AddTests;
import com.github.robtimus.junit.support.collections.UnmodifiableCollectionTests.ClearTests;
import com.github.robtimus.junit.support.collections.UnmodifiableCollectionTests.RemoveAllTests;
import com.github.robtimus.junit.support.collections.UnmodifiableCollectionTests.RemoveIfTests;
import com.github.robtimus.junit.support.collections.UnmodifiableCollectionTests.RemoveTests;
import com.github.robtimus.junit.support.collections.UnmodifiableCollectionTests.RetainAllTests;

class UnmodifiableSetTest {

    @Nested
    @DisplayName("iterator()")
    class IteratorTest extends IteratorTestBase {

        @Nested
        class IterationTest extends IteratorTestBase implements IterationTests<String> {
            // no additional tests
        }

        @Nested
        class RemoveTest extends IteratorTestBase implements UnmodifiableIteratorTests.RemoveTests<String> {
            // no additional tests
        }

        @Nested
        class ForEachRemainingTest extends IteratorTestBase implements ForEachRemainingTests<String> {
            // no additional tests
        }
    }

    @Nested
    class ForEachTest extends UnmodifiableSetTestBase implements ForEachTests<String> {
        // no additional tests
    }

    @Nested
    class ContainsTest extends UnmodifiableSetTestBase implements ContainsTests<String> {
        // no additional tests
    }

    @Nested
    class ToObjectArrayTest extends UnmodifiableSetTestBase implements ToObjectArrayTests<String> {
        // no additional tests
    }

    @Nested
    class ToArrayTest extends UnmodifiableSetTestBase implements ToArrayTests<String> {
        // no additional tests
    }

    @Nested
    class AddTest extends UnmodifiableSetTestBase implements AddTests<String> {
        // no additional tests
    }

    @Nested
    class RemoveTest extends UnmodifiableSetTestBase implements RemoveTests<String> {
        // no additional tests
    }

    @Nested
    class ContainsAllTest extends UnmodifiableSetTestBase implements ContainsAllTests<String> {
        // no additional tests
    }

    @Nested
    class AddAllTest extends UnmodifiableSetTestBase implements AddAllTests<String> {
        // no additional tests
    }

    @Nested
    class RemoveAllTest extends UnmodifiableSetTestBase implements RemoveAllTests<String> {
        // no additional tests
    }

    @Nested
    class RemoveIfTest extends UnmodifiableSetTestBase implements RemoveIfTests<String> {
        // no additional tests
    }

    @Nested
    class RetainAllTest extends UnmodifiableSetTestBase implements RetainAllTests<String> {
        // no additional tests
    }

    @Nested
    class ClearTest extends UnmodifiableSetTestBase implements ClearTests<String> {
        // no additional tests
    }

    @Nested
    class EqualsTest extends UnmodifiableSetTestBase implements EqualsTests<String> {
        // no additional tests
    }

    @Nested
    class HashCodeTest extends UnmodifiableSetTestBase implements HashCodeTests<String> {
        // no additional tests
    }

    @Nested
    class SpliteratorTest extends UnmodifiableSetTestBase implements SpliteratorTests<String> {
        // no additional tests
    }

    abstract static class UnmodifiableSetTestBase implements UnmodifiableSetTests<String> {

        @Override
        public Set<String> createIterable() {
            Set<String> set = createCollection(HashSet::new, 0, 10);
            return Collections.unmodifiableSet(set);
        }

        @Override
        public Collection<String> expectedElements() {
            return createCollection(ArrayList::new, 0, 10);
        }

        @Override
        public Collection<String> nonContainedElements() {
            return createCollection(ArrayList::new, 10, 20);
        }

        @Override
        public boolean fixedOrder() {
            return false;
        }
    }

    abstract static class IteratorTestBase extends UnmodifiableSetTestBase implements UnmodifiableIteratorTests<String> {
        // no additional methods needed at this time
    }
}
