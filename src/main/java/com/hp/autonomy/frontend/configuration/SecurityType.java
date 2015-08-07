package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.annotations.IdolDocument;
import com.autonomy.aci.client.annotations.IdolField;

/**
 * $Id$
 * <p/>
 * Copyright (c) 2013, Autonomy Systems Ltd.
 * <p/>
 * Last modified by $Author$ on $Date$
 */

/**
 * Class representing an IDOL security type.
 *
 * This class is annotated for use with {@link com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory}
 */
@IdolDocument("securitytype")
public class SecurityType {

	private String name;

	public String getName() {
		return name;
	}

	@IdolField("name")
	public void setName(final String name) {
		this.name = name;
	}
}
