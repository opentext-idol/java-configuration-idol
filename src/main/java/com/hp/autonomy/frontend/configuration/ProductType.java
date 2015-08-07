package com.hp.autonomy.frontend.configuration;

/*
 * $Id:$
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
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
