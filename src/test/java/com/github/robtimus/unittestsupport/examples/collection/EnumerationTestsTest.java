/*
 * EnumerationTestsTest.java
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.unittestsupport.collections.EnumerationTests.IterationTests;

@SuppressWarnings("nls")
class EnumerationTestsTest {

    @Nested
    @DisplayName("fixed order")
    class FixedOrder implements IterationTests<String> {

        private Set<String> methodsCalled;

        @BeforeEach
        void initializeMethodsCalled() {
            methodsCalled = new HashSet<>();
        }

        @Override
        public Enumeration<String> enumeration() {
            assertTrue(methodsCalled.add("enumeration"), "enumeration called multiple times");
            return Collections.enumeration(createCollection(ArrayList::new, 0, 10));
        }

        @Override
        public Collection<String> expectedElements() {
            assertTrue(methodsCalled.add("expectedElements"), "expectedElements called multiple times");
            List<String> list = createCollection(ArrayList::new, 0, 10);
            return Collections.unmodifiableList(list);
        }

        @Override
        public boolean fixedOrder() {
            return true;
        }
    }

    @Nested
    @DisplayName("unspecified order")
    class UnspecifiedOrder implements IterationTests<String> {

        private Set<String> methodsCalled;

        @BeforeEach
        void initializeMethodsCalled() {
            methodsCalled = new HashSet<>();
        }

        @Override
        public Enumeration<String> enumeration() {
            assertTrue(methodsCalled.add("enumeration"), "enumeration called multiple times");
            return Collections.enumeration(createCollection(HashSet::new, 0, 10));
        }

        @Override
        public Collection<String> expectedElements() {
            List<String> list = createCollection(ArrayList::new, 0, 10);
            return Collections.unmodifiableList(list);
        }

        @Override
        public boolean fixedOrder() {
            return false;
        }
    }
}
