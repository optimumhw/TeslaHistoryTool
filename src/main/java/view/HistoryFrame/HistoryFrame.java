package view.HistoryFrame;

import controller.Controller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import model.DataPoints.EnumResolutions;
import model.DataPoints.HistoryQueryResults;
import model.DataPoints.HistoryRequest;
import model.DataPoints.LiveDatapoint;
import model.DataPoints.StationInfo;
import model.DatapointList.DatapointListItem;
import model.EnumTimeZones;
import model.PropertyChangeNames;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import view.HistoryFrame.PushE3OSDataFrame.PushE3OSHistoryFrame;
import view.HistoryFrame.PushFromTeslaFrame.PushFromTeslaFrame;

public final class HistoryFrame extends javax.swing.JFrame implements PropertyChangeListener {

    private static HistoryFrame thisInstance;

    private final Controller controller;
    private final StationInfo selectedStation;
    private List<DatapointListItem> datapointsList;

    private HistoryQueryResults history;
    private Statistics historyStats;

    private final DateTimeFormatter zzFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
    private final DateTimeFormatter utcFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private DateTime utcStartDate;
    private DateTime utcEndDate;

    private DateTime siteLocalStartDate;
    private DateTime siteLocalEndDate;
    private String selectedQueryTimezone;

    private EnumQueryPeriods queryPeriod;
    private EnumMonths selectedMonth;
    private EnumYears selectedYear;

    private String filter = "";

    private Timer timer = null;

    private final String fiveMinuteString = "fiveMinute";

    public static HistoryFrame getInstance(final Controller controller, StationInfo selectedStation) {
        if (thisInstance == null) {
            thisInstance = new HistoryFrame(controller, selectedStation);
        }
        return thisInstance;
    }

