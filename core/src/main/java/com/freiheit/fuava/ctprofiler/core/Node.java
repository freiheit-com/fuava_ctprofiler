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

import java.util.Collection;

/**
 * Represents a Node in the CallTree
 * @author Klas Kalass (klas.kalass@freiheit.com) (initial creation)
 */
public interface Node {
    /**
     * Each node may be associated with a Layer, to help with layer-specific statistics.
     *
     * A node will inherit it's parents layer if it has none explicitely specified during call execution.
     */
    Layer getLayer();

    /**
     * The Path to this node.
     *
     * @return never null
     */
    NestedTimerPath getPath();

    /**
     * The call statistics for this node.
     *
     * @return never null
     */
    TimerStatistics getTimerStatistics();

    /**
     * The child nodes.
     *
     * @return never null
     */
    Collection<Node> getChildren();
}
