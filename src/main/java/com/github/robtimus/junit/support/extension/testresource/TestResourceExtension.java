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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.JUnitException;
import com.github.robtimus.io.function.IOBiFunction;
import com.github.robtimus.junit.support.extension.AbstractInjectExtension;
import com.github.robtimus.junit.support.extension.InjectionTarget;

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
        Class<?> targetType = target.type();
        return RESOURCE_CONVERTERS.containsKey(targetType)
                ? Optional.empty()
                : Optional.of(target.createException("Target type not supported: " + targetType)); //$NON-NLS-1$
    }

    @Override
    protected Object resolveValue(TestResource resource, InjectionTarget target, ExtensionContext context) {
        Class<?> targetType = target.type();
        if (CACHEABLE_RESOURCE_TYPES.contains(targetType)) {
            Namespace namespace = NAMESPACE.append(target.declaringClass());
            return context.getStore(namespace).getOrComputeIfAbsent(resource.value(), k -> resolveValue(resource, target));
        }
        return resolveValue(resource, target);
    }

    private Object resolveValue(TestResource resource, InjectionTarget target) {
        Class<?> targetType = target.type();
        try (InputStream inputStream = target.declaringClass().getResourceAsStream(resource.value())) {
            if (inputStream == null) {
                throw target.createException("Resource not found: " + resource.value()); //$NON-NLS-1$
            }

            IOBiFunction<InputStream, String, ?> resourceConverter = RESOURCE_CONVERTERS.get(targetType);

            return resourceConverter.apply(inputStream, resource.charset());
        } catch (IOException e) {
            throw target.createException("Could not convert resource to " + targetType, e); //$NON-NLS-1$
        }
    }

    private static String readContentAsString(InputStream inputStream, String charset) throws IOException {
        return readContentAsStringBuilder(inputStream, charset).toString();
    }

    private static StringBuilder readContentAsStringBuilder(InputStream inputStream, String charset) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, charset)) {
            StringBuilder sb = new StringBuilder();

            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
            return sb;
        }
    }

    private static byte[] readContentAsBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }
}
