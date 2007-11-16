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
package org.apache.geronimo.system.configuration;

import java.beans.PropertyEditor;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.apache.geronimo.system.configuration.condition.JexlExpressionParser;
import org.apache.geronimo.system.plugin.PluginXmlUtil;
import org.apache.geronimo.system.plugin.model.AttributeType;
import org.apache.geronimo.system.plugin.model.GbeanType;
import org.apache.geronimo.system.plugin.model.ReferenceType;
import org.apache.geronimo.util.EncryptionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @version $Rev$ $Date$
 */
public class GBeanOverride implements Serializable {

    private static final Log log = LogFactory.getLog(GBeanOverride.class);

    public static final String ATTRIBUTE_NAMESPACE = "http://geronimo.apache.org/xml/ns/attributes-1.2";
    private final Object name;
    private String comment;
    private boolean load;
    private final Map<String, String> attributes = new LinkedHashMap<String, String>();
    private final Map<String, ReferencePatterns> references = new LinkedHashMap<String, ReferencePatterns>();
    private final ArrayList<String> clearAttributes = new ArrayList<String>();
    private final ArrayList<String> nullAttributes = new ArrayList<String>();
    private final ArrayList<String> clearReferences = new ArrayList<String>();
    private final String gbeanInfo;
    private final JexlExpressionParser expressionParser;

    public GBeanOverride(String name, boolean load, JexlExpressionParser expressionParser) {
        this.name = name;
        this.load = load;
        gbeanInfo = null;
        this.expressionParser = expressionParser;
    }

    public GBeanOverride(AbstractName name, boolean load, JexlExpressionParser expressionParser) {
        this.name = name;
        this.load = load;
        gbeanInfo = null;
        this.expressionParser = expressionParser;
    }

    public GBeanOverride(GBeanOverride original, String oldArtifact, String newArtifact) {
        Object name = original.name;
        if (name instanceof String) {
            name = replace((String) name, oldArtifact, newArtifact);
        } else if (name instanceof AbstractName) {
            String value = name.toString();
            value = replace(value, oldArtifact, newArtifact);
            name = new AbstractName(URI.create(value));
        }
        this.name = name;
        this.load = original.load;
        this.comment = original.comment;
        this.attributes.putAll(original.attributes);
        this.references.putAll(original.references);
        this.clearAttributes.addAll(original.clearAttributes);
        this.nullAttributes.addAll(original.nullAttributes);
        this.clearReferences.addAll(original.clearReferences);
        this.gbeanInfo = original.gbeanInfo;
        this.expressionParser = original.expressionParser;
    }

    private static String replace(String original, String oldArtifact, String newArtifact) {
        int pos = original.indexOf(oldArtifact);
        if (pos == -1) {
            return original;
        }
        int last = -1;
        StringBuffer buf = new StringBuffer();
        while (pos > -1) {
            buf.append(original.substring(last + 1, pos));
            buf.append(newArtifact);
            last = pos + oldArtifact.length() - 1;
            pos = original.indexOf(oldArtifact, last);
        }
        buf.append(original.substring(last + 1));
        return buf.toString();
    }

    public GBeanOverride(GBeanData gbeanData, JexlExpressionParser expressionParser, ClassLoader classLoader) throws InvalidAttributeException {
        GBeanInfo gbeanInfo = gbeanData.getGBeanInfo();
        this.gbeanInfo = gbeanInfo.getSourceClass();
        if (this.gbeanInfo == null) {
            throw new IllegalArgumentException("GBeanInfo must have a source class set");
        }
        name = gbeanData.getAbstractName();
        load = true;

        // set attributes
        for (Object o : gbeanData.getAttributes().entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String attributeName = (String) entry.getKey();
            GAttributeInfo attributeInfo = gbeanInfo.getAttribute(attributeName);
            if (attributeInfo == null) {
                throw new InvalidAttributeException("No attribute: " + attributeName + " for gbean: " + gbeanData.getAbstractName());
            }
            Object attributeValue = entry.getValue();
            setAttribute(attributeName, attributeValue, attributeInfo.getType(), classLoader);
        }

        // references can be coppied in blind
        references.putAll(gbeanData.getReferences());
        this.expressionParser = expressionParser;
    }

