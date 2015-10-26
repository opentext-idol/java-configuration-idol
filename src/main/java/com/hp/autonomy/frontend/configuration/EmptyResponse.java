/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.annotations.IdolDocument;

/**
 * Class which replaces DontCareAsLongAsItsNotAnErrorProcessor - if there's an error, the response won't be read
 */
@IdolDocument("responsedata")
class EmptyResponse {
}
