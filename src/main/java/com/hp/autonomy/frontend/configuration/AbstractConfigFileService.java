package com.hp.autonomy.frontend.configuration;

import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.configuration.Config;
import lombok.extern.slf4j.Slf4j;

/*
 * $Id$
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author$ on $Date$
 */

/**
 * Reference implementation of {@link ConfigFileService}, which outputs configuration objects as JSON files.
 * An additional type bound is placed on the configuration object this class uses.
 *
 * This class requires that a default config file be available at runtime.
 *
 * Operations on the Config are thread safe.
 *
 * @param <T> The type of the Configuration object. If it extends {@link PasswordsConfig}, passwords will be encrypted
 *           and decrypted when the file is written and read respectively.  If it extends {@link LoginConfig}, a default
 *           login will be generated for the initial config file, and which will be removed on subsequent writes.
 *
 * @deprecated Use {@link AbstractAuthenticatingConfigFileService} instead
 */
@Slf4j
@Deprecated
public abstract class AbstractConfigFileService<T extends Config<T>> extends BaseConfigFileService<T> {

    @Override
    public T withHashedPasswords(final T config) {
        if(config instanceof LoginConfig<?>) {
            return ((LoginConfig<T>) config).withHashedPasswords();
        }

        return config;
    }

    @Override
    public T withoutDefaultLogin(final T config) {
        if(config instanceof LoginConfig<?>) {
            return ((LoginConfig<T>) config).withoutDefaultLogin();
        }

        return config;
    }

    @Override
    public T generateDefaultLogin(final T config) {
        if(config instanceof LoginConfig<?>) {
            return ((LoginConfig<T>) config).generateDefaultLogin();
        }

        return config;
    }
}
