/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;

import java.util.List;

/**
 * Service for interacting with community
 */
public interface CommunityService {

    /**
     * @param community The details of the community server
     * @return A list of {@link SecurityType} supported by the community
     */
    List<SecurityType> getSecurityTypes(AciServerDetails community);
}
