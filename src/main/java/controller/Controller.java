
package controller;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import model.DataPoints.HistoryRequest;
import model.EnumBaseURLs;
import model.EnumUsers;
import model.PropertyChangeNames;
import model.RestClient.RequestsResponses;
import model.TeslaAPIModel;
import org.joda.time.DateTime;


public class Controller {

    private TeslaAPIModel model = null;

    public Controller() {

    }

    public void tellControllerAboutTheModel(TeslaAPIModel model) {
        this.model = model;
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

}
