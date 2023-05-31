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
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;

/**
 * {@link Validator} for DistributedConfig
 */
public class DistributedConfigValidator implements Validator<DistributedConfig> {

    private AciService aciService;
    private IndexingService indexingService;
    private ProcessorFactory processorFactory;

    /**
     * @param aciService The AciService to use for validation
     */
    public void setAciService(final AciService aciService) {
        this.aciService = aciService;
    }

    /**
     * @param indexingService The IndexingService to use for validation
     */
    public void setIndexingService(final IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    /**
     * @param processorFactory The {@link ProcessorFactory} to use for validation
     */
    public void setProcessorFactory(final ProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    public ValidationResult<?> validate(final DistributedConfig config) {
        return config.validate(aciService, indexingService, processorFactory);
    }

    @Override
    public Class<DistributedConfig> getSupportedClass() {
        return DistributedConfig.class;
    }
}
