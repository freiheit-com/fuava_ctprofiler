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
package com.freiheit.fuava.ctprofiler.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.freiheit.fuava.ctprofiler.core.Layer;
import com.freiheit.fuava.ctprofiler.core.Layers;
import com.freiheit.fuava.ctprofiler.core.TimeKeeper;
import com.freiheit.fuava.ctprofiler.core.impl.ProfilerFactory;

/**
 * Ein Spring BeanPostProcessor, der all solche Beans mit profiling
 * versieht, die mindestens ein Interface implementieren, das auf
 * eines der angegebenen Patterns matched.
 *
 * @author Klas Kalass (klas.kalass@freiheit.com) (initial creation)
 */
public class ProfilingPostProcessor implements BeanPostProcessor {
    private List<Pattern> _patterns = Collections.emptyList();
    private Map<Pattern, Layer> _layerPatterns = Collections.emptyMap();
    private TimeKeeper _timeKeeper = ProfilerFactory.getGlobalTimeKeeper();
    private Set<String> _excludeBeanNames = Collections.emptySet();

    /**
     * Ein Spring BeanPostProcessor, der all solche Beans mit Profiling
     * versieht, die mindestens ein Interface implementieren, das auf
     * eines der angegebenen Patterns matched.
     */
    public ProfilingPostProcessor() {
    }

    public void setExcludeBeanNames(final Set<String> excludeBeanNames) {
        _excludeBeanNames = excludeBeanNames;
    }

    public void setInterfaceLayerPatterns(final Map<String, String> ifaceLayerPatterns) {
        final Map<Pattern, Layer> m = new LinkedHashMap<Pattern, Layer>();
        final List<String> patterns = new ArrayList<String>(ifaceLayerPatterns.keySet());
        Collections.sort(patterns);
        for (final String k: patterns) {
            final String v = ifaceLayerPatterns.get(k);
            m.put(Pattern.compile(k), Layers.forName(v));
        }
        _layerPatterns = m;
    }

    public void setInterfaceNamePatterns(final List<String> ifacePatterns) {
        final List<Pattern> ps = new ArrayList<Pattern>(ifacePatterns.size());
        for (final String p: ifacePatterns) {
            ps.add(Pattern.compile(p));
        }
        _patterns = ps;
    }


    public void setTimeKeeper(final TimeKeeper timeKeeper) {
        _timeKeeper = timeKeeper;

    }
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String name) throws BeansException {
        final Class<?> c = isCandidate(bean);
        if (!_excludeBeanNames.contains(name) && c != null) {
            final Layer layer = getLayer(name);
            return _timeKeeper.proxy(layer, bean);
        }
        return bean;
    }

    private Layer getLayer(final String name) {
        for (final Map.Entry<Pattern, Layer> e : _layerPatterns.entrySet()) {
            if (e.getKey().matcher(name).matches()) {
                return e.getValue();
            }
        }
        return Layers.inherit();
    }

    private boolean isCandidateIface(final Class<?> cls) {
        if (cls == null || !cls.isInterface()) {
            return false;
        }
        for (final Pattern p: getPatterns()) {
            if (p.matcher(cls.getCanonicalName()).matches()) {
                return true;
            }
        }
        return false;
    }

    private Collection<Pattern> getPatterns() {
        return _patterns.isEmpty() ? this._layerPatterns.keySet() : _patterns;
    }

    private Class<?> isCandidate(final Object bean) {
        if (bean == null) {
            return null;
        }
        return isCandidateCls(bean.getClass());
    }

    private Class<?> isCandidateCls(final Class<? extends Object> class1) {
        if (class1 == null) {
            return null;
        }
        if (isCandidateIface(class1)) {
            return class1;
        }
        for (final Class<?> iface : class1.getInterfaces()) {
            final Class<?> cc = isCandidateCls(iface);
            if (cc != null) {
                return cc;
            }
        }
        return isCandidateCls(class1.getSuperclass());
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String name) throws BeansException {
        return bean;
    }
}
