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
package org.apache.geronimo.cli.shutdown;

import java.util.Arrays;

import org.apache.geronimo.cli.AbstractCLI;
import org.apache.geronimo.cli.CLParser;
import org.apache.geronimo.main.Bootstrapper;


/**
 * @version $Rev$ $Date$
 */
public class ShutdownCLI extends AbstractCLI {

    public static void main(String[] args) {
        int status = new ShutdownCLI(args).executeMain();
        System.exit(status);
    }

    protected ShutdownCLI(String[] args) {
        super(args, System.err);
    }
    
    @Override
    protected CLParser getCLParser() {
        return new ShutdownCLParser(System.out);
    }
    
    @Override
    protected Bootstrapper createBootstrapper() {
        Bootstrapper boot = super.createBootstrapper();
        boot.setWaitForStop(false);
        boot.setUniqueStorage(true);
        boot.setStartBundles(Arrays.asList(
                "org.apache.servicemix.bundles/org.apache.servicemix.bundles.jline//jar",
                "org.apache.servicemix.bundles/org.apache.servicemix.bundles.ant//jar",
                "org.apache.geronimo.specs/geronimo-javaee-deployment_1.1MR3_spec//jar",
                "org.apache.geronimo.framework/geronimo-deploy-config//jar",
                "org.apache.geronimo.framework/geronimo-plugin//jar",
                "org.apache.geronimo.framework/geronimo-deploy-jsr88//jar",
                "org.apache.geronimo.framework/geronimo-deploy-tool//jar",                                
                "org.apache.geronimo.framework/shutdown//car"));
        boot.setLog4jConfigFile("var/log/server-log4j.properties");
        return boot;
    }

}
