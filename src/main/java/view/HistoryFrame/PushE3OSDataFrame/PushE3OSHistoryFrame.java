package view.HistoryFrame.PushE3OSDataFrame;

import controller.Controller;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.TableColumn;
import model.DatapointList.DatapointListItem;
import model.E3OS.LoadFromE3OS.DataPointFromSql;
import model.E3OS.LoadFromE3OS.E3OSStationRecord;
import model.E3OS.LoadFromE3OS.EnumMapStatus;
import model.E3OS.LoadFromE3OS.MappingTableRow;
import model.PropertyChangeNames;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import view.HistoryFrame.PushE3OSDataFrame.MappingTable.EnumMappingTableColumns;
import view.HistoryFrame.PushE3OSDataFrame.MappingTable.MappingTableCellRenderer;
import view.HistoryFrame.PushE3OSDataFrame.MappingTable.MappingTableModel;
import view.HistoryFrame.PushE3OSDataFrame.MappingTable.PopupMenuForMappingTable;
import view.HistoryFrame.PushE3OSDataFrame.SitesTable.E3OSSitesTableCellRenderer;
import view.HistoryFrame.PushE3OSDataFrame.SitesTable.E3OSSitesTableModel;
import view.HistoryFrame.PushE3OSDataFrame.SitesTable.EnumSitesTableColumns;
import view.HistoryFrame.PushE3OSDataFrame.SitesTable.PopupMenuForE3OSSitesTable;

public class PushE3OSHistoryFrame extends javax.swing.JFrame implements PropertyChangeListener {

    private static PushE3OSHistoryFrame thisInstance;
    private final Controller controller;
    private List<E3OSStationRecord> sitesList;
    private final List<DatapointListItem> datapointsList;

    private Timer lapsedTimeTimer;
    private final ActionListener lapsedTimeUpdater;
    private DateTime teslaPushTimerStartTime;
    private int completedBatches = 0;
    private int totalBatchesToPush = 0;

    private String mappingFilter = "";
    private List<MappingTableRow> mappingTable;

    private final DateTimeFormatter zzFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    public static PushE3OSHistoryFrame getInstance(
            final Controller controller,
            DateTime startDate,
            DateTime endDate,
            List<DatapointListItem> datapointsList) {

        if (thisInstance == null) {
            thisInstance = new PushE3OSHistoryFrame(
                    controller,
                    startDate,
                    endDate,
                    datapointsList);
        }
        return thisInstance;

    }

    private PushE3OSHistoryFrame(final Controller controller,
            DateTime startDate,
            DateTime endDate,
            List<DatapointListItem> datapointsList) {
        initComponents();

        this.controller = controller;
        this.datapointsList = datapointsList;

        this.jTextFieldStartDate.setText(startDate.toString(zzFormat));
        this.jTextFieldEndDate.setText(endDate.toString(zzFormat));

        this.jTextFieldMaxHoursPush.setText("12");
        this.jTextFieldMaxPointsPush.setText("50");
        this.jTextFieldSitesFilter.setText("");
        this.jTextFieldMappingFilter.setText("");

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

        controller.getE3OSSites();

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

    private void fillSitesTable(String filter) {

        List<E3OSStationRecord> filteredList = new ArrayList<>();

        String[] pointNamesInFilter = filter.split(" ");

        for (E3OSStationRecord stationRecord : this.sitesList) {

            if (filter.length() == 0) {
                filteredList.add(stationRecord);
                continue;
            }

            for (String filterPiece : Arrays.asList(pointNamesInFilter)) {

                if (matchesFilter(stationRecord.GetCustomerName(), filterPiece)
                        || matchesFilter(stationRecord.GetSiteShortName(), filterPiece)
                        || matchesFilter(stationRecord.GetInstShortName(), filterPiece)
                        || matchesFilter(stationRecord.GetStationShortName(), filterPiece)) {

                    if (!filteredList.contains(stationRecord)) {
                        filteredList.add(stationRecord);
                    }

                }

            }
        }

        this.jTableE3OSSites.setDefaultRenderer(Object.class, new E3OSSitesTableCellRenderer());
        this.jTableE3OSSites.setModel(new E3OSSitesTableModel(filteredList));
        this.jTableE3OSSites.setAutoCreateRowSorter(true);
        fixSitesTableDataPointsListColumns(jTableE3OSSites);
    }

    public boolean matchesFilter(String recordText, String filterPiece) {

        if (!this.jCheckBoxRegEx.isSelected() && recordText.contains(filterPiece)) {
            return true;
        } else if (this.jCheckBoxRegEx.isSelected()) {
            Pattern r = Pattern.compile(filterPiece);
            Matcher m = r.matcher(recordText);
            if (m.find()) {
                return true;
            }
        }

        return false;
    }

    public void fixSitesTableDataPointsListColumns(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            EnumSitesTableColumns colEnum = EnumSitesTableColumns.getColumnFromColumnNumber(i);
            TableColumn column = t.getColumnModel().getColumn(i);
            if (colEnum != null) {
                column.setPreferredWidth(colEnum.getWidth());
            } else {
                column.setPreferredWidth(50);
            }
        }
    }

