package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.configuration.Validator;
import lombok.Setter;

/*
 * $Id:$
 *
 * Copyright (c) 2014, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
public class CommunityAuthenticationValidator implements Validator<CommunityAuthentication> {

    @Setter
    private AciService aciService;

    @Override
    public ValidationResult<?> validate(final CommunityAuthentication config) {
        return config.validate(aciService);
    }

    @Override
    public Class<CommunityAuthentication> getSupportedClass() {
        return CommunityAuthentication.class;
    }

}
