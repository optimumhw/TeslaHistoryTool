package view.LiveDataCompareFrame.E3OSSiteTable;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.E3OS.CustTreeList.E3OSSite;

public class E3OSSiteTableModel extends AbstractTableModel {

    private final List<E3OSSite> siteList;

    public E3OSSiteTableModel(List<E3OSSite> siteList) {
        super();

        this.siteList = siteList;
    }

    public E3OSSite getRow(int modelIndex) {
        return siteList.get(modelIndex);
    }

    @Override
    public int getRowCount() {
        return siteList.size();
    }

    @Override
    public String getColumnName(int col) {
        return EnumE3OSSitesTableColumns.getColumnFromColumnNumber(col).getFriendlyName();
    }

    @Override
    public int getColumnCount() {
        return EnumE3OSSitesTableColumns.getColumnNames().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = "?";

        EnumE3OSSitesTableColumns enumCol = EnumE3OSSitesTableColumns.getColumnFromColumnNumber(columnIndex);

        E3OSSite dataRow = siteList.get(rowIndex);

        switch (enumCol) {
            case SiteId:
                val = dataRow.getSiteID();
                break;
            case Name:
                val = dataRow.getName();
                break;
            case ShortName:
                val = dataRow.getShortName();
                break;
            case CustomerName:
                val = dataRow.getCustomerName();
                break;
        }

        return val;
    }
}