    public GBeanOverride(GbeanType gbean, JexlExpressionParser expressionParser) throws InvalidGBeanException {
        String nameString = gbean.getName();
        if (nameString.indexOf('?') > -1) {
            name = new AbstractName(URI.create(nameString));
        } else {
            name = nameString;
        }

        String gbeanInfoString = gbean.getGbeanInfo();
        if (gbeanInfoString != null && gbeanInfoString.length() > 0) {
            gbeanInfo = gbeanInfoString;
        } else {
            gbeanInfo = null;
        }
        if (gbeanInfo != null && !(name instanceof AbstractName)) {
            throw new InvalidGBeanException("A gbean element using the gbeanInfo attribute must be specified using a full AbstractName: name=" + nameString);
        }

        load = gbean.isLoad();
        comment = gbean.getComment();

        // attributes
        for (Object o : gbean.getAttributeOrReference()) {
            if (o instanceof AttributeType) {
                AttributeType attr = (AttributeType) o;
                if (attr.isNull()) {
                    getNullAttributes().add(attr.getName());
                } else {
                    String value;
                    try {
                        value = PluginXmlUtil.extractAttributeValue(attr);
                    } catch (JAXBException e) {
                        throw new InvalidGBeanException("Could not extract attribute value from gbean override", e);
                    } catch (XMLStreamException e) {
                        throw new InvalidGBeanException("Could not extract attribute value from gbean override", e);
                    }
                    if (value == null || value.length() == 0) {
                        setClearAttribute(attr.getName());
                    } else {
                        String truevalue = (String) EncryptionManager.decrypt(value);
                        getAttributes().put(attr.getName(), truevalue);
                    }
                }
            } else if (o instanceof ReferenceType) {
                ReferenceType ref = (ReferenceType) o;
                if (ref.getPattern().isEmpty()) {
                    setClearReference(ref.getName());
                } else {
                    Set<AbstractNameQuery> patternSet = new HashSet<AbstractNameQuery>();
                    for (ReferenceType.Pattern pattern : ref.getPattern()) {
                        String groupId = pattern.getGroupId();
                        String artifactId = pattern.getArtifactId();
                        String version = pattern.getVersion();
                        String type = pattern.getType();
                        String module = pattern.getModule();
                        String name = pattern.getName();

                        Artifact referenceArtifact = null;
                        if (artifactId != null) {
                            referenceArtifact = new Artifact(groupId, artifactId, version, type);
                        }
                        Map<String, String> nameMap = new HashMap<String, String>();
                        if (module != null) {
                            nameMap.put("module", module);
                        }
                        if (name != null) {
                            nameMap.put("name", name);
                        }
                        AbstractNameQuery abstractNameQuery = new AbstractNameQuery(referenceArtifact, nameMap, Collections.EMPTY_SET);
                        patternSet.add(abstractNameQuery);
                    }
                    ReferencePatterns patterns = new ReferencePatterns(patternSet);
                    setReferencePatterns(ref.getName(), patterns);
                }
            }
        }
        this.expressionParser = expressionParser;
    }

    public Object getName() {
        return name;
    }

    public String getGBeanInfo() {
        return gbeanInfo;
    }

    public boolean isLoad() {
        return load;
    }

