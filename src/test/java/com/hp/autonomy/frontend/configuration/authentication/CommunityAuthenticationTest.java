/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.authentication;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.CasAuthentication;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.authentication.DefaultLogin;
import com.hp.autonomy.frontend.configuration.authentication.SingleUserAuthentication;
import com.hp.autonomy.frontend.configuration.authentication.TestConfig;
import com.hp.autonomy.frontend.configuration.authentication.UsernameAndPassword;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.fail;

public class CommunityAuthenticationTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();

        objectMapper.addMixInAnnotations(Authentication.class, Mixins.class);
    }

    @Test
    public void jsonSerialization() {
        final UsernameAndPassword defaultLogin = DefaultLogin.generateDefaultLogin();

        final ServerConfig community = new ServerConfig.Builder()
            .setProtocol(AciServerDetails.TransportProtocol.HTTP)
            .setHost("test-server")
            .setPort(9030)
            .build();

        final CommunityAuthentication communityAuthentication = CommunityAuthentication.builder()
            .community(community)
            .defaultLogin(defaultLogin)
            .method("autonomy")
            .build();

        final JsonNode jsonNode = objectMapper.valueToTree(communityAuthentication);

        // hard coding this would prevent package movement
        assertThat(jsonNode.get("name").asText(), is("CommunityAuthentication"));
        assertThat(jsonNode.get("method").asText(), is("autonomy"));
        assertThat(jsonNode.get("community").get("host").asText(), is("test-server"));
        assertThat(jsonNode.get("community").get("port").asInt(), is(9030));
        assertThat(jsonNode.get("community").get("protocol").asText(), is("HTTP"));
        assertThat(jsonNode.get("defaultLogin").get("username").asText(), is("admin"));
        assertThat(jsonNode.get("defaultLogin").get("password").asText(), notNullValue());
    }

    @Test
    public void jsonDeserialization() throws IOException {
        final InputStream inputStream = getClass().getResourceAsStream("/com/hp/autonomy/frontend/configuration/communityAuthentication.json");

        final TestConfig testConfig = objectMapper.readValue(inputStream, TestConfig.class);
        final Authentication<?> authentication = testConfig.getAuthentication();

        if (authentication instanceof CommunityAuthentication) {
            final CommunityAuthentication casAuthentication = (CommunityAuthentication) authentication;
            final ServerConfig cas = casAuthentication.getCommunity();

            assertThat(cas.getHost(), is("localhost"));
            assertThat(cas.getProtocol(), is(AciServerDetails.TransportProtocol.HTTP));
            assertThat(cas.getPort(), is(9030));
        } else {
            fail("Deserialized class not of correct type");
        }
    }

    @JsonSubTypes({
        @JsonSubTypes.Type(SingleUserAuthentication.class),
        @JsonSubTypes.Type(CommunityAuthentication.class),
        @JsonSubTypes.Type(CasAuthentication.class)
    })
    private static class Mixins {
    }
}
