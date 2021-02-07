
package model;

import java.util.ArrayList;
import java.util.List;


public enum EnumBaseURLs {
    LocalHost("http://127.0.0.1:4000/v1"),
    Ninja("https://api.optimumenergy.ninja/v1"),
    Prod("https://api.optimumenergyco.com/v1");

    private String url;

    EnumBaseURLs( String url )
    {
        this.url = url;
        
    }
    
    public String getURL(){
        return this.url;
    }
    
    static public List<String> getURLs(){
        List<String> urls = new ArrayList<>();
        for( EnumBaseURLs loc : EnumBaseURLs.values()){
          urls.add(loc.getURL() );
        }
        return urls;
    }
    
    static public EnumBaseURLs getHostFromName( String url ){
        for( EnumBaseURLs enumBaseURL : EnumBaseURLs.values() ){
            if( enumBaseURL.getURL().compareTo(url) == 0 ){
                return enumBaseURL; 
            }
        }
        return null;
    }
    
    
}