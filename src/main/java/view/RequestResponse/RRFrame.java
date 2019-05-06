package view.RequestResponse;



import controller.Controller;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.table.DefaultTableModel;
import model.PropertyChangeNames;
import model.RestClient.RequestsResponses;
import org.joda.time.DateTime;


public class RRFrame extends javax.swing.JFrame implements PropertyChangeListener {
    
    private static RRFrame thisInstance;

    private RequestsResponses rrs;
    private Controller controller;

    private RRFrame(Controller controller) {
        initComponents();

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.controller = controller;
        this.rrs = controller.getRRS();
        
        this.RRTable.setDefaultRenderer(Object.class, new RRTableCellRenderer(rrs));
        this.RRTable.setModel(new RRTableModel(rrs));

        this.RRTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        this.RRTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        this.RRTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        this.RRTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        this.RRTable.getColumnModel().getColumn(4).setPreferredWidth(900);

    }
    
    public static RRFrame getInstance( Controller controller ){
        if( thisInstance == null){
            thisInstance = new RRFrame( controller );
        }
        
        return thisInstance;
    }
    
    @Override
    public void dispose() {
        controller.removePropChangeListener(this);
        thisInstance = null;
        super.dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonClearRequests = new javax.swing.JButton();
        jPanelRR = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        RRTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButtonClearRequests.setText("Clear Requests");
        jButtonClearRequests.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearRequestsActionPerformed(evt);
            }
        });

        jPanelRR.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Requests and Responses"));

        RRTable.setModel(new DefaultTableModel());
        RRTable.setRequestFocusEnabled(false);
        RRTable.setShowGrid(true);
        RRTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                RRTableMousePressed(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RRTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(RRTable);

        javax.swing.GroupLayout jPanelRRLayout = new javax.swing.GroupLayout(jPanelRR);
        jPanelRR.setLayout(jPanelRRLayout);
        jPanelRRLayout.setHorizontalGroup(
            jPanelRRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRRLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 927, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelRRLayout.setVerticalGroup(
            jPanelRRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRRLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 818, Short.MAX_VALUE)
                        .addComponent(jButtonClearRequests))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanelRR, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonClearRequests)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelRR, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void RRTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RRTableMouseClicked
        int temp = evt.getClickCount();

        if (temp == 2) {

            int row = this.RRTable.rowAtPoint(evt.getPoint());
            int col = this.RRTable.columnAtPoint(evt.getPoint());

            if (row >= 0 && col >= 0) {
                DateTime ts = (DateTime) (this.RRTable.getValueAt(row, 0));

                RRJsonView frame = new RRJsonView(rrs.getObj(ts));
                frame.pack();
                frame.setLocationRelativeTo(this);
                frame.setVisible(true);
            }

        }
    }//GEN-LAST:event_RRTableMouseClicked

    private void jButtonClearRequestsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearRequestsActionPerformed
        controller.clearRRS();
        
    }//GEN-LAST:event_jButtonClearRequestsActionPerformed

    private void RRTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RRTableMousePressed
        if (evt.isPopupTrigger()) {
            PopUpMenuRRTable popup = new PopUpMenuRRTable(evt, controller, RRTable);
        }
    }//GEN-LAST:event_RRTableMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable RRTable;
    private javax.swing.JButton jButtonClearRequests;
    private javax.swing.JPanel jPanelRR;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        String propName = evt.getPropertyName();

        Object a = evt.getOldValue();
        Object b = evt.getNewValue();

        if (a != null && a.equals(b)) {
            return;
        }

        if (propName.equals(PropertyChangeNames.RequestResponseChanged.getName())) {
            this.rrs = (RequestsResponses) b;
            this.RRTable.setDefaultRenderer(Object.class, new RRTableCellRenderer(rrs));
            this.RRTable.setModel(new RRTableModel(rrs));

            this.RRTable.getColumnModel().getColumn(0).setPreferredWidth(250);
            this.RRTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            this.RRTable.getColumnModel().getColumn(2).setPreferredWidth(100);
            this.RRTable.getColumnModel().getColumn(3).setPreferredWidth(50);
            this.RRTable.getColumnModel().getColumn(4).setPreferredWidth(900);
        }

    }
}
