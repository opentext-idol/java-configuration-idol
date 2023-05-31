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

import com.autonomy.aci.client.transport.AciServerDetails;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;
import org.springframework.core.ResolvableType;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CommunityAuthenticationTest extends ConfigurationComponentTest<TestConfig> {
    @Override
    public void setUp() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(Authentication.class, Mixins.class);
        json = new JacksonTester<>(getClass(), ResolvableType.forClass(getType()), objectMapper);
    }

    @Override
    protected Class<TestConfig> getType() {
        return TestConfig.class;
    }

    @Override
    protected TestConfig constructComponent() {
        final UsernameAndPassword defaultLogin = DefaultLogin.generateDefaultLogin();

        final ServerConfig community = ServerConfig.builder()
                .protocol(AciServerDetails.TransportProtocol.HTTP)
                .host("test-server")
                .port(9030)
                .build();

        final CommunityAuthentication communityAuthentication = CommunityAuthentication.builder()
                .community(community)
                .defaultLogin(defaultLogin)
                .method("autonomy")
                .build();

        return new TestConfig(communityAuthentication);
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("/com/hp/autonomy/frontend/configuration/authentication/communityAuthentication.json"));
    }

    @Override
    protected void validateJson(final JsonContent<TestConfig> jsonContent) {
        jsonContent.assertThat().hasJsonPathStringValue("@.authentication.name", "CommunityAuthentication");
        jsonContent.assertThat().hasJsonPathStringValue("@.authentication.method", "autonomy");
        jsonContent.assertThat().hasJsonPathStringValue("@.authentication.community.host", "test-server");
        jsonContent.assertThat().hasJsonPathNumberValue("@.authentication.community.port", 9030);
        jsonContent.assertThat().hasJsonPathStringValue("@.authentication.community.protocol", "HTTP");
        jsonContent.assertThat().hasJsonPathStringValue("@.authentication.defaultLogin.username", "admin");
        jsonContent.assertThat().extractingJsonPathStringValue("@.authentication.defaultLogin.password").isNotNull();
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<TestConfig> objectContent) {
        @SuppressWarnings("CastToConcreteClass")
        final CommunityAuthentication authentication = (CommunityAuthentication) objectContent.getObject().getAuthentication();
        final ServerConfig community = authentication.getCommunity();
        assertThat(community.getHost(), is("localhost"));
        assertThat(community.getProtocol(), is(AciServerDetails.TransportProtocol.HTTP));
        assertThat(community.getPort(), is(9030));
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<TestConfig> objectContent) {
        @SuppressWarnings("CastToConcreteClass")
        final CommunityAuthentication authentication = (CommunityAuthentication) objectContent.getObject().getAuthentication();
        final ServerConfig community = authentication.getCommunity();
        assertThat(authentication.getMethod(), is("autonomy"));
        assertThat(community.getHost(), is("test-server"));
        assertThat(community.getProtocol(), is(AciServerDetails.TransportProtocol.HTTP));
        assertThat(community.getPort(), is(9030));
        assertThat(authentication.getDefaultLogin().getUsername(), is("admin"));
        assertNotNull(authentication.getDefaultLogin().getPassword());
    }

    @Override
    protected void validateString(final String objectAsString) {
        assertTrue(objectAsString.contains("method"));
    }

    @JsonSubTypes({
            @JsonSubTypes.Type(SingleUserAuthentication.class),
            @JsonSubTypes.Type(CommunityAuthentication.class),
            @JsonSubTypes.Type(CasAuthentication.class)
    })
    private static class Mixins {
    }
}
