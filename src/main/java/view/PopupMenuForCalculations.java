
package view;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;


public class PopupMenuForCalculations extends JPopupMenu {

    public PopupMenuForCalculations(java.awt.event.MouseEvent evt, final JTextArea testArea) {

        JMenuItem item = new JMenuItem("Copy");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String selectedText = testArea.getSelectedText();

                Toolkit tk = Toolkit.getDefaultToolkit();
                Clipboard cb = tk.getSystemClipboard();
                StringSelection s = new StringSelection(selectedText);
                cb.setContents(s, null);
            }
        });

        this.add(item);

        JMenuItem selectAllItem = new JMenuItem("Select All");
        selectAllItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                testArea.selectAll();
            }

        });
        this.add(selectAllItem);
        this.show(evt.getComponent(), evt.getX(), evt.getY());
    }

}
