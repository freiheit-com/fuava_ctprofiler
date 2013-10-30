/**
 * Copyright 2013 freiheit.com technologies gmbh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.freiheit.fuava.ctprofiler.core.impl;

import com.freiheit.fuava.ctprofiler.core.Layer;
import com.freiheit.fuava.ctprofiler.core.TimeKeeper;

/**
 * A {@link TimeKeeper} that is always disabled and thus does not do anything when called.
 *
 * @author klas
 */
final class DisabledTimeKeeper implements TimeKeeper {
    private static final TimeKeeper INSTANCE = new DisabledTimeKeeper();

    /**
     * Singleton constructor.
     */
    private DisabledTimeKeeper() {
    }


    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void begin(final String timerName) {
    }

    @Override
    public void begin(final Layer layer, final String timerName) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(final String timerName) {
    }

    @Override
    public void end(final Layer layer, final String timerName) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T proxy(final java.lang.Class<T> iface, final T instance) {
        return instance;
    }

    @Override
    public Object proxy(final Object instance) {
        return instance;
    }

    @Override
    public Object proxy(final Layer layer, final Object instance) {
        return instance;
    }

    @Override
    public <T> T proxy(final Layer layer, final java.lang.Class<T> iface, final T instance) {
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, T2 extends T> T2 proxyGeneric(final java.lang.Class<T> iface, final T2 instance) {
        return instance;
    }

    public static TimeKeeper getInstance() {
        return INSTANCE;
    }
}
