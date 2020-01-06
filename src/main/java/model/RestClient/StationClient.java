package model.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import model.DataPoints.StationInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import model.DataPoints.HistoryRequest;
import model.DataPoints.LiveDatapoint;
import model.DatapointList.DatapointListItem;
import model.EnumBaseURLs;
import model.E3OS.LoadFromE3OS.TeslaDataPointUpsertRequest;
import model.TTT.TTTDataPointUpsertRequest;

public class StationClient {

    private final RestClientCommon restClient;
    private EnumBaseURLs baseURL;

    public StationClient(EnumBaseURLs baseURL, RestClientCommon restClient) {
        this.restClient = restClient;
        this.baseURL = baseURL;
    }

    public void setServiceURLAndToken(EnumBaseURLs serviceURL, String accessToken) {
        this.baseURL = serviceURL;
        restClient.setOauthToken(accessToken);
    }

    public OEResponse getStations() throws IOException {

        String url = baseURL.getURL() + "/stations";

        OEResponse resObj = restClient.getResponse(url, true);

        if (resObj.responseCode == 200) {
            ObjectMapper mapper = new ObjectMapper();
            resObj.responseObject = mapper.readValue((String) resObj.responseObject, new TypeReference<List<StationInfo>>() {
            });
        }

        return resObj;
    }

    public OEResponse getStationInfo(String stationID) throws IOException {

        String url = baseURL.getURL() + "/stations/" + stationID;
        OEResponse resObj = restClient.getResponse(url, true);

        if (resObj.responseCode == 200) {
            ObjectMapper mapper = new ObjectMapper();
            resObj.responseObject = mapper.readValue((String) resObj.responseObject, StationInfo.class);
        }

        return resObj;
    }

    public OEResponse getDatapoints(String stationID) throws IOException {

        String url = baseURL.getURL() + "/stations/" + stationID + "/data-points";
        OEResponse resObj = restClient.getResponse(url, true);

        if (resObj.responseCode == 200) {
            ObjectMapper mapper = new ObjectMapper();
            resObj.responseObject = mapper.readValue((String) resObj.responseObject, new TypeReference<List<DatapointListItem>>() {
            });
        }

        return resObj;
    }

    public OEResponse getSubscribed(String stationID) throws IOException {

        String url = baseURL.getURL() + "/stations/" + stationID + "/data-points/subscribed";
        OEResponse resObj = restClient.getResponse(url, true);

        if (resObj.responseCode == 200) {
            ObjectMapper mapper = new ObjectMapper();
            resObj.responseObject = mapper.readValue((String) resObj.responseObject, new TypeReference<List<String>>() {
            });
        }

        return resObj;
    }

    public OEResponse getLiveData(List<String> dataPointIDs) throws IOException {

        String url = baseURL.getURL() + "/live-data/query";

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(dataPointIDs);

        payload = "{ \"ids\" : " + payload + "}";

        OEResponse resObj = restClient.doPostAndGetBody(url, payload, true);

        if (resObj.responseCode == 200) {
            mapper = new ObjectMapper();
            resObj.responseObject = mapper.readValue((String) resObj.responseObject, new TypeReference<List<LiveDatapoint>>() {
            });
        }

        return resObj;
    }

    public OEResponse getHistory(HistoryRequest historyRequest) throws IOException {

        String url = baseURL.getURL() + "/data/query";

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(historyRequest);

        OEResponse resObj = restClient.doPostAndGetBody(url, payload, true);

        if (resObj.responseCode == 200) {
            mapper = new ObjectMapper();
            resObj.responseObject = mapper.readValue((String) resObj.responseObject, new TypeReference<List<LiveDatapoint>>() {
            });
        }

        return resObj;
    }

    public OEResponse putHistory(TeslaDataPointUpsertRequest dur) throws JsonProcessingException, IOException {

        String url = baseURL.getURL() + "/data/upsert";

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(dur.getListOfPoints());

        OEResponse resObj = restClient.doPostAndGetBody(url, payload, true);

        return resObj;

    }
    
        public OEResponse putHistory(TTTDataPointUpsertRequest dur) throws JsonProcessingException, IOException {

        String url = baseURL.getURL() + "/data/upsert";

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(dur.getListOfPoints());

        OEResponse resObj = restClient.doPostAndGetBody(url, payload, true);

        return resObj;

    }

}
