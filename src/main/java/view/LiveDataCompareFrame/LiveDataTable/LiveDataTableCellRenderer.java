
package view.LiveDataCompareFrame.LiveDataTable;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class LiveDataTableCellRenderer extends DefaultTableCellRenderer {

    private final Color limeGreen = new Color(204, 255, 204);
    private final Color lightBlue = new Color(204,255,255);
    private final Color lightYellow = new Color(255,255,229);
    
    final int prec;
    //private final DateTimeFormatter zzFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
    private final DateTimeFormatter uiFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    
    public LiveDataTableCellRenderer( int prec ){
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
        this.setHorizontalAlignment( JLabel.LEFT );
        EnumLiveDataTableColumns colEnum = EnumLiveDataTableColumns.getColumnFromColumnNumber(column);
        if( colEnum == EnumLiveDataTableColumns.CoreValue || colEnum == EnumLiveDataTableColumns.E3OSvalue ){
            this.setHorizontalAlignment( JLabel.RIGHT );
        }

        if (value == null) {
            color = Color.lightGray;
            value = "---";
        } else if (value instanceof String) {
            color = Color.WHITE;
            String temp = (String) value;
            if (temp.compareTo("*blank*") == 0) {
                color = lightBlue;
            }
        } else if (value instanceof Double) {
            try {
                String precFormatString = "#0";
                String stringOfZeros = "000000";
                if (prec > 0) {
                    precFormatString += ".";
                    precFormatString = precFormatString.concat(stringOfZeros.substring(0, prec));
                }

                NumberFormat formatter = new DecimalFormat(precFormatString);
                value = formatter.format(value);
            } catch (Exception ex) {
                color = Color.pink;
                value = "oops";
            }
        } else if (value instanceof Boolean) {
            boolean b = (Boolean) value;
            color = (b) ? limeGreen : lightYellow;

        } else if (value instanceof DateTime ){
            DateTime ts = (DateTime)value;
            String dateStr = ts.toString( uiFormat );
            value = dateStr;
        }

        setBackground(isSelected ? color : color);
        return super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
    }
}