    private HistoryFrame(Controller controller, StationInfo selectedStation) {
        initComponents();
        
        this.setTitle( selectedStation.getName() + " History");

        this.controller = controller;
        this.selectedStation = selectedStation;

        queryPeriod = EnumQueryPeriods.LAST_30_DAYS;
        selectedMonth = EnumMonths.Jan;
        selectedYear = EnumYears.y2019;

        setStartAndEndDates(queryPeriod);

        fillQueryPeriodsDropDown(queryPeriod);
        fillMonthsDropDown(selectedMonth);
        fillReportYearDropDown();
        
        selectedQueryTimezone = selectedStation.getTimeZone();

        fillQueryTimeZonesDropDown();

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
    
    private void fillQueryTimeZonesDropDown() {

        ComboBoxModel comboBoxModel = new DefaultComboBoxModel(EnumTimeZones.getTimeZoneNames().toArray());
        this.jComboBoxQueryTimeZones.setModel(comboBoxModel);
        this.jComboBoxQueryTimeZones.setSelectedItem(selectedQueryTimezone);
        this.jComboBoxQueryTimeZones.setEnabled(true);

        this.jComboBoxQueryTimeZones.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                JComboBox<String> combo = (JComboBox<String>) event.getSource();
                String name = (String) combo.getSelectedItem();
                selectedQueryTimezone = EnumTimeZones.getTimeZoneFromName(name).getTimeZoneName();
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

    private void fillMonthsDropDown(EnumMonths initMonth) {

        ComboBoxModel comboBoxModel = new DefaultComboBoxModel(EnumMonths.getMonthNames().toArray());
        this.jComboBoxMonthPicker.setModel(comboBoxModel);
        this.jComboBoxMonthPicker.setSelectedIndex(initMonth.getDropDownIndex());
        this.jComboBoxMonthPicker.setEnabled(true);

        this.jComboBoxMonthPicker.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                JComboBox<String> combo = (JComboBox<String>) event.getSource();
                String name = (String) combo.getSelectedItem();
                selectedMonth = EnumMonths.getMonthFromName(name);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setStartAndEndDates(selectedMonth, selectedYear);
                    }
                });

            }
        });
    }

    private void fillReportYearDropDown() {

        ComboBoxModel comboBoxModel = new DefaultComboBoxModel(EnumYears.getYearNames().toArray());
        this.jComboBoxYears.setModel(comboBoxModel);
        this.jComboBoxYears.setSelectedIndex(EnumYears.y2019.getDropDownIndex());
        this.jComboBoxYears.setEnabled(true);

        this.jComboBoxYears.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                JComboBox<String> combo = (JComboBox<String>) event.getSource();
                String name = (String) combo.getSelectedItem();
                selectedYear = EnumYears.getYearFromName(name);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setStartAndEndDates(selectedMonth, selectedYear);
                    }
                });

            }
        });
    }

    private void setStartAndEndDates(EnumMonths month, EnumYears year) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month.getMonthNumber());
        cal.set(Calendar.YEAR, year.getYearNumber());
        int numOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        DateTimeZone zone = DateTimeZone.forID(selectedQueryTimezone);

        siteLocalStartDate = new DateTime(year.getYearNumber(), month.getMonthNumber() + 1, 1, 0, 0, zone);
        siteLocalEndDate = siteLocalStartDate.plusDays(numOfDaysInMonth).minusSeconds(1);

        this.jTextFieldStartDate.setText(siteLocalStartDate.toString(zzFormat));
        this.jTextFieldEndDate.setText(siteLocalEndDate.toString(zzFormat));

        setUTCLabels();

    }

    private void setStartAndEndDates(EnumQueryPeriods queryPeriod) {

        DateTime utcToday = DateTime.now().withZone(DateTimeZone.UTC);

        DateTimeZone zone = DateTimeZone.forID(selectedQueryTimezone);
        DateTime siteLocalToday = new DateTime(utcToday).withZone(zone);

        switch (queryPeriod) {
            case LAST_12_MONTHS: {
                siteLocalEndDate = siteLocalToday;
                siteLocalStartDate = siteLocalEndDate.minusMonths(12);
            }
            break;
            case THIS_YEAR: {
                siteLocalEndDate = siteLocalToday;
                siteLocalStartDate = siteLocalEndDate.minusDays(siteLocalToday.getDayOfYear() - 1);
                siteLocalStartDate = siteLocalStartDate.minusMillis(siteLocalStartDate.getMillisOfDay());

            }
            break;
            case LAST_30_DAYS: {
                siteLocalEndDate = siteLocalToday;
                siteLocalStartDate = siteLocalEndDate.minusDays(30);
                siteLocalStartDate = siteLocalStartDate.minusMillis(siteLocalStartDate.getMillisOfDay());

            }
            break;
            case THIS_MONTH: {
                siteLocalEndDate = siteLocalToday;
                siteLocalStartDate = siteLocalEndDate.minusDays(siteLocalEndDate.getDayOfMonth() - 1);
                siteLocalStartDate = siteLocalStartDate.minusMillis(siteLocalStartDate.getMillisOfDay());
            }
            break;
            case LAST_7_DAYS: {
                siteLocalEndDate = siteLocalToday;
                siteLocalStartDate = siteLocalEndDate.minusDays(7);
                siteLocalStartDate = siteLocalStartDate.minusMillis(siteLocalStartDate.getMillisOfDay());
            }
            break;

            case THIS_WEEK: {
                siteLocalEndDate = siteLocalToday;
                siteLocalStartDate = siteLocalEndDate.minusDays(siteLocalEndDate.getDayOfWeek());
                siteLocalStartDate = siteLocalStartDate.minusMillis(siteLocalStartDate.getMillisOfDay());
            }
            break;

            case LAST_24_HOURS: {
                siteLocalEndDate = siteLocalToday;
                siteLocalStartDate = siteLocalEndDate.minusDays(1);

            }
            break;

            case TODAY: {
                siteLocalEndDate = siteLocalToday;
                siteLocalStartDate = siteLocalEndDate.minusMillis(siteLocalEndDate.getMillisOfDay());

            }
        }

        this.jTextFieldStartDate.setText(siteLocalStartDate.toString(zzFormat));
        this.jTextFieldEndDate.setText(siteLocalEndDate.toString(zzFormat));

        setUTCLabels();

    }

    private void setUTCLabels() {

        DateTime tempStart = DateTime.parse(jTextFieldStartDate.getText(), zzFormat);
        DateTime tempEnd = DateTime.parse(jTextFieldEndDate.getText(), zzFormat);

        utcStartDate = new DateTime(tempStart).withZone(DateTimeZone.UTC);
        utcEndDate = new DateTime(tempEnd).withZone(DateTimeZone.UTC);

        this.jLabelutcStart.setText(utcStartDate.toString(utcFormat));
        this.jLabelutcEnd.setText(utcEndDate.toString(utcFormat));

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
                column.setPreferredWidth(150);
            } else {
                column.setPreferredWidth(150);
            }
        }
    }

    private List<String> getSpecialPointNames() {

        return Arrays.asList(new String[]{
            "TotalkWh",
            "BaselinekWh",
            "BaselinekW",
            "BaselinekWTon",
            "OAT",
            "OAWB",
            "TotalkW",
            "TotalTon"
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
        jLabelutcStart = new javax.swing.JLabel();
        jLabelutcEnd = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxMonthPicker = new javax.swing.JComboBox<>();
        jComboBoxYears = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jComboBoxQueryTimeZones = new javax.swing.JComboBox<>();
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
        jButtonPushE3OSData = new javax.swing.JButton();
        jButtonPushFromTesla = new javax.swing.JButton();
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

        jSplitPane1.setDividerLocation(800);

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
        jTextFieldStartDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldStartDateActionPerformed(evt);
            }
        });

        jTextFieldEndDate.setText("jTextField2");
        jTextFieldEndDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEndDateActionPerformed(evt);
            }
        });

        jComboBoxQueryPeriods.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Query Period:");

        jLabelutcStart.setText("2019-05-15T06:00:00.000Z");

        jLabelutcEnd.setText("2019-05-15T06:00:00.000Z");

        jLabel7.setText("or Month:");

        jComboBoxMonthPicker.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBoxYears.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel8.setText("Query TZ:");

        jComboBoxQueryTimeZones.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldEndDate)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabelutcStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelutcEnd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxQueryPeriods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxMonthPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxYears, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxQueryTimeZones, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
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
                        .addComponent(jTextFieldStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelutcStart))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBoxResolutions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelutcEnd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonRunQuery))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBoxQueryPeriods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jComboBoxMonthPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBoxYears, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jComboBoxQueryTimeZones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
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

        jButtonPushE3OSData.setText("Push Data From E3OS");
        jButtonPushE3OSData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPushE3OSDataActionPerformed(evt);
            }
        });

        jButtonPushFromTesla.setText("Push from existing Tesla site");
        jButtonPushFromTesla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPushFromTeslaActionPerformed(evt);
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonPushE3OSData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonPushFromTesla)
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
                    .addComponent(jButtonChart)
                    .addComponent(jButtonPushE3OSData)
                    .addComponent(jButtonPushFromTesla))
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
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
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

        if (jTableDataPointsList.getSelectedRowCount() > 0) {
            List<String> listOfTeslaPointIDs = new ArrayList<>();
            List<DatapointListItem> listOfTeslaPoints = new ArrayList<>();
            DataPointsListTableModel tableModel = (DataPointsListTableModel) (jTableDataPointsList.getModel());
            int[] selectedRowNumbers = jTableDataPointsList.getSelectedRows();
            for (int selectedRowNumber : selectedRowNumbers) {
                int modelRowNumber = jTableDataPointsList.convertRowIndexToModel(selectedRowNumber);
                DatapointListItem teslaPoint = tableModel.getRow(modelRowNumber);
                listOfTeslaPoints.add(teslaPoint);
                listOfTeslaPointIDs.add(teslaPoint.getId());
            }

            String resolution = (String) (this.jComboBoxResolutions.getSelectedItem());

            DateTimeZone zone = DateTimeZone.forID(selectedQueryTimezone);
            DateTime queryStart = DateTime.parse(jTextFieldStartDate.getText(), zzFormat).withZone(zone);
            DateTime queryEnd = DateTime.parse(jTextFieldEndDate.getText(), zzFormat).withZone(zone);

            if (!resolution.contentEquals(fiveMinuteString)) {
                HistoryRequest hr = new HistoryRequest(listOfTeslaPointIDs, queryStart, queryEnd, resolution, selectedQueryTimezone);
                controller.getHistory(hr);
                return;

            }

            List<String> listOfFiveMinutePointIDs = new ArrayList<>();
            List<String> listOfHourlyPointIDs = new ArrayList<>();

            for (DatapointListItem teslaPoint : listOfTeslaPoints) {
                String minRes = teslaPoint.getMinimumResolution();
                if (minRes.contentEquals(fiveMinuteString)) {
                    listOfFiveMinutePointIDs.add(teslaPoint.getId());
                } else {
                    listOfHourlyPointIDs.add(teslaPoint.getId());
                }
            }

            HistoryRequest generalRequest = new HistoryRequest(listOfFiveMinutePointIDs, queryStart, queryEnd, resolution, selectedQueryTimezone);
            HistoryRequest secondaryHourlyRequest = new HistoryRequest(listOfHourlyPointIDs, queryStart, queryEnd, "hour", selectedQueryTimezone);
            controller.getComboHistory(generalRequest, secondaryHourlyRequest);
        }

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

    private void jButtonPushE3OSDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPushE3OSDataActionPerformed

        List<DatapointListItem> listOfPoints = new ArrayList<>();

        for (DatapointListItem teslaPoint : datapointsList) {
            if (teslaPoint.getPointType().contentEquals("raw")) {
                listOfPoints.add(teslaPoint);
            }
        }

        if (listOfPoints.size() > 0) {
            DateTime pushStart = DateTime.parse(jTextFieldStartDate.getText(), zzFormat).withZone(DateTimeZone.UTC);
            DateTime pushEnd = DateTime.parse(jTextFieldEndDate.getText(), zzFormat).withZone(DateTimeZone.UTC);

            PushE3OSHistoryFrame frame = PushE3OSHistoryFrame.getInstance(controller, pushStart, pushEnd, listOfPoints);
            controller.addModelListener(frame);
            frame.pack();
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);
        }
    }//GEN-LAST:event_jButtonPushE3OSDataActionPerformed

    private void jButtonPushFromTeslaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPushFromTeslaActionPerformed
        List<DatapointListItem> listOfPoints = new ArrayList<>();

        for (DatapointListItem teslaPoint : datapointsList) {
            if (teslaPoint.getPointType().contentEquals("raw")) {
                listOfPoints.add(teslaPoint);
            }
        }

        if (listOfPoints.size() > 0) {
            DateTime pushStart = DateTime.parse(jTextFieldStartDate.getText(), zzFormat).withZone(DateTimeZone.UTC);
            DateTime pushEnd = DateTime.parse(jTextFieldEndDate.getText(), zzFormat).withZone(DateTimeZone.UTC);

            PushFromTeslaFrame frame = PushFromTeslaFrame.getInstance(controller, selectedStation, listOfPoints, pushStart, pushEnd);
            controller.addModelListener(frame);
            frame.pack();
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);
        }
    }//GEN-LAST:event_jButtonPushFromTeslaActionPerformed

    private void jTextFieldStartDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldStartDateActionPerformed
        setUTCLabels();
    }//GEN-LAST:event_jTextFieldStartDateActionPerformed

    private void jTextFieldEndDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldEndDateActionPerformed
        setUTCLabels();
    }//GEN-LAST:event_jTextFieldEndDateActionPerformed

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

        if (propName.equals(PropertyChangeNames.LiveDataReturned.getName())) {
            List<LiveDatapoint> dpList = (List<LiveDatapoint>) evt.getNewValue();

            HistoryTableModel model = (HistoryTableModel) this.jTableHistory.getModel();
            model.appendLiveData(dpList);

        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonChart;
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonMakeCSV;
    private javax.swing.JButton jButtonPushE3OSData;
    private javax.swing.JButton jButtonPushFromTesla;
    private javax.swing.JButton jButtonRunQuery;
    private javax.swing.JButton jButtonSpecialSelect;
    private javax.swing.JCheckBox jCheckBoxRegEx;
    private javax.swing.JComboBox<String> jComboBoxMonthPicker;
    private javax.swing.JComboBox<String> jComboBoxQueryPeriods;
    private javax.swing.JComboBox<String> jComboBoxQueryTimeZones;
    private javax.swing.JComboBox<String> jComboBoxResolutions;
    private javax.swing.JComboBox<String> jComboBoxYears;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelutcEnd;
    private javax.swing.JLabel jLabelutcStart;
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
