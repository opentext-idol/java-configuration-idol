package com.hp.autonomy.frontend.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * $Id
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author on $Date 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class PortsResponse {

    private int aciPort = 0,
                indexPort = 0,
                servicePort = 0;

}
