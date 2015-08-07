package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciResponseInputStream;

class NoopProcessor implements Processor<Boolean> {
    private static final long serialVersionUID = -3821182089107701210L;

    @Override
    public Boolean process(final AciResponseInputStream aciResponse) {
        return aciResponse.getStatusCode() == 200;
    }
}
