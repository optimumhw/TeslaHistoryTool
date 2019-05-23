package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.List;
import javax.swing.SwingWorker;
import model.Auth.LoginResponse;
import model.CSVCreator.CSVMaker;
import model.DataPoints.Datapoint;
import model.DataPoints.DatapointUpsertRequest;
import model.DataPoints.EnumResolutions;
import model.DataPoints.Equipment;
import model.DataPoints.HistoryQueryResults;
import model.DataPoints.HistoryRequest;
import model.DataPoints.LiveDatapoint;
import model.DataPoints.StationInfo;
import model.DatapointList.DatapointListItem;
import model.RestClient.LoginClient;
import model.RestClient.OEResponse;
import model.RestClient.RequestsResponses;
import model.RestClient.RestClientCommon;
import model.RestClient.StationClient;
import model.simulator.UpsertPoint;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeslaAPIModel extends java.util.Observable {

    private RestClientCommon api;
    private RequestsResponses rrs;

    private LoginClient loginClient;
    private StationClient stationClient;

    final private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    static Logger logger = LoggerFactory.getLogger(TeslaAPIModel.class.getName());

    public void addPropChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void initModel(EnumBaseURLs baseURL, EnumUsers user) {
        rrs = new RequestsResponses();
        api = new RestClientCommon(rrs);
        loginClient = new LoginClient(api);
    }

    public RestClientCommon getRestClient() {
        return api;
    }

    public RequestsResponses getRRS() {
        return this.rrs;
    }

    public void clearRRS() {
        rrs.clear();
        pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());
    }

    // === LOGIN ==========
    public void login(final EnumBaseURLs baseUrl, final EnumUsers user) {

        if (baseUrl == null || user == null) {
            return;
        }

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {
                OEResponse results = loginClient.login(baseUrl, user);
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    LoginResponse loginResponse;

                    if (resp.responseCode == 200) {
                        loginResponse = (LoginResponse) resp.responseObject;

                        stationClient = new StationClient(baseUrl, api);
                        stationClient.setServiceURLAndToken(baseUrl, loginResponse.getAccessToken());

                    } else {
                        loginResponse = new LoginResponse();
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.LoginResponseReturned.getName(), null, loginResponse);
                    pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());

                } catch (Exception ex) {
                    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                    logger.error(this.getClass().getName(), ex);
                }
            }
        };
        worker.execute();
    }

    // === STATIONS  ======================
    public void getStations() {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {
                OEResponse results = stationClient.getStations();
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        List<StationInfo> stations = (List<StationInfo>) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.StationsListReturned.getName(), null, stations);
                    } else {
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());

                } catch (Exception ex) {
                    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                    logger.error(this.getClass().getName(), ex);
                }
            }
        };
        worker.execute();
    }

    public void getStationInfo(final String stationID) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {
                OEResponse results = stationClient.getStationInfo(stationID);
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        StationInfo stationInfo = (StationInfo) resp.responseObject;

                        for (Datapoint dp : stationInfo.getDatapoints()) {
                            if (dp.getShortName().contentEquals("CHWFLO")) {
                                String msg = String.format("%s - %s ", dp.getShortName(), dp.getId());
                                System.out.println(msg);
                            }
                        }

                        pcs.firePropertyChange(PropertyChangeNames.StationInfoRetrieved.getName(), null, stationInfo);
                    } else {
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());

                } catch (Exception ex) {
                    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                    logger.error(this.getClass().getName(), ex);
                }
            }
        };
        worker.execute();
    }

    public void getDatapoints(final String stationID) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {
                OEResponse results = stationClient.getDatapoints(stationID);
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        List<DatapointListItem> pointsList = (List<DatapointListItem>) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.DatapointsReturned.getName(), null, pointsList);
                    } else {
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());

                } catch (Exception ex) {
                    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                    logger.error(this.getClass().getName(), ex);
                }
            }
        };
        worker.execute();
    }

    public void getStationInfoAndSubscribedFlag(final String stationID) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {
                OEResponse resultA = stationClient.getStationInfo(stationID);
                OEResponse resultB = stationClient.getSubscribed(stationID);

                StationInfo stationInfo = (StationInfo) resultA.responseObject;
                setSubscribedFlag(stationInfo, (List<String>) resultB.responseObject);

                OEResponse retVal = new OEResponse();
                retVal.responseCode = 200;
                retVal.responseObject = stationInfo;

                return retVal;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        StationInfo stationInfo = (StationInfo) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.StationInfoAndSubFlagRetrieved.getName(), null, stationInfo);
                    } else {
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());

                } catch (Exception ex) {
                    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                    logger.error(this.getClass().getName(), ex);
                }
            }
        };
        worker.execute();
    }

    private void setSubscribedFlag(StationInfo stationInfo, List<String> subscribedPoints) {

        if (subscribedPoints == null || subscribedPoints.size() <= 0) {
            return;
        }

        for (Datapoint dp : stationInfo.getDatapoints()) {
            dp.setSubScribedFlag(subscribedPoints.contains(dp.getId()));
        }

        for (Equipment eq : stationInfo.getequipments()) {
            for (Datapoint dp : eq.getDatapoints()) {
                dp.setSubScribedFlag(subscribedPoints.contains(dp.getId()));
            }
        }
    }

    public void getLiveData(final List<String> dataPointIDs) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {
                OEResponse results = stationClient.getLiveData(dataPointIDs);
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        List<LiveDatapoint> livePoints = (List<LiveDatapoint>) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.LiveDataReturned.getName(), null, livePoints);
                    } else {
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());

                } catch (Exception ex) {
                    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                    logger.error(this.getClass().getName(), ex);
                }
            }
        };
        worker.execute();
    }

    public void getHistory(final HistoryRequest historyRequest) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {
                OEResponse results = stationClient.getHistory(historyRequest);

                if (results.responseCode == 200) {

                    HistoryQueryResults history = new HistoryQueryResults((List<LiveDatapoint>) results.responseObject);

                    results = new OEResponse();
                    results.responseCode = 200;
                    results.responseObject = history;

                    return results;

                }

                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        HistoryQueryResults historyPoints = (HistoryQueryResults) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.HistoryReturned.getName(), null, historyPoints);
                    } else {
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());

                } catch (Exception ex) {
                    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                    logger.error(this.getClass().getName(), ex);
                }
            }
        };
        worker.execute();
    }

    public void putHistory(final List<UpsertPoint> dgPointsList, final EnumResolutions res, final DateTime startDateTime, final DateTime endDateTime) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                DateTime intervalStart = startDateTime.minusMillis(startDateTime.getMillisOfSecond());
                intervalStart = intervalStart.minusSeconds(intervalStart.getSecondOfMinute());
                intervalStart = intervalStart.minusMinutes(intervalStart.getMinuteOfHour());

                //endOfPeriod is the number of whole hours between the startDate and the endDate.
                DateTime stopTime = endDateTime;
                Hours hours = Hours.hoursBetween(intervalStart, stopTime);
                DateTime endOfPeriod = intervalStart.plusHours(hours.getHours());

                //if the endDate was not on an hour boundary, and an hour to cover the remainder.
                //e.g., if endDate was ...03:45:37 we want to push data to ...04:00:00
                if (stopTime.isAfter(endOfPeriod)) {
                    endOfPeriod = endOfPeriod.plusHours(1);
                }

                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                while (intervalStart.isBefore(endOfPeriod)) {

                    DateTime intervalEnd = intervalStart.plusHours(1);
                    DatapointUpsertRequest req = new DatapointUpsertRequest(dgPointsList, res, startDateTime, intervalStart, intervalEnd);

                    OEResponse hourResponse = stationClient.putHistory(req);

                    if (hourResponse.responseCode != 201) {
                        System.out.println("error pushing hour");
                    }
                    pcs.firePropertyChange(PropertyChangeNames.StationDatapointHistoryOneHourPushed.getName(), null, 1);

                    //increment loop index
                    pcs.firePropertyChange(PropertyChangeNames.StationDatapointHistoryOneHourPushed.getName(), null, 1);
                    intervalStart = intervalEnd;
                }

                OEResponse allHistoryPushedResponse = new OEResponse();
                allHistoryPushedResponse.responseCode = 200;
                allHistoryPushedResponse.responseObject = "Done!";

                return allHistoryPushedResponse;

            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        pcs.firePropertyChange(PropertyChangeNames.StationHistoryAllPushed.getName(), null, 1);
                    } else {
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());

                } catch (Exception ex) {
                    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                    logger.error(this.getClass().getName(), ex);
                }
            }
        };
        worker.execute();
    }

    public void createCSV(final String filePath, final HistoryQueryResults history) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                CSVMaker csvMaker = new CSVMaker(filePath, history);
                boolean flag = csvMaker.makeCSV();

                OEResponse results = new OEResponse();
                results.responseCode = 200;
                results.responseObject = flag;

                return results;

            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        boolean flag = (boolean) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.CSVCreated.getName(), null, flag);
                    } else {
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());

                } catch (Exception ex) {
                    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                    logger.error(this.getClass().getName(), ex);
                }
            }
        };
        worker.execute();
    }

}
