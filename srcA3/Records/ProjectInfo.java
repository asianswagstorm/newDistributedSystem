package Records;

public class ProjectInfo {
	 private String projectId;
	 private String clientName;
	 private String projectName;
	
	 public ProjectInfo() {
		 	this.projectId = "";
			this.clientName = "";
			this.projectName = "";
	 }
	 
	 public ProjectInfo(String projectId, String clientName, String projectName) {
			super();
			this.projectId = projectId;
			this.clientName = clientName;
			this.projectName = projectName;
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


	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectId() {
		 return projectId;
	 }
}
