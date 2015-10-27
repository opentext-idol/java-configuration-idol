/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Reference implementation of {@link ConfigFileService}, which outputs configuration objects as JSON files.
 * An additional type bound is placed on the configuration object this class uses.
 * <p/>
 * This class requires that a default config file be available at runtime.
 * <p/>
 * Operations on the Config are thread safe.
 * @param <T> The type of the Configuration object. If it extends {@link PasswordsConfig}, passwords will be encrypted
 * and decrypted when the file is written and read respectively.  If it extends {@link LoginConfig}, a default
 * login will be generated for the initial config file, and which will be removed on subsequent writes.
 * @deprecated Use {@link AbstractAuthenticatingConfigFileService} instead
 */
@Slf4j
@Deprecated
public abstract class AbstractConfigFileService<T extends Config<T>> extends BaseConfigFileService<T> {

    @Override
    public T withHashedPasswords(final T config) {
        if (config instanceof LoginConfig<?>) {
            return ((LoginConfig<T>) config).withHashedPasswords();
        }

        return config;
    }

    @Override
    public T withoutDefaultLogin(final T config) {
        if (config instanceof LoginConfig<?>) {
            return ((LoginConfig<T>) config).withoutDefaultLogin();
        }

        return config;
    }

    @Override
    public T generateDefaultLogin(final T config) {
        if (config instanceof LoginConfig<?>) {
            return ((LoginConfig<T>) config).generateDefaultLogin();
        }

        return config;
    }
}
