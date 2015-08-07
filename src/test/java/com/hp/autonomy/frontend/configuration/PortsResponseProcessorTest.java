package com.hp.autonomy.frontend.configuration;

import com.autonomy.test.unit.TestUtils;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * $Id:$
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
public class PortsResponseProcessorTest {

    @Test
    public void testProcessWithCoordinator() throws XMLStreamException {
        final PortsResponseProcessor portsResponseProcessor = new PortsResponseProcessor("aciport", "serviceport", "indexport");

        final XMLStreamReader xmlStreamReader = TestUtils.getResourceAsXMLStreamReader("/com/hp/autonomy/frontend/configuration/coordinatorGetStatusResponse.xml");

        final PortsResponse portsResponse = portsResponseProcessor.process(xmlStreamReader);

        assertThat(portsResponse.getAciPort(), is(40200));
        assertThat(portsResponse.getIndexPort(), is(40201));
        assertThat(portsResponse.getServicePort(), is(40202));
    }

    @Test
    public void testProcessWithGetChildren() throws XMLStreamException {
        final PortsResponseProcessor portsResponseProcessor = new PortsResponseProcessor("autn:port", "autn:serviceport");

        final XMLStreamReader xmlStreamReader = TestUtils.getResourceAsXMLStreamReader("/com/hp/autonomy/frontend/configuration/getChildrenResponse.xml");

        final PortsResponse portsResponse = portsResponseProcessor.process(xmlStreamReader);

        assertThat(portsResponse.getAciPort(), is(9002));
        assertThat(portsResponse.getIndexPort(), is(0));
        assertThat(portsResponse.getServicePort(), is(9003));
    }
}
