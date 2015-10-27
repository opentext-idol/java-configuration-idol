/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.nonaci.indexing.IndexingService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * {@link Authentication} representing a Community server.
 */
@Data
@JsonDeserialize(builder = CommunityAuthentication.Builder.class)
@JsonTypeName("CommunityAuthentication")
public class CommunityAuthentication implements Authentication<CommunityAuthentication> {

    private final DefaultLogin defaultLogin;

    /**
     * @return The configuration of the community server
     */
    private final ServerConfig community;

    private final String method;

    private CommunityAuthentication(final Builder builder) {
        this.defaultLogin = builder.defaultLogin;
        this.community = builder.community;
        this.method = builder.method;
    }

    /**
     * @return The security type (repository) used for authentication
     */
    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public UsernameAndPassword getDefaultLogin() {
        return defaultLogin.getDefaultLogin();
    }

    @Override
    public CommunityAuthentication generateDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.defaultLogin = DefaultLogin.generateDefaultLogin();

        return builder.build();
    }

    @Override
    public CommunityAuthentication withoutDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.defaultLogin = new DefaultLogin.Builder().build();

        return builder.build();
    }

    @Override
    public CommunityAuthentication withHashedPasswords() {
        return this;
    }

    @Override
    public CommunityAuthentication withoutPasswords() {
        return this;
    }

    @Override
    public CommunityAuthentication merge(final Authentication<?> other) {
        if (other instanceof CommunityAuthentication) {
            final CommunityAuthentication castOther = (CommunityAuthentication) other;

            final Builder builder = new Builder(this);

            builder.setDefaultLogin(this.defaultLogin == null ? castOther.defaultLogin : this.defaultLogin.merge(castOther.defaultLogin));
            builder.setCommunity(this.community == null ? castOther.community : this.community.merge(castOther.community));
            builder.setMethod(this.method == null ? castOther.method : this.method);

            return builder.build();
        } else {
            return this;
        }
    }

    @Override
    public void basicValidate() throws ConfigException {
        if (!LoginTypes.DEFAULT.equalsIgnoreCase(method)) {
            community.basicValidate("Community");
        }
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    /**
     * Checks that the community server details are valid
     * @param aciService The {@link AciService} to use for validation
     * @param processorFactory The {@link IdolAnnotationsProcessorFactory} to use for validation
     * @return A {@link ValidationResult} determining the validity of the server
     * @see ServerConfig#validate(AciService, IndexingService, IdolAnnotationsProcessorFactory)
     */
    public ValidationResult<?> validate(final AciService aciService, final IdolAnnotationsProcessorFactory processorFactory) {
        return community.validate(aciService, null, processorFactory);
    }

    @NoArgsConstructor
    @JsonPOJOBuilder(withPrefix = "set")
    @Setter
    @Accessors(chain = true)
    @JsonIgnoreProperties({"cas", "singleUser", "className"}) // backwards compatibility
    public static class Builder {

        private DefaultLogin defaultLogin = new DefaultLogin.Builder().build();
        private ServerConfig community;
        private String method;

        public Builder(final CommunityAuthentication communityAuthentication) {
            if (communityAuthentication.defaultLogin != null) {
                this.defaultLogin = communityAuthentication.defaultLogin;
            }

            this.community = communityAuthentication.community;
            this.method = communityAuthentication.method;
        }

        public CommunityAuthentication build() {
            return new CommunityAuthentication(this);
        }

    }
}