    private void createMappingsTable(List<DataPointFromSql> e3osPoints) {

        Map<String, String> overrideE3osName = getOverrideMap();

        //add core points
        mappingTable = new ArrayList<>();
        for (DatapointListItem pt : datapointsList) {
            mappingTable.add(new MappingTableRow(pt));
        }

        for (DataPointFromSql e3osPoint : e3osPoints) {
            boolean foundIt = false;

            for (MappingTableRow mappingTableRow : mappingTable) {

                if (overrideE3osName.containsKey(mappingTableRow.getTeslaName())) {
                    mappingTableRow.setMapStatus(EnumMapStatus.Mapped);
                    mappingTableRow.setE3osName(overrideE3osName.get(mappingTableRow.getTeslaName()));
                    mappingTableRow.setXid(e3osPoint);
                    foundIt = true;
                } else if (mappingTableRow.getTeslaName().equalsIgnoreCase(e3osPoint.getDatapointName())) {
                    mappingTableRow.setMapStatus(EnumMapStatus.Mapped);
                    mappingTableRow.setE3osName(e3osPoint.getDatapointName());
                    mappingTableRow.setXid(e3osPoint);
                    foundIt = true;
                }
            }
            if (!foundIt) {
                mappingTable.add(new MappingTableRow(e3osPoint));
            }
        }

    }

    private Map<String, String> getOverrideMap() {

        Map<String, String> map = new HashMap<>();

        map.put("CDWP1SPDNotOptimized", "CDWP1SPD_Alarm");
        map.put("CDWP2SPDNotOptimized", "CDWP2SPD_Alarm");
        map.put("CDWP3SPDNotOptimized", "CDWP3SPD_Alarm");

        map.put("CT4SPDNotOptimized", "CT4SPD_Alarm");
        map.put("CT3SPDNotOptimized", "CT3SPD_Alarm");
        map.put("CT2SPDNotOptimized", "CT2SPD_Alarm");
        map.put("CT1SPDNotOptimized", "CT1SPD_Alarm");

        map.put("PCHWP3SPDNotOptimized", "PCHWP3SPD_Alarm");
        map.put("PCHWP2SPDNotOptimized", "PCHWP2SPD_Alarm");
        map.put("PCHWP1SPDNotOptimized", "PCHWP1SPD_Alarm");
        map.put("BASCommunicationFailure", "commfail");
        map.put("EDGEMODE", "LOOPREQ");
        map.put("EDGEREADY", "OECREADY");
        map.put("CH1CHWSTSPNotOptimized", "CH1_CHWSTSP_Alarm");
        map.put("CH2CHWSTSPNotOptimized", "CH2_CHWSTSP_Alarm");

        return map;

    }

    private boolean matchesOverride(DataPointFromSql e3osPoint, MappingTableRow mappingTableRow) {

        String e3osFilter = "SPD_Alarm";
        String coreFilter = "SPDNotOptimized";

        if (mappingTableRow.getTeslaName().contains(coreFilter)) {
            System.out.println("here");
        }

        if (e3osPoint.getDatapointName().contains(e3osFilter)) {
            System.out.println("here");
        }

        return (mappingTableRow.getTeslaName().contains(coreFilter)
                && e3osPoint.getDatapointName().contains(e3osFilter)
                && mappingTableRow.getMapStatus() != EnumMapStatus.Mapped);

    }

