/*
 * DelegateTests.java
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

package com.github.robtimus.junit.support;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import com.github.robtimus.junit.support.reflection.MethodFinder;

/**
 * Base interface for testing that methods delegate to another object of the same type.
 *
 * @author Rob Spoor
 * @param <T> The type of object to test.
 */
public interface DelegateTests<T> {

    /**
     * Returns the type to delegate to.
     *
     * @return The type to delegate to.
     */
    Class<T> delegateType();

    /**
     * Creates the object to test.
     *
     * @param delegate The delegate to test against.
     * @return The created object.
     */
    T wrap(T delegate);

    /**
     * Returns an object that will return all methods to test.
     *
     * @return An object that will return all methods to test.
     */
    MethodFinder methods();

    /**
     * For each method returned by the object returned by {@link #methods()}, test that the result of {@link #wrap(Object)} delegates to its argument.
     *
     * @return A stream with the tests, one per method.
     */
    @TestFactory
    @DisplayName("delegates")
    @SuppressWarnings("nls")
    default Stream<DynamicTest> testDelegates() {
        Class<T> delegateType = delegateType();
        return methods().findMethods(delegateType)
                .distinct()
                .map(i -> {
                    Method method = i.getMethod();
                    String methodDisplayName = Arrays.stream(method.getParameterTypes())
                            .map(Class::getTypeName)
                            .collect(Collectors.joining(", ", method.getName() + "(", ")"));

                    return dynamicTest(methodDisplayName, () -> {
                        T delegate = mock(delegateType);
                        i.invoke(wrap(delegate));
                        i.invoke(verify(delegate));
                    });
                });
    }
}
