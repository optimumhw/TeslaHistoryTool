
package view.HistoryTable;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.DataPoints.HistoryQueryResults;
import org.joda.time.DateTime;


public class HistoryTableModel extends AbstractTableModel {

    private final HistoryQueryResults history;

    public HistoryTableModel(HistoryQueryResults history) {
        super();
        this.history = history;
    }

    @Override
    public int getRowCount() {
        return history.getTimestamps().size();
    }

    @Override
    public String getColumnName(int col) {
        if( col == 0){
            return "TimeStamp";
        }
        else {
            return history.getPointNames().get(col-1);
        }
    }

    @Override
    public int getColumnCount() {
        return history.getPointNames().size() + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = "?";

        DateTime timeStamp = history.getTimestamps().get(rowIndex);
        
        if( columnIndex == 0 ){
            val = timeStamp;
        }
        else{
            List< Object> values = ( List< Object> )history.getTimeStampToValuesArray().get(timeStamp);
            val = values.get(columnIndex-1);
        }

        return val;
    }

}

