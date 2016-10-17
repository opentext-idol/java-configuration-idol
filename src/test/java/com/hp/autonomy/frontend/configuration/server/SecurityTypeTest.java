/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactoryImpl;
import com.autonomy.aci.client.services.StAXProcessor;
import com.hp.autonomy.frontend.configuration.server.SecurityType;
import com.hp.autonomy.test.xml.XmlTestUtils;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class SecurityTypeTest {

    @Test
    public void testSingleSecurityType() throws XMLStreamException {
        final IdolAnnotationsProcessorFactory factory = new IdolAnnotationsProcessorFactoryImpl();
        final StAXProcessor<List<SecurityType>> processor = factory.listProcessorForClass(SecurityType.class);
        final XMLStreamReader reader = XmlTestUtils.getResourceAsXMLStreamReader("/com/hp/autonomy/frontend/configuration/community-single-security-type.xml");

        final List<SecurityType> output = processor.process(reader);

        assertThat(output, hasSize(1));
        assertThat(output.get(0).getName(), is("autonomy"));
    }

    @Test
    public void testMultipleSecurityTypes() throws XMLStreamException {
        final IdolAnnotationsProcessorFactory factory = new IdolAnnotationsProcessorFactoryImpl();
        final StAXProcessor<List<SecurityType>> processor = factory.listProcessorForClass(SecurityType.class);
        final XMLStreamReader reader = XmlTestUtils.getResourceAsXMLStreamReader("/com/hp/autonomy/frontend/configuration/community-multiple-security-types.xml");

        final List<SecurityType> output = processor.process(reader);

        assertThat(output, hasSize(2));
        assertThat(output.get(0).getName(), is("autonomy"));
        assertThat(output.get(1).getName(), is("ldap"));
    }

}
