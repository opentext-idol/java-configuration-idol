package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.actions.DontCareAsLongAsItsNotAnErrorProcessor;
import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.nonaci.indexing.IndexingService;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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
import static org.mockito.Mockito.*;

/*
 * $Id:$
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
@Slf4j
public class DistributedConfigTest {

    private AciService aciService;
    private IndexingService indexingService;

    @Before
    public void setUp() {
        aciService = mock(AciService.class);
        indexingService = mock(IndexingService.class);
    }

    @Test
    public void testValidateWithDistributedFalse() {
        final ValidationResult<?> validationResultOne = new ValidationResult<>(true, "ValidationResultOne");
        final ServerConfig standard = mock(ServerConfig.class);

        Mockito.<ValidationResult<?>>when(standard.validate(aciService, indexingService)).thenReturn(validationResultOne);

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
                .setDistributed(false)
                .setStandard(standard)
                .build();

        final ValidationResult<?> validationResultA = distributedConfig.validate(aciService, indexingService);

        verify(standard).validate(aciService, indexingService);

        assertThat(validationResultA, Matchers.<ValidationResult<?>>equalTo(validationResultOne));
    }

    @Test
    public void testValidateWithDistributedTrue() {
        final ValidationResult<?> validationResultOne = new ValidationResult<>(true, "ValidationResultOne");
        final ValidationResult<?> validationResultTwo = new ValidationResult<>(true, "ValidationResultTwo");
        final ServerConfig dih = mock(ServerConfig.class);
        final ServerConfig dah = mock(ServerConfig.class);

        Mockito.<ValidationResult<?>>when(dah.validate(aciService, indexingService)).thenReturn(validationResultOne);
        Mockito.<ValidationResult<?>>when(dih.validate(aciService, indexingService)).thenReturn(validationResultTwo);

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
                .setDistributed(true)
                .setDih(dih)
                .setDah(dah)
                .build();

        final ValidationResult<?> validationResultDistributed = distributedConfig.validate(aciService, indexingService);

        verify(dah).validate(aciService, indexingService);
        verify(dih).validate(aciService, indexingService);

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

        Mockito.<ValidationResult<?>>when(dah.validate(aciService, indexingService)).thenReturn(validationResultFail);
        Mockito.<ValidationResult<?>>when(dih.validate(aciService, indexingService)).thenReturn(validationResultFail);

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
                .setDistributed(true)
                .setDih(dih)
                .setDah(dah)
                .build();

        final ValidationResult<?> validationResultDistributed = distributedConfig.validate(aciService, indexingService);

        verify(dah).validate(aciService, indexingService);
        verify(dih).validate(aciService, indexingService);

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

        Mockito.<ValidationResult<?>>when(dah.validate(aciService, indexingService)).thenReturn(validationResultFail);
        Mockito.<ValidationResult<?>>when(dih.validate(aciService, indexingService)).thenReturn(validationResultSuccess);

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
                .setDistributed(true)
                .setDih(dih)
                .setDah(dah)
                .build();

        final ValidationResult<?> validationResultDistributed = distributedConfig.validate(aciService, indexingService);

        verify(dah).validate(aciService, indexingService);
        verify(dih).validate(aciService, indexingService);

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

        Mockito.<ValidationResult<?>>when(dah.validate(aciService, indexingService)).thenReturn(validationResultSuccess);
        Mockito.<ValidationResult<?>>when(dih.validate(aciService, indexingService)).thenReturn(validationResultFail);

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
                .setDistributed(true)
                .setDih(dih)
                .setDah(dah)
                .build();

        final ValidationResult<?> validationResultDistributed = distributedConfig.validate(aciService, indexingService);

        verify(dah).validate(aciService, indexingService);
        verify(dih).validate(aciService, indexingService);

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

        Mockito.<ValidationResult<?>>when(dah.validate(aciService, indexingService)).thenReturn(validationResultOne);
        Mockito.<ValidationResult<?>>when(dih.validate(aciService, indexingService)).thenReturn(validationResultTwo);

        when(dah.toAciServerDetails()).thenReturn(mock(AciServerDetails.class));

        when(aciService.executeAction(
                argThat(any(AciServerDetails.class)),
                argThat(isSetWithItems(aciParameter("action", "LanguageSettings"))),
                argThat(any(DontCareAsLongAsItsNotAnErrorProcessor.class))
        )).thenThrow(new AciErrorException());

        final DistributedConfig distributedConfig = new DistributedConfig.Builder()
                .setDistributed(true)
                .setDih(dih)
                .setDah(dah)
                .build();

        final ValidationResult<?> distributedValidationResult = distributedConfig.validate(aciService, indexingService);

        verify(dah).validate(aciService, indexingService);
        verify(dih).validate(aciService, indexingService);

        assertThat(distributedValidationResult, is(not(valid())));

        final DistributedConfig.DistributedValidationResultDetails validationDetails = (DistributedConfig.DistributedValidationResultDetails) distributedValidationResult.getData();

        assertThat((ValidationResult<?>) validationDetails.getDahValidationResult(), is(not(valid())));
        assertThat((ValidationResult<?>) validationDetails.getDihValidationResult(), is(nullValue()));
    }
}
