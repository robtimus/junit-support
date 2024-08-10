/*
 * TestUtils.java
 * Copyright 2024 Rob Spoor
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

package com.github.robtimus.junit.support.extension.util;

import static com.github.robtimus.junit.support.OptionalAssertions.assertIsPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.hamcrest.Matcher;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

@SuppressWarnings("javadoc")
public final class TestUtils {

    private TestUtils() {
    }

    public static EngineExecutionResults runTests(Class<?> testClass) {
        return EngineTestKit.engine(new JupiterTestEngine())
                .selectors(DiscoverySelectors.selectClass(testClass))
                .execute();
    }

    public static void assertSingleTestFailure(Class<?> testClass, Class<? extends Throwable> errorType, Matcher<String> messageMatcher) {
        EngineExecutionResults results = runTests(testClass);

        assertEquals(0, results.testEvents().succeeded().count());
        assertEquals(1, results.testEvents().failed().count());

        Throwable throwable = getSingleTestFailure(results);
        assertEquals(errorType, throwable.getClass());
        assertThat(throwable.getMessage(), messageMatcher);
    }

    public static Throwable getSingleTestFailure(EngineExecutionResults results) {
        TestExecutionResult result = assertIsPresent(results.testEvents().failed().stream()
                .map(event -> event.getPayload(TestExecutionResult.class))
                .findAny()
                .orElse(null));

        return assertIsPresent(result.getThrowable());
    }

    public static Throwable getSingleContainerFailure(EngineExecutionResults results) {
        TestExecutionResult result = assertIsPresent(results.containerEvents().failed().stream()
                .map(event -> event.getPayload(TestExecutionResult.class))
                .findAny()
                .orElse(null));

        return assertIsPresent(result.getThrowable());
    }
}
