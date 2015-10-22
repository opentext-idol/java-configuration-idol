/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.configuration.Validator;
import lombok.Setter;

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
