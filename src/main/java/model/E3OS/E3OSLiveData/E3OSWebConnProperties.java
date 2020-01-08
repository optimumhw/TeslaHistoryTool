
package model.E3OS.E3OSLiveData;

import model.E3OS.LoadFromE3OS.EnumE3OSEnvVars;


public class E3OSWebConnProperties {

    private final String host;
    private final String user;
    private final String password;

    public E3OSWebConnProperties() {
        host = System.getenv(EnumE3OSEnvVars.E3OS_WEB_HOST.getPropertyName());
        user = System.getenv(EnumE3OSEnvVars.E3OS_WEB_USER.getPropertyName());
        password = System.getenv(EnumE3OSEnvVars.E3OS_WEB_PASSWORD.getPropertyName());
    }

    public String getHost() {
        return this.host;
    }

    public String getUsername() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

}
