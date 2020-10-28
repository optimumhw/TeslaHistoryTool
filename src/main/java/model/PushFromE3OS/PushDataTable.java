package model.PushFromE3OS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.DatapointList.DatapointListItem;
import model.E3OS.LoadFromE3OS.DataPointFromSql;
import model.E3OS.LoadFromE3OS.EnumMapStatus;
import model.E3OS.LoadFromE3OS.MappingTableRow;

public class PushDataTable {

    ArrayList<MappingTableRow> mappingTable;

    public PushDataTable(List<DatapointListItem> datapointsList, List<DataPointFromSql> e3osPoints) {

        Map<String, DataPointFromSql> e3OSOverrideToRecordMap = getE3OSOverrideToRecordMap(e3osPoints);
        Map<String, String> coreToE3OSNameOverridesMap = getCoreToE3OSNameOverridesMap();

        validateOverrides(e3osPoints);

        //add all the core points to the table, set status to "no esos info"
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

            //if didn't find it, check overrides
            if (!foundIt) {
                String coreName = mappingTableRow.getTeslaName();
                if (coreName.equalsIgnoreCase("CH2SPD")) {
                    System.out.println("wait here");
                }
                if (coreToE3OSNameOverridesMap.containsKey(coreName)) {
                    String e3osName = coreToE3OSNameOverridesMap.get(coreName);
                    if (e3OSOverrideToRecordMap.containsKey(e3osName)) {
                        DataPointFromSql e3osPoint = e3OSOverrideToRecordMap.get(e3osName);
                        mappingTableRow.setMapStatus(EnumMapStatus.Mapped);
                        mappingTableRow.setE3osName(e3osPoint.getDatapointName());
                        mappingTableRow.setXid(e3osPoint);

                    } else {
                        System.out.println("e3os: " + e3osName + " was not found! for " + coreName);
                    }

                } else {
                    System.out.println(coreName + "was not found at all ");
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

}
