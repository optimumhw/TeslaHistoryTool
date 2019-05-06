package model.CSVCreator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.DataPoints.HistoryQueryResults;
import org.joda.time.DateTime;

public class CSVMaker {

    private final String filePath;
    private final HistoryQueryResults history;

    public CSVMaker(String filePath, HistoryQueryResults history) {
        this.filePath = filePath;
        this.history = history;
    }

    public boolean makeCSV() {

        BufferedWriter writer = null;
        boolean status = true;
        try {
            writer = new BufferedWriter(new FileWriter(filePath));
            
            String header = "timestamp," + String.join(",", history.getPointNames());
            writer.write(header);
            writer.newLine();

            for (DateTime timeStamp : history.getTimestamps()) {

                writer.write(timeStamp.toString());

                List<Object> values = history.getTimeStampToValuesArray().get(timeStamp);

                for (Object value : values) {
                    
                    writer.write(",");
                    if( value == null){
                       writer.write("null"); 
                    }
                    else if( value instanceof Double){
                        Double dee = (Double)value;
                         writer.write(dee.toString());
                    }
                    else if (value instanceof Integer) {
                        String str = Integer.toString((Integer) value);
                        writer.write(str);
                    }
                    else if( value instanceof Boolean){
                        boolean flag = (boolean)value;
                        writer.write( (flag)? "true" : "false");
                    }
                    else if( value instanceof String){
                        writer.write((String)value);
                    }
                    else{
                        writer.write("strange type");
                    }       
                }
                writer.newLine();
            }

        } catch (Exception ex) {
            Logger.getLogger(CSVMaker.class.getName()).log(Level.SEVERE, null, ex);
            status = false;
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
                Logger.getLogger(CSVMaker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return status;
    }

}
