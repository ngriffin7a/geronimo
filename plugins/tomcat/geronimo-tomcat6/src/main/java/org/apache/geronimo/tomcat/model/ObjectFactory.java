//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.02 at 10:12:18 AM PDT 
//


package org.apache.geronimo.tomcat.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.apache.geronimo.tomcat.modelxx package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Server_QNAME = new QName("", "Server");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.geronimo.tomcat.modelxx
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResourceEnvRefType }
     *
     */
    public ResourceEnvRefType createResourceEnvRefType() {
        return new ResourceEnvRefType();
    }

    /**
     * Create an instance of {@link ServerType }
     *
     */
    public ServerType createServerType() {
        return new ServerType();
    }

    /**
     * Create an instance of {@link ValveType }
     *
     */
    public ValveType createValveType() {
        return new ValveType();
    }

    /**
     * Create an instance of {@link EngineType }
     *
     */
    public EngineType createEngineType() {
        return new EngineType();
    }

    /**
     * Create an instance of {@link ServiceRefType }
     *
     */
    public ServiceRefType createServiceRefType() {
        return new ServiceRefType();
    }

    /**
     * Create an instance of {@link ClusterType }
     *
     */
    public ClusterType createClusterType() {
        return new ClusterType();
    }

    /**
     * Create an instance of {@link ContextType }
     *
     */
    public ContextType createContextType() {
        return new ContextType();
    }

    /**
     * Create an instance of {@link ConnectorType }
     *
     */
    public ConnectorType createConnectorType() {
        return new ConnectorType();
    }

    /**
     * Create an instance of {@link ExecutorType }
     *
     */
    public ExecutorType createExecutorType() {
        return new ExecutorType();
    }

    /**
     * Create an instance of {@link ListenerType }
     *
     */
    public ListenerType createListenerType() {
        return new ListenerType();
    }

    /**
     * Create an instance of {@link NamingResourcesType }
     *
     */
    public NamingResourcesType createNamingResourcesType() {
        return new NamingResourcesType();
    }

    /**
     * Create an instance of {@link HostType }
     *
     */
    public HostType createHostType() {
        return new HostType();
    }

    /**
     * Create an instance of {@link TransactionType }
     *
     */
    public TransactionType createTransactionType() {
        return new TransactionType();
    }

    /**
     * Create an instance of {@link EjbType }
     *
     */
    public EjbType createEjbType() {
        return new EjbType();
    }

    /**
     * Create an instance of {@link EnvironmentType }
     *
     */
    public EnvironmentType createEnvironmentType() {
        return new EnvironmentType();
    }

    /**
     * Create an instance of {@link RealmType }
     *
     */
    public RealmType createRealmType() {
        return new RealmType();
    }

    /**
     * Create an instance of {@link ServiceType }
     *
     */
    public ServiceType createServiceType() {
        return new ServiceType();
    }

    /**
     * Create an instance of {@link LocalEjbType }
     *
     */
    public LocalEjbType createLocalEjbType() {
        return new LocalEjbType();
    }

    /**
     * Create an instance of {@link ResourceType }
     *
     */
    public ResourceType createResourceType() {
        return new ResourceType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServerType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "Server")
    public JAXBElement<ServerType> createServer(ServerType value) {
        return new JAXBElement<ServerType>(_Server_QNAME, ServerType.class, null, value);
    }

}