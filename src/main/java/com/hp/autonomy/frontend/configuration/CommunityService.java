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
