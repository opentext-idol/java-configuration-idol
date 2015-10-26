/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.services.AciService;

/**
 * A validator for {@link Login}
 *
 * @deprecated Use a {@link Validator} of your {@link Authentication} type instead
 */
@Deprecated
public class LoginValidator implements Validator<Login> {

    private AciService aciService;
    private IdolAnnotationsProcessorFactory processorFactory;

    @Override
    public ValidationResult<?> validate(final Login login) {
        return login.validate(aciService, processorFactory);
    }

    @Override
    public Class<Login> getSupportedClass() {
        return Login.class;
    }

    public void setAciService(final AciService aciService) {
        this.aciService = aciService;
    }

    public void setProcessorFactory(final IdolAnnotationsProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }
}