    private void fillMappingsTable(String filter) {

        List<MappingTableRow> filteredList = new ArrayList<>();

        String[] pointNamesInFilter = filter.split(" ");

        for (MappingTableRow mappingTableRow : mappingTable) {

            if (filter.length() == 0) {
                filteredList.add(mappingTableRow);
                continue;
            }

            for (String pointNameFilter : Arrays.asList(pointNamesInFilter)) {
                if (!this.jCheckBoxMappingRexEx.isSelected() && (mappingTableRow.getE3osName().contains(pointNameFilter) || mappingTableRow.getTeslaName().contains(pointNameFilter))) {
                    if (!filteredList.contains(mappingTableRow)) {
                        filteredList.add(mappingTableRow);
                    }
                } else if (this.jCheckBoxMappingRexEx.isSelected()) {
                    Pattern r = Pattern.compile(pointNameFilter);
                    Matcher m1 = r.matcher(mappingTableRow.getE3osName());
                    if (m1.find()) {
                        if (!filteredList.contains(mappingTableRow)) {
                            filteredList.add(mappingTableRow);
                        }
                    }
                    Matcher m2 = r.matcher(mappingTableRow.getTeslaName());
                    if (m2.find()) {
                        if (!filteredList.contains(mappingTableRow)) {
                            filteredList.add(mappingTableRow);
                        }
                    }
                }
            }
        }

        this.jTableMapping.setDefaultRenderer(Object.class, new MappingTableCellRenderer());
        this.jTableMapping.setModel(new MappingTableModel(filteredList));
        this.jTableMapping.setAutoCreateRowSorter(true);
        fixTableDataPointsListColumns(jTableMapping);

    }

