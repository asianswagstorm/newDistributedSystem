package RMIInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;

public interface CenterServer extends Remote{
	
	public String createMRecord(String firstname, String lastname,String employeeID ,String emailID ,String projectInfo ,String location) throws RemoteException, ServerNotActiveException;
	public String createERecord(String firstname, String lastname,String employeeID ,String emailID, String projectID) throws RemoteException, ServerNotActiveException;
	public String getRecordCounts() throws RemoteException;
	public boolean editRecord(String recordID,String fieldName,String newValue) throws RemoteException;
	public String printAllData() throws RemoteException;

}
