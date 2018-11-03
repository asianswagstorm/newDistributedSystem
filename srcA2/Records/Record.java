package Records;
import java.io.Serializable;

public class Record implements Serializable {
    public enum Record_Type {MANAGER, EMPLOYEE}
    private String recordID;
    private String firstname;
    private String lastname;
    private Record_Type recordType;

    public Record(String recordID, String firstname, String lastname, Record_Type recordType) {
        this.recordID = recordID;
        this.firstname = firstname;
        this.lastname = lastname;
        this.recordType = recordType;
    }

    public String getRecordID() {
        return this.recordID;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Record_Type getRecordType() {
        return this.recordType;
    }
}
