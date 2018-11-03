package RMIInterface;

import Utils.Config;
import Utils.Config.Server_ID;

import org.omg.CORBA.DEMS;
import org.omg.CORBA.DEMSHelper;
import org.omg.CORBA.DEMSPOA;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA.*;

import static Utils.Config.*;
import java.lang.*;
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
//import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import Records.Employee;
import Records.Manager;
import Records.Record;

public class CenterServerImpl extends DEMSPOA implements Runnable{

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
	private ORB orb;
	private static String count_result[] = {"0", "0", "0"};   
	       
	
	public void setORB(ORB orb_val) {
		this.orb = orb_val;
	}

	public CenterServerImpl(Server_ID serverID) throws Exception {
		super();
		CenterServerImpl.ErecordID=0;
		CenterServerImpl.MrecordID=0;
		this.recordsCount = 0;
		this.mapRecords = new HashMap<String,ArrayList<Record>>();
		this.record = new ArrayList<Record>();
		//this.rmiPort = Config.getRMIPortByServerID(serverID);
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
			String projectInfo, String location)  {
	
		String newRecordID;
		synchronized (lockID) {
		    recordsCount++;
			newRecordID = String.format(Config.MANAGER_RECORD_FORMAT, MrecordID);
			MrecordID++;
		}
		// Create new record
		Manager newRecord = new Manager(newRecordID, firstname, lastname, employeeID, emailID, projectInfo, location);
		insertRecord(newRecord);
		LOGGER.info(String.format(Config.LOG_ADD_MANAGER_RECORD,managerID,newRecordID, firstname, lastname, employeeID, emailID, projectInfo, location));
		
		return newRecordID;

	}

