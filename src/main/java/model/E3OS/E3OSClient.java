package model.E3OS;

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
import model.E3OS.LoadFromE3OS.E3OSConnProperties;
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

    public E3OSClient(RequestsResponses rrs) {
        this.rrs = rrs;
    }

    public void authenticate() {

        String url = "https://e3os.optimumenergyco.com/services/Auth.ashx?cmd=login";

        E3OSConnProperties connProps = new E3OSConnProperties();
        
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("content-type", "application/json"));

        Map<String, String> postBody = new HashMap<>();
        postBody.put("username", connProps.getUsername());
        postBody.put("password", connProps.getPassword());
        postBody.put("access_token", "12341234-1234-1234-1234-123412341234");

        ObjectMapper mapper = new ObjectMapper();
        String payload;
        try {
            payload = mapper.writeValueAsString(postBody);
            OEResponse resp = doPostAndGetBody(url, nvps, payload);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(E3OSClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    /*
    
    protected OEResponse requestLiveData(){
        
        //https://e3os.optimumenergyco.com/services/LiveData.ashx?cmd=command1&request=
        
        //{"dataSourceType":"LiveData","minDateTime":637115884007570000,"dataPoints":[120631]}
        
        long ticks = DateTime.now().getMillis() * 1000L;
        
    }
    */
    

    protected OEResponse doPostAndGetBody(String url, List<NameValuePair> nvps, String payload) throws UnsupportedEncodingException, IOException {

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
