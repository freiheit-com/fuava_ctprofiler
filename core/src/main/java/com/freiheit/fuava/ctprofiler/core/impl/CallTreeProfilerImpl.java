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

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import com.freiheit.fuava.ctprofiler.core.CallTreeProfiler;
import com.freiheit.fuava.ctprofiler.core.Layer;
import com.freiheit.fuava.ctprofiler.core.Layers;
import com.freiheit.fuava.ctprofiler.core.Node;
import com.freiheit.fuava.ctprofiler.core.NodeComparators;
import com.freiheit.fuava.ctprofiler.core.Statistics;
import com.freiheit.fuava.ctprofiler.core.rendering.StatisticsRenderer;
import com.freiheit.fuava.ctprofiler.core.rendering.TxtRenderer;
import com.freiheit.fuava.ctprofiler.core.rendering.XmlRenderer;

/**
 * A profiler that organizes the logged times into a tree hierarchy, per thread.
 *
 * @author klas.kalass@freiheit.com (initial creation)
 * @author $Author: klas $ (last modification)
 * @version $Date: 2009-09-07 13:00:36 +0200 (Mo, 07. Sep 2009) $
 */
class CallTreeProfilerImpl implements CallTreeProfiler {

    private final ThreadLocal<Map<PathWithLayer, Call>> _callStack = new CallStackThreadLocal();


    /**
     * @author Klas Kalass (klas.kalass@freiheit.com)
     *
     */
    private static final class CallStackThreadLocal extends
            ThreadLocal<Map<PathWithLayer, Call>> {
        @Override
        protected java.util.Map<PathWithLayer, Call> initialValue() {
            return new LinkedHashMap<PathWithLayer, Call>();
        }
    }

    /**
     * @author Klas Kalass (klas.kalass@freiheit.com)
     *
     */
    private static final class StackedTimesThreadLocal extends
            ThreadLocal<StackedMeasuringTracks> {
        @Override
        protected StackedMeasuringTracks initialValue() {
            return new StackedMeasuringTracks();
        }
    }

    /**
     * Abstraction for a measuring track/point.
     */
    private static final class MeasuringTrack {
        private final MeasuringTrackName _measuringTrackName;
        private final long _starttime;

        /**
         * Constructor.
         * @param measuringTrackName the name
         * @param starttime the start time
         */
        public MeasuringTrack(final MeasuringTrackName measuringTrackName, final long starttime) {
            this._measuringTrackName = measuringTrackName;
            this._starttime = starttime;
        }

        public MeasuringTrackName getMeasuringTrackName() {

            return this._measuringTrackName;
        }

        @Override
        public String toString() {
            return _measuringTrackName.toString();
        }

        public long getStarttime() {

            return this._starttime;
        }

    }

    private static final class MeasuringTrackName {
        private final Layer layer;
        private final String name;

        public MeasuringTrackName(final Layer layer, final String name) {
            this.layer = layer;
            this.name = name;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof MeasuringTrackName) {
                final MeasuringTrackName n = (MeasuringTrackName)obj;
                return layer.equals(n.layer) && name.equals(n.name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return layer.hashCode() ^ name.hashCode();
        }

        @Override
        public String toString() {
            return name + "[" + layer + "]";
        }
    }
    /**
     * A Stack for {@link MeasuringTrack} instances.
     * @author klas.kalass@freiheit.com (initial creation)
     * @author $Author: klas $ (last modification)
     * @version $Date: 2009-09-07 13:00:36 +0200 (Mo, 07. Sep 2009) $
     */
    private static final class StackedMeasuringTracks {

        private final Stack<MeasuringTrack> _stack = new Stack<MeasuringTrack>();

        /**
         * Push.
         * @param measuringTrackName the name
         * @param starttime the start time
         */
        void push(final MeasuringTrackName measuringTrackName, final long starttime) {
            this._stack.push(new MeasuringTrack(measuringTrackName, starttime));
        }

        @Override
        public String toString() {
            return _stack.toString();
        }

