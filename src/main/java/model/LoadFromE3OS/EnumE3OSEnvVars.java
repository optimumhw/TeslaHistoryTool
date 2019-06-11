package model.LoadFromE3OS;

import java.util.ArrayList;
import java.util.List;

public enum EnumE3OSEnvVars {

    E3OS_SQL_HOST("E3OS_SQL_HOST"),
    E3OS_SQL_USER("E3OS_SQL_USER"),
    E3OS_SQL_PASSWORD("E3OS_SQL_PASSWORD");

    private final String propertyName;

    EnumE3OSEnvVars(String host) {
        this.propertyName = host;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    static public List<String> getEnumNames() {
        List<String> names = new ArrayList<>();
        for (EnumE3OSEnvVars u : EnumE3OSEnvVars.values()) {
            names.add(u.name());
        }
        return names;
    }

    static public EnumE3OSEnvVars getEnumFromString(String nameAsString) {
        for (EnumE3OSEnvVars u : EnumE3OSEnvVars.values()) {
            if (u.name().contains(nameAsString)) {
                return u;
            }
        }
        return null;
    }
}
