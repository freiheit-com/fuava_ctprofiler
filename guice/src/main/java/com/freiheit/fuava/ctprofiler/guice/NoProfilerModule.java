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


/**
 * A Guice module that provides CallTreeProfiler implementation that does not do any profiling.
 *
 * @author klas (initial creation)
 */
class NoProfilerModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(CallTreeProfiler.class).toInstance(ProfilerFactory.getDisabledProfiler());
        bind(TimeKeeper.class).toInstance(ProfilerFactory.getDisabledTimeKeeper());
    }
}
