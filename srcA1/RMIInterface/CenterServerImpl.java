package RMIInterface;

import Utils.Config;
import Utils.Config.Server_ID;

import static Utils.Config.RMI_REGISTRY_FORMAT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import Records.Employee;
import Records.Manager;
import Records.Record;

public class CenterServerImpl extends UnicastRemoteObject implements CenterServer, Runnable{

	private static HashMap<String , ArrayList<Record>> mapRecords;
	private ArrayList<Record> record;
	private int recordsCount;
	private static final Logger LOGGER = Logger.getLogger(CenterServer.class.getName());
	private int UDPPort;
	private int rmiPort;
	private Server_ID serverID;
	private static int ErecordID;
	private static int MrecordID;
	private final Object lockID = new Object();
	private final Object lockCount = new Object();

	public CenterServerImpl(Server_ID serverID) throws RemoteException {
		super();
		CenterServerImpl.ErecordID=0;
		CenterServerImpl.MrecordID=0;
		this.recordsCount = 0;
		CenterServerImpl.mapRecords=new HashMap<String , ArrayList<Record>>();
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
		LOGGER.setUseParentHandlers(false);
		LOGGER.info("Server " + this.serverID + " started at RMI port " + this.rmiPort + " and at UDP port " + this.UDPPort);
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

	public String createERecord(String firstname, String lastname, String employeeID, String emailID,
			String projectID) throws RemoteException, ServerNotActiveException {

		char lastNameInitial = Character.toUpperCase(lastname.charAt(0));
		boolean status =false;
		
		String newRecordID;
		synchronized (lockID) {
			newRecordID = String.format(Config.EMPLOYEE_RECORD_FORMAT, ErecordID);
			ErecordID++;
			System.out.println("Test3");
			
		}
		//create new record
		Employee newRecord = new Employee(newRecordID, firstname,lastname, employeeID,emailID, projectID);
		//Search if hashMap already contains a value with key as 1st character of last name ,if so add it to the exisiting list 
		ArrayList<Record> recordsList= getRecordsList(lastNameInitial);
		//recordsList is null
		System.out.println("recordsList: " + recordsList);
		if(mapRecords.containsKey(Character.toString(lastNameInitial)))
			synchronized(this){
				System.out.println("Test2");
				recordsList.add(newRecord);
				recordsCount++;
				status = true;
				mapRecords.put(Character.toString(lastNameInitial), recordsList);
				//ErecordID++;
			}	
		else {
			
			System.out.println("Test1");
			record.add(newRecord);
			mapRecords.put(Character.toString(lastNameInitial), record);
			ErecordID++;
			recordsCount++;
			status = true;
		}

		if(status) {
			System.out.println("recordsList: " +recordsList);
			System.out.println("Hash Map: " + mapRecords);
			LOGGER.info(String.format(Config.LOG_ADD_EMPLOYEE_RECORD, newRecordID, firstname, lastname, employeeID, emailID, projectID));
		}
		return newRecordID;

	}
	
	
	public String createMRecord(String firstname, String lastname, String employeeID, String emailID,
			ArrayList<String> projectInfo, String location) throws RemoteException, ServerNotActiveException {
		boolean status =false;
		char lastNameInitial = Character.toUpperCase(lastname.charAt(0));
		String newRecordID;
		synchronized (lockID) {
			newRecordID = String.format(Config.MANAGER_RECORD_FORMAT, MrecordID);
			MrecordID++;
		}
		// Create new record
		Manager newRecord = new Manager(newRecordID, firstname, lastname, employeeID, emailID, projectInfo, location);
		ArrayList<Record> recordsList = getRecordsList(lastNameInitial);
		if (mapRecords.containsKey(Character.toString(lastNameInitial))){
			synchronized (this) {
				recordsList.add(newRecord);
				mapRecords.put(Character.toString(lastNameInitial), recordsList);}
			recordsCount++;
			status=true;
		}
		else {  // Add the new record to the list
			record.add(newRecord);
			mapRecords.put(Character.toString(lastNameInitial), record);
			recordsCount++;  
			status=true;

		}
		if(status) {   
			System.out.println("recordsList: " +recordsList);
			System.out.println("Hash Map: " + mapRecords);
			LOGGER.info(String.format(Config.LOG_ADD_MANAGER_RECORD,newRecordID, firstname, lastname, employeeID, emailID, projectInfo, location));
		}

		return newRecordID;
	}

	public String getRecordCounts(){//Problem with ConnectUDP!!!! 

		DatagramSocket datagramSocket = null;
		String result = String.format("%s: %d", serverID, getRecordsNumber());
		try {
			for (Server_ID id : Server_ID.values()) {
				if (this.serverID != serverID) {
					//create a datagram socket and bind it to any local port
					datagramSocket = new DatagramSocket();
					//place data in a byte array
					byte[] request = Config.GET_RECORDS_COUNT_FUNC_NAME.getBytes();
					InetAddress host = InetAddress.getByName(Config.getHostnameByServerID(id));
					//create a datagram packet, specifying the data array and the receiver's address
					DatagramPacket sendRequestpacket = new DatagramPacket(request, Config.GET_RECORDS_COUNT_FUNC_NAME.length(), host, Config.getUDPPortByServerID(id));
					//invoke the send method of the socket with a reference to the datagram packet;
					datagramSocket.send(sendRequestpacket);
					LOGGER.info(String.format(Config.LOG_UDP_REQUEST_TO, host, Config.getUDPPortByServerID(id)));
					//create a byte array for receiving the data
					byte[] response = new byte[1000];
					//create a datagram packet, specifying the data array
					DatagramPacket receivedReplyPacket = new DatagramPacket(response, response.length);
					//invoke the receive method of the socket with a reference to the datagram packet
					datagramSocket.receive(receivedReplyPacket);
					result += String.format(", %s: %s", id, new String(receivedReplyPacket.getData()).trim());
					LOGGER.info(String.format(Config.LOG_UDP_RESPONSE_FROM, host, Config.getUDPPortByServerID(id)));

				}
			}

			/**
			 * Make sure that Log content will always come together
			 */
			synchronized (LOGGER) {
				LOGGER.info(String.format(Config.LOG_CLIENT_IP, RemoteServer.getClientHost()));
				LOGGER.info(result);
			}
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
			System.out.println(e.getMessage());
		} finally {
			if (datagramSocket != null)
				datagramSocket.close();
		}
		return result;
		
	}

	public int recordcount(int port){
		
		int numberOfRecords = 0;
		if(port == Config.getUDPPortByServerID(Config.Server_ID.CA)){  
			System.out.println("a1");
			ArrayList<?> value = null;
			for (Map.Entry<String, ArrayList<Record>> entry : mapRecords.entrySet()) {                 
				value = entry.getValue();                 
			}

			if(value==null){
				numberOfRecords=0; 
			}
			else{
				numberOfRecords=value.size();
			}

		}

		else if(port == Config.getUDPPortByServerID(Config.Server_ID.US)){
			System.out.println("a2");
			ArrayList<?> value = null;
			for (Map.Entry<String, ArrayList<Record>> entry : mapRecords.entrySet()) {
				value = entry.getValue();
			}

			if(value==null){
				numberOfRecords=0; 
			}
			else{
				numberOfRecords=value.size();
			}
		}

		else if(port == Config.getUDPPortByServerID(Config.Server_ID.UK))
		{	 System.out.println("a3");	
		ArrayList<?> value = null;
		for (Map.Entry<String, ArrayList<Record>> entry : mapRecords.entrySet()) {
			value = entry.getValue();
		}

		if(value==null){
			numberOfRecords=0; 
		}
		else{
			numberOfRecords=value.size();
		}
		}
		return numberOfRecords;
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

	//		??
	public boolean editRecord(String recordID, String fieldName, String newValue) throws RemoteException {
		boolean editStatus = false;
		Record recordFound = locateRecord(recordID);
		if (recordFound != null) {
			/**
			 * Lock the record found to prevent multiple threads edit the same record
			 */
			synchronized (recordFound) {
				if (recordFound.getRecordType().equals(Record.Record_Type.MANAGER)) {
					Manager managerRecord = (Manager) recordFound;
					if (fieldName.equalsIgnoreCase("mailID")) {
						managerRecord.setEmailID(newValue);
						editStatus=true;

					} else if(fieldName.equalsIgnoreCase("projectInfo")) {
						ArrayList<String> myList = new ArrayList<String>(Arrays.asList(newValue.split(",")));
						managerRecord.setProjectInfo(myList);                    		
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

				} else if (recordFound.getRecordType().equals(Record.Record_Type.EMPLOYEE))  { 
					Employee employeeRecord = (Employee) recordFound;
					if (fieldName.equalsIgnoreCase("projectID")) {
						employeeRecord.setProject_id(newValue);
						editStatus=true;

					}else if(fieldName.equalsIgnoreCase("mailID")) {
						employeeRecord.setMailId(newValue);
						editStatus=true;
					}
				}
			}
			// Logging
			LOGGER.info(String.format(Config.LOG_MODIFIED_RECORD_SUCCESS, recordID, fieldName, newValue));
		} else  
		{ LOGGER.info(String.format(Config.LOG_MODIFIED_RECORD_FAILED, recordID, fieldName, newValue));
		}
		return editStatus;
	}

	@Override
	public void printData() throws RemoteException {
		synchronized (mapRecords) {
			for (ArrayList<Record> recordsList : this.mapRecords.values()) {

				for(int i=0;i<recordsList.size();i++){
					if((recordsList.get(i)).getRecordType()==Records.Record.Record_Type.EMPLOYEE){
						Employee empvalues = (Employee) recordsList.get(i);
						System.out.println("First Name is : "+ empvalues.getFirstname());
						System.out.println("Last Name is : "+ empvalues.getLastname());
						System.out.println("Employee ID is : "+ empvalues.getEmployee_id());
						System.out.println("Email is: "+ empvalues.getMailId());
						System.out.println("Project ID is : "+ empvalues.getProject_id());
					}
					else if((recordsList.get(i)).getRecordType()==Records.Record.Record_Type.MANAGER){
						Manager manvalues = (Manager) recordsList.get(i);
						System.out.println("First Name is : "+ manvalues.getFirstname());
						System.out.println("Last Name is : "+ manvalues.getLastname());
						System.out.println("Employee ID is : "+ manvalues.getEmployeeID());
						System.out.println("Email is : "+ manvalues.getEmailID());
						System.out.println("Project Info is : "+ manvalues.getProjectInfo());
						System.out.println("Location is  : " + manvalues.getLocation());
					}
					else{
						System.out.println("Invalid obj");
					}

				}

			}
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
			for (ArrayList<Record> recordsList : CenterServerImpl.mapRecords.values()) {
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
	@Override
	public void run() {
		DatagramSocket datagramSocket=null;
		System.out.println("UDP Server with port number " + this.UDPPort+ " and Located at "+ this.serverID);

		try{

			datagramSocket =new DatagramSocket(this.UDPPort);
			byte[] bufferReceive = new byte[50];
			byte[] bufferSend = new byte[50];
			while(true){

				DatagramPacket receivedPacket =new DatagramPacket(bufferReceive,bufferReceive.length);
				datagramSocket.receive(receivedPacket);
				System.out.println("Port number at server run , fetched from UDP request ") ;
				System.out.println(receivedPacket.getPort());
				bufferSend = Integer.toString(recordcount(receivedPacket.getPort())).getBytes();

				DatagramPacket sendPackets=new DatagramPacket(bufferSend,bufferSend.length,receivedPacket.getAddress(),receivedPacket.getPort());
				datagramSocket.send(sendPackets);

			}
		}
		catch(SocketException ex){
			System.out.println("Socket "+ex.getMessage());
		}
		catch(IOException e){

			System.out.println("IO :"+e.getMessage());

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

	public static void main(String args[]) throws Exception {
		// Change server ID here
		final Config.Server_ID CAserverID = Config.Server_ID.CA;
		final Config.Server_ID USserverID = Config.Server_ID.US;
		final Config.Server_ID UKserverID = Config.Server_ID.UK;

		// Start RMI server
		CenterServerImpl serverCA = new CenterServerImpl(CAserverID);
		CenterServerImpl serverUS = new CenterServerImpl(USserverID);
		CenterServerImpl serverUK = new CenterServerImpl(UKserverID);

		Registry registry = LocateRegistry.createRegistry(serverCA.getRmiPort());

		registry.bind(String.format(RMI_REGISTRY_FORMAT, CAserverID, serverCA.getRmiPort()), serverCA);
		registry.bind(String.format(RMI_REGISTRY_FORMAT, USserverID, serverUS.getRmiPort()), serverUS);
		registry.bind(String.format(RMI_REGISTRY_FORMAT, UKserverID, serverUK.getRmiPort()), serverUK);

		Thread canadaThread = new Thread(serverCA);
		Thread unitedstatesThread = new Thread(serverUS);
		Thread unitedkingdomThread = new Thread(serverUK);

		// Start UDP server as thread available to receive request at all times
		canadaThread.start(); 
		unitedstatesThread.start();
		unitedkingdomThread.start();
	}

    private void initiateLogger() throws IOException {
        FileHandler fileHandler = new FileHandler(String.format(Config.LOG_SERVER_FILENAME, this.serverID));
        LOGGER.addHandler(fileHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
    }
}







