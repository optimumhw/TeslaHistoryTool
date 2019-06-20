package model;

public enum PropertyChangeNames {

    LoginResponseReturned("LoginResponseReturned"),
    ErrorResponse("ErrorResponse"),
    RequestResponseChanged("RequestResponseChanged"),
    StationsListReturned("StationsListReturned"),
    StationInfoRetrieved("StationInfoRetrieved"),
    DatapointsReturned("DatapointsReturned"),
    StationInfoAndSubFlagRetrieved("DatapointsAndSubFlagRetrieved"),
    LiveDataReturned("LiveDataReturned"),
    HistoryReturned("HistoryReturned"),
    ComboHistoryReturned("ComboHistoryReturned"),
    StationDatapointHistoryOneHourPushed("StationDatapointHistoryOneHourPushed"),
    StationHistoryAllPushed("StationHistoryForPeriodPushed"),
    CSVCreated("CSVCreated"),
    E3OSSitesReturned("E3OSSitesReturned"),
    E3OSPointsReturned("E3OSPointsReturned"),
    TeslaBucketPushed("TeslaBucketPushed"),
    TeslaPushComplete("TeslaPushComplete");

    private final String name;

    PropertyChangeNames(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
