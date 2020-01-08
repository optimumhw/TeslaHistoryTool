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
import model.DataPoints.ComboHistories;
import model.DataPoints.Datapoint;
import model.DataPoints.Equipment;
import model.DataPoints.HistoryQueryResults;
import model.DataPoints.HistoryRequest;
import model.DataPoints.LiveDatapoint;
import model.DataPoints.StationInfo;
import model.DatapointList.DatapointListItem;
import model.E3OS.CustTreeList.E3OSSite;
import model.E3OS.LoadFromE3OS.DSG2QueryResultRecord;
import model.E3OS.LoadFromE3OS.DSG2Runner;
import model.E3OS.LoadFromE3OS.DataPointFromSql;
import model.E3OS.LoadFromE3OS.E3OSConnProperties;
import model.E3OS.LoadFromE3OS.E3OSStationRecord;
import model.E3OS.LoadFromE3OS.MappingTableRow;
import model.E3OS.LoadFromE3OS.PointsListQueryRunner;
import model.E3OS.LoadFromE3OS.SiteQuery;
import model.E3OS.LoadFromE3OS.TeslaDataPointUpsertRequest;
import model.E3OS.E3OSClient;
import model.E3OS.E3OSLiveData.E3osAuthResponse;
import model.E3OS.E3OSLiveData.LiveDataRequest;
import model.E3OS.E3OSLiveData.LiveDataResponse;
import model.RestClient.LoginClient;
import model.RestClient.OEResponse;
import model.RestClient.RequestsResponses;
import model.RestClient.RestClientCommon;
import model.RestClient.StationClient;
import model.TTT.TTTDataPointUpsertRequest;
import model.TTT.TTTTableRow;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeslaAPIModel extends java.util.Observable {

    private RestClientCommon primaryRestClient;
    private RestClientCommon secondaryRestClient;
    private RequestsResponses rrs;

    private LoginClient primaryLoginClient;
    private LoginClient secondaryLoginClient;
    private StationClient primaryStationClient;
    private StationClient secondaryStationClient;
    private EnumBaseURLs primaryBaseURL;
    private EnumBaseURLs secondaryBaseURL;
    private E3OSClient e3osClient;

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
        primaryRestClient = new RestClientCommon(rrs);
        primaryLoginClient = new LoginClient(primaryRestClient);
        secondaryRestClient = new RestClientCommon(rrs);
        secondaryLoginClient = new LoginClient(secondaryRestClient);
        e3osClient = new E3OSClient(rrs);

    }

    public RestClientCommon getPrimaryRestClient() {
        return primaryRestClient;
    }

    public RestClientCommon getSecondaryRestClient() {
        return secondaryRestClient;
    }

    public RequestsResponses getRRS() {
        return this.rrs;
    }

    public void clearRRS() {
        rrs.clear();
        pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());
    }

    // === LOGIN ==========
    public void primaryLogin(final EnumBaseURLs primaryBaseURL) {

        if (primaryBaseURL == null) {
            return;
        }

        this.primaryBaseURL = primaryBaseURL;

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {
                OEResponse results = primaryLoginClient.login(primaryBaseURL);
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    LoginResponse loginResponse;

                    if (resp.responseCode == 200) {
                        loginResponse = (LoginResponse) resp.responseObject;

                        primaryStationClient = new StationClient(primaryBaseURL, primaryRestClient);
                        primaryStationClient.setServiceURLAndToken(primaryBaseURL, loginResponse.getAccessToken());

                    } else {
                        loginResponse = new LoginResponse();
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.PrimaryLoginResponseReturned.getName(), null, loginResponse);
                    pcs.firePropertyChange(PropertyChangeNames.RequestResponseChanged.getName(), null, getRRS());

                } catch (Exception ex) {
                    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                    logger.error(this.getClass().getName(), ex);
                }
            }
        };
        worker.execute();
    }

    public void secondaryLogin(final EnumBaseURLs secondaryBaseURL) {

        if (secondaryBaseURL == null) {
            return;
        }

        this.secondaryBaseURL = secondaryBaseURL;

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {
                OEResponse results = secondaryLoginClient.login(secondaryBaseURL);
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    LoginResponse loginResponse;

                    if (resp.responseCode == 200) {
                        loginResponse = (LoginResponse) resp.responseObject;

                        secondaryStationClient = new StationClient(secondaryBaseURL, secondaryRestClient);
                        secondaryStationClient.setServiceURLAndToken(secondaryBaseURL, loginResponse.getAccessToken());

                    } else {
                        loginResponse = new LoginResponse();
                        pcs.firePropertyChange(PropertyChangeNames.ErrorResponse.getName(), null, resp);
                    }
                    pcs.firePropertyChange(PropertyChangeNames.SecondaryLoginResponseReturned.getName(), null, loginResponse);
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
    public void getStations(final EnumPrimarySecodaryClient primaryOrSecondaryClient) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                OEResponse getStationsRequest;
                if (primaryOrSecondaryClient == EnumPrimarySecodaryClient.Primary) {
                    getStationsRequest = primaryStationClient.getStations();

                    if (getStationsRequest.responseCode == 401) {
                        System.out.println("getting a new primary token. was:");
                        System.out.println(primaryRestClient.getOAuthToken());
                        OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                        if (resp.responseCode == 200) {
                            LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                            String newToken = loginResponse.getAccessToken();
                            primaryRestClient.setOauthToken(newToken);
                            System.out.println("new primary token is:");
                            System.out.println(primaryRestClient.getOAuthToken());
                            getStationsRequest = primaryStationClient.getStations();

                        }
                    }

                } else {
                    getStationsRequest = secondaryStationClient.getStations();

                    if (getStationsRequest.responseCode == 401) {
                        System.out.println("getting a new secondary token. was:");
                        System.out.println(secondaryRestClient.getOAuthToken());
                        OEResponse resp = secondaryLoginClient.login(secondaryBaseURL);

                        if (resp.responseCode == 200) {
                            LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                            String newToken = loginResponse.getAccessToken();
                            secondaryRestClient.setOauthToken(newToken);
                            System.out.println("new secondary token is:");
                            System.out.println(secondaryRestClient.getOAuthToken());
                            getStationsRequest = secondaryStationClient.getStations();

                        }
                    }
                }

                return getStationsRequest;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        List<StationInfo> stations = (List<StationInfo>) resp.responseObject;

                        if (primaryOrSecondaryClient == EnumPrimarySecodaryClient.Primary) {
                            pcs.firePropertyChange(PropertyChangeNames.StationsListReturned.getName(), null, stations);
                        } else {
                            pcs.firePropertyChange(PropertyChangeNames.SecondaryStationsListReturned.getName(), null, stations);
                        }
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
                OEResponse getStationInfoRequest = primaryStationClient.getStationInfo(stationID);

                if (getStationInfoRequest.responseCode == 401) {
                    System.out.println("getting a new token. was:");
                    System.out.println(primaryRestClient.getOAuthToken());

                    OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                    if (resp.responseCode == 200) {
                        LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                        String newToken = loginResponse.getAccessToken();
                        primaryRestClient.setOauthToken(newToken);

                        System.out.println("new token is:");
                        System.out.println(primaryRestClient.getOAuthToken());

                        getStationInfoRequest = primaryStationClient.getStationInfo(stationID);
                    }
                }

                return getStationInfoRequest;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        StationInfo stationInfo = (StationInfo) resp.responseObject;
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

    public void getDatapoints(final EnumPrimarySecodaryClient primaryOrSecondaryClient, final String stationID) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                OEResponse getDataPointsRequest;

                if (primaryOrSecondaryClient == EnumPrimarySecodaryClient.Primary) {
                    getDataPointsRequest = primaryStationClient.getDatapoints(stationID);

                    if (getDataPointsRequest.responseCode == 401) {
                        System.out.println("getting a new primary token. was:");
                        System.out.println(primaryRestClient.getOAuthToken());

                        OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                        if (resp.responseCode == 200) {
                            LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                            String newToken = loginResponse.getAccessToken();
                            primaryRestClient.setOauthToken(newToken);
                            System.out.println("new primary token is:");
                            System.out.println(primaryRestClient.getOAuthToken());
                            getDataPointsRequest = primaryStationClient.getDatapoints(stationID);
                        }
                    }

                } else {
                    getDataPointsRequest = secondaryStationClient.getDatapoints(stationID);

                    if (getDataPointsRequest.responseCode == 401) {
                        System.out.println("getting a new secondary token. was:");
                        System.out.println(secondaryRestClient.getOAuthToken());
                        OEResponse resp = secondaryLoginClient.login(secondaryBaseURL);

                        if (resp.responseCode == 200) {
                            LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                            String newToken = loginResponse.getAccessToken();
                            secondaryRestClient.setOauthToken(newToken);
                            System.out.println("new token is:");
                            System.out.println(secondaryRestClient.getOAuthToken());
                            getDataPointsRequest = secondaryStationClient.getDatapoints(stationID);
                        }
                    }
                }
                return getDataPointsRequest;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        List<DatapointListItem> pointsList = (List<DatapointListItem>) resp.responseObject;

                        if (primaryOrSecondaryClient == EnumPrimarySecodaryClient.Primary) {

                            pcs.firePropertyChange(PropertyChangeNames.DatapointsReturned.getName(), null, pointsList);
                        } else {
                            pcs.firePropertyChange(PropertyChangeNames.SecondaryDatapointsReturned.getName(), null, pointsList);
                        }
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
                OEResponse resultA = primaryStationClient.getStationInfo(stationID);
                OEResponse resultB = primaryStationClient.getSubscribed(stationID);

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
                OEResponse getLiveDataRequest = primaryStationClient.getLiveData(dataPointIDs);

                if (getLiveDataRequest.responseCode == 401) {
                    System.out.println("getting a new token. was:");
                    System.out.println(primaryRestClient.getOAuthToken());

                    OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                    if (resp.responseCode == 200) {
                        LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                        String newToken = loginResponse.getAccessToken();
                        primaryRestClient.setOauthToken(newToken);

                        System.out.println("new token is:");
                        System.out.println(primaryRestClient.getOAuthToken());

                        getLiveDataRequest = primaryStationClient.getLiveData(dataPointIDs);
                    }

                }

                return getLiveDataRequest;
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

    public void getHistoryInFrames(
            final List<DatapointListItem> listOfTeslaPoints,
            final DateTime startAt,
            final DateTime endAt,
            final String resolution,
            final String timeZone,
            final int maxHours,
            final int maxPoints) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                final String fiveMinuteString = "fiveMinute";

                HistoryQueryResults masterResults = new HistoryQueryResults(true, listOfTeslaPoints);

                DateTime frameStart = startAt;

                int countOfFramesProcessed = 0;

                while (frameStart.isBefore(endAt)) {

                    DateTime frameEnd = frameStart.plusHours(maxHours);
                    if (frameEnd.isAfter(endAt)) {
                        frameEnd = endAt;
                    }
                    
                    
                    OEResponse historyQueryResponse = null;

                    for (int pointIndexStart = 0; pointIndexStart < listOfTeslaPoints.size(); pointIndexStart += maxPoints) {
                        int pointIndexEnd = Math.min(pointIndexStart + maxPoints, listOfTeslaPoints.size());
                        List<DatapointListItem> framePoints = listOfTeslaPoints.subList(pointIndexStart, pointIndexEnd);

                        if (!resolution.contentEquals(fiveMinuteString)) {

                            List<String> framePointIDs = new ArrayList<>();
                            for (DatapointListItem tdp : framePoints) {
                                framePointIDs.add(tdp.getId());
                            }

                            HistoryRequest hr = new HistoryRequest(framePointIDs, frameStart, frameEnd, resolution, timeZone);
                            historyQueryResponse = primaryStationClient.getHistory(hr);
                            
                            if (historyQueryResponse.responseCode == 401) {
                            System.out.println("getting a new token. was:");
                            System.out.println(primaryRestClient.getOAuthToken());

                            OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                            if (resp.responseCode == 200) {
                                LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                                String newToken = loginResponse.getAccessToken();
                                primaryRestClient.setOauthToken(newToken);

                                System.out.println("new token is:");
                                System.out.println(primaryRestClient.getOAuthToken());

                                historyQueryResponse = primaryStationClient.getHistory(hr);
                            }

                        }

                        } else {

                            List<String> listOfFiveMinutePointIDs = new ArrayList<>();
                            List<String> listOfHourlyPointIDs = new ArrayList<>();

                            for (DatapointListItem teslaPoint : framePoints) {
                                String minRes = teslaPoint.getMinimumResolution();
                                if (minRes.contentEquals(fiveMinuteString)) {
                                    listOfFiveMinutePointIDs.add(teslaPoint.getId());
                                } else {
                                    listOfHourlyPointIDs.add(teslaPoint.getId());
                                }
                            }

                            HistoryRequest fiveMinuteRequest = new HistoryRequest(listOfFiveMinutePointIDs, frameStart, frameEnd, resolution, timeZone);
                            HistoryRequest hourRequest = new HistoryRequest(listOfHourlyPointIDs, frameStart, frameEnd, "hour", timeZone);

                            OEResponse fiveMinResponse  = null;
                            OEResponse hourResponse  = null;
                            
                            HistoryQueryResults fiveMinuteResults = null;
                            HistoryQueryResults hourResults = null;

                            if (fiveMinuteRequest.getIds().size() > 0) {
                                fiveMinResponse = primaryStationClient.getHistory(fiveMinuteRequest);

                                if (fiveMinResponse.responseCode == 401) {
                                    System.out.println("getting a new token. was:");
                                    System.out.println(primaryRestClient.getOAuthToken());

                                    OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                                    if (resp.responseCode == 200) {
                                        LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                                        String newToken = loginResponse.getAccessToken();
                                        primaryRestClient.setOauthToken(newToken);

                                        System.out.println("new token is:");
                                        System.out.println(primaryRestClient.getOAuthToken());

                                        fiveMinResponse = primaryStationClient.getHistory(fiveMinuteRequest);
                                    }

                                }

                                if (fiveMinResponse.responseCode == 200) {
                                    fiveMinuteResults = new HistoryQueryResults((List<LiveDatapoint>) fiveMinResponse.responseObject);
                                }
                            }

                            if (hourRequest.getIds().size() > 0) {
                                hourResponse = primaryStationClient.getHistory(hourRequest);

                                if (hourResponse.responseCode == 401) {
                                    System.out.println("getting a new token. was:");
                                    System.out.println(primaryRestClient.getOAuthToken());

                                    OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                                    if (resp.responseCode == 200) {
                                        LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                                        String newToken = loginResponse.getAccessToken();
                                        primaryRestClient.setOauthToken(newToken);

                                        System.out.println("new token is:");
                                        System.out.println(primaryRestClient.getOAuthToken());

                                        hourResponse = primaryStationClient.getHistory(hourRequest);
                                    }

                                }

                                if (hourResponse.responseCode == 200) {
                                    hourResults = new HistoryQueryResults((List<LiveDatapoint>) hourResponse.responseObject);
                                }
                            }

                            ComboHistories comboHistories = new ComboHistories(fiveMinuteResults, hourResults);
                            HistoryQueryResults historyResults = new HistoryQueryResults(comboHistories);
                            
                            historyQueryResponse = new OEResponse();
                            historyQueryResponse.responseCode = 200;
                            historyQueryResponse.responseObject = historyResults;
                            
                        }


                        if (historyQueryResponse.responseCode == 200) {
                            countOfFramesProcessed++;
                            pcs.firePropertyChange(PropertyChangeNames.FrameProcessed.getName(), null, countOfFramesProcessed);
                            HistoryQueryResults history = (HistoryQueryResults) historyQueryResponse.responseObject;
                            masterResults.appendFrame(history);
                        } else {
                            pcs.firePropertyChange(PropertyChangeNames.FrameError.getName(), null, historyQueryResponse.responseCode);
                            return historyQueryResponse;
                        }
                    }

                    frameStart = frameStart.plusHours(maxHours);

                }

                OEResponse historyInFramesResp = new OEResponse();

                historyInFramesResp.responseCode = 200;
                historyInFramesResp.responseObject = masterResults;

                return historyInFramesResp;

            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        HistoryQueryResults historyResults = (HistoryQueryResults) resp.responseObject;
                        pcs.firePropertyChange(PropertyChangeNames.FramesCompleted.getName(), null, historyResults);
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
                OEResponse historyQueryResponse = primaryStationClient.getHistory(historyRequest);

                if (historyQueryResponse.responseCode == 401) {
                    System.out.println("getting a new token. was:");
                    System.out.println(primaryRestClient.getOAuthToken());

                    OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                    if (resp.responseCode == 200) {
                        LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                        String newToken = loginResponse.getAccessToken();
                        primaryRestClient.setOauthToken(newToken);

                        System.out.println("new token is:");
                        System.out.println(primaryRestClient.getOAuthToken());

                        historyQueryResponse = primaryStationClient.getHistory(historyRequest);
                    }

                }

                if (historyQueryResponse.responseCode == 200) {

                    HistoryQueryResults history = new HistoryQueryResults((List<LiveDatapoint>) historyQueryResponse.responseObject);

                    historyQueryResponse = new OEResponse();
                    historyQueryResponse.responseCode = 200;
                    historyQueryResponse.responseObject = history;

                    return historyQueryResponse;

                }

                return historyQueryResponse;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        HistoryQueryResults historyResults = (HistoryQueryResults) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.HistoryReturned.getName(), null, historyResults);
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

    public void getComboHistory(final HistoryRequest fiveMinuteRequest, final HistoryRequest hourRequest) {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                HistoryQueryResults fiveMinHistory = null;
                HistoryQueryResults hourHistory = null;

                if (fiveMinuteRequest.getIds().size() > 0) {
                    OEResponse fiveMinResponse = primaryStationClient.getHistory(fiveMinuteRequest);

                    if (fiveMinResponse.responseCode == 401) {
                        System.out.println("getting a new token. was:");
                        System.out.println(primaryRestClient.getOAuthToken());

                        OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                        if (resp.responseCode == 200) {
                            LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                            String newToken = loginResponse.getAccessToken();
                            primaryRestClient.setOauthToken(newToken);

                            System.out.println("new token is:");
                            System.out.println(primaryRestClient.getOAuthToken());

                            fiveMinResponse = primaryStationClient.getHistory(fiveMinuteRequest);
                        }

                    }

                    if (fiveMinResponse.responseCode == 200) {
                        fiveMinHistory = new HistoryQueryResults((List<LiveDatapoint>) fiveMinResponse.responseObject);
                    }
                }

                if (hourRequest.getIds().size() > 0) {
                    OEResponse hourResponse = primaryStationClient.getHistory(hourRequest);

                    if (hourResponse.responseCode == 401) {
                        System.out.println("getting a new token. was:");
                        System.out.println(primaryRestClient.getOAuthToken());

                        OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                        if (resp.responseCode == 200) {
                            LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                            String newToken = loginResponse.getAccessToken();
                            primaryRestClient.setOauthToken(newToken);

                            System.out.println("new token is:");
                            System.out.println(primaryRestClient.getOAuthToken());

                            hourResponse = primaryStationClient.getHistory(hourRequest);
                        }

                    }

                    if (hourResponse.responseCode == 200) {
                        hourHistory = new HistoryQueryResults((List<LiveDatapoint>) hourResponse.responseObject);
                    }
                }

                ComboHistories comboHistories = new ComboHistories(fiveMinHistory, hourHistory);
                HistoryQueryResults historyResults = new HistoryQueryResults(comboHistories);

                OEResponse comboResponse = new OEResponse();
                comboResponse.responseCode = 200;
                comboResponse.responseObject = historyResults;

                return comboResponse;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        HistoryQueryResults historyResults = (HistoryQueryResults) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.HistoryReturned.getName(), null, historyResults);
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

    public void getE3OSSites() {

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                SiteQuery queryRunner = new SiteQuery();
                List<E3OSStationRecord> sitesList = queryRunner.runSiteQuery();
                OEResponse results = new OEResponse();
                results.responseCode = 200;
                results.responseObject = sitesList;
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        List<E3OSStationRecord> sitesList = (List<E3OSStationRecord>) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.E3OSSitesReturned.getName(), null, sitesList);
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
            OEResponse teslaPutResponse = primaryStationClient.putHistory(tdpu);

            if (teslaPutResponse.responseCode == 422) {
                System.out.println("unprocessable entity");
                return teslaPutResponse;
            }

            if (teslaPutResponse.responseCode >= 500) {
                System.out.println("retrying...");
                teslaPutResponse = primaryStationClient.putHistory(tdpu);

            } else if (teslaPutResponse.responseCode == 401) {
                System.out.println("getting a new token. was:");
                System.out.println(primaryRestClient.getOAuthToken());

                OEResponse resp = primaryLoginClient.login(this.primaryBaseURL);

                if (resp.responseCode == 200) {
                    LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                    String newToken = loginResponse.getAccessToken();
                    primaryRestClient.setOauthToken(newToken);

                    System.out.println("new token is:");
                    System.out.println(primaryRestClient.getOAuthToken());

                    teslaPutResponse = primaryStationClient.putHistory(tdpu);
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

    public void pullFromTeslsPushToTesla(
            final EnumPrimarySecodaryClient usePrimaryOrSecondaryClientForHistoryPull,
            final DateTime pushStartTime,
            final DateTime pushEndTime,
            final List<TTTTableRow> mappedRows,
            final int maxHoursPerPush,
            final int maxPointsPerPush,
            final String stationTimeZone) {

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

                        List<TTTTableRow> pointsToPush = mappedRows.subList(startPushIndex, endIndex);
                        pullFromTeslsPushToTeslaInterval(usePrimaryOrSecondaryClientForHistoryPull, intervalStart, intervalEnd, pointsToPush, stationTimeZone);
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

    private OEResponse pullFromTeslsPushToTeslaInterval(EnumPrimarySecodaryClient usePrimaryOrSecondaryClientForHistoryPull, DateTime pushStartTime, DateTime pushEndTime, List<TTTTableRow> mappedRows, String stationTimeZone) {

        final String fiveMinuteString = "fiveMinute";
        List<String> fromIDs = new ArrayList<>();
        Map< String, String> fromIDtoIDMap = new HashMap<>();

        for (TTTTableRow mtr : mappedRows) {
            fromIDs.add(mtr.getFromID());
            fromIDtoIDMap.put(mtr.getFromID(), mtr.getToID());
        }

        try {

            HistoryRequest historyRequest = new HistoryRequest(fromIDs, pushStartTime, pushEndTime, fiveMinuteString, stationTimeZone);
            OEResponse results;

            if (usePrimaryOrSecondaryClientForHistoryPull == EnumPrimarySecodaryClient.Primary) {
                results = primaryStationClient.getHistory(historyRequest);

                if (results.responseCode == 401) {
                    System.out.println("getting a new primary token. was:");
                    System.out.println(primaryRestClient.getOAuthToken());
                    OEResponse resp = primaryLoginClient.login(primaryBaseURL);

                    if (resp.responseCode == 200) {
                        LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                        String newToken = loginResponse.getAccessToken();
                        primaryRestClient.setOauthToken(newToken);
                        System.out.println("new token is:");
                        System.out.println(primaryRestClient.getOAuthToken());
                        results = primaryStationClient.getHistory(historyRequest);
                    }
                }
            } else {

                results = secondaryStationClient.getHistory(historyRequest);

                if (results.responseCode == 401) {
                    System.out.println("getting a new primary token. was:");
                    System.out.println(secondaryRestClient.getOAuthToken());
                    OEResponse resp = secondaryLoginClient.login(secondaryBaseURL);

                    if (resp.responseCode == 200) {
                        LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                        String newToken = loginResponse.getAccessToken();
                        secondaryRestClient.setOauthToken(newToken);
                        System.out.println("new secodary token is:");
                        System.out.println(secondaryRestClient.getOAuthToken());
                        results = secondaryStationClient.getHistory(historyRequest);
                    }
                }

            }

            if (results.responseCode != 200) {
                return results;
            }

            List<LiveDatapoint> history = (List<LiveDatapoint>) results.responseObject;

            TTTDataPointUpsertRequest tdpu = new TTTDataPointUpsertRequest(history, fromIDtoIDMap);
            OEResponse teslaPutResponse = primaryStationClient.putHistory(tdpu);

            if (teslaPutResponse.responseCode == 422) {
                System.out.println("unprocessable entity");
                return teslaPutResponse;
            }

            if (teslaPutResponse.responseCode >= 500) {
                System.out.println("retrying...");
                teslaPutResponse = primaryStationClient.putHistory(tdpu);

            } else if (teslaPutResponse.responseCode == 401) {
                System.out.println("getting a new primary token. was:");
                System.out.println(primaryRestClient.getOAuthToken());

                //OEResponse resp = loginClient.login(this.baseURL);
                OEResponse resp = primaryLoginClient.login(secondaryBaseURL);

                if (resp.responseCode == 200) {
                    LoginResponse loginResponse = (LoginResponse) resp.responseObject;
                    String newToken = loginResponse.getAccessToken();
                    primaryRestClient.setOauthToken(newToken);

                    System.out.println("new token is:");
                    System.out.println(primaryRestClient.getOAuthToken());

                    teslaPutResponse = primaryStationClient.putHistory(tdpu);

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

    // === E3OSLive =====
    public void e3osLiveAuthenticate(){

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                OEResponse results = e3osClient.authenticate();
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        E3osAuthResponse e3osAuthResp = (E3osAuthResponse) resp.responseObject;
                        
                        pcs.firePropertyChange(PropertyChangeNames.E3OSLiveAuthenticated.getName(), null, e3osAuthResp);
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
    
    
    public void getE3OSSiteList(){

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                OEResponse results = e3osClient.getE3OSSiteList();
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        List<E3OSSite> siteList = (List<E3OSSite>) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.E3OSSiteListReturned.getName(), null, siteList);
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
    
   
    

    public void e3osLiveDataRequest( final LiveDataRequest ldr ){

        SwingWorker worker = new SwingWorker< OEResponse, Void>() {

            @Override
            public OEResponse doInBackground() throws IOException {

                OEResponse results = e3osClient.requestLiveData(ldr);
                return results;
            }

            @Override
            public void done() {
                try {
                    OEResponse resp = get();

                    if (resp.responseCode == 200) {
                        List<LiveDataResponse> liveDataResponse = (List<LiveDataResponse>) resp.responseObject;

                        pcs.firePropertyChange(PropertyChangeNames.E3OSLiveDataReturned.getName(), null, liveDataResponse);
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
