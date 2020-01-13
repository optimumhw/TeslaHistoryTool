package view.LiveDataCompareFrame.E3OSSiteTable;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.E3OS.CustTreeList.E3OSSite;
import model.E3OS.E3OSLiveData.E3OSStation;

public class E3OSStationTableModel extends AbstractTableModel {

    private final List<E3OSStation> stationList;

    public E3OSStationTableModel(List<E3OSStation> stationList) {
        super();

        this.stationList = stationList;
    }

    public E3OSStation getRow(int modelIndex) {
        return stationList.get(modelIndex);
    }

    @Override
    public int getRowCount() {
        return stationList.size();
    }

    @Override
    public String getColumnName(int col) {
        return EnumE3OSStationTableColumns.getColumnFromColumnNumber(col).getFriendlyName();
    }

    @Override
    public int getColumnCount() {
        return EnumE3OSStationTableColumns.getColumnNames().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = "?";

        EnumE3OSStationTableColumns enumCol = EnumE3OSStationTableColumns.getColumnFromColumnNumber(columnIndex);

        E3OSStation dataRow = stationList.get(rowIndex);

        switch (enumCol) {
            case CustomerID:
                val = dataRow.getCustomerID();
                break;
            case CustomerName:
                val = dataRow.getCustomerName();
                break;
            case InstallationID:
                val = dataRow.getInstallationID();
                break;
            case InstallationName:
                val = dataRow.getInstallationName();
                break;
            case StationID:
                val = dataRow.getStationID();
                break;
            case StationName:
                val = dataRow.getStationName();
                break;
            case IsEnabled:
                val = dataRow.getIsEnabled();
                break;
            case SupervisorID:
                val = dataRow.getSupervisorID();
                break;
            case Supervisor:
                val = dataRow.getSupervisor();
                break;

        }

        return val;
    }
}
