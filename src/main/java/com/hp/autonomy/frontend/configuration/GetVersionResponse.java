/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.annotations.IdolBuilder;
import com.autonomy.aci.client.annotations.IdolBuilderBuild;
import com.autonomy.aci.client.annotations.IdolDocument;
import com.autonomy.aci.client.annotations.IdolField;
import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@IdolDocument("responsedata")
@Data
public class GetVersionResponse {

    private final Set<String> productTypes;

    private GetVersionResponse(final Set<String> productTypes) {
        this.productTypes = productTypes;
    }

    @IdolDocument("responsedata")
    @IdolBuilder
    public static class Builder {

        private Set<String> productTypes;

        @IdolField("autn:producttypecsv")
        public Builder setProductTypes(final String productTypes) {
            this.productTypes = new HashSet<>(Arrays.asList(productTypes.split(",")));
            return this;
        }

        @IdolBuilderBuild
        public GetVersionResponse build() {
            return new GetVersionResponse(productTypes);
        }

    }

}