    public void setLoad(boolean load) {
        this.load = load;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    public ArrayList<String> getClearAttributes() {
        return clearAttributes;
    }

    public ArrayList<String> getNullAttributes() {
        return nullAttributes;
    }

    public boolean getNullAttribute(String attributeName) {
        return nullAttributes.contains(attributeName);
    }

    public boolean getClearAttribute(String attributeName) {
        return clearAttributes.contains(attributeName);
    }

    public ArrayList<String> getClearReferences() {
        return clearReferences;
    }

    public boolean getClearReference(String referenceName) {
        return clearReferences.contains(referenceName);
    }

    public void setClearAttribute(String attributeName) {
        if (!clearAttributes.contains(attributeName))
            clearAttributes.add(attributeName);
    }

    public void setNullAttribute(String attributeName) {
        if (!nullAttributes.contains(attributeName))
            nullAttributes.add(attributeName);
    }

    public void setClearReference(String referenceName) {
        if (!clearReferences.contains(referenceName))
            clearReferences.add(referenceName);
    }

    public void setAttribute(String attributeName, Object attributeValue, String attributeType, ClassLoader classLoader) throws InvalidAttributeException {
        String stringValue = getAsText(attributeValue, attributeType, classLoader);
        setAttribute(attributeName, stringValue);
    }

    public void setAttribute(String attributeName, String attributeValue) {
        if (attributeValue == null || attributeValue.length() == 0) {
            clearAttributes.add(attributeName);
        } else {
            attributes.put(attributeName, attributeValue);
        }
    }

    public Map<String, ReferencePatterns> getReferences() {
        return references;
    }

    public ReferencePatterns getReferencePatterns(String name) {
        return references.get(name);
    }

    public void setReferencePatterns(String name, ReferencePatterns patterns) {
        references.put(name, patterns);
    }

    public boolean applyOverrides(GBeanData data, Artifact configName, AbstractName gbeanName, ClassLoader classLoader) throws InvalidConfigException {
        if (!isLoad()) {
            return false;
        }

        GBeanInfo gbeanInfo = data.getGBeanInfo();

        // set attributes
        for (Map.Entry<String, String> entry : getAttributes().entrySet()) {
            String attributeName = entry.getKey();
            GAttributeInfo attributeInfo = gbeanInfo.getAttribute(attributeName);
            if (attributeInfo == null) {
                throw new InvalidConfigException("No attribute: " + attributeName + " for gbean: " + data.getAbstractName());
            }
            String valueString = entry.getValue();
            Object value = getValue(attributeInfo, valueString, configName, gbeanName, classLoader);
            data.setAttribute(attributeName, value);
        }

        //Clear attributes
        for (String attribute : getClearAttributes()) {
            if (getClearAttribute(attribute)) {
                data.clearAttribute(attribute);
            }
        }

        //Null attributes
        for (String attribute : getNullAttributes()) {
            if (getNullAttribute(attribute)) {
                data.setAttribute(attribute, null);
            }
        }

        // set references
        for (Map.Entry<String, ReferencePatterns> entry : getReferences().entrySet()) {

            String referenceName = entry.getKey();
            GReferenceInfo referenceInfo = gbeanInfo.getReference(referenceName);
            if (referenceInfo == null) {
                throw new InvalidConfigException("No reference: " + referenceName + " for gbean: " + data.getAbstractName());
            }

            ReferencePatterns referencePatterns = entry.getValue();

            data.setReferencePatterns(referenceName, referencePatterns);
        }

        //Clear references
        for (String reference : getClearReferences()) {
            if (getClearReference(reference)) {
                data.clearReference(reference);
            }
        }

        return true;
    }

    private synchronized Object getValue(GAttributeInfo attribute, String value, Artifact configurationName, AbstractName gbeanName, ClassLoader classLoader) {
        if (value == null) {
            return null;
        }
        value = substituteVariables(attribute.getName(), value);
        try {
            PropertyEditor editor = PropertyEditors.findEditor(attribute.getType(), classLoader);
            if (editor == null) {
                log.debug("Unable to parse attribute of type " + attribute.getType() + "; no editor found");
                return null;
            }
            editor.setAsText(value);
            log.debug("Setting value for " + configurationName + "/" + gbeanName + "/" + attribute.getName() + " to value " + value);
            return editor.getValue();
        } catch (ClassNotFoundException e) {
            log.error("Unable to load attribute type " + attribute.getType());
            return null;
        }
    }

    public String substituteVariables(String attributeName, String input) {
        if (expressionParser != null) {
            return expressionParser.parse(input);
        }
        return input;
    }

    /**
     * Creates a new child of the supplied parent with the data for this
     * GBeanOverride, adds it to the parent, and then returns the new
     * child element.
     *
     * @return newly created element for this override
     */
    public GbeanType writeXml() {
        GbeanType gbean = new GbeanType();
        String gbeanName;
        if (name instanceof String) {
            gbeanName = (String) name;
        } else {
            gbeanName = name.toString();
        }
        gbean.setName(gbeanName);
        if (gbeanInfo != null) {
            gbean.setGbeanInfo(gbeanInfo);
        }
        if (!load) {
            gbean.setLoad(false);
        }
        if (comment != null) {
            gbean.setComment(comment);
        }

        // attributes
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                setNullAttribute(name);
            } else {
                if (getNullAttribute(name)) {
                    nullAttributes.remove(name);
                }
                if (name.toLowerCase().indexOf("password") > -1) {
                    value = EncryptionManager.encrypt(value);
                }
/**
 * if there was a value such as jdbc url with &amp; then when that value was oulled
 * from the config.xml the &amp; would have been replaced/converted to '&', we need to check
 * and change it back because an & would create a parse exception.
 */
                value = "<attribute xmlns='" + ATTRIBUTE_NAMESPACE + "'>" + value.replaceAll("&(?!amp;)", "&amp;") + "</attribute>";
                Reader reader = new StringReader(value);
                try {
                    AttributeType attribute = PluginXmlUtil.loadAttribute(reader);
                    attribute.setName(name);
                    gbean.getAttributeOrReference().add(attribute);
                } catch (Exception e) {
                    log.error("Could not serialize attribute " + name + " in gbean " + gbeanName + ", value: " + value, e);
                }
            }
        }

