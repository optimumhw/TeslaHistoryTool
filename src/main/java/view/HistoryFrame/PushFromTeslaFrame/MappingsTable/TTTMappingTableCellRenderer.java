
package view.HistoryFrame.PushFromTeslaFrame.MappingsTable;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import model.LoadFromE3OS.EnumMapStatus;
import model.LoadFromE3OS.MappingTableRow;
import model.TTT.TTTMapStatus;
import model.TTT.TTTTableRow;
import view.DataPointsTable.EnumDatpointsTableColumns;
import view.HistoryFrame.PushE3OSDataFrame.MappingTable.MappingTableModel;


public class TTTMappingTableCellRenderer extends DefaultTableCellRenderer {

    final Color limeGreen;

    public TTTMappingTableCellRenderer() {

        limeGreen = new Color(204, 255, 204);

    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

        Color color = Color.WHITE;
        setBackground(isSelected ? color : color);
        this.setHorizontalAlignment(JLabel.LEFT);

        EnumTTTMappingTableColumns enumColumn = EnumTTTMappingTableColumns.getColumnFromColumnNumber(column);
        TTTMappingTableModel model = (TTTMappingTableModel) table.getModel();
        int modelIndex = table.convertRowIndexToModel(row);
        TTTTableRow mappedRow = model.getRow(modelIndex);

        if (column == 0) {
            if (mappedRow.getMapStatus() == TTTMapStatus.Overridden) {
                color = Color.YELLOW;
            }
            else if (mappedRow.getMapStatus() == TTTMapStatus.NoFromInfo) {
                color = Color.PINK;
            }
            else if (mappedRow.getMapStatus() == TTTMapStatus.NoToInfo) {
                color = Color.YELLOW;
            }
        } else {
            if (value == null) {
                color = Color.lightGray;
                value = "---";
            } else if (value instanceof String) {
                color = Color.WHITE;
                String temp = (String) value;
                if (temp.compareTo("NaN") == 0) {
                    value = "'NaN'";
                }
            } else if (value instanceof Double) {
                try {
                    String precFormatString = "#0";
                    String stringOfZeros = "000000";
                    if (3 > 0) {
                        precFormatString += ".";
                        precFormatString = precFormatString.concat(stringOfZeros.substring(0, 3));
                    }
                    NumberFormat formatter = new DecimalFormat(precFormatString);
                    value = formatter.format(value);
                } catch (Exception ex) {
                    color = Color.pink;
                    value = "oops";
                }
            } else if (value instanceof Boolean) {
                color = limeGreen;
            }
        }

        setBackground(isSelected ? color : color);
        return super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
    }
}