/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.server;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.hamcrest.core.AllOf.allOf;

class SetContainingItems<T> extends ArgumentMatcher<Set<? super T>> {

    private final Matcher<? super T> matcher;

    private SetContainingItems(final Matcher<? super T> matcher) {
        this.matcher = matcher;
    }

    @SafeVarargs
    @Factory
    static <T> Matcher<Set<T>> isSetWithItems(final Matcher<T>... matchers) {
        final Collection<Matcher<? super Set<T>>> all = new ArrayList<>(matchers.length);

        for (final Matcher<T> elementMatcher : matchers) {
            // Doesn't forward to hasItem() method so compiler can sort out generics.
            all.add(new SetContainingItems<>(elementMatcher));
        }

        return allOf(all);
    }

    @Override
    public boolean matches(final Object item) {
        if (!(item instanceof Set)) {
            return false;
        }

        final Iterable<?> itemAsSet = (Iterable<?>) item;
        for (final Object setItem : itemAsSet) {
            if (matcher.matches(setItem)) {
                return true;
            }
        }

        return false;
    }
}
