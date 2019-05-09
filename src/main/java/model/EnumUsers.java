package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum EnumUsers {
    DevOps("devops@optimumenergyco.com", "TESLA_DEV_USER_PASSWORD"),
    ProdUser("devops@optimumenergyco.com", "TESLA_PROD_USER_PASSWORD");

    private final String email;
    private String password;

    EnumUsers(String email, String envVarName) {
        this.email = email;
        this.password = System.getenv(envVarName);
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }
    
    static public List<String> getUsernames(){
        List<String> userNames = new ArrayList<>();
        for( EnumUsers user : EnumUsers.values()){
          userNames.add(user.name() );
        }
        return userNames;
    }
    
    static public EnumUsers getUserFromName( String userName ){
        for( EnumUsers user : EnumUsers.values() ){
            if( user.name().compareTo(userName) == 0 ){
                return user; 
            }
        }
        return null;
    }

}
