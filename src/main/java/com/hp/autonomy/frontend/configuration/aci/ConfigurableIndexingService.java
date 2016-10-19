/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.aci;

import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.IndexingService;

/**
 * An {@link IndexingService} which retrieves its configuration dynamically when {@link #executeCommand} is called
 */
public interface ConfigurableIndexingService extends IndexingService {

    ServerDetails getServerDetails();

}
