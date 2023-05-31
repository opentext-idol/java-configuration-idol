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

package com.hp.autonomy.frontend.configuration.authentication;

import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@SuppressWarnings("WeakerAccess")
@Builder
@ToString
@AllArgsConstructor
public class TestConfig extends SimpleComponent<TestConfig> implements AuthenticationConfig<TestConfig> {
    private final Authentication<?> authentication;

    @Override
    public TestConfig generateDefaultLogin() {
        return null;
    }

    @Override
    public TestConfig withHashedPasswords() {
        return null;
    }

    @Override
    public Authentication<?> getAuthentication() {
        return authentication;
    }

    @Override
    public TestConfig withoutDefaultLogin() {
        return null;
    }
}
