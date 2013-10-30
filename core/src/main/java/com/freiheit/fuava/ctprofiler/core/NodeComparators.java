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

import java.util.Comparator;


/**
 * Common comparators to compare {@link Node} instances to each other.
 *
 * @author Klas Kalass (klas.kalass@freiheit.com) (initial creation)
 */
public final class NodeComparators {

    private static final DurationComparator DURATION_COMPARATOR = new DurationComparator();

    private static final class DurationComparator implements Comparator<Node> {
        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(final Node o1, final Node o2) {
            final long n1 = o1.getTimerStatistics().getTotalNanos();
            final long n2 = o2.getTimerStatistics().getTotalNanos();
            return n1 < n2 ? 1 : (n1 > n2) ? -1 : 0;
        }
    }

    private NodeComparators() {
        // utility class constructor
    }

    public static Comparator<Node> duration() {
        return DURATION_COMPARATOR;
    }
}
