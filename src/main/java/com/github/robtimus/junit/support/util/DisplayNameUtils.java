/*
 * DisplayNameUtils.java
 * Copyright 2021 Rob Spoor
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

package com.github.robtimus.junit.support.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;

/**
 * A utility class for creating display names for {@link DynamicTest} or {@link DynamicContainer}.
 *
 * @author Rob Spoor
 * @since 1.1
 */
public final class DisplayNameUtils {

    private DisplayNameUtils() {
    }

    /**
     * Returns a display name for a method.
     *
     * @param method The method.
     * @return A display name for the given method.
     */
    public static String getMethodDisplayName(Method method) {
        return getMethodDisplayName(method.getName(), method.getParameterTypes());
    }

    /**
     * Returns a display name for a method.
     *
     * @param name The method name.
     * @param parameterTypes The method parameter types.
     * @return A display name for the method with the given name and parameter types.
     */
    @SuppressWarnings("nls")
    public static String getMethodDisplayName(String name, Class<?>[] parameterTypes) {
        return Arrays.stream(parameterTypes)
                .map(DisplayNameUtils::getTypeName)
                .collect(Collectors.joining(", ", name + "(", ")"));
    }

    @SuppressWarnings("nls")
    private static String getTypeName(Class<?> parameterType) {
        if (parameterType.isArray()) {
            return getTypeName(parameterType.getComponentType()) + "[]";
        }
        return parameterType.getSimpleName();
    }
}
