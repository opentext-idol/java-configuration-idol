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

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.opentext.idol.types.marshalling.ProcessorFactory;
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
