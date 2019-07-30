package model;

import java.util.ArrayList;
import java.util.List;

public enum EnumConnProperties {

    TESLA_USER("TESLA_USER"),
    TESLA_PASSWORD("TESLA_PASSWORD"),
    TESLA_DEV_USER("TESLA_DEV_USER"),
    TESLA_DEV_PASSWORD("TESLA_DEV_PASSWORD"),
    TESLA_LOCAL_USER("TESLA_LOCAL_USER"),
    TESLA_LOCAL_PASSWORD("TESLA_LOCAL_PASSWORD");

    private final String propertyName;

    EnumConnProperties(String host) {
        this.propertyName = host;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    static public List<String> getEnumNames() {
        List<String> names = new ArrayList<>();
        for (EnumConnProperties u : EnumConnProperties.values()) {
            names.add(u.name());
        }
        return names;
    }

    static public EnumConnProperties getEnumFromString(String nameAsString) {
        for (EnumConnProperties u : EnumConnProperties.values()) {
            if (u.name().contains(nameAsString)) {
                return u;
            }
        }
        return null;
    }
}
