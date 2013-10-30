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
import com.freiheit.fuava.ctprofiler.core.NestedTimerPath;
import com.freiheit.fuava.ctprofiler.core.TimerStatistics;


/**
 * Container for a path and the corresponding call value.
 * @author klas.kalass@freiheit.com (initial creation)
 * @author $Author: klas $ (last modification)
 * @version $Date: 2009-08-20 14:21:18 +0200 (Do, 20. Aug 2009) $
 */
final class PathValue {
    private final Layer _l;
    private final NestedTimerPath _p;
    private final TimerStatistics _c;

    /**
     * Creates a new PathValue.
     * @param l the layer, may be null if not explicitely specified.
     * @param p the path
     * @param c the call info
     */
    PathValue(final Layer l, final NestedTimerPath p, final TimerStatistics c) {
        this._p = p;
        this._c = c;
        this._l = l;
    }

    /**
     * @return the layer explicitely specified, or null.
     */
    public Layer getLayer() {
        return this._l;
    }

    public NestedTimerPath getPath() {
        return this._p;
    }

    public TimerStatistics getCall() {
        return this._c;
    }
}