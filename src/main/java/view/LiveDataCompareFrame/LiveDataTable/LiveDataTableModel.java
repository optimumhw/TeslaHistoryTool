package view.LiveDataCompareFrame.LiveDataTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import model.DataPoints.CoreDatapoint;
import model.DataPoints.Equipment;
import model.DataPoints.LiveDatapoint;
import model.DataPoints.StationInfo;
import model.E3OS.E3OSLiveData.E3OSDataPoint;
import model.E3OS.E3OSLiveData.LiveDataPointAndValue;
import model.E3OS.E3OSLiveData.LiveDataResponse;

public class LiveDataTableModel extends AbstractTableModel {

    private List<CoreDatapoint> datapointList;
    private Map<String, LiveDataMappingTableRow> coreIDtoMaapingTableRow;
    private List<String> subscribedPoints;

    private List<E3OSDataPoint> e3osDataPoints;
    private Map<Integer, LiveDataMappingTableRow> e3osIDtoMappingTableRow;

    private List<LiveDataMappingTableRow> mappingRows;

    public LiveDataTableModel(StationInfo stationInfo, List<String> otherUIPointNames, List<E3OSDataPoint> e3osDataPoints) {
        super();

        datapointList = new ArrayList<>();
        datapointList = stationInfo.getDatapoints();
        for (Equipment eq : stationInfo.getequipments()) {
            datapointList.addAll(eq.getDatapoints());
        }

        /*
        coreIDtoCorePointMap = new HashMap<>();
        for (CoreDatapoint dp : datapointList) {
            coreIDtoCorePointMap.put(dp.getId(), dp);
        }
        
        e3osIDtoE3OSPointMap = new HashMap<>();
        for (E3OSDataPoint dp : e3osDataPoints) {
            e3osIDtoE3OSPointMap.put(dp.getId(), dp);
        }
         */
        subscribedPoints = new ArrayList<>();
        for (CoreDatapoint dp : datapointList) {
            if (dp.getSubscribedFlag() || otherUIPointNames.contains(dp.getShortName())) {
                subscribedPoints.add(dp.getId());
            }
        }

        mappingRows = new ArrayList<>();

        for (CoreDatapoint corePoint : datapointList) {
            mappingRows.add(new LiveDataMappingTableRow(corePoint));
        }

        for (E3OSDataPoint e3osPoint : e3osDataPoints) {
            Boolean foundIt = false;

            for (LiveDataMappingTableRow mrow : mappingRows) {
                if (e3osPoint.getName().contentEquals(mrow.getCoreName())) {
                    mrow.setMapStatus(EnumLiveDataMapStatus.Mapped);
                    mrow.setE3osName(e3osPoint.getName());
                    mrow.setE3osID(e3osPoint.getId());
                    mrow.setE3osValue(null);
                    foundIt = true;
                }
            }

            if (!foundIt) {
                mappingRows.add(new LiveDataMappingTableRow(e3osPoint));
            }

        }

        coreIDtoMaapingTableRow = new HashMap<>();
        e3osIDtoMappingTableRow = new HashMap<>();
        for (LiveDataMappingTableRow mrow : mappingRows) {
            coreIDtoMaapingTableRow.put(mrow.getCoreID(), mrow);
            e3osIDtoMappingTableRow.put(mrow.getE3osID(), mrow);
        }

    }

    public List<String> getSubscribedPoints() {
        return subscribedPoints;
    }

    public LiveDataMappingTableRow getRow(int idx) {
        return mappingRows.get(idx);
    }

    @Override
    public int getRowCount() {
        return mappingRows.size();
    }

    @Override
    public String getColumnName(int col) {
        return EnumLiveDataTableColumns.getColumnFromColumnNumber(col).getFriendlyName();
    }

    @Override
    public int getColumnCount() {
        return EnumLiveDataTableColumns.getColumnNames().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = "?";

        EnumLiveDataTableColumns enumCol = EnumLiveDataTableColumns.getColumnFromColumnNumber(columnIndex);

        LiveDataMappingTableRow mappingTableRow = mappingRows.get(rowIndex);

        switch (enumCol) {
            case MapStatus:
                val = mappingTableRow.getMapStatus().name();
                break;
            case PollFlag:
                val = mappingTableRow.getPollFlag();
                break;

            case CoreName:
                val = mappingTableRow.getCoreName();
                break;
            case CoreType:
                val = mappingTableRow.getCoreType();
                break;
            case CoreID:
                val = mappingTableRow.getCoreID();
                break;
            case CoreValue:
                val = mappingTableRow.getCoreValue();
                break;
            case E3OSName:
                val = mappingTableRow.getE3osName();
                break;
            case E3OSID:
                val = mappingTableRow.getE3osID();
                break;
            case E3OSvalue:
                val = mappingTableRow.getE3osValue();
                break;

        }

        return val;
    }
    
    public List<String> getCorePollPointIDs() {
        List<String> corePointIDs = new ArrayList<>();
        for (LiveDataMappingTableRow mrow : mappingRows) {
            if (mrow.getMapStatus() == EnumLiveDataMapStatus.Mapped && mrow.getCoreID().length() > 0 && mrow.getPollFlag()) {
                corePointIDs.add(mrow.getCoreID());
            }
        }
        return corePointIDs;
    }

    public List<Integer> getE3OSPollPointIDs() {
        List<Integer> e3osIDs = new ArrayList<>();
        for (LiveDataMappingTableRow mrow : mappingRows) {
            if (mrow.getMapStatus() == EnumLiveDataMapStatus.Mapped && mrow.getE3osID() > 0 && mrow.getPollFlag()) {
                e3osIDs.add(mrow.getE3osID());
            }
        }
        return e3osIDs;
    }

    public void appendLiveData(List<LiveDatapoint> livePoints) {

        for (LiveDatapoint livePoint : livePoints) {

            if (coreIDtoMaapingTableRow.containsKey(livePoint.getId())) {
                LiveDataMappingTableRow dpInTable = coreIDtoMaapingTableRow.get(livePoint.getId());
                if (livePoint.getValues() != null) {
                    dpInTable.setCoreValue(livePoint.getValues().get(0));
                }
            }
            fireTableDataChanged();
        }

    }

    public void appendE3OSLiveData(LiveDataResponse listOfLiveDataResponse) {

        for (LiveDataPointAndValue e3osPointAndValue : listOfLiveDataResponse.getData()) {

            if (e3osIDtoMappingTableRow.containsKey(e3osPointAndValue.getId())) {
                LiveDataMappingTableRow dpInTable = e3osIDtoMappingTableRow.get(e3osPointAndValue.getId());

                //if (e3osPointAndValue.getValue() != null) {
                dpInTable.setE3osValue(e3osPointAndValue.getValue());
                //}
                fireTableDataChanged();
            }

        }
    }

}
