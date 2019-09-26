package view.HistoryFrame.PushFromTeslaFrame;

import controller.Controller;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import model.DataPoints.StationInfo;
import model.DatapointList.DatapointListItem;
import model.EnumBaseURLs;
import model.EnumPrimarySecodaryClient;
import model.PropertyChangeNames;
import model.TTT.TTTMapStatus;
import model.TTT.TTTTableRow;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import view.HistoryFrame.PushE3OSDataFrame.MappingTable.PopupMenuForMappingTable;

import view.HistoryFrame.PushFromTeslaFrame.MappingsTable.EnumTTTMappingTableColumns;
import view.HistoryFrame.PushFromTeslaFrame.MappingsTable.TTTMappingTableCellRenderer;
import view.HistoryFrame.PushFromTeslaFrame.MappingsTable.TTTMappingTableModel;
import view.StationsTable.EnumStationsTableColumns;
import view.StationsTable.StationsTableCellRenderer;
import view.StationsTable.StationsTableModel;

public final class PushFromTeslaFrame extends javax.swing.JFrame implements PropertyChangeListener {

    private static PushFromTeslaFrame thisInstance;
    private final Controller controller;
    private final StationInfo toStationInfo;
    private final List<DatapointListItem> toStationDatapointList;

    private EnumBaseURLs secondaryBaseURL;
    private StationInfo fromStationInfo;
    private List<StationInfo> fromStationInfoList;
    private List<DatapointListItem> fromStationDatapointList;

    private Timer lapsedTimeTimer;
    private final ActionListener lapsedTimeUpdater;
    private DateTime teslaPushTimerStartTime;
    private int completedBatches = 0;
    private int totalBatchesToPush = 0;

    private String filter = "";
    private List<TTTTableRow> mappingTable;

    private final DateTimeFormatter zzFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    public static PushFromTeslaFrame getInstance(
            Controller controller,
            StationInfo toStationInfo,
            List<DatapointListItem> toStationDatapointList,
            DateTime startDate,
            DateTime endDate) {

        if (thisInstance == null) {
            thisInstance = new PushFromTeslaFrame(
                    controller,
                    toStationInfo,
                    toStationDatapointList,
                    startDate,
                    endDate);
        }
        return thisInstance;

    }

