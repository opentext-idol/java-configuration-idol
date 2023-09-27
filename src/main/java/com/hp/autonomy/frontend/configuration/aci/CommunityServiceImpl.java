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
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.AciParameters;
import com.opentext.idol.types.marshalling.ProcessorFactory;
import com.opentext.idol.types.responses.CommunityStatusResponseData;
import com.opentext.idol.types.responses.SecurityType;
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
