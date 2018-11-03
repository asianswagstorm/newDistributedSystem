package client;

import RMIInterface.CenterServer;
import Records.Employee;
import Utils.Config;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import static Utils.Config.RMI_REGISTRY_FORMAT;

public class ManagerClient  {
	private static Logger LOGGER;
	private static String managerID;
	private static String Location = null;
	public static CenterServer theServer;
	private static String FirstName;
	private static String LastName;
	private static String fieldName;
	private static String employeeID;
	private static String recordIDtoEdit;
	private static String mutable1="";
	private static String mutable2="";
	private static String newValue;
	private static String mailID;
	private static String ProjectID ="";
	private static ArrayList<String> projectInfo =null;
	private static int option;
	private static Scanner scanner = new Scanner(System.in);
	private static FileHandler fileHandler = null;
	private static Config.Server_ID serverID; 

	public static void main(String args[]) throws Exception {

		String ClientName;
		String ProjectName;
		
		boolean validaterecordID=false;
		
		ManagerLogin();
		
		boolean quit = false;
		while (!quit) {
			boolean validateOption=false;
			while(!validateOption) {
				System.out.println("Enter Your Choice");
				option = scanner.nextInt();
				scanner.nextLine();
			if( option >= 0 && option <= 5) {
					validateOption = true;
					
				}else{System.out.println("Invalid Option");}
				}
			
			switch (option) {

			case 0:
				
				options();
				break;
				//Create Employee Record
			case 1:

				System.out.println("Enter Employee's First Name");
				FirstName = scanner.next();
				scanner.nextLine();

				System.out.println("Enter Employee's Last Name");
				boolean validateLastName=false;
				while(!validateLastName) {
				LastName = scanner.next();
				if (LastName.matches("[a-zA-Z]+$")) {
					validateLastName=true;
					break;
				}else {
					System.out.println("Invalid Lastname, must not contain numbers");
				}
				}
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

				CenterServer createEmployeeRecord = connectToRmiServer(serverID);
				String recordID = createEmployeeRecord.createERecord(FirstName, LastName, employeeID, mailID, ProjectID );  
				createEmployeeRecord.printData();

				if (recordID != null) {
					System.out.println("Employee Record Created Successfully");
					LOGGER.info("Employee Record created Successfully from Manager: ( " + managerID +" ) " +
							String.format(Config.LOG_ADD_EMPLOYEE_RECORD, recordID, FirstName, LastName, employeeID, mailID, ProjectID));

				} else {
					System.out.println("Employee Record was not created");
					LOGGER.info("Employee Record not created");
				}

				break;
				//Create Manager record
			case 2:

				System.out.println("Enter Manager's First Name");
				FirstName = scanner.next();

				System.out.println("Enter Manager's Last Name");
				validateLastName=false;
				while(!validateLastName) {
				LastName = scanner.next();
				if (LastName.matches("[a-zA-Z]+$")) {
					validateLastName=true;
					break;
				}else {
					System.out.println("Invalid Lastname, must not contain numbers");
				}
				}
				
				
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
				//scanner.nextLine();

				System.out.println("Enter the Email");
				mailID = scanner.next();
				//scanner.nextLine();

				projectInfo = new ArrayList<String>();
				System.out.println("Enter Project ID, Client Name , Project Name");

				validProjectID = false;
				while(!validProjectID) {
					ProjectID = scanner.next();
					if( ProjectID.substring(0,1).equalsIgnoreCase("P")) {
						validProjectID=true;
						projectInfo.add(ProjectID);
						break;
					}
					else{
						System.out.println("ProjectID must start with P. ex: P0001");
					}

				}
				scanner.nextLine();
				ClientName = scanner.nextLine() ;
				projectInfo.add(ClientName);
				ProjectName = scanner.nextLine() ;
				projectInfo.add(ProjectName);

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
				CenterServer createManagerRecord = connectToRmiServer(serverID);
				recordID = createManagerRecord.createMRecord(FirstName, LastName, employeeID, mailID, projectInfo, Location); 
				createManagerRecord.printData();
				//Print to Manager_ServerID
				if (recordID != null) {
					System.out.println("Manager record created Successfully");
					LOGGER.info("Manager Record created Succesffully by Manager: ( " + managerID + " ) " +
							String.format(Config.LOG_ADD_MANAGER_RECORD, recordID, FirstName, LastName, employeeID, mailID, projectInfo, Location)	);

				} else {
					System.out.println("Manager Record not created");
					LOGGER.info("Manager Record not created");
				}
				break;
			case 3:
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
                    
                     if( fieldName == "mailID") {
                    	 mailID = newValue;}
                         
                     else if( fieldName == "projectID") {
                    	 ProjectID = newValue;}
                     
                  LOGGER.info("Edited " + fieldName + "  Succesffully by : " +
                 String.format(Config.LOG_NEW_EMPLOYEE_MODIFIED_RECORD, recordIDtoEdit, FirstName, LastName, employeeID, mailID, ProjectID));
                	 
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
                     
                     if( fieldName == "mailID") {
                    	 mailID = newValue;}
                         
                     else if( fieldName == "projectInfo") {
                    	 projectInfo.clear();
                    	 projectInfo.add(newValue);}
                        
                     else if ( fieldName == "Location") {
                    	 Location = newValue;
                         } 
                     LOGGER.info("Edited " + fieldName + " Succesffully by : " +
                             String.format(Config.LOG_NEW_MANAGER_MODIFIED_RECORD, recordIDtoEdit, FirstName, LastName, employeeID, mailID, projectInfo, Location));
                     
                     break;
                	
                }else System.out.println("Invalid ID");
               }
                
                CenterServer editRecord = connectToRmiServer(serverID);
				boolean editedRecords = editRecord.editRecord(recordIDtoEdit, fieldName, newValue); 
				editRecord.printData();
				if (editedRecords) {
				
                	System.out.println("Edited Record Succesffully ");
                	
                } else {
                	System.out.println("Could not Edit the Record");
                	LOGGER.info("Could not Edit the Record by: " + managerID);
                }
					
				validaterecordID=false;
				break;
			case 4:
				System.out.println("Record Count");
				CenterServer CANADAServer = connectToRmiServer((Config.Server_ID.CA));
				CenterServer USAServer = connectToRmiServer((Config.Server_ID.US));
				CenterServer UKServer = connectToRmiServer((Config.Server_ID.UK));
				
				String result = CANADAServer.getRecordCounts() + " " + USAServer.getRecordCounts() +" "+ UKServer.getRecordCounts();//stuck here
				System.out.println(result);//null 
				break;

			case 5:
				quit = true;
				System.out.println(managerID + " has left." );
				fileHandler.close();
				ManagerLogin();	
				quit = false;
				validProjectID = false;
				validateEmployeeId=false;
				validateLastName=false;

				break;
			}
		}
	}

	
	public static void Logging() throws Exception {
		//For Logging  
		SimpleFormatter formatter = new SimpleFormatter();
		LOGGER = Logger.getLogger(CenterServer.class.getName());
		
		if (managerID.startsWith("CA")) {
			//Creating Log File for CA Manager
			fileHandler = new FileHandler(String.format(Config.LOG_MANAGER_CA_FILENAME, managerID));
		}else if (managerID.startsWith("US")) {
			//Creating Log File for US Manager
			fileHandler = new FileHandler(String.format(Config.LOG_MANAGER_US_FILENAME, managerID));
			}
		else if (managerID.startsWith("UK")) {
			//Creating Log File for UK Manager
			fileHandler = new FileHandler(String.format(Config.LOG_MANAGER_UK_FILENAME, managerID));
		}
		LOGGER.addHandler(fileHandler);
		LOGGER.setUseParentHandlers(false);
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
				serverID = Config.Server_ID.valueOf(managerID.substring(0, 2).toUpperCase()); 
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
		System.out.println("5 Exit");
	}

	//Helper functions
	private static CenterServer connectToRmiServer(Config.Server_ID serverID) throws Exception {
		int rmiPort = Config.getRMIPortByServerID(serverID);
		Registry registry = LocateRegistry.getRegistry(rmiPort);
		return (CenterServer) registry.lookup(String.format(RMI_REGISTRY_FORMAT, serverID, rmiPort));
	}

	public static String getMutable1() {
		return mutable1;
	}

	public static void setMutable1(String mutable12) {
		mutable1 = mutable12;
	}

	public static String getMutable2() {
		return mutable2;
	}


	public static void setMutable2(String mutable21) {
		mutable2 = mutable21;
	}
	
}

