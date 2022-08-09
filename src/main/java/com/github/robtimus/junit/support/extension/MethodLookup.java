/*
 * MethodLookup.java
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

package com.github.robtimus.junit.support.extension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/**
 * A class to help find methods based on annotations or other method references. These lookups work similar to the lookups used for
 * {@link MethodSource}. In addition, it supports multiple sets of supported parameter types. This can be used to validate method references if
 * the parameter types are given, or perform multiple lookups otherwise.
 * <p>
 * Instances of this class are not thread safe when configuring them using {@link #orParameterTypes(Class...)}. Once an instance is configured, it's
 * safe to call {@link #find(String, ExtensionContext)} from different threads concurrently.
 *
 * @author Rob Spoor
 */
@SuppressWarnings("nls")
public final class MethodLookup {

    static final Pattern METHOD_REFERENCE_PATTERN = createMethodReferencePattern();

    private final List<Class<?>[]> parameterTypeCombinations;
    private final StringJoiner combinationRepresentations;

    private MethodLookup() {
        parameterTypeCombinations = new ArrayList<>();
        combinationRepresentations = new StringJoiner(", ", "[", "]");
    }

    /**
     * Creates a new method lookup instance.
     *
     * @param parameterTypes The preferred set of parameter types.
     * @return The created method lookup instance.
     */
    public static MethodLookup withParameterTypes(Class<?>... parameterTypes) {
        MethodLookup lookup = new MethodLookup();
        lookup.addParameterTypes(parameterTypes);
        return lookup;
    }

    /**
     * Adds another allowed set of parameter types.
     *
     * @param parameterTypes The set of parameter types.
     * @return This object.
     */
    public MethodLookup orParameterTypes(Class<?>... parameterTypes) {
        addParameterTypes(parameterTypes);
        return this;
    }

    private void addParameterTypes(Class<?>... parameterTypes) {
        combinationRepresentations.add(toString(parameterTypes));
        parameterTypeCombinations.add(parameterTypes.clone());
    }

    private String toString(Class<?>... classes) {
        return Arrays.stream(classes)
                .map(this::toString)
                .collect(Collectors.joining(", ", "(", ")"));
    }

    private String toString(Class<?> clazz) {
        String canonicalName = clazz.getCanonicalName();
        return canonicalName != null ? canonicalName : clazz.getName();
    }

    /**
     * Tries to find a method. If the method cannot be found, an exception is thrown.
     * <p>
     * The method reference can be defined in a number of ways:
     * <ul>
     * <li><em>{@code methodName}</em> for a method in the test class itself.
     *     The parameter types for the method are those used to create this instance; the first match will be returned.</li>
     * <li><em>{@code className}</em>{@code #}<em>{@code methodName}</em> for a method in the defined class.
     *     The parameter types for the method are those used to create this instance; the first match will be returned.</li>
     * <li><em>{@code methodName}</em>(<em>parameterTypes</em>) for a method in the test class itself.
     *     The parameter types are used as-is, but these must match one of the sets of parameter types used to create this instance.</li>
     * <li><em>{@code className}</em>{@code #}<em>{@code methodName}</em>(<em>parameterTypes</em>) for a method in the defined class.
     *     The parameter types are used as-is, but these must match one of the sets of parameter types used to create this instance.</li>
     * </ul>
     *
     * @param methodReference A reference to the method to find.
     * @param context The current extension context; never {@code null}.
     * @return A result describing the method that was found.
     */
    public Result find(String methodReference, ExtensionContext context) {
        if (isBlank(methodReference)) {
            throw new PreconditionViolationException("methodReference must not be null or blank");
        }

        Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(methodReference);
        if (!matcher.matches()) {
            throw new PreconditionViolationException(String.format("[%s] is not a valid method reference: "
                    + "it must be the method name, optionally preceded by a fully qualified class name followed by a '#', "
                    + "and optionally followed by a parameter list enclosed in parentheses.",
                    methodReference));
        }

        String className = className(matcher);
        String methodName = methodName(matcher);
        String methodArguments = methodArguments(matcher);

        Class<?> factoryClass = className == null
                ? context.getRequiredTestClass()
                : ReflectionSupport.tryToLoadClass(className)
                        .getOrThrow(cause -> new JUnitException(String.format("Could not load class [%s]", className), cause));

        return methodArguments == null
                ? findUsingParameterTypes(factoryClass, methodName)
                : findUsingMethodArguments(factoryClass, methodName, methodArguments);
    }

    private Result findUsingParameterTypes(Class<?> factoryClass, String methodName) {
        // Try all combinations until a match is found
        for (ListIterator<Class<?>[]> iterator = parameterTypeCombinations.listIterator(); iterator.hasNext(); ) {
            int index = iterator.nextIndex();
            Class<?>[] parameterTypes = iterator.next();
            Method match = ReflectionSupport.findMethod(factoryClass, methodName, parameterTypes).orElse(null);
            if (match != null) {
                return new Result(match, index);
            }
        }

        // No match
        throw new PreconditionViolationException(String.format("Could not find method [%s] in class [%s] with a parameter combination in %s",
                methodName, factoryClass.getName(), combinationRepresentations));
    }

