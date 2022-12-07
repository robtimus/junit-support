/*
 * LoggerContextHelper.java
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

abstract class LoggerContextHelper<L, A> {

    private L originalLevel;
    private List<A> originalAppenders;
    private boolean originalUseParentAppenders;

    abstract L getLevel();

    abstract void setLevel(L level);

    void addAppenders(List<A> appenders) {
        for (A appender : appenders) {
            addAppender(appender);
        }
    }

    void setAppender(A appender) {
        removeAppenders();
        addAppender(appender);
    }

    void setAppenders(List<A> appenders) {
        removeAppenders();
        addAppenders(appenders);
    }

    void removeAppenders() {
        for (A appender : appenders()) {
            removeAppender(appender);
        }
    }

    void removeAppenders(Predicate<? super A> filter) {
        for (A existingAppender : appenders()) {
            if (filter.test(existingAppender)) {
                removeAppender(existingAppender);
            }
        }
    }

    Stream<A> streamAppenders() {
        return StreamSupport.stream(appenders().spliterator(), false);
    }

    private List<A> listAppenders() {
        List<A> appenders = new ArrayList<>();
        for (A appender : appenders()) {
            appenders.add(appender);
        }
        return appenders;
    }

    abstract Iterable<A> appenders();

    abstract void addAppender(A appender);

    abstract void removeAppender(A appender);

    abstract boolean useParentAppenders();

    abstract void useParentAppenders(boolean useParentAppenders);

    void saveSettings() {
        originalLevel = getLevel();
        originalAppenders = listAppenders();
        originalUseParentAppenders = useParentAppenders();
    }

    void restore() {
        setLevel(originalLevel);
        setAppenders(originalAppenders);
        useParentAppenders(originalUseParentAppenders);
    }
}
