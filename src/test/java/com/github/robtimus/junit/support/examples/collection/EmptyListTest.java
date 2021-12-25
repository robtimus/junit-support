/*
 * EmptyListTest.java
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

package com.github.robtimus.junit.support.examples.collection;

import static com.github.robtimus.junit.support.examples.collection.CollectionFactory.createCollection;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.collections.IteratorTests;
import com.github.robtimus.junit.support.collections.ListIteratorTests;
import com.github.robtimus.junit.support.collections.ListTests;
import com.github.robtimus.junit.support.collections.UnmodifiableListIteratorTests;
import com.github.robtimus.junit.support.collections.CollectionTests.ClearTests;
import com.github.robtimus.junit.support.collections.CollectionTests.ContainsAllTests;
import com.github.robtimus.junit.support.collections.CollectionTests.ContainsTests;
import com.github.robtimus.junit.support.collections.CollectionTests.RemoveAllTests;
import com.github.robtimus.junit.support.collections.CollectionTests.RemoveIfTests;
import com.github.robtimus.junit.support.collections.CollectionTests.RemoveTests;
import com.github.robtimus.junit.support.collections.CollectionTests.RetainAllTests;
import com.github.robtimus.junit.support.collections.CollectionTests.ToArrayTests;
import com.github.robtimus.junit.support.collections.CollectionTests.ToObjectArrayTests;
import com.github.robtimus.junit.support.collections.IterableTests.ForEachTests;
import com.github.robtimus.junit.support.collections.ListTests.EqualsTests;
import com.github.robtimus.junit.support.collections.ListTests.GetTests;
import com.github.robtimus.junit.support.collections.ListTests.HashCodeTests;
import com.github.robtimus.junit.support.collections.ListTests.IndexOfTests;
import com.github.robtimus.junit.support.collections.ListTests.LastIndexOfTests;
import com.github.robtimus.junit.support.collections.ListTests.ListIteratorIndexedTests;
import com.github.robtimus.junit.support.collections.ListTests.SubListTests;
import com.github.robtimus.junit.support.collections.UnmodifiableCollectionTests.AddAllTests;
import com.github.robtimus.junit.support.collections.UnmodifiableCollectionTests.AddTests;
import com.github.robtimus.junit.support.collections.UnmodifiableListTests.AddAllIndexedTests;
import com.github.robtimus.junit.support.collections.UnmodifiableListTests.AddIndexedTests;
import com.github.robtimus.junit.support.collections.UnmodifiableListTests.RemoveIndexedTests;
import com.github.robtimus.junit.support.collections.UnmodifiableListTests.ReplaceAllTests;
import com.github.robtimus.junit.support.collections.UnmodifiableListTests.SetTests;

class EmptyListTest {

    @Nested
    @DisplayName("iterator()")
    class IteratorTest {

        @Nested
        class IterationTest extends IteratorTestBase implements IteratorTests.IterationTests<String> {
            // no additional tests
        }

        @Nested
        class RemoveTest extends IteratorTestBase implements IteratorTests.RemoveTests<String> {
            // no additional tests
        }

        @Nested
        class ForEachRemainingTest extends IteratorTestBase implements IteratorTests.ForEachRemainingTests<String> {
            // no additional tests
        }
    }

    @Nested
    class ForEachTest extends ListTestBase implements ForEachTests<String> {
        // no additional tests
    }

    @Nested
    class ContainsTest extends ListTestBase implements ContainsTests<String> {
        // no additional tests
    }

    @Nested
    class ToObjectArrayTest extends ListTestBase implements ToObjectArrayTests<String> {
        // no additional tests
    }

    @Nested
    class ToArrayTest extends ListTestBase implements ToArrayTests<String> {

        // smaller length is not possible; override to disable the test
        @Override
        public void testToArrayWithSmallerLength() {
            throw new UnsupportedOperationException();
        }
    }

    @Nested
    class AddTest extends ListTestBase implements AddTests<String> {
        // no additional tests
    }

    @Nested
    class RemoveTest extends ListTestBase implements RemoveTests<String> {
        // no additional tests
    }

    @Nested
    class ContainsAllTest extends ListTestBase implements ContainsAllTests<String> {
        // no additional tests
    }

    @Nested
    class AddAllTest extends ListTestBase implements AddAllTests<String> {
        // no additional tests
    }

    @Nested
    class RemoveAllTest extends ListTestBase implements RemoveAllTests<String> {
        // no additional tests
    }

    @Nested
    class RemoveIfTest extends ListTestBase implements RemoveIfTests<String> {
        // no additional tests
    }

    @Nested
    class RetainAllTest extends ListTestBase implements RetainAllTests<String> {
        // no additional tests
    }

    @Nested
    class ClearTest extends ListTestBase implements ClearTests<String> {
        // no additional tests
    }

    @Nested
    class ReplaceAllTest extends ListTestBase implements ReplaceAllTests<String> {

        @Override
        public UnaryOperator<String> replaceElementOperator() {
            return s -> s + s;
        }
    }

    @Nested
    class EqualsTest extends ListTestBase implements EqualsTests<String> {
        // no additional tests
    }

    @Nested
    class HashCodeTest extends ListTestBase implements HashCodeTests<String> {
        // no additional tests
    }

    @Nested
    class GetTest extends ListTestBase implements GetTests<String> {
        // no additional tests
    }

    @Nested
    class SetTest extends ListTestBase implements SetTests<String> {

        @Override
        public UnaryOperator<String> replaceElementOperator() {
            return s -> s + s;
        }
    }

    @Nested
    class AddIndexedTest extends ListTestBase implements AddIndexedTests<String> {
        // no additional tests
    }

    @Nested
    class AddAllIndexedTest extends ListTestBase implements AddAllIndexedTests<String> {
        // no additional tests
    }

    @Nested
    class RemoveIndexedTest extends ListTestBase implements RemoveIndexedTests<String> {
        // no additional tests
    }

    @Nested
    class IndexOfTest extends ListTestBase implements IndexOfTests<String> {
        // no additional tests
    }

    @Nested
    class LastIndexOfTest extends ListTestBase implements LastIndexOfTests<String> {
        // no additional tests
    }

    @Nested
    @DisplayName("listIterator() and listIterator(int)")
    class ListIteratorTest extends ListIteratorTestBase implements ListIteratorIndexedTests<String> {

        // size() / 2 is equal to size(), so this method will not work correctly
        @Override
        public void testListIteratorIndexedWithIndexEqualToSizeDivTwo() {
            throw new UnsupportedOperationException();
        }

        @Nested
        class IterationTest extends IteratorTestBase implements IterationTests<String> {
            // no additional tests
        }

        @Nested
        class RemoveTest extends ListIteratorTestBase implements ListIteratorTests.RemoveTests<String> {
            // no additional tests
        }

        @Nested
        class ForEachRemainingTest extends ListIteratorTestBase implements ForEachRemainingTests<String> {
            // no additional tests
        }

        @Nested
        class SetTest extends ListIteratorTestBase implements ListIteratorTests.SetTests<String> {

            @Override
            public UnaryOperator<String> replaceElementOperator() {
                return s -> s + s;
            }

            @Override
            public String singleElement() {
                return "X"; //$NON-NLS-1$
            }
        }

        @Nested
        class AddTest extends ListIteratorTestBase implements UnmodifiableListIteratorTests.AddTests<String> {

            @Override
            public String newElement() {
                return "X"; //$NON-NLS-1$
            }
        }
    }

    @Nested
    class SubListTest extends ListTestBase implements SubListTests<String> {

        // partial range is not possible; override to disable the test
        @Override
        public void testSubListWithPartialRange() {
            throw new UnsupportedOperationException();
        }
    }

    @Nested
    class SpliteratorTest extends ListTestBase implements ListTests.SpliteratorTests<String> {

        // Collections.emptyList().spliterator() incorrectly does not report ORDERED, so disable this test
        @Override
        public void testSpliteratorHasOrderedCharacteristic() {
            throw new UnsupportedOperationException();
        }

        @Nested
        class TryAdvanceTest extends SpliteratorTestBase
                implements com.github.robtimus.junit.support.collections.SpliteratorTests.TryAdvanceTests<String> {
            // no additional tests
        }

        @Nested
        class ForEachRemainingTest extends SpliteratorTestBase
                implements com.github.robtimus.junit.support.collections.SpliteratorTests.ForEachRemainingTests<String> {
            // no additional tests
        }
    }

    @SuppressWarnings("nls")
    abstract static class ListTestBase implements ListTests<String> {

        private Set<String> methodsCalled;

        @BeforeEach
        void initializeMethodsCalled() {
            methodsCalled = new HashSet<>();
        }

        @Override
        public List<String> iterable() {
            assertTrue(methodsCalled.add("iterable"), "iterable called multiple times");
            return Collections.emptyList();
        }

        @Override
        public List<String> expectedElements() {
            return Collections.unmodifiableList(new ArrayList<>());
        }

        @Override
        public Collection<String> nonContainedElements() {
            List<String> list = createCollection(ArrayList::new, 0, 10);
            return Collections.unmodifiableList(list);
        }
    }

    abstract static class IteratorTestBase extends ListTestBase implements IteratorTests<String> {

        @Override
        public boolean fixedOrder() {
            return true;
        }
    }

    abstract static class ListIteratorTestBase extends ListTestBase implements ListIteratorTests<String> {

        @Override
        public boolean fixedOrder() {
            return true;
        }
    }

    abstract static class SpliteratorTestBase extends ListTestBase
            implements com.github.robtimus.junit.support.collections.SpliteratorTests<String> {

        @Override
        public boolean fixedOrder() {
            return true;
        }
    }
}
