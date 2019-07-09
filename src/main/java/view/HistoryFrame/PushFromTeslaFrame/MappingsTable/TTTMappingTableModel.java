package view.HistoryFrame.PushFromTeslaFrame.MappingsTable;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.TTT.TTTMapStatus;
import model.TTT.TTTTableRow;

public class TTTMappingTableModel extends AbstractTableModel {

    private final List<TTTTableRow> mappingTableRows;

    public TTTMappingTableModel(List<TTTTableRow> mappingTableRows) {
        super();

        this.mappingTableRows = mappingTableRows;

    }

    public TTTTableRow getRow(int modelIndex) {
        return mappingTableRows.get(modelIndex);
    }

    public List<TTTTableRow> getMappedRows() {
        List<TTTTableRow> mappedRows = new ArrayList<>();
        for (TTTTableRow row : mappingTableRows) {
            if (row.getMapStatus() == TTTMapStatus.Mapped) {
                mappedRows.add(row);
            }
        }

        return mappedRows;
    }

    @Override
    public int getRowCount() {
        return mappingTableRows.size();
    }

    @Override
    public String getColumnName(int col) {
        return EnumTTTMappingTableColumns.getColumnFromColumnNumber(col).getFriendlyName();
    }

    @Override
    public int getColumnCount() {
        return EnumTTTMappingTableColumns.getColumnNames().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = "?";

        EnumTTTMappingTableColumns enumCol = EnumTTTMappingTableColumns.getColumnFromColumnNumber(columnIndex);

        TTTTableRow dataRow = mappingTableRows.get(rowIndex);

        switch (enumCol) {
            case MapStatus:
                val = dataRow.getMapStatus().name();
                break;
            case FromName:
                val = dataRow.getFromName();
                break;
            case FromID:
                val = dataRow.getFromID();
                break;
            case FromType:
                val = dataRow.getFromType();
                break;
            case ToName:
                val = dataRow.getToName();
                break;
            case ToID:
                val = dataRow.getToID();
                break;
            case ToType:
                val = dataRow.getToType();
                break;

        }

        return val;
    }
}
