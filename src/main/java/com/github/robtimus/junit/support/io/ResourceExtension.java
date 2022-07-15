/*
 * ResourceExtension.java
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
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import com.github.robtimus.io.function.IOBiFunction;

class ResourceExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

    private static final Map<Class<?>, IOBiFunction<InputStream, String, ?>> RESOURCE_CONVERTERS = createResourceConverters();

    private static Map<Class<?>, IOBiFunction<InputStream, String, ?>> createResourceConverters() {
        Map<Class<?>, IOBiFunction<InputStream, String, ?>> converters = new HashMap<>();
        converters.put(String.class, ResourceExtension::readContentAsString);
        converters.put(CharSequence.class, ResourceExtension::readContentAsStringBuilder);
        converters.put(StringBuilder.class, ResourceExtension::readContentAsStringBuilder);
        converters.put(byte[].class, (inputStream, charset) -> readContentAsBytes(inputStream));
        return Collections.unmodifiableMap(converters);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        injectFields(null, context.getRequiredTestClass(), ReflectionUtils::isStatic);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        context.getRequiredTestInstances().getAllInstances()
                .forEach(instance -> injectFields(instance, instance.getClass(), ReflectionUtils::isNotStatic));
    }

    private void injectFields(Object testInstance, Class<?> testClass, Predicate<Field> predicate) {
        findAnnotatedFields(testClass, Resource.class, predicate).forEach(field -> setResource(field, testInstance, testClass));
    }

    private void setResource(Field field, Object testInstance, Class<?> testClass) {
        Resource resource = field.getAnnotation(Resource.class);

        Class<?> fieldType = field.getType();
        IOBiFunction<InputStream, String, ?> resourceConverter = RESOURCE_CONVERTERS.get(fieldType);
        if (resourceConverter == null) {
            throw new ExtensionConfigurationException("Field type not supported: " + fieldType); //$NON-NLS-1$
        }

        Object resourceValue = resolveResource(resource, testClass, fieldType, resourceConverter,
                ExtensionConfigurationException::new, ExtensionConfigurationException::new);

        try {
            makeAccessible(field).set(testInstance, resourceValue);
        } catch (Exception t) {
            ExceptionUtils.throwAsUncheckedException(t);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Resource resource = parameterContext.findAnnotation(Resource.class).orElse(null);
        return resource != null && isValidParameterType(parameterContext.getParameter().getType());
    }

    private boolean isValidParameterType(Class<?> parameterType) {
        return RESOURCE_CONVERTERS.containsKey(parameterType);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        // supportsParameter ensures that @Resource is present and the parameter type is supported

        Resource resource = parameterContext.findAnnotation(Resource.class).orElseThrow(IllegalStateException::new);

        Class<?> parameterType = parameterContext.getParameter().getType();
        IOBiFunction<InputStream, String, ?> resourceConverter = RESOURCE_CONVERTERS.get(parameterType);

        Class<?> declaringClass = parameterContext.getDeclaringExecutable().getDeclaringClass();

        return resolveResource(resource, declaringClass, parameterType, resourceConverter,
                ParameterResolutionException::new, ParameterResolutionException::new);
    }

    @SuppressWarnings("resource")
    private Object resolveResource(Resource resource, Class<?> declaringClass, Class<?> resourceType,
            IOBiFunction<InputStream, String, ?> resourceConverter,
            Function<String, RuntimeException> exceptionWithMessage,
            BiFunction<String, Throwable, RuntimeException> exceptionWithMessageAndCause) {

        InputStream inputStream = declaringClass.getResourceAsStream(resource.value());
        if (inputStream == null) {
            throw exceptionWithMessage.apply("Resource not found: " + resource.value()); //$NON-NLS-1$
        }
        try {
            return resourceConverter.apply(inputStream, resource.charset());
        } catch (IOException e) {
            throw exceptionWithMessageAndCause.apply("Could not convert resource to " + resourceType, e); //$NON-NLS-1$
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
