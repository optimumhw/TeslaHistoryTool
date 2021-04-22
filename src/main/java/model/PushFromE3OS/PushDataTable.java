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
        Map<String, String> trailingPieceOverrideMap = getTrailingPieceOverridesMap();

        validateOverrides(e3osPoints);

        //add all the core points to the table, set status to "no e3os info"
        mappingTable = new ArrayList<>();

        for (DatapointListItem pt : datapointsList) {
            mappingTable.add(new MappingTableRow(pt));
        }

        //go through all the rows and try to find a matching e3os point
        for (MappingTableRow mappingTableRow : mappingTable) {

            if (getExceptionsList().contains(mappingTableRow.getTeslaName())) {
                continue;
            }

            boolean foundIt = false;
            for (DataPointFromSql e3osPoint : e3osPoints) {

                if (getExceptionsList().contains(e3osPoint.getDatapointName())) {
                    continue;
                }

                if (getSitePointsToUnmapList(stationInfo.getName()).contains(e3osPoint.getDatapointName())) {
                    continue;
                }

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

                    EnumOverrideType overrideType = EnumOverrideType.StationRegEx;

                    String coreKey = getMatchingOverrideKey(regexOverridesMap, coreName);

                    String[] pieces = coreName.split(coreKey, 2);
                    String trailingPiece = pieces[1];

                    //check trailingPieceOverride
                    if (trailingPieceOverrideMap.containsKey(trailingPiece)) {
                        trailingPiece = trailingPieceOverrideMap.get(trailingPiece);
                        overrideType = EnumOverrideType.DoubleOverride;
                    }

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
                            mappingTableRow.setOverrideType(overrideType);
                            foundIt = true;
                        }

                    }
                }

                //if not found it, try applying just the trailing override
                if (!foundIt) {

                    for (String coreTail : trailingPieceOverrideMap.keySet()) {
                        String e3osTail = trailingPieceOverrideMap.get(coreTail);
                        String[] pieces = coreName.split(coreTail, 2);

                        if (pieces == null || pieces.length < 2) {
                            continue;
                        }

                        String e3osNameToMatch = pieces[0] + e3osTail;

                        // if there is a key for this point, look through e3os points for a match
                        for (DataPointFromSql e3osPoint : e3osPoints) {

                            String e3osPointName = e3osPoint.getDatapointName();

                            //match on formed name
                            if (e3osPointName.equalsIgnoreCase(e3osNameToMatch)) {
                                mappingTableRow.setMapStatus(EnumMapStatus.Mapped);
                                mappingTableRow.setE3osName(e3osPoint.getDatapointName());
                                mappingTableRow.setXid(e3osPoint);
                                mappingTableRow.setOverrideType(EnumOverrideType.TailOnlyOverride);
                                foundIt = true;
                            }

                        }

                    }

                }

                if (hasMatchingOverrideValue(regexOverridesMap, coreName)) {

                    EnumOverrideType overrideType = EnumOverrideType.ReverseRegEx;

                    String e3osNameToMatch = getMatchingOverrideKeyFromValue(regexOverridesMap, coreName);

                    // if there is a key for this point, look through e3os points for a match
                    for (DataPointFromSql e3osPoint : e3osPoints) {

                        String e3osPointName = e3osPoint.getDatapointName();

                        if (getSitePointsToUnmapList(stationInfo.getName()).contains(e3osPointName)) {
                            continue;
                        }

                        //match on formed name
                        if (e3osPointName.equalsIgnoreCase(e3osNameToMatch)) {
                            mappingTableRow.setMapStatus(EnumMapStatus.Mapped);
                            mappingTableRow.setE3osName(e3osPoint.getDatapointName());
                            mappingTableRow.setXid(e3osPoint);
                            mappingTableRow.setOverrideType(overrideType);
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

        map.put("Ton", "JACETon");

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

    private String getMatchingOverrideKeyFromValue(Map<String, String> regexOverridesMap, String corePointName) {

        for (String key : regexOverridesMap.keySet()) {
            if (matchesPointName(corePointName, regexOverridesMap.get(key))) {
                return regexOverridesMap.get(key);
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

    private boolean hasMatchingOverrideValue(Map<String, String> regexOverridesMap, String coreName) {
        for (String v : regexOverridesMap.values()) {
            if (matchesPointName(coreName, v)) {
                return true;
            }
        }

        return false;
    }

    private List<String> getExceptionsList() {
        List<String> list = new ArrayList<>();

        list.add("CDWPSPD");
        list.add("EDGEMODE");

        return list;
    }

    private List<String> getSitePointsToUnmapList(String siteName) {
        List<String> list = new ArrayList<>();

        EnumOverrideSites enumOverrideSite = EnumOverrideSites.getEnumFromName(siteName);

        switch (enumOverrideSite) {

            case SUZHOU:
                list.add("TESPSPD");
                list.add("TESPTR");
                break;

            case Pickle:
                list.add("TESPSPD");
                list.add("TESPTR");
                break;

            case CHANDLER:
                list.add("PCHWPSPD");
                break;

            case MANATI:
                list.add("CHWPSPD");
                break;

            case EC1EC2:
                list.add("CTTR");
                list.add("CTFSPD");
                break;

            case CORNELIA:
                list.add("CTTR");
                list.add("CTFSPD");
                break;

        }

        return list;
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
            case UTMB:
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");
                map.put("CDWPSPD", "CDWCircuit1SPDSP");

                map.put("LOOPREQ", "OEMode");

                break;

            case Pickle:

                map.put("CH1", "CH1");
                map.put("CH2", "CH2");
                map.put("CH3", "CH3");

                map.put("PCHWP1", "PCHWP2");
                map.put("PCHWP2", "PCHWP3");
                map.put("PCHWP3", "PCHWP4");
                map.put("TESCHWP1", "TESCHWP1");
                map.put("TESCHWP2", "TESCHWP2");
                map.put("TESCHWP3", "TESCHWP3");

                map.put("CHWPTR", "primaryChilledWaterPumpControlGroup1PTRSP");
                map.put("CHWPSPD", "primaryChilledWaterPumpControlGroup1SPDSP");

                map.put("TESPTR", "thermalEnergyStorageControlGroup1PTRSP");
                map.put("TESSPD", "thermalEnergyStorageControlGroup1SPDSP");

                map.put("CDWP1", "CDWP1");
                map.put("CDWP2", "CDWP2");
                map.put("CDWP3", "CDWP3");

                map.put("CDWPSPD", "condenserWaterPumpControlGroup1SPDSP");
                map.put("CDWPTR", "condenserWaterPumpControlGroup1PTRSP");

                map.put("CT1", "CT1");
                map.put("CT2", "CT2");
                map.put("CT3", "CT3");

                map.put("CTTR", "coolingTowerControlGroup1CTTR");
                map.put("CTFSPD", "coolingTowerControlGroup1CTFSPD");
                map.put("CDWBPVCMD", "coolingTowerControlGroup1CDWBPVSP");

                break;

            case SUZHOU:

                map.put("LOOPREQ", "OEMode");
                map.put("Ton", "JaceTon");

                map.put("CH1", "CH1");
                map.put("CH2", "CH2");
                map.put("CH3", "CH3");
                map.put("CH4", "CH4");
                map.put("CH5", "CH5");
                map.put("CH6", "CH6");

                map.put("CHWP1", "PCHWP1");
                map.put("CHWP2", "PCHWP2");
                map.put("CHWP3", "PCHWP3");
                map.put("CHWP4", "PCHWP4");
                map.put("PCHWP5", "PCHWP5");
                map.put("PCHWP6", "PCHWP6");
                map.put("PCHWP7", "PCHWP7");

                map.put("CHWPTR", "primaryChilledWaterPumpControlGroup1PTRSP");
                map.put("CHWPSPD", "primaryChilledWaterPumpControlGroup1SPDSP");

                map.put("CHWPTR2", "primaryChilledWaterPumpControlGroup2PTRSP");
                map.put("CHWPSPD2", "primaryChilledWaterPumpControlGroup2SPDSP");

                map.put("CDWP1", "CDWP1");
                map.put("CDWP2", "CDWP2");
                map.put("CDWP3", "CDWP3");
                map.put("CDWP4", "CDWP4");
                map.put("CDWP5", "CDWP5");
                map.put("CDWP6", "CDWP6");

                map.put("CDWPSPD", "condenserWaterPumpControlGroup1SPDSP");
                map.put("CDWPSPD2", "condenserWaterPumpControlGroup2SPDSP");
                map.put("CDWPSPD3", "condenserWaterPumpControlGroup3SPDSP");
                map.put("CDWPSPD4", "condenserWaterPumpControlGroup4SPDSP");
                map.put("CDWPSPD5", "condenserWaterPumpControlGroup5SPDSP");
                map.put("CDWPSPD6", "condenserWaterPumpControlGroup6SPDSP");

                map.put("CT1", "CT1");
                map.put("CT2", "CT2");
                map.put("CT3", "CT3");
                map.put("CT4", "CT4");
                map.put("CT5", "CT5");
                map.put("CT6", "CT6");

                map.put("CTTR", "coolingTowerControlGroup1CTTRSP");
                map.put("CTFSPD", "coolingTowerControlGroup1CTFSPD");

                map.put("CTFSPD2", "coolingTowerControlGroup2CTFSPD");
                map.put("CDWBPVCMD", "coolingTowerControlGroup2CDWBPVSP");

                break;

            case CHANDLER:

                map.put("CH10", "CH10");
                map.put("CH11", "CH11");
                map.put("CH12", "CH12");
                map.put("CH13", "CH13");
                map.put("CH4", "CH4");
                map.put("CH5", "CH5");
                map.put("CH6", "CH6");
                map.put("CH7", "CH7");
                map.put("CH8", "CH8");
                map.put("CH9", "CH9");

                map.put("PCHWP4", "PCHWP4");
                map.put("PCHWP5", "PCHWP5");
                map.put("PCHWP6", "PCHWP6");
                map.put("PCHWP7", "PCHWP7");
                map.put("PCHWP8", "PCHWP8");
                map.put("PCHWP9", "PCHWP9");
                map.put("PCWHP10", "PCWHP10");
                map.put("PCHWP11", "PCHWP11");
                map.put("PCHWP12", "PCHWP12");
                map.put("PCHWP13", "PCHWP13");
                map.put("SCHWP4", "SCHWP4");
                map.put("SCHWP5", "SCHWP5");
                map.put("SCHWP6", "SCHWP6");
                map.put("SCHWP7", "SCHWP7");
                map.put("SCHWP8", "SCHWP8");
                map.put("SCHWP17", "SCHWP17");
                map.put("SCHWP18", "SCHWP18");
                map.put("SCHWP19", "SCHWP19");

                map.put("PCHWPSPD", "primaryChilledWaterPumpControlGroup1SPDSP");
                map.put("PCHWPSPD2", "primaryChilledWaterPumpControlGroup2SPDSP");
                map.put("SCHWPTR3", "secondaryChilledWaterPumpControlGroup1PTRSP");

                map.put("SCHWDPSP", "secondaryChilledWaterPumpControlGroup1DPSP");

                map.put("CDWP9", "CDWP9");
                map.put("CDWP10", "CDWP10");
                map.put("CDWP11", "CDWP11");
                map.put("CDWP12", "CDWP12");
                map.put("CDWP13", "CDWP13");
                map.put("CDWP14", "CDWP14");
                map.put("CDWP15", "CDWP15");
                map.put("CDWP19", "CDWP19");
                map.put("CDWP20", "CDWP20");
                map.put("CDWP21", "CDWP21");
                map.put("CDWP22", "CDWP22");
                map.put("CDWP23", "CDWP23");

                map.put("CDWPSPD", "condenserWaterPumpControlGroup1SPDSP");
                map.put("CDWPSPD2", "condenserWaterPumpControlGroup2SPDSP");
                map.put("", "");
                map.put("CT7", "CT7");
                map.put("CT8", "CT8");
                map.put("CT9", "CT9");
                map.put("CT10", "CT10");
                map.put("CT11", "CT11");
                map.put("CT12", "CT12");

                map.put("CTTR", "????");
                map.put("CTFSPD", "coolingTowerControlGroup1CTFSPD");
                map.put("CTFSPD2", "coolingTowerControlGroup2CTFSPD");
                map.put("CTBPV", "coolingTowerControlGroup1CDWBPVSP");
                map.put("CTBPV2", "coolingTowerControlGroup2CDWBPVSP");
                break;

            case CORNELIA:

                map.put("CH1", "CH1");
                map.put("CH2", "CH2");
                map.put("CH3", "CH3");
                map.put("CH4", "CH4");

                map.put("PCHWP1", "PCHWP1");
                map.put("PCHWP2", "PCHWP2");
                map.put("PCHWP3", "PCHWP3");
                map.put("PCHWP4", "PCHWP4");

                map.put("CHWPSPD", "primaryChilledWaterPumpControlGroup1SPDSP");
                map.put("CHWPTR", "primaryChilledWaterPumpControlGroup1PTRSP");

                map.put("CDWP1", "CDWP1");
                map.put("CDWP2", "CDWP2");
                map.put("CDWP3", "CDWP3");
                map.put("CDWP4A", "CDWP4");
                map.put("CDWP4B", "CDWP5");

                map.put("CDWPSPD", "condenserWaterPumpControlGroup1SPDSP");
                map.put("CDWPSPD2", "condenserWaterPumpControlGroup2SPDSP");
                map.put("CDWPSPD3", "condenserWaterPumpControlGroup3SPDSP");
                map.put("CDWPSPD4", "condenserWaterPumpControlGroup4SPDSP");

                map.put("CT1", "CT1");
                map.put("CT2", "CT2");
                map.put("CT3A", "CT3");
                map.put("CT3B", "CT4");
                map.put("CT4", "CT5");

                map.put("CTFSPD", "coolingTowerControlGroup1CTFSPD");
                map.put("CTTR", "coolingTowerControlGroup1CTTR");
                map.put("CTFSPD2", "coolingTowerControlGroup2CTFSPD");
                map.put("CTTR2", "coolingTowerControlGroup2CTTR");
                map.put("CTFSPD3", "coolingTowerControlGroup3CTFSPD");
                map.put("CTTR3", "coolingTowerControlGroup3CTTR");
                map.put("CTFSPD4", "coolingTowerControlGroup4CTFSPD");

                break;

            case LATINA:
                map.put("CH1", "CH13");
                map.put("CH2", "CH14");
                map.put("CH4", "CH15");
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

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");

                map.put("PCHWPTR2", "PCHWCircuit2PTRSP");
                map.put("PCHWPSPD2", "PCHWCircuit2SPDSP");
                map.put("PCHWPTR3", "PCHWCircuit3PTRSP");
                map.put("PCHWPSPD3", "PCHWCircuit3SPDSP");
                map.put("SCHWPTR", "SCHWPCircuit1PTRSP");
                map.put("SCHWPSPD", "SCHWPCircuit1SPDSP");
                map.put("SCHWPTR2", "SCHWPCircuit2PTRSP");
                map.put("SCHWPSPD2", "SCHWPCircuit2SPDSP");
                map.put("SCHWPTR3", "SCHWPCircuit3PTRSP");
                map.put("SCHWPSPD3", "SCHWPCircuit3SPDSP");
                map.put("SCHWPTR4", "SCHWPCircuit4PTRSP");
                map.put("SCHWPSPD4", "SCHWPCircuit4SPDSP");

                map.put("CDWPSPD", "CDWCircuit1SPDSP");
                map.put("CDWPTR", "CDWCircuit1PTRSP");
                map.put("CDWPSPD2", "CDWCircuit2SPDSP");
                map.put("CDWPTR2", "CDWCircuit2PTRSP");
                map.put("CDWPSPD3", "CDWCircuit3SPDSP");
                map.put("CDWPTR3", "CDWCircuit3PTRSP");
                break;

            case MANATI:

                map.put("CH3", "CH3");
                map.put("CH4", "CH4");
                map.put("CH5", "CH5");
                map.put("CH6", "CH6");
                map.put("CH7", "CH7");

                map.put("CHWP1", "PCHWP1");
                map.put("CHWP2", "PCHWP2");
                map.put("CHWP3", "PCHWP3");
                map.put("CHWP4", "PCHWP4");
                map.put("CHWP5", "PCHWP5");
                map.put("OBICHWP1", "SCHWP1");
                map.put("OBICHWP2", "SCHWP2");
                map.put("OBICHWP3", "SCHWP3");

                map.put("CHWPTR", "primaryChilledWaterPumpControlGroup1PTRSP");
                map.put("CHWPSPD", "primaryChilledWaterPumpControlGroup1SPDSP");

                map.put("CHWPTR2", "secondaryChilledWaterPumpControlGroup1PTRSP");
                map.put("CHWPSPD2", "secondaryChilledWaterPumpControlGroup1SPDSP");

                map.put("CDWP9", "CDWP1");
                map.put("CDWP10", "CDWP2");
                map.put("CDWP11", "CDWP3");
                map.put("CDWP12", "CDWP4");
                map.put("CDWP13", "CDWP5");

                map.put("CDWPTR", "condenserWaterPumpControlGroup1PTRSP");
                map.put("CDWPSPD", "condenserWaterPumpControlGroup1SPDSP");

                map.put("CDWPTR2", "condenserWaterPumpControlGroup2SPDSP");
                map.put("CDWPSPD2", "condenserWaterPumpControlGroup2PTRSP");

                map.put("CT1", "CT1");
                map.put("CT2", "CT2");
                map.put("CT3A", "CT31");
                map.put("CT3B", "CT32");
                map.put("CT4A", "CT41");
                map.put("CT4B", "CT42");

                map.put("CTTR", "coolingTowerControlGroup1CTTR");
                map.put("CTFSPD", "coolingTowerControlGroup1CTFSPD");

                map.put("CTTR2", "coolingTowerControlGroup2CTTR");
                map.put("CTFSPD2", "coolingTowerControlGroup2CTFSPD");

                break;

            case BIOCORK:

                map.put("CH1", "CH931");
                map.put("CH2", "CH932");
                map.put("CH4", "CH934");

                map.put("PCHWP931", "PCHWP931");
                map.put("PCHWP932", "PCHWP932");
                map.put("PCHWP936", "PCHWP936");
                map.put("SCHWP933", "SCHWP933");
                map.put("SCHWP934", "SCHWP934");
                map.put("SCHWP935", "SCHWP935");
                map.put("SCHWP937", "SCHWP937");
                map.put("SCHWP938", "SCHWP938");

                map.put("PCHWPTR", "primaryChilledWaterPumpControlGroup1PTRSP");
                map.put("PCHWPSPD", "primaryChilledWaterPumpControlGroup1SPDSP");

                map.put("SCHWPTR", "secondaryChilledWaterPumpControlGroup1PTRSP");
                map.put("SCHWPTR2", "secondaryChilledWaterPumpControlGroup2PTRSP");

                map.put("SCHWPDPSP", "secondaryChilledWaterPumpControlGroup1DPSP");
                map.put("SCHWPDPSP2", "secondaryChilledWaterPumpControlGroup2DPSP");

                map.put("CDWP940", "CDWP940");
                map.put("CDWP941", "CDWP941");
                map.put("CDWP942", "CDWP942");
                map.put("CDWP943", "CDWP943");
                map.put("CDWP944", "CDWP944");

                map.put("CDWPTR", "condenserWaterPumpControlGroup1PTRSP");
                map.put("CDWPSPD", "condenserWaterPumpControlGroup1SPDSP");

                map.put("CT1", "CT931");
                map.put("CT2", "CT932");
                map.put("CT3", "CT933");

                map.put("CTFSPD", "coolingTowerControlGroup1CTFSPD");
                map.put("CTFTR", "coolingTowerControlGroup1CTTR");

                break;

            case INDEPEND:
                map.put("CT1A", "CT11");
                map.put("CT1B", "CT12");
                map.put("CT2", "CT2");
                map.put("CT3", "CT3");
                map.put("CT4", "CT4");

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");
                map.put("PCHWPTR2", "PCHWCircuit2PTRSP");
                map.put("PCHWPSPD2", "PCHWCircuit2SPDSP");
                map.put("PCHWPSPD3", "PCHWCircuit3SPDSP");

                map.put("CDWPTR", "CDWCircuit4PTRSP");
                map.put("CDWPSPD", "CDWCircuit4SPDSP");
                map.put("CDWPTR2", "CDWCircuit6PTRSP");
                map.put("CDWPSPD2", "CDWCircuit6SPDSP");
                map.put("CDWPTR3", "CDWCircuit5PTRSP");
                map.put("CDWPSPD3", "CDWCircuit5SPDSP");

                break;

            case VISTAKON_7:

                map.put("CT1A", "CT11");
                map.put("CT1B", "CT12");
                map.put("CT2A", "CT21");
                map.put("CT2B", "CT22");

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");
                map.put("CDWPSPD", "CDWCircuit1SPDSP");

                break;

            case VISTAKON_1:
                map.put("CT1A", "CT11");
                map.put("CT1B", "CT12");
                map.put("CT2A", "CT21");
                map.put("CT2B", "CT22");

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");
                map.put("CDWPSPD", "CDWCircuit1SPDSP");
                map.put("CDWPSPD2", "CDWCircuit2SPDSP");
                map.put("CDWPSPD3", "CDWCircuit3SPDSP");

                break;

            case VISTAKON_PH2:
                map.put("CT3A", "CT31");
                map.put("CT3B", "CT32");
                map.put("CT4A", "CT41");
                map.put("CT4B", "CT42");
                map.put("CT5A", "CT51");
                map.put("CT5B", "CT52");

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");

                map.put("CDWPTR", "CDWCircuit4PTRSP");
                map.put("CDWPSPD", "CDWCircuit4SPDSP");
                map.put("CDWPTR2", "CDWCircuit5PTRSP");
                map.put("CDWPSPD2", "CDWCircuit5SPDSP");
                map.put("CDWPTR3", "CDWCircuit6PTRSP");
                map.put("CDWPSPD3", "CDWCircuit6SPDSP");

                break;

            case VISTAKON_RND:
                map.put("CT1A", "CT11");
                map.put("CT1B", "CT12");
                map.put("CT2A", "CT21");
                map.put("CT2B", "CT22");

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");

                map.put("CDWPTR", "CDWCircuit2PTRSP");
                map.put("CDWPSPD", "CDWCircuit2SPDSP");

                break;

            case LAJOLLA:
                map.put("Ton", "JACETon");
                map.put("CH4", "CH12");

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");
                map.put("PCHWPTR2", "PCHWCircuit2PTRSP");
                map.put("PCHWPSPD2", "PCHWCircuit2SPDSP");

                map.put("SCHWPTR", "SCHWCircuit1PTRSP");
                map.put("SCHWPSPD", "SCHWCircuit1SPDSP");
                map.put("SCHWPTR2", "SCHWCircuit2PTRSP");
                map.put("SCHWPSPD2", "SCHWCircuit2SPDSP");

                map.put("CDWPTR", "CDWCircuit2PTRSP");
                map.put("CDWPSPD", "CDWCircuit2SPDSP");

                break;

            case SAN_ANGELO:
                map.put("CH1", "CH50");
                map.put("CH2", "CH51");
                map.put("CH3", "CH61");
                map.put("CH4", "CH62");
                map.put("CH5", "CH63");

                map.put("CT1A", "CT1");
                map.put("CT2A", "CT2");
                map.put("CT3A", "CT3");
                map.put("CT52", "CT52");
                map.put("CT53", "CT53");

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPTR2", "PCHWCircuit2PTRSP");
                map.put("PCHWPSPD2", "PCHWCircuit2SPDSP");

                map.put("CDWPTR", "CDWCircuit3PTRSP");
                map.put("CDWPSPD", "CDWCircuit3SPDSP");
                map.put("CDWPTR2", "CDWCircuit4PTRSP");
                map.put("CDWPSPD2", "CDWCircuit4SPDSP");

                map.put("CHWPSPD", "PCHWCircuit1SPDSP");
                map.put("CHWPSPD2", "PCHWCircuit2SPDSP");

                break;

            case YALE:

                map.put("CT1", "CT1");
                map.put("CT2A", "CT21");
                map.put("CT2B", "CT22");
                map.put("CT2C", "CT23");
                map.put("CT6", "CT6");
                map.put("CT7", "CT7");
                map.put("PCHWP1", "PCHWP1");
                map.put("PCHWP2", "PCHWP2");
                map.put("PCHWPP1", "PCHWP3");
                map.put("PCHWPP2", "PCHWP4");
                map.put("Ton", "JACETon");

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");

                map.put("CDWPTR", "CDWCircuit5PTRSP");
                map.put("CDWPSPD", "CDWCircuit5SPDSP");
                map.put("CDWPTR1", "CDWCircuit4PTRSP");
                map.put("CDWPSPD1", "CDWCircuit4SPDSP");

                break;

            case FORT_WASH:

                map.put("CHA", "CH1");
                map.put("CHB", "CH2");
                map.put("CHC", "CH3");
                map.put("CHD", "CH4");
                map.put("CHF", "CH5");

                map.put("CDWPA", "CDWP1");
                map.put("CDWPB", "CDWP2");
                map.put("CDWPC", "CDWP3");
                map.put("CDWPCD", "CDWP4");
                map.put("CDWPD", "CDWP5");
                map.put("CDWPE", "CDWP6");
                map.put("CDWPF", "CDWP7");
                map.put("CDWPH", "CDWP8");

                map.put("CTA", "CT1");
                map.put("CTB", "CT2");
                map.put("CTC", "CT3");
                map.put("CTD", "CT4");
                map.put("CTF", "CT5  ");

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");
                map.put("PCHWPTR2", "PCHWCircuit2PTRSP");
                map.put("PCHWPSPD2", "PCHWCircuit2SPDSP");
                map.put("PCHWPTR3", "PCHWCircuit3PTRSP");
                map.put("PCHWPSPD3", "PCHWCircuit3SPDSP");
                map.put("PCHWPTR4", "PCHWCircuit4PTRSP");
                map.put("PCHWPSPD4", "PCHWCircuit4SPDSP");
                map.put("PCHWPTR5", "PCHWCircuit5PTRSP");
                map.put("PCHWPSPD5", "PCHWCircuit5SPDSP");

                map.put("CDWPTR", "CDWCircuit4PTRSP");
                map.put("CDWPSPD", "CDWCircuit4SPDSP");
                map.put("CDWPTR2", "CDWCircuit5PTRSP");
                map.put("CDWPSPD2", "CDWCircuit5SPDSP");
                map.put("CDWPTR3", "CDWCircuit6PTRSP");
                map.put("CDWPSPD3", "CDWCircuit6SPDSP");

                break;

            case SPRINGHOUSE_CUP:
                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");
                map.put("SCHWPTR", "SCHWCircuit1PTRSP");
                map.put("SCHWPSPD", "SCHWCircuit1SPDSP");

                map.put("CDWPTR", "CDWCircuit3PTRSP");
                map.put("CDWPSPD", "CDWCircuit3SPDSP");
                map.put("CDWPTR2", "CDWCircuit4PTRSP");
                map.put("CDWPSPD2", "CDWCircuit4SPDSP");

                break;

            case BLDG42:
                map.put("PCHWP4", "PCHWP424");
                map.put("PCHWP5", "PCHWP425");
                map.put("PCHWP6", "PCHWP426");
                map.put("SCHWP1", "SCHWP421");
                map.put("SCHWP2", "SCHWP422");
                map.put("SCHWP3", "SCHWP423");

                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");
                map.put("SCHWPTR", "SCHWCircuit1PTRSP");
                map.put("SCHWPSPD", "SCHWCircuit1SPDSP");

                map.put("CDWPTR2", "CDWCircuit2PTRSP");
                map.put("CDWPSPD2", "CDWCircuit2SPDSP");

                break;

            case ETHICON_NEW_MEXICO:
                map.put("PCHWPTR", "PCHWCircuit1PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit1SPDSP");
                map.put("SCHWPTR", "SCHWCircuit1PTRSP");
                map.put("SCHWPSPD", "SCHWCircuit1SPDSP");

                map.put("CDWPTR", "CDWCircuit2PTRSP");
                map.put("CDWPSPD", "CDWCircuit2SPDSP");

                break;

            case XIAN:
                map.put("CH1", "CH1");
                map.put("CH2", "CH2");
                map.put("CH3", "CH3");
                map.put("CH4", "CH4");
                map.put("CH5", "CH5");
                map.put("HRCH1", "CH6");

                map.put("CHWP1", "PCHWP1");
                map.put("CHWP2", "PCHWP2");
                map.put("CHWP3", "PCHWP3");
                map.put("CHWP4", "PCHWP4");
                map.put("CHWP5", "PCHWP5");
                map.put("CHWP6", "PCHWP6");

                map.put("CHWPTR", "PCHWCircuit1PTRSP");
                map.put("CHWPSPD", "PCHWCircuit1SPDSP");

                map.put("CHWPTR2", "PCHWCircuit2PTRSP");
                map.put("CHWPSPD2", "PCHWCircuit2SPDSP");

                map.put("CDWP1", "CDWP1");
                map.put("CDWP2", "CDWP2");
                map.put("CDWP3", "CDWP3");
                map.put("CDWP4", "CDWP4");
                map.put("CDWP7", "CDWP7");
                map.put("HXCDWP1", "CDWP5");
                map.put("HXCDWP2", "CDWP6");

                map.put("CDWPTR", "CDWCircuit1PTRSP");
                map.put("CDWPSPD", "CDWCircuit1SPDSP");

                map.put("CDWPTR2", "CDWCircuit2PTRSP");
                map.put("CDWPSPD2", "CDWCircuit2SPDSP");

                map.put("CT1", "CT1");
                map.put("CT2", "CT2");
                map.put("CT3", "CT3");
                map.put("CT4", "CT4");
                map.put("CT5", "CT5");

                break;

            case GURABOJ2:

                map.put("CH1", "CH1");
                map.put("CH2", "CH2");
                map.put("CH3", "CH3");
                map.put("CH4", "CH4");
                map.put("CH5", "CH5");
                map.put("CH6", "CH6");

                map.put("PCHWP1", "PCHWP6");
                map.put("PCHWP2", "PCWHP7");
                map.put("CHWP1", "PCHWP1");
                map.put("CHWP2", "PCHWP2");
                map.put("CHWP3", "PCHWP3");
                map.put("CHWP4", "PCHWP4");
                map.put("CHWP5", "PCHWP5");

                map.put("PCHWPTR", "PCHWCircuit2PTRSP");
                map.put("PCHWPSPD", "PCHWCircuit2SPDSP");
                map.put("CHWPSPD", "PCHWCircuit1SPDSP");
                map.put("CHWPTR", "PCHWCircuit1PTRSP");

                map.put("CTP1", "CDWP1");
                map.put("CTP3", "CDWP3");
                map.put("CTP161", "CDWP161");
                map.put("CTP162", "CDWP162");
                map.put("CDWP4", "CDWP4");
                map.put("CDWP5", "CDWP5");
                map.put("CDWP6", "CDWP6");

                map.put("CDWPTR", "CDWCircuit1PTRSP");
                map.put("CDWPSPD", "CDWCircuit1SPDSP");

                map.put("CT1", "CT1");
                map.put("CT2", "CT2");
                map.put("CT3", "CT3");
                map.put("CT4A", "CT41");
                map.put("CT4B", "CT42");
                map.put("CT4C", "CT43");
                map.put("CT4D", "CT44");
                map.put("CT5A", "CT51");
                map.put("CT5B", "CT52");
                map.put("CT5C", "CT53");
                map.put("CT5D", "CT54");

                break;

            case EC1EC2:

                map.put("CH1", "CH1");
                map.put("CH2", "CH2");
                map.put("CH3", "CH3");
                map.put("CH4", "CH4");
                map.put("CH5", "CH5");
                map.put("CH6", "CH6");
                map.put("CH7", "CH7");
                map.put("CH8", "CH8");

                map.put("PCHWP1", "PCHWP1");
                map.put("PCHWP2", "PCHWP2");
                map.put("PCHWP3", "PCHWP3");
                map.put("PCHWP4", "PCHWP4");

                map.put("CHWPTR", "primaryChilledWaterPumpControlGroup1PTRSP");

                map.put("CDWP1", "CDWP1");
                map.put("CDWP2", "CDWP2");
                map.put("CDWP3", "CDWP3");
                map.put("CDWP4", "CDWP4");
                map.put("CDWP5", "CDWP5");
                map.put("CDWP6", "CDWP6");
                map.put("CDWP7", "CDWP7");
                map.put("CDWP8", "CDWP8");

                map.put("CDWPTR", "condenserWaterPumpControlGroup1PTRSP");
                map.put("CDWPSPD", "condenserWaterPumpControlGroup1SPDSP");
                map.put("CDWPTR2", "condenserWaterPumpControlGroup2PTRSP");
                map.put("CDWPSPD2", "condenserWaterPumpControlGroup2SPDSP");

                map.put("CT1", "CT1");
                map.put("CT2", "CT2");
                map.put("CT3", "CT3");
                map.put("CT4", "CT4");
                map.put("CT5", "CT5");
                map.put("CT6", "CT6");
                map.put("CT7", "CT7");
                map.put("CT8", "CT8");

                map.put("CTTR", "coolingTowerControlGroup1CTTR");
                map.put("CTFSPD", "coolingTowerControlGroup1CTFSPD");
                map.put("CDWBPVCMD", "coolingTowerControlGroup1CDWBPVSP");
                map.put("CTTR2", "coolingTowerControlGroup2CTTR");
                map.put("CTFSPD2", "coolingTowerControlGroup2CTFSPD");

                break;

            default:
                break;

        }

        return map;
    }

    public Map<String, String> getTrailingPieceOverridesMap() {

        Map<String, String> map = new HashMap<>();

        map.put("SPDNotOptimized", "SPD_Alarm");

        return map;

    }

}
