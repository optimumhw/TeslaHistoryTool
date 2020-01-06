
package view.HistoryFrame.PushE3OSDataFrame.MappingTable;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.E3OS.LoadFromE3OS.EnumMapStatus;
import model.E3OS.LoadFromE3OS.MappingTableRow;

public class MappingTableModel extends AbstractTableModel {

    private final List<MappingTableRow> mappingTableRows;

    public MappingTableModel(List<MappingTableRow> mappingTableRows) {
        super();
        
        this.mappingTableRows = mappingTableRows;


    }


    public MappingTableRow getRow(int modelIndex) {
        return mappingTableRows.get(modelIndex);
    }

    public List<MappingTableRow> getMappedRows() {
        List<MappingTableRow> mappedRows = new ArrayList<>();
        for (MappingTableRow row : mappingTableRows) {
            if (row.getMapStatus() == EnumMapStatus.Mapped && row.getTeslaType().contentEquals("raw")) {
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
        return EnumMappingTableColumns.getColumnFromColumnNumber(col).getFriendlyName();
    }

    @Override
    public int getColumnCount() {
        return EnumMappingTableColumns.getColumnNames().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = "?";

        EnumMappingTableColumns enumCol = EnumMappingTableColumns.getColumnFromColumnNumber(columnIndex);

        MappingTableRow dataRow = mappingTableRows.get(rowIndex);

        switch (enumCol) {
            case MapStatus:
                val = dataRow.getMapStatus().name();
                break;
            case E3OSName:
                val = dataRow.getE3osName();
                break;
            case E3OSXid:
                val = dataRow.getXid().getXID() + dataRow.getE3osName();
                break;
            case TeslaName:
                val = dataRow.getTeslaName();
                break;
            case TeslaType:
                val = dataRow.getTeslaType();
                break;
            case TeslaID:
                val = dataRow.getTeslaID();
                break;

        }

        return val;
    }
}
