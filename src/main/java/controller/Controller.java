package controller;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import model.Auth.LoginResponse;
import model.DataPoints.EnumResolutions;
import model.DataPoints.HistoryQueryResults;
import model.DataPoints.HistoryRequest;
import model.DataPoints.LiveDatapoint;
import model.DataPoints.StationInfo;
import model.EnumBaseURLs;
import model.EnumUsers;
import model.PropertyChangeNames;
import model.RestClient.OEResponse;
import model.RestClient.RequestsResponses;
import model.TeslaAPIModel;
import model.simulator.UpsertPoint;
import org.joda.time.DateTime;
import view.MainFrame;
import static model.PropertyChangeNames.LoginResponseReturned;

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

    public void initModel(EnumBaseURLs baseURL, EnumUsers user){
        model.initModel(baseURL, user);
    }
    
    public void login(final EnumBaseURLs baseUrl, final EnumUsers user){
        model.login(baseUrl, user);
    }

    public void getStations() {
        model.getStations();
    }

    public void getStationInfo(String stationID) {
        model.getStationInfo(stationID);
    }

    public void getDatapoints(String stationID) {
        model.getDatapoints(stationID);
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

    public void putHistory(List<UpsertPoint> dgPointsList, EnumResolutions res, DateTime startDateTime, DateTime endDateTime) {
        model.putHistory(dgPointsList, res, startDateTime, endDateTime);
    }
    
    public void createCSV( String filePath,  HistoryQueryResults history){
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

        } else if (propName.equals(PropertyChangeNames.LoginResponseReturned.getName())) {
            LoginResponse loginResponse = (LoginResponse) evt.getNewValue();
            view.setLoggedInInfo(true, loginResponse);

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
