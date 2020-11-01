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

package com.github.robtimus.junit.support.collections.examples;

import static com.github.robtimus.junit.support.collections.examples.CollectionFactory.createCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.collections.EnumerationTests.IterationTests;

class EnumerationTestsTest {

    @Nested
    @DisplayName("fixed order")
    class FixedOrder implements IterationTests<String> {

        @Override
        public Enumeration<String> createEnumeration() {
            return Collections.enumeration(createCollection(ArrayList::new, 0, 10));
        }

        @Override
        public Collection<String> expectedElements() {
            return createCollection(ArrayList::new, 0, 10);
        }

        @Override
        public boolean fixedOrder() {
            return true;
        }
    }

    @Nested
    @DisplayName("unspecified order")
    class UnspecifiedOrder implements IterationTests<String> {

        @Override
        public Enumeration<String> createEnumeration() {
            return Collections.enumeration(createCollection(HashSet::new, 0, 10));
        }

        @Override
        public Collection<String> expectedElements() {
            return createCollection(ArrayList::new, 0, 10);
        }

        @Override
        public boolean fixedOrder() {
            return false;
        }
    }
}
