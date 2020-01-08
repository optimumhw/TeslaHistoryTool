package model.E3OS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import model.E3OS.CustTreeList.E3OSSite;
import model.E3OS.E3OSLiveData.E3OSWebConnProperties;
import model.E3OS.E3OSLiveData.E3osAuthResponse;
import model.E3OS.E3OSLiveData.LiveDataRequest;
import model.E3OS.E3OSLiveData.LiveDataResponse;
import model.RestClient.EnumCallType;
import model.RestClient.EnumRequestType;
import model.RestClient.OEResponse;
import model.RestClient.RRObj;
import model.RestClient.RequestsResponses;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class E3OSClient {

    private final String badCredentials = "incorrect credentials";

    static Logger logger = LoggerFactory.getLogger(E3OSClient.class.getName());

    private RequestsResponses rrs;
    
    private final E3OSWebConnProperties connProps;

    public E3OSClient(RequestsResponses rrs) {
        this.rrs = rrs;
        
        this.connProps = new E3OSWebConnProperties();
    }

    public OEResponse authenticate() {

        String url = connProps.getHost() + "/services/Auth.ashx?cmd=login";

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("content-type", "application/json"));

        Map<String, String> postBody = new HashMap<>();
        postBody.put("username", connProps.getUsername());
        postBody.put("password", connProps.getPassword());
        postBody.put("access_token", "12341234-1234-1234-123412341234");

        ObjectMapper mapper = new ObjectMapper();
        String payload;

        OEResponse resp = new OEResponse();
        try {
            payload = mapper.writeValueAsString(postBody);
            resp = doPostAndGetBody(url, nvps, payload);

            if (resp.responseCode == 200) {
                resp.responseObject = mapper.readValue((String) resp.responseObject, E3osAuthResponse.class);
            }

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(E3OSClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resp;

    }

    public OEResponse getE3OSSiteList() {

        String url = connProps.getHost() + "/services/AdminService.ashx?cmd=site-list";

        OEResponse resp = new OEResponse();

        try {

            ObjectMapper mapper = new ObjectMapper();

            String e3osAccessToken = "12341234-1234-1234-123412341234";

            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("content-type", "application/json"));
            nvps.add(new BasicNameValuePair("oauth", "Bearer " + e3osAccessToken));

            String payload = "";

            resp = doPostAndGetBody(url, nvps, payload);

            if (resp.responseCode == 200 ) {
               resp.responseObject = mapper.readValue((String) resp.responseObject, new TypeReference<List<E3OSSite>>() {});
            }

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(E3OSClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resp;

    }

    public OEResponse requestLiveData(LiveDataRequest ldr) {

        String url = connProps.getHost() + "/services/LiveData.ashx?cmd=command1&request=";

        OEResponse resp = new OEResponse();

        try {

            ObjectMapper mapper = new ObjectMapper();
            String reqParam = mapper.writeValueAsString(ldr);

            String e3osAccessToken = "12341234-1234-1234-123412341234";

            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("content-type", "application/json"));
            nvps.add(new BasicNameValuePair("oauth", "Bearer " + e3osAccessToken));

            String payload = "";

            resp = doPostAndGetBody(url, nvps, payload);

            if (resp.responseCode == 200) {
                resp.responseObject = mapper.readValue((String) resp.responseObject, LiveDataResponse.class);
            }

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(E3OSClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resp;

    }

    private OEResponse doPostAndGetBody(String url, List<NameValuePair> nvps, String payload) throws UnsupportedEncodingException, IOException {

        String responseString = "";

        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        HttpPost postRequest = new HttpPost(url);
        OEResponse resp = new OEResponse();

        try {

            for (NameValuePair h : nvps) {
                postRequest.addHeader(h.getName(), h.getValue());
            }

            postRequest.setEntity(new StringEntity(payload));

            rrs.addRequest(new RRObj(DateTime.now(), EnumCallType.REQUEST, EnumRequestType.POST, 0, url, payload, "aaa"));

            response = httpClient.execute(postRequest, context);
            resp.responseCode = response.getStatusLine().getStatusCode();

            resp.responseObject = "no content";
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream stream = entity.getContent();

                BufferedReader br = new BufferedReader(new InputStreamReader((stream)));
                String output;
                while ((output = br.readLine()) != null) {
                    responseString += output;
                }
                if (resp.responseCode == 401) {
                    responseString = badCredentials;
                }
                resp.responseObject = responseString;
            }

        } catch (Exception ex) {
            resp.responseObject = ex.getMessage();
            resp.responseCode = 999;
            String msg = Integer.toString(resp.responseCode) + ": " + ex.getMessage();
            rrs.addRequest(new RRObj(DateTime.now(), EnumCallType.RESPONSE, EnumRequestType.POST, resp.responseCode, url, msg, "aaa"));
        } finally {
            if (response != null) {
                rrs.addRequest(new RRObj(DateTime.now(), EnumCallType.RESPONSE, EnumRequestType.POST, resp.responseCode, url, responseString, "aaa"));
                response.close();
            }
            httpClient.close();
        }
        return resp;
    }

}
