/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jmxremoting;

import java.util.HashMap;
import java.util.Map;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;

/**
 * A Connector that supports the server sideof JSR 160 JMX Remoting.
 * 
 * @version $Revision: 1.3 $ $Date: 2004/06/04 22:31:56 $
 */
public class JMXConnector implements GBean {
    private final Kernel kernel;
    private final Log log;
    private String url;
    private String applicationConfigName;

    private JMXConnectorServer server;

    /**
     * Constructor for creating the connector
     *
     * @param kernel a reference to the kernel
     */
    public JMXConnector(Kernel kernel, String objectName) {
        this.kernel = kernel;
        log = LogFactory.getLog(objectName);
    }

    /**
     * Return the name of the JAAS Application Configuration Entry this
     * connector uses to authenticate users. If null, users are not
     * be authenticated (not recommended).
     *
     * @return the authentication configuration name
     */
    public String getApplicationConfigName() {
        return applicationConfigName;
    }

    /**
     * Set the name of the JAAS Application Configuration Entry this
     * connector should use to authenticate users. If null, users will not
     * be authenticated (not recommended).
     *
     * @param applicationConfigName the authentication configuration name
     */
    public void setApplicationConfigName(String applicationConfigName) {
        this.applicationConfigName = applicationConfigName;
    }

    /**
     * Return the JMX URL for this connector.
     *
     * @return the JMX URL for this connector
     */
    public String getURL() {
        return url;
    }

    /**
     * Set the JMX URL for this connector
     *
     * @param url the JMX URL for this connector
     */
    public void setURL(String url) {
        this.url = url;
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        JMXServiceURL serviceURL = new JMXServiceURL(url);
        Map env = new HashMap();
        if (applicationConfigName != null) {
            env.put(JMXConnectorServer.AUTHENTICATOR, new Authenticator(applicationConfigName));
        } else {
            log.warn("Starting unauthenticating JMXConnector for " + serviceURL);
        }
        server = JMXConnectorServerFactory.newJMXConnectorServer(serviceURL, env, kernel.getMBeanServer());
        server.start();
        log.info("Started JMXConnector " + server.getAddress());
    }

    public void doStop() throws Exception {
        server.stop();
        server = null;
        log.info("Stopped JMXConnector " + url);
    }

    public void doFail() {
        try {
            doStop();
            log.warn("Failure in JMXConnector " + url);
        } catch (Exception e) {
            log.warn("Error stopping JMXConnector after failure", e);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(JMXConnector.class);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("URL", String.class, true);
        infoFactory.addAttribute("ApplicationConfigName", String.class, true);
        infoFactory.addReference("Kernel", Kernel.class);
        infoFactory.setConstructor(new String[]{"Kernel", "objectName"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
