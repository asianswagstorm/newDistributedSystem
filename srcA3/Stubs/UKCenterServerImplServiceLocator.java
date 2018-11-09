/**
 * CenterServerImplServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package Stubs;

public class UKCenterServerImplServiceLocator extends org.apache.axis.client.Service implements Stubs.CenterServerImplService {

    public UKCenterServerImplServiceLocator() {
    }


    public UKCenterServerImplServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public UKCenterServerImplServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for CenterServerImpl
    private java.lang.String CenterServerImpl_address = "http://localhost:8888/CenterServerImpl/UK?wsdl";

    public java.lang.String getCenterServerImplAddress() {
        return CenterServerImpl_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String CenterServerImplWSDDServiceName = "CenterServerImpl";

    public java.lang.String getCenterServerImplWSDDServiceName() {
        return CenterServerImplWSDDServiceName;
    }

    public void setCenterServerImplWSDDServiceName(java.lang.String name) {
        CenterServerImplWSDDServiceName = name;
    }

    public Stubs.CenterServerImpl getCenterServerImpl() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(CenterServerImpl_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getCenterServerImpl(endpoint);
    }

    public Stubs.CenterServerImpl getCenterServerImpl(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
        	Stubs.CenterServerImplSoapBindingStub _stub = new Stubs.CenterServerImplSoapBindingStub(portAddress, this);
            _stub.setPortName(getCenterServerImplWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setCenterServerImplEndpointAddress(java.lang.String address) {
        CenterServerImpl_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (Server.CenterServerImpl.class.isAssignableFrom(serviceEndpointInterface)) {
            	Stubs.CenterServerImplSoapBindingStub _stub = new Stubs.CenterServerImplSoapBindingStub(new java.net.URL(CenterServerImpl_address), this);
                _stub.setPortName(getCenterServerImplWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("CenterServerImpl".equals(inputPortName)) {
            return getCenterServerImpl();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://Server", "CenterServerImplService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://Server", "CenterServerImpl"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("CenterServerImpl".equals(portName)) {
            setCenterServerImplEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
