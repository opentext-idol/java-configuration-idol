/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.services.AciService;
import lombok.Setter;

/**
 * Validator for {@link CommunityAuthentication}
 */
public class CommunityAuthenticationValidator implements Validator<CommunityAuthentication> {

    /**
     * @param The {@link AciService} to use for validation
     */
    @Setter
    private AciService aciService;

    /**
     * @param The {@link IdolAnnotationsProcessorFactory} to use for validation
     */
    @Setter
    private IdolAnnotationsProcessorFactory processorFactory;

    @Override
    public ValidationResult<?> validate(final CommunityAuthentication config) {
        return config.validate(aciService, processorFactory);
    }

    @Override
    public Class<CommunityAuthentication> getSupportedClass() {
        return CommunityAuthentication.class;
    }

}
