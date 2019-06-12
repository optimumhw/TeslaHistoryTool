package model.RestClient;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Auth.LoginResponse;
import model.EnumBaseURLs;
import model.TeslaUsersInfo;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;


public class LoginClient {
    
    private final String badCredentials = "incorrect credentials";

    private final RestClientCommon restClient;

    public LoginClient(RestClientCommon restClient) {
        this.restClient = restClient;
    }
    
    
    public OEResponse login(EnumBaseURLs serviceURL) throws IOException {

        TeslaUsersInfo user = new TeslaUsersInfo(serviceURL);
        String url = serviceURL.getURL() + "/oauth/token";

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("content-type", "application/json"));

        Map<String, String> postBody = new HashMap<>();
        postBody.put("grant_type", "password");
        postBody.put("email", user.getUserName());
        postBody.put("password", user.getPassword());

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(postBody);
        OEResponse resp = restClient.doPostAndGetBody(url, payload, false);
        if (resp.responseCode == 200) {
            resp.responseObject = mapper.readValue((String) resp.responseObject, LoginResponse.class);
            return resp;
        }
        
        OEResponse resp2 = new OEResponse();
        resp2.responseCode = resp.responseCode;
        resp2.responseObject = badCredentials;
        return resp2;
    }

}
