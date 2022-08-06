/*
 * TestResourceExtension.java
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

package com.github.robtimus.junit.support.extension.testresource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import com.github.robtimus.io.function.IOBiFunction;
import com.github.robtimus.junit.support.extension.AbstractInjectExtension;
import com.github.robtimus.junit.support.extension.InjectionTarget;

@SuppressWarnings("nls")
class TestResourceExtension extends AbstractInjectExtension<TestResource> {

    private static final Map<Class<?>, IOBiFunction<InputStream, String, ?>> RESOURCE_CONVERTERS;
    private static final Set<Class<?>> CACHEABLE_RESOURCE_TYPES;

    private static final Namespace NAMESPACE = Namespace.create(TestResourceExtension.class);

    static {
        Map<Class<?>, IOBiFunction<InputStream, String, ?>> resourceConverters = new HashMap<>();
        resourceConverters.put(String.class, TestResourceExtension::readContentAsString);
        resourceConverters.put(CharSequence.class, TestResourceExtension::readContentAsStringBuilder);
        resourceConverters.put(StringBuilder.class, TestResourceExtension::readContentAsStringBuilder);
        resourceConverters.put(byte[].class, (inputStream, charset) -> readContentAsBytes(inputStream));
        RESOURCE_CONVERTERS = Collections.unmodifiableMap(resourceConverters);

        Set<Class<?>> cacheResourceTypes = new HashSet<>();
        cacheResourceTypes.add(String.class);
        CACHEABLE_RESOURCE_TYPES = Collections.unmodifiableSet(cacheResourceTypes);
    }

    TestResourceExtension() {
        super(TestResource.class);
    }

    @Override
    protected Optional<JUnitException> validateTarget(InjectionTarget target, TestResource resource, ExtensionContext context) {
        if (target.isAnnotated(LoadWith.class)) {
            // don't validate the factory method yet
            return Optional.empty();
        }

        Class<?> targetType = target.type();
        return RESOURCE_CONVERTERS.containsKey(targetType)
                ? Optional.empty()
                : Optional.of(target.createException("Target type not supported: " + targetType));
    }

    @Override
    protected Object resolveValue(TestResource resource, InjectionTarget target, ExtensionContext context) {
        LoadWith loadWith = target.findAnnotation(LoadWith.class).orElse(null);
        if (loadWith != null) {
            String methodName = loadWith.value();
            return methodName.contains("#")
                    ? resolveValue(resource, null, methodName, target, context)
                    : resolveValue(resource, context.getRequiredTestClass(), methodName, target, context);
        }

        Class<?> targetType = target.type();
        if (CACHEABLE_RESOURCE_TYPES.contains(targetType)) {
            Namespace namespace = NAMESPACE.append(target.declaringClass(), resource.value());
            return context.getStore(namespace).getOrComputeIfAbsent(resource.charset(), k -> resolveValue(resource, target));
        }
        return resolveValue(resource, target);
    }

    private Object resolveValue(TestResource resource, Class<?> factoryClass, String methodName, InjectionTarget target,
            ExtensionContext context) {

        String fullyQualifiedMethodName = factoryClass == null || methodName.isEmpty() ? methodName : factoryClass.getName() + "#" + methodName;

        String[] methodParts = ReflectionUtils.parseFullyQualifiedMethodName(fullyQualifiedMethodName);

        methodName = methodParts[1];
        String methodParameters = methodParts[2];

        if (factoryClass == null) {
            factoryClass = loadRequiredClass(methodParts[0]);
        }

        Method factoryMethod;

        switch (methodParameters) {
            case "":
                return resolveValueFromReaderOrInputStream(resource, factoryClass, methodName, target, context);
            case "InputStream":
                factoryMethod = ReflectionUtils.getRequiredMethod(factoryClass, methodName, InputStream.class);
                return resolveValueFromInputStream(resource, factoryMethod, target, context);
            case "Reader":
                factoryMethod = ReflectionUtils.getRequiredMethod(factoryClass, methodName, Reader.class);
                return resolveValueFromReader(resource, factoryMethod, target, context);
            default:
                throw new PreconditionViolationException(
                        String.format("factory method [%s] has unsupported formal parameters", fullyQualifiedMethodName));
        }
    }

    private Object resolveValueFromReaderOrInputStream(TestResource resource, Class<?> factoryClass, String methodName, InjectionTarget target,
            ExtensionContext context) {

        Method factoryMethod = ReflectionSupport.findMethod(factoryClass, methodName, Reader.class).orElse(null);
        if (factoryMethod != null) {
            return resolveValueFromReader(resource, factoryMethod, target, context);
        }
        factoryMethod = ReflectionUtils.getRequiredMethod(factoryClass, methodName, InputStream.class);
        return resolveValueFromInputStream(resource, factoryMethod, target, context);
    }

    private Object resolveValueFromInputStream(TestResource resource, Method factoryMethod, InjectionTarget target, ExtensionContext context) {
        try (InputStream inputStream = target.declaringClass().getResourceAsStream(resource.value())) {
            validateResource(resource, target, inputStream);

            Object testInstance = context.getTestInstance().orElse(null);
            return ReflectionSupport.invokeMethod(factoryMethod, testInstance, inputStream);
        } catch (IOException e) {
            ExceptionUtils.throwAsUncheckedException(e);
            return null;
        }
    }

    private Object resolveValueFromReader(TestResource resource, Method factoryMethod, InjectionTarget target, ExtensionContext context) {
        try (InputStream inputStream = target.declaringClass().getResourceAsStream(resource.value())) {
            validateResource(resource, target, inputStream);

            try (Reader reader = new InputStreamReader(inputStream, resource.charset())) {
                Object testInstance = context.getTestInstance().orElse(null);
                return ReflectionSupport.invokeMethod(factoryMethod, testInstance, reader);
            }
        } catch (IOException e) {
            ExceptionUtils.throwAsUncheckedException(e);
            return null;
        }
    }

    private Class<?> loadRequiredClass(String className) {
        return ReflectionSupport.tryToLoadClass(className)
                .getOrThrow(cause -> new JUnitException(String.format("Could not load class [%s]", className), cause));
    }

    private Object resolveValue(TestResource resource, InjectionTarget target) {
        Class<?> targetType = target.type();
        try (InputStream inputStream = target.declaringClass().getResourceAsStream(resource.value())) {
            validateResource(resource, target, inputStream);

            IOBiFunction<InputStream, String, ?> resourceConverter = RESOURCE_CONVERTERS.get(targetType);

            return resourceConverter.apply(inputStream, resource.charset());
        } catch (IOException e) {
            ExceptionUtils.throwAsUncheckedException(e);
            return null;
        }
    }

    private void validateResource(TestResource resource, InjectionTarget target, InputStream inputStream) {
        if (inputStream == null) {
            throw target.createException("Resource not found: " + resource.value());
        }
    }

    private static String readContentAsString(InputStream inputStream, String charset) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, charset)) {
            return TestResourceLoaders.toString(reader);
        }
    }

    private static StringBuilder readContentAsStringBuilder(InputStream inputStream, String charset) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, charset)) {
            return TestResourceLoaders.toStringBuilder(reader);
        }
    }

    private static byte[] readContentAsBytes(InputStream inputStream) throws IOException {
        return TestResourceLoaders.toBytes(inputStream);
    }
}
