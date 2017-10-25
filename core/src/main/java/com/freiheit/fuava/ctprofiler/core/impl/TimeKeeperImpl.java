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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import com.freiheit.fuava.ctprofiler.core.CallTreeProfiler;
import com.freiheit.fuava.ctprofiler.core.Layer;
import com.freiheit.fuava.ctprofiler.core.Layers;
import com.freiheit.fuava.ctprofiler.core.TimeKeeper;

/**
 * Implementation of {@link TimeKeeper} that uses a {@link CallTreeProfiler} for doing the real work.
 * @author klas
 *
 */
class TimeKeeperImpl implements TimeKeeper {

    private final CallTreeProfiler _callTreeProfiler;

    public TimeKeeperImpl(final CallTreeProfiler callTreeProfiler) {
        if (callTreeProfiler == null) {
            throw new NullPointerException("Cannot construct a TimeKeeper without a call tree profiler");
        }
        _callTreeProfiler = callTreeProfiler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return _callTreeProfiler.isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void begin(final String timerName) {
        if (_callTreeProfiler.isEnabled()) {
            _callTreeProfiler.begin(timerName, System.nanoTime());
        }
    }

    @Override
    public void begin(final Layer layer, final String timerName) {
        if (_callTreeProfiler.isEnabled()) {
            _callTreeProfiler.begin(layer, timerName, System.nanoTime());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(final String timerName) {
        if (_callTreeProfiler.isEnabled()) {
            _callTreeProfiler.end(timerName, System.nanoTime());
        }
    }

    @Override
    public void end(final Layer layer, final String timerName) {
        if (_callTreeProfiler.isEnabled()) {
            _callTreeProfiler.end(layer, timerName, System.nanoTime());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T proxy(final java.lang.Class<T> iface, final T instance) {
        return proxy(Layers.inherit(), iface, instance);
    }

    @Override
    public <T> T proxy(final Layer layer, final java.lang.Class<T> iface, final T instance) {
        if (!_callTreeProfiler.isEnabled()) {
            return instance;
        }
        final Object o = doProxy(layer, new Class[]{iface}, instance);
        return iface.cast(o);
    }

    @SuppressWarnings("unchecked")
    private <T> Object doProxy(final Layer layer, @SuppressWarnings("rawtypes") final java.lang.Class[] ifaces, final T instance) {
        final Object o = Proxy.newProxyInstance(getClass().getClassLoader(), ifaces, new InvocationHandler() {
            private final String _classSimpleName = instance.getClass().getSimpleName();

            @Override
            //CHECKSTYLE:OFF
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                final String timerName = _classSimpleName + "." + method.getName();
                //CHECKSTYLE:ON
                _callTreeProfiler.begin(layer, timerName, System.nanoTime());
                try {
                    setAccess(method);
                    return method.invoke(instance, args);
                } catch (final InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof RuntimeException || cause instanceof Error){
                        throw cause;
                    }

                    // throw cause if it's a declared exception
                    for (Class<?> cls : method.getExceptionTypes()) {
                        if (cls.isInstance(cause)) {
                            throw cause;
                        }
                    }

                    throw new RuntimeException(cause);
                } finally {
                    _callTreeProfiler.end(layer, timerName, System.nanoTime());
                }
            }

            private void setAccess(final Method method) {
                try {
                    method.setAccessible(true);
                } catch (final SecurityException e){
                    // ignore - will lead to InvocationTargetException if this call would have been necessary, and does not matter else
                }
            }
        });
        return o;
    };

    @Override
    public Object proxy(final Object instance) {
        return proxy(Layers.inherit(), instance);
    }

    @Override
    public Object proxy(final Layer layer, final Object instance) {
        if (instance == null) {
            return null;
        }
        final Set<Class<?>> interfaces = add(instance.getClass(), new LinkedHashSet<Class<?>>());

        return doProxy(layer, (new ArrayList<Class<?>>(interfaces)).toArray(new Class[interfaces.size()]), instance);
    }

    private Set<Class<?>> add(final Class<?> cls, final Set<Class<?>> s) {
        if (cls.isInterface()) {
            s.add(cls);
        }
        for (final Class<?> iface : cls.getInterfaces()) {
            add(iface, s);
        }
        final Class<?> superclass = cls.getSuperclass();
        if (superclass != null) {
            add(superclass, s);
        }
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T, T2 extends T> T2 proxyGeneric(final java.lang.Class<T> iface, final T2 instance) {
        final Object o = proxy(iface, instance);
        return (T2)o;
    };
}
