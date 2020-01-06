package view.HistoryFrame.PushE3OSDataFrame.SitesTable;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.E3OS.LoadFromE3OS.E3OSStationRecord;

public class E3OSSitesTableModel extends AbstractTableModel {

    private final List<E3OSStationRecord> e3osSitesTableRow;

    public E3OSSitesTableModel(List<E3OSStationRecord> e3osSitesTableRow) {
        super();

        this.e3osSitesTableRow = e3osSitesTableRow;
    }

    public E3OSStationRecord getRow(int modelIndex) {
        return e3osSitesTableRow.get(modelIndex);
    }

    @Override
    public int getRowCount() {
        return e3osSitesTableRow.size();
    }

    @Override
    public String getColumnName(int col) {
        return EnumSitesTableColumns.getColumnFromColumnNumber(col).getFriendlyName();
    }

    @Override
    public int getColumnCount() {
        return EnumSitesTableColumns.getColumnNames().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = "?";

        EnumSitesTableColumns enumCol = EnumSitesTableColumns.getColumnFromColumnNumber(columnIndex);

        E3OSStationRecord dataRow = e3osSitesTableRow.get(rowIndex);

        switch (enumCol) {
            case CustomerID:
                val = dataRow.GetCustomerId();
                break;
            case CustShortName:
                val = dataRow.GetCustomerName();
                break;
            case SiteId:
                val = dataRow.GetSiteId();
                break;
            case SiteShortName:
                val = dataRow.GetSiteShortName();
                break;
            case InstId:
                val = dataRow.GetInstallationId();
                break;
            case InstShortName:
                val = dataRow.GetInstShortName();
                break;
            case StationId:
                val = dataRow.GetStationId();
                break;
            case StationShortName:
                val = dataRow.GetStationShortName();
                break;

        }

        return val;
    }
}
