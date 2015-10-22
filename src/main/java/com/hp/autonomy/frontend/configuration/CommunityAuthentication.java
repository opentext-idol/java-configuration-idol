/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.AciService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@JsonDeserialize(builder = CommunityAuthentication.Builder.class)
@JsonTypeName("CommunityAuthentication")
public class CommunityAuthentication implements Authentication<CommunityAuthentication> {

    private final DefaultLogin defaultLogin;
    private final ServerConfig community;
    private final String method;

    private CommunityAuthentication(final Builder builder) {
        this.defaultLogin = builder.defaultLogin;
        this.community = builder.community;
        this.method = builder.method;
    }

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
        if(other instanceof CommunityAuthentication) {
            final CommunityAuthentication castOther = (CommunityAuthentication) other;

            final Builder builder = new Builder(this);

            builder.setDefaultLogin(this.defaultLogin == null ? castOther.defaultLogin : this.defaultLogin.merge(castOther.defaultLogin));
            builder.setCommunity(this.community == null ? castOther.community : this.community.merge(castOther.community));
            builder.setMethod(this.method == null ? castOther.method : this.method);

            return builder.build();
        }
        else {
            return this;
        }
    }

    @Override
    public void basicValidate() throws ConfigException {
        if(!LoginTypes.DEFAULT.equalsIgnoreCase(method)) {
            community.basicValidate("Community");
        }
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public ValidationResult<?> validate(final AciService aciService) {
        return community.validate(aciService, null);
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
            if(communityAuthentication.defaultLogin != null) {
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
