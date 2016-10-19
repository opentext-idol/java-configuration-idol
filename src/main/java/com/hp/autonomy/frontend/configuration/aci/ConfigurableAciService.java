/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.aci;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciServerDetails;

import java.util.Set;

/**
 * An {@link AciService} which retrieves its configuration dynamically when {@link #executeAction} is called
 */
public interface ConfigurableAciService extends AciService {

    AciServerDetails getServerDetails();

}
