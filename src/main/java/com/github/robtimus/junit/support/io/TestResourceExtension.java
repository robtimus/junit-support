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

package com.github.robtimus.junit.support.io;

import static com.github.robtimus.junit.support.io.IOUtils.readAll;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import com.github.robtimus.io.function.IOBiFunction;
import com.github.robtimus.junit.support.extension.AbstractInjectExtension;
import com.github.robtimus.junit.support.extension.InjectionTarget;

class TestResourceExtension extends AbstractInjectExtension<TestResource> {

    private static final Map<Class<?>, IOBiFunction<InputStream, String, ?>> RESOURCE_CONVERTERS = createResourceConverters();

    private static Map<Class<?>, IOBiFunction<InputStream, String, ?>> createResourceConverters() {
        Map<Class<?>, IOBiFunction<InputStream, String, ?>> converters = new HashMap<>();
        converters.put(String.class, TestResourceExtension::readContentAsString);
        converters.put(CharSequence.class, TestResourceExtension::readContentAsStringBuilder);
        converters.put(StringBuilder.class, TestResourceExtension::readContentAsStringBuilder);
        converters.put(byte[].class, (inputStream, charset) -> readContentAsBytes(inputStream));
        return Collections.unmodifiableMap(converters);
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
    @SuppressWarnings("resource")
    protected Object resolveValue(TestResource resource, InjectionTarget target, ExtensionContext context) {
        InputStream inputStream = target.declaringClass().getResourceAsStream(resource.value());
        if (inputStream == null) {
            throw target.createException("Resource not found: " + resource.value()); //$NON-NLS-1$
        }

        Class<?> targetType = target.type();
        IOBiFunction<InputStream, String, ?> resourceConverter = RESOURCE_CONVERTERS.get(targetType);
        try {
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
            return readAll(reader);
        }
    }

    private static byte[] readContentAsBytes(InputStream inputStream) throws IOException {
        try (InputStream is = inputStream) {
            return readAll(inputStream);
        }
    }
}
