package model.LoadFromE3OS;

public class E3OSConnProperties {

    private final String host;
    private final String user;
    private final String password;

    public E3OSConnProperties() {
        host = System.getenv(EnumE3OSEnvVars.E3OS_SQL_HOST.getPropertyName());
        user = System.getenv(EnumE3OSEnvVars.E3OS_SQL_USER.getPropertyName());
        password = System.getenv(EnumE3OSEnvVars.E3OS_SQL_PASSWORD.getPropertyName());
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
