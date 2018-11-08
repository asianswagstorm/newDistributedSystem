package Server;


import javax.xml.ws.Endpoint;

import Server.CenterServerImpl;
import Utils.Config;

public class CenterServerPublish {
	public static void main(String args[]) throws Exception {
		// Change server ID here
		final Config.Server_ID CAserverID = Config.Server_ID.CA;
		final Config.Server_ID USserverID = Config.Server_ID.US;
		final Config.Server_ID UKserverID = Config.Server_ID.UK;

		// Start RMI server
		CenterServerImpl serverCA = new CenterServerImpl(CAserverID);
		CenterServerImpl serverUS = new CenterServerImpl(USserverID);
		CenterServerImpl serverUK = new CenterServerImpl(UKserverID);

		   try {
		  Endpoint endpointCA = Endpoint.publish("http://localhost:8080/CenterServerImpl/ca", serverCA);
		  System.out.println("Server " + CAserverID + " is published " + endpointCA.isPublished() + " is running ...");
		  Endpoint endpointUS = Endpoint.publish("http://localhost:8080/CenterServerImpl/us", serverUS);
		  System.out.println("Server " + USserverID + " s published " + endpointUS.isPublished() + " is running ...");
		  Endpoint endpointUK = Endpoint.publish("http://localhost:8080/CenterServerImpl/uk", serverUK);
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
