/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.authentication;

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import lombok.Setter;

/**
 * Validator for {@link CommunityAuthentication}
 */
@SuppressWarnings("JavaDoc")
public class CommunityAuthenticationValidator implements Validator<CommunityAuthentication> {

    /**
     * @param The {@link AciService} to use for validation
     */
    @Setter
    private AciService aciService;

    /**
     * @param The {@link ProcessorFactory} to use for validation
     */
    @Setter
    private ProcessorFactory processorFactory;

    @Override
    public ValidationResult<?> validate(final CommunityAuthentication config) {
        return config.validate(aciService, processorFactory);
    }

    @Override
    public Class<CommunityAuthentication> getSupportedClass() {
        return CommunityAuthentication.class;
    }

}
