package Records;

public class ProjectInfo {
	private int projectID;
	private String clientName;
	private String projectName;
	
	public ProjectInfo(int projectID, String clientName, String projectName) {
		super();
		this.projectID = projectID;
		this.clientName = clientName;
		this.projectName = projectName;
	}

	public int getProjectID() {
		return projectID;
	}

	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}
