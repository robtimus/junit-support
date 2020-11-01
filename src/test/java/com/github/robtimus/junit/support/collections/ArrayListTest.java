/*
 * ArrayListTest.java
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
import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import com.github.robtimus.junit.support.collections.ListTests.AddAllIndexedTests;
import com.github.robtimus.junit.support.collections.ListTests.AddAllTests;
import com.github.robtimus.junit.support.collections.ListTests.AddIndexedTests;
import com.github.robtimus.junit.support.collections.ListTests.AddTests;
import com.github.robtimus.junit.support.collections.ListTests.EqualsTests;
import com.github.robtimus.junit.support.collections.ListTests.GetTests;
import com.github.robtimus.junit.support.collections.ListTests.HashCodeTests;
import com.github.robtimus.junit.support.collections.ListTests.IndexOfTests;
import com.github.robtimus.junit.support.collections.ListTests.LastIndexOfTests;
import com.github.robtimus.junit.support.collections.ListTests.ListIteratorIndexedTests;
import com.github.robtimus.junit.support.collections.ListTests.RemoveIndexedTests;
import com.github.robtimus.junit.support.collections.ListTests.ReplaceAllTests;
import com.github.robtimus.junit.support.collections.ListTests.SetTests;
import com.github.robtimus.junit.support.collections.ListTests.SpliteratorTests;
import com.github.robtimus.junit.support.collections.ListTests.SubListTests;

class ArrayListTest {

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
        // no additional tests
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
        }

        @Nested
        class AddTest extends ListIteratorTestBase implements ListIteratorTests.AddTests<String> {

            @Override
            public String newElement() {
                return "X"; //$NON-NLS-1$
            }
        }
    }

    @Nested
    class SubListTest extends ListTestBase implements SubListTests<String> {

        @Nested
        class RemoveTest implements RemoveTests<String> {

            @Override
            public Collection<String> createIterable() {
                List<String> list = createCollection(ArrayList::new, 0, 10);
                return list.subList(3, 7);
            }

            @Override
            public Collection<String> expectedElements() {
                return createCollection(ArrayList::new, 3, 7);
            }

            @Override
            public Collection<String> nonContainedElements() {
                return createCollection(ArrayList::new, 7, 10);
            }

            @Override
            public boolean fixedOrder() {
                return true;
            }
        }
    }

    @Nested
    class SpliteratorTest extends ListTestBase implements SpliteratorTests<String> {
        // no additional tests
    }

    abstract static class ListTestBase implements ListTests<String> {

        @Override
        public List<String> createIterable() {
            return createCollection(ArrayList::new, 0, 10);
        }

        @Override
        public List<String> expectedElements() {
            return createCollection(ArrayList::new, 0, 10);
        }

        @Override
        public Collection<String> nonContainedElements() {
            return createCollection(ArrayList::new, 10, 20);
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
}
