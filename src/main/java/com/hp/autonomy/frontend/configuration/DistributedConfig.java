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

/**
 * {@link ConfigurationComponent} for representing an IDOL component which:
 * <ol>
 *     <li>May be configured to be distributed</li>
 *     <li>Has an index port which is used by the application</li>
 * </ol>
 *
 * If an IDOL component is only required to be distributed, it is sufficient to use {@link ServerConfig} and configure
 * the set of product types to include the required distribution component.
 */
@Data
@JsonDeserialize(builder = DistributedConfig.Builder.class)
@JsonTypeName("DistributedConfig")
public class DistributedConfig implements ConfigurationComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfig.class);

    /**
     * True if the distributed settings are being used; false otherwise
     */
    private final Boolean distributed;

    /**
     * @return The settings for non distributed configuration
     */
    private final ServerConfig standard;

    /**
     * @return The settings for ACI distribution
     */
    private final ServerConfig dih;

    /**
     * @return The settings for Indexing distribution
     */
    private final ServerConfig dah;

    private DistributedConfig(final Builder builder) {
        this.distributed = builder.distributed;
        this.standard = builder.standard;
        this.dih = builder.dih;
        this.dah = builder.dah;
    }

    /**
     * Merges this DistributedConfig with another DistributedConfig.
     * @param distributedConfig The DistributedConfig to merge with.
     * @return A new DistributedConfig whose settings replace the fields in this that are null with those from distributedConfig
     */
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

    /**
     * Fetches the port details for the currently configured config
     * @param aciService The {@link AciService} to use to test the ACI and service ports
     * @param indexingService The {@link IndexingService} to use to test the index port
     * @return A DistributedConfig whose index and service ports have been filled in appropriately
     */
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

    /**
     * @return The {@link AciServerDetails} for the ACI part of the config. If distributed is true these will be the dah
     * settings, otherwise they will be the standard settings.
     */
    public AciServerDetails toAciServerDetails() {
        if (distributed) {
            return dah.toAciServerDetails();
        } else {
            return standard.toAciServerDetails();
        }
    }

    /**
     * @return The {@link ServerDetails} for the indexing part of the config. If distributed is true these will be the dih
     * settings, otherwise they will be the standard settings.
     */
    public ServerDetails toServerDetails() {
        if (distributed) {
            return dih.toServerDetails();
        } else {
            return standard.toServerDetails();
        }
    }

    /**
     * Validates the DistributedConfig. If distributed is true, it will only be valid if both the dah and dih are valid.
     * Otherwise, it will be valid if the standard config is valid.
     *
     * If using a DAH this method requires that a call to LanguageSettings will succeed. This requirement may be removed
     * in a future version.
     * @param aciService The {@link com.autonomy.aci.client.services.AciService} to use for validation
     * @param indexingService The {@link com.autonomy.nonaci.indexing.IndexingService} to use for validation. If the server does not support indexing
     * this may be null
     * @param processorFactory The {@link IdolAnnotationsProcessorFactory} used to process the responses
     * @return A validation result which will be:
     * <ul>
     *     <li>True if the configuration is valid; or false otherwise</li>
     *     <li>If the result is invalid because the call to language settings failed, the data will be {@link com.hp.autonomy.frontend.configuration.DistributedConfig.Validation#LANGUAGE_SETTINGS}</li>
     *     <li>If the result is invalid for any other reason the result will be the same as ServerConfig#validate</li>
     * </ul>
     * @see ServerConfig#validate(AciService, IndexingService, IdolAnnotationsProcessorFactory)
     */
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

                // TODO: it shouldn't be mandatory to run a LanguageSettings check as not all products require it
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

    /**
     *
     * @param component The name of the configuration section, to be used in case of failure
     * @return true if all the required settings exist
     * @throws ConfigException If the ServerConfig is invalid
     * @see ServerConfig#basicValidate(String)
     */
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

    public enum Validation {
        LANGUAGE_SETTINGS
    }

    @Data
    static class DistributedValidationResultDetails {
        private ValidationResult<?> dihValidationResult;
        private ValidationResult<?> dahValidationResult;
    }

}
