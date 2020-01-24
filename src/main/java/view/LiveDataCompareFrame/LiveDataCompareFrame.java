package view.LiveDataCompareFrame;

import controller.Controller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import model.DataPoints.CoreDatapoint;
import model.DataPoints.Equipment;
import model.DataPoints.LiveDatapoint;
import model.DataPoints.StationInfo;
import model.E3OS.E3OSLiveData.E3OSDataPoint;
import model.E3OS.E3OSLiveData.E3OSStation;
import model.E3OS.E3OSLiveData.E3osAuthResponse;
import model.E3OS.E3OSLiveData.LiveDataRequest;
import model.E3OS.E3OSLiveData.LiveDataResponse;
import model.PropertyChangeNames;
import org.jfree.ui.DateCellRenderer;
import org.joda.time.DateTime;
import view.LiveDataCompareFrame.E3OSSiteTable.E3OSSiteTableCellRenderer;
import view.LiveDataCompareFrame.E3OSSiteTable.E3OSStationTableModel;
import view.LiveDataCompareFrame.E3OSSiteTable.EnumE3OSStationTableColumns;
import view.LiveDataCompareFrame.LiveDataTable.EnumLiveDataMapStatus;
import view.LiveDataCompareFrame.LiveDataTable.EnumLiveDataTableColumns;
import view.LiveDataCompareFrame.LiveDataTable.LiveDataMappingTableRow;
import view.LiveDataCompareFrame.LiveDataTable.LiveDataTableCellRenderer;
import view.LiveDataCompareFrame.LiveDataTable.LiveDataTableModel;

public class LiveDataCompareFrame extends javax.swing.JFrame implements PropertyChangeListener {

    private static LiveDataCompareFrame thisInstance;

    private final Controller controller;
    private final StationInfo selectedStation;

    List<E3OSDataPoint> e3osDataPoints;

    private Timer coreTimer = null;
    private Timer e3osTimer = null;

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

        this.jLabelCoreStationName.setText(selectedStation.getName());

        SpinnerNumberModel spinModel = new SpinnerNumberModel(5, 5, 20, 1);
        this.jSpinnerCore.setModel(spinModel);

        spinModel = new SpinnerNumberModel(5, 5, 20, 1);
        this.jSpinnerE3OS.setModel(spinModel);

        e3osDataPoints = new ArrayList<>();

        configureCheckBoxUseRegex();
        configureShowPollPointsOnly();
        configureCheckBoxHideUnmapped();

