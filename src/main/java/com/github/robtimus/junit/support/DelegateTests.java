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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import com.github.robtimus.junit.support.reflection.MethodProvider;

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
     * Returns a provider for the methods to test.
     *
     * @return A provider that will return all methods to test.
     */
    MethodProvider methods();

    /**
     * For each method returned by the object returned by {@link #methods()}, test that the result of {@link #wrap(Object)} delegates to its argument.
     *
     * @return A stream with the tests, one per method.
     */
    @TestFactory
    @DisplayName("delegates")
    default Stream<DynamicTest> testDelegates() {
        Class<T> delegateType = delegateType();
        return methods()
                .without(m -> Modifier.isStatic(m.getModifiers()))
                .methods(delegateType)
                .distinct()
                .map(o -> o.asTest(m -> {
                    T delegate = mock(delegateType);
                    m.invoke(wrap(delegate));
                    m.invoke(verify(delegate));
                }));
    }
}
