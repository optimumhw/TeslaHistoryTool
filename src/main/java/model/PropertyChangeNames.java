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
    HistoryReturned("HistoryReturned");
    //StationDatapointHistoryOneHourPushed("StationDatapointHistoryOneHourPushed"),
    //StationHistoryAllPushed("StationHistoryForPeriodPushed");

    private final String name;

    PropertyChangeNames(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
