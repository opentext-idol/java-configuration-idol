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

package com.hp.autonomy.frontend.configuration.aci;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.ActionParameter;

import java.util.Set;

/**
 * Base implementation of {@link ConfigurableAciService}
 */
public abstract class AbstractConfigurableAciService implements ConfigurableAciService {

    private final AciService aciService;

    public AbstractConfigurableAciService(final AciService aciService) {
        this.aciService = aciService;
    }

    @Override
    public <T> T executeAction(final Set<? extends ActionParameter<?>> parameters, final Processor<T> processor) {
        return executeAction(getServerDetails(), parameters, processor);
    }

    /**
     * Uses the provided AciServerDetails rather than those returned by {@link #getServerDetails()}
     * {@inheritDoc}
     */
    @Override
    public <T> T executeAction(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters, final Processor<T> processor) {
        return aciService.executeAction(serverDetails, parameters, processor);
    }

}