        // cleared attributes
        for (String name : clearAttributes) {
            AttributeType attribute = new AttributeType();
            attribute.setName(name);
            gbean.getAttributeOrReference().add(attribute);
        }

        // Null attributes
        for (String name : nullAttributes) {
            AttributeType attribute = new AttributeType();
            attribute.setName(name);
            attribute.setNull(true);
            gbean.getAttributeOrReference().add(attribute);
        }

        // references
        for (Map.Entry<String, ReferencePatterns> entry : references.entrySet()) {
            String name = entry.getKey();
            ReferencePatterns patterns = entry.getValue();
            ReferenceType reference = new ReferenceType();
            reference.setName(name);

            Set<AbstractNameQuery> patternSet;
            if (patterns.isResolved()) {
                patternSet = Collections.singleton(new AbstractNameQuery(patterns.getAbstractName()));
            } else {
                patternSet = patterns.getPatterns();
            }

            for (AbstractNameQuery pattern : patternSet) {
                ReferenceType.Pattern patternType = new ReferenceType.Pattern();
                Artifact artifact = pattern.getArtifact();

                if (artifact != null) {
                    if (artifact.getGroupId() != null) {
                        patternType.setGroupId(artifact.getGroupId());
                    }
                    if (artifact.getArtifactId() != null) {
                        patternType.setArtifactId(artifact.getArtifactId());
                    }
                    if (artifact.getVersion() != null) {
                        patternType.setVersion(artifact.getVersion().toString());
                    }
                    if (artifact.getType() != null) {
                        patternType.setType(artifact.getType());
                    }
                }

                Map nameMap = pattern.getName();
                if (nameMap.get("module") != null) {
                    patternType.setModule((String) nameMap.get("module"));
                }

                if (nameMap.get("name") != null) {
                    patternType.setName((String) nameMap.get("name"));
                }
                reference.getPattern().add(patternType);
            }
            gbean.getAttributeOrReference().add(reference);
        }

        // cleared references
        for (String name : clearReferences) {
            ReferenceType reference = new ReferenceType();
            reference.setName(name);
            gbean.getAttributeOrReference().add(reference);
        }

