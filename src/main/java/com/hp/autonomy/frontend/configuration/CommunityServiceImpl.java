/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.AciParameters;

import java.util.List;

public class CommunityServiceImpl implements CommunityService {

    private AciService aciService;

    private IdolAnnotationsProcessorFactory processorFactory;

    @Override
    public List<SecurityType> getSecurityTypes(final AciServerDetails community) {
        try {
            return aciService.executeAction(community, new AciParameters("getstatus"),
                processorFactory.listProcessorForClass(SecurityType.class));
        } catch (final RuntimeException e) {
            return null;
        }
    }

    public void setAciService(final AciService aciService) {
        this.aciService = aciService;
    }

    public void setProcessorFactory(final IdolAnnotationsProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }
}
