package model;

import java.util.ArrayList;
import java.util.List;

public enum EnumUsers {
    DevOps("devops@optimumenergyco.com", "TESLA_DEV_USER_PASSWORD"),
    ProdUser("devops@optimumenergyco.com", "TESLA_PROD_USER_PASSWORD");

    private final String email;
    private final String password;

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
