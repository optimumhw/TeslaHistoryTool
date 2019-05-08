package view.HistoryFrame;

import controller.Controller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import model.DataPoints.Datapoint;
import model.DataPoints.EnumResolutions;
import model.DataPoints.HistoryQueryResults;
import model.DataPoints.HistoryRequest;
import model.DataPoints.StationInfo;
import model.DatapointList.DatapointListItem;
import model.PropertyChangeNames;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import view.DataGenerator.DataGeneratorFrame;
import view.DataPointsTable.EnumDatpointsTableColumns;
import view.HistoryFrame.Chart.HistoryChartFrame;
import view.HistoryFrame.DatapointsListTable.DataPointsListTableCellRenderer;
import view.HistoryFrame.DatapointsListTable.DataPointsListTableModel;
import view.HistoryFrame.DatapointsListTable.EnumDataPointsListTableColumns;
import view.HistoryFrame.DatapointsListTable.PopupMenuForDataPointsListTable;
import view.HistoryFrame.HistoryStatsTable.HistoryStatsTableCellRenderer;
import view.HistoryFrame.HistoryStatsTable.HistoryStatsTableModel;
import view.HistoryFrame.HistoryStatsTable.PopupMenuHistoryStatsTable;
import view.HistoryFrame.HistoryStatsTable.Statistics;
import view.HistoryFrame.HistoryTable.HistoryTableCellRenderer;
import view.HistoryFrame.HistoryTable.HistoryTableModel;
import view.HistoryFrame.HistoryTable.PopupMenuForHistoryTable;

public final class HistoryFrame extends javax.swing.JFrame implements PropertyChangeListener {

    private static HistoryFrame thisInstance;

    private final Controller controller;
    private final StationInfo selectedStation;
    private List<DatapointListItem> datapointsList;

    private HistoryQueryResults history;
    private Statistics historyStats;

    private final String timeZone = "UTC";
    private final DateTimeFormatter zzFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    private DateTime utcStartDate;
    private DateTime utcEndDate;

    private EnumQueryPeriods queryPeriod;

    private String filter = "";

    public static HistoryFrame getInstance(final Controller controller, StationInfo selectedStation) {
        if (thisInstance == null) {
            thisInstance = new HistoryFrame(controller, selectedStation);
        }
        return thisInstance;
    }

    private HistoryFrame(Controller controller, StationInfo selectedStation) {
        initComponents();

        this.controller = controller;
        this.selectedStation = selectedStation;

        queryPeriod = EnumQueryPeriods.LAST_30_DAYS;

        setStartAndEndDates(queryPeriod);


        fillQueryPeriodsDropDown(queryPeriod);

        setPrecSpinner();
        fillHistoryResolutionDropdown();
        controller.getDatapoints(selectedStation.getId());

    }

    @Override
    public void dispose() {
        controller.removePropChangeListener(this);
        thisInstance = null;
        super.dispose();
    }

