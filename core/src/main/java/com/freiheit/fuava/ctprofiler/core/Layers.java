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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides convenient constructor methods for instances of {@link Layer}.
 *
 * @author Klas Kalass (klas.kalass@freiheit.com) (initial creation)
 */
public final class Layers {
    private static final class LayerImpl implements Layer {
        private final String name;
        public LayerImpl(final String name) {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof LayerImpl) {
                final LayerImpl o = (LayerImpl)obj;
                return name.equals(o.name);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return name.hashCode();
        }
        @Override
        public String toString() {
            return name;
        }
    }
    private static final Map<String, Layer> registeredLayers = new ConcurrentHashMap<String, Layer>();

    /**
     * The Layer used if no layer was specified.
     */
    private static Layer INHERIT = forName("__inherit");
    public static Layer DEFAULT = forName("default");
    public static Layer BUSINESS = forName("business");
    public static Layer INTEGRATION = forName("integration");
    public static Layer PRESENTATION = forName("presentation");

    private Layers() {
        // utility class constructor
    }

    /**
     * Get the named layer instance.
     */
    public static Layer forName(final String name) {
        final String key = name.toLowerCase();
        Layer r = registeredLayers.get(key);
        if (r == null) {
            r = new LayerImpl(name);
            registeredLayers.put(key, r);
        }
        return r;
    }

    public static Layer inherit() {
        return INHERIT;
    }
}
