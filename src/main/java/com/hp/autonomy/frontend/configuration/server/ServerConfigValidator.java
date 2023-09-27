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

package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.nonaci.indexing.IndexingService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.opentext.idol.types.marshalling.ProcessorFactory;

/**
 * A {@link Validator} for {@link ServerConfig}
 */
public class ServerConfigValidator implements Validator<ServerConfig> {

    private AciService aciService;
    private IndexingService indexingService;
    private ProcessorFactory processorFactory;

    /**
     * @param processorFactory The {@link ProcessorFactory} to use for validation
     */
    public void setProcessorFactory(final ProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    /**
     * @param aciService The {@link AciService} to use for validation
     */
    public void setAciService(final AciService aciService) {
        this.aciService = aciService;
    }

    /**
     * Sets the indexing service to use for validation.  This is an optional dependency, if none of the
     * ACI servers to be validated will have index ports.
     * @param indexingService The indexing service to use for validation
     */
    public void setIndexingService(final IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Override
    public ValidationResult<?> validate(final ServerConfig config) {
        return config.validate(aciService, indexingService, processorFactory);
    }

    @Override
    public Class<ServerConfig> getSupportedClass() {
        return ServerConfig.class;
    }
}
