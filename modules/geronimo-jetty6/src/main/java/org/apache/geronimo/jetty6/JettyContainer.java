/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.jetty6;

import org.apache.geronimo.management.geronimo.WebContainer;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;

/**
 * @version $Rev$ $Date$
 */
public interface JettyContainer extends WebContainer {
    void addListener(Connector listener);

    void removeListener(Connector listener);

    void addContext(ContextHandler context);

    void removeContext(ContextHandler context);

    InternalJAASJettyRealm addRealm(String realmName);

    void removeRealm(String realmName);

    void resetStatistics();

    void setCollectStatistics(boolean on);

    boolean getCollectStatistics();

    long getCollectStatisticsStarted();

    void setRequestLog(RequestLog log);

    /* ------------------------------------------------------------ */
    RequestLog getRequestLog();

}
