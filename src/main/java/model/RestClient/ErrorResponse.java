package model.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {

    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;

    public String getError() {
        return this.error;
    }

    public String getMessage() {
        return this.message;
    }

}
