/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.services.impl.AbstractStAXProcessor;
import com.autonomy.aci.client.services.impl.ErrorProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.util.Locale;

class PortsResponseProcessor extends AbstractStAXProcessor<PortsResponse> {

    private static final long serialVersionUID = -7902521754676336142L;

    private final String aciPortName;
    private final String indexPortName;
    private final String servicePortName;

    PortsResponseProcessor(final String aciPortName, final String servicePortName) {
        this(aciPortName, servicePortName, null);
    }

    PortsResponseProcessor(final String aciPortName, final String servicePortName, final String indexPortName) {
        this.aciPortName = aciPortName;
        this.indexPortName = indexPortName;
        this.servicePortName = servicePortName;
    }

    @Override
    public PortsResponse process(final XMLStreamReader xmlStreamReader) {
        try {
            if (isErrorResponse(xmlStreamReader)) {
                setErrorProcessor(new ErrorProcessor());
                processErrorResponse(xmlStreamReader);
            }

            final PortsResponse response = new PortsResponse();

            while(xmlStreamReader.hasNext()) {
                final int event = xmlStreamReader.next();

                if(event == XMLEvent.START_ELEMENT) {
                    final String localName = xmlStreamReader.getLocalName().toLowerCase(Locale.US);

                    if(localName.equals(aciPortName)) {
                        response.setAciPort(Integer.parseInt(xmlStreamReader.getElementText()));
                    }
                    else if(localName.equals(servicePortName)) {
                        response.setServicePort(Integer.parseInt(xmlStreamReader.getElementText()));
                    }
                    else if(localName.equals(indexPortName)) {
                        response.setIndexPort(Integer.parseInt(xmlStreamReader.getElementText()));
                    }
                }
            }

            return response;
        }
        catch(final XMLStreamException e) {
            throw new ProcessorException("Error reading ports");
        }
    }

}
