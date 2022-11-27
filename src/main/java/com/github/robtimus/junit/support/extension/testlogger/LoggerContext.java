/*
 * LoggerContext.java
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

package com.github.robtimus.junit.support.extension.testlogger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

abstract class LoggerContext<L, A> {

    private L originalLevel;
    private List<A> originalAppenders;
    private boolean originalUseParentAppenders;

    abstract L doGetLevel();

    abstract void doSetLevel(L level);

    void doAddAppenders(List<A> appenders) {
        for (A appender : appenders) {
            doAddAppender(appender);
        }
    }

    void doSetAppender(A appender) {
        doRemoveAppenders();
        doAddAppender(appender);
    }

    void doSetAppenders(List<A> appenders) {
        doRemoveAppenders();
        doAddAppenders(appenders);
    }

    void doRemoveAppenders() {
        for (A appender : doListAppenders()) {
            doRemoveAppender(appender);
        }
    }

    void doRemoveAppenders(Predicate<? super A> filter) {
        for (A existingAppender : doListAppenders()) {
            if (filter.test(existingAppender)) {
                doRemoveAppender(existingAppender);
            }
        }
    }

    Stream<A> streamAppenders() {
        return StreamSupport.stream(doListAppenders().spliterator(), false);
    }

    private List<A> listAppenders() {
        List<A> appenders = new ArrayList<>();
        for (A appender : doListAppenders()) {
            appenders.add(appender);
        }
        return appenders;
    }

    abstract Iterable<A> doListAppenders();

    abstract void doAddAppender(A appender);

    abstract void doRemoveAppender(A appender);

    abstract boolean doGetUseParentAppenders();

    abstract void doSetUseParentAppenders(boolean useParentAppenders);

    void saveSettings() {
        originalLevel = doGetLevel();
        originalAppenders = listAppenders();
        originalUseParentAppenders = doGetUseParentAppenders();
    }

    /**
     * Restores the original settings of the logger.
     * <p>
     * It should usually not be necessary to call this method, as it will be called automatically once this context goes out of scope.
     */
    public void restore() {
        doSetLevel(originalLevel);
        doSetAppenders(originalAppenders);
        doSetUseParentAppenders(originalUseParentAppenders);
    }
}
