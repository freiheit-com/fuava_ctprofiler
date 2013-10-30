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

import java.io.IOException;
import java.util.Collections;

import com.freiheit.fuava.ctprofiler.core.CallTreeProfiler;
import com.freiheit.fuava.ctprofiler.core.Layer;
import com.freiheit.fuava.ctprofiler.core.Statistics;

/**
 * A very simplistic CallTreeProfiler that is always disabled and thus does nothing when called.
 * @author klas
 *
 */
class DisabledCallTreeProfiler implements CallTreeProfiler {

    public static CallTreeProfiler getInstance() {
        return new DisabledCallTreeProfiler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void begin(final String timerName, final long startTimeNanos) {
        // ignore
    }

    @Override
    public void begin(final Layer layer, final String timerName, final long startTimeNanos) {
        // ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        // ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(final String timerName, final long endTimeNanos) {
        // ignore
    }

    @Override
    public void end(final Layer layer, final String timerName, final long endTimeNanos) {
        // ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(final String timerName, final long endTimeNanos, final Statistics state) {
        // ignore
    }

    @Override
    public boolean isEnabled() {
        // disabled => false
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Appendable> T renderThreadStateAsText(final T buffer) throws IOException {
        // ignore
        return buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Appendable> T renderThreadStateAsXml(final T buffer) throws IOException {
        return buffer;
    }

    @Override
    public Statistics getStatistics() {
        final Thread currentThread = Thread.currentThread();
        return ThreadStatisticsImpl.getInstance(currentThread.getId(), currentThread.getName(), Collections.<PathWithLayer, Call>emptyMap());
    }
}
