package view;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.Controller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import model.Auth.LoginResponse;
import model.DataPoints.Datapoint;
import model.DataPoints.Equipment;
import model.DataPoints.LiveDatapoint;
import model.DataPoints.StationInfo;
import model.EnumBaseURLs;
import model.RestClient.ErrorResponse;
import model.RestClient.OEResponse;
import org.jfree.ui.DateCellRenderer;
import view.DataPointsTable.DatapointsTableCellRenderer;
import view.DataPointsTable.DatapointsTableModel;
import view.DataPointsTable.EnumDatpointsTableColumns;
import view.DataPointsTable.PopupMenuForDataPointsTable;
import view.HistoryFrame.HistoryFrame;
import view.RequestResponse.RRFrame;
import view.StationsTable.EnumStationsTableColumns;
import view.StationsTable.StationsTableCellRenderer;
import view.StationsTable.StationsTableModel;

public class MainFrame extends javax.swing.JFrame {

    private Controller controller;

    private EnumBaseURLs selectedBaseURL;

    private StationInfo selectedStation;
    private StationInfo selectedStationInfo;

    private Timer timer = null;

    public MainFrame() {
        initComponents();

        SpinnerNumberModel spinModel = new SpinnerNumberModel(5, 5, 20, 1);
        this.jSpinnerPollInterval.setModel(spinModel);

        selectedStation = null;
        selectedStationInfo = null;

        selectedBaseURL = EnumBaseURLs.Prod;

        setLoggedInInfo(false, null);
        //subscribedPoints = new ArrayList<>();
        //current_datapointList = new ArrayList<>();
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void showError(OEResponse response) {

        ObjectMapper mapper = new ObjectMapper();
        String error = "";
        String message = "";

        try {
            ErrorResponse errorResponse = mapper.readValue((String) response.responseObject, ErrorResponse.class);
            error = errorResponse.getError();
            message = errorResponse.getMessage();
        } catch (Exception ex) {
            error = "?";
            message = (String) response.responseObject;
        }

        Object[] options = {"OK"};

        String multiLineMessage = "no message in response";
        if (message != null) {
            int maxlineLen = 80;
            multiLineMessage = "";
            while (message.length() > maxlineLen) {
                multiLineMessage += message.substring(0, maxlineLen) + System.getProperty("line.separator");
                message = message.substring(maxlineLen);
            }
            multiLineMessage += message;
        }

        JOptionPane.showOptionDialog(null,
                multiLineMessage,
                Integer.toString(response.responseCode) + " - " + error,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null, options, options[0]);
    }

    public void setLoggedInInfo(boolean loggedIn, LoginResponse loginResponse) {

        this.jLabelLoggedIn.setText((loggedIn) ? "token" : "no token");
        if (loginResponse != null) {
            this.jLabelLoggedIn.setToolTipText(loginResponse.getAccessToken());
        }

    }

    public void fillAPIHosts() {
        ComboBoxModel comboBoxModel = new DefaultComboBoxModel(EnumBaseURLs.getURLs().toArray());
        this.jComboBoxBaseURLs.setModel(comboBoxModel);

        this.jComboBoxBaseURLs.setSelectedItem(selectedBaseURL.getURL());

        this.jComboBoxBaseURLs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                JComboBox<String> combo = (JComboBox<String>) event.getSource();
                String name = (String) combo.getSelectedItem();
                selectedBaseURL = EnumBaseURLs.getHostFromName(name);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        clearStationsTable();
                        clearDatapointsTable();
                    }
                });

            }
        });
    }

    private void initModel(EnumBaseURLs baseURL) {
        controller.initModel();
        controller.login(baseURL);
    }

    public void clearStationsTable() {

        this.jTableStationsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        this.jTableStationsTable.setModel(new DefaultTableModel());
        this.jTableStationsTable.setAutoCreateRowSorter(true);
        fixStationsTableColumnWidths(jTableStationsTable);

    }

    public void fillStationsTable(List<StationInfo> stations) {
        this.jTableStationsTable.setDefaultRenderer(Object.class, new StationsTableCellRenderer());
        this.jTableStationsTable.setModel(new StationsTableModel(stations));
        fixStationsTableColumnWidths(jTableStationsTable);
    }

    public void fixStationsTableColumnWidths(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            EnumStationsTableColumns colEnum = EnumStationsTableColumns.getColumnFromColumnNumber(i);
            TableColumn column = t.getColumnModel().getColumn(i);
            if (colEnum != null) {
                column.setPreferredWidth(colEnum.getWidth());
            } else {
                column.setPreferredWidth(50);
            }

        }

    }

    public void clearDatapointsTable() {

        this.jTableDatapointsTable.setDefaultRenderer(Object.class, new DateCellRenderer());
        this.jTableDatapointsTable.setModel(new DefaultTableModel());
    }

    public void fillEquipmentDropdown(StationInfo stationInfo) {

        this.selectedStationInfo = stationInfo;
        ComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        clearDatapointsTable();

        this.jComboBoxEquipment.setModel(comboBoxModel);
        jComboBoxEquipment.addItem("Station");

        for (Equipment eq : selectedStationInfo.getequipments()) {
            jComboBoxEquipment.addItem(eq.getShortName());
        }

        this.jComboBoxEquipment.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                JComboBox<String> combo = (JComboBox<String>) event.getSource();
                final String name = (String) combo.getSelectedItem();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        fillDatapointsTable(name);
                    }
                });
            }
        });

    }

    private void fillDatapointsTable(String name) {

        clearDatapointsTable();
        this.jTableDatapointsTable.setDefaultRenderer(Object.class, new DatapointsTableCellRenderer());
        this.jTableDatapointsTable.setModel(new DatapointsTableModel(selectedStationInfo, name, getOtherUIPointNames()));
        this.jTableDatapointsTable.setAutoCreateRowSorter(true);
        fixDatapointsTableColumnWidths(jTableDatapointsTable);

    }

    
    private List<String> getOtherUIPointNames(){
        List<String> uiOtherPointNames = new ArrayList<>();
        
        uiOtherPointNames.add("TotalTon");
        uiOtherPointNames.add("TotalkW");
        uiOtherPointNames.add("PlantEfficiency");
        uiOtherPointNames.add("ChillerEfficiency");
        uiOtherPointNames.add("ChillersRunning");
        
        return uiOtherPointNames;
    }
    
    
    private void fixDatapointsTableColumnWidths(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            EnumDatpointsTableColumns colEnum = EnumDatpointsTableColumns.getColumnFromColumnNumber(i);
            TableColumn column = t.getColumnModel().getColumn(i);
            if (colEnum != null) {
                column.setPreferredWidth(colEnum.getWidth());
            } else {
                column.setPreferredWidth(50);
            }
        }

    }

    public void fillLiveData(List<LiveDatapoint> dpList) {
        DatapointsTableModel model = (DatapointsTableModel) (this.jTableDatapointsTable.getModel());
        model.appendLiveData(dpList);
    }

    private void killLivePollingTimer() {

        jSpinnerPollInterval.setEnabled(true);

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonGetStations = new javax.swing.JButton();
        jButtonQuit = new javax.swing.JButton();
        jButtonRequests = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableStationsTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxEquipment = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableDatapointsTable = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextAreaCalculation = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxBaseURLs = new javax.swing.JComboBox<>();
        jButtonHistoryFrame = new javax.swing.JButton();
        jSpinnerPollInterval = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jTogglePollForLiveData = new javax.swing.JToggleButton();
        jLabelLoggedIn = new javax.swing.JLabel();
        jButtonLogin = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButtonGetStations.setText("Get Stations List");
        jButtonGetStations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetStationsActionPerformed(evt);
            }
        });

        jButtonQuit.setText("Quit");
        jButtonQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonQuitActionPerformed(evt);
            }
        });

        jButtonRequests.setText("Requests");
        jButtonRequests.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRequestsActionPerformed(evt);
            }
        });

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Stations"));

        jTableStationsTable.setAutoCreateRowSorter(true);
        jTableStationsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableStationsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableStationsTable.setShowGrid(true);
        jTableStationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableStationsTableMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(jTableStationsTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1059, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setTopComponent(jPanel1);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Station Info"));

        jLabel3.setText("Owner:");

        jComboBoxEquipment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxEquipment, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jComboBoxEquipment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel3))
        );

        jTableDatapointsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableDatapointsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableDatapointsTable.setShowGrid(true);
        jTableDatapointsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableDatapointsTableMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTableDatapointsTable);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1047, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTextAreaCalculation.setColumns(20);
        jTextAreaCalculation.setRows(5);
        jTextAreaCalculation.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTextAreaCalculationMousePressed(evt);
            }
        });
        jScrollPane4.setViewportView(jTextAreaCalculation);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        jLabel1.setText("Base URL:");

        jComboBoxBaseURLs.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButtonHistoryFrame.setText("History");
        jButtonHistoryFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHistoryFrameActionPerformed(evt);
            }
        });

        jLabel2.setText("secs:");

        jTogglePollForLiveData.setText("Poll for LiveData");
        jTogglePollForLiveData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTogglePollForLiveDataActionPerformed(evt);
            }
        });

        jLabelLoggedIn.setText("*loggedIn*");

        jButtonLogin.setText("Login");
        jButtonLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoginActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxBaseURLs, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonLogin)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelLoggedIn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonGetStations)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRequests))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonHistoryFrame)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTogglePollForLiveData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinnerPollInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonQuit))
                    .addComponent(jSplitPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonGetStations)
                    .addComponent(jButtonRequests)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxBaseURLs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelLoggedIn)
                    .addComponent(jButtonLogin))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonQuit)
                    .addComponent(jButtonHistoryFrame)
                    .addComponent(jSpinnerPollInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jTogglePollForLiveData))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonQuitActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButtonQuitActionPerformed

    private void jButtonGetStationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetStationsActionPerformed
        controller.getStations();
    }//GEN-LAST:event_jButtonGetStationsActionPerformed

    private void jTableDatapointsTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableDatapointsTableMousePressed

        if (evt.isPopupTrigger()) {
            PopupMenuForDataPointsTable popup = new PopupMenuForDataPointsTable(evt, jTableDatapointsTable);
        }
        
        int row = jTableDatapointsTable.rowAtPoint(evt.getPoint());
        int modelIndex = jTableDatapointsTable.convertRowIndexToModel(row);
        DatapointsTableModel mod = (DatapointsTableModel) jTableDatapointsTable.getModel();
        Datapoint dataPoint = mod.getRow(modelIndex);

        this.jTextAreaCalculation.setText(dataPoint.getCalculation());


    }//GEN-LAST:event_jTableDatapointsTableMousePressed

    private void jButtonRequestsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRequestsActionPerformed
        RRFrame frame = RRFrame.getInstance(controller);
        controller.addModelListener(frame);
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }//GEN-LAST:event_jButtonRequestsActionPerformed

    private void jTableStationsTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableStationsTableMousePressed

        killLivePollingTimer();
        this.jTogglePollForLiveData.setSelected(false);

        if (evt.isPopupTrigger()) {
            PopupMenuForDataPointsTable popup = new PopupMenuForDataPointsTable(evt, jTableStationsTable);
        }

        clearDatapointsTable();

        int row = jTableStationsTable.getSelectedRow();
        int modelIndex = jTableStationsTable.convertRowIndexToModel(row);
        StationsTableModel mod = (StationsTableModel) jTableStationsTable.getModel();
        selectedStation = mod.getRow(modelIndex);
        controller.getStationInfoAndSubscribedFlag(selectedStation.getId());


    }//GEN-LAST:event_jTableStationsTableMousePressed

    private void jTogglePollForLiveDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTogglePollForLiveDataActionPerformed

        if (jTogglePollForLiveData.isSelected()) {
            long sec = (int) jSpinnerPollInterval.getModel().getValue();
            long interval = 1000 * sec;
            long startDelay = 0;

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        DatapointsTableModel mod = (DatapointsTableModel) jTableDatapointsTable.getModel();
                        controller.getLiveData(mod.getSubscribedPoints());
                        System.out.println("polling...");
                    } catch (Exception ex) {
                        System.out.println("oops. something went wrong with the timer");
                    }
                }
            }, startDelay, interval);

            jSpinnerPollInterval.setEnabled(false);

        } else {
            if (timer != null) {
                timer.cancel();
            }
            jSpinnerPollInterval.setEnabled(true);
        }
    }//GEN-LAST:event_jTogglePollForLiveDataActionPerformed

    private void jButtonHistoryFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHistoryFrameActionPerformed

        if (selectedStation != null) {

            killLivePollingTimer();
            this.jTogglePollForLiveData.setSelected(false);

            HistoryFrame frame = HistoryFrame.getInstance(controller, selectedStation);
            controller.addModelListener(frame);
            frame.pack();
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);
        }
    }//GEN-LAST:event_jButtonHistoryFrameActionPerformed

    private void jButtonLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoginActionPerformed
        initModel(selectedBaseURL);
    }//GEN-LAST:event_jButtonLoginActionPerformed

    private void jTextAreaCalculationMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextAreaCalculationMousePressed
        if (evt.isPopupTrigger()) {
            PopupMenuForCalculations popup = new PopupMenuForCalculations(evt, jTextAreaCalculation);
        }
    }//GEN-LAST:event_jTextAreaCalculationMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonGetStations;
    private javax.swing.JButton jButtonHistoryFrame;
    private javax.swing.JButton jButtonLogin;
    private javax.swing.JButton jButtonQuit;
    private javax.swing.JButton jButtonRequests;
    private javax.swing.JComboBox<String> jComboBoxBaseURLs;
    private javax.swing.JComboBox<String> jComboBoxEquipment;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelLoggedIn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSpinner jSpinnerPollInterval;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTableDatapointsTable;
    private javax.swing.JTable jTableStationsTable;
    private javax.swing.JTextArea jTextAreaCalculation;
    private javax.swing.JToggleButton jTogglePollForLiveData;
    // End of variables declaration//GEN-END:variables

}
