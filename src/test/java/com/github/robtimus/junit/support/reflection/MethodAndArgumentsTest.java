/*
 * MethodAndArgumentsTest.java
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

package com.github.robtimus.junit.support.reflection;

import static com.github.robtimus.junit.support.reflection.MethodAndArguments.intParameter;
import static com.github.robtimus.junit.support.reflection.MethodAndArguments.methodWithArguments;
import static com.github.robtimus.junit.support.reflection.MethodAndArguments.methodWithParameters;
import static com.github.robtimus.junit.support.reflection.MethodAndArguments.parameter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

@SuppressWarnings("nls")
class MethodAndArgumentsTest {

    @Test
    @DisplayName("with parameters")
    void testWithParameters() {
        MethodAndArguments invokableMethod = methodWithParameters(List.class, "add", intParameter(2), parameter(Object.class, "foo"));

        @SuppressWarnings("unchecked")
        List<String> list = mock(List.class);

        invokableMethod.invoke(list);

        verify(list).add(2, "foo");
    }

    @Test
    @DisplayName("with incompatible parameters during creation")
    void testWithIncompatibleParametersDuringCreation() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> methodWithParameters(List.class, "add", intParameter(2), parameter("foo")));
        assertThat(error.getCause(), instanceOf(NoSuchMethodException.class));
    }

    @Test
    @DisplayName("with incompatible parameters during invoke")
    void testWithIncompatibleParametersDuringInvoke() {
        Method method = assertDoesNotThrow(() -> List.class.getMethod("add", int.class, Object.class));
        MethodAndArguments invokableMethod = methodWithArguments(method, "foo", "bar");

        @SuppressWarnings("unchecked")
        List<String> list = mock(List.class);

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> invokableMethod.invoke(list));
        assertThat(error.getCause(), instanceOf(IllegalArgumentException.class));
    }
}
