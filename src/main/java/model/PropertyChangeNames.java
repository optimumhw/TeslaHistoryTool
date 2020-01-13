package model;

public enum PropertyChangeNames {

    PrimaryLoginResponseReturned("LoginResponseReturned"),
    SecondaryLoginResponseReturned("FromLoginResponseReturned"),
    ErrorResponse("ErrorResponse"),
    RequestResponseChanged("RequestResponseChanged"),
    StationsListReturned("StationsListReturned"),
    SecondaryStationsListReturned("SecondaryStationsListReturned"),
    StationInfoRetrieved("StationInfoRetrieved"),
    DatapointsReturned("DatapointsReturned"),
    SecondaryDatapointsReturned("SecondaryDatapointsReturned"),
    StationInfoAndSubFlagRetrieved("DatapointsAndSubFlagRetrieved"),
    LiveDataReturned("LiveDataReturned"),
    HistoryReturned("HistoryReturned"),
    FrameProcessed("FrameProcessed"),
    FrameError("FrameError"),
    FramesCompleted("HistoryReturned"),
    ComboHistoryReturned("ComboHistoryReturned"),
    StationDatapointHistoryOneHourPushed("StationDatapointHistoryOneHourPushed"),
    StationHistoryAllPushed("StationHistoryForPeriodPushed"),
    CSVCreated("CSVCreated"),
    E3OSSitesReturned("E3OSSitesReturned"),
    E3OSPointsReturned("E3OSPointsReturned"),
    TeslaBucketPushed("TeslaBucketPushed"),
    TeslaPushComplete("TeslaPushComplete"),
    E3OSLiveAuthenticated("E3OSLiveAuthenticated"),
    E3OSStationListReturned("E3OSStationListReturned"),
    E3OSPointsListReturned("E3OSPointsListReturned"),
    E3OSLiveDataReturned("E3OSLiveDataReturned");

    private final String name;

    PropertyChangeNames(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
