/*
 * SubClassTest.java
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

package com.github.robtimus.junit.support.examples.covariantreturn;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import com.github.robtimus.junit.support.CovariantReturnTests;

class SubClassTest {

    @Nested
    @DisplayName("with static methods")
    class WithStaticMethods implements CovariantReturnTests<SubClass> {

        @Override
        public Class<SubClass> objectType() {
            return SubClass.class;
        }
    }

    @Nested
    @DisplayName("without static methods")
    class WithoutStaticMethods implements CovariantReturnTests<SubClass> {
        @Override
        public Class<SubClass> objectType() {
            return SubClass.class;
        }

        @Override
        public Stream<Method> methods() {
            return CovariantReturnTests.super.methods()
                    .filter(m -> !Modifier.isStatic(m.getModifiers()));
        }
    }
}
