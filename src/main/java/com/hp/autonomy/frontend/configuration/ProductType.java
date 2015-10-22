/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

public enum ProductType {

    AXE("Content"),
    CAP("IndexTasks"),
    CLASSSERVER("Category"),
    DAH("DAH"),
    DIH("DIH"),
    IDOLPROXY("IDOL Proxy"),
    QMS("Query Manipulation Service"),
    SERVICECOORDINATOR("Coordinator"),
    STATS("Stats Server"),
    UASERVER("Community"),
    VIEW("View");

    private final String friendlyName;

    private ProductType(final String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
