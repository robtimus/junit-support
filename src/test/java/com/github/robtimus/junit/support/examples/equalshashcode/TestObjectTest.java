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
class TestObjectTest {

    @Nested
    @DisplayName("non-null String")
    class NonNullString implements EqualsAndHashCodeTests<TestObject> {

        @Override
        public TestObject object() {
            return new TestObject(13, "foo", true);
        }

        @Override
        public Stream<TestObject> unequalObjects() {
            return Stream.of(
                    new TestObject(14, "foo", true),
                    new TestObject(13, "bar", true),
                    new TestObject(13, null, true),
                    new TestObject(13, "foo", false),
                    new TestObjectSub(13, "foo", true, -1)
            );
        }

        @Override
        public Stream<TestObject> unequalHashCodeObjects() {
            return unequalObjects()
                    // TestObjectSub doesn't redefine equals or hashCode, so the instance with the same i, s and b values has the same hashCode
                    .filter(o -> !(o instanceof TestObjectSub));
        }
    }

    @Nested
    @DisplayName("null String")
    class NullString implements EqualsAndHashCodeTests<TestObject> {

        @Override
        public TestObject object() {
            return new TestObject(13, null, true);
        }

        @Override
        public Stream<TestObject> unequalObjects() {
            return Stream.of(
                    new TestObject(14, null, true),
                    new TestObject(13, "foo", true),
                    new TestObject(13, null, false),
                    new TestObjectSub(13, null, true, -1)
            );
        }

        @Override
        public Stream<TestObject> unequalHashCodeObjects() {
            return unequalObjects()
                    // TestObjectSub doesn't redefine equals or hashCode, so the instance with the same i, s and b values has the same hashCode
                    .filter(o -> !(o instanceof TestObjectSub));
        }
    }
}
