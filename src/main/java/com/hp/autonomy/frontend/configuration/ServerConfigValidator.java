/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.configuration.Validator;
import com.autonomy.nonaci.indexing.IndexingService;

/**
 * A {@link Validator} for {@link ServerConfig}
 */
public class ServerConfigValidator implements Validator<ServerConfig> {

    private AciService aciService;
    private IndexingService indexingService;

    public void setAciService(final AciService aciService) {
        this.aciService = aciService;
    }

    /**
     * Sets the indexing service to use for validation.  This is an optional dependency, if none of the
     * ACI servers to be validated will have index ports.
     *
     * @param indexingService The indexing service to use for validation
     */
    public void setIndexingService(final IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Override
    public ValidationResult<?> validate(final ServerConfig config) {
        return config.validate(aciService, indexingService);
    }

    @Override
    public Class<ServerConfig> getSupportedClass() {
        return ServerConfig.class;
    }
}
