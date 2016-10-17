/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.aci;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciServerDetails;

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
    public <T> T executeAction(final Set<? extends AciParameter> parameters, final Processor<T> processor) {
        return executeAction(getServerDetails(), parameters, processor);
    }

    /**
     * Uses the provided AciServerDetails rather than those returned by {@link #getServerDetails()}
     * @inheritDoc
     */
    @Override
    public <T> T executeAction(final AciServerDetails serverDetails, final Set<? extends AciParameter> parameters, final Processor<T> processor) {
        return aciService.executeAction(serverDetails, parameters, processor);
    }

}
