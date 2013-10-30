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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.freiheit.fuava.ctprofiler.core.Statistics;
import com.freiheit.fuava.ctprofiler.core.TimerStatistics;


/**
 * Measuring the number and duration of calls.
 * @author klas.kalass@freiheit.com (initial creation)
 * @author $Author: klas $ (last modification)
 * @version $Date: 2009-08-20 14:21:18 +0200 (Do, 20. Aug 2009) $
 */
final class Call implements TimerStatistics {
    private final int _num;
    private final long _totalNanos;
    private final Collection<Statistics> _subStates;

    /**
     * Creates an empty call.
     */
    public Call() {
        this._num = 0;
        this._totalNanos = 0;
        this._subStates = Collections.emptyList();
    }

    /**
     * Private constructor for use by {@link #add(long)}.
     * @param num the number of calls
     * @param totalNanos the current total execution time for this type of call
     * @param subStates all substates separately for this kind of call
     */
    private Call(final int num, final long totalNanos, final Collection<Statistics> subStates) {
        assert subStates != null;
        this._num = num;
        this._totalNanos = totalNanos;
        this._subStates = subStates;
    }

    /**
     * Add a call.
     * @param durationNanos the duration of the current call
     * @param subState the Call Tree of a sub-task that was executed in a different thread.
     * @return a new call instance with the result of the addition
     */
    public Call add(final long durationNanos, final Statistics subState) {
        final Collection<Statistics> subStates;
        if (subState == null) {
            subStates = _subStates;
        } else {
            subStates = new ArrayList<Statistics>(_subStates);
            subStates.add(subState);
        }
        return new Call(this._num + 1, this._totalNanos + durationNanos, subStates);
    }

    @Override
    public Collection<Statistics> getSubStatistics() {
        return _subStates;
    }

    @Override
    public long getTotalNanos() {
        return this._totalNanos;
    }

    @Override
    public String toString() {
        return "Calls[num: " + _num + ", totalNanos: " + _totalNanos + "]";
    }

    @Override
    public int getNumberOfCalls() {
        return this._num;
    }
}