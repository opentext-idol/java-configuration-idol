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

/**
 * Enumeration of IDOL components which can be configured. The enum constants correspond to autn:producttypecsv field of
 * the GetVersion action.
 */
public enum ProductType {

    /**
     * Represents Answer Server
     */
    ANSWERSERVER("Answer Server"),

    /**
     * Represents Content/Suir/AXE/DRE
     */
    AXE("Content"),

    /**
     * Represents Indextasks/CAP
     */
    CAP("IndexTasks"),

    /**
     * Represents Category/Laune/Classserver
     */
    CLASSSERVER("Category"),

    /**
     * Represents DAH
     */
    DAH("DAH"),

    /**
     * Represents DIH
     */
    DIH("DIH"),

    /**
     * Represents Distributed Connector
     */
    DISTRIBUTED_CONNECTOR("Distributed Connector"),

    /**
     * Represents IDOL Server
     */
    IDOLPROXY("IDOL Proxy"),

    /**
     * Represents QMS
     */
    QMS("Query Manipulation Service"),

    /**
     * Represents Coordinator
     */
    SERVICECOORDINATOR("Coordinator"),

    /**
     * Represents Statsserver
     */
    STATS("Stats Server"),

    /**
     * Represents Community/Nore/UAServer
     */
    UASERVER("Community"),

    /**
     * Represents View
     */
    VIEW("View");

    private final String friendlyName;

    ProductType(final String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /**
     * @return A friendly name for the product which more closely reflects the current branding.
     */
    public String getFriendlyName() {
        return friendlyName;
    }
}
