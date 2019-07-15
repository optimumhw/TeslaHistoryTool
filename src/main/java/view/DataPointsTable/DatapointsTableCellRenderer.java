package view.DataPointsTable;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DatapointsTableCellRenderer extends DefaultTableCellRenderer {

    private final Color limeGreen = new Color(204, 255, 204);
    private final Color lightBlue = new Color(204,255,255);
    private final Color lightYellow = new Color(255,255,229);

    public DatapointsTableCellRenderer() {


    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        
        EnumDatpointsTableColumns colEnum = EnumDatpointsTableColumns.getColumnFromColumnNumber(column);

        Color color = Color.WHITE;
        setBackground(isSelected ? color : color);
        this.setHorizontalAlignment(JLabel.LEFT);

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
            
            boolean b = (Boolean)value;
            if( colEnum == EnumDatpointsTableColumns.Sub ){
                color = (b)? limeGreen : Color.lightGray;
            }
            else{
                color = (b)? lightBlue : lightYellow;
            }
        }

        setBackground(isSelected ? color : color);
        return super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
    }
}
