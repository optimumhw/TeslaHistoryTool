
package view.HistoryFrame.HistoryStatsTable;

import java.util.ArrayList;
import java.util.List;


public enum EnumStatsRows {
    AVERAGE("mean", 0),
    STDDEV("stddev", 1),
    SUM("sum",2),
    MAX("max", 3),
    MIN("min", 4),
    COUNT("#notnull", 5),
    TOTALROWS("numrows", 6);

    private final String name;
    private final int row;

    EnumStatsRows( String name, int row )
    {
        this.name = name;
        this.row = row;
        
    }
    
    public String getName(){
        return this.name;
    }
    
    public int getRow(){
        return this.row;
    }
    
    static public EnumStatsRows getEnumFromRowNumber( int row ){
        for( EnumStatsRows avgsRowEnum : EnumStatsRows.values()){
            if( avgsRowEnum.getRow() == row ){
                return avgsRowEnum;
            }
        }
        
        return null;
    }

    
    static public List<String> getNames(){
        List<String> names = new ArrayList<>();
        for( EnumStatsRows avgsRowEnum : EnumStatsRows.values()){
          names.add(avgsRowEnum.getName() );
        }
        return names;
    }
    
}
