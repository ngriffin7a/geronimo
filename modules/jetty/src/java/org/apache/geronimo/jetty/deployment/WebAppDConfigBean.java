/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.jetty.deployment;

import java.io.IOException;
import java.io.PrintWriter;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.deployment.plugin.j2ee.ENCHelper;
import org.apache.geronimo.deployment.util.XMLUtil;
import org.w3c.dom.Element;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/05 01:37:56 $
 */
public class WebAppDConfigBean extends DConfigBeanSupport {
    private String contextRoot;
    private boolean contextPriorityClassLoader;

    private final ENCHelper encHelper;

    WebAppDConfigBean(DDBean ddBean) {
        super(ddBean);
        encHelper = new ENCHelper(ddBean);
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        pcs.firePropertyChange("contextRoot", this.contextRoot, contextRoot);
        this.contextRoot = contextRoot;
    }
    
         
         /* ------------------------------------------------------------------------------- */
         /** getContextPriorityClassLoader.
          * @return True if this context should give web application class in preference over the containers 
          * classes, as per the servlet specification recommendations.
          */
         public boolean getContextPriorityClassLoader(){
             return contextPriorityClassLoader;
         }
     
         /* ------------------------------------------------------------------------------- */
         /** setContextPriorityClassLoader.
          * @param contextPriority True if this context should give web application class in preference over the containers 
          * classes, as per the servlet specification recommendations.
          */
         public void setContextPriorityClassLoader(boolean p){
             pcs.firePropertyChange("contextPriorityClassLoader", this.contextPriorityClassLoader, p);
               this.contextPriorityClassLoader = p;
         }



    public DConfigBean getDConfigBean(DDBean ddBean) throws ConfigurationException {
        return encHelper.getDConfigBean(ddBean);
    }

    public void removeDConfigBean(DConfigBean dcBean) throws BeanNotFoundException {
        encHelper.removeDConfigBean(dcBean);
    }

    public String[] getXpaths() {
        return ENCHelper.ENC_XPATHS;
    }

    public void toXML(PrintWriter writer) throws IOException {
        if (contextRoot != null) {
            writer.print("<context-root>");
            writer.print(contextRoot);
            writer.println("</context-root>");
            writer.print("<context-priority-classloader>");
            writer.print(contextPriorityClassLoader);
            writer.println("</context-priority-classloader>");
        }
        encHelper.toXML(writer);
    }

    public void fromXML(Element element) {
        contextRoot = XMLUtil.getChildContent(element, "context-root", null, null);
        encHelper.fromXML(element);
    }
}
