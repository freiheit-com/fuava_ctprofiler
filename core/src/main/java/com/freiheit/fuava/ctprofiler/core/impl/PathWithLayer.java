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

/**
 * @author Klas Kalass (klas.kalass@freiheit.com) (initial creation)
 */
class PathWithLayer {
    private final NestedTimerPath path;
    private final Layer layer;
    public PathWithLayer(final Layer layer, final NestedTimerPath path) {
        this.layer = layer;
        this.path = path;
    }

    public Layer getLayer() {
        return layer;
    }

    public NestedTimerPath getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof PathWithLayer) {
            final PathWithLayer p = (PathWithLayer)obj;
            return path.equals(p.path) && layer == p.layer;
        }
        return false;
    }

    @Override
    public String toString() {
        return path + "[" + layer + "]";
    }
}
