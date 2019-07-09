package model.TTT;

import model.DatapointList.DatapointListItem;


public class TTTTableRow {

    private TTTMapStatus mapStatus;
    private String fromName;
    private String fromType;
    private String fromID;
    private String toName;
    private String toType;
    private String toID;

    public TTTTableRow() {

        mapStatus = TTTMapStatus.NoInfo;
        fromName = "?";
        fromType = "?";
        fromType = "?";
        toName = "?";
        toType = "?";
        toID = "?";
    }

    public void setFrom(DatapointListItem pt) {

        fromName = pt.getShortName();
        fromType = pt.getPointType();
        fromID = pt.getId();
        toName = "?";
        toType = "?";
        toID = "?";
    }

    public void setTo(DatapointListItem pt) {

        fromName = "?";
        fromType = "?";
        fromID = "?";
        toName = pt.getShortName();
        toType = pt.getPointType();
        toID = pt.getId();
    }

    public void setMapStatus(TTTMapStatus mapStatus) {
        this.mapStatus = mapStatus;
    }

    public TTTMapStatus getMapStatus() {
        return mapStatus;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public String getFromType() {
        return fromName;
    }

    public void setFromID(String fromID) {
        this.fromID = fromID;
    }

    public String getFromID() {
        return fromID;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getToName() {
        return toName;
    }

    public void setToType(String toType) {
        this.toType = toType;
    }

    public String getToType() {
        return toType;
    }

    public void setToID(String toID) {
        this.toID = toID;
    }

    public String getToID() {
        return toID;
    }

}
