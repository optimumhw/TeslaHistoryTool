package view.LiveDataCompareFrame;

import controller.Controller;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import model.DataPoints.StationInfo;
import model.E3OS.CustTreeList.E3OSSite;
import model.E3OS.E3OSLiveData.E3osAuthResponse;
import model.PropertyChangeNames;
import view.LiveDataCompareFrame.E3OSSiteTable.E3OSSiteTableCellRenderer;
import view.LiveDataCompareFrame.E3OSSiteTable.E3OSSiteTableModel;
import view.LiveDataCompareFrame.E3OSSiteTable.EnumE3OSSitesTableColumns;

public class LiveDataCompareFrame extends javax.swing.JFrame implements PropertyChangeListener {

    private static LiveDataCompareFrame thisInstance;

    private final Controller controller;
    private final StationInfo selectedStation;

    public static LiveDataCompareFrame getInstance(final Controller controller, StationInfo selectedStation) {
        if (thisInstance == null) {
            thisInstance = new LiveDataCompareFrame(controller, selectedStation);
        }
        return thisInstance;
    }

    public LiveDataCompareFrame(Controller controller, StationInfo selectedStation) {
        initComponents();

        this.controller = controller;
        this.selectedStation = selectedStation;
    }

    @Override
    public void dispose() {
        controller.removePropChangeListener(this);
        thisInstance = null;
        super.dispose();
    }

    private void ShowE3OSAuthResponse(E3osAuthResponse e3osAuthResp) {
        this.jLabelToken.setText(e3osAuthResp.getToken());
        this.jLabelExpires.setText(e3osAuthResp.getExpires());

        controller.getCustTreeList();
    }

    private void fillE3OSSiteList(List<E3OSSite> e3osCustList) {

        this.jTableE3OSSites.setDefaultRenderer(Object.class, new E3OSSiteTableCellRenderer());
        this.jTableE3OSSites.setModel(new E3OSSiteTableModel(e3osCustList));
        this.jTableE3OSSites.setAutoCreateRowSorter(true);
        fixTableDataPointsListColumns(jTableE3OSSites);

    }

    public void fixTableDataPointsListColumns(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            EnumE3OSSitesTableColumns colEnum = EnumE3OSSitesTableColumns.getColumnFromColumnNumber(i);
            TableColumn column = t.getColumnModel().getColumn(i);
            if (colEnum != null) {
                column.setPreferredWidth(colEnum.getWidth());
            } else {
                column.setPreferredWidth(50);
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        e3osAuth = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabelToken = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabelExpires = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableE3OSSites = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableLiveDataCompare = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jButtonClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        e3osAuth.setText("e3os Authenticate");
        e3osAuth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                e3osAuthActionPerformed(evt);
            }
        });

        jLabel1.setText("Token:");

        jLabelToken.setText("*token*");

        jLabel3.setText("Expires:");

        jLabelExpires.setText("*expires*");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(e3osAuth)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelToken)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelExpires)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(e3osAuth)
                    .addComponent(jLabel1)
                    .addComponent(jLabelToken)
                    .addComponent(jLabel3)
                    .addComponent(jLabelExpires))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "E3OS Site List"));

        jTableE3OSSites.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTableE3OSSites.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableE3OSSites.setShowGrid(true);
        jScrollPane1.setViewportView(jTableE3OSSites);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 844, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setTopComponent(jPanel2);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Live Data Compare"));

        jTableLiveDataCompare.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTableLiveDataCompare);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 844, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(jPanel3);

        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonClose)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonClose)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButtonCloseActionPerformed

    private void e3osAuthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_e3osAuthActionPerformed
        controller.e3osLiveAuthenticate();
    }//GEN-LAST:event_e3osAuthActionPerformed

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (propName.equals(PropertyChangeNames.E3OSLiveAuthenticated.getName())) {
            E3osAuthResponse e3osAuthResp = (E3osAuthResponse) evt.getNewValue();

            ShowE3OSAuthResponse(e3osAuthResp);
        } else if (propName.equals(PropertyChangeNames.E3OSSiteListReturned.getName())) {
            List<E3OSSite> siteList = (List<E3OSSite>) evt.getNewValue();

            fillE3OSSiteList(siteList);
        }

        /*
        if (propName.equals(PropertyChangeNames.DatapointsReturned.getName())) {
            datapointsList = (List<DatapointListItem>) evt.getNewValue();
            fillDataPointsListTable(this.filter);
        }


        if (propName.equals(PropertyChangeNames.LiveDataReturned.getName())) {
            List<LiveDatapoint> dpList = (List<LiveDatapoint>) evt.getNewValue();

            HistoryTableModel model = (HistoryTableModel) this.jTableHistory.getModel();
            model.appendLiveData(dpList);

        }
         */
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton e3osAuth;
    private javax.swing.JButton jButtonClose;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelExpires;
    private javax.swing.JLabel jLabelToken;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTableE3OSSites;
    private javax.swing.JTable jTableLiveDataCompare;
    // End of variables declaration//GEN-END:variables
}
