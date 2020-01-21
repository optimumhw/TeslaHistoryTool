
package view.LiveDataCompareFrame.LiveDataTable;

import model.DataPoints.CoreDatapoint;
import model.E3OS.E3OSLiveData.E3OSDataPoint;


public class LiveDataMappingTableRow {

    private EnumLiveDataMapStatus mapStatus;
    private boolean pollFlag;
    
    private String coreName;
    private String coreType;
    private String coreID;
    private Object coreValue;

    private String e3osShortName;
    private int e3osID;
    private Object e3osValue;

    public LiveDataMappingTableRow() {

        mapStatus = EnumLiveDataMapStatus.NoInfo;
        pollFlag = false;
        
        
        coreName = "?";
        coreType = "?";
        coreID = "?";
        coreValue = null;
        
        e3osShortName = "?";
        e3osID = 0;
        e3osValue = null;

    }
    
    public LiveDataMappingTableRow(CoreDatapoint pt){
        mapStatus = EnumLiveDataMapStatus.NoE3OSInfo;
        pollFlag = false;
        
        coreName = pt.getShortName();
        coreType = pt.getPointType();
        coreID = pt.getId();
        coreValue = null;
        
        e3osShortName = "?";
        e3osID = 0;
        e3osValue = null;
        

    }
    
        public LiveDataMappingTableRow(E3OSDataPoint e3osPoint){
        mapStatus = EnumLiveDataMapStatus.NoCoreInfo;
        
        e3osShortName = e3osPoint.getName();
        e3osID = e3osPoint.getId();
        
        
        e3osValue = null;
        
        coreName = "?";
        coreType = "?";
        coreID = "?";
        coreValue = null;
    }
    
    public void setMapStatus(EnumLiveDataMapStatus mapStatus) {
        this.mapStatus = mapStatus;
    }

    public EnumLiveDataMapStatus getMapStatus() {
        return mapStatus;
    }
    
    public void setPollFlag(boolean pollFlag) {
        this.pollFlag = pollFlag;
    }

    public boolean getPollFlag() {
        return pollFlag;
    }
    
    
    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }

    public String getCoreName() {
        return coreName;
    }
    
    public void setCoreType(String coreType) {
        this.coreType = coreType;
    }

    public String getCoreType() {
        return coreType;
    }

    public void setCoreID(String coreId) {
        this.coreID = coreId;
    }

    public String getCoreID() {
        return coreID;
    }
    
    public void setCoreValue(Object coreValue) {
        this.coreValue = coreValue;
    }

    public Object getCoreValue() {
        return coreValue;
    }
    
    
    
   
    public void setE3osName(String e3osName) {
        this.e3osShortName = e3osName;
    }

    public String getE3osName() {
        return e3osShortName;
    }

    public void setE3osID(int e3osID) {
        this.e3osID = e3osID;
    }

    public int getE3osID() {
        return e3osID;
    }
    
    public void setE3osValue(Object e3osValue) {
        this.e3osValue = e3osValue;
    }

    public Object getE3osValue() {
        return e3osValue;
    }



}