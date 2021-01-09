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

package com.github.robtimus.unittestsupport.examples.collection;

import static com.github.robtimus.unittestsupport.examples.collection.CollectionFactory.createCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.unittestsupport.collections.CollectionTests.ContainsAllTests;
import com.github.robtimus.unittestsupport.collections.CollectionTests.ContainsTests;
import com.github.robtimus.unittestsupport.collections.CollectionTests.ToArrayTests;
import com.github.robtimus.unittestsupport.collections.CollectionTests.ToObjectArrayTests;
import com.github.robtimus.unittestsupport.collections.IterableTests.ForEachTests;
import com.github.robtimus.unittestsupport.collections.SetTests;
import com.github.robtimus.unittestsupport.collections.SetTests.EqualsTests;
import com.github.robtimus.unittestsupport.collections.SetTests.HashCodeTests;
import com.github.robtimus.unittestsupport.collections.UnmodifiableCollectionTests.AddAllTests;
import com.github.robtimus.unittestsupport.collections.UnmodifiableCollectionTests.AddTests;
import com.github.robtimus.unittestsupport.collections.UnmodifiableCollectionTests.ClearTests;
import com.github.robtimus.unittestsupport.collections.UnmodifiableCollectionTests.RemoveAllTests;
import com.github.robtimus.unittestsupport.collections.UnmodifiableCollectionTests.RemoveIfTests;
import com.github.robtimus.unittestsupport.collections.UnmodifiableCollectionTests.RemoveTests;
import com.github.robtimus.unittestsupport.collections.UnmodifiableCollectionTests.RetainAllTests;
import com.github.robtimus.unittestsupport.collections.UnmodifiableIteratorTests;
import com.github.robtimus.unittestsupport.collections.UnmodifiableSetTests;

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
    class ContainsAllTest extends SetTestBase implements ContainsAllTests<String> {
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

        @Nested
        class TryAdvanceTest extends SpliteratorTestBase
                implements com.github.robtimus.unittestsupport.collections.SpliteratorTests.TryAdvanceTests<String> {
            // no additional tests
        }

        @Nested
        class ForEachRemainingTest extends SpliteratorTestBase
                implements com.github.robtimus.unittestsupport.collections.SpliteratorTests.ForEachRemainingTests<String> {
            // no additional tests
        }
    }

    abstract static class SetTestBase implements UnmodifiableSetTests<String> {

        @Override
        public Set<String> createIterable() {
            Set<String> set = createCollection(HashSet::new, 0, 10);
            return Collections.unmodifiableSet(set);
        }

        @Override
        public Collection<String> expectedElements() {
            List<String> list = createCollection(ArrayList::new, 0, 10);
            return Collections.unmodifiableList(list);
        }

        @Override
        public Collection<String> nonContainedElements() {
            List<String> list = createCollection(ArrayList::new, 10, 20);
            return Collections.unmodifiableList(list);
        }

        @Override
        public boolean fixedOrder() {
            return false;
        }
    }

    abstract static class IteratorTestBase extends SetTestBase implements UnmodifiableIteratorTests<String> {
        // no additional methods needed at this time
    }

    abstract static class SpliteratorTestBase extends SetTestBase
            implements com.github.robtimus.unittestsupport.collections.SpliteratorTests<String> {
        // no additional methods needed at this time
    }
}