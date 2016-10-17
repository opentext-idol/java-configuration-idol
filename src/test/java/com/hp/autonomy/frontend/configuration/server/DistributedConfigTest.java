/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.nonaci.indexing.IndexingService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.GetVersionResponseData;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.hp.autonomy.frontend.configuration.server.IsValidMatcher.valid;
import static com.hp.autonomy.frontend.configuration.server.ServerConfigTest.IsAciParameter.aciParameter;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("CastToConcreteClass")
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DistributedConfigTest {
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

    @Before
    public void setUp() {
        when(processorFactory.getVoidProcessor()).thenReturn(voidProcessor);
        when(processorFactory.getResponseDataProcessor(GetVersionResponseData.class)).thenReturn(getVersionProcessor);
    }

    @Test
    public void testValidateWithDistributedFalse() {
        final ValidationResult<?> validationResultOne = new ValidationResult<>(true, "ValidationResultOne");
        final ServerConfig standard = mock(ServerConfig.class);

        Mockito.<ValidationResult<?>>when(standard.validate(aciService, indexingService, processorFactory)).thenReturn(validationResultOne);

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
            .setDistributed(false)
            .setStandard(standard)
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

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
            .setDistributed(true)
            .setDih(dih)
            .setDah(dah)
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

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
            .setDistributed(true)
            .setDih(dih)
            .setDah(dah)
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

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
            .setDistributed(true)
            .setDih(dih)
            .setDah(dah)
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

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
            .setDistributed(true)
            .setDih(dih)
            .setDah(dah)
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
    public void testValidateWithDistributedTrueAndLanguageErrorExceptionThrown() {
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

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
            .setDistributed(true)
            .setDih(dih)
            .setDah(dah)
            .build();

        final ValidationResult<?> distributedValidationResult = distributedConfig.validate(aciService, indexingService, processorFactory);

        verify(dah).validate(aciService, indexingService, processorFactory);
        verify(dih).validate(aciService, indexingService, processorFactory);

        assertThat(distributedValidationResult, is(not(valid())));

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails) distributedValidationResult.getData();

        assertThat(validationDetails.getDahValidationResult(), is(not(valid())));
        assertThat(validationDetails.getDihValidationResult(), is(nullValue()));
    }
}
