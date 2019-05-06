package view.DataGenerator;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DGTableCellRenderer extends DefaultTableCellRenderer {

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

        Boolean notMinOrMaxCol = column != EnumDGTableColumns.MinValue.getColumnNumber()
                && column != EnumDGTableColumns.MaxValue.getColumnNumber();
        this.setHorizontalAlignment((notMinOrMaxCol) ? JLabel.LEFT : JLabel.RIGHT);

        if (value == null) {
            color = Color.yellow;
            value = "null";
        } else if (value instanceof Double) {
            try {
                String precFormatString = "#0.000";
                NumberFormat formatter = new DecimalFormat(precFormatString);
                value = formatter.format(value);
                this.setHorizontalAlignment(JLabel.RIGHT);
            } catch (Exception ex) {
                color = Color.cyan;
                value = "NaN";
            }
        } else if (value instanceof Boolean) {
            color = new Color(214, 249, 248);
            value = ((Boolean) value == true) ? "true" : "false";
        }

        setBackground(isSelected ? color : color);
        return super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
    }
}
