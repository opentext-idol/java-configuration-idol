/*
 * (c) Copyright 2013-2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.transport.AciServerDetails;
import org.mockito.ArgumentMatcher;

class IsAciServerDetails extends ArgumentMatcher<AciServerDetails> {
    private final String host;
    private final int port;

    IsAciServerDetails(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean matches(final Object o) {
        if (!(o instanceof AciServerDetails)) {
            return false;
        }

        final AciServerDetails serverDetails = (AciServerDetails) o;

        boolean result = true;

        if (host != null) {
            result = host.equals(serverDetails.getHost());
        }

        if (port != -1) {
            result = result && port == serverDetails.getPort();
        }

        return result;
    }
}
