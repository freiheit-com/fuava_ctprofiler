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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.freiheit.fuava.ctprofiler.core.Layer;
import com.freiheit.fuava.ctprofiler.core.Layers;
import com.freiheit.fuava.ctprofiler.core.NestedTimerPath;
import com.freiheit.fuava.ctprofiler.core.Node;
import com.freiheit.fuava.ctprofiler.core.Statistics;
import com.freiheit.fuava.ctprofiler.core.TimerStatistics;

class ThreadStatisticsImpl implements Statistics {
    private final long _threadId;
    private final String _threadName;
    private final Collection<Node> _roots;

    private static final class NodeImpl implements Node {
        private final Layer layer;
        private final NestedTimerPath path;
        private final TimerStatistics statistics;
        private final Collection<Node> children;

        public NodeImpl(
                final Layer layer,
                final NestedTimerPath path, final TimerStatistics statistics, final Collection<Node> children) {
            if (layer == null) {
                throw new NullPointerException();
            }
            this.layer = layer;
            this.path = path;
            this.statistics = statistics;
            this.children = children;
        }

        @Override
        public Layer getLayer() {
            return layer;
        }

        @Override
        public Collection<Node> getChildren() {
            return children;
        }

        @Override
        public NestedTimerPath getPath() {
            return path;
        }

        @Override
        public TimerStatistics getTimerStatistics() {
            return statistics;
        }

    }

    private ThreadStatisticsImpl(final long threadId, final String threadName, final Map<PathWithLayer, TimerStatistics> paths) {
        _threadId = threadId;
        _threadName = threadName;
        _roots = getRoots(paths);
    }

    private Collection<Node> getRoots( final Map<PathWithLayer, TimerStatistics> paths) {
        // create  a tree
        final Map<NestedTimerPath, List<PathValue>> pathsByParent = new HashMap<NestedTimerPath, List<PathValue>>();

        for (final Map.Entry<PathWithLayer, TimerStatistics> keyValue : paths.entrySet()) {
            final PathWithLayer p = keyValue.getKey();
            final NestedTimerPath parent = p.getPath().getParent();
            List<PathValue> ps = pathsByParent.get(parent);
            if (ps == null) {
                ps = new ArrayList<PathValue>();
                pathsByParent.put(parent, ps);
            }
            ps.add(new PathValue(p.getLayer(), p.getPath(), keyValue.getValue()));
        }

        final List<PathValue> roots = pathsByParent.get(PathImpl.ROOT);
        final List<Node> rootNodes = new ArrayList<Node>();
        if (roots != null) {
            for (final PathValue pv : roots) {
                final Layer pvl = pv.getLayer();
                final Layer l = pvl.equals(Layers.inherit()) ? Layers.DEFAULT: pvl;
                rootNodes.add(toNode(pathsByParent, l, pv.getPath(), pv.getCall()));
            }
        }

        return rootNodes;
    }

    private Node toNode(final Map<NestedTimerPath, List<PathValue>> pathsByParent, final Layer layer, final NestedTimerPath paths, final TimerStatistics statistics) {
        if (statistics == null) {
            throw new NullPointerException("statistics must not be null");
        }
        if (paths == null) {
            throw new NullPointerException("path must not be null");
        }
        final List<PathValue> pvs = pathsByParent.get(paths);
        final Collection<Node> children = new ArrayList<Node>();
        if (pvs != null) {
            for (final PathValue pv : pvs) {
                final Layer pvl = pv.getLayer();
                final Layer l = pvl.equals(Layers.inherit()) ? layer: pvl;
                children.add(toNode(pathsByParent, l, pv.getPath(), pv.getCall()));
            }
        }
        return new NodeImpl(layer, paths, statistics, children);
    }

    static Statistics getCurrentThreadInstance(final Map<PathWithLayer, Call> paths) {
        return getInstance(Thread.currentThread(), paths);
    }

    static Statistics getInstance(final Thread thread, final Map<PathWithLayer, Call> paths) {
        return getInstance(thread.getId(), thread.getName(), paths);
    }

    static Statistics getInstance(final long threadId, final String threadName, final Map<PathWithLayer, Call> paths) {
        final Map<PathWithLayer, TimerStatistics> m = new LinkedHashMap<PathWithLayer, TimerStatistics>();
        for (final Map.Entry<PathWithLayer, Call> e : paths.entrySet()) {
            m.put(e.getKey(), e.getValue());
        }
        return new ThreadStatisticsImpl(threadId, threadName, m);
    }

    @Override
    public long getThreadId() {
        return _threadId;
    }

    @Override
    public String getThreadName() {
        return _threadName;
    }

    @Override
    public Collection<Node> getRoots() {
        return _roots;
    }

    @Override
    public long getTotalNanos() {
        long r = 0;
        for (final Node n : _roots) {
            r += n.getTimerStatistics().getTotalNanos();
        }
        return r;
    }
}
