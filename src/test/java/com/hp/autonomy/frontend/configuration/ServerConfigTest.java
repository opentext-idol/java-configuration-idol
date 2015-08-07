package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.actions.common.GetVersionProcessor;
import com.autonomy.aci.actions.common.Version;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.IndexCommand;
import com.autonomy.nonaci.indexing.IndexingException;
import com.autonomy.nonaci.indexing.IndexingService;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Factory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static com.hp.autonomy.frontend.configuration.IsValidMatcher.valid;
import static com.hp.autonomy.frontend.configuration.ServerConfigTest.IsAciParameter.aciParameter;
import static com.hp.autonomy.frontend.configuration.SetContainingItems.isSetWithItems;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * $Id:$
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
@Slf4j
public class ServerConfigTest {

    private AciService aciService;
    private IndexingService indexingService;

    @Before
    public void setUp() {
        aciService = mock(AciService.class);
        indexingService = mock(IndexingService.class);
    }

    @Test
    public void testValidate() {
        final ProductType productType = ProductType.SERVICECOORDINATOR;
        final Version version = new Version();
        version.setProductTypes(Collections.singleton(productType.name()));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 6666)),
            argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
            argThat(any(GetVersionProcessor.class))
        )).thenReturn(version);

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 6666)),
            argThat(isSetWithItems(aciParameter("action", "GetChildren"))),
            argThat(any(PortsResponseProcessor.class))
        )).thenReturn(new PortsResponse(6666, 0, 6668));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 6668)),
            argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
            argThat(any(NoopProcessor.class))
        )).thenReturn(true);

        final ServerConfig serverConfig = new ServerConfig.Builder()
            .setHost("example.com")
            .setPort(6666)
            .setProductType(Collections.singleton(productType))
            .build();

        assertThat(serverConfig.validate(aciService, null), is(valid()));
    }

    @Test
    public void testValidateWithIndexPort() {
        final String indexErrorMessage = "Bad command or file name";
        final ProductType productType = ProductType.AXE;

        final Version version = new Version();
        version.setProductTypes(Collections.singleton(productType.name()));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7666)),
            argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
            argThat(any(GetVersionProcessor.class))
        )).thenReturn(version);

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7666)),
            argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
            argThat(any(PortsResponseProcessor.class))
        )).thenReturn(new PortsResponse(7666, 7667, 7668));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7668)),
            argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
            argThat(any(NoopProcessor.class))
        )).thenReturn(true);

        when(indexingService.executeCommand(
            argThat(new IsServerDetails("example.com", 7667)),
            argThat(any(IndexCommand.class))
        )).thenThrow(new IndexingException(indexErrorMessage));

        final ServerConfig serverConfig = new ServerConfig.Builder()
            .setHost("example.com")
            .setPort(7666)
            .setProductType(Collections.singleton(productType))
            .setIndexErrorMessage(indexErrorMessage)
            .build();

        assertThat(serverConfig.validate(aciService, indexingService), is(valid()));
    }

    @Test
    public void testValidateWithIncorrectIndexErrorMessage() {
        final ProductType productType = ProductType.AXE;

        final Version version = new Version();
        version.setProductTypes(Collections.singleton(productType.name()));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7666)),
            argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
            argThat(any(GetVersionProcessor.class))
        )).thenReturn(version);

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7666)),
            argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
            argThat(any(PortsResponseProcessor.class))
        )).thenReturn(new PortsResponse(7666, 7667, 7668));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7668)),
            argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
            argThat(any(NoopProcessor.class))
        )).thenReturn(true);

        when(indexingService.executeCommand(
            argThat(new IsServerDetails("example.com", 7667)),
            argThat(any(IndexCommand.class))
        )).thenThrow(new IndexingException("ERRORPARAMBAD"));

        final ServerConfig serverConfig = new ServerConfig.Builder()
            .setHost("example.com")
            .setPort(7666)
            .setProductType(Collections.singleton(productType))
            .setIndexErrorMessage("Bad command or file name")
            .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService);
        assertThat(validationResult.getData(), is((Object) ServerConfig.Validation.FETCH_PORT_ERROR));
        assertThat(serverConfig.validate(aciService, indexingService), is(not(valid())));
    }

    @Test
    public void testValidateWithWrongVersion() {
        final Version version = new Version();
        version.setProductTypes(Collections.singleton(ProductType.UASERVER.name()));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 6666)),
            argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
            argThat(any(GetVersionProcessor.class))
        )).thenReturn(version);

        // no further stubbing required because we won't get that far

        final ServerConfig serverConfig = new ServerConfig.Builder()
            .setHost("example.com")
            .setPort(6666)
            .setProductType(Collections.singleton(ProductType.AXE))
            .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService);
        assertThat(validationResult.getData(), is((Object) new ServerConfig.IncorrectServerType(Arrays.asList("Content"))));
        assertThat(serverConfig.validate(aciService, null), is(not(valid())));
    }

    @Test
    public void testValidateWithNoServicePort() {
        final ProductType productType = ProductType.SERVICECOORDINATOR;
        final Version version = new Version();
        version.setProductTypes(Collections.singleton(productType.name()));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 6666)),
            argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
            argThat(any(GetVersionProcessor.class))
        )).thenReturn(version);

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 6666)),
            argThat(isSetWithItems(aciParameter("action", "GetChildren"))),
            argThat(any(PortsResponseProcessor.class))
        )).thenReturn(new PortsResponse(6666, 0, 0));


        final ServerConfig serverConfig = new ServerConfig.Builder()
            .setHost("example.com")
            .setPort(6666)
            .setProductType(Collections.singleton(productType))
            .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService);
        assertThat(validationResult.getData(), is((Object) ServerConfig.Validation.FETCH_PORT_ERROR));
        assertThat(serverConfig.validate(aciService, null), is(not(valid())));
    }

    @Test
    public void testValidateWithMissingIndexPort() {
        final String indexErrorMessage = "Bad command or file name";
        final ProductType productType = ProductType.AXE;

        final Version version = new Version();
        version.setProductTypes(Collections.singleton(productType.name()));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7666)),
            argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
            argThat(any(GetVersionProcessor.class))
        )).thenReturn(version);

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7666)),
            argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
            argThat(any(PortsResponseProcessor.class))
        )).thenReturn(new PortsResponse(7666, 0, 7668));

        final ServerConfig serverConfig = new ServerConfig.Builder()
            .setHost("example.com")
            .setPort(7666)
            .setProductType(Collections.singleton(productType))
            .setIndexErrorMessage(indexErrorMessage)
            .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService);
        assertThat(validationResult.getData(), is((Object) ServerConfig.Validation.FETCH_PORT_ERROR));
        assertThat(serverConfig.validate(aciService, null), is(not(valid())));
    }

    @Test
    public void testValidateWithInvalidHost() {
        final ServerConfig serverConfig = new ServerConfig.Builder()
            .setHost("")
            .setPort(6666)
            .setProductType(Collections.singleton(ProductType.SERVICECOORDINATOR))
            .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService);
        assertThat(validationResult.getData(), is((Object) ServerConfig.Validation.REQUIRED_FIELD_MISSING));
        assertThat(serverConfig.validate(aciService, null), is(not(valid())));
    }

    @Test
    public void testValidateWithInvalidPort() {
        final ServerConfig serverConfig = new ServerConfig.Builder()
            .setHost("example.com")
            .setPort(0)
            .setProductType(Collections.singleton(ProductType.SERVICECOORDINATOR))
            .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService);
        assertThat(validationResult.getData(), is((Object) ServerConfig.Validation.REQUIRED_FIELD_MISSING));
        assertThat(serverConfig.validate(aciService, null), is(not(valid())));
    }

    @Test
    public void testValidateWithMultipleAllowedServers() {
        final Version version = new Version();
        version.setProductTypes(Collections.singleton("AXE"));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7666)),
            argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
            argThat(any(GetVersionProcessor.class))
        )).thenReturn(version);

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7666)),
            argThat(isSetWithItems(aciParameter("action", "GetChildren"))),
            argThat(any(PortsResponseProcessor.class))
        )).thenReturn(new PortsResponse(7666, 7667, 7668));

        when(aciService.executeAction(
            argThat(new IsAciServerDetails("example.com", 7668)),
            argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
            argThat(any(NoopProcessor.class))
        )).thenReturn(true);

        final ServerConfig serverConfig = new ServerConfig.Builder()
            .setHost("example.com")
            .setPort(7666)
            .setProductType(new HashSet<>(Arrays.asList(ProductType.AXE, ProductType.DAH, ProductType.IDOLPROXY)))
            .build();

        assertThat(serverConfig.validate(aciService, null), is(valid()));
    }

    static class IsAciParameter extends ArgumentMatcher<AciParameter> {

        private final String name;
        private final String value;

        private IsAciParameter(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        @Factory
        static IsAciParameter aciParameter(final String name, final String value) {
            return new IsAciParameter(name, value);
        }

        @Override
        public boolean matches(final Object argument) {
            if(!(argument instanceof AciParameter)) {
                return false;
            }

            final AciParameter parameter = (AciParameter) argument;

            return name.equalsIgnoreCase(parameter.getName())
                && value.equalsIgnoreCase(parameter.getValue());
        }
    }

    // duplicate all this due to deficiencies of the Autonomy APIs
    private static class IsServerDetails extends ArgumentMatcher<ServerDetails> {

        private final String host;
        private final int port;

        private IsServerDetails() {
            this(null, -1);
        }

        private IsServerDetails(final int port) {
            this(null, port);
        }

        private IsServerDetails(final String host) {
            this(host, -1);
        }

        private IsServerDetails(final String host, final int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public boolean matches(final Object o) {
            if(! (o instanceof ServerDetails)) {
                return false;
            }

            final ServerDetails serverDetails = (ServerDetails) o;

            boolean result = true;

            if(host != null) {
                result = host.equals(serverDetails.getHost());
            }

            if(port != -1) {
                result = result && port == serverDetails.getPort();
            }

            return result;
        }
    }
}
