package model.PushFromE3OS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.DataPoints.StationInfo;
import model.DatapointList.DatapointListItem;
import model.E3OS.LoadFromE3OS.DataPointFromSql;
import model.E3OS.LoadFromE3OS.EnumMapStatus;
import model.E3OS.LoadFromE3OS.EnumOverrideType;
import model.E3OS.LoadFromE3OS.MappingTableRow;
import view.HistoryFrame.PushE3OSDataFrame.MappingTable.EnumOverrideSites;

public class PushDataTable {

    ArrayList<MappingTableRow> mappingTable;

    public PushDataTable(StationInfo stationInfo, List<DatapointListItem> datapointsList, List<DataPointFromSql> e3osPoints) {

        Map<String, DataPointFromSql> e3OSOverrideToRecordMap = getE3OSOverrideToRecordMap(e3osPoints);

        Map<String, String> regexOverridesMap = getCoreToE3OSNameRegExOverridesMap(stationInfo.getName());
        Map<String, String> coreToE3OSNameOverridesMap = getCoreToE3OSNameOverridesMap();

        validateOverrides(e3osPoints);

        //add all the core points to the table, set status to "no e3os info"
        mappingTable = new ArrayList<>();

        for (DatapointListItem pt : datapointsList) {
            mappingTable.add(new MappingTableRow(pt));
        }

        //go through all the rows and try to find a matching e3os point
        for (MappingTableRow mappingTableRow : mappingTable) {
            boolean foundIt = false;
            for (DataPointFromSql e3osPoint : e3osPoints) {
                //match on short name
                if (mappingTableRow.getTeslaName().equalsIgnoreCase(e3osPoint.getDatapointName())) {
                    mappingTableRow.setMapStatus(EnumMapStatus.Mapped);
                    mappingTableRow.setE3osName(e3osPoint.getDatapointName());
                    mappingTableRow.setXid(e3osPoint);
                    foundIt = true;
                }

            }

            //if didn't find it, first check new overrides
            if (!foundIt) {

                String coreName = mappingTableRow.getTeslaName();             
                
                if (hasMatchingOverrideKey(regexOverridesMap, coreName)) {
                    
                    String coreKey = getMatchingOverrideKey(regexOverridesMap, coreName);            
                    String[] pieces = coreName.split(coreKey, 2);                  
                    String trailingPiece = pieces[1];
                   
                    String e3osPattern = regexOverridesMap.get(coreKey);
                    
                    String e3osNameToMatch = e3osPattern + trailingPiece;

                    // if there is a key for this point, look through e3os points for a match
                    for (DataPointFromSql e3osPoint : e3osPoints) {

                        String e3osPointName = e3osPoint.getDatapointName();
                        
                        //match on formed name
                        if (e3osPointName.equalsIgnoreCase(e3osNameToMatch)) {
                            mappingTableRow.setMapStatus(EnumMapStatus.Mapped);
                            mappingTableRow.setE3osName(e3osPoint.getDatapointName());
                            mappingTableRow.setXid(e3osPoint);
                            mappingTableRow.setOverrideType(EnumOverrideType.StationRegEx);
                            foundIt = true;
                        }

                    }
                }
            }

            //if still didn't find it, check the other overrides
            if (!foundIt) {
                String coreName = mappingTableRow.getTeslaName();
                if (coreToE3OSNameOverridesMap.containsKey(coreName)) {
                    String e3osName = coreToE3OSNameOverridesMap.get(coreName);
                    if (e3OSOverrideToRecordMap.containsKey(e3osName)) {
                        DataPointFromSql e3osPoint = e3OSOverrideToRecordMap.get(e3osName);
                        mappingTableRow.setMapStatus(EnumMapStatus.Mapped);
                        mappingTableRow.setE3osName(e3osPoint.getDatapointName());
                        mappingTableRow.setXid(e3osPoint);
                        mappingTableRow.setOverrideType(EnumOverrideType.OtherOverride);

                    } else {
                        System.out.println("override error - e3os name: " + e3osName + " was not found for core name: " + coreName);
                    }

                } else {
                    System.out.println("core name: " + coreName + " no match not found");
                }
            }
        }

        //check to make sure all the e3os points are in the table
        for (DataPointFromSql e3osPoint : e3osPoints) {
            boolean foundIt = false;
            for (MappingTableRow mappingTableRow : mappingTable) {
                if (mappingTableRow.getE3osName().equalsIgnoreCase(e3osPoint.getDatapointName())) {
                    foundIt = true;
                    break;
                }
            }
            if (!foundIt) {
                mappingTable.add(new MappingTableRow(e3osPoint));
            }
        }

    }

