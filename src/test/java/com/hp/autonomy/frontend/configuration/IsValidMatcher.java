/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

class IsValidMatcher extends BaseMatcher<ValidationResult<?>> {

    @Override
    public boolean matches(final Object item) {
        return item instanceof ValidationResult && ((ValidationResult<?>) item).isValid();
    }

    static IsValidMatcher valid() {
        return new IsValidMatcher();
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("valid");
    }

    @Override
    public void describeMismatch(final Object item, final Description description) {
        description.appendText(" was not valid");
    }
}
