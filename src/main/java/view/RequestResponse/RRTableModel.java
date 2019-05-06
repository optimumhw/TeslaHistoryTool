
package view.RequestResponse;


import javax.swing.table.AbstractTableModel;
import model.RestClient.RRObj;
import model.RestClient.RequestsResponses;
import org.joda.time.DateTime;


public class RRTableModel extends AbstractTableModel {

    private final RequestsResponses rrs;
    private final int count;
    
    private final String colNames[] = {"timestamp", "call", "type", "status", "desc" };

    public RRTableModel(RequestsResponses rrs) {
        super();
        this.rrs = rrs;
        this.count = rrs.getTimestampsInOrder().size();
        
    }

    @Override
    public int getRowCount() {
        return count;
    }

    @Override
    public String getColumnName(int col) {
        return colNames[ col ];
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        DateTime timestamp = this.rrs.getTimestampsInOrder().get(rowIndex);
        
        RRObj rr = this.rrs.getObj( timestamp );

        Object val = "?";
        
        switch( columnIndex ){
            
            case 0 :
                val = rr.getTimestamp();
                break;
                
            case 1 :
                val = rr.getCallType();
                break;
            
            case 2 :
                val = rr.getReqType();
                break;
                
            case 3 :
                val = rr.getStatus();
                break;
                
            case 4 :
                val = rr.getURL();
                break; 
        }
        
        return val;
    }


}