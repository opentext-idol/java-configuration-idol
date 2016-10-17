/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.annotations.IdolDocument;
import com.autonomy.aci.client.annotations.IdolField;

/**
 * Class representing an IDOL security type.
 * <p/>
 * This class is annotated for use with {@link com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory}
 */
@IdolDocument("securitytype")
public class SecurityType {

    private String name;

    public String getName() {
        return name;
    }

    @IdolField("name")
    public void setName(final String name) {
        this.name = name;
    }
}