    private Map<String, DataPointFromSql> getE3OSOverrideToRecordMap(List<DataPointFromSql> e3osPoints) {

        Map<String, String> e3osOverrides = getE3OSToCoreNameOverridesMap();

        Map<String, DataPointFromSql> map = new HashMap<>();
        for (DataPointFromSql e3osPoint : e3osPoints) {

            if (e3osPoint.getDatapointName().equalsIgnoreCase("CH2COM1SPD")) {
                System.out.println("found it");
            }

            if (e3osOverrides.containsKey(e3osPoint.dpName)) {
                map.put(e3osPoint.dpName, e3osPoint);
            }

        }
        return map;
    }

    public boolean validateOverrides(List<DataPointFromSql> e3osPoints) {

        System.out.println("validating overrides...");
        boolean isValid = true;
        for (String e3osShortName : getE3OSToCoreNameOverridesMap().keySet()) {
            boolean foundIt = false;
            for (DataPointFromSql e3osPoint : e3osPoints) {
                if (e3osPoint.dpName.equalsIgnoreCase(e3osShortName)) {
                    foundIt = true;
                }
            }

            if (!foundIt) {
                System.out.println("could not find :" + e3osShortName);
                isValid = false;
            }
        }

        System.out.println("done.");
        return isValid;
    }

    private Map<String, String> getCoreToE3OSNameOverridesMap() {

        // CORE -- E3OS
        Map<String, String> map = new HashMap<>();
        for (String key : getE3OSToCoreNameOverridesMap().keySet()) {
            map.put(getE3OSToCoreNameOverridesMap().get(key), key);
        }

        return map;
    }

    private Map<String, String> getE3OSToCoreNameOverridesMap() {

        Map<String, String> map = new HashMap<>();

        // E3OS -- CORE
        map.put("CDWP1SPD_Alarm", "CDWP1SPDNotOptimized");
        map.put("CDWP2SPD_Alarm", "CDWP2SPDNotOptimized");
        map.put("CDWP3SPD_Alarm", "CDWP3SPDNotOptimized");
        map.put("CT4SPD_Alarm", "CT4SPDNotOptimized");
        map.put("CT3SPD_Alarm", "CT3SPDNotOptimized");
        map.put("CT2SPD_Alarm", "CT2SPDNotOptimized");
        map.put("CT1SPD_Alarm", "CT1SPDNotOptimized");
        map.put("PCHWP3SPD_Alarm", "PCHWP3SPDNotOptimized");
        map.put("PCHWP2SPD_Alarm", "PCHWP2SPDNotOptimized");
        map.put("PCHWP1SPD_Alarm", "PCHWP1SPDNotOptimized");
        map.put("commfail", "BASCommunicationFailure");
        map.put("LOOPREQ", "EDGEMODE");
        map.put("OECREADY", "EDGEREADY");
        map.put("CH1_CHWSTSP_Alarm", "CH1CHWSTSPNotOptimized");
        map.put("CH2_CHWSTSP_Alarm", "CH2CHWSTSPNotOptimized");

        map.put("CHWP3SPD", "PCHWP3SPD");
        map.put("CHWP3SS", "PCHWP3SS");
        map.put("CHWP3S", "PCHWP3S");
        map.put("CHWP3kW", "PCHWP3kW");
        map.put("CHWP3Failed", "PCHWP3Failed");
        map.put("CHWP2SPD", "PCHWP2SPD");
        map.put("CHWP2SS", "PCHWP2SS");
        map.put("CHWP2S", "PCHWP2S");
        map.put("CHWP2kW", "PCHWP2kW");
        map.put("CHWP2Failed", "PCHWP2Failed");
        map.put("CHWP1SPD", "PCHWP1SPD");
        map.put("CHWP1SS", "PCHWP1SS");
        map.put("CHWP1S", "PCHWP1S");
        map.put("CHWP1kW", "PCHWP1kW");
        map.put("CHWP1Failed", "PCHWP1Failed");
        map.put("CH3COM1SPD", "CH3SPD");
        map.put("CH3COM1IGV", "CH3IGV");
        map.put("CH2COM1SPD", "CH2SPD");
        map.put("CH2COM1IGV", "CH2IGV");
        map.put("CH1COM1SPD", "CH1SPD");
        map.put("CH1COM1IGV", "CH1IGV");

        return map;

    }

