/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.aci;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.CommunityStatusResponseData;
import com.hp.autonomy.types.idol.responses.SecurityType;
import com.hp.autonomy.types.requests.idol.actions.status.StatusActions;

import java.util.List;

/**
 * Default implementation of {@link CommunityService}.
 */
public class CommunityServiceImpl implements CommunityService {

    private AciService aciService;

    private ProcessorFactory processorFactory;

    @Override
    public List<SecurityType> getSecurityTypes(final AciServerDetails community) {
        try {
            final CommunityStatusResponseData responseData = aciService.executeAction(community, new AciParameters(StatusActions.GetStatus.name()),
                    processorFactory.getResponseDataProcessor(CommunityStatusResponseData.class));
            return responseData.getSecurityTypes().getSecurityType();
        } catch (final RuntimeException ignored) {
            return null;
        }
    }

    /**
     * @param aciService The {@link AciService} to use for interacting with Community
     */
    public void setAciService(final AciService aciService) {
        this.aciService = aciService;
    }

    /**
     * @param processorFactory The {@link ProcessorFactory} to use for reading Community responses
     */
    public void setProcessorFactory(final ProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }
}
