
package model;

public class TeslaUsersInfo {

    private final String tesla_user;
    private final String tesla_password;

    public TeslaUsersInfo() {
        tesla_user = System.getenv(EnumConnProperties.TESLA_USER.getPropertyName());
        tesla_password = System.getenv(EnumConnProperties.TESLA_PASSWORD.getPropertyName());
    }

    public String getUserName() {
        return this.tesla_user;
    }

    public String getPassword() {
        return this.tesla_password;
    }

}