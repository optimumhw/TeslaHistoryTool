
package model.LoadFromE3OS;

import model.DatapointList.DatapointListItem;


public class MappingTableRow {

    private EnumMapStatus mapStatus;
    private String e3osName;
    private DataPointFromSql xid;
    private String teslaName;
    private String teslaType;
    private String teslaID;

    public MappingTableRow() {

        mapStatus = EnumMapStatus.NoInfo;
        e3osName = "?";
        xid = new DataPointFromSql();
        teslaName = "?";
        teslaType = "?";
        teslaID = "?";
    }
    
    public MappingTableRow(DatapointListItem pt){
        mapStatus = EnumMapStatus.NoE3OSInfo;
        e3osName = "?";
        xid = new DataPointFromSql();
        teslaName = pt.getShortName();
        teslaType = pt.getPointType();
        teslaID = pt.getId();
    }
    
        public MappingTableRow(DataPointFromSql e3osPoint){
        mapStatus = EnumMapStatus.NoTeslaInfo;
        e3osName = e3osPoint.getDatapointName();
        xid = e3osPoint;
        teslaName = "?";
        teslaType = "?";
        teslaID = "?";
    }
    
    public void setMapStatus(EnumMapStatus mapStatus) {
        this.mapStatus = mapStatus;
    }

    public EnumMapStatus getMapStatus() {
        return mapStatus;
    }
    
   
    public void setE3osName(String e3osName) {
        this.e3osName = e3osName;
    }

    public String getE3osName() {
        return e3osName;
    }

    public void setXid(DataPointFromSql xid) {
        this.xid = xid;
    }

    public DataPointFromSql getXid() {
        return xid;
    }

    public void setTeslaName(String teslaName) {
        this.teslaName = teslaName;
    }

    public String getTeslaName() {
        return teslaName;
    }
    
    public void setTeslaType(String teslaType) {
        this.teslaType = teslaType;
    }

    public String getTeslaType() {
        return teslaType;
    }

    public void setTeslaID(String teslaID) {
        this.teslaID = teslaID;
    }

    public String getTeslaID() {
        return teslaID;
    }

}

