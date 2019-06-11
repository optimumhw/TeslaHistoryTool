package model.LoadFromE3OS;

public class DataPointFromSql {

    public String xid;
    public String dpName;

    public DataPointFromSql() {
        this.xid = "?";
        this.dpName = "?";
    }

    public DataPointFromSql(String fullName, String dpName) {

        this.xid = fullName.substring(0, fullName.length() - dpName.length());
        this.dpName = dpName;
    }

    public String getXID() {
        return this.xid;
    }

    public String getDatapointName() {
        return this.dpName;
    }

}
