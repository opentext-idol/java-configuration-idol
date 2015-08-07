package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.AciService;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.CasConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.UsernameAndPassword;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang.RandomStringUtils;

/*
 * $Id$
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author$ on $Date$
 */

/**
 * Configuration for Login options - how to authenticate and the location of community
 * @deprecated Use {@link Authentication} instead
 */
@JsonDeserialize(builder = Login.Builder.class)
@Getter
@EqualsAndHashCode
@Deprecated
public class Login implements Authentication<Login> {

    /**
     * The authentication method. This should be either a community security type, or the special values "cas" (use
     * CAS SSO), "external" (assumes an authenticating reverse proxy is used) or "default" (use the credentials
     * in the config file).
     */
    private final String method;

    /**
     * The CAS configuration to use.  This will only apply if method is set to "cas".
     */
    private final CasConfig cas;

    /**
     * The default login.  This will only apply if method is set to "default".
     */
    private final UsernameAndPassword defaultLogin;
    private final ServerConfig community;

    private Login(final Builder builder) {
        this.method = builder.method;
        this.cas = builder.cas;
        this.defaultLogin = builder.defaultLogin;
        this.community = builder.community;
    }

    @Override
    public Login merge(final Authentication<?> other)  {
        if(other instanceof Login) {
            final Login login = (Login) other;

            final Builder builder = new Builder();

            builder.setMethod(this.method == null ? login.method : this.method);
            builder.setCas(this.cas == null ? login.cas : this.cas.merge(login.cas));
            builder.setDefaultLogin(this.defaultLogin == null ? login.defaultLogin : this.defaultLogin.merge(login.defaultLogin));
            builder.setCommunity(this.community == null ? login.community : this.community.merge(login.community));

            return builder.build();
        }
        else {
            return this;
        }
    }

    /**
     * @return A new Login without a default login.
     */
    @Override
    public Login withoutDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.defaultLogin = null;

        return builder.build();
    }

    /**
     * @return A new Login with a default username and password
     */
    @Override
    public Login generateDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.defaultLogin = new UsernameAndPassword("admin", generatePassword());

        return builder.build();
    }

    @Override
    public Login withHashedPasswords() {
        return this;
    }

    @Override
    public Login withoutPasswords() {
        return this;
    }

    private String generatePassword() {
        return RandomStringUtils.random(12, true, true);
    }

    public ValidationResult<?> validate(final AciService aciService) {
        return community.validate(aciService, null);
    }

    @Override
    public void basicValidate() throws ConfigException {
        if(this.method.equalsIgnoreCase(LoginTypes.CAS)){
            this.cas.basicValidate();
        }
        else if(!this.method.equalsIgnoreCase(LoginTypes.DEFAULT)){
            this.community.basicValidate("Community");
        }
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {

        private String method;
        private CasConfig cas;
        private UsernameAndPassword defaultLogin;
        private ServerConfig community;

        public Builder() {}

        public Builder(final Login login) {
            this.method = login.method;
            this.cas = login.cas;
            this.defaultLogin = login.defaultLogin;
            this.community = login.community;
        }

        public Builder setCas(final CasConfig casConfig) {
            this.cas = casConfig;
            return this;
        }

        public Builder setCommunity(final ServerConfig community) {
            this.community = community;
            return this;
        }

        public Builder setDefaultLogin(final UsernameAndPassword defaultLogin) {
            this.defaultLogin = defaultLogin;
            return this;
        }

        public Builder setMethod(final String method) {
            this.method = method;
            return this;
        }

        public Login build() {
            return new Login(this);
        }
    }
}
