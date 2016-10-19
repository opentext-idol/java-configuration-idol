/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.server;

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
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.requests.idol.actions.general.GeneralActions;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConfigurationComponent} for representing an IDOL component which:
 * <ol>
 * <li>May be configured to be distributed</li>
 * <li>Has an index port which is used by the application</li>
 * </ol>
 * <p>
 * If an IDOL component is only required to be distributed, it is sufficient to use {@link ServerConfig} and configure
 * the set of product types to include the required distribution component.
 */
@SuppressWarnings({"InstanceVariableOfConcreteClass", "JavaDoc", "WeakerAccess", "DefaultAnnotationParam"})
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = DistributedConfig.DistributedConfigBuilder.class)
@JsonTypeName("DistributedConfig")
public class DistributedConfig extends SimpleComponent<DistributedConfig> implements OptionalConfigurationComponent<DistributedConfig> {

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

    /**
     * Fetches the port details for the currently configured config
     *
     * @param aciService      The {@link AciService} to use to test the ACI and service ports
     * @param indexingService The {@link IndexingService} to use to test the index port
     * @return A DistributedConfig whose index and service ports have been filled in appropriately
     */
    public DistributedConfig fetchServerDetails(final AciService aciService, final IndexingService indexingService, final ProcessorFactory processorFactory) {
        final DistributedConfigBuilder builder = toBuilder();

        if (distributed) {
            builder.dih(dih.fetchServerDetails(aciService, indexingService, processorFactory));
            builder.dah(dah.fetchServerDetails(aciService, indexingService, processorFactory));
        } else {
            builder.standard(standard.fetchServerDetails(aciService, indexingService, processorFactory));
        }

        return builder.build();
    }

    /**
     * @return The {@link AciServerDetails} for the ACI part of the config. If distributed is true these will be the dah
     * settings, otherwise they will be the standard settings.
     */
    public AciServerDetails toAciServerDetails() {
        return distributed ? dah.toAciServerDetails() : standard.toAciServerDetails();
    }

    /**
     * @return The {@link AciServerDetails} for the ACI part of the config. If distributed is true these will be the DIH
     * settings, otherwise they will be the standard settings.
     */
    public AciServerDetails toIndexingAciServerDetails() {
        return distributed ? dih.toAciServerDetails() : standard.toAciServerDetails();
    }

    /**
     * @return The {@link ServerDetails} for the indexing part of the config. If distributed is true these will be the dih
     * settings, otherwise they will be the standard settings.
     */
    public ServerDetails toServerDetails() {
        return distributed ? dih.toServerDetails() : standard.toServerDetails();
    }

    /**
     * Validates the DistributedConfig. If distributed is true, it will only be valid if both the dah and dih are valid.
     * Otherwise, it will be valid if the standard config is valid.
     * <p>
     * If using a DAH this method requires that a call to LanguageSettings will succeed. This requirement may be removed
     * in a future version.
     *
     * @param aciService       The {@link AciService} to use for validation
     * @param indexingService  The {@link IndexingService} to use for validation. If the server does not support indexing
     *                         this may be null
     * @param processorFactory The {@link ProcessorFactory} used to process the responses
     * @return A validation result which will be:
     * <ul>
     * <li>True if the configuration is valid; or false otherwise</li>
     * <li>If the result is invalid because the call to language settings failed, the data will be {@link DistributedConfig.Validation#LANGUAGE_SETTINGS}</li>
     * <li>If the result is invalid for any other reason the result will be the same as ServerConfig#validate</li>
     * </ul>
     * @see ServerConfig#validate(AciService, IndexingService, ProcessorFactory)
     */
    public ValidationResult<?> validate(final AciService aciService, final IndexingService indexingService, final ProcessorFactory processorFactory) {
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
                        aciService.executeAction(dah.toAciServerDetails(),
                                new AciParameters(GeneralActions.LanguageSettings.name()),
                                processorFactory.getVoidProcessor());
                    } catch (final AciErrorException ignored) {
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
     * @param component The name of the configuration section, to be used in case of failure
     * @throws ConfigException If the ServerConfig is invalid
     * @see ServerConfig#basicValidate(String)
     */
    @Override
    public void basicValidate(final String component) throws ConfigException {
        if (distributed) {
            dih.basicValidate(component);
            dah.basicValidate(component);
        } else {
            standard.basicValidate(component);
        }
    }

    @Override
    @JsonIgnore
    public Boolean getEnabled() {
        return true;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class DistributedConfigBuilder {
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
