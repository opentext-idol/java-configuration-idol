/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.IndexCommand;
import com.autonomy.nonaci.indexing.IndexingException;
import com.autonomy.nonaci.indexing.IndexingService;

/**
 * Base implementation of {@link ConfigurableIndexingService}
 */
public abstract class AbstractConfigurableIndexingService implements ConfigurableIndexingService {
    private final IndexingService indexingService;

    protected AbstractConfigurableIndexingService(final IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Override
    public int executeCommand(final IndexCommand command) throws IndexingException {
        return indexingService.executeCommand(getServerDetails(), command);
    }

    /**
     * Uses the given {@link ServerDetails} instead of those returned by {@link #getServerDetails()}
     * @inheritDoc
     */
    @Override
    public int executeCommand(final ServerDetails serverDetails, final IndexCommand command) throws IndexingException {
        return indexingService.executeCommand(serverDetails, command);
    }
}
