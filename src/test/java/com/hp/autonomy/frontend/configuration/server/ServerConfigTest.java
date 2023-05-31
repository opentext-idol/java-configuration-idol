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

package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.IndexCommand;
import com.autonomy.nonaci.indexing.IndexingException;
import com.autonomy.nonaci.indexing.IndexingService;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.GetChildrenResponseData;
import com.hp.autonomy.types.idol.responses.GetStatusResponseData;
import com.hp.autonomy.types.idol.responses.GetVersionResponseData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Factory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.regex.Pattern;

import static com.hp.autonomy.frontend.configuration.server.IsValidMatcher.valid;
import static com.hp.autonomy.frontend.configuration.server.ServerConfigTest.IsAciParameter.aciParameter;
import static com.hp.autonomy.frontend.configuration.server.SetContainingItems.isSetWithItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ServerConfigTest extends ConfigurationComponentTest<ServerConfig> {
    @Mock
    private Processor<Void> voidProcessor;
    @Mock
    private Processor<GetVersionResponseData> getVersionProcessor;
    @Mock
    private AciService aciService;
    @Mock
    private IndexingService indexingService;
    @Mock
    private ProcessorFactory processorFactory;

    @Override
    public void setUp() {
        super.setUp();
        when(processorFactory.getVoidProcessor()).thenReturn(voidProcessor);
        when(processorFactory.getResponseDataProcessor(GetVersionResponseData.class)).thenReturn(getVersionProcessor);
    }

    @Test
    public void testValidate() {
        final ProductType productType = ProductType.SERVICECOORDINATOR;

        final GetVersionResponseData getVersionResponseData = mockGetVersionResponse(productType.name());

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 6666)),
                argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
                any()
        )).thenReturn(getVersionResponseData);

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 6666)),
                argThat(isSetWithItems(aciParameter("action", "GetChildren"))),
                any()
        )).thenReturn(mockGetChildrenResponse(6666, 6668));

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 6668)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(true);

        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(6666)
                .productType(Collections.singleton(productType))
                .build();

        assertThat(serverConfig.validate(aciService, null, processorFactory), is(valid()));
    }

    @Test
    public void testValidateWithIndexPort() {
        final ProductType productType = ProductType.AXE;

        final GetVersionResponseData getVersionResponseData = mockGetVersionResponse(productType.name());

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7666)),
                argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
                any()
        )).thenReturn(getVersionResponseData);

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7666)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(mockGetStatusResponse(7666, 7667, 7668));

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7668)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(true);

        final String indexErrorMessage = "Bad command or file name";
        when(indexingService.executeCommand(
                argThat(new IsServerDetails("example.com", 7667)),
                any(IndexCommand.class)
        )).thenThrow(new IndexingException(indexErrorMessage));

        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(7666)
                .productType(Collections.singleton(productType))
                .indexErrorMessage(indexErrorMessage)
                .build();

        assertThat(serverConfig.validate(aciService, indexingService, processorFactory), is(valid()));
    }

    @Test
    public void testValidateWithIncorrectIndexErrorMessage() {
        final ProductType productType = ProductType.AXE;

        final GetVersionResponseData getVersionResponseData = mockGetVersionResponse(productType.name());

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7666)),
                argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
                any()
        )).thenReturn(getVersionResponseData);

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7666)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(mockGetStatusResponse(7666, 7667, 7668));

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7668)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(true);

        when(indexingService.executeCommand(
                argThat(new IsServerDetails("example.com", 7667)),
                any(IndexCommand.class)
        )).thenThrow(new IndexingException("ERRORPARAMBAD"));

        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(7666)
                .productType(Collections.singleton(productType))
                .indexErrorMessage("Bad command or file name")
                .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService, processorFactory);
        assertThat(validationResult.getData(), is(ServerConfig.Validation.FETCH_PORT_ERROR));
        assertThat(serverConfig.validate(aciService, indexingService, processorFactory), is(not(valid())));
    }

    @Test
    public void testValidateWithWrongVersion() {
        final ProductType productType = ProductType.UASERVER;
        final GetVersionResponseData getVersionResponseData = mockGetVersionResponse(productType.name());

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 6666)),
                argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
                any()
        )).thenReturn(getVersionResponseData);

        // no further stubbing required because we won't get that far

        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(6666)
                .productType(Collections.singleton(ProductType.AXE))
                .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService, processorFactory);
        assertThat(validationResult.getData(), is(new ServerConfig.IncorrectServerType(Collections.singletonList("Content"))));
        assertThat(serverConfig.validate(aciService, null, processorFactory), is(not(valid())));
    }

    @Test
    public void testValidateWithNoServicePort() {
        final ProductType productType = ProductType.SERVICECOORDINATOR;

        final GetVersionResponseData getVersionResponseData = mockGetVersionResponse(productType.name());

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 6666)),
                argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
                any()
        )).thenReturn(getVersionResponseData);

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 6666)),
                argThat(isSetWithItems(aciParameter("action", "GetChildren"))),
                any()
        )).thenReturn(mockGetChildrenResponse(6666, null));


        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(6666)
                .productType(Collections.singleton(productType))
                .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService, processorFactory);
        assertThat(validationResult.getData(), is(ServerConfig.Validation.SERVICE_PORT_ERROR));
        assertThat(serverConfig.validate(aciService, null, processorFactory), is(not(valid())));
    }

    @Test
    public void testValidateWithMissingIndexPort() {
        final ProductType productType = ProductType.AXE;

        final GetVersionResponseData getVersionResponseData = mockGetVersionResponse(productType.name());

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7666)),
                argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
                any()
        )).thenReturn(getVersionResponseData);

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7666)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(mockGetChildrenResponse(7666, 7668));

        final String indexErrorMessage = "Bad command or file name";
        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(7666)
                .productType(Collections.singleton(productType))
                .indexErrorMessage(indexErrorMessage)
                .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService, processorFactory);
        assertThat(validationResult.getData(), is(ServerConfig.Validation.FETCH_PORT_ERROR));
        assertThat(serverConfig.validate(aciService, null, processorFactory), is(not(valid())));
    }

    @Test
    public void testValidateWithInvalidHost() {
        final ServerConfig serverConfig = ServerConfig.builder()
                .host("")
                .port(6666)
                .productType(Collections.singleton(ProductType.SERVICECOORDINATOR))
                .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService, processorFactory);
        assertThat(validationResult.getData(), is(ServerConfig.Validation.REQUIRED_FIELD_MISSING));
        assertThat(serverConfig.validate(aciService, null, processorFactory), is(not(valid())));
    }

    @Test
    public void testValidateWithInvalidPort() {
        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(0)
                .productType(Collections.singleton(ProductType.SERVICECOORDINATOR))
                .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, indexingService, processorFactory);
        assertThat(validationResult.getData(), is(ServerConfig.Validation.REQUIRED_FIELD_MISSING));
        assertThat(serverConfig.validate(aciService, null, processorFactory), is(not(valid())));
    }

    @Test
    public void testValidateWithMultipleAllowedServers() {
        final ProductType productType = ProductType.AXE;
        final GetVersionResponseData getVersionResponseData = mockGetVersionResponse(productType.name());

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7666)),
                argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
                any()
        )).thenReturn(getVersionResponseData);

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7666)),
                argThat(isSetWithItems(aciParameter("action", "GetChildren"))),
                any()
        )).thenReturn(mockGetChildrenResponse(7666, 7668));

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7668)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(true);

        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(7666)
                .productType(EnumSet.of(ProductType.AXE, ProductType.DAH, ProductType.IDOLPROXY))
                .build();

        assertThat(serverConfig.validate(aciService, null, processorFactory), is(valid()));
    }

    @Test
    public void testValidateWithProductTypeRegex() {
        final GetVersionResponseData getVersionResponseData = mockGetVersionResponse("FILESYSTEMCONNECTOR");

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7008)),
                argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
                any()
        )).thenReturn(getVersionResponseData);

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7008)),
                argThat(isSetWithItems(aciParameter("action", "GetChildren"))),
                any()
        )).thenReturn(mockGetChildrenResponse(7008, 7010));

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7010)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(true);

        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(7008)
                .productTypeRegexFromString(".*?CONNECTOR")
                .build();

        assertThat(serverConfig.validate(aciService, null, processorFactory), is(valid()));
    }

    @Test
    public void testValidateWithInvalidProductTypeRegex() {
        final GetVersionResponseData getVersionResponseData = mockGetVersionResponse("FILESYSTEMCONNECTOR");

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7008)),
                argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
                any()
        )).thenReturn(getVersionResponseData);

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7008)),
                argThat(isSetWithItems(aciParameter("action", "GetChildren"))),
                any()
        )).thenReturn(mockGetChildrenResponse(7008, 7010));

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 7010)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(true);

        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(7008)
                .productTypeRegexFromString(".*?SERVER")
                .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, null, processorFactory);

        assertThat(validationResult, is(not(valid())));
        assertThat(validationResult.getData(), CoreMatchers.is(ServerConfig.Validation.REGULAR_EXPRESSION_MATCH_ERROR));
    }

    @Test
    public void testDistributedConnectorCallsGetStatus() {
        final GetVersionResponseData getVersionResponseData = mockGetVersionResponse("DISTRIBUTED_CONNECTOR");

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 10000)),
                argThat(isSetWithItems(aciParameter("action", "GetVersion"))),
                any()
        )).thenReturn(getVersionResponseData);

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 10000)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(mockGetStatusResponse(10000, null, 10002));

        when(aciService.executeAction(
                argThat(new IsAciServerDetails("example.com", 10002)),
                argThat(isSetWithItems(aciParameter("action", "GetStatus"))),
                any()
        )).thenReturn(true);

        final ServerConfig serverConfig = ServerConfig.builder()
                .host("example.com")
                .port(10000)
                .productTypeRegex(Pattern.compile(".*?CONNECTOR"))
                .build();

        final ValidationResult<?> validationResult = serverConfig.validate(aciService, null, processorFactory);

        assertThat(validationResult, is(valid()));
    }

    @Override
    protected Class<ServerConfig> getType() {
        return ServerConfig.class;
    }

    @Override
    protected ServerConfig constructComponent() {
        return ServerConfig.builder()
                .host("localhost")
                .port(9000)
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("/com/hp/autonomy/frontend/configuration/server/server.json"));
    }

    @Override
    protected void validateJson(final JsonContent<ServerConfig> jsonContent) {
        jsonContent.assertThat().hasJsonPathStringValue("@.host", "localhost");
        jsonContent.assertThat().hasJsonPathNumberValue("@.port", 9000);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<ServerConfig> objectContent) {
        objectContent.assertThat().hasFieldOrPropertyWithValue("protocol", AciServerDetails.TransportProtocol.HTTP);
        objectContent.assertThat().hasFieldOrPropertyWithValue("host", "localhost");
        objectContent.assertThat().hasFieldOrPropertyWithValue("port", 16000);
        assertThat(objectContent.getObject().getProductType(), hasSize(3));
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<ServerConfig> objectContent) {
        objectContent.assertThat().hasFieldOrPropertyWithValue("protocol", AciServerDetails.TransportProtocol.HTTP);
        objectContent.assertThat().hasFieldOrPropertyWithValue("host", "localhost");
        objectContent.assertThat().hasFieldOrPropertyWithValue("port", 9000);
        assertThat(objectContent.getObject().getProductType(), hasSize(3));
    }

    @Override
    protected void validateString(final String objectAsString) {
        assertTrue(objectAsString.contains("protocol"));
    }

    private GetVersionResponseData mockGetVersionResponse(final String productType) {
        final GetVersionResponseData getVersionResponseData = new GetVersionResponseData();
        getVersionResponseData.setProducttypecsv(productType);
        return getVersionResponseData;
    }

    private GetChildrenResponseData mockGetChildrenResponse(final int port, final Integer servicePort) {
        final GetChildrenResponseData getChildrenResponseData = new GetChildrenResponseData();
        getChildrenResponseData.setPort(port);
        if (servicePort != null) {
            getChildrenResponseData.setServiceport(servicePort);
        }
        return getChildrenResponseData;
    }

    private GetStatusResponseData mockGetStatusResponse(final int port, final Integer indexPort, final int servicePort) {
        final GetStatusResponseData getStatusResponseData = new GetStatusResponseData();
        getStatusResponseData.setAciport(port);
        getStatusResponseData.setIndexport(indexPort);
        getStatusResponseData.setServiceport(servicePort);
        return getStatusResponseData;
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
            if (!(argument instanceof AciParameter)) {
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

        private IsServerDetails(final String host, final int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public boolean matches(final Object o) {
            if (!(o instanceof ServerDetails)) {
                return false;
            }

            final ServerDetails serverDetails = (ServerDetails) o;

            boolean result = true;

            if (host != null) {
                result = host.equals(serverDetails.getHost());
            }

            if (port != -1) {
                result = result && port == serverDetails.getPort();
            }

            return result;
        }
    }
}
