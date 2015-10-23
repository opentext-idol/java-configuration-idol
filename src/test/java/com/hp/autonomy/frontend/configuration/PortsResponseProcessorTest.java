/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.hp.autonomy.test.xml.XmlTestUtils;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PortsResponseProcessorTest {

    @Test
    public void testProcessWithCoordinator() throws XMLStreamException {
        final PortsResponseProcessor portsResponseProcessor = new PortsResponseProcessor("aciport", "serviceport", "indexport");

        final XMLStreamReader xmlStreamReader = XmlTestUtils.getResourceAsXMLStreamReader("/com/hp/autonomy/frontend/configuration/coordinatorGetStatusResponse.xml");

        final PortsResponse portsResponse = portsResponseProcessor.process(xmlStreamReader);

        assertThat(portsResponse.getAciPort(), is(40200));
        assertThat(portsResponse.getIndexPort(), is(40201));
        assertThat(portsResponse.getServicePort(), is(40202));
    }

    @Test
    public void testProcessWithGetChildren() throws XMLStreamException {
        final PortsResponseProcessor portsResponseProcessor = new PortsResponseProcessor("autn:port", "autn:serviceport");

        final XMLStreamReader xmlStreamReader = XmlTestUtils.getResourceAsXMLStreamReader("/com/hp/autonomy/frontend/configuration/getChildrenResponse.xml");

        final PortsResponse portsResponse = portsResponseProcessor.process(xmlStreamReader);

        assertThat(portsResponse.getAciPort(), is(9002));
        assertThat(portsResponse.getIndexPort(), is(0));
        assertThat(portsResponse.getServicePort(), is(9003));
    }
}