        /**
         * remove the toplevel instance.
         * @param measuringTrackName the name
         */
        void pop(final MeasuringTrackName measuringTrackName) {
            final int level = this._stack.size();
            if (level == 0) {
                return;
            }
            final MeasuringTrack te = this._stack.pop();
            if (!measuringTrackName.equals(te.getMeasuringTrackName())) {
                throw new IllegalStateException("Expected MeasuringTrack '"
                        + measuringTrackName + "', but had timer '"
                        + te.getMeasuringTrackName() + "' on my stack"
                );
            }

        }

    }

    private ThreadLocal<StackedMeasuringTracks> _stackedTimes = createStackedTimes();

    /**
     * Creates the thread local.
     * @return a new thread local for stacked measuring tracks
     */
    private ThreadLocal<StackedMeasuringTracks> createStackedTimes() {
        return new StackedTimesThreadLocal();
    }

    private final Comparator<Node> _comparator;


    /**
     * Create a profiler.
     *
     * @param config the configuration of the profiler.
     */
    public CallTreeProfilerImpl(final Configuration config) {
        _comparator = config.sortCallStacksByDuration() ? NodeComparators.duration() : null;
        setProfilingEnabled(config.isEnabled());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        final ThreadLocal<StackedMeasuringTracks> threadLocal = _stackedTimes;
        if (threadLocal != null) {
            threadLocal.remove();
        }
        _callStack.remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void begin(final String timerName, final long startTime) {
        doBegin(Layers.inherit(), timerName, startTime);
    }

    @Override
    public void begin(final Layer layer, final String timerName, final long startTimeNanos) {
        doBegin(layer, timerName, startTimeNanos);
    }

    private void doBegin(final Layer layer,final String timerName, final long startTime) {
        final ThreadLocal<StackedMeasuringTracks> st = _stackedTimes;
        if (st == null) {
            return;
        }
        st.get().push(new MeasuringTrackName(layer, timerName), startTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(final String timerName, final long endTime) {
        end(timerName, endTime, null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void end(final String timerName, final long endTime, final Statistics subState) {
        doEnd(Layers.inherit(), timerName, endTime, subState);
    }

    @Override
    public void end(final Layer layer, final String timerName, final long endTimeNanos) {
        doEnd(layer, timerName, endTimeNanos, null);
    }

    public void doEnd(final Layer layer, final String timerName, final long endTime, final Statistics subState) {
        final ThreadLocal<StackedMeasuringTracks> stt = _stackedTimes;
        if (stt == null) {
            return;
        }

        final StackedMeasuringTracks st = stt.get();
        final long starttime = st._stack.peek().getStarttime();
        final long duration = endTime - starttime;
        updateCallStack(duration, st, subState);
        st.pop(new MeasuringTrackName(layer, timerName));
    }

    /**
     * Updates the call stack.
     * @param duration duration in nanos
     * @param st the current stack
     */
    private void updateCallStack(final long duration, final StackedMeasuringTracks st, final Statistics subState) {

        final Map<PathWithLayer, Call> callStack = _callStack.get();
        final String[] p = new String[st._stack.size()];
        for (int i = 0; i < p.length; i++) {
            final MeasuringTrack m = st._stack.get(i);
            p[i] = m.getMeasuringTrackName().name;
        }
        final Layer layer = st._stack.peek()._measuringTrackName.layer;
        final PathWithLayer path = new PathWithLayer(layer, new PathImpl(p));
        Call c = callStack.get(path);
        if (c == null) {
            c = new Call();
        }
        callStack.put(path, c.add(duration, subState));
    }

    @Override
    public Statistics getStatistics() {
        return ThreadStatisticsImpl.getCurrentThreadInstance(_callStack.get());
    }

    /**
     * Enable/Disable the profiling.
     */
    public void setProfilingEnabled(final boolean b) {
        if (b) {
            if (_stackedTimes == null) {
                _stackedTimes = createStackedTimes();
            }
        } else {
            _stackedTimes = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Appendable> T renderThreadStateAsText(final T buffer) throws IOException {
        StatisticsRenderer.render(new TxtRenderer("", buffer), getStatistics(), _comparator);
        return buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Appendable> T  renderThreadStateAsXml(final T buffer) throws IOException {
        StatisticsRenderer.render(new XmlRenderer("", buffer), getStatistics(), _comparator);
        return buffer;
    }

    @Override
    public boolean isEnabled() {
        return _stackedTimes != null;
    }

}

