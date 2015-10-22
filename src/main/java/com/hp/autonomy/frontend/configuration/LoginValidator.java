/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.configuration.Validator;

/**
 * A validator for {@link Login}
 *
 * @deprecated Use a {@link Validator} of your {@link Authentication} type instead
 */
@Deprecated
public class LoginValidator implements Validator<Login> {

    private AciService aciService;

    @Override
    public ValidationResult<?> validate(final Login login) {
        return login.validate(aciService);
    }

    @Override
    public Class<Login> getSupportedClass() {
        return Login.class;
    }

    public void setAciService(final AciService aciService) {
        this.aciService = aciService;
    }
}
