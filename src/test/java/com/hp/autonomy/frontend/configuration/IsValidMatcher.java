package com.hp.autonomy.frontend.configuration;

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