    public void fixTableDataPointsListColumns(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            EnumMappingTableColumns colEnum = EnumMappingTableColumns.getColumnFromColumnNumber(i);
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
        jLabel2 = new javax.swing.JLabel();
        jTextFieldStartDate = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldEndDate = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldMaxHoursPush = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldMaxPointsPush = new javax.swing.JTextField();
        jTextFieldSitesFilter = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxRegEx = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jButtonPushData = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jProgressBarPush = new javax.swing.JProgressBar();
        jButtonClose = new javax.swing.JButton();
        jLabelStatus = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableMapping = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldMappingFilter = new javax.swing.JTextField();
        jCheckBoxMappingRexEx = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableE3OSSites = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Push E3OS Data");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Parameters"));

        jLabel2.setText("Start Date:");

        jTextFieldStartDate.setText("jTextField1");

        jLabel3.setText("End Date:");

        jTextFieldEndDate.setText("jTextField2");

        jLabel4.setText("Max Hours / Push:");

        jTextFieldMaxHoursPush.setText("jTextField3");

        jLabel5.setText("Max # Points / Push:");

        jTextFieldMaxPointsPush.setText("jTextField4");

        jTextFieldSitesFilter.setText("jTextField1");
        jTextFieldSitesFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldSitesFilterActionPerformed(evt);
            }
        });

        jLabel1.setText("Sites Filter:");

        jCheckBoxRegEx.setText("RegEx");

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
                        .addComponent(jTextFieldStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldMaxHoursPush, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldMaxPointsPush, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(64, 64, 64)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldSitesFilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxRegEx)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldMaxHoursPush, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jTextFieldMaxPointsPush, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldSitesFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jCheckBoxRegEx)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jButtonPushData.setText("Push Data");
        jButtonPushData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPushDataActionPerformed(evt);
            }
        });

        jLabel6.setText("Progress:");

        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        jLabelStatus.setText("*status*");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonPushData)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBarPush, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonClose)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jProgressBarPush, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonPushData)
                        .addComponent(jLabel6))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonClose)
                        .addComponent(jLabelStatus)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "E3OS Stations"));
        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Mapping"));

        jTableMapping.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableMapping.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableMapping.setShowGrid(true);
        jTableMapping.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableMappingMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTableMapping);

        jLabel7.setText("Filter:");

        jTextFieldMappingFilter.setText("jTextField1");
        jTextFieldMappingFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldMappingFilterActionPerformed(evt);
            }
        });

        jCheckBoxMappingRexEx.setText("Use RegEx");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1026, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMappingFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxMappingRexEx)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextFieldMappingFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxMappingRexEx))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
        );

        jSplitPane1.setBottomComponent(jPanel2);

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
        jTableE3OSSites.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableE3OSSitesMousePressed(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableE3OSSitesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTableE3OSSites);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1036, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSplitPane1))
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
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButtonCloseActionPerformed

    private void jButtonPushDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPushDataActionPerformed

        if (jTableMapping.getSelectedRowCount() > 0) {

            int maxHoursPerPush = Integer.parseInt(this.jTextFieldMaxHoursPush.getText());
            int maxPointsPerPush = Integer.parseInt(this.jTextFieldMaxPointsPush.getText());

            MappingTableModel model = (MappingTableModel) this.jTableMapping.getModel();
            int[] rowNumbers = jTableMapping.getSelectedRows();
            List<MappingTableRow> pushRows = new ArrayList<>();
            for (int selectedRowNumber : rowNumbers) {
                int modelIndex = jTableMapping.convertRowIndexToModel(selectedRowNumber);
                MappingTableRow mappedRow = model.getRow(modelIndex);

                if (mappedRow.getMapStatus() == EnumMapStatus.Mapped) {
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

            controller.pullFromE3OSPushToTesla(pushStartTime, pushEndTime, pushRows, maxHoursPerPush, maxPointsPerPush);
        }
    }//GEN-LAST:event_jButtonPushDataActionPerformed

    private void jTableMappingMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableMappingMousePressed
        if (evt.isPopupTrigger()) {
            PopupMenuForMappingTable popup = new PopupMenuForMappingTable(evt, jTableMapping);
        }
    }//GEN-LAST:event_jTableMappingMousePressed

    private void jTableE3OSSitesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableE3OSSitesMousePressed
        if (evt.isPopupTrigger()) {
            PopupMenuForE3OSSitesTable popup = new PopupMenuForE3OSSitesTable(evt, jTableMapping);
        }
    }//GEN-LAST:event_jTableE3OSSitesMousePressed

    private void jTableE3OSSitesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableE3OSSitesMouseClicked
        if (evt.getClickCount() == 2) {
            int row = jTableE3OSSites.rowAtPoint(evt.getPoint());
            int modelIndex = jTableE3OSSites.convertRowIndexToModel(row);
            E3OSSitesTableModel mod = (E3OSSitesTableModel) jTableE3OSSites.getModel();
            E3OSStationRecord e3osStationRecord = mod.getRow(modelIndex);
            int stationId = e3osStationRecord.GetStationId();
            controller.getE3OSDatapoints(Integer.toString(stationId));
        }
    }//GEN-LAST:event_jTableE3OSSitesMouseClicked

    private void jTextFieldSitesFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldSitesFilterActionPerformed
        String filter = this.jTextFieldSitesFilter.getText();
        fillSitesTable(filter);
    }//GEN-LAST:event_jTextFieldSitesFilterActionPerformed

    private void jTextFieldMappingFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldMappingFilterActionPerformed
        this.mappingFilter = jTextFieldMappingFilter.getText();
        fillMappingsTable(this.mappingFilter);
    }//GEN-LAST:event_jTextFieldMappingFilterActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonPushData;
    private javax.swing.JCheckBox jCheckBoxMappingRexEx;
    private javax.swing.JCheckBox jCheckBoxRegEx;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JProgressBar jProgressBarPush;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTableE3OSSites;
    private javax.swing.JTable jTableMapping;
    private javax.swing.JTextField jTextFieldEndDate;
    private javax.swing.JTextField jTextFieldMappingFilter;
    private javax.swing.JTextField jTextFieldMaxHoursPush;
    private javax.swing.JTextField jTextFieldMaxPointsPush;
    private javax.swing.JTextField jTextFieldSitesFilter;
    private javax.swing.JTextField jTextFieldStartDate;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (propName.equals(PropertyChangeNames.E3OSSitesReturned.getName())) {
            this.sitesList = (List<E3OSStationRecord>) evt.getNewValue();
            fillSitesTable(this.jTextFieldSitesFilter.getText());

        } else if (propName.equals(PropertyChangeNames.E3OSPointsReturned.getName())) {
            List<DataPointFromSql> e3osPoints = (List<DataPointFromSql>) evt.getNewValue();
            createMappingsTable(e3osPoints);
            fillMappingsTable(this.mappingFilter);

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
