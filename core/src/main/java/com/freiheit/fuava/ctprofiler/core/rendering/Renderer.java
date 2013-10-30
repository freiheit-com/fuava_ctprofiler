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
import com.freiheit.fuava.ctprofiler.core.Statistics;
import com.freiheit.fuava.ctprofiler.core.TimerStatistics;

public abstract class Renderer {
    public void begin(final Statistics call) throws IOException {};

    public boolean beginPath(final NestedTimerPath root, final TimerStatistics call) throws IOException {return true;};
    public void endPath(final NestedTimerPath root, final TimerStatistics call) throws IOException {};

    public void beginSubtasks() throws IOException {};
    public void endSubtask(final Statistics subState) throws IOException {};
    public void endSubtasks() throws IOException {};
    public void beginSubtask(final Statistics subState) throws IOException {};

    public void end(final Statistics call) throws IOException {};
}