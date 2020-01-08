package model.E3OS.E3OSLiveData;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.joda.time.DateTime;

public class E3osAuthResponse {

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("token")
    private String token;

    @JsonProperty("expires")
    private String expires;

    @JsonProperty("permissions")
    private List<E3osPermission> permissions;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public String getExpires() {
        return expires;
    }

    public List<E3osPermission> getPermissions() {
        return permissions;
    }

}
