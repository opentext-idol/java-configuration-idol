/*
 * (c) Copyright 2013-2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.nonaci.indexing.IndexingService;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.GetVersionResponseData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static com.hp.autonomy.frontend.configuration.server.IsValidMatcher.valid;
import static com.hp.autonomy.frontend.configuration.server.ServerConfigTest.IsAciParameter.aciParameter;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("CastToConcreteClass")
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DistributedConfigTest extends ConfigurationComponentTest<DistributedConfig> {
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
    public void testValidateWithDistributedFalse() {
        final ValidationResult<?> validationResultOne = new ValidationResult<>(true, "ValidationResultOne");
        final ServerConfig standard = mock(ServerConfig.class);

        Mockito.<ValidationResult<?>>when(standard.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultOne);

        final DistributedConfig distributedConfig = DistributedConfig.builder()
            .distributed(false)
            .standard(standard)
            .build();

        final ValidationResult<?> validationResultA = distributedConfig.validate(aciService, indexingService, processorFactory);

        verify(standard).validate(aciService, indexingService, processorFactory);

        assertThat(validationResultA, Matchers.equalTo(validationResultOne));
    }

    @Test
    public void testValidateWithDistributedTrue() {
        final ValidationResult<?> validationResultOne = new ValidationResult<>(true, "ValidationResultOne");
        final ValidationResult<?> validationResultTwo = new ValidationResult<>(true, "ValidationResultTwo");
        final ServerConfig dih = mock(ServerConfig.class);
        final ServerConfig dah = mock(ServerConfig.class);

        Mockito.<ValidationResult<?>>when(dah.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultOne);
        Mockito.<ValidationResult<?>>when(dih.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultTwo);

        final DistributedConfig distributedConfig = DistributedConfig.builder()
            .distributed(true)
            .dih(dih)
            .dah(dah)
            .build();

        final ValidationResult<?> validationResultDistributed = distributedConfig.validate(aciService, indexingService, processorFactory);

        verify(dah).validate(aciService, indexingService, processorFactory);
        verify(dih).validate(aciService, indexingService, processorFactory);

        assertThat(validationResultDistributed, is(valid()));

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails) validationResultDistributed.getData();

        assertThat(validationDetails.getDahValidationResult(), is(nullValue()));
        assertThat(validationDetails.getDihValidationResult(), is(nullValue()));
    }

    @Test
    public void testValidateWithDistributedTrueAndInvalidDahAndDihResults() {
        final ValidationResult<?> validationResultFail = new ValidationResult<>(false, "ValidationResultFail");
        final ServerConfig dih = mock(ServerConfig.class);
        final ServerConfig dah = mock(ServerConfig.class);

        Mockito.<ValidationResult<?>>when(dah.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultFail);
        Mockito.<ValidationResult<?>>when(dih.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultFail);

        final DistributedConfig distributedConfig = DistributedConfig.builder()
            .distributed(true)
            .dih(dih)
            .dah(dah)
            .build();

        final ValidationResult<?> validationResultDistributed = distributedConfig.validate(aciService, indexingService, processorFactory);

        verify(dah).validate(aciService, indexingService, processorFactory);
        verify(dih).validate(aciService, indexingService, processorFactory);

        assertThat(validationResultDistributed, is(not(valid())));

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails) validationResultDistributed.getData();

        assertThat(validationDetails.getDahValidationResult(), Matchers.is(validationResultFail));
        assertThat(validationDetails.getDihValidationResult(), Matchers.is(validationResultFail));
    }

    @Test
    public void testValidateWithDistributedTrueAndAnInvalidDahResult() {
        final ValidationResult<?> validationResultFail = new ValidationResult<>(false, "ValidationResultFail");
        final ValidationResult<?> validationResultSuccess = new ValidationResult<>(true, "ValidationResultSuccess");
        final ServerConfig dih = mock(ServerConfig.class);
        final ServerConfig dah = mock(ServerConfig.class);

        Mockito.<ValidationResult<?>>when(dah.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultFail);
        Mockito.<ValidationResult<?>>when(dih.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultSuccess);

        final DistributedConfig distributedConfig = DistributedConfig.builder()
            .distributed(true)
            .dih(dih)
            .dah(dah)
            .build();

        final ValidationResult<?> validationResultDistributed = distributedConfig.validate(aciService, indexingService, processorFactory);

        verify(dah).validate(aciService, indexingService, processorFactory);
        verify(dih).validate(aciService, indexingService, processorFactory);

        assertThat(validationResultDistributed, is(not(valid())));

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails) validationResultDistributed.getData();

        assertThat(validationDetails.getDahValidationResult(), Matchers.is(validationResultFail));
        assertThat(validationDetails.getDihValidationResult(), is(nullValue()));
    }

    @Test
    public void testValidateWithDistributedTrueAndAnInvalidDihResult() {

        final ValidationResult<?> validationResultFail = new ValidationResult<>(false, "ValidationResultFail");
        final ValidationResult<?> validationResultSuccess = new ValidationResult<>(true, "ValidationResultSuccess");
        final ServerConfig dih = mock(ServerConfig.class);
        final ServerConfig dah = mock(ServerConfig.class);

        Mockito.<ValidationResult<?>>when(dah.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultSuccess);
        Mockito.<ValidationResult<?>>when(dih.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultFail);

        final DistributedConfig distributedConfig = DistributedConfig.builder()
            .distributed(true)
            .dih(dih)
            .dah(dah)
            .build();

        final ValidationResult<?> validationResultDistributed = distributedConfig.validate(aciService, indexingService, processorFactory);

        verify(dah).validate(aciService, indexingService, processorFactory);
        verify(dih).validate(aciService, indexingService, processorFactory);

        assertThat(validationResultDistributed, is(not(valid())));

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails) validationResultDistributed.getData();

        assertThat(validationDetails.getDahValidationResult(), is(nullValue()));
        assertThat(validationDetails.getDihValidationResult(), Matchers.is(validationResultFail));
    }

    @Test
    public void testValidateWithDistributedTrueAndLanguageErrorStillValid() {
        final ValidationResult<?> validationResultOne = new ValidationResult<>(true, "ValidationResultOne");
        final ValidationResult<?> validationResultTwo = new ValidationResult<>(true, "ValidationResultTwo");
        final ServerConfig dih = mock(ServerConfig.class);
        final ServerConfig dah = mock(ServerConfig.class);

        Mockito.<ValidationResult<?>>when(dah.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultOne);
        Mockito.<ValidationResult<?>>when(dih.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultTwo);

        when(dah.toAciServerDetails()).thenReturn(mock(AciServerDetails.class));

        when(aciService.executeAction(
            any(AciServerDetails.class),
            argThat(SetContainingItems.isSetWithItems(aciParameter("action", "LanguageSettings"))),
            any()
        )).thenThrow(new AciErrorException());

        final DistributedConfig distributedConfig = DistributedConfig.builder()
            .distributed(true)
            .dih(dih)
            .dah(dah)
            .build();

        final ValidationResult<?> distributedValidationResult = distributedConfig.validate(aciService, indexingService, processorFactory);

        verify(dah).validate(aciService, indexingService, processorFactory);
        verify(dih).validate(aciService, indexingService, processorFactory);

        assertThat(distributedValidationResult, is(valid()));

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails) distributedValidationResult.getData();

        assertThat(validationDetails.getDahValidationResult(), is(valid()));
        assertThat(validationDetails.getDihValidationResult(), is(nullValue()));
    }

    @Override
    protected Class<DistributedConfig> getType() {
        return DistributedConfig.class;
    }

    @Override
    protected DistributedConfig constructComponent() {
        return DistributedConfig.builder()
                .distributed(false)
                .standard(ServerConfig.builder()
                        .host("localhost")
                        .port(9000)
                        .build())
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("/com/hp/autonomy/frontend/configuration/server/distributed.json"));
    }

    @Override
    protected void validateJson(final JsonContent<DistributedConfig> jsonContent) {
        jsonContent.assertThat().hasJsonPathBooleanValue("@.distributed", false);
        jsonContent.assertThat().hasJsonPathStringValue("@.standard.host", "localhost");
        jsonContent.assertThat().hasJsonPathNumberValue("@.standard.port", 9000);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<DistributedConfig> objectContent) {
        objectContent.assertThat().hasFieldOrPropertyWithValue("distributed", false);
        objectContent.assertThat().hasFieldOrProperty("standard").isNotNull();
        objectContent.assertThat().hasFieldOrProperty("dih").isNotNull();
        objectContent.assertThat().hasFieldOrProperty("dah").isNotNull();
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<DistributedConfig> objectContent) {
        final ServerConfig standard = objectContent.getObject().getStandard();
        assertThat(standard.getHost(), is("localhost"));
        assertThat(standard.getPort(), is(9000));
        objectContent.assertThat().hasFieldOrProperty("standard").isNotNull();
        objectContent.assertThat().hasFieldOrProperty("dih").isNotNull();
        objectContent.assertThat().hasFieldOrProperty("dah").isNotNull();
    }

    @Override
    protected void validateString(final String objectAsString) {
        assertTrue(objectAsString.contains("standard"));
    }
}