    public ArrayList<MappingTableRow> getMappingTable() {
        return this.mappingTable;
    }

    //=================================
    private String getMatchingOverrideKey(Map<String, String> regexOverridesMap, String corePointName) {
        for (String key : regexOverridesMap.keySet()) {
            if (matchesPointName(corePointName, key)) {
                return key;
            }
        }

        return null;
    }

    private boolean matchesPointName(String pointName, String prefix) {

        Pattern r = Pattern.compile(prefix);
        Matcher m = r.matcher(pointName);
        if (m.find()) {
            return true;
        }

        return false;
    }

    private boolean hasMatchingOverrideKey(Map<String, String> regexOverridesMap, String coreName) {
        for (String key : regexOverridesMap.keySet()) {
            if (matchesPointName(coreName, key)) {
                return true;
            }
        }

        return false;
    }

    private Map<String, String> getCoreToE3OSNameRegExOverridesMap(String siteName) {

        // CORE --> E3OS
        Map<String, String> map = new HashMap<>();
        Map<String, String> e3osToCoreOverridesMap = getRegexOverridesMap(siteName);
        for (String key : e3osToCoreOverridesMap.keySet()) {
            map.put(e3osToCoreOverridesMap.get(key), key);
        }

        return map;
    }

    public Map<String, String> getRegexOverridesMap(String siteName) {

        Map<String, String> map = new HashMap<>();
        
        EnumOverrideSites enumOverrideSite = EnumOverrideSites.getEnumFromName(siteName);

        switch (enumOverrideSite) {

            // e3os --> core
            case SUZHOU:
                map.put("CH1", "CH1");
                map.put("CH2", "CH2");
                map.put("CH3", "CH3");
                map.put("CH4", "CH4");
                map.put("CH5", "CH5");
                map.put("CH6", "CH6");
                map.put("PCHWP1", "PCHWP1");
                map.put("PCHWP2", "PCHWP2");
                map.put("PCHWP3", "PCHWP3");
                map.put("PCHWP4", "PCHWP4");
                map.put("PCHWP5", "PCHWP5");
                map.put("PCHWP6", "PCHWP6");
                map.put("PCHWP7", "PCHWP7");
                map.put("CDWP1", "CDWP1");
                map.put("CDWP2", "CDWP2");
                map.put("CDWP3", "CDWP3");
                map.put("CDWP4", "CDWP4");
                map.put("CDWP5", "CDWP5");
                map.put("CDWP6", "CDWP6");
                map.put("CT1", "CT1");
                map.put("CT2", "CT2");
                map.put("CT3", "CT3");
                map.put("CT4", "CT4");
                map.put("CT5", "CT5");
                map.put("CT6", "CT6");
                break;

            case CORNELIA:
                map.put("CH1", "CH1");
                map.put("CH2", "CH2");
                map.put("CH3", "CH3");
                map.put("CH4", "CH4");
                map.put("PCHWP1", "PCHWP1");
                map.put("PCHWP2", "PCHWP2");
                map.put("PCHWP3", "PCHWP3");
                map.put("PCHWP4", "PCHWP4 ");
                map.put("CDWP1", "CDWP1");
                map.put("CDWP2", "CDWP2");
                map.put("CDWP3", "CDWP3");
                map.put("CDWP4A", "CDWP4");
                map.put("CDWP4B", "CDWP5");
                map.put("CT1", "CT1");
                map.put("CT2", "CT2");
                map.put("CT3A", "CT3");
                map.put("CT3B", "CT4");
                map.put("CT4", "CT5");
                break;

            case LATINA:
                map.put("CH13", "CH13");
                map.put("CH14", "CH14");
                map.put("CH15", "CH15");
                map.put("PCHWP132A", "PCHWP132");
                map.put("PCHWP132B", "PCHWP133");
                map.put("PCHWP142A", "PCHWP142");
                map.put("PCHWP142B", "PCHWP143");
                map.put("PCHWP152A", "PCHWP152");
                map.put("PCHWP152B", "PCHWP153");
                map.put("SCHWP11", "SCHWP11");
                map.put("SCHWP12", "SCHWP12");
                map.put("SCHWP13", "SCHWP13");
                map.put("SCHWP14", "SCHWP14");
                map.put("SCHWP21", "SCHWP21");
                map.put("SCHWP22", "SCHWP22");
                map.put("SCHWP23", "SCHWP23");
                map.put("SCHWP24", "SCHWP24");
                map.put("SCHWP31", "SCHWP31");
                map.put("SCHWP32", "SCHWP32");
                map.put("SCHWP33", "SCHWP33");
                map.put("SCHWP41", "SCHWP41");
                map.put("SCHWP42", "SCHWP42");
                map.put("SCHWP43", "SCHWP43");
                map.put("SCHWP44", "SCHWP44");
                map.put("CDWP131A", "CDWP131");
                map.put("CDWP131B", "CDWP132");
                map.put("CDWP141A", "CDWP141");
                map.put("CDWP141B", "CDWP142");
                map.put("CDWP151A", "CDWP151");
                map.put("CDWP151B", "CDWP152");
                map.put("CT131", "CT131");
                map.put("CT132", "CT132");
                map.put("CT133", "CT133");
                map.put("CT141", "CT141");
                map.put("CT142", "CT142");
                map.put("CT143", "CT143");
                map.put("CT151", "CT151");
                map.put("CT152", "CT152");
                map.put("CT153", "CT153");
                break;

            case MANATI:
                map.put("CH3", "CH3");
                map.put("CH4", "CH4");
                map.put("CH5", "CH5");
                map.put("CH6", "CH6");
                map.put("CH7", "CH7    ");
                map.put("PCHWP1", "PCHWP1");
                map.put("PCHWP2", "PCHWP2");
                map.put("PCHWP3", "PCHWP3");
                map.put("PCHWP4", "PCHWP4");
                map.put("PCHWP5", "PCHWP5");
                map.put("OBICHWP1", "SCHWP1");
                map.put("OBICHWP2", "SCHWP2");
                map.put("OBICHWP3", "SCHWP3");
                map.put("CDWP1", "CDWP1");
                map.put("CDWP2", "CDWP2");
                map.put("CDWP3", "CDWP3");
                map.put("CDWP4", "CDWP4");
                map.put("CDWP5", "CDWP5");
                map.put("CT1", "CT1");
                map.put("CT2", "CT2");
                map.put("CT3A", "CT3");
                map.put("CT3B", "CT4");
                map.put("CT4A", "CT5");
                map.put("CT4B", "CT6");
                break;

            case BIOCORK:
                map.put("CH931", "CH931");
                map.put("CH932", "CH932");
                map.put("CH934", "CH934");
                map.put("PCHWP931", "PCHWP931");
                map.put("PCHWP932", "PCHWP932");
                map.put("PCHWP936", "PCHWP936");
                map.put("SCHWP933", "SCHWP933");
                map.put("SCHWP934", "SCHWP934");
                map.put("SCHWP935", "SCHWP935");
                map.put("SCHWP937", "SCHWP937");
                map.put("SCHWP938", "SCHWP938");
                map.put("CDWP940", "CDWP940");
                map.put("CDWP941", "CDWP941");
                map.put("CDWP942", "CDWP942");
                map.put("CDWP943", "CDWP943");
                map.put("CDWP944", "CDWP944");
                map.put("CT931", "CT931");
                map.put("CT932", "CT932");
                map.put("CT933", "CT933");
                break;
                
            default:
                break;

        }

        return map;
    }

}
