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
     * {@inheritDoc}
     */
    @Override
    public int executeCommand(final ServerDetails serverDetails, final IndexCommand command) throws IndexingException {
        return indexingService.executeCommand(serverDetails, command);
    }
}
