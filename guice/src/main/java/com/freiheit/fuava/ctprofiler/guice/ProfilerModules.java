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
package com.freiheit.fuava.ctprofiler.guice;

import java.lang.reflect.Method;
import java.util.Locale;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.freiheit.fuava.ctprofiler.core.Layer;
import com.freiheit.fuava.ctprofiler.core.TimeKeeper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;


/**
 * Factory for common profiler modules.
 *
 * @author klas (initial creation)
 */
public final class ProfilerModules {

    @SuppressWarnings("unchecked")
    private static final class MatchClassesContainingDAOs extends
            AbstractMatcher<Class>
    {
        private final String _token;
        MatchClassesContainingDAOs(final String token) {
            _token = token;
        }
        @Override
        public boolean matches(final Class t) {
            final boolean dao = t.getSimpleName().toUpperCase(new Locale("en")).contains(_token);
            return dao;
        }
    }

    private static final class MatchMethodsForProfiling extends
            AbstractMatcher<Method>
    {
        @Override
        public boolean matches(final Method t) {
            return t.getAnnotation(ProfileMe.class) != null;
        }
    }

    /**
     * A module that decorates matched method calls with profiling.
     */
    private static class ProfilingMethodInterceptorModule extends AbstractModule {

        private final Matcher<? super Class<?>> _classMatcher;
        private final Matcher<? super Method>   _methodMatcher;
        private final Layer _layer;

        class ProfilingMethodInterceptor implements MethodInterceptor {
            private final Layer _layer;

            public ProfilingMethodInterceptor(final Layer layer) {
                _layer = layer;
            }

            @Inject
            private TimeKeeper _timeKeeper;

            @Override
            public Object invoke(final MethodInvocation invocation) throws Throwable {
                final Method method = invocation.getMethod();
                final String key = _timeKeeper.isEnabled() ? method.getDeclaringClass().getSimpleName() + "." + method.getName() : null;
                _timeKeeper.begin(_layer, key);
                try {
                    return invocation.proceed();
                } finally {
                    _timeKeeper.end(_layer, key);
                }
            }

        }

        public ProfilingMethodInterceptorModule(
                final Layer layer,
                final Matcher<? super Class<?>> classMatcher,
                final Matcher<? super Method> methodMatcher
        ){
            _classMatcher = classMatcher;
            _methodMatcher = methodMatcher;
            _layer = layer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void configure() {
            final ProfilingMethodInterceptor interceptor = new ProfilingMethodInterceptor(_layer);
            requestInjection(interceptor);
            bindInterceptor(_classMatcher, _methodMatcher, interceptor);
        }

    }

    @SuppressWarnings("unchecked")
    public static Matcher<Class> matchClassesContaining(final String token) {
        return new MatchClassesContainingDAOs(token);
    }

    public static Matcher<Method> matchProfileMeMethods() {
        return new MatchMethodsForProfiling();
    }

    /**
     * Creates a decorator for all matched methods in the matched classes,
     * which decorates calls to those methods with profiling information.
     *
     * <pre>
     * install(ProfilerModules.decorator(ProfilerModules.matchClassesContainingDaos(), ProfilerModules.matchProfileMeMethods()));
     * </pre>
     */
    public static Module decorator(
            final Layer layer,
            final Matcher<? super Class<?>> classMatcher,
            final Matcher<? super Method> methodMatcher
    )  {
        return new ProfilingMethodInterceptorModule(layer, classMatcher, methodMatcher);
    }

    private ProfilerModules() {
    }

    public static Module noProfiling() {
        return new NoProfilerModule();
    }

    public static Module profiling() {
        return new ProfilerModule();
    }


}
