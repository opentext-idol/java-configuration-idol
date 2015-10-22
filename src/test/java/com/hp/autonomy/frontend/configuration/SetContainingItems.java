/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.AllOf.allOf;

class SetContainingItems<T> extends ArgumentMatcher<Set<? super T>> {

    private final Set<? super T> set = new HashSet<>();
    private Matcher<? super T> matcher;

    @SafeVarargs
    private SetContainingItems(final T... items) {
        set.addAll(Arrays.asList(items));
    }

    private SetContainingItems(final Matcher<? super T> matcher) {
        this.matcher = matcher;
    }

    @SafeVarargs
    @Factory
    static <T> SetContainingItems<T> isSetWithItems(final T... items) {
        return new SetContainingItems<>(items);
    }

    @SafeVarargs
    @Factory
    static <T> Matcher<Set<T>> isSetWithItems(final Matcher<? super T>... matchers) {
        final List<Matcher<? super Set<T>>> all = new ArrayList<>(matchers.length);

        for (final Matcher<? super T> elementMatcher : matchers) {
            // Doesn't forward to hasItem() method so compiler can sort out generics.
            all.add(new SetContainingItems<>(elementMatcher));
        }

        return allOf(all);
    }

    @Override
    public boolean matches(final Object item) {
        if(!(item instanceof Set)) {
            return false;
        }

        final Set<?> itemAsSet = (Set<?>) item;

        if(matcher == null) {
            return set.containsAll(itemAsSet);
        }
        else {
            for(final Object setItem : itemAsSet) {
                if (matcher.matches(setItem)) {
                    return true;
                }
            }

            return false;
        }
    }
}
