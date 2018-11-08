package Records;

public class Employee extends Record {
	public enum Mutable_Fields {MailId, employee_id, project_id}
	//object attributes
	String firstname;
	String lastname;
	String MailId;
	String employee_id;
	String project_id;
	
	public Employee(String recordID, String firstname,String lastname,String employee_id ,String MailId,String project_id) {
		super(recordID, firstname, lastname, Record_Type.EMPLOYEE);
		this.firstname = firstname;
		this.lastname = lastname;
		this.employee_id = employee_id;
		this.MailId = MailId;
		this.project_id = project_id;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getMailId() {
		return MailId;
	}
	public void setMailId(String mailId) {
		MailId = mailId;
	}
	public String getEmployee_id() {
		return employee_id;
	}
	public void setEmployee_id(String employee_id) {
		this.employee_id = employee_id;
	}
	public String getProject_id() {
		return project_id;
	}
	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}

	
	
}
