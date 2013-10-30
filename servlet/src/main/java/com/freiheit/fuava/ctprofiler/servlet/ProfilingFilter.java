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
package com.freiheit.fuava.ctprofiler.servlet;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;

import com.freiheit.fuava.ctprofiler.core.CallTreeProfiler;
import com.freiheit.fuava.ctprofiler.core.Layer;
import com.freiheit.fuava.ctprofiler.core.Layers;
import com.freiheit.fuava.ctprofiler.core.Node;
import com.freiheit.fuava.ctprofiler.core.NodeComparators;
import com.freiheit.fuava.ctprofiler.core.Statistics;
import com.freiheit.fuava.ctprofiler.core.impl.ProfilerFactory;
import com.freiheit.fuava.ctprofiler.core.rendering.StatisticsRenderer;
import com.freiheit.fuava.ctprofiler.core.rendering.TxtRenderer;


/**
 * A Java Servlet Filter that adds profiling.
 */
public class ProfilingFilter implements Filter {
    public static final String PARAM_LAYER = "layer";
    public static final String PARAM_LEAF_STATISTICS_THRESHOLD_NANOS = "leafStatisticsThresholdNanos";
    public static final String PARAM_LEAF_STATISTICS_MAX_ITEMS = "leafStatisticsMaxItems";
    public static final String PARAM_ORDERING = "ordering";
    public static final String PARAM_CALL_DURATION_THRESHOLD_NANOS = "callDurationThresholdNanos";
    public static final String PARAM_REQUEST_DURATION_THRESHOLD_NANOS = "requestDurationThresholdNanos";
    public static final String PARAM_CALL_INDENTATION = "callIndentation";
    public static final String PARAM_LEAF_NAME_PREFIX = "leafNamePrefix";


    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProfilingFilter.class);

    private CallTreeProfiler callTreeProfiler = ProfilerFactory.getGlobalProfiler();
    private long callDurationThresholdNanos = TimeUnit.MILLISECONDS.toNanos(1);
    private long leafStatisticsThresholdNanos = TimeUnit.MILLISECONDS.toNanos(10);
    private int leafStatisticsMaxItems = 20;
    private long requestDurationThresholdNanos = TimeUnit.MILLISECONDS.toNanos(200);
    private Ordering ordering = Ordering.SEMI_SEQUENTIAL;

    private String callIndentation  = ". ";
    private String leafNamePrefix   = "+ ";
    private Layer layer = Layers.PRESENTATION;

    public enum Ordering {
        /**
         * Sequential, earlier calls of the same level will be printed first - but all
         * calls that share the same name and parent will be put together in one node, regardless
         * of other calls following
         */
        SEMI_SEQUENTIAL {
            @Override
            public Comparator<Node> getComparator() {
                return null;
            }
        },
        /**
         * Slowest calls will be printed first
         */
        DURATION {
            @Override
            public Comparator<Node> getComparator() {
                return NodeComparators.duration();
            }
        };

        public abstract Comparator<Node> getComparator();
    }

    public void setLayerName(final String layer) {
        this.layer = Layers.forName(layer);
    }

    public void setCallDurationThresholdNanos(final long callDurationThresholdNanos) {
        this.callDurationThresholdNanos = callDurationThresholdNanos;
    }

    public void setCallIndentation(final String callIndentation) {
        this.callIndentation = callIndentation;
    }

    public void setCallTreeProfiler(final CallTreeProfiler callTreeProfiler) {
        this.callTreeProfiler = callTreeProfiler;
    }

    public void setLeafNamePrefix(final String leafNamePrefix) {
        this.leafNamePrefix = leafNamePrefix;
    }

    public void setOrdering(final Ordering ordering) {
        this.ordering = ordering;
    }

    public void setRequestDurationThresholdNanos(final long requestDurationThresholdNanos) {
        this.requestDurationThresholdNanos = requestDurationThresholdNanos;
    }

    public void setLeafStatisticsMaxItems(final int leafStatisticsMaxItems) {
        this.leafStatisticsMaxItems = leafStatisticsMaxItems;
    }

    public void setLeafStatisticsThresholdNanos(final long leafStatisticsThresholdNanos) {
        this.leafStatisticsThresholdNanos = leafStatisticsThresholdNanos;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain arg2) throws IOException, ServletException {
        final String requestId = buildProfilingRequestId(request);
        callTreeProfiler.clear();
        callTreeProfiler.begin(layer, requestId, System.nanoTime());
        try {
            arg2.doFilter(request, response);
        } finally {
            callTreeProfiler.end(layer, requestId, System.nanoTime());
            if (callTreeProfiler.isEnabled()) {
                try {
                    final StringBuilder buffer = new StringBuilder("\n");
                    final Statistics statistics = callTreeProfiler.getStatistics();
                    final long totalNanos = statistics.getTotalNanos();
                    if (totalNanos > requestDurationThresholdNanos) {
                        final TxtRenderer renderer = new TxtRenderer("", buffer);
                        renderer.setLeafNamePrefix(leafNamePrefix);
                        renderer.setCallIndentation(callIndentation);
                        renderer.setTotalNanosThreshold(callDurationThresholdNanos);
                        renderer.setLeafStatisticsThresholdNanos(leafStatisticsThresholdNanos);
                        renderer.setLeafStatisticsMaxItems(leafStatisticsMaxItems);
                        StatisticsRenderer.render(renderer, statistics, ordering.getComparator());

                    } else {
                        buffer.append("too fast for CallTreeProfiler logging: " + totalNanos + "nanos "  + requestId);
                    }
                    LOG.info(buffer.toString());
                } catch (final IOException io) {
                    // ignore - cannot help, if this happens
                }
            }
            callTreeProfiler.clear();

        }
    }


    private String buildProfilingRequestId(final ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            final HttpServletRequest hsr = (HttpServletRequest)request;
            final String pathInfo = getPath(hsr);
            return hsr.getMethod() + " " + pathInfo;
        }
        return "UNKNOWN";
    }

    private String getPath(final HttpServletRequest hsr) {
        final String pathInfo = hsr.getPathInfo();
        if (pathInfo != null) {
            return pathInfo;
        }
        final String requestUri = hsr.getRequestURI();
        if (requestUri != null) {
            return requestUri;
        }
        return "/";
    }

    private long parseOrDefault(final String v, final long defaultvalue) {
        if (v == null || v.trim().isEmpty()) {
            return defaultvalue;
        }
        try {
            return Long.parseLong(v);
        } catch (final NumberFormatException e) {
            return defaultvalue;
        }
    }
    private String parseOrDefault(final String v, final String defaultvalue) {
        if (v == null || v.trim().isEmpty()) {
            return defaultvalue;
        }
        try {
            return v;
        } catch (final NumberFormatException e) {
            return defaultvalue;
        }
    }

    @Override
    public void init(final FilterConfig config) throws ServletException {
        leafStatisticsMaxItems = (int)parseOrDefault(config.getInitParameter(PARAM_LEAF_STATISTICS_MAX_ITEMS), leafStatisticsMaxItems);
        requestDurationThresholdNanos = parseOrDefault(config.getInitParameter(PARAM_REQUEST_DURATION_THRESHOLD_NANOS), requestDurationThresholdNanos);
        callDurationThresholdNanos = parseOrDefault(config.getInitParameter(PARAM_CALL_DURATION_THRESHOLD_NANOS), callDurationThresholdNanos);
        ordering = parseOrdering(config.getInitParameter(PARAM_ORDERING), ordering);
        callIndentation = parseOrDefault(config.getInitParameter(PARAM_CALL_INDENTATION), callIndentation);
        leafNamePrefix = parseOrDefault(config.getInitParameter(PARAM_LEAF_NAME_PREFIX), leafNamePrefix);
        leafStatisticsThresholdNanos = parseOrDefault(config.getInitParameter(PARAM_LEAF_STATISTICS_THRESHOLD_NANOS), leafStatisticsThresholdNanos);
        layer = Layers.forName(parseOrDefault(config.getInitParameter(PARAM_LAYER), layer.getName()));
    }

    private Ordering parseOrdering(final String initParameter, final Ordering defaultValue) {
        if (initParameter == null || initParameter.trim().isEmpty()) {
            return defaultValue;
        }
        return Ordering.valueOf(initParameter.toUpperCase());
    }


}
