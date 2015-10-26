/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.AciParameters;
import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.IndexingService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@JsonDeserialize(builder = DistributedConfig.Builder.class)
@JsonTypeName("DistributedConfig")
public class DistributedConfig implements ConfigurationComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfig.class);

    private final Boolean distributed;

    private final ServerConfig standard;

    private final ServerConfig dih;

    private final ServerConfig dah;

    private DistributedConfig(final Builder builder) {
        this.distributed = builder.distributed;
        this.standard = builder.standard;
        this.dih = builder.dih;
        this.dah = builder.dah;
    }

    public DistributedConfig merge(final DistributedConfig distributedConfig) {
        if (distributedConfig != null) {
            final Builder builder = new Builder();

            builder.setDistributed(this.distributed == null ? distributedConfig.distributed : this.distributed);
            builder.setStandard(this.standard == null ? distributedConfig.standard : this.standard.merge(distributedConfig.standard));
            builder.setDih(this.dih == null ? distributedConfig.dih : this.dih.merge(distributedConfig.dih));
            builder.setDah(this.dah == null ? distributedConfig.dah : this.dah.merge(distributedConfig.dah));

            return builder.build();
        }

        return this;
    }

    public DistributedConfig fetchServerDetails(final AciService aciService, final IndexingService indexingService) {
        final Builder builder = new Builder(this);

        if (distributed) {
            builder.setDih(this.dih.fetchServerDetails(aciService, indexingService));
            builder.setDah(this.dah.fetchServerDetails(aciService, indexingService));
        } else {
            builder.setStandard(this.standard.fetchServerDetails(aciService, indexingService));
        }

        return builder.build();
    }

    public AciServerDetails toAciServerDetails() {
        if (distributed) {
            return dah.toAciServerDetails();
        } else {
            return standard.toAciServerDetails();
        }
    }

    public ServerDetails toServerDetails() {
        if (distributed) {
            return dih.toServerDetails();
        } else {
            return standard.toServerDetails();
        }
    }

    public ValidationResult<?> validate(final AciService aciService, final IndexingService indexingService, final IdolAnnotationsProcessorFactory processorFactory) {
        try {
            if (distributed) {
                final DistributedValidationResultDetails distributedValidationResultDetails = new DistributedValidationResultDetails();

                final ValidationResult<?> dihValidation = dih.validate(aciService, indexingService, processorFactory);
                final ValidationResult<?> dahValidation = dah.validate(aciService, indexingService, processorFactory);

                final boolean dihValid = dihValidation.isValid();
                boolean dahValid = dahValidation.isValid();

                if (!dihValidation.isValid()) {
                    distributedValidationResultDetails.setDihValidationResult(dihValidation);
                }

                if (dahValidation.isValid()) {
                    try {
                        aciService.executeAction(dah.toAciServerDetails(), new AciParameters("LanguageSettings"), processorFactory.forClass(EmptyResponse.class));
                    } catch (final AciErrorException e) {
                        dahValid = false;
                        distributedValidationResultDetails.setDahValidationResult(new ValidationResult<>(false, Validation.LANGUAGE_SETTINGS));
                    }
                } else {
                    distributedValidationResultDetails.setDahValidationResult(dahValidation);
                }

                return new ValidationResult<Object>(dihValid && dahValid, distributedValidationResultDetails);
            } else {
                return standard.validate(aciService, indexingService, processorFactory);
            }
        } catch (final RuntimeException e) {
            LOGGER.debug("Error validating config", e);
            return new ValidationResult<>(false, ServerConfig.Validation.FETCH_PORT_ERROR);
        }
    }

    public boolean basicValidate(final String component) throws ConfigException {
        if (distributed) {
            return dih.basicValidate(component) && dah.basicValidate(component);
        } else {
            return standard.basicValidate(component);
        }
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @NoArgsConstructor
    @JsonPOJOBuilder(withPrefix = "set")
    @Setter
    @Accessors(chain = true)
    public static class Builder {

        private Boolean distributed;

        private ServerConfig standard;

        private ServerConfig dih;
        private ServerConfig dah;

        public Builder(final DistributedConfig distributedConfig) {
            this.distributed = distributedConfig.distributed;
            this.standard = distributedConfig.standard;
            this.dih = distributedConfig.dih;
            this.dah = distributedConfig.dah;
        }

        public DistributedConfig build() {
            return new DistributedConfig(this);
        }
    }

    enum Validation {
        LANGUAGE_SETTINGS
    }

    @Data
    static class DistributedValidationResultDetails {
        private ValidationResult<?> dihValidationResult;
        private ValidationResult<?> dahValidationResult;
    }

}
