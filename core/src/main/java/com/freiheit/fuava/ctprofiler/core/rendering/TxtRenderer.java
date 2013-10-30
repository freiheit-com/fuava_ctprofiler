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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.freiheit.fuava.ctprofiler.core.NestedTimerPath;
import com.freiheit.fuava.ctprofiler.core.Node;
import com.freiheit.fuava.ctprofiler.core.Statistics;
import com.freiheit.fuava.ctprofiler.core.TimerStatistics;

public class TxtRenderer extends Renderer {
    private static final int DEFAULT_CURRENTLY_SKIPPED_LEVEL = -1;
    private final Appendable sb;
    private final Stack<String> pres;
    private String leafNamePrefix = "|-";
    private String callIndentation = "| ";
    private String lineend = "\n";
    private long totalNanosThreshold;

    private int currentlySkippedLevel = DEFAULT_CURRENTLY_SKIPPED_LEVEL;
    private int numCurrentlySkipped = 0;
    private long leafStatisticsThresholdNanos;
    private int leafStatisticsMaxItems=0;// disable leafStatistics
    private TimerStatistics lastCall;
    private NestedTimerPath lastRoot;

    public TxtRenderer(final String pre, final Appendable sb) {
        assert pre != null;
        this.sb = sb;
        this.pres = new Stack<String>();
        this.pres.push(pre);
    }

    public void setLeafNamePrefix(final String leafToken) {
        this.leafNamePrefix = leafToken;
    }
    public void setLineend(final String lineend) {
        this.lineend = lineend;
    }
    public void setCallIndentation(final String callIndentation) {
        this.callIndentation = callIndentation;
    }
    public void setTotalNanosThreshold(final long totalNanosThreshold) {
        this.totalNanosThreshold = totalNanosThreshold;
    }

    public void setLeafStatisticsMaxItems(final int leafStatisticsMaxItems) {
        this.leafStatisticsMaxItems = leafStatisticsMaxItems;
    }

    @Override
    public void begin(final Statistics statistics) throws IOException {
    }


    @Override
    public void end(final Statistics statistics) throws IOException {
        if (leafStatisticsMaxItems > 0) {
            printLeafStatistics(statistics);
        }
    }

    private void printLeafStatistics(final Statistics statistics) throws IOException {
        final Map<String, Collection<TimerStatistics>> m = addStatistics(statistics.getRoots(), new HashMap<String, Collection<TimerStatistics>>());
        class LeafStatistics {
            private final String leafName;
            private final int totalCallCount;
            private final long totalNanos;
            public LeafStatistics(final String leafName, final int totalCallCount, final long totalNanos) {
                this.leafName = leafName;
                this.totalCallCount = totalCallCount;
                this.totalNanos = totalNanos;
            }
            public int getTotalCallCount() {
                return totalCallCount;
            }

            public long getTotalNanos() {
                return totalNanos;
            }
            public String getLeafName() {
                return leafName;
            }
        }

        final List<LeafStatistics> stats = new ArrayList<LeafStatistics>();
        for (final Map.Entry<String, Collection<TimerStatistics>> e : m.entrySet()) {
            int totalCallCount = 0;
            long totalCallNanos = 0;
            for (final TimerStatistics s : e.getValue()) {
                totalCallCount += s.getNumberOfCalls();
                totalCallNanos += s.getTotalNanos();
            }
            stats.add(new LeafStatistics(e.getKey(), totalCallCount, totalCallNanos));
        }
        Collections.sort(stats, new Comparator<LeafStatistics>() {
            @Override
            public int compare(final LeafStatistics o1, final LeafStatistics o2) {
                final long n1 = o1.getTotalNanos();
                final long n2 = o2.getTotalNanos();
                return  n1 < n2 ? 1 : (n1 == n2 ? 0 : -1);
            }
        });

        int numRendererd = 0;

        sb.append(String.format("\n\nTop %2d Durations by Leaf Names > %12.2fms:\n", Long.valueOf(leafStatisticsMaxItems), Double.valueOf(getTotalMillis(leafStatisticsThresholdNanos))));
        for (final LeafStatistics ls : stats) {
            if (ls.getTotalNanos() > leafStatisticsThresholdNanos && numRendererd < leafStatisticsMaxItems) {
                numRendererd += 1;
                sb.append(String.format("[%7d] %12.2fms %s\n",
                        Long.valueOf(ls.getTotalCallCount()),
                        Double.valueOf(getTotalMillis(ls.getTotalNanos())),
                        ls.getLeafName()
                ));
            }
        }
    }

