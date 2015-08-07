package com.hp.autonomy.frontend.configuration;

/*
 * $Id:$
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */

import com.hp.autonomy.frontend.configuration.AuthenticatingConfig;

/**
 * Interface representing a config that contains a {@link Login}
 * @param <T> The type of the config.
 * @deprecated Use {@link AuthenticationConfig} instead
 */
@Deprecated
public interface LoginConfig<T> extends AuthenticatingConfig<T> {

    Login getLogin();

}
