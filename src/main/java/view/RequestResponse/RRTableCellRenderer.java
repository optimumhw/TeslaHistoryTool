
package view.RequestResponse;


import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import model.RestClient.EnumCallType;
import model.RestClient.EnumRequestType;
import model.RestClient.RequestsResponses;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class RRTableCellRenderer extends DefaultTableCellRenderer {

    private final RequestsResponses rrs;

    public RRTableCellRenderer(RequestsResponses rrs) {
        this.rrs = rrs;
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

        switch (column) {
            case 0:
                DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yy HH:mm:ss.SSS");
                value = fmt.print((DateTime) value);
                break;

            case 1:
                EnumCallType callType = (EnumCallType) value;
                value = callType.name();
                break;
                
            case 2:
                EnumRequestType requestType = (EnumRequestType) value;
                value = requestType.name();
                break;
                
            case 3:
                int status = (int) value;
                if( status == 0 ){
                    value = "";
                    color = Color.WHITE;
                }
                else if( status == 200 ){
                    color = Color.GREEN;
                }
                else {
                    color =  Color.pink;
                }
                break;
        }

        setBackground(isSelected ? color : color);
       
        return super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
    }

}
