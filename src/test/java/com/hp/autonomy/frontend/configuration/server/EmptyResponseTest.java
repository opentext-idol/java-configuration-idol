/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactoryImpl;
import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.StAXProcessor;
import com.hp.autonomy.frontend.configuration.server.EmptyResponse;
import com.hp.autonomy.test.xml.XmlTestUtils;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class EmptyResponseTest {

    private IdolAnnotationsProcessorFactory processorFactory;

    @Before
    public void setUp() {
        processorFactory = new IdolAnnotationsProcessorFactoryImpl();
    }

    @Test
    public void testEmptyResponse() throws XMLStreamException {
        final StAXProcessor<List<EmptyResponse>> processor = processorFactory.listProcessorForClass(EmptyResponse.class);

        final EmptyResponse returnValue = processor.process(XmlTestUtils.getResourceAsXMLStreamReader("/com/hp/autonomy/frontend/configuration/getVersionResponse.xml")).get(0);

        assertThat(returnValue, is(notNullValue()));
    }

    @Test(expected = AciErrorException.class)
    public void testEmptyResponseThrowsOnErrorResponses() throws XMLStreamException {
        final StAXProcessor<List<EmptyResponse>> processor = processorFactory.listProcessorForClass(EmptyResponse.class);

        processor.process(XmlTestUtils.getResourceAsXMLStreamReader("/com/hp/autonomy/frontend/configuration/errorResponse.xml")).get(0);
    }

}
