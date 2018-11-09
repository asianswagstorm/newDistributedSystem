/**
 * CenterServerImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package Stubs;

public interface CenterServerImpl extends java.rmi.Remote {
    public java.lang.String createERecord(java.lang.String managerID, java.lang.String firstname, java.lang.String lastname, java.lang.String employeeID, java.lang.String emailID, java.lang.String projectID) throws java.rmi.RemoteException;
    public java.lang.String getRecordCounts(java.lang.String managerID) throws java.rmi.RemoteException;
    public int getRecordsNumber() throws java.rmi.RemoteException;
    public boolean editRecord(java.lang.String managerID, java.lang.String recordID, java.lang.String fieldName, java.lang.String newValue) throws java.rmi.RemoteException;
    public boolean transferRecord(java.lang.String managerID, java.lang.String recordID, java.lang.String remoteCenterServerName) throws java.rmi.RemoteException;
    public java.lang.String createMRecord(java.lang.String managerID, java.lang.String firstname, java.lang.String lastname, java.lang.String employeeID, java.lang.String emailID, Records.ProjectInfo projectInfo, java.lang.String location) throws java.rmi.RemoteException;
}
