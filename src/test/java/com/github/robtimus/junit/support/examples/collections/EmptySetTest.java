/*
 * EmptySetTest.java
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

import static com.github.robtimus.junit.support.examples.collections.CollectionFactory.createCollection;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.test.collections.CollectionTests.ClearTests;
import com.github.robtimus.junit.support.test.collections.CollectionTests.ContainsAllTests;
import com.github.robtimus.junit.support.test.collections.CollectionTests.ContainsTests;
import com.github.robtimus.junit.support.test.collections.CollectionTests.RemoveAllTests;
import com.github.robtimus.junit.support.test.collections.CollectionTests.RemoveIfTests;
import com.github.robtimus.junit.support.test.collections.CollectionTests.RemoveTests;
import com.github.robtimus.junit.support.test.collections.CollectionTests.RetainAllTests;
import com.github.robtimus.junit.support.test.collections.CollectionTests.ToArrayTests;
import com.github.robtimus.junit.support.test.collections.CollectionTests.ToArrayWithGeneratorTests;
import com.github.robtimus.junit.support.test.collections.CollectionTests.ToObjectArrayTests;
import com.github.robtimus.junit.support.test.collections.IterableTests.ForEachTests;
import com.github.robtimus.junit.support.test.collections.IteratorTests;
import com.github.robtimus.junit.support.test.collections.SetTests;
import com.github.robtimus.junit.support.test.collections.SetTests.EqualsTests;
import com.github.robtimus.junit.support.test.collections.SetTests.HashCodeTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableCollectionTests.AddAllTests;
import com.github.robtimus.junit.support.test.collections.UnmodifiableCollectionTests.AddTests;

class EmptySetTest {

    @Nested
    @DisplayName("iterator()")
    class IteratorTest extends IteratorTestBase {

        @Nested
        class IterationTest extends IteratorTestBase implements IterationTests<String> {
            // no additional tests
        }

        @Nested
        class RemoveTest extends IteratorTestBase implements IteratorTests.RemoveTests<String> {
            // no additional tests
        }

        @Nested
        class ForEachRemainingTest extends IteratorTestBase implements ForEachRemainingTests<String> {
            // no additional tests
        }
    }

    @Nested
    class ForEachTest extends SetTestBase implements ForEachTests<String> {
        // no additional tests
    }

    @Nested
    class ContainsTest extends SetTestBase implements ContainsTests<String> {
        // no additional tests
    }

    @Nested
    class ToObjectArrayTest extends SetTestBase implements ToObjectArrayTests<String> {
        // no additional tests
    }

    @Nested
    class ToArrayTest extends SetTestBase implements ToArrayTests<String> {

        // smaller length is not possible; override to disable the test
        @Override
        public void testToArrayWithSmallerLength() {
            throw new UnsupportedOperationException();
        }
    }

    @Nested
    class ToArrayWithGeneratorTest extends SetTestBase implements ToArrayWithGeneratorTests<String> {
        // no additional tests
    }

    @Nested
    class ContainsAllTest extends SetTestBase implements ContainsAllTests<String> {
        // no additional tests
    }

    @Nested
    class AddTest extends SetTestBase implements AddTests<String> {
        // no additional tests
    }

    @Nested
    class RemoveTest extends SetTestBase implements RemoveTests<String> {
        // no additional tests
    }

    @Nested
    class AddAllTest extends SetTestBase implements AddAllTests<String> {
        // no additional tests
    }

    @Nested
    class RemoveAllTest extends SetTestBase implements RemoveAllTests<String> {
        // no additional tests
    }

    @Nested
    class RemoveIfTest extends SetTestBase implements RemoveIfTests<String> {
        // no additional tests
    }

    @Nested
    class RetainAllTest extends SetTestBase implements RetainAllTests<String> {
        // no additional tests
    }

    @Nested
    class ClearTest extends SetTestBase implements ClearTests<String> {
        // no additional tests
    }

    @Nested
    class EqualsTest extends SetTestBase implements EqualsTests<String> {
        // no additional tests
    }

    @Nested
    class HashCodeTest extends SetTestBase implements HashCodeTests<String> {
        // no additional tests
    }

    @Nested
    class SpliteratorTest extends SetTestBase implements SetTests.SpliteratorTests<String> {

        // Collections.emptySet().spliterator() incorrectly does not report DISTINCT, so disable this test
        @Override
        public void testSpliteratorHasDistinctCharacteristic() {
            throw new UnsupportedOperationException();
        }

        @Nested
        class TryAdvanceTest extends SpliteratorTestBase
                implements com.github.robtimus.junit.support.test.collections.SpliteratorTests.TryAdvanceTests<String> {
            // no additional tests
        }

        @Nested
        class ForEachRemainingTest extends SpliteratorTestBase
                implements com.github.robtimus.junit.support.test.collections.SpliteratorTests.ForEachRemainingTests<String> {
            // no additional tests
        }
    }

    @SuppressWarnings("nls")
    abstract static class SetTestBase implements SetTests<String> {

        private Set<String> methodsCalled;

        @BeforeEach
        void initializeMethodsCalled() {
            methodsCalled = new HashSet<>();
        }

        @Override
        public Set<String> iterable() {
            assertTrue(methodsCalled.add("iterable"), "iterable called multiple times");
            return Collections.emptySet();
        }

        @Override
        public Collection<String> expectedElements() {
            return Collections.unmodifiableList(new ArrayList<>());
        }

        @Override
        public Collection<String> nonContainedElements() {
            List<String> list = createCollection(ArrayList::new, 0, 10);
            return Collections.unmodifiableList(list);
        }

        @Override
        public boolean fixedOrder() {
            return false;
        }
    }

    abstract static class IteratorTestBase extends SetTestBase implements IteratorTests<String> {
        // no additional methods needed at this time
    }

    abstract static class SpliteratorTestBase extends SetTestBase
            implements com.github.robtimus.junit.support.test.collections.SpliteratorTests<String> {
        // no additional methods needed at this time
    }
}
