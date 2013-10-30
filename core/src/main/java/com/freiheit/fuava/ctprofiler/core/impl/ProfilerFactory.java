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

import com.freiheit.fuava.ctprofiler.core.CallTreeProfiler;
import com.freiheit.fuava.ctprofiler.core.TimeKeeper;

/**
 * Factory class for creating CallTreeProfiler and TimeKeeper instances.
 *
 * @author klas
 */
public final class ProfilerFactory {
    /**
     * The name of the system configuration property that enables profiling for the global profiler instance.
     * <p>The default value (if this property is not explicitely set) is 'true'.
     * Set it to any other value than 'true' to disable profiling for the global instance.</p>
     * <h6>Example:</h6>
     * <pre>
     * java -Dfdc.fuava.ctprofiler.enabled=false YourApp
     * </pre>
     */
    public static final String PROP_GLOBAL_IS_ENABLED = "fdc.fuava.ctprofiler.enabled";

    private static final CallTreeProfiler GLOBAL_PROFILER = isGlobalEnabled()
    ? new CallTreeProfilerImpl(new AbstractConfiguration() {}) : DisabledCallTreeProfiler.getInstance();
    private static final TimeKeeper GLOBAL_TIME_KEEPER = isGlobalEnabled()
    ? new TimeKeeperImpl(GLOBAL_PROFILER) : DisabledTimeKeeper.getInstance();

    private ProfilerFactory() {
    }

    /**
     * Get the global profiler instance.
     *
     * <p>It is recommended to use dependency injection and {@link #createProfiler()} instead
     * of this method, but if you cannot or do not want to use dependency injection, you
     * can use the global profiler instance everywhere.</p>
     *
     * <p>Whether the global profiler is enabled or disabled, is determined by the java system property
     * {@value com.freiheit.fuava.ctprofiler.core.impl.ProfilerFactory#PROP_GLOBAL_IS_ENABLED}.</p>
     */
    public static CallTreeProfiler getGlobalProfiler() {
        return GLOBAL_PROFILER;
    }

    /**
     * Get the global time keeper instance.
     *
     * <p>It is recommended to use dependency injection and {@link #createTimeKeeper(CallTreeProfiler)} instead
     * of this method, but if you cannot or do not want to use dependency injection, you
     * can use the global time keeper instance everywhere.</p>
     *
     * <p>Whether the global time keeper is enabled or disabled, is determined by the java system property
     * {@value com.freiheit.fuava.ctprofiler.core.impl.ProfilerFactory#PROP_GLOBAL_IS_ENABLED}.</p>
     */
    public static TimeKeeper getGlobalTimeKeeper() {
        return GLOBAL_TIME_KEEPER;
    }

    private static boolean isGlobalEnabled() {
        final String enabledString = System.getProperty(PROP_GLOBAL_IS_ENABLED);
        if (enabledString == null || enabledString.trim().length() == 0) {
            return true;
        }
        return Boolean.valueOf(enabledString);
    }
    /**
     * Create an enabled CallTreeProfiler.
     *
     * <p>This instance is always enabled and will output the call stacks sorted
     *    by chronological order, not by execution time.</p>
     *
     * <p>Please note that multiple instances do not interact with each other, so you
     * need to ensure yourself to use the same instance everywhere so
     * that your callstack is complete (i.e. do singleton scope binding in your dependency injection framework).</p>
     *
     * @return a new profiler instance.
     */
    public static CallTreeProfiler createProfiler(){
        return new CallTreeProfilerImpl(new AbstractConfiguration(){});
    }

    /**
     * Create a CallTreeProfiler that behaves accordingly to the given Configuration.
     *
     * <p>Please note that multiple instances do not interact with each other, so you
     * need to ensure yourself to use the same instance everywhere so
     * that your callstack is complete (i.e. do singleton scope binding in your dependency injection framework).</p>
     *
     * @param configuration the configuration to use for profiling
     * @return a new profiler instance.
     */
    public static CallTreeProfiler createProfiler(final Configuration configuration) {
        return new CallTreeProfilerImpl(configuration);
    }

    /**
     * Create a TimeKeeper that uses the given CallTreeProfiler instance for delegating the real work.
     *
     * @param profiler the profiler to use
     * @return a new Time Keeper.
     */
    public static TimeKeeper createTimeKeeper(final CallTreeProfiler profiler) {
        return new TimeKeeperImpl(profiler);
    }

    /**
     * A CallTreeProfiler that is always disabled.
     *
     * <p>This type of CallTreeProfiler is very usefull in all situations where you are not
     * interested in measuring execution times at all. For example in Unit Tests, or as a default instantiation
     * of your CallTreeProfiler variable, if you want to make dependency injection of a functional
     * implementation optional.</p>
     *
     * @return a CallTreeProfiler that is always disabled.
     */
    public static CallTreeProfiler getDisabledProfiler() {
        return DisabledCallTreeProfiler.getInstance();
    }

    /**
     * A TimeKeeper that is always disabled.
     *
     * <p>This type of TimeKeeper is very usefull in all situations where you are not
     * interested in measuring execution times at all. For example in Unit Tests, or as a default instantiation
     * of your TimeKeeper variable, if you want to make dependency injection of a functional
     * implementation optional.</p>
     *
     * @return A TimeKeeper that is always disabled.
     */
    public static TimeKeeper getDisabledTimeKeeper() {
        return DisabledTimeKeeper.getInstance();
    }
}
