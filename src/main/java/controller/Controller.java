package controller;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import model.Auth.LoginResponse;
import model.DataPoints.EnumResolutions;
import model.DataPoints.HistoryQueryResults;
import model.DataPoints.HistoryRequest;
import model.DataPoints.LiveDatapoint;
import model.DataPoints.StationInfo;
import model.DatapointList.DatapointListItem;
import model.EnumBaseURLs;
import model.EnumPrimarySecodaryClient;
import model.LoadFromE3OS.MappingTableRow;
import model.PropertyChangeNames;
import model.RestClient.OEResponse;
import model.RestClient.RequestsResponses;
import model.TTT.TTTTableRow;
import model.TeslaAPIModel;
import org.joda.time.DateTime;
import view.MainFrame;

public class Controller implements java.awt.event.ActionListener, PropertyChangeListener {

    private TeslaAPIModel model = null;
    private MainFrame view = null;

    public Controller() {

    }

    public void tellControllerAboutTheModel(TeslaAPIModel model) {
        this.model = model;
        model.addPropChangeListener(this);
    }

    public void addModelListener(PropertyChangeListener listener) {
        model.addPropChangeListener(listener);
    }

    public void removePropChangeListener(PropertyChangeListener listener) {
        model.removePropChangeListener(listener);
    }

    public TeslaAPIModel getModel() {
        return model;
    }

    public RequestsResponses getRRS() {
        return model.getRRS();
    }

    public void clearRRS() {
        model.clearRRS();
    }

    public void tellTheControllerAboutTheView(MainFrame view) {
        this.view = view;
    }

    public void initModel() {
        model.initModel();
    }

    public void login(EnumBaseURLs baseUrl) {
        model.primaryLogin(baseUrl);
    }

    public void fromLogin(EnumBaseURLs baseUrl) {
        model.secondaryLogin(baseUrl);
    }

    public void getStations(EnumPrimarySecodaryClient fromTo) {
        model.getStations(fromTo);
    }

    public void getStationInfo(String stationID) {
        model.getStationInfo(stationID);
    }

    public void getDatapoints(final EnumPrimarySecodaryClient fromTo, String stationID) {
        model.getDatapoints(fromTo, stationID);
    }

    public void getStationInfoAndSubscribedFlag(String stationID) {
        model.getStationInfoAndSubscribedFlag(stationID);
    }

    public void getLiveData(List<String> dataPointIDs) {
        model.getLiveData(dataPointIDs);
    }

    public void getHistory(final HistoryRequest historyRequest) {
        model.getHistory(historyRequest);
    }

    public void getHistoryInFrames(
            List<DatapointListItem> listOfTeslaPoints,
            DateTime startAt,
            DateTime endAt,
            String resolution,
            String timeZone,
            int maxHours,
            int maxPoints) {
        model.getHistoryInFrames(listOfTeslaPoints, startAt, endAt, resolution, timeZone, maxHours, maxPoints);
    }

    public void getComboHistory(final HistoryRequest fiveMinuteRequest, final HistoryRequest hourRequest) {
        model.getComboHistory(fiveMinuteRequest, hourRequest);
    }

    public void getE3OSSites() {
        model.getE3OSSites();
    }

    public void getE3OSDatapoints(final String stationID) {
        model.getE3OSDatapoints(stationID);
    }

    public void pullFromE3OSPushToTesla(
            final DateTime pushStartTime,
            final DateTime pushEndTime,
            final List<MappingTableRow> mappedRows,
            final int maxHoursPerPush,
            final int maxPointsPerPush) {
        model.pullFromE3OSPushToTesla(pushStartTime, pushEndTime, mappedRows, maxHoursPerPush, maxPointsPerPush);
    }

    public void pullFromTeslsPushToTesla(
            EnumPrimarySecodaryClient fromTo,
            DateTime pushStartTime,
            DateTime pushEndTime,
            List<TTTTableRow> mappedRows,
            int maxHoursPerPush,
            int maxPointsPerPush,
            String stationTimeZone) {
        model.pullFromTeslsPushToTesla(fromTo, pushStartTime, pushEndTime, mappedRows, maxHoursPerPush, maxPointsPerPush, stationTimeZone);
    }

    public void createCSV(String filePath, HistoryQueryResults history) {
        model.createCSV(filePath, history);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //Object obj = e.getSource();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        String propName = evt.getPropertyName();

        //pcs.firePropertyChange(PropertyChangeNames.LoginResponse.getName(), null, loginResponse);
        if (propName.equals(PropertyChangeNames.ErrorResponse.getName())) {
            view.showError((OEResponse) evt.getNewValue());

        } else if (propName.equals(PropertyChangeNames.PrimaryLoginResponseReturned.getName())) {
            LoginResponse loginResponse = (LoginResponse) evt.getNewValue();
            view.setLoggedInInfo(true, loginResponse);
            model.getStations(EnumPrimarySecodaryClient.Primary);

        } else if (propName.equals(PropertyChangeNames.StationsListReturned.getName())) {
            List<StationInfo> stations = (List<StationInfo>) evt.getNewValue();
            view.fillStationsTable(stations);

        } else if (propName.equals(PropertyChangeNames.StationInfoAndSubFlagRetrieved.getName())) {
            StationInfo stationInfo = (StationInfo) evt.getNewValue();
            view.fillEquipmentDropdown(stationInfo);

        } else if (propName.equals(PropertyChangeNames.LiveDataReturned.getName())) {
            List<LiveDatapoint> dpList = (List<LiveDatapoint>) evt.getNewValue();
            view.fillLiveData(dpList);
        }

    }
}
