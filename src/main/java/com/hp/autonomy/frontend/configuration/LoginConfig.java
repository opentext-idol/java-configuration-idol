/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

/**
 * Interface representing a config that contains a {@link Login}
 * @param <T> The type of the config.
 * @deprecated Use {@link AuthenticationConfig} instead
 */
@Deprecated
public interface LoginConfig<T> extends AuthenticatingConfig<T> {

    Login getLogin();

}
