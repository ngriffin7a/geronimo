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
package org.apache.geronimo.j2ee.deployment;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.BundleResourceContext;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 *  //TODO still needed?
 * @version $Rev:386276 $ $Date$
 */
public class BundleDeploymentContext extends EARContext {

    private Bundle bundle;

    public BundleDeploymentContext(Environment environment,
                                   ConfigurationModuleType moduleType,
                                   Naming naming,
                                   BundleContext bundleContext,
                                   AbstractNameQuery serverName,
                                   AbstractName baseName,
                                   AbstractNameQuery transactionManagerObjectName,
                                   AbstractNameQuery connectionTrackerObjectName,
                                   AbstractNameQuery corbaGBeanObjectName,
                                   Map messageDestinations,
                                   Bundle bundle) throws DeploymentException {
        super(null, null,
              environment, moduleType, naming, new BundleResourceContext(bundle), bundleContext,
              serverName, baseName, transactionManagerObjectName, connectionTrackerObjectName,
              corbaGBeanObjectName, messageDestinations);
        this.bundle = bundle;
    }


//    @Override
//    public void initializeConfiguration() throws DeploymentException {
//        try {
//            ConfigurationData configurationData = new ConfigurationData(moduleType, null, childConfigurationDatas, environment, baseDir, inPlaceConfigurationDir, naming);
//            configurationData.setBundle(bundle);
//            configurationManager.loadConfiguration(configurationData);
//            this.configuration = configurationManager.getConfiguration(environment.getConfigId());
//        } catch (Exception e) {
//            throw new DeploymentException("Unable to create configuration for deployment", e);
//        }
//    }

    @Override
    public void getCompleteManifestClassPath(Deployable deployable,
                                             URI moduleBaseUri,
                                             URI resolutionUri,
                                             Collection<String> classpath,
                                             Collection<String> exclusions) throws DeploymentException {
    }

}