    private PushFromTeslaFrame(
            Controller controller,
            StationInfo toStationInfo,
            List<DatapointListItem> toStationDatapointList,
            DateTime startDate,
            DateTime endDate) {
        initComponents();

        this.controller = controller;
        this.toStationInfo = toStationInfo;
        this.toStationDatapointList = toStationDatapointList;
        this.jTextFieldStartDate.setText(startDate.toString(zzFormat));
        this.jTextFieldEndDate.setText(endDate.toString(zzFormat));

        secondaryBaseURL = EnumBaseURLs.Ninja;
        this.jLabelFromStationName.setText("TBD");
        this.jLabelToStationName.setText(toStationInfo.getName());
        this.jTextFieldMaxHoursPush.setText("12");
        this.jTextFieldMaxPointsPush.setText("50");
        this.jTextFieldFilter.setText("");

        lapsedTimeUpdater = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                DateTime tempTeslaPushTimerEndTime = DateTime.now();
                Period period = new Period(teslaPushTimerStartTime, tempTeslaPushTimerEndTime);
                String lapsedTimeString = String.format("%03d %02d:%02d:%02d", period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());
                jLabelStatus.setText(lapsedTimeString);
                lapsedTimeTimer.restart();
            }
        };
        
        fillAPIHosts();

    }

    public void fillAPIHosts() {
        ComboBoxModel comboBoxModel = new DefaultComboBoxModel(EnumBaseURLs.getURLs().toArray());
        this.jComboBoxFromHost.setModel(comboBoxModel);

        this.jComboBoxFromHost.setSelectedItem(secondaryBaseURL.getURL());

        this.jComboBoxFromHost.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                JComboBox<String> combo = (JComboBox<String>) event.getSource();
                String name = (String) combo.getSelectedItem();
                secondaryBaseURL = EnumBaseURLs.getHostFromName(name);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        clearStationsTable();
                    }
                });

            }
        });
    }

    public void clearStationsTable() {

        this.jTableStationsFrom.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        this.jTableStationsFrom.setModel(new DefaultTableModel());
        this.jTableStationsFrom.setAutoCreateRowSorter(true);
        fixStationsTableColumnWidths(jTableStationsFrom);

    }

    public void fillStationsTable() {
        this.jTableStationsFrom.setDefaultRenderer(Object.class, new StationsTableCellRenderer());
        this.jTableStationsFrom.setModel(new StationsTableModel(fromStationInfoList));
        fixStationsTableColumnWidths(jTableStationsFrom);
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

    private void createMappingsTable() {

        final String rawString = "raw";

        mappingTable = new ArrayList<>();
        for (DatapointListItem pt : toStationDatapointList) {
            if (pt.getPointType().contentEquals(rawString)) {
                TTTTableRow tempRow = new TTTTableRow();
                tempRow.setTo(pt);
                mappingTable.add(tempRow);
            }
        }

        for (DatapointListItem pt : fromStationDatapointList) {
            if (!pt.getPointType().contentEquals(rawString)) {
                continue;
            }

            boolean foundIt = false;
            for (TTTTableRow mappingTableRow : mappingTable) {

                if (mappingTableRow.getToName().equalsIgnoreCase(pt.getShortName())) {
                    mappingTableRow.setMapStatus(TTTMapStatus.Mapped);
                    mappingTableRow.setFromName(pt.getShortName());
                    mappingTableRow.setFromType(pt.getPointType());
                    mappingTableRow.setFromID(pt.getId());
                    foundIt = true;
                }

            }
            if (!foundIt) {
                TTTTableRow tempRow = new TTTTableRow();
                tempRow.setFrom(pt);
                mappingTable.add(tempRow);
            }
        }

    }

    private void clearMappingsTable() {
        this.jTableMappings.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        this.jTableMappings.setModel(new DefaultTableModel());
    }

    private void fillMappingsTable(String filter) {

        List<TTTTableRow> filteredList = new ArrayList<>();

        String[] pointNamesInFilter = filter.split(" ");

        for (TTTTableRow mappingTableRow : mappingTable) {

            if (filter.length() == 0) {
                filteredList.add(mappingTableRow);
                continue;
            }

            for (String pointNameFilter : Arrays.asList(pointNamesInFilter)) {
                if (!this.jCheckBoxRegEx.isSelected() && (mappingTableRow.getFromName().contains(pointNameFilter) || mappingTableRow.getToName().contains(pointNameFilter))) {
                    if (!filteredList.contains(mappingTableRow)) {
                        filteredList.add(mappingTableRow);
                    }
                } else if (this.jCheckBoxRegEx.isSelected()) {
                    Pattern r = Pattern.compile(pointNameFilter);
                    Matcher m1 = r.matcher(mappingTableRow.getFromName());
                    if (m1.find()) {
                        if (!filteredList.contains(mappingTableRow)) {
                            filteredList.add(mappingTableRow);
                        }
                    }
                    Matcher m2 = r.matcher(mappingTableRow.getToName());
                    if (m2.find()) {
                        if (!filteredList.contains(mappingTableRow)) {
                            filteredList.add(mappingTableRow);
                        }
                    }
                }
            }
        }

        this.jTableMappings.setDefaultRenderer(Object.class, new TTTMappingTableCellRenderer());
        this.jTableMappings.setModel(new TTTMappingTableModel(filteredList));
        this.jTableMappings.setAutoCreateRowSorter(true);
        fixMappintTableColumns(jTableMappings);

    }

    public void fixMappintTableColumns(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            EnumTTTMappingTableColumns colEnum = EnumTTTMappingTableColumns.getColumnFromColumnNumber(i);
            TableColumn column = t.getColumnModel().getColumn(i);
            if (colEnum != null) {
                column.setPreferredWidth(colEnum.getWidth());
            } else {
                column.setPreferredWidth(50);
            }
        }
    }

    @Override
    public void dispose() {
        if (lapsedTimeTimer != null) {
            lapsedTimeTimer.stop();
            lapsedTimeTimer = null;
        }
        controller.removePropChangeListener(this);
        thisInstance = null;
        super.dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldStartDate = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldEndDate = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldMaxHoursPush = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextFieldMaxPointsPush = new javax.swing.JTextField();
        jLabelFromStationName = new javax.swing.JLabel();
        jLabelToStationName = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableStationsFrom = new javax.swing.JTable();
        jComboBoxFromHost = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jButtonFromHostLogin = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableMappings = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldFilter = new javax.swing.JTextField();
        jCheckBoxRegEx = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jButtonPushData = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jProgressBarPush = new javax.swing.JProgressBar();
        jButtonClose = new javax.swing.JButton();
        jLabelStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Push Data From Tesla");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "From / To"));

        jLabel3.setText("Push data from: ");

        jLabel5.setText("To:");

        jLabel7.setText("Start Date:");

        jTextFieldStartDate.setText("jTextField1");

        jLabel8.setText("End Date:");

        jTextFieldEndDate.setText("jTextField2");

        jLabel9.setText("Max Hours / Push:");

        jTextFieldMaxHoursPush.setText("jTextField3");

        jLabel10.setText("Max # Points / Push:");

        jTextFieldMaxPointsPush.setText("jTextField4");

        jLabelFromStationName.setText("*from*");

        jLabelToStationName.setText("*to*");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelFromStationName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelToStationName))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldMaxHoursPush, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldMaxPointsPush, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(jLabelFromStationName)
                    .addComponent(jLabelToStationName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextFieldStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jTextFieldEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextFieldMaxHoursPush, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jTextFieldMaxPointsPush, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Existing Tesla Sites to pull FROM"));

        jTableStationsFrom.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableStationsFrom.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableStationsFrom.setShowGrid(true);
        jTableStationsFrom.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableStationsFromMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTableStationsFrom);

        jComboBoxFromHost.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("From Host:");

        jButtonFromHostLogin.setText("Login");
        jButtonFromHostLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFromHostLoginActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 869, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxFromHost, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonFromHostLogin)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxFromHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jButtonFromHostLogin))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setTopComponent(jPanel2);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Point Mappings"));

        jTableMappings.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableMappings.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableMappings.setShowGrid(true);
        jTableMappings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableMappingsMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(jTableMappings);

        jLabel2.setText("Filter:");

        jTextFieldFilter.setText("jTextField1");
        jTextFieldFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilterActionPerformed(evt);
            }
        });

        jCheckBoxRegEx.setText("Use RegEx");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 859, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxRegEx)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jCheckBoxRegEx))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel3);

        jButtonPushData.setText("Push Data");
        jButtonPushData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPushDataActionPerformed(evt);
            }
        });

        jLabel1.setText("Progress:");

        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        jLabelStatus.setText("*status*");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jButtonPushData)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBarPush, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonClose)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonClose)
                        .addComponent(jLabelStatus))
                    .addComponent(jProgressBarPush, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonPushData)
                        .addComponent(jLabel1)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSplitPane1)
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

    private void jButtonPushDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPushDataActionPerformed

        if (jTableMappings.getSelectedRowCount() > 0) {

            int maxHoursPerPush = Integer.parseInt(this.jTextFieldMaxHoursPush.getText());
            int maxPointsPerPush = Integer.parseInt(this.jTextFieldMaxPointsPush.getText());

            TTTMappingTableModel model = (TTTMappingTableModel) this.jTableMappings.getModel();
            int[] rowNumbers = jTableMappings.getSelectedRows();
            List<TTTTableRow> pushRows = new ArrayList<>();
            for (int selectedRowNumber : rowNumbers) {
                int modelIndex = jTableMappings.convertRowIndexToModel(selectedRowNumber);
                TTTTableRow mappedRow = model.getRow(modelIndex);

                if (mappedRow.getMapStatus() == TTTMapStatus.Mapped) {
                    pushRows.add(mappedRow);
                }
            }

            DateTime pushStartTime = DateTime.parse(jTextFieldStartDate.getText(), zzFormat).withZone(DateTimeZone.UTC);
            DateTime pushEndTime = DateTime.parse(jTextFieldEndDate.getText(), zzFormat).withZone(DateTimeZone.UTC);

            //endOfPeriod is the number of whole hours between the startDate and the endDate.
            Hours hours = Hours.hoursBetween(pushStartTime, pushEndTime);
            int totalNumberOfHoursToPush = hours.getHours();
            int totalNumberOfPointsToPush = pushRows.size();
            maxPointsPerPush = Math.min(maxPointsPerPush, pushRows.size());

            int numHourGroups = (totalNumberOfHoursToPush + 1) / maxHoursPerPush;
            int numPointGroups = (totalNumberOfPointsToPush + 1) / maxPointsPerPush;

            totalBatchesToPush = numHourGroups * numPointGroups;

            jProgressBarPush.setMaximum(100);
            jProgressBarPush.setValue(0);
            jProgressBarPush.setStringPainted(true);

            this.jButtonPushData.setEnabled(false);
            teslaPushTimerStartTime = DateTime.now();
            lapsedTimeTimer = new Timer(1000, lapsedTimeUpdater);
            lapsedTimeTimer.start();

            controller.pullFromTeslsPushToTesla(EnumPrimarySecodaryClient.Secondary, pushStartTime, pushEndTime, pushRows, maxHoursPerPush, maxPointsPerPush, toStationInfo.getTimeZone());

        }

    }//GEN-LAST:event_jButtonPushDataActionPerformed

    private void jTableStationsFromMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableStationsFromMousePressed
        clearMappingsTable();

        int row = jTableStationsFrom.getSelectedRow();
        int modelIndex = jTableStationsFrom.convertRowIndexToModel(row);
        StationsTableModel mod = (StationsTableModel) jTableStationsFrom.getModel();
        this.fromStationInfo = mod.getRow(modelIndex);

        this.jLabelFromStationName.setText(fromStationInfo.getName());

        controller.getDatapoints(EnumPrimarySecodaryClient.Secondary, fromStationInfo.getId());

    }//GEN-LAST:event_jTableStationsFromMousePressed

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButtonCloseActionPerformed

    private void jTableMappingsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableMappingsMousePressed
        if (evt.isPopupTrigger()) {
            PopupMenuForMappingTable popup = new PopupMenuForMappingTable(evt, jTableMappings);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jTableMappingsMousePressed

    private void jTextFieldFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilterActionPerformed
        this.filter = jTextFieldFilter.getText();
        fillMappingsTable(this.filter);
    }//GEN-LAST:event_jTextFieldFilterActionPerformed

    private void jButtonFromHostLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFromHostLoginActionPerformed
        controller.fromLogin(secondaryBaseURL);
    }//GEN-LAST:event_jButtonFromHostLoginActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonFromHostLogin;
    private javax.swing.JButton jButtonPushData;
    private javax.swing.JCheckBox jCheckBoxRegEx;
    private javax.swing.JComboBox<String> jComboBoxFromHost;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelFromStationName;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel jLabelToStationName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JProgressBar jProgressBarPush;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTableMappings;
    private javax.swing.JTable jTableStationsFrom;
    private javax.swing.JTextField jTextFieldEndDate;
    private javax.swing.JTextField jTextFieldFilter;
    private javax.swing.JTextField jTextFieldMaxHoursPush;
    private javax.swing.JTextField jTextFieldMaxPointsPush;
    private javax.swing.JTextField jTextFieldStartDate;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        
        //
        if (propName.equals(PropertyChangeNames.SecondaryLoginResponseReturned.getName())) {
            controller.getStations(EnumPrimarySecodaryClient.Secondary);

        } else if (propName.equals(PropertyChangeNames.SecondaryStationsListReturned.getName())) {
            fromStationInfoList = (List<StationInfo>) evt.getNewValue();
            fillStationsTable();

        } else if (propName.equals(PropertyChangeNames.DatapointsReturned.getName())) {
            fromStationDatapointList = (List<DatapointListItem>) evt.getNewValue();
            createMappingsTable();
            fillMappingsTable(this.filter);

        } else if (propName.equals(PropertyChangeNames.TeslaBucketPushed.getName())) {
            completedBatches += 1;
            double percComplete = (double) completedBatches / (double) totalBatchesToPush;
            percComplete *= 100;

            int count = (int) percComplete;

            jProgressBarPush.setValue(Math.min(count, jProgressBarPush.getMaximum()));

        } else if (propName.equals(PropertyChangeNames.TeslaPushComplete.getName())) {
            jProgressBarPush.setBackground(Color.GREEN);
            jProgressBarPush.invalidate();
            jProgressBarPush.repaint();

            if (lapsedTimeTimer != null) {
                lapsedTimeTimer.stop();
            }

            DateTime historyPusTimerEnd = DateTime.now();
            Period period = new Period(teslaPushTimerStartTime, historyPusTimerEnd);
            System.out.println("lapsed time: " + PeriodFormat.getDefault().print(period));
            System.out.println(String.format("%02d:%02d:%02d", period.getHours(), period.getMinutes(), period.getSeconds()));

            String lapsedTimeString = String.format("%03d %02d:%02d:%02d", period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());
            jLabelStatus.setText(lapsedTimeString);

            Object[] options = {"OK"};
            JOptionPane.showOptionDialog(null,
                    String.format("Lapsed time: %03d days %02d:%02d:%02d", period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds()),
                    "Done!",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);

            this.dispose();

        } else if (propName.equals(PropertyChangeNames.PrimaryLoginResponseReturned.getName())) {
            this.dispose();
        }
    }
}
