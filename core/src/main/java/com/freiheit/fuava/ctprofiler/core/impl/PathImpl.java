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

import java.util.Arrays;

import com.freiheit.fuava.ctprofiler.core.NestedTimerPath;

/**
 * Abstraction for the path of the measurement points.
 */
class PathImpl implements NestedTimerPath {
    public static final NestedTimerPath ROOT = new NestedTimerPath() {
        @Override
        public int getLevel() {
            return 0;
        }
        @Override
        public String getLeafTimerName() {
            return "";
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public NestedTimerPath getParent() {
            return null;
        }
    };

    private final String[] _p;

    /**
     * Only for usage by the ROOT implementation.
     */
    PathImpl() {
        this._p = new String[0];
    }

    /**
     * Creates a path for the given string array.
     * @param p the string array which describes the path
     */
    PathImpl(final String[] p) {
        assert p.length > 0;
        this._p = p;
    }

    /**
     * Get a path instance for the given path.
     * @param p the path as a string array
     * @return a path instance
     */
    public static NestedTimerPath getInstance(final String[] p) {
        if (p == null || p.length == 0) {
            return ROOT;
        }
        return new PathImpl(p);
    }

    @Override
    public String getLeafTimerName() {
        return this._p[this._p.length - 1];
    }

    @Override
    public int getLevel() {
        return this._p.length;
    }

    /**
     * Get the parent of this path.
     * @return the parent of this path
     */
    @Override
    public NestedTimerPath getParent() {
        final String[] parent = new String[this._p.length - 1];
        for (int i = 0; i < parent.length; i++) {
            parent[i] = this._p[i];
        }
        return PathImpl.getInstance(parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof PathImpl) {
            final PathImpl path = (PathImpl)obj;
            return Arrays.equals(this._p, path._p);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this._p);
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Arrays.toString(this._p);
    }
}