    private Result findUsingMethodArguments(Class<?> factoryClass, String methodName, String methodArguments) {
        Method method = ReflectionSupport.findMethod(factoryClass, methodName, methodArguments)
                .orElseThrow(() -> new PreconditionViolationException(String.format("Could not find method [%s(%s)] in class [%s]",
                        methodName, methodArguments, factoryClass.getName())));

        // don't just return the method; check that it's one of the expected ones
        for (ListIterator<Class<?>[]> iterator = parameterTypeCombinations.listIterator(); iterator.hasNext(); ) {
            int index = iterator.nextIndex();
            Class<?>[] parameterTypes = iterator.next();
            Method match = ReflectionSupport.findMethod(factoryClass, methodName, parameterTypes).orElse(null);
            if (match != null && method.equals(match)) {
                return new Result(method, index);
            }
        }

        // Although the method exists, it is not supported
        throw new PreconditionViolationException(String.format("Method [%s(%s)] in class [%s] does not have a parameter combination in %s",
                methodName, methodArguments, factoryClass.getName(), combinationRepresentations));
    }

    /**
     * Tries to find a method. If the method cannot be found, an exception is thrown.
     * <p>
     * The method reference can be defined in a number of ways:
     * <ul>
     * <li><em>{@code methodName}</em> for a method in the test class itself.
     *     If multiple methods are found with the given name, an exception is thrown.</li>
     * <li><em>{@code className}</em>{@code #}<em>{@code methodName}</em> for a method in the defined class.
     *     If multiple methods are found with the given name, an exception is thrown.</li>
     * <li><em>{@code methodName}</em>(<em>parameterTypes</em>) for a method in the test class itself.
     *     The parameter types are used as-is.</li>
     * <li><em>{@code className}</em>{@code #}<em>{@code methodName}</em>(<em>parameterTypes</em>) for a method in the defined class.
     *     The parameter types are used as-is.</li>
     * </ul>
     *
     * @param methodReference A reference to the method to find.
     * @param context The current extension context; never {@code null}.
     * @return The method that was found.
     */
    public static Method findMethod(String methodReference, ExtensionContext context) {
        if (isBlank(methodReference)) {
            throw new PreconditionViolationException("methodReference must not be null or blank");
        }

        Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(methodReference);
        if (!matcher.matches()) {
            throw new PreconditionViolationException(String.format("[%s] is not a valid method reference: "
                    + "it must be the method name, optionally preceded by a fully qualified class name followed by a '#', "
                    + "and optionally followed by a parameter list enclosed in parentheses.",
                    methodReference));
        }

        String className = className(matcher);
        String methodName = methodName(matcher);
        String methodArguments = methodArguments(matcher);

        Class<?> factoryClass = className == null
                ? context.getRequiredTestClass()
                : ReflectionSupport.tryToLoadClass(className)
                        .getOrThrow(cause -> new JUnitException(String.format("Could not load class [%s]", className), cause));

        return methodArguments == null
                ? findSingleMethodWithName(factoryClass, methodName)
                : findMethodUsingMethodArguments(factoryClass, methodName, methodArguments);
    }

    private static Method findSingleMethodWithName(Class<?> factoryClass, String methodName) {
        List<Method> methods = ReflectionSupport.findMethods(factoryClass, method -> methodName.equals(method.getName()),
                HierarchyTraversalMode.TOP_DOWN);

        if (methods.isEmpty()) {
            throw new PreconditionViolationException(String.format("Could not find method [%s] in class [%s]", methodName, factoryClass.getName()));
        }
        if (methods.size() > 1) {
            throw new PreconditionViolationException(String.format("Found several methods named [%s] in class [%s]",
                    methodName, factoryClass.getName()));
        }
        return methods.get(0);
    }

    private static Method findMethodUsingMethodArguments(Class<?> factoryClass, String methodName, String methodArguments) {
        return ReflectionSupport.findMethod(factoryClass, methodName, methodArguments)
                .orElseThrow(() -> new PreconditionViolationException(String.format("Could not find method [%s(%s)] in class [%s]",
                        methodName, methodArguments, factoryClass.getName())));
    }

    private static boolean isBlank(String value) {
        return value == null || value.chars().allMatch(Character::isWhitespace);
    }

    private static Pattern createMethodReferencePattern() {
        // Let's be a bit lazy in the pattern; actual method lookup will trigger errors if parts are incorrectly formatted

        String javaIdentifier = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
        String javaType = String.format("%s(?:\\.%s)*", javaIdentifier, javaIdentifier);

        String classNamePart = String.format("(?<className>%s(?:\\.%s)*)", javaIdentifier, javaIdentifier);
        String methodNamePart = String.format("(?<methodName>%s)", javaIdentifier);
        String methodArgumentsPart = String.format("(?<methodArguments>(?:%s(?:,\\s*%s)*)?)", javaType, javaType);

        String regex = String.format("(?:%s#)?%s(?:\\(%s\\))?", classNamePart, methodNamePart, methodArgumentsPart);
        return Pattern.compile(regex);
    }

    static String className(Matcher matcher) {
        return matcher.group("className");
    }

    static String methodName(Matcher matcher) {
        return matcher.group("methodName");
    }

    static String methodArguments(Matcher matcher) {
        return matcher.group("methodArguments");
    }

    /**
     * The result of finding a method.
     * Besides the method itself, this class also knows which parameter type combination was used to find the method,
     * and supports invoking the method.
     *
     * @author Rob Spoor
     */
    public static final class Result {

        private final Method method;
        private final int index;

        Result(Method method, int index) {
            this.method = method;
            this.index = index;
        }

        /**
         * Returns the method that was found.
         *
         * @return The method that was found.
         */
        public Method method() {
            return method;
        }

        /**
         * Returns the index of the parameter type combination that was used to find the method.
         *
         * @return The index of the parameter type combination that was used to find the method.
         */
        public int index() {
            return index;
        }
    }
}