        fillLiveDataTable();
    }

    @Override
    public void dispose() {

        if (coreTimer != null) {
            coreTimer.cancel();
            coreTimer = null;
        }

        if (e3osTimer != null) {
            e3osTimer.cancel();
            e3osTimer = null;
        }

        controller.removePropChangeListener(this);
        thisInstance = null;
        super.dispose();
    }

    private void killCoreLivePollingTimer() {

        jSpinnerCore.setEnabled(true);

        if (coreTimer != null) {
            coreTimer.cancel();
            coreTimer = null;
        }
    }

    private void killE3OSLivePollingTimer() {

        jSpinnerE3OS.setEnabled(true);

        if (e3osTimer != null) {
            e3osTimer.cancel();
            e3osTimer = null;
        }
    }

    private void configureCheckBoxUseRegex() {
        this.jCheckBoxUseRegex.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearLiveDataTable();
                fillLiveDataTable();
            }
        });
    }

    private void configureShowPollPointsOnly() {
        this.jCheckBoxShowPollPointsOnly.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearLiveDataTable();
                fillLiveDataTable();
            }
        });
    }

    private void configureCheckBoxHideUnmapped() {

        this.jCheckBoxHideUnmapped.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearLiveDataTable();
                fillLiveDataTable();
            }
        });
    }

    private void ShowE3OSAuthResponse(E3osAuthResponse e3osAuthResp) {
        this.jLabelToken.setText(e3osAuthResp.getToken());
        this.jLabelExpires.setText(e3osAuthResp.getExpires());
        controller.getE3OSStationList();
    }

    private void fillE3OSStationListTable(List<E3OSStation> stationList) {
        this.jTableE3OSStations.setDefaultRenderer(Object.class, new E3OSSiteTableCellRenderer());
        this.jTableE3OSStations.setModel(new E3OSStationTableModel(stationList));
        this.jTableE3OSStations.setAutoCreateRowSorter(true);
        fixTableDataPointsListColumns(jTableE3OSStations);
    }

    public void fixTableDataPointsListColumns(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            EnumE3OSStationTableColumns colEnum = EnumE3OSStationTableColumns.getColumnFromColumnNumber(i);
            TableColumn column = t.getColumnModel().getColumn(i);
            if (colEnum != null) {
                column.setPreferredWidth(colEnum.getWidth());
            } else {
                column.setPreferredWidth(50);
            }
        }
    }

    private List<String> getOtherUIPointNames() {
        List<String> uiOtherPointNames = new ArrayList<>();

        uiOtherPointNames.add("TotalTon");
        uiOtherPointNames.add("TotalkW");
        uiOtherPointNames.add("PlantEfficiency");
        uiOtherPointNames.add("ChillerEfficiency");
        uiOtherPointNames.add("ChillersRunning");

        return uiOtherPointNames;
    }

    public void clearLiveDataTable() {

        this.jTableLiveDataCompare.setDefaultRenderer(Object.class, new DateCellRenderer());
        this.jTableLiveDataCompare.setModel(new DefaultTableModel());
    }

    private List<LiveDataMappingTableRow> createLiveDataMappingTable() {

        Map< String, CoreDatapoint> tempMap = new HashMap<>();

        List<CoreDatapoint> datapointList = selectedStation.getDatapoints();

        for (CoreDatapoint dp : datapointList) {
            if (!tempMap.containsKey(dp.getId())) {
                tempMap.put(dp.getId(), dp);
            }
        }

        for (Equipment eq : selectedStation.getequipments()) {

            for (CoreDatapoint eqPt : eq.getDatapoints()) {
                if (!tempMap.containsKey(eqPt.getId())) {
                    tempMap.put(eqPt.getId(), eqPt);
                    datapointList.add(eqPt);
                }
            }
        }

        List<LiveDataMappingTableRow> mappingRows = new ArrayList<>();

        for (CoreDatapoint corePoint : datapointList) {
            mappingRows.add(new LiveDataMappingTableRow(corePoint));
        }

        for (E3OSDataPoint e3osPoint : e3osDataPoints) {
            Boolean foundIt = false;

            for (LiveDataMappingTableRow mrow : mappingRows) {
                if (e3osPoint.getName().contentEquals(mrow.getCoreName())) {
                    mrow.setMapStatus(EnumLiveDataMapStatus.Mapped);
                    mrow.setE3osName(e3osPoint.getName());
                    mrow.setE3osID(e3osPoint.getId());
                    mrow.setE3osValue(null);
                    foundIt = true;
                }
            }

            if (!foundIt) {
                mappingRows.add(new LiveDataMappingTableRow(e3osPoint));
            }

        }

        return mappingRows;
    }

    private void fillLiveDataTable() {

        clearLiveDataTable();
        List<LiveDataMappingTableRow> mappingTableRows = createLiveDataMappingTable();
        List<LiveDataMappingTableRow> filteredList = new ArrayList<>();

        String filter = this.jTextFieldPointFilter.getText();
        String[] pointNameFilters = filter.split(" ");

        for (LiveDataMappingTableRow mtRow : mappingTableRows) {

            if (filter.length() == 0) {
                filteredList.add(mtRow);
                continue;
            }

            for (String pointNameFilter : Arrays.asList(pointNameFilters)) {

                if (!this.jCheckBoxUseRegex.isSelected()
                        && (mtRow.getCoreName().contains(pointNameFilter)
                        || mtRow.getE3osName().contains(pointNameFilter))) {
                    filteredList.add(mtRow);
                } else if (this.jCheckBoxUseRegex.isSelected()) {
                    Pattern r = Pattern.compile(pointNameFilter);
                    Matcher m = r.matcher(mtRow.getCoreName());
                    if (m.find()) {
                        filteredList.add(mtRow);
                    }
                    m = r.matcher(mtRow.getE3osName());
                    if (m.find()) {
                        filteredList.add(mtRow);
                    }
                }
            }
        }

        List<LiveDataMappingTableRow> finalList = new ArrayList<>();

        for (LiveDataMappingTableRow mtRow : filteredList) {

            if (mtRow.getMapStatus() != EnumLiveDataMapStatus.Mapped && jCheckBoxHideUnmapped.isSelected()) {
                continue;
            }

            if (!mtRow.getPollFlag() && jCheckBoxShowPollPointsOnly.isSelected()) {
                continue;
            }
            
            finalList.add(mtRow);
        }

        this.jTableLiveDataCompare.setDefaultRenderer(Object.class, new LiveDataTableCellRenderer(3));
        this.jTableLiveDataCompare.setModel(new LiveDataTableModel(finalList));
        this.jTableLiveDataCompare.setAutoCreateRowSorter(true);
        fixLiveDataTableColumns(jTableLiveDataCompare);
    }

    public void fixLiveDataTableColumns(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            EnumLiveDataTableColumns colEnum = EnumLiveDataTableColumns.getColumnFromColumnNumber(i);
            TableColumn column = t.getColumnModel().getColumn(i);
            if (colEnum != null) {
                column.setPreferredWidth(colEnum.getWidth());
            } else {
                column.setPreferredWidth(50);
            }
        }
    }

    public void fillCoreLiveData(List<LiveDatapoint> dpList) {
        LiveDataTableModel model = (LiveDataTableModel) (this.jTableLiveDataCompare.getModel());
        model.appendLiveData(dpList);
    }

    public void fillE3OSLiveData(LiveDataResponse liveDataResponse) {
        LiveDataTableModel model = (LiveDataTableModel) (this.jTableLiveDataCompare.getModel());
        model.appendE3OSLiveData(liveDataResponse);
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
        jLabel2 = new javax.swing.JLabel();
        jLabelCoreStationName = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableE3OSStations = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableLiveDataCompare = new javax.swing.JTable();
        jCheckBoxHideUnmapped = new javax.swing.JCheckBox();
        jCheckBoxShowPollPointsOnly = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldPointFilter = new javax.swing.JTextField();
        jCheckBoxUseRegex = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jButtonClose = new javax.swing.JButton();
        jToggleButtonLiveCore = new javax.swing.JToggleButton();
        jSpinnerCore = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jToggleButtonLiveE3OS = new javax.swing.JToggleButton();
        jLabel5 = new javax.swing.JLabel();
        jSpinnerE3OS = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Live Data Comparison");

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

        jLabel2.setText("Opticx Core Station:");

        jLabelCoreStationName.setText("*station name*");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelCoreStationName))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(e3osAuth)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelToken)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelExpires)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabelCoreStationName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabelToken)
                        .addComponent(jLabel3)
                        .addComponent(jLabelExpires))
                    .addComponent(e3osAuth))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "E3OS Station List"));

        jTableE3OSStations.setAutoCreateRowSorter(true);
        jTableE3OSStations.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableE3OSStations.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableE3OSStations.setShowGrid(true);
        jTableE3OSStations.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableE3OSStationsMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTableE3OSStations);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1206, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setTopComponent(jPanel2);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Live Data Compare"));

        jTableLiveDataCompare.setAutoCreateRowSorter(true);
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
        jTableLiveDataCompare.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableLiveDataCompare.setShowGrid(true);
        jTableLiveDataCompare.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableLiveDataCompareMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(jTableLiveDataCompare);

        jCheckBoxHideUnmapped.setText("Hide Unmapped");

        jCheckBoxShowPollPointsOnly.setText("Poll Pts Only");

        jLabel6.setText("Filter:");

        jTextFieldPointFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPointFilterActionPerformed(evt);
            }
        });

        jCheckBoxUseRegex.setText("Use RegEx");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1206, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPointFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 505, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxUseRegex)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBoxShowPollPointsOnly)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxHideUnmapped)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxHideUnmapped)
                    .addComponent(jCheckBoxShowPollPointsOnly)
                    .addComponent(jLabel6)
                    .addComponent(jTextFieldPointFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxUseRegex, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel3);

        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        jToggleButtonLiveCore.setText("Poll Core Data");
        jToggleButtonLiveCore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonLiveCoreActionPerformed(evt);
            }
        });

        jLabel4.setText("secs:");

        jToggleButtonLiveE3OS.setText("Poll E3OS Data");
        jToggleButtonLiveE3OS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonLiveE3OSActionPerformed(evt);
            }
        });

        jLabel5.setText("secs:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToggleButtonLiveCore)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSpinnerCore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButtonLiveE3OS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSpinnerE3OS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonClose)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonClose)
                    .addComponent(jToggleButtonLiveCore)
                    .addComponent(jSpinnerCore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jToggleButtonLiveE3OS)
                    .addComponent(jLabel5)
                    .addComponent(jSpinnerE3OS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28))
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
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void jTableE3OSStationsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableE3OSStationsMousePressed
        killE3OSLivePollingTimer();
        killCoreLivePollingTimer();

        clearLiveDataTable();

        int row = jTableE3OSStations.getSelectedRow();
        int modelIndex = jTableE3OSStations.convertRowIndexToModel(row);
        E3OSStationTableModel mod = (E3OSStationTableModel) jTableE3OSStations.getModel();
        E3OSStation selectedStation = mod.getRow(modelIndex);
        controller.getE3OSPointsList(selectedStation.getCustomerID(), selectedStation.getStationID());

    }//GEN-LAST:event_jTableE3OSStationsMousePressed

    private void jToggleButtonLiveCoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonLiveCoreActionPerformed
        if (jToggleButtonLiveCore.isSelected()) {
            long sec = (int) jSpinnerCore.getModel().getValue();
            long interval = 1000 * sec;
            long startDelay = 0;

            coreTimer = new Timer();
            coreTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {

                        LiveDataTableModel mod = (LiveDataTableModel) jTableLiveDataCompare.getModel();
                        List<String> listOfCoreIDs = mod.getCorePollPointIDs();

                        if (listOfCoreIDs.size() > 0) {
                            controller.getLiveData(listOfCoreIDs);
                            System.out.println("polling for core live data...");
                        }
                    } catch (Exception ex) {
                        System.out.println("oops. something went wrong with the core timer");
                    }
                }
            }, startDelay, interval);

            jSpinnerCore.setEnabled(false);

        } else {
            if (coreTimer != null) {
                coreTimer.cancel();
                System.out.println("polling for core live data stopped...");
            }
            jSpinnerCore.setEnabled(true);
        }
    }//GEN-LAST:event_jToggleButtonLiveCoreActionPerformed

    private void jToggleButtonLiveE3OSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonLiveE3OSActionPerformed
        if (jToggleButtonLiveE3OS.isSelected()) {

            long sec = (int) jSpinnerE3OS.getModel().getValue();
            long interval = 1000 * sec;
            long startDelay = 0;

            e3osTimer = new Timer();
            e3osTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {

                        LiveDataTableModel mod = (LiveDataTableModel) jTableLiveDataCompare.getModel();
                        final List<Integer> e3osPointIds = mod.getE3OSPollPointIDs();
                        if (e3osPointIds.size() > 0) {

                            LiveDataRequest ldr = new LiveDataRequest(DateTime.now(), e3osPointIds);
                            controller.e3osLiveDataRequest(ldr);
                            System.out.println("polling for e3os live data...");
                        }

                    } catch (Exception ex) {
                        System.out.println("oops. something went wrong with the e3os timer");
                    }
                }
            }, startDelay, interval);

            jSpinnerE3OS.setEnabled(false);

        } else {
            if (e3osTimer != null) {
                e3osTimer.cancel();
                System.out.println("polling for e3os live data stopped...");
            }
            jSpinnerE3OS.setEnabled(true);
        }
    }//GEN-LAST:event_jToggleButtonLiveE3OSActionPerformed

    private void jTableLiveDataCompareMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableLiveDataCompareMousePressed
        int row = jTableLiveDataCompare.getSelectedRow();
        int modelIndex = jTableLiveDataCompare.convertRowIndexToModel(row);
        LiveDataTableModel mod = (LiveDataTableModel) jTableLiveDataCompare.getModel();
        LiveDataMappingTableRow mappingTableRow = mod.getRow(modelIndex);
        mappingTableRow.setPollFlag(!mappingTableRow.getPollFlag());
        mod.fireTableDataChanged();

    }//GEN-LAST:event_jTableLiveDataCompareMousePressed

    private void jTextFieldPointFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPointFilterActionPerformed
        fillLiveDataTable();
    }//GEN-LAST:event_jTextFieldPointFilterActionPerformed

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (propName.equals(PropertyChangeNames.E3OSLiveAuthenticated.getName())) {
            E3osAuthResponse e3osAuthResp = (E3osAuthResponse) evt.getNewValue();
            ShowE3OSAuthResponse(e3osAuthResp);

        } else if (propName.equals(PropertyChangeNames.E3OSStationListReturned.getName())) {
            List<E3OSStation> stationList = (List<E3OSStation>) evt.getNewValue();
            fillE3OSStationListTable(stationList);

        } else if (propName.equals(PropertyChangeNames.E3OSPointsListReturned.getName())) {
            e3osDataPoints = (List<E3OSDataPoint>) evt.getNewValue();
            fillLiveDataTable();

        } else if (propName.equals(PropertyChangeNames.LiveDataReturned.getName())) {
            List<LiveDatapoint> dpList = (List<LiveDatapoint>) evt.getNewValue();
            fillCoreLiveData(dpList);

        } else if (propName.equals(PropertyChangeNames.E3OSLiveDataReturned.getName())) {
            LiveDataResponse liveDataResponse = (LiveDataResponse) evt.getNewValue();
            fillE3OSLiveData(liveDataResponse);
        }

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton e3osAuth;
    private javax.swing.JButton jButtonClose;
    private javax.swing.JCheckBox jCheckBoxHideUnmapped;
    private javax.swing.JCheckBox jCheckBoxShowPollPointsOnly;
    private javax.swing.JCheckBox jCheckBoxUseRegex;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelCoreStationName;
    private javax.swing.JLabel jLabelExpires;
    private javax.swing.JLabel jLabelToken;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinnerCore;
    private javax.swing.JSpinner jSpinnerE3OS;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTableE3OSStations;
    private javax.swing.JTable jTableLiveDataCompare;
    private javax.swing.JTextField jTextFieldPointFilter;
    private javax.swing.JToggleButton jToggleButtonLiveCore;
    private javax.swing.JToggleButton jToggleButtonLiveE3OS;
    // End of variables declaration//GEN-END:variables
}
