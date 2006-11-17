/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.testsuite.deployment;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.DefaultSelenium;

import org.openqa.selenium.server.SeleniumServer;

/**
 * Provides custom extentions to Selenium.
 *
 * @version $Rev$ $Date$
 */
public class ExtendedSelenium
    extends DefaultSelenium
{
    public ExtendedSelenium(final String serverHost, final int serverPort, final String browserStartCommand, final String browserURL) {
        super(serverHost, serverPort, browserStartCommand, browserURL);
    }
    
    /**
     * Remove a cookie from the browser.
     *
     * <p>
     * This requires some custom hooks in <tt>user-extensions.js</tt>.  
     * When using the <tt>selenium-maven-plugin</tt> the defaults should be merged
     * into the <tt>user-extensions.js</tt> which is loaded by the server.
     */
    public void removeCookie(final String name, final String path) {
        this.getEval("selenium.removeCookie('" + name + "', '" + path + "')");
    }
}
