package view.DataGenerator;

import model.simulator.UpsertPoint;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.simulator.EnumPattern;
import model.simulator.EnumPeriod;

public class DGPointTableModel extends AbstractTableModel {

    private final List<UpsertPoint> listOfPoints;

    public DGPointTableModel(List<UpsertPoint> listOfPoints) {
        super();
        this.listOfPoints = listOfPoints;

    }
    
    public UpsertPoint getRowFromTable( int rowIndex ){
        return listOfPoints.get(rowIndex);
    }
    
    
    public List<UpsertPoint> getRows(){
        return this.listOfPoints;
    }

    
    @Override
    public Class getColumnClass(int column) {

        if (getValueAt(0, column) == null) {
            return String.class;
        }
        return getValueAt(0, column).getClass();

    }

    public UpsertPoint getSiteDatapoint(int rowNumber) {
        return listOfPoints.get(rowNumber);
    }

    @Override
    public int getRowCount() {
        return listOfPoints.size();
    }

    @Override
    public String getColumnName(int col) {
        return EnumDGTableColumns.getColumnFromColumnNumber(col).getFriendlyName();
    }

    @Override
    public int getColumnCount() {
        return EnumDGTableColumns.getColumnNames().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        UpsertPoint point = this.listOfPoints.get(rowIndex);

        EnumDGTableColumns colEnum = EnumDGTableColumns.getColumnFromColumnNumber(columnIndex);

        Object val = "?";
        switch (colEnum) {

               
            case ID:
                val = point.getPoint().getId();
                break;
            case ShortName:
                val = point.getPoint().getShortName();
                break;
            case PointType:
                val = point.getPoint().getPointType();
                break;
            case MinValue:
                val = point.getMinValue();
                break;
            case MaxValue:
                val = point.getMaxValue();
                break;
            case Pattern:
                val = point.getPattern().getName();
                break;
            case Period:
                val = point.getPeriod().getName();
                break;
            case Offset:
                //if (point.getPointType() == EnumPointType.booleanType) {
                //    val = "Ignored";
                //} else {
                    val = point.getOffset();
                //}
                break;

        }
        return val;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        EnumDGTableColumns colEnum = EnumDGTableColumns.getColumnFromColumnNumber(columnIndex);
        return colEnum == EnumDGTableColumns.MinValue
                || colEnum == EnumDGTableColumns.MaxValue
                || colEnum == EnumDGTableColumns.Pattern
                || colEnum == EnumDGTableColumns.Period
                || colEnum == EnumDGTableColumns.Offset;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        UpsertPoint pointRow = listOfPoints.get(rowIndex);

        EnumDGTableColumns colEnum = EnumDGTableColumns.getColumnFromColumnNumber(columnIndex);

        switch (colEnum) {
            case MinValue:
                Double min;
                if (aValue instanceof Double) {
                    min = (Double) aValue;
                } else {
                    try {
                        min = Double.parseDouble((String) aValue);
                    } catch (Exception ex) {
                        min = 0.0;
                    }
                }
                pointRow.setMinValue(min);
                break;
            case MaxValue:
                Double max;
                if (aValue instanceof Double) {
                    max = (Double) aValue;
                } else {
                    try {
                        max = Double.parseDouble((String) aValue);
                    } catch (Exception ex) {
                        max = 0.0;
                    }
                }
                pointRow.setMaxValue(max);
                break;
            case Pattern:
                String patternNameStr = (String) aValue;
                EnumPattern pattern = EnumPattern.getEnumFromName(patternNameStr);
                pointRow.setPattern(pattern);
                break;
            case Period:
                String periodNameStr = (String) aValue;
                EnumPeriod period = EnumPeriod.getEnumFromName(periodNameStr);
                pointRow.setPeriod(period);
                break;
            case Offset:
                Double offset;
                if (aValue instanceof Double) {
                    offset = (Double) aValue;
                } else {
                    try {
                        offset = Double.parseDouble((String) aValue);
                    } catch (Exception ex) {
                        offset = 0.0;
                    }
                }
                pointRow.setOffset(offset);
                break;

        }

        fireTableDataChanged();
    }
}
