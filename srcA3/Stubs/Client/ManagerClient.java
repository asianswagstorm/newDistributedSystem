package Client;

import Records.ProjectInfo;
import Server.CenterServer;
import Stubs.CenterServerImpl;
import Utils.Config;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.io.IOException;

import java.net.URL;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import static Utils.Config.*;

public class ManagerClient  {
	private static Logger LOGGER;
	private static String managerID;
	private static String TransferServer;
	private static String Location = null;
	public static CenterServer demsServer;
	private static String FirstName;
	private static String LastName;
	private static String fieldName;
	private static String employeeID;
	private static String recordIDtoEdit;
	private static String newValue;
	private static String mailID;
	private static String ProjectID ="";
	private static ProjectInfo projectInfo =null;
	private static int option;
	private static Scanner scanner = new Scanner(System.in);
	//private static Config.Server_ID serverID;

	protected ManagerClient(String newmanagerID) throws IOException {
	        managerID = newmanagerID;  
	    }
	
	 
	
	public static void main(String args[]) throws Exception {
	
		singleThread();
	
	}
	
	public static void singleThread() throws Exception{
		ManagerLogin();
		Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 2).toUpperCase()); 
		ManagerClient client = new ManagerClient(managerID);
		demsServer = connectToServer(serverID);
		System.out.println("server id is "+serverID);
	
		do {
			boolean validateOption=false;
			while(!validateOption) {
				System.out.println("Enter Your Choice");
				option = scanner.nextInt();
				scanner.nextLine();
			if( option >= 0 && option <= 7) {
					validateOption = true;
					
				}else{System.out.println("Invalid Option");}
				}
			
			switch (option) {

			case 0:
				options();
				break;
				//Create Employee Record
			case 1:
				client.createERecord(demsServer);
				break;
				//Create Manager record
			case 2:
				client.createMRecord(demsServer);
				break;
			case 3:
				client.editRecord(demsServer);
				break;
			case 4:
				client.getRecordCount(demsServer);
				break;

			case 5:
				client.transferRecord (demsServer);
				break;
			case 6:
				System.out.println(managerID + " has logged off." );
				//fileHandler.close();
				ManagerLogin();
				client = new ManagerClient(managerID);
				serverID = Config.Server_ID.valueOf(managerID.substring(0, 2).toUpperCase()); 
				demsServer = connectToServer(serverID);
			
				break;
			default:
				break;
			}

		} while (option != 7);
	}
	
	private static CenterServer connectToServer(Config.Server_ID serverID) throws Exception {
		switch (serverID) {
        case CA: {
           Stubs.CACenterServerImplServiceLocator locator = new Stubs.CACenterServerImplServiceLocator();
           //return  locator.getCenterServerImpl();
		
           URL url = new URL(locator.getCenterServerImplAddress());
			QName qName = new QName("http://Server/","CenterServerImplService");
			Service service = Service.create(url, qName);
			return service.getPort(CenterServer.class);
        }
        case US: {
           Stubs.USCenterServerImplServiceLocator locator = new Stubs.USCenterServerImplServiceLocator();
           //return  locator.getCenterServerImpl();
			URL url = new URL(locator.getCenterServerImplAddress());
			QName qName = new QName("http://Server/","CenterServerImplService");
			Service service = Service.create(url, qName);
			return service.getPort(CenterServer.class);
        }
        case UK: {
           Stubs.UKCenterServerImplServiceLocator locator = new Stubs.UKCenterServerImplServiceLocator();
           // return  locator.getCenterServerImpl();
			URL url = new URL(locator.getCenterServerImplAddress());
			QName qName = new QName("http://Server/", "CenterServerImplService");
			Service service = Service.create(url, qName);
			return service.getPort(CenterServer.class);
        }
    }
		return null;
  
	}
	public void createERecord(CenterServer demsServer)  {
		System.out.println("Enter Employee's First Name");
		FirstName = scanner.next();
		scanner.nextLine();

		System.out.println("Enter Employee's Last Name");
		LastName = scanner.next();
		scanner.nextLine();

		System.out.println("Enter Employee ID");
		boolean validateEmployeeId=false;
		while(!validateEmployeeId) {
			employeeID = scanner.next();
			if( employeeID.substring(0,1).equalsIgnoreCase("E")) {
				validateEmployeeId=true;
				break;
			}

			else	{
				System.out.println("EmployeeID must start with E. ex: E0001");
			}
		}

		scanner.nextLine();
		System.out.println("Enter the Email");
		mailID = scanner.nextLine();

		System.out.println("Enter the Project ID");
		boolean validProjectID = false;
		while(!validProjectID) {
			ProjectID = scanner.next();
			scanner.nextLine();
			if( ProjectID.substring(0,1).equalsIgnoreCase("P")) {
				validProjectID=true;
				break;
			}
			else{
				System.out.println("ProjectID must start with P. ex: P0001");
			}
		}
		
		String recordID = demsServer.createERecord(managerID, FirstName, LastName, employeeID, mailID, ProjectID );  
		demsServer.printAllData();

		if (recordID != null) {
			System.out.println("Employee Record Created Successfully");
			LOGGER.info("Employee Record created Successfully from Manager: " +
					String.format(Config.LOG_ADD_EMPLOYEE_RECORD, managerID, recordID, FirstName, LastName, employeeID, mailID, ProjectID));

		} else {
			System.out.println("Employee Record was not created");
			LOGGER.info("Employee Record not created");
		}
	}
	public void createMRecord(CenterServer demsServer)  {
		String ClientName="";
		String ProjectName="";
	
		boolean validateEmployeeId=false;
		boolean validProjectID=false;
		
		System.out.println("Enter Manager's First Name");
		FirstName = scanner.next();
		
		System.out.println("Enter Manager's Last Name");
		LastName = scanner.next();
		
		validateEmployeeId = false;
		while(!validateEmployeeId) {
			System.out.println("Enter Manager's Employee ID");
			employeeID = scanner.next();
			if( employeeID.substring(0,1).equalsIgnoreCase("M")) {
				validateEmployeeId=true;
				break;
			}

			else	{
				System.out.println("EmployeeID must start with M. ex: M0001");
			}
		}
		
		System.out.println("Enter the Email");
		mailID = scanner.next();

		projectInfo = new ProjectInfo(ProjectID, ClientName, ProjectName);
		System.out.println("Enter Project ID, Client Name , Project Name");

		validProjectID = false;
		while(!validProjectID) {
			ProjectID = scanner.next();
			if( ProjectID.substring(0,1).equalsIgnoreCase("P")) {
				validProjectID=true;
				projectInfo.setProjectId(ProjectID);
				break;
			}
			else{
				System.out.println("ProjectID must start with P. ex: P0001");
			}

		}
		scanner.nextLine();
		ClientName = scanner.nextLine() ;
		projectInfo.setClientName(ClientName);
		ProjectName = scanner.nextLine() ;
		projectInfo.setProjectName(ProjectName);
		
		//Validating Location
		boolean validateLocation=false;
		while(!validateLocation){
			System.out.println("Enter the location");
			String locationScanned = scanner.next();
			if (locationScanned.equalsIgnoreCase("CA")||locationScanned.equalsIgnoreCase("US")||locationScanned.equalsIgnoreCase("UK")) {
				Location=locationScanned;
				validateLocation=true;
				break;
			}
			else{
				System.out.println("Location must be CA ,US or UK");
			}
		}
		
		String recordID = demsServer.createMRecord(managerID, FirstName, LastName, employeeID, mailID, projectInfo, Location);
		demsServer.printAllData();
		
		//Print to Manager_ServerID
		if (recordID != null) {
			String ProjectInfoString = "[ " +projectInfo.getProjectId() +" , " +projectInfo.getClientName()+ " , " + projectInfo.getProjectName() + " ]";
			System.out.println("Manager record created Successfully");
			LOGGER.info("Manager Record created Succesffully by Manager: " +
					String.format(Config.LOG_ADD_MANAGER_RECORD,managerID, recordID, FirstName, LastName, employeeID, mailID, ProjectInfoString, Location)	);

		} else {
			System.out.println("Manager Record not created");
			LOGGER.info("Manager Record not created");
		}
	}
	
	public void editRecord(CenterServer demsServer) throws RemoteException  {
		boolean validaterecordID=false;
		System.out.println("Enter the record ID you wish to Edit ER***** or MR*****");
		int fieldChoice;
		while(!validaterecordID) {
    	   
    	   recordIDtoEdit = scanner.next();
    	   scanner.nextLine();
		 if(recordIDtoEdit.startsWith("ER")){
        	validaterecordID = true;
        	System.out.println("records to edit \n"
        			+ "1) mailID (Email)\n"
        			+ "2) projectID");
        	 System.out.println("Choose the Field Name you wish to Edit");
        	 fieldChoice = Integer.parseInt(scanner.nextLine());
        	 switch(fieldChoice) {
        	 case 1:
        		 fieldName = "mailID";
        	 break;
        	 case 2:
        		 fieldName = "projectID";
        		 break;
        	 }
        	 System.out.println("Enter the value you wish to replace with");
             newValue= scanner.nextLine();
            
        	break;
        }else if (recordIDtoEdit.startsWith("MR")){
        	validaterecordID = true;
        	System.out.println("records to edit \n"
        			+ "1) mailID (Email)\n"
        			+ "2) projectInfo (ProjectID, Client Name, Project Name\n"
        			+ "3) Location CA, US, UK");
        	 System.out.println("Choose the Field Name you wish to Edit");
        	 fieldChoice = Integer.parseInt(scanner.nextLine());
        	 switch(fieldChoice) {
        	 case 1:
        		 fieldName = "mailID";
        	 break;
        	 case 2:
        		 fieldName = "projectInfo";
        		 break;
        	 case 3:
        		 fieldName = "Location";
        		 break;
        	default:
        		break;
        	 }
        	 System.out.println("Enter the value you wish to replace with");
             newValue= scanner.nextLine();
             
             break;
        	
        }else System.out.println("Invalid ID");
       }
       
        //RMI
       // CenterServer editRecord = connectToRmiServer(serverID);
		boolean isEditSuccess = demsServer.editRecord(managerID, recordIDtoEdit, fieldName, newValue); 
		demsServer.printAllData();
		if (isEditSuccess) {

        	System.out.println("Edited Record " + recordIDtoEdit + " Succesffully " +fieldName +": " + newValue );
        	LOGGER.info("Edited Record " + recordIDtoEdit + " Succesffully " +fieldName +": " + newValue );
        	
        } else {
        	System.out.println("Could not Edit the Record " + recordIDtoEdit + " by: " + managerID);
        	LOGGER.info("Could not Edit Record " + recordIDtoEdit + " by: " + managerID);
        }
			
		validaterecordID=false;
	}
	public void getRecordCount (CenterServer demsServer) throws RemoteException {

		System.out.println("Record Count");
		String result = demsServer.getRecordCounts(managerID);
		System.out.println(result);//null 
		LOGGER.info(String.format(Config.GET_RECORDS_COUNT, managerID, result));
	}
	public void transferRecord (CenterServer demsServer) throws RemoteException {
		 System.out.print("Enter Record ID to be Tranfered: ");
	        String recordID = scanner.nextLine().toUpperCase();
	        System.out.print("Enter Server Name: ");
	        String serverName = scanner.nextLine().toUpperCase();
	       
	        boolean isSuccess = demsServer.transferRecord(managerID, recordID, serverName);
	        if (isSuccess) {
	            LOGGER.info(recordID + " transfered to " + serverName);
	            System.out.println(recordID + " transfered to " + serverName);
	            TransferServer= serverName;
	        }
	        else {
	            LOGGER.info(recordID + " failed to transfer to " + serverName);
	            System.out.println(recordID + " failed to transfer to " + serverName);
	        }
	}
	
	private static void Logging() throws Exception {
		//For Logging  
		
		LOGGER = Logger.getLogger(managerID);
	    LOGGER.setUseParentHandlers(false);

	    FileHandler fileHandler = new FileHandler(String.format(Config.LOG_MANAGER_FILENAME, managerID));
	    
		LOGGER.addHandler(fileHandler);
		SimpleFormatter formatter = new SimpleFormatter();
		fileHandler.setFormatter(formatter);
	}
	
	public static void ManagerLogin() throws Exception  {

		boolean validateManagerId=false;
		managerID = "";
		while(!validateManagerId){
			System.out.println("Enter your ManagerID");
			managerID=scanner.next();

			if(managerID.trim().startsWith("CA")||managerID.trim().startsWith("US")||managerID.trim().startsWith("UK")){
				validateManagerId=true;
				Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 2).toUpperCase()); 
				  
			    System.out.println(managerID + " connect to server " + serverID.name() + " successfully");
			  	
			    break;
			}
			else{
				System.out.println("Invalid Manager ID!! Manager Id should start with CA or US or UK");	
			}
		}
		Logging();
		options();
	}

	public static void options(){
		System.out.println("Choose from the  following options ");
		System.out.println("0 Print the options again");
		System.out.println("1 Create Employee Records");
		System.out.println("2 Create Manager Records");
		System.out.println("3 Edit the Records");
		System.out.println("4 Get the Record Count");
		System.out.println("5 Transfer a Record");
		System.out.println("6 Logout");
		System.out.println("7 to Exit");
	}
	
}

