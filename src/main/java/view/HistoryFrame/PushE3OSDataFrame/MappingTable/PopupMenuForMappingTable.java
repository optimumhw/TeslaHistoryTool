
package view.HistoryFrame.PushE3OSDataFrame.MappingTable;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;


public class PopupMenuForMappingTable extends JPopupMenu {

    public PopupMenuForMappingTable(java.awt.event.MouseEvent evt, final JTable table) {

        JMenuItem item = new JMenuItem("Copy");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int[] selectedRowIndicies = table.getSelectedRows();

                int numrows = selectedRowIndicies.length;
                int numcols = table.getColumnCount();

                StringBuilder sbf = new StringBuilder();

                for (int i = 0; i < table.getColumnCount(); i++) {
                    sbf.append(table.getColumnName(i));
                    sbf.append("\t");
                }

                sbf.append("\n");

                for (int i = 0; i < numrows; i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) {

                        Object value = table.getValueAt(selectedRowIndicies[i], j);

                        if (value instanceof Double) {
                            NumberFormat formatter = new DecimalFormat("#0.000");
                            value = formatter.format(value);
                        }

                        sbf.append(value);

                        if (j < numcols - 1) {
                            sbf.append("\t");
                        }
                    }
                    sbf.append("\n");
                }

                Toolkit tk = Toolkit.getDefaultToolkit();
                Clipboard cb = tk.getSystemClipboard();
                StringSelection s = new StringSelection(sbf.toString());
                cb.setContents(s, null);
            }
        });

        this.add(item);

        JMenuItem selectAllItem = new JMenuItem("Select All");
        selectAllItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                table.selectAll();
            }

        });
        this.add(selectAllItem);
        this.show(evt.getComponent(), evt.getX(), evt.getY());
    }

}
