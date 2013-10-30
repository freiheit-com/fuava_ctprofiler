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

import java.io.IOException;

/**
 * A profiler that organizes the logged times into a tree hierarchy, per thread.
 *
 *
 * @author klas.kalass@freiheit.com (initial creation)
 * @author $Author: klas $ (last modification)
 * @version $Date: 2009-08-20 14:21:18 +0200 (Do, 20. Aug 2009) $
 */
public interface CallTreeProfiler {

    /**
     * Check if the call tree profiler is actually enabled.
     *
     * You do not need to call this check when calling {@link #begin(String, long)} or {@link #end(String, long)},
     * but you might wish to use it  before calling {@link #renderThreadStateAsText(Appendable)}
     * or {@link #renderThreadStateAsXml(Appendable)}
     * to avoid producing unnecessary log entries.
     *
     * @return true if call tree profiling is enabled
     */
    boolean isEnabled();

    /**
     * Clears the state for the  current thread.
     */
    void clear();

    /**
     * Begin measuring execution time of the given timerName.
     *
     * <p>When you call this method, you need to make sure that you will call
     *    one of the end methods as well. A recommended pattern is:</p>
     *
     * <pre>
     * CallTreeProfiler profiler = ...
     * profiler.begin("timername", System.nanoTime());
     * try {
     *     // do some work
     * } finally {
     *     profiler.end("timername", System.nanoTime());
     * }
     * </pre>
     *
     * @param timerName the name of the timer to measure, typically a method name
     * @param startTimeNanos the time when measuring was started in nano seconds
     */
    void begin(String timerName, long startTimeNanos);

    /**
     * Begin measuring execution time of the given timerName and associate an explicit Layer with this execution.
     *
     * <p>When you call this method, you need to make sure that you will call
     *    one of the end methods as well. A recommended pattern is:</p>
     *
     * <pre>
     * CallTreeProfiler profiler = ...
     * profiler.begin(layer, "timername", System.nanoTime());
     * try {
     *     // do some work
     * } finally {
     *     profiler.end(layer, "timername", System.nanoTime());
     * }
     * </pre>
     *
     * @param layer the layer to associate this call with
     * @param timerName the name of the timer to measure, typically a method name
     * @param startTimeNanos the time when measuring was started in nano seconds
     */
    void begin(Layer layer, String timerName, long startTimeNanos);

    /**
     * End measuring execution time of the given timerName.
     *
     * @param timerName the name of the timer to measure, typically a method name
     * @param endTimeNanos the time when measuring ended in nano seconds
     */
    void end(String timerName, long endTimeNanos);

    /**
     * End measuring execution time of the given timerName.
     *
     * @param layer the layer to associate this call with
     * @param timerName the name of the timer to measure, typically a method name
     * @param endTimeNanos the time when measuring ended in nano seconds
     */
    void end(Layer layer, String timerName, long endTimeNanos);


    /**
     * Render the state of the current thread as text.
     * @param buffer the appendable to render to
     * @throws IOException if an IO Exception occured while rendering to the appendable
     * @param <T> the type of the appendable
     * @return the appendable, for method chaining
     */
    <T extends Appendable> T renderThreadStateAsText(T buffer) throws IOException;

    /**
     * Render the state of the current thread as text.
     * @param buffer the appendable to render t instance)o
     * @throws IOException if an IO Exception occured while rendering to the appendable
     * @param <T> the type of the appendable
     * @return the appendable, for method chaining
     */
    <T extends Appendable> T  renderThreadStateAsXml(T buffer) throws IOException;

    /**
     * Get a copy of the state of the call tree profiler for the current Thread, similar to the render methods
     * above, but returns the state in a more structured fashion.
     *
     * @return the state
     */
    Statistics getStatistics();

    /**
     * End measuring execution time of the given timerName and associate the given sub-thread statistics with it.
     *
     * @param timerName the name of the timer to measure, typically a method name
     * @param endTimeNanos the time when measuring ended in nano seconds
     * @param statistics the statistics to associate with the specified timer, or null if there is no sub-thread statistics
     */
    void end(String timerName, long endTimeNanos, Statistics statistics);
}