    private static Map<String, Collection<TimerStatistics>> addStatistics(final Collection<Node> nodes, final Map<String, Collection<TimerStatistics>> m) {
        for (final Node n : nodes) {
            final String key = n.getPath().getLeafTimerName();
            Collection<TimerStatistics> c = m.get(key);
            if (c == null) {
                c = new ArrayList<TimerStatistics>();
                m.put(key, c);
            }
            c.add(n.getTimerStatistics());
            addStatistics(n.getChildren(), m);
        }
        return m;
    }
    @Override
    public boolean beginPath(final NestedTimerPath root, final TimerStatistics call) throws IOException {
        sb.append(getPre());
        if (call != null) {
            final boolean belowThreshold = call.getTotalNanos() < totalNanosThreshold;
            final boolean currentlySkippedLevelChanges = root.getLevel() != currentlySkippedLevel;
            if (isCurrentlySkipping() && (!belowThreshold || currentlySkippedLevelChanges)) {
                if (numCurrentlySkipped == 1 && lastRoot != null) {
                    renderCall(lastRoot, lastCall);
                } else {
                    renderCurrentlySkippedLine();
                }
            }
            if (belowThreshold) {
                if (currentlySkippedLevelChanges) {
                    // render something
                    numCurrentlySkipped = 1;
                } else {
                    numCurrentlySkipped += 1;
                }
                currentlySkippedLevel = root.getLevel();
                lastRoot = root;
                lastCall = call;
                return false; // skip rendering
            } else {
                numCurrentlySkipped = 0;
                currentlySkippedLevel = DEFAULT_CURRENTLY_SKIPPED_LEVEL;
            }

        }
        renderCall(root, call);
        lastRoot = root;
        lastCall = call;
        return true; // add content if applicable
    }

    private void renderCall(final NestedTimerPath root, final TimerStatistics call) throws IOException {
        renderCallTimings(call);
        renderNodeName(root);
    }

    private void renderNodeName(final NestedTimerPath root) throws IOException {
        appendPathIndent(sb, root.getLevel());
        sb.append(root.getLeafTimerName());
        sb.append(lineend);
    }

    private void renderCallTimings(final TimerStatistics call) throws IOException {
        if (call != null) {
            sb.append(String.format("[%7d] %12.2fms ",
                    Long.valueOf(call.getNumberOfCalls()),
                    Double.valueOf(getTotalMillis(call.getTotalNanos()))
            ));
        }
    }

    private boolean isCurrentlySkipping() {
        return currentlySkippedLevel != DEFAULT_CURRENTLY_SKIPPED_LEVEL;
    }

    private void renderCurrentlySkippedLine() throws IOException {
        sb.append("                         ");
        appendPathIndent(sb, currentlySkippedLevel);
        sb.append(String.format("(%3d paths each < %4.2fms)",
                Long.valueOf(numCurrentlySkipped),
                Double.valueOf(getTotalMillis(totalNanosThreshold))
        ));
        sb.append(lineend);
    }

    private void appendPathIndent(final Appendable sb, final int level) throws IOException {
        for (int i = 0; i < (level - 1); ++i) {
            sb.append(callIndentation);
        }
        if (level > 0) {
            sb.append(leafNamePrefix);
        }
    }

    private double getTotalMillis(final long nanos) {
        return nanos / 1000000.0;
    }

    @Override
    public void endPath(final NestedTimerPath root, final TimerStatistics call) {

    }

    @Override
    public void beginSubtasks() throws IOException {
        sb.append(getPre()).append("----------- BEGIN SUBTASK ----------");
    }

    private String getPre() {
        return pres.peek();
    }
    @Override
    public void endSubtasks() throws IOException {
        sb.append(getPre()).append("----------- END   SUBTASK ----------\n\n");
    }

    @Override
    public void beginSubtask(final Statistics subState) throws IOException {
        final String p = getPre() + "    ";
        this.pres.push(p);
        sb.append("\n").append(p).append("[").append(subState.getThreadName()).append("]");
    }
    @Override
    public void endSubtask(final Statistics subState) throws IOException {
        this.pres.pop();
    }

    public void setLeafStatisticsThresholdNanos(final long leafStatisticsThresholdNanos) {
        this.leafStatisticsThresholdNanos = leafStatisticsThresholdNanos;
    }
}