	public String createERecord(String managerID, String firstname, String lastname, String employeeID, String emailID,
			String projectID) {
		
		String newRecordID;
		synchronized (lockID) {
		    recordsCount++;
			newRecordID = String.format(Config.EMPLOYEE_RECORD_FORMAT, ErecordID);
			ErecordID++;
			System.out.println("Test3");
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
            	
            	synchronized(newRecordsList) {
            		newRecordsList.addAll(0,oldRecordsList);
            		newRecordsList.add(newRecord);
            		mapRecords.remove(Character.toString(lastNameInitial));
            		mapRecords.put(Character.toString(lastNameInitial), newRecordsList); }
            
            } else {
            	synchronized(newRecordsList) {
            	newRecordsList.add(newRecord);
                mapRecords.put(Character.toString(lastNameInitial), newRecordsList);
                }
            }
            // Add the new record to the list
        
            System.out.println("Old recordsList: " +oldRecordsList);
            System.out.println("New recordsList: " +newRecordsList);
			System.out.println("Hash Map: " + mapRecords);
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

	/*private String[] send_and_receive_packets(int port1, int port2)        //invoked server sends the count request to other two servers and recieve their reply
	{
		String result[] = {"", ""};
		try {

			InetAddress host = InetAddress.getLocalHost();
			byte_array_request = "count".getBytes();
			datagramSocket = new DatagramSocket();
			sendRequestpacket1 = new DatagramPacket(byte_array_request, byte_array_request.length, host, port1);//first packet is sent to first serve(port no1)
			this.datagramSocket.send(sendRequestpacket1);


			receivedReplyPacket1 = new DatagramPacket(byte_array_reply, byte_array_reply.length);
			this.datagramSocket.receive(receivedReplyPacket1);

			datagramSocket = new DatagramSocket();
			sendRequestpacket2 = new DatagramPacket(byte_array_request, byte_array_request.length, host, port2);
			datagramSocket.send(sendRequestpacket2);

			receivedReplyPacket2 = new DatagramPacket(byte_array_reply, byte_array_reply.length);
			this.datagramSocket.receive(receivedReplyPacket2);

			result[0] = new String(receivedReplyPacket1.getData()).trim();
			result[1] = new String(receivedReplyPacket2.getData()).trim();
		} catch (Exception e) {

		}

		return result;
	}*/

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
						managerRecord.setProjectInfo(myList[0] + ", " + myList[1] + ", "+ myList[2]);                    		
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
					if(editStatus == true)
						LOGGER.info("Edited " + fieldName + " Succesffully by : " +
								String.format(Config.LOG_NEW_MANAGER_MODIFIED_RECORD, managerID, recordID, managerRecord.getFirstname(), managerRecord.getLastname(), managerRecord.getEmployeeID(), managerRecord.getEmailID(), managerRecord.getProjectInfo(), managerRecord.getLocation()));



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
						output = "First Name is : "+ manvalues.getFirstname()+"\n" + 
								"Last Name is : "+ manvalues.getLastname()+"\n" + 
								"Employee ID is : "+ manvalues.getEmployeeID()+"\n" + 
								"Email is : "+ manvalues.getEmailID()+"\n" + 
								"Project Info is : "+ manvalues.getProjectInfo()+"\n" + 
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

			synchronized (mapRecords) {
				for (ArrayList<Record> recordsList : this.mapRecords.values()) {
					Record recordFound = null;
					Iterator<Record> iterator = recordsList.iterator();
					//while (iterator.hasNext()) {
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

									result= transferMRecord(remoteCenterServerName, managerID, managerRecord.getRecordID() ,managerRecord.getFirstname(), managerRecord.getLastname(), managerRecord.getEmployeeID(), managerRecord.getEmailID(), managerRecord.getProjectInfo(),  managerRecord.getLocation());
									LOGGER.info("TRANSFERRING " + recordID + " INTO SERVER " + remoteCenterServerName);
								} else {
									Employee employeeRecord = (Employee) recordFound;

									result= transferERecord(remoteCenterServerName, managerID, employeeRecord.getRecordID(), employeeRecord.getFirstname(), employeeRecord.getLastname(), employeeRecord.getEmployee_id(), employeeRecord.getMailId(), employeeRecord.getProject_id());
									LOGGER.info("TRANSFERRING " + recordID + " INTO SERVER " + remoteCenterServerName);
								}

								System.out.println(result);
								requestContent = result;
								byte[] request = requestContent.getBytes();
								datagramSocket = new DatagramSocket();
								DatagramPacket sentPacket = new DatagramPacket(request, request.length, host, Config.getUDPPortByServerID(serverID));
								datagramSocket.send(sentPacket);
								/*
	                                byte[] response = new byte[1000];
	                                DatagramPacket receivedPacket = new DatagramPacket(response, response.length);
	                                datagramSocket.receive(receivedPacket);	                                
	                                System.out.println(new String(receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength())); //Output 0 
								 */
								//new String(receivedPacket.getData()).trim();
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

	private String transferMRecord(String ServerToTransfer, String managerID, String recordID, String firstName, String lastName, String employeeID, String email, String projectInfo, String location) {
		char lastNameInitial = Character.toUpperCase((lastName.charAt(0)));

		// Create new record
		Manager newRecord = new Manager(recordID, firstName, lastName, employeeID, email, projectInfo, location);


		synchronized (mapRecords) {
			ArrayList<Record> recordsList;
			if (mapRecords.containsKey(Character.toString(lastNameInitial)))
				recordsList = mapRecords.get(Character.toString(lastNameInitial));
			else {
				recordsList = new ArrayList<>();
				mapRecords.put(Character.toString(lastNameInitial), recordsList);
			}

			// Add the new record to the list
			System.out.println(recordsList);
			recordsList.add(newRecord);
			System.out.println("Adding Record to " +  ServerToTransfer );
			recordsCount++;
			LOGGER.info(String.format(Config.TRANSFER_MANAGER_RECORD, managerID, recordID, firstName, lastName, employeeID, email, projectInfo, location));
		}

		return recordID;
	}

	private String transferERecord(String ServerToTransfer, String managerID, String recordID, String firstName, String lastName, String employeeID, String email, String projectID) {
		char lastNameInitial = Character.toUpperCase(lastName.charAt(0));

		// Create new record
		//Employee newRecord = null;
		switch(ServerToTransfer) {
		case "CA":
			this.UDPPort = Config.getUDPPortByServerID(Config.Server_ID.CA);
			break;
		case "US":
			this.UDPPort = Config.getUDPPortByServerID(Config.Server_ID.US);
			break;
		case "UK":
			this.UDPPort = Config.getUDPPortByServerID(Config.Server_ID.UK);
			break;

		}
		Employee newRecord = new Employee(recordID, firstName, lastName, employeeID, email, projectID);


		synchronized (mapRecords) {
			ArrayList<Record> recordsList;
			if (mapRecords.containsKey(Character.toString(lastNameInitial)))
				recordsList =  mapRecords.get(Character.toString(lastNameInitial));
			else {
				recordsList = new ArrayList<>();
				mapRecords.put(Character.toString(lastNameInitial), recordsList);
			}

			// Add the new record to the list
			recordsList.add(newRecord);

			System.out.println(recordsList);
			System.out.println("Adding Record to " +  ServerToTransfer );
			recordsCount++;

			LOGGER.info(String.format(Config.TRANSFER_EMPLOYEE_RECORD, managerID, recordID, firstName, lastName, employeeID, email, projectID));
		}

		return recordID;
	}

	public String printRecord(String managerID, String recordID) {
		// TODO Auto-generated method stub
		return null;
	}

	/*public int recordcount(int port){

		int numberOfRecords = 0;
		if(port == Config.getUDPPortByServerID(Config.Server_ID.CA)){  
			System.out.println("a1");
			ArrayList value = null;
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
			ArrayList value = null;
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
		ArrayList value = null;
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
	}*/

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
				
				String data  =new String(receivePacket.getData()).trim();
				System.out.println("Checking UDP port:  "+ Config.getUDPPortByServerID(serverID)) ;
				System.out.println(data.substring(0, 5)); //count

				if(data.substring(0, 5).equals("count")) {
					
					int currentCount = getRecordsNumber();
					bufferSend = (currentCount + "").getBytes();
					DatagramPacket sendPackets=new DatagramPacket(bufferSend,bufferSend.length,receivePacket.getAddress(),receivePacket.getPort());
					socket.send(sendPackets);
					System.out.println("Number of records in this port: " + getRecordsNumber());
				}
				else {
					//Transfer stuff
					System.out.println("data is \n " +data.toString());

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
	/*
	public void startUDPServer() {
	    DatagramSocket socket = null;
	    try {
	        socket = new DatagramSocket(this.UDPPort);
	        LOGGER.info(String.format(Config.LOG_UDP_SERVER_START, this.UDPPort));

	        while (true) {
	            // Get the request
	            byte[] buffer = new byte[1000];
	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
	            socket.receive(request);


	            DatagramSocket threadSocket = socket;
	            new Thread(() -> {
	                String replyStr = "-1";
	                String strRequest = new String(request.getData()).trim();
	                String[] requestComponent = strRequest.split(Config.DELIMITER);
	                switch (requestComponent[0]) {
	                    case Config.GET_RECORDS_COUNT_FUNC_NAME:
	                        replyStr = Integer.toString(getRecordsNumber());
	                        break;
	                    case Config.TRANSFER_EMPLOYEE_RECORD:
	                        replyStr = transferERecord(requestComponent[1], requestComponent[2], requestComponent[3], requestComponent[4], requestComponent[5], requestComponent[6], requestComponent[7]);
	                        break;
	                    case Config.TRANSFER_MANAGER_RECORD:
	                        replyStr = transferMRecord(requestComponent[1], requestComponent[2], requestComponent[3], requestComponent[4], requestComponent[5], requestComponent[6], requestComponent[7], requestComponent[8]);
	                        break;
	                }

	                // Reply back
	                DatagramPacket response = new DatagramPacket(replyStr.getBytes(), replyStr.length(), request.getAddress(), request.getPort());
	                try {
	                    threadSocket.send(response);

	                } catch (IOException e) {
	                    LOGGER.severe(e.getMessage());
	                    e.printStackTrace();
	                }
	            }).start();
	        }
	    } catch (Exception e) {
	        LOGGER.severe(e.getMessage());
	        System.out.println(e.getMessage());
	    } finally {
	        if (socket != null) {
	            socket.close();
	            LOGGER.info(String.format(Config.LOG_UDP_SERVER_STOP, this.UDPPort));
	        }

	    }
	}*/

	public static void main(String args[]) throws Exception {
		final Config.Server_ID CAserverID = Config.Server_ID.CA;
		final Config.Server_ID USserverID = Config.Server_ID.US;
		final Config.Server_ID UKserverID = Config.Server_ID.UK;

		//Servants to be registered to ORB.
		CenterServerImpl serverCA = new CenterServerImpl(CAserverID);
		CenterServerImpl serverUS = new CenterServerImpl(USserverID);
		CenterServerImpl serverUK = new CenterServerImpl(UKserverID);

		try {

			ORB orb = ORB.init(args,null);
			POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootPOA.the_POAManager().activate();

			serverCA.setORB(orb);
			serverUS.setORB(orb);
			serverUK.setORB(orb);

			org.omg.CORBA.Object refCA = rootPOA.servant_to_reference(serverCA);
			org.omg.CORBA.Object refUS = rootPOA.servant_to_reference(serverUS);
			org.omg.CORBA.Object refUK = rootPOA.servant_to_reference(serverUK);
			DEMS demsServer1 = DEMSHelper.narrow(refCA);
			DEMS demsServer2 = DEMSHelper.narrow(refUS);
			DEMS demsServer3 = DEMSHelper.narrow(refUK);

			// Get the root Naming Context
			org.omg.CORBA.Object objRef = orb.resolve_initial_references(Config.CORBA.NAME_SERVICE);
			NamingContextExt namingContextRef = NamingContextExtHelper.narrow(objRef);

			// Bind the object reference to the Naming Context
			NameComponent path1[] = namingContextRef.to_name(CAserverID.name());
			NameComponent path2[] = namingContextRef.to_name(USserverID.name());
			NameComponent path3[] = namingContextRef.to_name(UKserverID.name());

			namingContextRef.rebind(path1, demsServer1);
			namingContextRef.rebind(path2, demsServer2);
			namingContextRef.rebind(path3, demsServer3);

			// Run the server
			Thread canadaThread = new Thread(serverCA);
			Thread unitedstatesThread = new Thread(serverUS);
			Thread unitedkingdomThread = new Thread(serverUK);

			// Start UDP server as thread available to receive request at all times
			canadaThread.start(); 
			unitedstatesThread.start();
			unitedkingdomThread.start();

			// serverCA.startUDPServer();
			//   serverUS.startUDPServer();
			//  serverUK.startUDPServer();

			System.out.println("Server " + CAserverID.name() + " is running ...");
			System.out.println("Server " + USserverID.name() + " is running ...");
			System.out.println("Server " + UKserverID.name() + " is running ...");
			orb.run();


		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);

		}

		//Registry registry = LocateRegistry.createRegistry(serverCA.getRmiPort());

		//registry.bind(String.format(RMI_REGISTRY_FORMAT, CAserverID, serverCA.getRmiPort()), serverCA);
		//registry.bind(String.format(RMI_REGISTRY_FORMAT, USserverID, serverUS.getRmiPort()), serverUS);
		//registry.bind(String.format(RMI_REGISTRY_FORMAT, UKserverID, serverUK.getRmiPort()), serverUK);


	}

	private void initiateLogger() throws IOException {
		FileHandler fileHandler = new FileHandler(String.format(Config.LOG_SERVER_FILENAME, this.serverID));
		LOGGER.addHandler(fileHandler);
		SimpleFormatter formatter = new SimpleFormatter();
		fileHandler.setFormatter(formatter);
	}

}




