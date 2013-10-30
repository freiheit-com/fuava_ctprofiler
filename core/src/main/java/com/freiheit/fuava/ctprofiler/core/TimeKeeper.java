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
 * Convenience access to the {@link CallTreeProfiler} functionality that includes creation of transparently profiling proxy objects and simplistic time measurement methods.
 *
 * <p>This interface is intended for all cases where you want to do some call profiling from
 * application code directly, i.e. there is no integration with other profiling/time measurement
 * where you would need to reuse time data.</p>
 *
 *
 * @author klas
 */
public interface TimeKeeper {

    /**
     * Checks wether the execution times will actually be measured.
     *
     * <p>If this method returns false, you may safely skip calculation of your timers name (if applicable)
     * and just pass a dummy string to not waste string calculation effort</p>
     *
     * @return true if call tree profiling is enabled.
     */
    boolean isEnabled();

    /**
     * Create a proxy that profiles all calls to the given instance.
     *
     * @param <T> the type to proxy
     * @param iface the interface to proxy
     * @param instance the instance to proxy
     * @return the proxy to use instead of the instance
     */
    <T> T proxy(Class<T> iface, T instance);

    /**
     * Create a proxy that profiles all calls to the given instance, associating the given Layer with all calls.
     *
     * @param <T> the type to proxy
     * @param iface the interface to proxy
     * @param instance the instance to proxy
     * @return the proxy to use instead of the instance
     */
    <T> T proxy(Layer layer, Class<T> iface, T instance);

    /**
     * Create a proxy that implements all interfaces implemented by the given instance
     * and profiles all calls.
     */
    Object proxy(Object instance);

    /**
     * Create a proxy that implements all interfaces implemented by the given instance
     * and profiles all calls, associating the given Layer to all calls.
     */
    Object proxy(Layer layer, Object instance);

    /**
     * Create a proxy that profiles all calls to the given instance.
     *
     * This method is basically the same as {@link #proxy(Class, Object)},
     * but it will cast the result to the subtype T2 of T. This method was
     * introduced as a workaround for the fact that T cannot be a parameterized
     * type here (due to the usage of Class&lt;T>), and it enables you to hide some
     * ugly casting, <em>but you need to ensure yourself that T2 has the same runtime
     * class as T</em>.
     *
     * <pre>
     * // correct:
     * List&lt;String> list = proxyGeneric(List.class, new ArrayList&lt;String>());
     *
     * // wrong:
     * ArrayList&lt;String> list = proxyGeneric(List.class, new ArrayList&lt;String>());
     * </pre>
     * @param <T> the type to proxy
     * @param iface the interface to proxy
     * @param instance the instance to proxy
     * @return the proxy to use instead of the instance
     */
    <T, T2 extends T> T2 proxyGeneric(Class<T> iface, T2 instance);

    /**
     * Begin measuring execution time for the given timerName.
     *
     * <p>You need to ensure that {@link #end(String)} is called under all circumstances. The following pattern is recommended:</p>
     *
     * <pre>
     * TimeKeeper profiler = ...
     * profiler.begin("name");
     * try {
     *    // do stuff
     * } finally {
     *     profiler.end("name");
     * }
     * </pre>
     * @param timerName the name of the timer to measure, typically a method name
     */
    void begin(String timerName);

    /**
     * End measuring execution time for the given timerName.
     *
     * <p>Always use this method in conjunction with {@link #begin(String)}. The following pattern is recommended:</p>
     *
     * <pre>
     * TimeKeeper profiler = ...
     * profiler.begin("name");
     * try {
     *    // do stuff
     * } finally {
     *     profiler.end("name");
     * }
     * </pre>
     * @param timerName the name of the timer to measure, typically a method name
     */
    void end(String timerName);

    /**
     * Begin measuring execution time for the given timerName and associate an explicit Layer with this execution.
     *
     * <p>You need to ensure that {@link #end(Layer, String)} is called under all circumstances. The following pattern is recommended:</p>
     *
     * <pre>
     * TimeKeeper profiler = ...
     * profiler.begin(layer, "name");
     * try {
     *    // do stuff
     * } finally {
     *     profiler.end(layer, "name");
     * }
     * </pre>
     * @param layer the layer to associate this call with
     * @param timerName the name of the timer to measure, typically a method name
     */
    void begin(Layer layer, String timerName);

    /**
     * End measuring execution time for the given timerName in the specified Layer.
     *
     * <p>Always use this method in conjunction with {@link #begin(Layer, String)}. The following pattern is recommended:</p>
     *
     * <pre>
     * TimeKeeper profiler = ...
     * profiler.begin(layer, "name");
     * try {
     *    // do stuff
     * } finally {
     *     profiler.end(layer, "name");
     * }
     * </pre>
     * @param layer the layer to associate this call with
     * @param timerName the name of the timer to measure, typically a method name
     */
    void end(Layer layer, String timerName);
}
