package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import org.mockito.ArgumentMatcher;

class IsAciServerDetails extends ArgumentMatcher<AciServerDetails> {

    private final String host;
    private final int port;

    private IsAciServerDetails() {
        this(null, -1);
    }

    private IsAciServerDetails(final int port) {
        this(null, port);
    }

    private IsAciServerDetails(final String host) {
        this(host, -1);
    }

    IsAciServerDetails(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean matches(final Object o) {
        if(!(o instanceof AciServerDetails)) {
            return false;
        }

        final AciServerDetails serverDetails = (AciServerDetails) o;

        boolean result = true;

        if(host != null) {
            result = host.equals(serverDetails.getHost());
        }

        if(port != -1) {
            result = result && port == serverDetails.getPort();
        }

        return result;
    }
}
