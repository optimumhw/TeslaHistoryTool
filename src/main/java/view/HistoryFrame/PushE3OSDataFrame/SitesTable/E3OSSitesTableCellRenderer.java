
package view.HistoryFrame.PushE3OSDataFrame.SitesTable;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class E3OSSitesTableCellRenderer extends DefaultTableCellRenderer {

    //final Color limeGreen;

    public E3OSSitesTableCellRenderer() {

        //limeGreen = new Color(204, 255, 204);

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
        this.setHorizontalAlignment(JLabel.LEFT);

        //EnumSitesTableColumns enumColumn = EnumSitesTableColumns.getColumnFromColumnNumber(column);
        //E3OSSitesTableModel model = (E3OSSitesTableModel) table.getModel();
        //int modelIndex = table.convertRowIndexToModel(row);
        //mappedRow = model.getRow(modelIndex);
        
        return super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
    }
}