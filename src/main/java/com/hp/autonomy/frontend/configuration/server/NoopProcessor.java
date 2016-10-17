/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciResponseInputStream;

class NoopProcessor implements Processor<Boolean> {
    private static final long serialVersionUID = -3821182089107701210L;

    @Override
    public Boolean process(final AciResponseInputStream aciResponse) {
        return aciResponse.getStatusCode() == 200;
    }
}
