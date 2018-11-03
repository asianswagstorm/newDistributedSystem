package Records;

public class Employee extends Record {
	public enum Mutable_Fields {MailId, employee_id, project_id}
	//object attributes
	static String firstname;
	static String lastname;
	static String MailId;
	static String employee_id;
	static String project_id;
	
	public Employee(String recordID, String firstname,String lastname,String employee_id ,String MailId,String project_id) {
		super(recordID, firstname, lastname, Record_Type.EMPLOYEE);
		Employee.firstname = firstname;
		Employee.lastname = lastname;
		Employee.employee_id = employee_id;
		Employee.MailId = MailId;
		Employee.project_id = project_id;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		Employee.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		Employee.lastname = lastname;
	}
	public String getMailId() {
		return MailId;
	}
	public void setMailId(String mailId) {
		Employee.MailId = mailId;
	}
	public String getEmployee_id() {
		return employee_id;
	}
	public void setEmployee_id(String employee_id) {
		Employee.employee_id = employee_id;
	}
	public String getProject_id() {
		return project_id;
	}
	public void setProject_id(String project_id) {
		Employee.project_id = project_id;
	}

	
	
}