    private void setPrecSpinner() {
        SpinnerNumberModel spinModel = new SpinnerNumberModel(3, 0, 6, 1);
        this.jSpinnerPrec.setModel(spinModel);
        jSpinnerPrec.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                if (history != null && history.getTimestamps().size() > 0) {
                    int prec = (int) jSpinnerPrec.getModel().getValue();
                    fillHistoryTable(prec);
                    fillHistoryStatsTable(prec);
                }
            }
        });
    }



    private void fillQueryPeriodsDropDown(EnumQueryPeriods initPeriod) {

        ComboBoxModel comboBoxModel = new DefaultComboBoxModel(EnumQueryPeriods.getQueryPeriodNames().toArray());
        this.jComboBoxQueryPeriods.setModel(comboBoxModel);
        this.jComboBoxQueryPeriods.setSelectedIndex(initPeriod.getDropDownIndex());
        this.jComboBoxQueryPeriods.setEnabled(true);

        this.jComboBoxQueryPeriods.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                JComboBox<String> combo = (JComboBox<String>) event.getSource();
                String name = (String) combo.getSelectedItem();
                queryPeriod = EnumQueryPeriods.getQueryPeriodFromName(name);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setStartAndEndDates(queryPeriod);
                    }
                });

            }
        });
    }

    private void setStartAndEndDates(EnumQueryPeriods queryPeriod) {

        /*
    LAST_12_MONTHS("Last 12 Months", 0),
    THIS_YEAR("This Year", 1),
    LAST_30_DAYS("Last 30 Days", 2),
    THIS_MONTH("This Month", 3),
    LAST_7_DAYS("Last 7 Days", 4),
    THIS_WEEK("This Week", 5),
    LAST_24_HOURS("Last 24 Hours", 6),
    TODAY("Today", 7);
         */
        DateTime today = DateTime.now().withZone(DateTimeZone.UTC);

        //Calendar cal = Calendar.getInstance();
        //cal.set(Calendar.MONTH, month.getMonthNumber());
        //cal.set(Calendar.YEAR, year.getYearNumber());
        //int numOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        switch (queryPeriod) {
            case LAST_12_MONTHS: {
                utcEndDate = today;
                utcStartDate = today.minusMonths(12);
            }
            break;
            case THIS_YEAR: {
                utcEndDate = today;
                utcStartDate = utcEndDate.minusDays(today.getDayOfYear() - 1);
                utcStartDate = utcStartDate.minusMillis(utcStartDate.getMillisOfDay());

            }
            break;
            case LAST_30_DAYS: {
                utcEndDate = today;
                utcStartDate = utcEndDate.minusDays(30);
                utcStartDate = utcStartDate.minusMillis(utcStartDate.getMillisOfDay());

            }
            break;
            case THIS_MONTH: {
                utcEndDate = today;
                utcStartDate = utcEndDate.minusDays(utcEndDate.getDayOfMonth() - 1);
                utcStartDate = utcStartDate.minusMillis(utcStartDate.getMillisOfDay());
            }
            break;
            case LAST_7_DAYS: {
                utcEndDate = today;
                utcStartDate = utcEndDate.minusDays(7);
                utcStartDate = utcStartDate.minusMillis(utcStartDate.getMillisOfDay());
            }
            break;

            case THIS_WEEK: {
                utcEndDate = today;
                utcStartDate = utcEndDate.minusDays(utcEndDate.getDayOfWeek());
                utcStartDate = utcStartDate.minusMillis(utcStartDate.getMillisOfDay());
            }
            break;

            case LAST_24_HOURS: {
                utcEndDate = today;
                utcStartDate = utcEndDate.minusDays(1);

            }
            break;

            case TODAY: {
                utcEndDate = today;
                utcStartDate = utcEndDate.minusMillis(utcEndDate.getMillisOfDay());

            }
        }

        this.jTextFieldStartDate.setText(utcStartDate.toString());
        this.jTextFieldEndDate.setText(utcEndDate.toString());

    }

    public void fillDataPointsListTable(String filter) {

        List<DatapointListItem> filteredList = new ArrayList<>();

        String[] pointNamesInFilter = filter.split(" ");

        for (DatapointListItem point : datapointsList) {

            if (filter.length() == 0) {
                filteredList.add(point);
                continue;
            }

            for (String pointNameFilter : Arrays.asList(pointNamesInFilter)) {
                if (!this.jCheckBoxRegEx.isSelected() && point.getShortName().contains(pointNameFilter)) {
                    if (!filteredList.contains(point)) {
                        filteredList.add(point);
                    }
                } else if (this.jCheckBoxRegEx.isSelected()) {
                    Pattern r = Pattern.compile(pointNameFilter);
                    Matcher m = r.matcher(point.getShortName());
                    if (m.find()) {
                        if (!filteredList.contains(point)) {
                            filteredList.add(point);
                        }
                    }
                }
            }
        }

        this.jTableDataPointsList.setDefaultRenderer(Object.class, new DataPointsListTableCellRenderer());
        this.jTableDataPointsList.setModel(new DataPointsListTableModel(filteredList));
        this.jTableDataPointsList.setAutoCreateRowSorter(true);
        fixTableDataPointsListColumns(jTableDataPointsList);

    }

    public void fixTableDataPointsListColumns(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            EnumDataPointsListTableColumns colEnum = EnumDataPointsListTableColumns.getColumnFromColumnNumber(i);
            TableColumn column = t.getColumnModel().getColumn(i);
            if (colEnum != null) {
                column.setPreferredWidth(colEnum.getWidth());
            } else {
                column.setPreferredWidth(50);
            }
        }
    }

    private void fillHistoryResolutionDropdown() {
        ComboBoxModel comboBoxModel = new DefaultComboBoxModel(EnumResolutions.getNames().toArray());
        EnumResolutions res = EnumResolutions.FIVEMINUTE;
        this.jComboBoxResolutions.setModel(comboBoxModel);
        this.jComboBoxResolutions.setSelectedIndex(res.getDropDownIndex());
        this.jComboBoxResolutions.setEnabled(true);
    }

    public void clearHistoryTable() {

        this.jTableHistory.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        this.jTableHistory.setModel(new DefaultTableModel());
        //this.jTableHistory.setAutoCreateRowSorter(true);

    }

    public void fillHistoryTable(int prec) {
        this.jTableHistory.setDefaultRenderer(Object.class, new HistoryTableCellRenderer(prec));
        this.jTableHistory.setModel(new HistoryTableModel(history));
        //this.jTableHistory.setAutoCreateRowSorter(true);
        fixHistoryTableColumnWidths(jTableHistory);

    }

    public void clearHistoryStatsTable() {

        this.jTableHistoryStats.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        this.jTableHistoryStats.setModel(new DefaultTableModel());
        //this.jTableHistoryStats.setAutoCreateRowSorter(true);

    }

    public void fillHistoryStatsTable(int prec) {
        this.jTableHistoryStats.setDefaultRenderer(Object.class, new HistoryStatsTableCellRenderer(prec));
        this.jTableHistoryStats.setModel(new HistoryStatsTableModel(historyStats));
        //this.jTableHistoryStats.setAutoCreateRowSorter(true);
        fixHistoryTableColumnWidths(jTableHistoryStats);
    }

    public void fixHistoryTableColumnWidths(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            TableColumn column = t.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(250);
            } else {
                column.setPreferredWidth(150);
            }
        }
    }

    private List<String> getSpecialPointNames() {

        return Arrays.asList(new String[]{
            "PlantRunning",
            "OptimizationDisabled",
            "BASCommunicationFailure",
            "CDWSTSPNotOptimized",
            "CHWDPSPNotOptimized",
            "CH1CHWSTSPNotOptimized",
            "CH2CHWSTSPNotOptimized",
            "CH3CHWSTSPNotOptimized ",
            "CT1SPDNotOptimized",
            "CT2SPDNotOptimized",
            "PCHWP1SPDNotOptimized",
            "PCHWP2SPDNotOptimized",
            "PCHWP3SPDNotOptimized",
            "SCHWP1SPDNotOptimized",
            "SCHWP2SPDNotOptimized",
            "SCHWP3SPDNotOptimized",
            "CDWP1SPDNotOptimized",
            "CDWP2SPDNotOptimized",
            "CDWP3SPDNotOptimized"
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel9 = new javax.swing.JPanel();
        jButtonClose = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButtonRunQuery = new javax.swing.JButton();
        jComboBoxResolutions = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldStartDate = new javax.swing.JTextField();
        jTextFieldEndDate = new javax.swing.JTextField();
        jComboBoxQueryPeriods = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jSpinnerPrec = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jButtonMakeCSV = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableHistory = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableHistoryStats = new javax.swing.JTable();
        jButtonChart = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableDataPointsList = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jCheckBoxRegEx = new javax.swing.JCheckBox();
        jTextFieldFilter = new javax.swing.JTextField();
        jButtonSpecialSelect = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaCalculation = new javax.swing.JTextArea();

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("History");

        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        jSplitPane1.setDividerLocation(700);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "History"));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Query Parameters"));

        jLabel1.setText("Start Date:");

        jLabel2.setText("End Date:");

        jButtonRunQuery.setText("Run Query");
        jButtonRunQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRunQueryActionPerformed(evt);
            }
        });

        jComboBoxResolutions.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Reso:");

        jTextFieldStartDate.setText("jTextField1");

        jTextFieldEndDate.setText("jTextField2");

        jComboBoxQueryPeriods.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Query Period:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextFieldStartDate))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextFieldEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxQueryPeriods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonRunQuery)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxResolutions, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jTextFieldStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBoxResolutions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                        .addComponent(jButtonRunQuery)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBoxQueryPeriods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Query Results"));

        jLabel5.setText("Precision");

        jButtonMakeCSV.setText("CSV");
        jButtonMakeCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMakeCSVActionPerformed(evt);
            }
        });

        jTableHistory.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableHistory.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableHistory.setShowGrid(true);
        jTableHistory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableHistoryMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTableHistory);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTableHistoryStats.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableHistoryStats.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableHistoryStats.setShowGrid(true);
        jTableHistoryStats.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableHistoryStatsMousePressed(evt);
            }
        });
        jScrollPane4.setViewportView(jTableHistoryStats);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButtonChart.setText("Chart");
        jButtonChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChartActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButtonMakeCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonChart)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinnerPrec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerPrec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jButtonMakeCSV)
                    .addComponent(jButtonChart))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel3);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Datapoints"));

        jTableDataPointsList.setAutoCreateRowSorter(true);
        jTableDataPointsList.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableDataPointsList.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableDataPointsList.setShowGrid(true);
        jTableDataPointsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableDataPointsListMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(jTableDataPointsList);

        jLabel3.setText("Filter:");

        jCheckBoxRegEx.setText("Use RegEx");

        jTextFieldFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilterActionPerformed(evt);
            }
        });

        jButtonSpecialSelect.setText("Special Select");
        jButtonSpecialSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSpecialSelectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldFilter))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jCheckBoxRegEx)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonSpecialSelect)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxRegEx)
                    .addComponent(jButtonSpecialSelect))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Calculation"));

        jTextAreaCalculation.setColumns(20);
        jTextAreaCalculation.setRows(5);
        jScrollPane3.setViewportView(jTextAreaCalculation);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonClose))
                    .addComponent(jSplitPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonClose)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTableDataPointsListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableDataPointsListMousePressed
        if (evt.isPopupTrigger()) {
            PopupMenuForDataPointsListTable popup = new PopupMenuForDataPointsListTable(evt, jTableDataPointsList);
        }

        if (jTableDataPointsList.getSelectedRowCount() > 0) {
            //enableQueryButtons(true);
        } else {
            //enableQueryButtons(false);
        }

        int row = jTableDataPointsList.rowAtPoint(evt.getPoint());
        int modelIndex = jTableDataPointsList.convertRowIndexToModel(row);
        DataPointsListTableModel mod = (DataPointsListTableModel) jTableDataPointsList.getModel();
        DatapointListItem dataPoint = mod.getRow(modelIndex);

        this.jTextAreaCalculation.setText(dataPoint.getCalculation());

    }//GEN-LAST:event_jTableDataPointsListMousePressed

    private void jButtonRunQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRunQueryActionPerformed

        clearHistoryTable();
        clearHistoryStatsTable();

        List<String> listOfPoints = new ArrayList<>();
        DataPointsListTableModel tableModel = (DataPointsListTableModel) (jTableDataPointsList.getModel());
        int[] selectedRowNumbers = jTableDataPointsList.getSelectedRows();
        for (int selectedRowNumber : selectedRowNumbers) {
            int modelRowNumber = jTableDataPointsList.convertRowIndexToModel(selectedRowNumber);
            DatapointListItem dataRow = tableModel.getRow(modelRowNumber);
            listOfPoints.add(dataRow.getId());
        }

        String resolution = (String) (this.jComboBoxResolutions.getSelectedItem());

        DateTime queryStart = DateTime.parse(jTextFieldStartDate.getText(), zzFormat).withZone(DateTimeZone.UTC);
        DateTime queryEnd = DateTime.parse(jTextFieldEndDate.getText(), zzFormat).withZone(DateTimeZone.UTC);;

        HistoryRequest hr = new HistoryRequest(listOfPoints, queryStart, queryEnd, resolution, this.timeZone);
        controller.getHistory(hr);
    }//GEN-LAST:event_jButtonRunQueryActionPerformed

    private void jTableHistoryMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableHistoryMousePressed
        if (evt.isPopupTrigger()) {
            PopupMenuForHistoryTable popup = new PopupMenuForHistoryTable(evt, jTableHistory);
        }

    }//GEN-LAST:event_jTableHistoryMousePressed

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        dispose();
    }//GEN-LAST:event_jButtonCloseActionPerformed

    private void jButtonSpecialSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSpecialSelectActionPerformed
        List<String> specialPoints = getSpecialPointNames();

        DataPointsListTableModel tableModel = (DataPointsListTableModel) (jTableDataPointsList.getModel());

        for (int row = 0; row < jTableDataPointsList.getRowCount(); row++) {
            int modelRowNumber = jTableDataPointsList.convertRowIndexToModel(row);
            DatapointListItem dataRow = tableModel.getRow(modelRowNumber);
            if (specialPoints.contains(dataRow.getShortName())) {
                jTableDataPointsList.addRowSelectionInterval(row, row);
            }

        }

    }//GEN-LAST:event_jButtonSpecialSelectActionPerformed

    private void jTextFieldFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilterActionPerformed
        this.filter = jTextFieldFilter.getText();
        fillDataPointsListTable(this.filter);
    }//GEN-LAST:event_jTextFieldFilterActionPerformed

    private void jButtonMakeCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMakeCSVActionPerformed

        if (history != null && history.getTimestamps().size() > 0) {
            JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

            //int returnValue = jfc.showOpenDialog(null);
            int returnValue = jfc.showSaveDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                this.jButtonMakeCSV.setEnabled(false);
                File selectedFile = jfc.getSelectedFile();
                System.out.println(selectedFile.getAbsolutePath());
                controller.createCSV(selectedFile.getAbsolutePath(), history);
            }
        }
    }//GEN-LAST:event_jButtonMakeCSVActionPerformed

    private void jTableHistoryStatsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableHistoryStatsMousePressed
        if (evt.isPopupTrigger()) {
            PopupMenuHistoryStatsTable popup = new PopupMenuHistoryStatsTable(evt, jTableHistoryStats);
        }
    }//GEN-LAST:event_jTableHistoryStatsMousePressed

    private void jButtonChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChartActionPerformed
                if (history != null && history.getTimestamps().size() > 0) {


                    HistoryChartFrame chartFrame = new HistoryChartFrame(controller, history);
            controller.addModelListener(chartFrame);
            chartFrame.pack();
            chartFrame.setLocationRelativeTo(this);
            chartFrame.setVisible(true);

        }
    }//GEN-LAST:event_jButtonChartActionPerformed

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (propName.equals(PropertyChangeNames.DatapointsReturned.getName())) {
            datapointsList = (List<DatapointListItem>) evt.getNewValue();
            fillDataPointsListTable(this.filter);
        }

        if (propName.equals(PropertyChangeNames.HistoryReturned.getName())) {
            int prec = (int) this.jSpinnerPrec.getModel().getValue();

            history = (HistoryQueryResults) evt.getNewValue();
            fillHistoryTable(prec);
            historyStats = new Statistics(history);
            fillHistoryStatsTable(prec);
        }

        if (propName.equals(PropertyChangeNames.CSVCreated.getName())) {
            boolean flag = (boolean) evt.getNewValue();
            System.out.println("done! : " + ((flag) ? "created" : "failed"));
            this.jButtonMakeCSV.setEnabled(true);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonChart;
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonMakeCSV;
    private javax.swing.JButton jButtonRunQuery;
    private javax.swing.JButton jButtonSpecialSelect;
    private javax.swing.JCheckBox jCheckBoxRegEx;
    private javax.swing.JComboBox<String> jComboBoxQueryPeriods;
    private javax.swing.JComboBox<String> jComboBoxResolutions;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSpinner jSpinnerPrec;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTableDataPointsList;
    private javax.swing.JTable jTableHistory;
    private javax.swing.JTable jTableHistoryStats;
    private javax.swing.JTextArea jTextAreaCalculation;
    private javax.swing.JTextField jTextFieldEndDate;
    private javax.swing.JTextField jTextFieldFilter;
    private javax.swing.JTextField jTextFieldStartDate;
    // End of variables declaration//GEN-END:variables
}
