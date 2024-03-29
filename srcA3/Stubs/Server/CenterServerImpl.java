package Server;

import Utils.Config;
import Utils.Config.Server_ID;

import static Utils.Config.*;
import java.lang.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
/*import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;*/
//import java.rmi.server.ServerNotActiveException;
//import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.ws.Endpoint;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import Records.Employee;
import Records.Manager;
import Records.ProjectInfo;
import Records.Record;

@WebService(endpointInterface="Server.CenterServer")
@SOAPBinding(style=Style.RPC)
public class CenterServerImpl implements CenterServer,  Runnable{

	private static final Object lockID = new Object();
	private static final Object lockCount = new Object();
	private final Object recordsMapLock = new Object();
	HashMap<String , ArrayList<Record>> mapRecords = new HashMap<String , ArrayList<Record>>();
	private ArrayList<Record> record;
	private int recordsCount;
	private static final Logger LOGGER = Logger.getLogger(CenterServer.class.getName());
	private int UDPPort;
	private int rmiPort;
	private Server_ID serverID;
	private static int ErecordID;
	private static int MrecordID;
	private static String count_result[] = {"0", "0", "0"};   
	
	public CenterServerImpl(Server_ID serverID) throws Exception {
		super();
		CenterServerImpl.ErecordID=0;
		CenterServerImpl.MrecordID=0;
		this.recordsCount = 0;
		this.mapRecords = new HashMap<String,ArrayList<Record>>();
		this.record = new ArrayList<Record>();
		this.rmiPort = Config.getRMIPortByServerID(serverID);
		this.UDPPort = Config.getUDPPortByServerID(serverID);
		this.serverID = serverID;
		FileHandler server_log_file =null;

		try {   	
			server_log_file=new FileHandler(Config.LOG_SERVER_FILENAME);
			SimpleFormatter formatter=new SimpleFormatter();
			server_log_file.setFormatter(formatter);
			LOGGER.addHandler(server_log_file);
		}
		catch (SecurityException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		LOGGER.info("Server " + this.serverID + " started at ORB Initial port 900 and at UDP port " + this.UDPPort);
	}

	public int getUDPPort() {
		return UDPPort;
	}

	public void setUDPPort(int uDPPort) {
		UDPPort = uDPPort;
	}

	public int getRmiPort() {
		return rmiPort;
	}

	public void setRmiPort(int rmiPort) {
		this.rmiPort = rmiPort;
	}

	public int getERecordID() {
		return ErecordID;
	}

	public void setRecordID(int ErecordID) {
		this.ErecordID = ErecordID;
	}
	public int getMRecordID() {
		return MrecordID;
	}

	public void setMRecordID(int MrecordID) {
		this.MrecordID = MrecordID;
	}
	
	public String createMRecord(String managerID, String firstname, String lastname, String employeeID, String emailID,
			ProjectInfo projectInfo, String location)  {

		String newRecordID;
		synchronized (lockID) {

			newRecordID = String.format(Config.MANAGER_RECORD_FORMAT, MrecordID);
			MrecordID++;
		}
		// Create new record
		Manager newRecord = new Manager(newRecordID, firstname, lastname, employeeID, emailID, projectInfo, location);
		insertRecord(newRecord);
		String PinfoString = "[ " + projectInfo.getProjectId() + " , " +  projectInfo.getClientName() + " , " +  projectInfo.getProjectName() + " ]";
		LOGGER.info(String.format(Config.LOG_ADD_MANAGER_RECORD,managerID,newRecordID, firstname, lastname, employeeID, emailID, PinfoString, location));

		return newRecordID;

	}
	 
	public String createERecord(String managerID, String firstname, String lastname, String employeeID, String emailID,
			String projectID) {

		String newRecordID;
		synchronized (lockID) {
			newRecordID = String.format(Config.EMPLOYEE_RECORD_FORMAT, ErecordID);
			ErecordID++;
		}
		//create new record
		Employee newRecord = new Employee(newRecordID, firstname,lastname, employeeID,emailID, projectID);
		//Search if hashMap already contains a value with key as 1st character of last name ,if so add it to the exisiting list 
		insertRecord(newRecord);

		LOGGER.info(String.format(Config.LOG_ADD_EMPLOYEE_RECORD,managerID, newRecordID, firstname, lastname, employeeID, emailID, projectID));

		return newRecordID;

	}

	private void insertRecord(Record newRecord) {
		char lastNameInitial = Character.toUpperCase(newRecord.getLastname().charAt(0));

		synchronized (recordsMapLock) {
			ArrayList<Record> oldRecordsList = new ArrayList<Record>();
			ArrayList<Record> newRecordsList = new ArrayList<Record>();

			if (mapRecords.containsKey(Character.toString(lastNameInitial))) {
				oldRecordsList = mapRecords.get(Character.toString(lastNameInitial));
				//newRecordsList.addAll(0,oldRecordsList);
				//newRecordsList.add(newRecord);
				//mapRecords.remove(Character.toString(lastNameInitial));
				//mapRecords.put(Character.toString(lastNameInitial), newRecordsList); 
			} else { 	
				newRecordsList.add(newRecord);
				mapRecords.put(Character.toString(lastNameInitial), newRecordsList);    
			}
			// Add the new record to the list
			oldRecordsList.add(newRecord);
			recordsCount++;
			System.out.println("Old recordsList: " +oldRecordsList.toString());
			System.out.println("New recordsList: " +newRecordsList.toString());
			System.out.println("Hash Map: " + mapRecords.toString());
		}
	}
	 
	public synchronized String getRecordCounts(String managerID) {                                      //socket
		DatagramSocket datagramSocket = null;
		String result_value = String.format("%s: %d", serverID, getRecordsNumber());
		try {
			for (Server_ID id : Server_ID.values()) {

				if (id != serverID) {
					InetAddress host = InetAddress.getByName(Config.getHostnameByServerID(id));
					UDPPort = Config.getUDPPortByServerID(id);
					datagramSocket = new DatagramSocket();
					byte[] request = "count".getBytes(); //byte array to send request
					DatagramPacket sendRequestpacket = new DatagramPacket(request, request.length, host, UDPPort);
					datagramSocket.send(sendRequestpacket);

					byte[] response = new byte[1000];   //byte array to receive reply
					DatagramPacket responsePacket = new DatagramPacket(response, response.length);   //packets to receive request
					datagramSocket.receive(responsePacket);
					result_value += String.format(", %s %s", id, new String(responsePacket.getData()).trim());
				}
			}

		} catch (Exception e) {
			LOGGER.info(e.getMessage());
			System.out.println(e.getMessage());
		} finally {
			if (datagramSocket != null)
				datagramSocket.close();
		}
		return result_value;
	}
	
	public boolean editRecord(String managerID, String recordID, String fieldName, String newValue) {
		boolean editStatus = false;
		Record recordFound = locateRecord(recordID);
		if (recordFound != null) {
			/**
			 * Lock the record found to prevent multiple threads edit the same record
			 */
			synchronized (recordFound) {
				if (recordFound.getRecordType().equals(Record.Record_Type.MANAGER)) {
					System.out.println("MANAGER EDIT");
					Manager managerRecord = (Manager) recordFound;
					if (fieldName.equalsIgnoreCase("mailID")) {
						managerRecord.setEmailID(newValue);
						editStatus=true;

					} else if(fieldName.equalsIgnoreCase("projectInfo")) {
						String[] myList = newValue.split(",");
						ProjectInfo pInfo = managerRecord.getProjectInfo();
						pInfo.setProjectId(myList[0]);
						pInfo.setClientName(myList[1]);
						pInfo.setProjectName(myList[2]);
						
						managerRecord.setProjectInfo(pInfo);
						
						editStatus=true;

					} else if(fieldName.equalsIgnoreCase("Location")) {

						switch(newValue) {
						case "CA":
							managerRecord.setLocation("CA");
							editStatus=true;
							break;
						case "US":	
							managerRecord.setLocation("US");
							editStatus=true;
							break;
						case "UK":	
							managerRecord.setLocation("UK");
							editStatus=true;
							break;
						}
					}
					else{
						System.out.println("Opps !!, Cannot edit any other fields except for MailID , ProjectInfo, Location");
						editStatus=false;
					}
					if(editStatus == true) {
						String ProjectInfoString = "[ " +managerRecord.getProjectInfo().getProjectId() +" , " +managerRecord.getProjectInfo().getClientName()+ " , " + managerRecord.getProjectInfo().getProjectName() + " ]";

						LOGGER.info("Edited " + fieldName + " Succesffully by : " +
								String.format(Config.LOG_NEW_MANAGER_MODIFIED_RECORD, managerID, recordID, managerRecord.getFirstname(), managerRecord.getLastname(), managerRecord.getEmployeeID(), managerRecord.getEmailID(), ProjectInfoString, managerRecord.getLocation()));
					}


				} else if (recordFound.getRecordType().equals(Record.Record_Type.EMPLOYEE))  { 
					System.out.println("EMPLOYEE EDIT");
					Employee employeeRecord = (Employee) recordFound;
					if (fieldName.equalsIgnoreCase("projectID")) {
						employeeRecord.setProject_id(newValue);
						System.out.println("new Project ID: " + employeeRecord.getProject_id());
						editStatus=true;

					}else if(fieldName.equalsIgnoreCase("mailID")) {
						employeeRecord.setMailId(newValue);
						System.out.println("new email ID: " + employeeRecord.getMailId());
						editStatus=true;
					}
					LOGGER.info("Edited " + fieldName + "  Succesffully by : " +
							String.format(Config.LOG_NEW_EMPLOYEE_MODIFIED_RECORD, managerID, recordID, employeeRecord.getFirstname(), employeeRecord.getLastname(), employeeRecord.getEmployee_id(), employeeRecord.getMailId(), employeeRecord.getProject_id()));
				}
			}
			// Logging
			LOGGER.info(String.format(Config.LOG_MODIFIED_RECORD_SUCCESS,managerID, recordID, fieldName, newValue));
		} else  
		{ LOGGER.info(String.format(Config.LOG_MODIFIED_RECORD_FAILED, managerID, recordID, fieldName, newValue));
		}
		return editStatus;
	}
	
	public String printAllData()  {
		String output="";
		synchronized (mapRecords) {
			for (ArrayList<Record> recordsList : this.mapRecords.values()) {

				for(int i=0;i<recordsList.size();i++){
					if((recordsList.get(i)).getRecordType()==Records.Record.Record_Type.EMPLOYEE){
						Employee empvalues = (Employee) recordsList.get(i);
						output = "First Name is : "+ empvalues.getFirstname()+"\n" + 
								"Last Name is : "+ empvalues.getLastname()+"\n" + 
								"Employee ID is : "+ empvalues.getEmployee_id()+"\n" + 
								"Email is: "+ empvalues.getMailId()+"\n" + 
								"Project ID is : "+ empvalues.getProject_id();
					}
					else if((recordsList.get(i)).getRecordType()==Records.Record.Record_Type.MANAGER){
						Manager manvalues = (Manager) recordsList.get(i);
						String ProjectInfoString = "[ " +manvalues.getProjectInfo().getProjectId() +" , " +manvalues.getProjectInfo().getClientName()+ " , " + manvalues.getProjectInfo().getProjectName() + " ]";
						output = "First Name is : "+ manvalues.getFirstname()+"\n" + 
								"Last Name is : "+ manvalues.getLastname()+"\n" + 
								"Employee ID is : "+ manvalues.getEmployeeID()+"\n" + 
								"Email is : "+ manvalues.getEmailID()+"\n" + 
								"Project Info is : "+ ProjectInfoString +"\n" + 
								"Location is  : " + manvalues.getLocation();
					}
					else{
						output ="Invalid obj";
					}

				}

			}
		}
		return output;
	}
	
	public synchronized boolean transferRecord(String managerID, String recordID, String remoteCenterServerName) {
		DatagramSocket datagramSocket = null;
		if (remoteCenterServerName.compareTo(this.serverID.name()) != 0) {

			String result = "";

			synchronized (recordsMapLock) {
				for (ArrayList<Record> recordsList : this.mapRecords.values()) {
					Record recordFound = null;

					for (Record rec : recordsList) {
						if (rec.getRecordID().equals(recordID)) {
							recordFound = rec;
						}}

					System.out.println(recordFound);
					if (recordFound.getRecordID().compareTo(recordID) == 0) {
						synchronized (recordFound) {
							// Create the same record on another server via UDP
							// DatagramSocket datagramSocket = null;
							try {

								String requestContent = "";
								Server_ID serverID = Config.Server_ID.valueOf(remoteCenterServerName);
								InetAddress host = InetAddress.getByName(Config.getHostnameByServerID(serverID));

								if (recordFound.getRecordType() == Record.Record_Type.MANAGER) {
									Manager managerRecord = (Manager) recordFound;
									String PInfoString = managerRecord.getProjectInfo().getProjectId() + "_" + managerRecord.getProjectInfo().getClientName() + "_" + managerRecord.getProjectInfo().getProjectName() ;
									requestContent += "Transfer-Manager-Record"+ ","+  managerID+ "," + managerRecord.getRecordID()+ "," +managerRecord.getFirstname()+ "," +managerRecord.getLastname()+ "," + managerRecord.getEmployeeID()+ "," +managerRecord.getEmailID()+ "," + PInfoString + "," +managerRecord.getLocation();
									//result= transferMRecord(remoteCenterServerName, managerID, managerRecord.getRecordID() ,managerRecord.getFirstname(), managerRecord.getLastname(), managerRecord.getEmployeeID(), managerRecord.getEmailID(), managerRecord.getProjectInfo(),  managerRecord.getLocation());
									LOGGER.info("TRANSFERRING MANAGER RECORD " + recordID + " INTO SERVER " + remoteCenterServerName);
								} else {
									Employee employeeRecord = (Employee) recordFound;
									requestContent += "Transfer-Employee-Record"+ ","+managerID + "," + recordID + "," + employeeRecord.getFirstname() + "," + employeeRecord.getLastname() + "," + employeeRecord.getEmployee_id() + "," + employeeRecord.getMailId() + "," + employeeRecord.getProject_id();
									//result= transferERecord(remoteCenterServerName, managerID, employeeRecord.getRecordID(), employeeRecord.getFirstname(), employeeRecord.getLastname(), employeeRecord.getEmployee_id(), employeeRecord.getMailId(), employeeRecord.getProject_id());
									LOGGER.info("TRANSFERRING EMPLOYEE RECORD " + recordID + " INTO SERVER " + remoteCenterServerName);
								}

								//requestContent = result;
								byte[] request = requestContent.getBytes();
								datagramSocket = new DatagramSocket();
								DatagramPacket sentPacket = new DatagramPacket(request, request.length, host, Config.getUDPPortByServerID(serverID));
								datagramSocket.send(sentPacket);

								byte[] response = new byte[1000];
								DatagramPacket receivedPacket = new DatagramPacket(response, response.length);
								datagramSocket.receive(receivedPacket);	                                
								System.out.println("Receive Socket: " + new String(receivedPacket.getData())); //Output 0 

								String[] resultArray = new String(receivedPacket.getData()).trim().split(",");
								result = resultArray[2];
								System.out.println("result is: " + result);
							} catch (Exception e) {
								LOGGER.severe(e.getMessage());
								System.out.println(e.getMessage());
							} finally {
								if (datagramSocket != null)
									datagramSocket.close();
							}
						}
						// If success, delete the record on this server
						System.out.println(result +" " + recordID);
						if (result.compareTo(recordID) == 0) {
							recordsList.remove(recordFound); 
							synchronized (lockCount) {
								LOGGER.info("REMOVED " + recordID + " FROM SERVER " + serverID);
								recordsCount--;
							}

							LOGGER.info(String.format(Config.LOG_TRANSFER_RECORD_SUCCESS, managerID, recordID, remoteCenterServerName));

							return true;
						}
						else {
							LOGGER.info(String.format(Config.LOG_TRANSFER_RECORD_FAIL, managerID, recordID, remoteCenterServerName));
							return false;
						}
					}

				}
				LOGGER.info(String.format(Config.LOG_TRANSFER_RECORD_FAIL, managerID, recordID, remoteCenterServerName));
				return false;

			}
		} 
		else {
			System.out.println("Attempting to transfer in same server");
			LOGGER.info("Attempting to transfer in same server");
			LOGGER.info(String.format(Config.LOG_TRANSFER_RECORD_FAIL, managerID, recordID, remoteCenterServerName));
			return false;
		}
	}

	private String transferMRecord(String ServerToTransfer, String managerID, String recordID, String firstName, String lastName, String employeeID, String email, ProjectInfo projectInfo, String location) {

		// Create new record
		Manager newRecord = new Manager(recordID, firstName, lastName, employeeID, email, projectInfo, location);
		insertRecord(newRecord);
		System.out.println("Adding Record to " +  ServerToTransfer );
		String ProjectInfoString = "[ " +projectInfo.getProjectId() +" , " +projectInfo.getClientName()+ " , " + projectInfo.getProjectName() + " ]";
		LOGGER.info(String.format(Config.TRANSFER_MANAGER_RECORD, ServerToTransfer, managerID, recordID, firstName, lastName, employeeID, email, ProjectInfoString, location));
		return recordID;
	}

	private String transferERecord(String ServerToTransfer, String managerID, String recordID, String firstName, String lastName, String employeeID, String email, String projectID) {

		// Create new record
		Employee newRecord = new Employee(recordID, firstName, lastName, employeeID, email, projectID);
		insertRecord(newRecord);
		System.out.println("Adding Record to " +  ServerToTransfer );
		LOGGER.info(String.format(Config.TRANSFER_EMPLOYEE_RECORD, ServerToTransfer, managerID, recordID, firstName, lastName, employeeID, email, projectID));		
		return recordID;
	}
	 
	public String printRecord(String managerID, String recordID) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRecordsNumber() {
		/**
		 * This function could be called concurrently by many threads
		 * when some servers request the number of records of this server at the same time
		 * Make sure only one thread can access the shared variable at a time
		 */
		synchronized (lockCount) {
			return this.recordsCount;
		}
	}

	public Record locateRecord(String recordID) {
		Record recordFound = null;
		boolean isRecordFound = false;
		/**
		 * Synchronize the whole HashMap to prevent other
		 * threads from modifying it when it is being iterated
		 */
		synchronized (mapRecords) {
			for (ArrayList<Record> recordsList : this.mapRecords.values()) {
				Iterator<Record> iterator = recordsList.iterator();
				while ((iterator.hasNext()) && (!isRecordFound)) {
					recordFound = iterator.next();
					if (recordFound.getRecordID().compareTo(recordID) == 0) {
						isRecordFound = true;
						break;
					}
				}
			}
		}
		if (!isRecordFound)
			recordFound = null;
		return recordFound;
	}

	public void run() {
		DatagramSocket datagramSocket = null;
		System.out.println("UDP Server with port number " + Config.getUDPPortByServerID(serverID)+ " and Located at "+ serverID);

		try{
			datagramSocket =new DatagramSocket(Config.getUDPPortByServerID(serverID));
			byte[] bufferReceive = new byte[2000];
			byte[] bufferSend = new byte[2000];
			while(true){
				DatagramPacket receivePacket =new DatagramPacket(bufferReceive,bufferReceive.length);
				datagramSocket.receive(receivePacket);
				DatagramSocket socket = datagramSocket;

				System.out.println("ServerId is: " + serverID);

				String data  =new String(receivePacket.getData());
				System.out.println("Checking UDP port:  "+ Config.getUDPPortByServerID(serverID)) ;

				if(data.substring(0, 5).equals("count")) {

					int currentCount = getRecordsNumber();
					bufferSend = (currentCount + "").getBytes();
					DatagramPacket sendPackets=new DatagramPacket(bufferSend,bufferSend.length,receivePacket.getAddress(),receivePacket.getPort());
					socket.send(sendPackets);
					System.out.println("Number of records in this port: " + getRecordsNumber());
				}
				else if(data.substring(0, 8).equals("Transfer")){
					//Transfer stuff
					System.out.println("data is \n" +data.toString());
					String[] fields = data.toString().split(",");
					String managerID = fields[1];
					String recordID = fields[2];
					String fname = fields[3];
					String lname = fields[4];
					String EID = fields[5];
					String Mail = fields[6];
					//
					if(recordID.contains("ER")) {
						System.out.println("Record is an Employee Record");//if record id is a employee record
						String PID = fields[7];
						new Thread(new Runnable() {
							public void run() {
								System.out.println("Transfering the record");
								transferERecord(serverID.toString(),managerID,recordID, fname, lname, EID, Mail,PID);
								System.out.println("Record Transfered");
							}
						}).start();

					}else {
						System.out.println("Record is an Manager Record");//if record id is a manager record
						
						ProjectInfo PInfo = new ProjectInfo() ;
					
						String[] pInfoString	= fields[7].split("_");
						PInfo.setProjectId(pInfoString[0]);
						PInfo.setClientName(pInfoString[1]);
						PInfo.setProjectName(pInfoString[2]);
						System.out.println("Transferred: " + PInfo.getProjectId() + ", "+ PInfo.getClientName()+ ", " + PInfo.getProjectName());
						String Location = fields[8];
						
						new Thread(new Runnable() {
							public void run() {
								System.out.println("Transfering the record");
								transferMRecord(serverID.toString(),managerID,recordID, fname, lname,EID,Mail, PInfo , Location);
								System.out.println("Record Transfered");
							}
						}).start();
					}
					bufferSend =  data.toString().getBytes();
					DatagramPacket sendPackets=new DatagramPacket(bufferSend,bufferSend.length,receivePacket.getAddress(),receivePacket.getPort());
					socket.send(sendPackets);
				}
			}
		}catch(SocketException e){
			System.out.println("Socket "+e.getMessage());
			e.printStackTrace();
		}catch(IOException e){
			System.out.println("IO :"+e.getMessage());
			e.printStackTrace();
		}

	}

	private ArrayList<Record> getRecordsList(char lastNameInitial) {
		synchronized (mapRecords) {
			if (mapRecords.containsKey(Character.toString(lastNameInitial)))
				return mapRecords.get(Character.toString(lastNameInitial));
			else {
				ArrayList<Record> recordsList = new ArrayList<>();
				mapRecords.put(Character.toString(lastNameInitial), recordsList);
				return recordsList;
			}
		}
	}

	private void initiateLogger() throws IOException {
		FileHandler fileHandler = new FileHandler(String.format(Config.LOG_SERVER_FILENAME, this.serverID));
		LOGGER.addHandler(fileHandler);
		SimpleFormatter formatter = new SimpleFormatter();
		fileHandler.setFormatter(formatter);
	}

	public static void main(String args[]) throws Exception {
		// Change server ID here
		final Config.Server_ID CAserverID = Config.Server_ID.CA;
		final Config.Server_ID USserverID = Config.Server_ID.US;
		final Config.Server_ID UKserverID = Config.Server_ID.UK;

		
		CenterServerImpl serverCA = new CenterServerImpl(CAserverID);
		CenterServerImpl serverUS = new CenterServerImpl(USserverID);
		CenterServerImpl serverUK = new CenterServerImpl(UKserverID);
		
		   try {//Soen423A3WebService/services/CenterServerImpl/
		  Endpoint endpointCA = Endpoint.publish("http://localhost:8888/CenterServerImpl/CA", serverCA);
		  System.out.println("Server " + CAserverID + " is published " + endpointCA.isPublished() + " is running ...");
		  Endpoint endpointUS = Endpoint.publish("http://localhost:8888/CenterServerImpl/US", serverUS);
		  System.out.println("Server " + USserverID + " s published " + endpointUS.isPublished() + " is running ...");
		  Endpoint endpointUK = Endpoint.publish("http://localhost:8888/CenterServerImpl/UK", serverUK);
		  System.out.println("Server " + UKserverID + " is published " + endpointUK.isPublished() + " is running ...");
		   } catch (Exception e) {
	            System.out.println("ERROR: " + e);
	            e.printStackTrace(System.out);
	        }

		Thread canadaThread = new Thread(serverCA);
		Thread unitedstatesThread = new Thread(serverUS);
		Thread unitedkingdomThread = new Thread(serverUK);

		// Start UDP server as thread available to receive request at all times
		canadaThread.start(); 
		unitedstatesThread.start();
		unitedkingdomThread.start();

	}
}