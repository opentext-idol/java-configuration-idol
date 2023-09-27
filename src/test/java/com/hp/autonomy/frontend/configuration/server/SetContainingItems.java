/*
 * Copyright 2013-2015 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.configuration.server;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.hamcrest.core.AllOf.allOf;

class SetContainingItems<T> extends BaseMatcher<Set<? super T>> {

    private final ArgumentMatcher<? super T> matcher;

    private SetContainingItems(final ArgumentMatcher<? super T> matcher) {
        this.matcher = matcher;
    }

    @SafeVarargs
    @Factory
    static <T> ArgumentMatcher<Set<T>> isSetWithItems(final ArgumentMatcher<T>... matchers) {
        final Collection<Matcher<? super Set<T>>> all = new ArrayList<>(matchers.length);

        for (final ArgumentMatcher<T> elementMatcher : matchers) {
            // Doesn't forward to hasItem() method so compiler can sort out generics.
            all.add(new SetContainingItems<>(elementMatcher));
        }

        return new HamcrestArgumentMatcher<>(allOf(all));
    }

    @Override
    public boolean matches(final Object item) {
        if (!(item instanceof Set)) {
            return false;
        }

        final Iterable<?> itemAsSet = (Iterable<?>) item;
        for (final Object setItem : itemAsSet) {
            if (matcher.matches((T) setItem)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("match in set: ");
        description.appendValue(matcher);
    }

}
