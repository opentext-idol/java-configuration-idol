/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.EncryptionCodec;
import com.autonomy.aci.client.util.AciParameters;
import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.IndexingException;
import com.autonomy.nonaci.indexing.IndexingService;
import com.autonomy.nonaci.indexing.impl.IndexCommandImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.marshalling.processors.NoopProcessor;
import com.hp.autonomy.types.idol.responses.GetChildrenResponseData;
import com.hp.autonomy.types.idol.responses.GetStatusResponseData;
import com.hp.autonomy.types.idol.responses.GetVersionResponseData;
import com.hp.autonomy.types.requests.idol.actions.general.GeneralActions;
import com.hp.autonomy.types.requests.idol.actions.status.StatusActions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Configuration for an ACI server, which can also include index and service ports.
 */
@SuppressWarnings({"JavaDoc", "WeakerAccess", "DefaultAnnotationParam"})
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = ServerConfig.ServerConfigBuilder.class)
public class ServerConfig extends SimpleComponent<ServerConfig> implements OptionalConfigurationComponent<ServerConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfig.class);

    private static final int MAX_PORT = 65535;

    private final AciServerDetails.TransportProtocol protocol;
    private final String host;
    private final Integer port;

    private final ServerDetails.TransportProtocol indexProtocol;
    private final Integer indexPort;
    private final AciServerDetails.TransportProtocol serviceProtocol;
    private final Integer servicePort;

    /**
     * @return The producttypecsv of the server, used for validation
     */
    private final Set<ProductType> productType;

    /**
     * @return The error message to expect when testing the index port of the server.  If not defined it is assumed that
     * this server does not support indexing.
     */
    private final String indexErrorMessage;

    /**
     * @return A Pattern used to match the product type. Useful for connectors.
     */
    private final Pattern productTypeRegex;

    /**
     * @return An EncryptionCodec used for encryption. This is not serialized.
     */
    private final EncryptionCodec encryptionCodec;

    /**
     * Creates a new ServerConfig with the given ServerDetails for indexing
     *
     * @param serverDetails The ServerDetails to use
     * @return A new ServerConfig with the supplied indexing details
     */
    public ServerConfig withIndexServer(final ServerDetails serverDetails) {
        return toBuilder()
                .indexProtocol(serverDetails.getProtocol())
                .indexPort(serverDetails.getPort())
                .build();
    }

    /**
     * Fetches the index and service ports from the component
     *
     * @param aciService      The {@link AciService} used to discover the ports.
     * @param indexingService The {@link IndexingService} used to test the index port. This can be null if no index port is specified.
     * @param processorFactory Idol response parser generator
     * @param serverProductTypes The product types associated with the server
     * @return A new ServerConfig with its indexing and service details filled in.
     */
    public ServerConfig fetchServerDetails(
            final AciService aciService,
            final IndexingService indexingService,
            final ProcessorFactory processorFactory,
            final Collection<String> serverProductTypes
    ) {
        final ServerConfigBuilder builder = toBuilder();

        final Ports ports = determinePorts(aciService, processorFactory, serverProductTypes);

        if (ports.indexPort != null) {
            final ServerDetails indexDetails = new ServerDetails();
            indexDetails.setHost(host);
            indexDetails.setPort(ports.indexPort);
            boolean isIndexPortValid = false;

            for (final ServerDetails.TransportProtocol protocol : Arrays.asList(ServerDetails.TransportProtocol.HTTP, ServerDetails.TransportProtocol.HTTPS)) {
                indexDetails.setProtocol(protocol);

                if (testIndexingConnection(indexDetails, indexingService, indexErrorMessage)) {
                    // test http first. If the server is https, it will give an error (quickly),
                    // whereas the timeout when doing https to a http server takes a really long time
                    builder.indexProtocol(protocol);
                    builder.indexPort(ports.indexPort);

                    isIndexPortValid = true;
                    break;
                }
            }

            if (!isIndexPortValid) {
                throw new IllegalArgumentException("Server does not have a valid index port");
            }
        }

        final int servicePort = ports.servicePort;
        final AciServerDetails servicePortDetails = new AciServerDetails();
        servicePortDetails.setHost(host);
        servicePortDetails.setPort(servicePort);

        for (final AciServerDetails.TransportProtocol protocol : Arrays.asList(AciServerDetails.TransportProtocol.HTTP, AciServerDetails.TransportProtocol.HTTPS)) {
            servicePortDetails.setProtocol(protocol);
            servicePortDetails.setPort(servicePort);

            if (testServicePortConnection(servicePortDetails, aciService)) {
                // test http first. If the server is https, it will give an error (quickly),
                // whereas the timeout when doing https to a http server takes a really long time
                builder.serviceProtocol(protocol);
                builder.servicePort(servicePort);

                //Both index and service ports are valid
                return builder.build();
            }
        }

        //Index port valid but service port invalid
        throw new IllegalArgumentException("Server does not have a valid service port");
    }

    private Ports determinePorts(
            final AciService aciService,
            final ProcessorFactory processorFactory,
            final Collection<String> serverProductTypes
    ) {
        try {
            // getStatus doesn't always return ports, but does when an index port is used
            // some versions of Distributed Connector don't return the service port from GetChildren
            final boolean useGetStatusToDeterminePorts = indexErrorMessage != null || serverProductTypes.contains(ProductType.DISTRIBUTED_CONNECTOR.name());

            if (useGetStatusToDeterminePorts) {
                final Processor<GetStatusResponseData> processor = processorFactory.getResponseDataProcessor(GetStatusResponseData.class);
                final GetStatusResponseData getStatusResponseData = aciService.executeAction(toAciServerDetails(), new AciParameters(StatusActions.GetStatus.name()), processor);

                return new Ports(getStatusResponseData.getAciport(), getStatusResponseData.getIndexport(), getStatusResponseData.getServiceport());
            } else {
                final Processor<GetChildrenResponseData> processor = processorFactory.getResponseDataProcessor(GetChildrenResponseData.class);
                final GetChildrenResponseData responseData = aciService.executeAction(toAciServerDetails(), new AciParameters(GeneralActions.GetChildren.name()), processor);

                return new Ports(responseData.getPort(), null, responseData.getServiceport());
            }
        } catch (final RuntimeException e) {
            throw new IllegalArgumentException("Unable to connect to ACI server", e);
        }
    }

    private boolean testServicePortConnection(final AciServerDetails serviceDetails, final AciService aciService) {
        try {
            aciService.executeAction(serviceDetails, new AciParameters("getstatus"), new NoopProcessor());
            return true;
        } catch (final RuntimeException ignored) {
            return false;
        }
    }

    private boolean testIndexingConnection(final ServerDetails indexDetails, final IndexingService indexingService, final CharSequence errorMessage) {
        try {
            indexingService.executeCommand(indexDetails, new IndexCommandImpl("test"));
        } catch (final IndexingException e) {
            // we got back a response from the index port
            return e.getMessage().contains(errorMessage);
        } catch (final RuntimeException ignored) {
            // any other kind of exception is bad
        }

        return false;
    }

    /**
     * @return A representation of this server as an {@link AciServerDetails}
     */
    public AciServerDetails toAciServerDetails() {
        final AciServerDetails details = new AciServerDetails(protocol, host, port);
        if (this.encryptionCodec != null) {
            details.setEncryptionCodec(this.encryptionCodec);
        }
        return details;
    }

    /**
     * @return A representation of this server as an {@link ServerDetails}
     */
    public ServerDetails toServerDetails() {
        final ServerDetails serverDetails = new ServerDetails();

        serverDetails.setHost(host);
        serverDetails.setPort(indexPort);
        serverDetails.setProtocol(indexProtocol);

        return serverDetails;
    }

    /**
     * Validates that the required settings are supplied and that the target server is responding
     *
     * @param aciService       The {@link AciService} to use for validation
     * @param indexingService  The {@link IndexingService} to use for validation. If the server does not support indexing
     *                         this may be null
     * @param processorFactory The {@link ProcessorFactory}
     * @return A {@link ValidationResult} which will be
     * <ul>
     * <li>Valid if the server config is valid</li>
     * <li>If it is not valid because the given server is not of the require type, the data will be a {@link IncorrectServerType},
     * containing a list of valid server types</li>
     * <li>If it is invalid for any other reason, the data will be a {@link ServerConfig.Validation}</li>
     * </ul>
     */
    public ValidationResult<?> validate(final AciService aciService, final IndexingService indexingService, final ProcessorFactory processorFactory) {
        // if the host is blank further testing is futile
        try {
            // string doesn't matter here as we swallow the exception
            basicValidate(null);
        } catch (final ConfigException ignored) {
            return new ValidationResult<>(false, Validation.REQUIRED_FIELD_MISSING);
        }

        final Collection<String> serverProductTypes;

        try {
            serverProductTypes = getServerProductTypes(aciService, processorFactory);
        } catch (final RuntimeException e) {
            LOGGER.debug("Error validating server version for {}", productType);
            LOGGER.debug("", e);
            return new ValidationResult<>(false, Validation.CONNECTION_ERROR);
        }

        if (!testServerVersion(serverProductTypes)) {
            if (productTypeRegex == null) {
                final List<String> friendlyNames = new ArrayList<>();

                for (final ProductType productType : this.productType) {
                    friendlyNames.add(productType.getFriendlyName());
                }

                return new ValidationResult<>(false, new IncorrectServerType(friendlyNames));
            } else {
                // can't use friendly names for regex
                return new ValidationResult<Object>(false, Validation.REGULAR_EXPRESSION_MATCH_ERROR);
            }
        }

        try {
            final ServerConfig serverConfig = fetchServerDetails(aciService, indexingService, processorFactory, serverProductTypes);

            final boolean result = serverConfig.servicePort > 0;

            final boolean indexPortPresent = indexErrorMessage != null;
            return indexPortPresent ? new ValidationResult<>(result && serverConfig.indexPort > 0,
                    Validation.SERVICE_OR_INDEX_PORT_ERROR) : new ValidationResult<>(result, Validation.SERVICE_PORT_ERROR);

        } catch (final RuntimeException e) {
            LOGGER.debug("Error validating config", e);
            return new ValidationResult<>(false, Validation.FETCH_PORT_ERROR);
        }
    }

    /**
     * @param component The name of the configuration section, to be used in case of failure
     * @throws ConfigException If the ServerConfig is invalid
     */
    @Override
    public void basicValidate(final String component) throws ConfigException {
        if (port == null || port <= 0 || port > MAX_PORT) {
            throw new ConfigException(component,
                    component + ": port number must be between 1 and 65535.");
        } else if (StringUtils.isBlank(host)) {
            throw new ConfigException(component,
                    component + ": host name must not be blank.");
        }
    }

    private Collection<String> getServerProductTypes(final AciService aciService, final ProcessorFactory processorFactory) {
        // Community's ProductName is just IDOL, so we need to check the product type
        final GetVersionResponseData versionResponseData = aciService
                .executeAction(toAciServerDetails(),
                        new AciParameters(GeneralActions.GetVersion.name()),
                        processorFactory.getResponseDataProcessor(GetVersionResponseData.class));

        return new HashSet<>(Arrays.asList(versionResponseData.getProducttypecsv().split(",")));
    }

    private boolean testServerVersion(final Collection<String> serverProductTypes) {
        return productTypeRegex == null
                ? productType.stream().anyMatch(p -> serverProductTypes.contains(p.name()))
                : serverProductTypes.stream().anyMatch(serverProductType -> productTypeRegex.matcher(serverProductType).matches());
    }

    /**
     * @return The service port details of this ServerConfig as an {@link AciServerDetails}
     */
    public AciServerDetails toServiceServerDetails() {
        return new AciServerDetails(serviceProtocol, host, servicePort);
    }

    @Override
    @JsonIgnore
    public Boolean getEnabled() {
        return true;
    }

    public enum Validation {
        REQUIRED_FIELD_MISSING,
        CONNECTION_ERROR,
        SERVICE_PORT_ERROR,
        SERVICE_OR_INDEX_PORT_ERROR,
        FETCH_PORT_ERROR,
        INCORRECT_SERVER_TYPE,
        REGULAR_EXPRESSION_MATCH_ERROR
    }

    @Data
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class IncorrectServerType {
        private final Validation validation = Validation.INCORRECT_SERVER_TYPE;
        private final List<String> friendlyNames;
    }

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(value = "encryptionCodec", ignoreUnknown = true) // for compatibility with old AciServerDetails config files
    public static class ServerConfigBuilder {
        private AciServerDetails.TransportProtocol protocol = AciServerDetails.TransportProtocol.HTTP;
        private AciServerDetails.TransportProtocol serviceProtocol = AciServerDetails.TransportProtocol.HTTP;
        private ServerDetails.TransportProtocol indexProtocol = ServerDetails.TransportProtocol.HTTP;
        private Pattern productTypeRegex;
        private EncryptionCodec encryptionCodec;

        @JsonProperty("productTypeRegex")
        public String getProductTypeRegexAsString() {
            return Objects.toString(productTypeRegex);
        }

        @JsonProperty("productTypeRegex")
        public ServerConfigBuilder productTypeRegexFromString(final String productTypeRegex) {
            this.productTypeRegex = productTypeRegex == null ? null :
                Pattern.compile(productTypeRegex);
            return this;
        }
    }

    @AllArgsConstructor
    private static class Ports {
        final int aciPort;
        final Integer indexPort;
        final int servicePort;
    }
}
