/*
 * Copyright 2013-2015 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.configuration.server;

import com.autonomy.aci.client.transport.AciServerDetails;
import org.mockito.ArgumentMatcher;

class IsAciServerDetails implements ArgumentMatcher<AciServerDetails> {
    private final String host;
    private final int port;

    IsAciServerDetails(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean matches(final AciServerDetails serverDetails) {
        if (serverDetails == null) {
            return false;
        }

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
