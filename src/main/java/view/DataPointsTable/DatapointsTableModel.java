package view.DataPointsTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import model.DataPoints.Datapoint;
import model.DataPoints.Equipment;
import model.DataPoints.LiveDatapoint;
import model.DataPoints.StationInfo;

public class DatapointsTableModel extends AbstractTableModel {

    private List<Datapoint> datapointList;
    private Map<String, Datapoint> idToDatapointMap;
    private List<String> subscribedPoints;

    public DatapointsTableModel(StationInfo stationInfo, String name) {
        super();
        
        datapointList = new ArrayList<>();
        if (name.contentEquals("Station")) {
            datapointList = stationInfo.getDatapoints();
            return;
        }
        
        else{

        for (Equipment eq : stationInfo.getequipments()) {
            if (eq.getShortName().contentEquals(name)) {
                datapointList = eq.getDatapoints();
                break;
            }
        }}

        idToDatapointMap = new HashMap<>();
        for (Datapoint dp : datapointList) {
            idToDatapointMap.put(dp.getId(), dp);
        }

        idToDatapointMap = new HashMap<>();
        for (Datapoint dp : datapointList) {
            idToDatapointMap.put(dp.getId(), dp);
        }
        
        subscribedPoints = new ArrayList<>();
        for (Datapoint dp : datapointList) {
            if (dp.getSubscribedFlag()) {
                subscribedPoints.add(dp.getId());
            }
        }

    }
    
    public List<String> getSubscribedPoints(){
        return subscribedPoints;
    }

    public Datapoint getRow(int idx) {
        return datapointList.get(idx);
    }

    @Override
    public int getRowCount() {
        return datapointList.size();
    }

    @Override
    public String getColumnName(int col) {
        return EnumDatpointsTableColumns.getColumnFromColumnNumber(col).getFriendlyName();
    }

    @Override
    public int getColumnCount() {
        return EnumDatpointsTableColumns.getColumnNames().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = "?";

        EnumDatpointsTableColumns enumCol = EnumDatpointsTableColumns.getColumnFromColumnNumber(columnIndex);

        Datapoint datapoint = datapointList.get(rowIndex);

        switch (enumCol) {
            case Sub:
                val = datapoint.getSubscribedFlag();
                break;
            case LiveValue:
                val = datapoint.getLiveDataValue();
                break;
            case ID:
                val = datapoint.getId();
                break;
            case Name:
                val = datapoint.getName();
                break;
            case ShortName:
                val = datapoint.getShortName();
                break;
            case UOM:
                val = datapoint.getUnitOfMeasurement();
                break;
            case OwnerID:
                val = datapoint.getOwnerId();
                break;
            case Created:
                val = datapoint.getCreatedAt();
                break;
            case Updated:
                val = datapoint.getUpdatedAt();
                break;
            case OwnerType:
                val = datapoint.getOwnerType();
                break;
            case PointType:
                val = datapoint.getPointType();
                break;
            case Calculation:
                val = datapoint.getCalculation();
                break;
            case Reso:
                val = datapoint.getMinimumResolution();
                break;
            case Rollup:
                val = datapoint.getRollupAggregation();
                break;
        }

        return val;
    }

    public void appendLiveData(List<LiveDatapoint> livePoints) {

        for (LiveDatapoint livePoint : livePoints) {

            if (idToDatapointMap.containsKey(livePoint.getId())) {
                Datapoint dpInTable = idToDatapointMap.get(livePoint.getId());
                if (livePoint.getValues() != null) {
                    dpInTable.setLiveDataValue(livePoint.getValues().get(0));

                }
            }

            fireTableDataChanged();
        }

    }
}
