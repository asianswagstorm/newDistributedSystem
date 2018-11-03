package Records;

import java.util.ArrayList;

public class Manager extends Record {
    public enum Mutable_Fields {employeeID, emailID, location}
	private String firstname;
	private String lastname;
	private String employeeID;
	private String emailID;
	private String projectInfo; 
	private String location;
	
	public Manager(String recordID, String firstname, String lastname,String employeeID ,String emailID ,String projectInfo ,String location) {
		    super(recordID, firstname, lastname, Record_Type.MANAGER);
			this.firstname = firstname;
			this.lastname = lastname;
			this.employeeID = employeeID;
			this.emailID = emailID;
			this.projectInfo = projectInfo;
			this.location = location;
		}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstName) {
		this.firstname = firstName;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastName) {
		this.lastname = lastname;
	}

	public String getEmployeeID() {
		return employeeID;
	}

	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
	}

	public String getEmailID() {
		return emailID;
	}

	public void setEmailID(String emailID) {
		this.emailID = emailID;
	}

	public String getProjectInfo() {
		return projectInfo;
	}

	public void setProjectInfo(String projectInfo) {
		this.projectInfo = projectInfo;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
