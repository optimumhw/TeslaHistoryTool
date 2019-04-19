package model;

import java.util.ArrayList;
import java.util.List;

public enum EnumUsers {
    DevOps("devops@optimumenergyco.com", "password"),
    ProdUser("devops@optimumenergyco.com", "jm#@5wM*SHO4Pq$^8dQizjc1vkLkZonQ7&s4f00kE1Gf1@ewR6w^^XbDnwkAVIbd");

    private final String email;
    private final String password;

    EnumUsers(String email, String password) {
        this.email = email;
        this.password = password;
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
