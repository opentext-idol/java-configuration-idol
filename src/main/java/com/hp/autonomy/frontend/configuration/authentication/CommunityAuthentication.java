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

package com.hp.autonomy.frontend.configuration.authentication;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.nonaci.indexing.IndexingService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * {@link Authentication} representing a Community server.
 */
@SuppressWarnings({"WeakerAccess", "JavaDoc", "InstanceVariableOfConcreteClass", "DefaultAnnotationParam"})
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = CommunityAuthentication.CommunityAuthenticationBuilder.class)
@JsonTypeName("CommunityAuthentication")
public class CommunityAuthentication extends SimpleComponent<CommunityAuthentication> implements Authentication<CommunityAuthentication> {

    private final UsernameAndPassword defaultLogin;

    /**
     * @return The configuration of the community server
     */
    private final ServerConfig community;

    /**
     * @return The security type (repository) used for authentication
     */
    private final String method;

    @Override
    public CommunityAuthentication generateDefaultLogin() {
        return toBuilder()
                .defaultLogin(DefaultLogin.generateDefaultLogin())
                .build();
    }

    @Override
    public CommunityAuthentication withoutDefaultLogin() {
        return toBuilder()
                .defaultLogin(UsernameAndPassword.builder().build())
                .build();
    }

    @Override
    public CommunityAuthentication withHashedPasswords() {
        return this;
    }

    @Override
    public CommunityAuthentication withoutPasswords() {
        return this;
    }

    @SuppressWarnings({"InstanceofConcreteClass", "CastToConcreteClass"})
    @Override
    public CommunityAuthentication merge(final Authentication<?> other) {
        return other instanceof CommunityAuthentication ? merge((CommunityAuthentication) other) : this;
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (!LoginTypes.DEFAULT.equalsIgnoreCase(method)) {
            community.basicValidate("Community");
        }
    }

    @Override
    @JsonIgnore
    public Boolean getEnabled() {
        return true;
    }

    /**
     * Checks that the community server details are valid
     *
     * @param aciService       The {@link AciService} to use for validation
     * @param processorFactory The {@link ProcessorFactory} to use for validation
     * @return A {@link ValidationResult} determining the validity of the server
     * @see ServerConfig#validate(AciService, IndexingService, ProcessorFactory)
     */
    public ValidationResult<?> validate(final AciService aciService, final ProcessorFactory processorFactory) {
        return community.validate(aciService, null, processorFactory);
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties({"cas", "singleUser", "className"}) // backwards compatibility
    public static class CommunityAuthenticationBuilder {
    }
}
