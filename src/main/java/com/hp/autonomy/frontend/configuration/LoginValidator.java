package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.configuration.Validator;

/*
 * $Id:$
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */

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
