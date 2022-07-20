/*
 * TestObjectTest.java
 * Copyright 2022 Rob Spoor
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

package com.github.robtimus.junit.support.examples.equalshashcode;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.EqualsAndHashCodeTests;

@SuppressWarnings("nls")
class TestObjectSubTest {

    @Nested
    @DisplayName("non-null String")
    class NonNullString implements EqualsAndHashCodeTests<TestObjectSub> {

        @Override
        public TestObjectSub object() {
            return new TestObjectSub(13, "foo", true, -1);
        }

        @Override
        public Stream<TestObjectSub> equalObjects() {
            return Stream.of(
                    new TestObjectSub(13, "foo", true, -1),
                    new TestObjectSub(13, "foo", true, 1)
            );
        }

        @Override
        public Stream<TestObjectSub> unequalObjects() {
            return Stream.of(
                    new TestObjectSub(14, "foo", true, -1),
                    new TestObjectSub(13, "bar", true, -1),
                    new TestObjectSub(13, null, true, -1),
                    new TestObjectSub(13, "foo", false, -1)
            );
        }
    }

    @Nested
    @DisplayName("null String")
    class NullString implements EqualsAndHashCodeTests<TestObjectSub> {

        @Override
        public TestObjectSub object() {
            return new TestObjectSub(13, null, true, -1);
        }

        @Override
        public Stream<TestObjectSub> equalObjects() {
            return Stream.of(
                    new TestObjectSub(13, null, true, -1),
                    new TestObjectSub(13, null, true, 1)
            );
        }

        @Override
        public Stream<TestObjectSub> unequalObjects() {
            return Stream.of(
                    new TestObjectSub(14, null, true, -1),
                    new TestObjectSub(13, "foo", true, -1),
                    new TestObjectSub(13, null, false, -1)
            );
        }
    }
}
