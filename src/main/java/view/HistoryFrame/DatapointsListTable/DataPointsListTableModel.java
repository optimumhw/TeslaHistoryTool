
package view.HistoryFrame.DatapointsListTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import model.DatapointList.DatapointListItem;


public class DataPointsListTableModel extends AbstractTableModel {

    private final List<DatapointListItem> datapointList;
    private final Map<String, DatapointListItem> idToDatapointMap;

    public DataPointsListTableModel(List<DatapointListItem> datapointList) {
        super();

        this.datapointList = datapointList;
        idToDatapointMap = new HashMap<>();
        for (DatapointListItem dp : datapointList) {
            idToDatapointMap.put(dp.getId(), dp);
        }

    }

    public DatapointListItem getRow(int idx) {
        return datapointList.get(idx);
    }

    @Override
    public int getRowCount() {
        return datapointList.size();
    }

    @Override
    public String getColumnName(int col) {
        return EnumDataPointsListTableColumns.getColumnFromColumnNumber(col).getFriendlyName();
    }

    @Override
    public int getColumnCount() {
        return EnumDataPointsListTableColumns.getColumnNames().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = "?";

        EnumDataPointsListTableColumns enumCol = EnumDataPointsListTableColumns.getColumnFromColumnNumber(columnIndex);

        DatapointListItem datapoint = datapointList.get(rowIndex);

        switch (enumCol) {

            case ID:
                val = datapoint.getId();
                break;
            case PointType:
                val = datapoint.getPointType();
                break;
            case MinReso:
                val = datapoint.getMinimumResolution();
                break;
            case Rollup:
                val = datapoint.getrollupAggregation();
                break;
            case Name:
                val = datapoint.getName();
                break;
            case ShortName:
                val = datapoint.getShortName();
                break;
            case SiteId:
                val = datapoint.getSiteId();
                break;
        }

        return val;
    }

}

