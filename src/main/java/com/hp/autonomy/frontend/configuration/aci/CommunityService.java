/*
 * (c) Copyright 2013-2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.configuration.aci;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.types.idol.responses.SecurityType;

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
