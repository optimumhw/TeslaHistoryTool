
package view.HistoryFrame.HistoryStatsTable;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class HistoryStatsTableCellRenderer extends DefaultTableCellRenderer {

    final int prec;
    public HistoryStatsTableCellRenderer( int prec ) {
        this.prec = prec;
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
        this.setHorizontalAlignment( (column == 0 )? JLabel.LEFT : JLabel.RIGHT);

        if( value == null ){
            color = Color.yellow;
            value = "NULL";
        }
        
        else if (value instanceof Double){
            try {
                String precFormatString = "#0";
                String stringOfZeros = "000000";
                if( prec > 0 ){
                    precFormatString += ".";
                    precFormatString = precFormatString.concat( stringOfZeros.substring(0, prec));
                }
                NumberFormat formatter = new DecimalFormat(precFormatString);
                value = formatter.format(value);
            } catch (Exception ex) {
                color = Color.cyan;
                value = "NaN";
            }
        }
        else if (value instanceof Boolean ){
            color = Color.blue;
        }

         setBackground(isSelected ? color : color);
        return super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
    }

}