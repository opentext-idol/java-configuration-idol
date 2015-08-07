package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.nonaci.indexing.IndexingService;

public class DistributedConfigValidator implements Validator<DistributedConfig> {

    private AciService aciService;
    private IndexingService indexingService;

    public void setAciService(final AciService aciService) {
        this.aciService = aciService;
    }

    public void setIndexingService(final IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Override
    public ValidationResult<?> validate(final DistributedConfig config) {
        return config.validate(aciService, indexingService);
    }

    @Override
    public Class<DistributedConfig> getSupportedClass() {
        return DistributedConfig.class;
    }
}
