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
package com.freiheit.fuava.ctprofiler.core.rendering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.freiheit.fuava.ctprofiler.core.NestedTimerPath;
import com.freiheit.fuava.ctprofiler.core.Node;
import com.freiheit.fuava.ctprofiler.core.Statistics;
import com.freiheit.fuava.ctprofiler.core.TimerStatistics;


/**
 * Utility for rendering {@link Statistics}.
 *
 * @author klas
 *
 */
public final class StatisticsRenderer {

    private StatisticsRenderer() {
    }

    public static void render(
            final Renderer renderer,
            final Statistics statistics,
            final Comparator<Node> cmp
    ) throws IOException {
        renderer.begin(statistics);
        // start with the root and traverse the tree
        renderCallPath(renderer, statistics, cmp, null, null, statistics.getRoots());
        renderer.end(statistics);
    }

    public static void render(
            final Renderer renderer,
            final Statistics statistics
    ) throws IOException {
        render(renderer, statistics, null);
    }


    private static void renderCallPath(
            final Renderer renderer,
            final Statistics statistics,
            final Comparator<Node> cmp,
            final NestedTimerPath root,
            final TimerStatistics call,
            final Collection<Node> children
    ) throws IOException {
        if (root == null || renderer.beginPath(root, call)) {
            if (call != null && !call.getSubStatistics().isEmpty()) {
                // only consider substates created in other threads than the  current one,
                // because substates of this thread is included in this states call map
                final Collection<Statistics> substates = new ArrayList<Statistics>(call.getSubStatistics());
                final Iterator<Statistics> it = substates.iterator();
                while (it.hasNext()) {
                    final Statistics next = it.next();
                    if (statistics.getThreadId() == next.getThreadId()) {
                        it.remove();
                    }
                }
                if (!substates.isEmpty()) {
                    renderer.beginSubtasks();
                    for (final Statistics subState : substates) {
                        renderer.beginSubtask(subState);
                        render(renderer, subState, cmp);
                        renderer.endSubtask(subState);
                    }

                    renderer.endSubtasks();
                }
            }
            final Collection<Node> sorted = getSorted(children, cmp);
            for (final Node pv : sorted) {
                renderCallPath(renderer, statistics, cmp, pv.getPath(), pv.getTimerStatistics(), pv.getChildren());
            }
        }
        if (root != null) {
            renderer.endPath(root, call);
        }
    }

    private static Collection<Node> getSorted(final Collection<Node> children, final Comparator<Node> cmp) {
        if (cmp == null) {
            return children;
        }
        final ArrayList<Node> l = new ArrayList<Node>(children);
        Collections.sort(l, cmp);
        return l;
    }

}
