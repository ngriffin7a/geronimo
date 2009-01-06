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

package org.apache.geronimo.naming.deployment.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.Resources;

import org.apache.geronimo.j2ee.deployment.annotation.ResourceAnnotationHelper;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedWebApp;
import org.apache.geronimo.naming.deployment.EnvironmentEntryBuilder;
import org.apache.geronimo.naming.deployment.SwitchingServiceRefBuilder;
import org.apache.geronimo.testsupport.XmlBeansTestSupport;
import org.apache.geronimo.xbeans.javaee.WebAppDocument;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlOptions;

/**
 * Testcases for each of the various AnnotationHelper class
 */
public class AnnotationHelperTest extends XmlBeansTestSupport {

    private Class[] classes = {ResourceAnnotationTest.class};

    private ClassFinder classFinder = new ClassFinder(classes);
    private ClassLoader classLoader = this.getClass().getClassLoader();
    private XmlOptions options = new XmlOptions();

    public void testResourceAnnotationHelper() throws Exception {

        //-------------------------------------------------
        // Ensure annotations are discovered correctly
        //-------------------------------------------------
        List<Class> annotatedClasses = classFinder.findAnnotatedClasses(Resources.class);
        assertNotNull(annotatedClasses);
        assertEquals(1, annotatedClasses.size());
        assertTrue(annotatedClasses.contains(ResourceAnnotationTest.class));

        List<Method> annotatedMethods = classFinder.findAnnotatedMethods(Resource.class);
        assertNotNull(annotatedMethods);
        assertEquals(2, annotatedMethods.size());
        assertTrue(annotatedMethods.contains(ResourceAnnotationTest.class.getDeclaredMethod("setAnnotatedMethod1", new Class[]{String.class})));
        assertTrue(annotatedMethods.contains(ResourceAnnotationTest.class.getDeclaredMethod("setAnnotatedMethod2", new Class[]{String.class})));

        List<Field> annotatedFields = classFinder.findAnnotatedFields(Resource.class);
        assertNotNull(annotatedFields);
        assertEquals(2, annotatedFields.size());
        assertTrue(annotatedFields.contains(ResourceAnnotationTest.class.getDeclaredField("annotatedField1")));
        assertTrue(annotatedFields.contains(ResourceAnnotationTest.class.getDeclaredField("annotatedField2")));

        //-------------------------------------------------
        // Ensure annotations are processed correctly
        //-------------------------------------------------
        //
        // 2. env-entry
        //
        URL srcXML = classLoader.getResource("annotation/empty-web-src.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXML, options);
        WebAppDocument webAppDoc = (WebAppDocument) xmlObject.changeType(WebAppDocument.type);
        WebAppType webApp = webAppDoc.getWebApp();
        AnnotatedWebApp annotatedWebApp = new AnnotatedWebApp(webApp);
        ResourceAnnotationHelper.processAnnotations(annotatedWebApp, classFinder, EnvironmentEntryBuilder.EnvEntryRefProcessor.INSTANCE);
        URL expectedXML = classLoader.getResource("annotation/env-entry-expected.xml");
        XmlObject expected = XmlObject.Factory.parse(expectedXML);
        log.debug("[@Resource <env-entry> Source XML] " + '\n' + webApp.toString() + '\n');
        log.debug("[@Resource <env-entry> Expected XML]" + '\n' + expected.toString() + '\n');
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(webApp, expected, problems);
        assertTrue("Differences: " + problems, ok);
        //
        // 3. service-ref
        //
        srcXML = classLoader.getResource("annotation/empty-web-src.xml");
        xmlObject = XmlObject.Factory.parse(srcXML, options);
        webAppDoc = (WebAppDocument) xmlObject.changeType(WebAppDocument.type);
        webApp = webAppDoc.getWebApp();
        annotatedWebApp = new AnnotatedWebApp(webApp);
        ResourceAnnotationHelper.processAnnotations(annotatedWebApp, classFinder, SwitchingServiceRefBuilder.ServiceRefProcessor.INSTANCE);
        expectedXML = classLoader.getResource("annotation/service-ref-expected.xml");
        expected = XmlObject.Factory.parse(expectedXML);
        log.debug("[@Resource <service-ref> Source XML] " + '\n' + webApp.toString() + '\n');
        log.debug("[@Resource <service-ref> Expected XML]" + '\n' + expected.toString() + '\n');
        problems = new ArrayList();
        ok = compareXmlObjects(webApp, expected, problems);
        assertTrue("Differences: " + problems, ok);
    }

}