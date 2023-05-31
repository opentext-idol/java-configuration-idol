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
