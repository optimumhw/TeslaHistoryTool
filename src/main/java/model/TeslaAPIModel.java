package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingWorker;
import model.Auth.LoginResponse;
import model.CSVCreator.CSVMaker;
import model.DataPoints.Datapoint;
import model.DataPoints.Equipment;
import model.DataPoints.HistoryQueryResults;
import model.DataPoints.HistoryRequest;
import model.DataPoints.LiveDatapoint;
import model.DataPoints.StationInfo;
import model.DatapointList.DatapointListItem;
import model.LoadFromE3OS.DSG2QueryResultRecord;
import model.LoadFromE3OS.DSG2Runner;
import model.LoadFromE3OS.DataPointFromSql;
import model.LoadFromE3OS.E3OSConnProperties;
import model.LoadFromE3OS.MappingTableRow;
import model.LoadFromE3OS.PointsListQueryRunner;
import model.LoadFromE3OS.TeslaDataPointUpsertRequest;
import model.RestClient.LoginClient;
import model.RestClient.OEResponse;
import model.RestClient.RequestsResponses;
import model.RestClient.RestClientCommon;
import model.RestClient.StationClient;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeslaAPIModel extends java.util.Observable {

    private RestClientCommon api;
    private RequestsResponses rrs;

    private LoginClient loginClient;
    private StationClient stationClient;
    private EnumBaseURLs baseURL;

    final private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    static Logger logger = LoggerFactory.getLogger(TeslaAPIModel.class.getName());

    public void addPropChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void initModel() {
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
    public void login(final EnumBaseURLs baseUrl) {

        if (baseUrl == null) {
            return;
        }

        this.baseURL = baseUrl;

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {
                OEResponse results = loginClient.login(baseUrl);
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

    public void getE3OSDatapoints(final String stationID) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                PointsListQueryRunner queryRunner = new PointsListQueryRunner();
                List<DataPointFromSql> listOfPoints = queryRunner.runDataPointsQuery(stationID);
                OEResponse results = new OEResponse();
                results.responseCode = 200;
                results.responseObject = listOfPoints;
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        List<DataPointFromSql> pointsList = (List<DataPointFromSql>) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.E3OSPointsReturned.getName(), null, pointsList);
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

    public void pullFromE3OSPushToTesla(
            final DateTime pushStartTime,
            final DateTime pushEndTime,
            final List<MappingTableRow> mappedRows,
            final int maxHoursPerPush,
            final int maxPointsPerPush) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                //Push data on hour boudaries
                //Starting from startTime with minutes, seconds, millis set to zero.
                DateTime intervalStart = pushStartTime.minusMillis(pushStartTime.getMillisOfSecond());
                intervalStart = intervalStart.minusSeconds(intervalStart.getSecondOfMinute());
                intervalStart = intervalStart.minusMinutes(intervalStart.getMinuteOfHour());

                //endOfPeriod is the number of whole hours between the startDate and the endDate.
                Hours hours = Hours.hoursBetween(intervalStart, pushEndTime);
                DateTime endOfPeriod = intervalStart.plusHours(hours.getHours());

                //if the endDate was not on an hour boundary, add an hour to cover the remainder.
                //e.g., if endDate was ...03:45:37 we want to push data to ...04:00:00
                if (pushEndTime.isAfter(endOfPeriod)) {
                    endOfPeriod = endOfPeriod.plusHours(1);
                }

                while (intervalStart.isBefore(endOfPeriod)) {

                    DateTime intervalEnd = intervalStart.plusHours(maxHoursPerPush);

                    int startPushIndex = 0;

                    while (startPushIndex < mappedRows.size()) {

                        int endIndex = Math.min(startPushIndex + maxPointsPerPush, mappedRows.size());

                        List<MappingTableRow> pointsToPush = mappedRows.subList(startPushIndex, endIndex);
                        pullFromEdisonPushToTeslaInterval(intervalStart, intervalEnd, pointsToPush);
                        pcs.firePropertyChange(PropertyChangeNames.TeslaBucketPushed.getName(), null, 1);
                        startPushIndex += maxPointsPerPush;
                    }

                    //increment loop index
                    intervalStart = intervalEnd;
                }

                OEResponse periodHistoryPushStatus = new OEResponse();
                periodHistoryPushStatus.responseCode = 201;
                periodHistoryPushStatus.responseObject = "points pushed";
                return periodHistoryPushStatus;

            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 201) {
                        String msg = (String) resp.responseObject;
                        pcs.firePropertyChange(PropertyChangeNames.TeslaPushComplete.getName(), null, msg);
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

    private OEResponse pullFromEdisonPushToTeslaInterval(DateTime pushStartTime, DateTime pushEndTime, List<MappingTableRow> mappedRows) {

        List<String> e3osPointNames = new ArrayList<>();
        List<DataPointFromSql> points = new ArrayList<>();
        Map< String, MappingTableRow> e3osNameToMappingTableRowMap = new HashMap<>();

        for (MappingTableRow mtr : mappedRows) {
            e3osPointNames.add(mtr.getE3osName());
            e3osNameToMappingTableRowMap.put(mtr.getE3osName(), mtr);
            points.add(mtr.getXid());
        }

        try {

            E3OSConnProperties e3osConnProps = new E3OSConnProperties();

            DSG2Runner dsg2Runner = new DSG2Runner(e3osConnProps);

            String startTime = pushStartTime.toString();
            String endTime = pushEndTime.toString();

            List<DSG2QueryResultRecord> e3osHistory = dsg2Runner.runDSG2Query(startTime, endTime, points);

            if (e3osHistory.size() == 0) {
                OEResponse resp = new OEResponse();
                resp.responseCode = 201;
                resp.responseObject = "no histories from e3os";
                return resp;
            }
            
            TeslaDataPointUpsertRequest tdpu = new TeslaDataPointUpsertRequest(e3osHistory, e3osNameToMappingTableRowMap);
            OEResponse teslaPutResponse = stationClient.putHistory(tdpu);

            if (teslaPutResponse.responseCode == 422) {
                System.out.println("unprocessable entity");
                return teslaPutResponse;
            }

            if (teslaPutResponse.responseCode >= 500) {
                System.out.println("retrying...");
                teslaPutResponse = stationClient.putHistory(tdpu);

            } else if (teslaPutResponse.responseCode == 401) {
                System.out.println("getting a new token. was:");
                System.out.println(api.getOAuthToken());

                OEResponse resp = loginClient.login(this.baseURL);

                if (resp.responseCode == 200) {
                    LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                    String newToken = loginResponse.getAccessToken();
                    api.setOauthToken(newToken);

                    System.out.println("new token is:");
                    System.out.println(api.getOAuthToken());

                    teslaPutResponse = stationClient.putHistory(tdpu);
                }
            }

            return teslaPutResponse;

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(TeslaAPIModel.class.getName()).log(Level.SEVERE, null, ex);
            OEResponse resp = new OEResponse();
            resp.responseCode = 999;
            resp.responseObject = "not sure";
            return resp;
        }
    }

}
