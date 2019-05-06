
package view.RequestResponse;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;


public class PopUpMenuForRRTextArea  extends JPopupMenu {

    public PopUpMenuForRRTextArea(java.awt.event.MouseEvent evt, final JTextArea area) {

        JMenuItem itemCopy = new JMenuItem("Copy selection to clipboard");
        itemCopy.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                String selectedText = area.getSelectedText();
                
                Toolkit tk = Toolkit.getDefaultToolkit();
                Clipboard cb = tk.getSystemClipboard();
                StringSelection s = new StringSelection(selectedText);
                cb.setContents(s, null);
            }
        });
        this.add(itemCopy);
        
        JMenuItem itemSelectAll = new JMenuItem("Copy all to clipboard");
        itemSelectAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                area.selectAll();
                String selectedText = area.getSelectedText();
                
                Toolkit tk = Toolkit.getDefaultToolkit();
                Clipboard cb = tk.getSystemClipboard();
                StringSelection s = new StringSelection(selectedText);
                cb.setContents(s, null);
            }
        });
        this.add(itemSelectAll);
        this.show(evt.getComponent(), evt.getX(), evt.getY());
    }
}
