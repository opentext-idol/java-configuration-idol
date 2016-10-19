/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
