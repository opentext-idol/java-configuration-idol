/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.StAXProcessor;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.nonaci.indexing.IndexingService;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.hp.autonomy.frontend.configuration.IsValidMatcher.valid;
import static com.hp.autonomy.frontend.configuration.ServerConfigTest.IsAciParameter.aciParameter;
import static com.hp.autonomy.frontend.configuration.SetContainingItems.isSetWithItems;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
public class DistributedConfigTest {

    private AciService aciService;
    private IndexingService indexingService;
    private IdolAnnotationsProcessorFactory processorFactory;

    @Before
    public void setUp() {
        aciService = mock(AciService.class);
        indexingService = mock(IndexingService.class);
        processorFactory = mock(IdolAnnotationsProcessorFactory.class);

        when(processorFactory.forClass(EmptyResponse.class)).thenReturn(mock(StAXProcessor.class));
        when(processorFactory.forClass(GetVersionResponse.class)).thenReturn(mock(StAXProcessor.class));
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

        assertThat(validationResultA, Matchers.<ValidationResult<?>>equalTo(validationResultOne));
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

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails)validationResultDistributed.getData();

        assertThat((ValidationResult<?>)validationDetails.getDahValidationResult(), is(nullValue()));
        assertThat((ValidationResult<?>)validationDetails.getDihValidationResult(), is(nullValue()));
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

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails)validationResultDistributed.getData();

        assertThat((ValidationResult<?>)validationDetails.getDahValidationResult(), Matchers.<ValidationResult<?>>is(validationResultFail));
        assertThat((ValidationResult<?>)validationDetails.getDihValidationResult(), Matchers.<ValidationResult<?>>is(validationResultFail));
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

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails)validationResultDistributed.getData();

        assertThat((ValidationResult<?>)validationDetails.getDahValidationResult(), Matchers.<ValidationResult<?>>is(validationResultFail));
        assertThat((ValidationResult<?>)validationDetails.getDihValidationResult(), is(nullValue()));
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

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails)validationResultDistributed.getData();

        assertThat((ValidationResult<?>)validationDetails.getDahValidationResult(), is(nullValue()));
        assertThat((ValidationResult<?>) validationDetails.getDihValidationResult(), Matchers.<ValidationResult<?>>is(validationResultFail));
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
                argThat(any(AciServerDetails.class)),
                argThat(isSetWithItems(aciParameter("action", "LanguageSettings"))),
                argThat(any(StAXProcessor.class))
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

        assertThat((ValidationResult<?>) validationDetails.getDahValidationResult(), is(not(valid())));
        assertThat((ValidationResult<?>) validationDetails.getDihValidationResult(), is(nullValue()));
    }
}
