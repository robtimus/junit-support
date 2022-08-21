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
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.ReflectionSupport;
import com.github.robtimus.junit.support.extension.AbstractInjectExtension;
import com.github.robtimus.junit.support.extension.InjectionTarget;
import com.github.robtimus.junit.support.extension.MethodLookup;

@SuppressWarnings("nls")
class TestResourceExtension extends AbstractInjectExtension<TestResource> {

    private static final Map<Class<?>, ResourceConverter> RESOURCE_CONVERTERS;
    private static final Map<String, String> EOL_VALUES;
    private static final Map<String, Supplier<String>> ENCODING_LOOKUPS;

    private static final MethodLookup LOAD_AS_LOOKUP = MethodLookup.withParameterTypes(Reader.class, InjectionTarget.class)
            .orParameterTypes(Reader.class)
            .orParameterTypes(InputStream.class, InjectionTarget.class)
            .orParameterTypes(InputStream.class);

    static {
        Map<Class<?>, ResourceConverter> resourceConverters = new HashMap<>();
        resourceConverters.put(String.class, TestResourceExtension::readContentAsString);
        resourceConverters.put(CharSequence.class, TestResourceExtension::readContentAsCharSequence);
        resourceConverters.put(StringBuilder.class, TestResourceExtension::readContentAsStringBuilder);
        resourceConverters.put(byte[].class, (inputStream, target, context) -> readContentAsBytes(inputStream, target));
        RESOURCE_CONVERTERS = Collections.unmodifiableMap(resourceConverters);

        Map<String, String> eolValues = new HashMap<>();
        eolValues.put("LF", EOL.LF);
        eolValues.put("CR", EOL.CR);
        eolValues.put("CRLF", EOL.CRLF);
        eolValues.put("SYSTEM", System.lineSeparator());
        eolValues.put("ORIGINAL", EOL.ORIGINAL);
        eolValues.put("NONE", EOL.NONE);
        EOL_VALUES = Collections.unmodifiableMap(eolValues);

        Map<String, Supplier<String>> encodingLookups = new HashMap<>();
        encodingLookups.put("DEFAULT", () -> Charset.defaultCharset().name());
        encodingLookups.put("SYSTEM", TestResourceExtension::lookupSystemEncoding);
        encodingLookups.put("NATIVE", TestResourceExtension::lookupNativeEncoding);
        ENCODING_LOOKUPS = Collections.unmodifiableMap(encodingLookups);
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
    protected Object resolveValue(TestResource resource, InjectionTarget target, ExtensionContext context) throws IOException {
        LoadWith loadWith = target.findAnnotation(LoadWith.class).orElse(null);
        if (loadWith != null) {
            validateNoEOL(target, "@EOL not allowed in combination with @LoadWith");

            MethodLookup.Result lookupResult = LOAD_AS_LOOKUP.find(loadWith.value(), context);
            switch (lookupResult.index()) {
                case 0: // Reader + InjectionTarget
                case 1: // Reader
                    return resolveValueFromReader(resource, lookupResult.method(), target, context);
                default: // InputStream + InjectionTarget, InputStream
                    return resolveValueFromInputStream(resource, lookupResult.method(), target, context);
            }
        }

        return resolveValueFromInputStream(resource, target, context);
    }

    private Object resolveValueFromInputStream(TestResource resource, Method factoryMethod, InjectionTarget target, ExtensionContext context)
            throws IOException {

        try (InputStream inputStream = target.declaringClass().getResourceAsStream(resource.value())) {
            validateResource(resource, target, inputStream);

            validateNoEncoding(target, "@Encoding not allowed when using InputStream");

            Object testInstance = context.getTestInstance().orElse(null);
            return factoryMethod.getParameterCount() == 1
                    ? ReflectionSupport.invokeMethod(factoryMethod, testInstance, inputStream)
                    : ReflectionSupport.invokeMethod(factoryMethod, testInstance, inputStream, target);
        }
    }

    private Object resolveValueFromReader(TestResource resource, Method factoryMethod, InjectionTarget target, ExtensionContext context)
            throws IOException {

        try (InputStream inputStream = target.declaringClass().getResourceAsStream(resource.value())) {
            validateResource(resource, target, inputStream);

            String encoding = lookupEncoding(target, context);
            try (Reader reader = new InputStreamReader(inputStream, encoding)) {
                Object testInstance = context.getTestInstance().orElse(null);
                return factoryMethod.getParameterCount() == 1
                        ? ReflectionSupport.invokeMethod(factoryMethod, testInstance, reader)
                        : ReflectionSupport.invokeMethod(factoryMethod, testInstance, reader, target);
            }
        }
    }

    private Object resolveValueFromInputStream(TestResource resource, InjectionTarget target, ExtensionContext context) throws IOException {
        Class<?> targetType = target.type();
        try (InputStream inputStream = target.declaringClass().getResourceAsStream(resource.value())) {
            validateResource(resource, target, inputStream);

            ResourceConverter resourceConverter = RESOURCE_CONVERTERS.get(targetType);

            return resourceConverter.convert(inputStream, target, context);
        }
    }

    private void validateResource(TestResource resource, InjectionTarget target, InputStream inputStream) {
        if (inputStream == null) {
            throw target.createException("Resource not found: " + resource.value());
        }
    }

    private static String readContentAsString(InputStream inputStream, InjectionTarget target, ExtensionContext context) throws IOException {

        String lineSeparator = lookupLineSeparator(target, context);

        String encoding = lookupEncoding(target, context);
        try (Reader reader = new InputStreamReader(inputStream, encoding)) {
            return EOL.ORIGINAL.equals(lineSeparator)
                    ? TestResourceLoaders.toString(reader)
                    : TestResourceLoaders.toString(reader, lineSeparator);
        }
    }

    private static CharSequence readContentAsCharSequence(InputStream inputStream, InjectionTarget target, ExtensionContext context)
            throws IOException {

        String lineSeparator = lookupLineSeparator(target, context);

        String encoding = lookupEncoding(target, context);
        try (Reader reader = new InputStreamReader(inputStream, encoding)) {
            return EOL.ORIGINAL.equals(lineSeparator)
                    ? TestResourceLoaders.toCharSequence(reader)
                    : TestResourceLoaders.toCharSequence(reader, lineSeparator);
        }
    }

    private static StringBuilder readContentAsStringBuilder(InputStream inputStream, InjectionTarget target, ExtensionContext context)
            throws IOException {

        String lineSeparator = lookupLineSeparator(target, context);

        String encoding = lookupEncoding(target, context);
        try (Reader reader = new InputStreamReader(inputStream, encoding)) {
            return EOL.ORIGINAL.equals(lineSeparator)
                    ? TestResourceLoaders.toStringBuilder(reader)
                    : TestResourceLoaders.toStringBuilder(reader, lineSeparator);
        }
    }

    private static byte[] readContentAsBytes(InputStream inputStream, InjectionTarget target) throws IOException {
        validateNoEOL(target, "@EOL not allowed for byte[]");
        validateNoEncoding(target, "@Encoding not allowed for byte[]");

        return TestResourceLoaders.toBytes(inputStream);
    }

    static String lookupLineSeparator(InjectionTarget target, ExtensionContext context) {
        EOL eol = target.findAnnotation(EOL.class, true).orElse(null);
        if (eol == null) {
            return lookupDefaultLineSeparator(context);
        }
        // Return LF, CR, CRLF, NONE and ORIGINAL as-is
        return EOL.SYSTEM.equals(eol.value()) ? System.lineSeparator() : eol.value();
    }

    private static String lookupDefaultLineSeparator(ExtensionContext context) {
        String eolParameter = context.getConfigurationParameter(EOL.DEFAULT_EOL_PROPERTY_NAME).orElse("ORIGINAL");
        return EOL_VALUES.getOrDefault(eolParameter, eolParameter);
    }

    static String lookupEncoding(InjectionTarget target, ExtensionContext context) {
        Encoding encoding = target.findAnnotation(Encoding.class, true).orElse(null);
        if (encoding == null) {
            return lookupDefaultEncoding(context);
        }
        String encodingValue = encoding.value();
        switch (encodingValue) {
            case Encoding.DEFAULT:
                return Charset.defaultCharset().name();
            case Encoding.SYSTEM:
                return lookupSystemEncoding();
            case Encoding.NATIVE:
                return lookupNativeEncoding();
            default:
                return encodingValue;
        }
    }

    private static String lookupDefaultEncoding(ExtensionContext context) {
        String encodingParameter = context.getConfigurationParameter(Encoding.DEFAULT_ENCODING_PROPERTY_NAME).orElse("UTF-8");
        Supplier<String> encodingLookup = ENCODING_LOOKUPS.get(encodingParameter);
        return encodingLookup != null ? encodingLookup.get() : encodingParameter;
    }

    private static String lookupSystemEncoding() {
        String encoding = System.getProperty("file.encoding");
        if (encoding == null) {
            throw new PreconditionViolationException("System property [file.encoding] not available");
        }
        return encoding;
    }

    private static String lookupNativeEncoding() {
        String encoding = System.getProperty("native.encoding");
        if (encoding == null) {
            throw new PreconditionViolationException("System property [native.encoding] not available");
        }
        return encoding;
    }

    private static void validateNoEOL(InjectionTarget target, String message) {
        EOL eol = target.findAnnotation(EOL.class).orElse(null);
        if (eol != null) {
            throw new PreconditionViolationException(message);
        }
    }

    private static void validateNoEncoding(InjectionTarget target, String message) {
        Encoding encoding = target.findAnnotation(Encoding.class).orElse(null);
        if (encoding != null) {
            throw new PreconditionViolationException(message);
        }
    }

    private interface ResourceConverter {

        Object convert(InputStream inputStream, InjectionTarget target, ExtensionContext context) throws IOException;
    }
}
