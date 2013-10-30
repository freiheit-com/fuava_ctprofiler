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
package com.freiheit.fuava.ctprofiler.guice;

import com.freiheit.fuava.ctprofiler.core.CallTreeProfiler;
import com.freiheit.fuava.ctprofiler.core.TimeKeeper;
import com.freiheit.fuava.ctprofiler.core.impl.ProfilerFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;


/**
 * Guice module for binding the needed CallTreeProfiler implementation.
 *
 * @author klas (initial creation)
 */
class ProfilerModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        // further bindings are added via the Provider methods.
    }

    /**
     * Provide a call tree profiler.
     */
    @Provides @Singleton
    public CallTreeProfiler provideCallTreeProfiler() {
        return ProfilerFactory.createProfiler();
    }

    /**
     * Provide a simple call tree profiler.
     */
    @Provides @Singleton
    public TimeKeeper provideTimeKeeper(final CallTreeProfiler callTreeProfiler) {
        return ProfilerFactory.createTimeKeeper(callTreeProfiler);
    }
}
