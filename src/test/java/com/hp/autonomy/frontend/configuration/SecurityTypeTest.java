package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactoryImpl;
import com.autonomy.aci.client.services.StAXProcessor;
import com.autonomy.test.unit.TestUtils;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/*
 * $Id:$
 *
 * Copyright (c) 2014, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
public class SecurityTypeTest {

    @Test
    public void testSingleSecurityType() throws XMLStreamException {
        final IdolAnnotationsProcessorFactory factory = new IdolAnnotationsProcessorFactoryImpl();
        final StAXProcessor<List<SecurityType>> processor = factory.listProcessorForClass(SecurityType.class);
        final XMLStreamReader reader = TestUtils.getResourceAsXMLStreamReader("/com/hp/autonomy/frontend/configuration/community-single-security-type.xml");

        final List<SecurityType> output = processor.process(reader);

        assertThat(output, hasSize(1));
        assertThat(output.get(0).getName(), is("autonomy"));
    }

    @Test
    public void testMultipleSecurityTypes() throws XMLStreamException {
        final IdolAnnotationsProcessorFactory factory = new IdolAnnotationsProcessorFactoryImpl();
        final StAXProcessor<List<SecurityType>> processor = factory.listProcessorForClass(SecurityType.class);
        final XMLStreamReader reader = TestUtils.getResourceAsXMLStreamReader("/com/hp/autonomy/frontend/configuration/community-multiple-security-types.xml");

        final List<SecurityType> output = processor.process(reader);

        assertThat(output, hasSize(2));
        assertThat(output.get(0).getName(), is("autonomy"));
        assertThat(output.get(1).getName(), is("ldap"));
    }

}
