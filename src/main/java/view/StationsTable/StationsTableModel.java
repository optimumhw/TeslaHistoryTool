package view.StationsTable;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.DataPoints.StationInfo;

public class StationsTableModel extends AbstractTableModel {

    private final List<StationInfo> stations;

    public StationsTableModel(List<StationInfo> stations) {
        super();

        this.stations = stations;

    }

    public StationInfo getRow(int idx) {
        return stations.get(idx);
    }

    @Override
    public int getRowCount() {
        return stations.size();
    }

    @Override
    public String getColumnName(int col) {
        return EnumStationsTableColumns.getColumnFromColumnNumber(col).getFriendlyName();
    }

    @Override
    public int getColumnCount() {
        return EnumStationsTableColumns.getColumnNames().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = "?";

        EnumStationsTableColumns enumCol = EnumStationsTableColumns.getColumnFromColumnNumber(columnIndex);

        StationInfo stationInfo = stations.get(rowIndex);

        switch (enumCol) {
            case CustName:
                val = stationInfo.getCustomerName();
                break;
            case ID:
                val = stationInfo.getId();
                break;
            case Name:
                val = stationInfo.getName();
                break;
            case ActivationCode:
                val = stationInfo.getActivationCode();
                break;
            case ActivatedAt:
                val = stationInfo.getActivatedAt();
                break;
            case ExpiresAt:
                val = stationInfo.getExpiresAt();
                break;
            case SiteID:
                val = stationInfo.getSiteId();
                break;
            case CommissionedAt:
                val = stationInfo.getcommissionedAt();
                break;
            case ShortName:
                val = stationInfo.getShortName();
                break;
            case PlantID:
                val = stationInfo.getPlantID();
                break;
            case BaselineEnabled:
                val = stationInfo.getBaselineEnabled();
                break;
            case RegenertationAllowed:
                val = stationInfo.getRegenerationAllowed();
                break;
            case AtomEnabled:
                val = stationInfo.getAtomEnabled();
                break;
            case ProductType:
                val = stationInfo.getProductType();
                break;
            case CreatedAt:
                val = stationInfo.getCreatedAt();
                break;
            case UpdatedAt:
                val = stationInfo.getUpdatedAt();
                break;
            case Address:
                val = stationInfo.getAddress();
                break;
            case TimeZone:
                val = stationInfo.getTimeZone();
                break;
            case Latitude:
                val = stationInfo.getLatitude();
                break;
            case Longitude:
                val = stationInfo.getLongitude();
                break;
        }

        return val;
    }

}
