package Server;

import java.rmi.Remote;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import Records.ProjectInfo;

@WebService
@SOAPBinding(style=Style.RPC)
public interface CenterServer extends Remote{
    @WebMethod
	public String createMRecord (String managerID, String firstName, String lastName, String employeeID, String emailID, ProjectInfo projectInfo, String location);
    @WebMethod
    public String createERecord (String managerID, String firstName, String lastName, String employeeID, String emailID, String projectID);
    @WebMethod
    public String getRecordCounts (String managerID);
    @WebMethod
    public boolean editRecord (String managerID, String recordID, String fieldName, String newValue);
    @WebMethod
    public boolean transferRecord (String managerID, String recordID, String remoteCenterServerName);
    @WebMethod
    public String printAllData ();
    @WebMethod
    public String printRecord (String managerID, String recordID);
}
