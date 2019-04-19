
package model.RestClient;

import org.joda.time.DateTime;


public class RRObj {

    private final DateTime timestamp;
    private final EnumCallType callType;
    private final EnumRequestType reqType;
    private int status;
    private final String url;
    private final String payload;
    private final String token;

    public RRObj(DateTime timestamp, EnumCallType callType, EnumRequestType reqType, int status, String url, String payload, String token) {
        this.timestamp = timestamp;
        this.callType = callType;
        this.reqType = reqType;
        this.status = status;
        this.url = url;
        this.payload = payload;
        this.token = token;
    }

    public RRObj(DateTime timestamp, EnumCallType callType, EnumRequestType reqType, String url, String token) {
        this.timestamp = timestamp;
        this.callType = callType;
        this.reqType = reqType;
        this.url = url;
        this.payload = "";
        this.token = token;
    }

    public DateTime getTimestamp() {
        return this.timestamp;
    }

    public EnumCallType getCallType() {
        return this.callType;
    }

    public EnumRequestType getReqType() {
        return this.reqType;
    }

    public int getStatus() {
        return this.status;
    }

    public String getURL() {
        return this.url;
    }

    public String getPayload() {
        return this.payload;
    }
    
    public String getToken(){
        return this.token;
    }

    public String getCurlStatement() {
        String curl = "";

        switch (reqType) {
            case GET:
                curl = String.format("curl -X %s \"%s\" -H \"Authorization: Bearer %s\"",
                        reqType.name(), url, token);
                break;

            case POST:
                curl = String.format("curl -X %s -d '%s' \"%s\" -H \"Accept: application/json\" -H \"Content-Type: application/json\" -H \"Authorization: Bearer %s\"",
                        reqType.name(), this.payload, url, token);
                break;
                
             case LOGIN:
                curl = String.format("curl -X %s -d '%s' \"%s\" -H \"Accept: application/json\" -H \"Content-Type: application/json\" ",
                        EnumRequestType.POST, this.payload, url );
                break;

            case PUT:
                curl = String.format("curl -X %s -d '%s' \"%s\" -H \"Accept: application/json\" -H \"Content-Type: application/json\" -H \"Authorization: Bearer %s\"",
                        reqType.name(), this.payload, url, token);
                break;
            case DELETE:

                curl = String.format("curl -X %s \"%s\" -H \"Authorization: Bearer %s\"",
                        reqType.name(), url, token);
                break;

        }

        return curl;
    }

}