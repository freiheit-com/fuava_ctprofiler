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
package com.freiheit.fuava.ctprofiler.core;

/**
 * A full Path of nested calls to the Call Tree Profiler.
 *
 * @author klas
 */
public interface NestedTimerPath {

    /**
     * The name of the timer, regardless of its parents.
     *
     * <p>There may be multiple instances of {@link NestedTimerPath} with the
     *    same timer name, but different parents.</p>
     */
    public String getLeafTimerName();

    /**
     * The number of elements in the entire path up to this element
     * (i.e. number of nested timer names), usefull for indentation and such.
     */
    public int getLevel();

    /**
     * Get the parent of this path element.
     * @return the parent of this path, or null if there is no further parent.
     */
    public NestedTimerPath getParent();
}