        return gbean;
    }

    public Element writeXml(Document doc, Element parent) {
        String gbeanName;
        if (name instanceof String) {
            gbeanName = (String) name;
        } else {
            gbeanName = name.toString();
        }

        Element gbean = doc.createElement("gbean");
        parent.appendChild(gbean);
        gbean.setAttribute("name", gbeanName);
        if (gbeanInfo != null) {
            gbean.setAttribute("gbeanInfo", gbeanInfo);
        }
        if (!load) {
            gbean.setAttribute("load", "false");
        }

        // attributes
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                setNullAttribute(name);
            } else {
                if (getNullAttribute(name)) {
                    nullAttributes.remove(name);
                }
                if (name.toLowerCase().indexOf("password") > -1) {
                    value = EncryptionManager.encrypt(value);
                }
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", name);
                gbean.appendChild(attribute);
                if (value.length() == 0) {
                    attribute.setAttribute("value", "");
                } else {
                    try {
                        //
                        // NOTE: Construct a new document to handle mixed content attribute values
                        //       then add nodes which are children of the first node.  This allows
                        //       value to be XML or text.
                        //

                        DocumentBuilderFactory factory = XmlUtil.newDocumentBuilderFactory();
                        DocumentBuilder builder = factory.newDocumentBuilder();

                        /**
                         * if there was a value such as jdbc url with &amp; then when that value was oulled
                         * from the config.xml the &amp; would have been replaced/converted to '&', we need to check
                         * and change it back because an & would create a parse exception.
                         */
                        value = value.replaceAll("&(?!amp;)", "&amp;");

//                        String unsubstitutedValue = unsubstitutedAttributes.get(name);
//                        if (unsubstitutedValue != null) {
//                            log.debug("writeXML attribute " + name
//                                    + " using raw value "
//                                    + unsubstitutedValue
//                                    + " instead of cooked value "
//                                    + value + ".");
//                            value = unsubstitutedValue;
//                        }

                        // Wrap value in an element to be sure we can handle xml or text values
                        String xml = "<fragment>" + value + "</fragment>";
                        InputSource input = new InputSource(new StringReader(xml));
                        Document fragment = builder.parse(input);

                        Node root = fragment.getFirstChild();
                        NodeList children = root.getChildNodes();
                        for (int i = 0; i < children.getLength(); i++) {
                            Node child = children.item(i);

                            // Import the child (and its children) into the new document
                            child = doc.importNode(child, true);
                            attribute.appendChild(child);
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Failed to write attribute value fragment: " + e.getMessage(), e);
                    }
                }
            }
        }

        // cleared attributes
        for (String name : clearAttributes) {
            Element attribute = doc.createElement("attribute");
            gbean.appendChild(attribute);
            attribute.setAttribute("name", name);
        }

        // Null attributes
        for (String name : nullAttributes) {
            Element attribute = doc.createElement("attribute");
            gbean.appendChild(attribute);
            attribute.setAttribute("name", name);
            attribute.setAttribute("null", "true");
        }

        // references
        for (Map.Entry<String, ReferencePatterns> entry : references.entrySet()) {
            String name = entry.getKey();
            ReferencePatterns patterns = entry.getValue();

            Element reference = doc.createElement("reference");
            reference.setAttribute("name", name);
            gbean.appendChild(reference);

            Set<AbstractNameQuery> patternSet;
            if (patterns.isResolved()) {
                patternSet = Collections.singleton(new AbstractNameQuery(patterns.getAbstractName()));
            } else {
                patternSet = patterns.getPatterns();
            }

            for (AbstractNameQuery pattern : patternSet) {
                Element pat = doc.createElement("pattern");
                reference.appendChild(pat);
                Artifact artifact = pattern.getArtifact();

                if (artifact != null) {
                    if (artifact.getGroupId() != null) {
                        Element group = doc.createElement("groupId");
                        group.appendChild(doc.createTextNode(artifact.getGroupId()));
                        pat.appendChild(group);
                    }
                    if (artifact.getArtifactId() != null) {
                        Element art = doc.createElement("artifactId");
                        art.appendChild(doc.createTextNode(artifact.getArtifactId()));
                        pat.appendChild(art);
                    }
                    if (artifact.getVersion() != null) {
                        Element version = doc.createElement("version");
                        version.appendChild(doc.createTextNode(artifact.getVersion().toString()));
                        pat.appendChild(version);
                    }
                    if (artifact.getType() != null) {
                        Element type = doc.createElement("type");
                        type.appendChild(doc.createTextNode(artifact.getType()));
                        pat.appendChild(type);
                    }
                }

                Map nameMap = pattern.getName();
                if (nameMap.get("module") != null) {
                    Element module = doc.createElement("module");
                    module.appendChild(doc.createTextNode(nameMap.get("module").toString()));
                    pat.appendChild(module);
                }

                if (nameMap.get("name") != null) {
                    Element patName = doc.createElement("name");
                    patName.appendChild(doc.createTextNode(nameMap.get("name").toString()));
                    pat.appendChild(patName);
                }
            }
        }

        // cleared references
        for (String name : clearReferences) {
            Element reference = doc.createElement("reference");
            reference.setAttribute("name", name);
            gbean.appendChild(reference);
        }

        return gbean;
    }

    public static String getAsText(Object value, String type, ClassLoader classLoader) throws InvalidAttributeException {
        try {
            String attributeStringValue = null;
            if (value != null) {
                PropertyEditor editor = PropertyEditors.findEditor(type, classLoader);
                if (editor == null) {
                    throw new InvalidAttributeException("Unable to format attribute of type " + type + "; no editor found");
                }
                editor.setValue(value);
                attributeStringValue = editor.getAsText();
            }
            return attributeStringValue;
        } catch (ClassNotFoundException e) {
            //todo: use the Configuration's ClassLoader to load the attribute, if this ever becomes an issue
            throw (InvalidAttributeException) new InvalidAttributeException("Unable to store attribute type " + type).initCause(e);
        }
    }
}
