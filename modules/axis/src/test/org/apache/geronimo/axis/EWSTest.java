/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.axis;

import org.apache.geronimo.ews.ws4j2ee.toWs.Ws4J2ee;

import java.io.File;

public class EWSTest extends AbstractTestCase {
    /**
     * @param testName
     */
    public EWSTest(String testName) {
        super(testName);
    }

    public void testEcho() throws Exception {
        System.out.println(new File(".").getAbsolutePath());
        GeronimoWsDeployContext deployContext =
            new GeronimoWsDeployContext(
                getTestFile("target/samples/echo.jar"),
                outDir);
        Ws4J2ee ws4j2ee = new Ws4J2ee(deployContext, null);
        ws4j2ee.generate();
    }

    protected void setUp() throws Exception {
        new File(outDir).mkdirs();
    }

    protected void tearDown() throws Exception {
    }

}
