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
package com.freiheit.fuava.ctprofiler.core.rendering;

import java.io.IOException;

import com.freiheit.fuava.ctprofiler.core.NestedTimerPath;
import com.freiheit.fuava.ctprofiler.core.TimerStatistics;

public class XmlRenderer extends Renderer {
    private final Appendable sb;

    public XmlRenderer(final String pre, final Appendable sb) {
        this.sb = sb;
    }


    @Override
    public boolean beginPath(final NestedTimerPath root, final TimerStatistics call) throws IOException {
        for (int i = 0; i < 2 * root.getLevel(); ++i) {
            sb.append(' ');
        }
        sb.append("<measure name=\"");
        sb.append(root.getLeafTimerName());
        sb.append("\"");
        if (call != null) {
            long rest = call.getTotalNanos();
            final long seconds = rest / 1000000000;
            rest = rest % 1000000000;
            final long millis = rest / 1000000;
            rest = rest % 1000000;
            final long micros = rest / 1000;
            final long nanos = rest % 1000;
            sb.append(" calls=\"").append(Integer.toString(call.getNumberOfCalls())).append("\"")
            .append(" seconds=\"").append(Long.toString(seconds)).append("\"")
            .append(" millis=\"").append(Long.toString(millis)).append("\"")
            .append(" micros=\"").append(Long.toString(micros)).append("\"")
            .append(" nanos=\"").append(Long.toString(nanos)).append("\"")
            .append("");
        }
        sb.append(">\n");
        return true;
    }
    @Override
    public void endPath(final NestedTimerPath root, final TimerStatistics call) throws IOException {
        sb.append("</measure>\n");
    }
}