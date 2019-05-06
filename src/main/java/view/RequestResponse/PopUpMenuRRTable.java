package view.RequestResponse;

import controller.Controller;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import model.RestClient.EnumCallType;
import model.RestClient.RRObj;
import model.RestClient.RequestsResponses;
import org.joda.time.DateTime;

public class PopUpMenuRRTable extends JPopupMenu {

    public PopUpMenuRRTable(java.awt.event.MouseEvent evt, final Controller controller, final JTable table) {

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

        JMenuItem curlStatement = new JMenuItem("Copy curl to clipboard");
        curlStatement.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (table.getSelectedRows().length > 0) {
                    RequestsResponses rrs = controller.getRRS();

                    String curl = "";
                    int[] selectedRowIndicies = table.getSelectedRows();
                    for (int i = 0; i < selectedRowIndicies.length; i++) {

                        int row = selectedRowIndicies[i];
                        RRTableModel model = (RRTableModel) table.getModel();
                        int modelIndex = table.convertRowIndexToModel(row);
                        DateTime ts = (DateTime) (model.getValueAt(modelIndex, 0));
                        RRObj rrObj = rrs.getObj(ts);

                        if (rrObj.getCallType() == EnumCallType.REQUEST) {
                            //TODO: fix me
                            curl += rrObj.getCurlStatement();
                        } else {
                            curl += rrObj.getPayload();
                        }

                        if (i < selectedRowIndicies.length - 1) {
                            curl += System.getProperty("line.separator");
                            curl += System.getProperty("line.separator");
                        }

                    }

                    Toolkit tk = Toolkit.getDefaultToolkit();
                    Clipboard cb = tk.getSystemClipboard();
                    StringSelection s = new StringSelection(curl);
                    cb.setContents(s, null);

                }
            }
        });
        this.add(curlStatement);

        this.show(evt.getComponent(), evt.getX(), evt.getY());

    }
}
