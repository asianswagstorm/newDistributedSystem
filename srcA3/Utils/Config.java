package Utils;

//import Utils.Config.Server_ID;
public class Config {
	public class CORBA {
        public static final String ROOT_POA = "RootPOA";
        public static final String NAME_SERVICE = "NameService";
    }
    // Records
    public static final String EMPLOYEE_RECORD_FORMAT = "ER%05d";
    public static final String MANAGER_RECORD_FORMAT = "MR%05d";

    // Servers
    public enum Server_ID {CA, US, UK}
    public static final String CANADA_HOSTNAME = "localhost";
    public static final String USA_HOSTNAME = "localhost";
    public static final String UNITED_KINGDOM_HOSTNAME = "localhost";
    public static final int CANADA_UDP_PORT = 6000;
    public static final int USA_UDP_PORT = 6001;
    public static final int UNITED_KINGDOM_UDP_PORT = 6002;
    public static final int CANADA_RMI_PORT = 1099;
    public static final int USA_RMI_PORT = 1099;
    public static final int UNITED_KINGDOM_RMI_PORT = 1099;
    public static final String GET_RECORDS_COUNT = "%s count: %s";
    public static final String TRANSFER_MANAGER_RECORD = "transferMRecord: Into new server: %s by Manager: %s , the record ID: %s Name = %s %s, EmployeeID = %s, Email = %s,ProjectInfo = %s, Location = %s";
    public static final String TRANSFER_EMPLOYEE_RECORD = "transferERecord: Into new server: %s by Manager: %s , the record ID: %s Name = %s %s, EmployeeID = %s, Email = %s,ProjectID = %s";
    public static final String RMI_REGISTRY_FORMAT = "rmi://%s:%d/";
    public static final String DELIMITER = "|";
    // Logging
    public static final String LOG_SERVER_FILENAME = "/Users/Andy/Desktop/RMILOGS/SOEN423/A2/Server_Log_File.log";
    public static final String LOG_Tansfered_record = "/Users/Andy/Desktop/RMILOGS/SOEN423/A2/Tansfered Server %s.log";
    public static final String LOG_MANAGER_FILENAME = "/Users/Andy/Desktop/RMILOGS/SOEN423/A2/Manager_ID= %s.log";
    public static final String LOG_MANAGER_CA_FILENAME = "/Users/Andy/Desktop/RMILOGS/SOEN423/A2/Manager_CA_ID=%s.log";
    public static final String LOG_MANAGER_US_FILENAME = "/Users/Andy/Desktop/RMILOGS/SOEN423/A2/Manager_US_ID=%s.log";
    public static final String LOG_MANAGER_UK_FILENAME = "/Users/Andy/Desktop/RMILOGS/SOEN423/A2/Manager_UK_ID=%s.log";
    public static final String LOG_MODIFIED_RECORD_SUCCESS = "%s Modified record: ID = %s, FieldName = %s, Value = %s";
    public static final String LOG_NEW_EMPLOYEE_MODIFIED_RECORD = "%s Updated records: ID = %s, Name = %s %s, EmployeeID = %s, Email = %s,ProjectID = %s"; 
    public static final String LOG_NEW_MANAGER_MODIFIED_RECORD = "%s Updated records: ID = %s, Name = %s %s, EmployeeID = %s, Email = %s,ProjectInfo = %s, Location = %s"; 
    public static final String LOG_MODIFIED_RECORD_FAILED = "%s No record found: ID = %s, FieldName = %s, Value = %s";
    public static final String LOG_ADD_MANAGER_RECORD = " \n %s Added record: ID = %s, Name = %s %s, EmployeeID = %s, Email = %s, ProjectInfo = %s, Location = %s";
    public static final String LOG_ADD_EMPLOYEE_RECORD = " \n %s Added record: ID = %s, Name = %s %s, EmployeeID = %s, Email = %s,ProjectID = %s";
    public static final String LOG_TRANSFER_MANAGER_RECORD = "%s transfer %s: Name(%s %s) EmployeeID(%s) Email(%s) ProjectInfo(%s) Location(%s)";
    public static final String LOG_TRANSFER_EMPLOYEE_RECORD = "%s transfer %s: Name(%s %s) EmployeeID(%s) Email(%s) ProjectID(%s)";
    public static final String LOG_TRANSFER_RECORD_SUCCESS = "%s transfer %s to %s";
    public static final String LOG_TRANSFER_RECORD_FAIL = "%s failed to transfer %s to %s";
    
    public static final String LOG_UDP_REQUEST_FROM = "UDP Request from: Address = %s, Port = %s";
    public static final String LOG_UDP_RESPONSE_TO = "UDP Response to: Address = %s, Port = %s";
    public static final String LOG_UDP_REQUEST_TO = "UDP Request to: Address = %s, Port = %s";
    public static final String LOG_UDP_RESPONSE_FROM = "UDP Response from: Address = %s, Port = %s";
    public static final String LOG_UDP_SERVER_START = "UDP Server started at port %s";
    public static final String LOG_UDP_SERVER_STOP = "UDP Server at port %s stopped";
    public static final String LOG_CONNECT_RMI_SUCCESS = "Connect to the %s server at port %s successfully";

    public static int getUDPPortByServerID(Server_ID server_id) {
        switch (server_id) {
            case CA:
                return CANADA_UDP_PORT;
            case US:
                return USA_UDP_PORT;
            case UK:
                return UNITED_KINGDOM_UDP_PORT;
            default:
                return 0;
        }
    }

    public static int getRMIPortByServerID(Server_ID server_id) {
        switch (server_id) {
            case CA:
                return CANADA_RMI_PORT;
            case US:
                return USA_RMI_PORT;
            case UK:
                return UNITED_KINGDOM_RMI_PORT;
            default:
                return 0;
        }
    }
    
    public static String getHostnameByServerID(Server_ID server_id) {
        switch (server_id) {
            case CA:
                return CANADA_HOSTNAME;
            case US:
                return USA_HOSTNAME;
            case UK:
                return UNITED_KINGDOM_HOSTNAME;
            default:
                return "Wrong Server ID";
        }
    }
}
