package Stubs;

import java.rmi.RemoteException;

public class CenterServerImplProxy implements Stubs.CenterServerImpl {
  private String _endpoint = null;
  private Stubs.CenterServerImpl centerServerImpl = null;
  
  public CenterServerImplProxy() {
    _initCenterServerImplProxy();
  }
  
  public CenterServerImplProxy(String endpoint) {
    _endpoint = endpoint;
    _initCenterServerImplProxy();
  }
  
  private void _initCenterServerImplProxy() {
    try {
      centerServerImpl = (new Stubs.CACenterServerImplServiceLocator()).getCenterServerImpl();
      if (centerServerImpl != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)centerServerImpl)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)centerServerImpl)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (centerServerImpl != null)
      ((javax.xml.rpc.Stub)centerServerImpl)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public Stubs.CenterServerImpl getCenterServerImpl() {
    if (centerServerImpl == null)
      _initCenterServerImplProxy();
    return centerServerImpl;
  }
  
  public int getRecordsNumber() throws java.rmi.RemoteException{
    if (centerServerImpl == null)
      _initCenterServerImplProxy();
    return centerServerImpl.getRecordsNumber();
  }
  
  public java.lang.String createERecord(java.lang.String managerID, java.lang.String firstname, java.lang.String lastname, java.lang.String employeeID, java.lang.String emailID, java.lang.String projectID) throws java.rmi.RemoteException{
    if (centerServerImpl == null)
      _initCenterServerImplProxy();
    return centerServerImpl.createERecord(managerID, firstname, lastname, employeeID, emailID, projectID);
  }
  
  public java.lang.String getRecordCounts(java.lang.String managerID) throws java.rmi.RemoteException{
    if (centerServerImpl == null)
      _initCenterServerImplProxy();
    return centerServerImpl.getRecordCounts(managerID);
  }
  
  public boolean editRecord(java.lang.String managerID, java.lang.String recordID, java.lang.String fieldName, java.lang.String newValue) throws java.rmi.RemoteException{
    if (centerServerImpl == null)
      _initCenterServerImplProxy();
    return centerServerImpl.editRecord(managerID, recordID, fieldName, newValue);
  }
  
  public boolean transferRecord(java.lang.String managerID, java.lang.String recordID, java.lang.String remoteCenterServerName) throws java.rmi.RemoteException{
    if (centerServerImpl == null)
      _initCenterServerImplProxy();
    return centerServerImpl.transferRecord(managerID, recordID, remoteCenterServerName);
  }
  
  public java.lang.String createMRecord(java.lang.String managerID, java.lang.String firstname, java.lang.String lastname, java.lang.String employeeID, java.lang.String emailID, Records.ProjectInfo projectInfo, java.lang.String location) throws java.rmi.RemoteException{
    if (centerServerImpl == null)
      _initCenterServerImplProxy();
    return centerServerImpl.createMRecord(managerID, firstname, lastname, employeeID, emailID, projectInfo, location);
  }

@Override
public String printAllData() throws RemoteException {
    if (centerServerImpl == null)
        _initCenterServerImplProxy();
      return centerServerImpl.printAllData();
}
  
  
}