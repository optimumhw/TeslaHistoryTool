package view.DataGenerator;

import model.simulator.UpsertPoint;
import controller.Controller;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.TableColumn;
import model.DataPoints.Datapoint;
import model.DataPoints.EnumResolutions;
import model.PropertyChangeNames;
import model.simulator.EnumPattern;
import model.simulator.EnumPeriod;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;

public class DataGeneratorFrame extends javax.swing.JFrame implements PropertyChangeListener {

    private static DataGeneratorFrame thisInstance;
    private final Controller controller;

    private List<UpsertPoint> dgTableRows;

    private final List<Datapoint> pointsList;

    private Timer lapsedTimeTimer;
    private final ActionListener lapsedTimeUpdater;
    private DateTime historyPushTimerStart;

    public static DataGeneratorFrame getInstance(
            final Controller controller,
            List<Datapoint> pointsList,
            DateTime startDateTime,
            DateTime endDateTime
    ) {
        if (thisInstance == null) {
            thisInstance = new DataGeneratorFrame(
                    controller,
                    pointsList,
                    startDateTime,
                    endDateTime);
        }
        return thisInstance;
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

    private DataGeneratorFrame(
            Controller controller,
            List<Datapoint> pointsList,
            DateTime startDateTime,
            DateTime endDateTime) {

        initComponents();

        lapsedTimeUpdater = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                DateTime historyPusTimerEnd = DateTime.now();
                Period period = new Period(historyPushTimerStart, historyPusTimerEnd);
                String lapsedTimeString = String.format("%03d %02d:%02d:%02d", period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());
                jLabelLapsedTime.setText(lapsedTimeString);
                lapsedTimeTimer.restart();

            }
        };

        this.controller = controller;
        this.pointsList = pointsList;

        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        this.jTextFieldStartTime.setText(startDateTime.toString(fmt));
        this.jTextFieldEndTime.setText(endDateTime.toString(fmt));

        jButtonStart.setEnabled(false);

        dgTableRows = new ArrayList<>();

        for (Datapoint dp : pointsList) {
            dgTableRows.add(new UpsertPoint(dp));
        }

        fillDatapointsTable(dgTableRows);

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelOptions = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldStartTime = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldEndTime = new javax.swing.JTextField();
        jPanelPointValues = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTablePointValues = new javax.swing.JTable();
        jPanelProgress = new javax.swing.JPanel();
        jProgressBar = new javax.swing.JProgressBar();
        jButtonStart = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabelLapsedTime = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("History Generator");

        jPanelOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Options"));

        jLabel1.setText("Start Time:");

        jTextFieldStartTime.setText("jTextField1");

        jLabel2.setText("End Time:");

        jTextFieldEndTime.setText("jTextField2");

        javax.swing.GroupLayout jPanelOptionsLayout = new javax.swing.GroupLayout(jPanelOptions);
        jPanelOptions.setLayout(jPanelOptionsLayout);
        jPanelOptionsLayout.setHorizontalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelOptionsLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldEndTime))
                    .addGroup(jPanelOptionsLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(400, Short.MAX_VALUE))
        );
        jPanelOptionsLayout.setVerticalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldEndTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(82, Short.MAX_VALUE))
        );

        jPanelPointValues.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Point Values"));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTablePointValues.setModel(new javax.swing.table.DefaultTableModel(
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
        jTablePointValues.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTablePointValues.setShowGrid(true);
        jTablePointValues.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTablePointValuesMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTablePointValues);

        javax.swing.GroupLayout jPanelPointValuesLayout = new javax.swing.GroupLayout(jPanelPointValues);
        jPanelPointValues.setLayout(jPanelPointValuesLayout);
        jPanelPointValuesLayout.setHorizontalGroup(
            jPanelPointValuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPointValuesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanelPointValuesLayout.setVerticalGroup(
            jPanelPointValuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPointValuesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanelProgress.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Progress"));

        jProgressBar.setStringPainted(true);

        jButtonStart.setText("Make History");
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Close");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jLabelLapsedTime.setText("000 00:00:00");

        javax.swing.GroupLayout jPanelProgressLayout = new javax.swing.GroupLayout(jPanelProgress);
        jPanelProgress.setLayout(jPanelProgressLayout);
        jPanelProgressLayout.setHorizontalGroup(
            jPanelProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProgressLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelLapsedTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCancel)
                .addContainerGap())
        );
        jPanelProgressLayout.setVerticalGroup(
            jPanelProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProgressLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelLapsedTime))
                    .addComponent(jButtonStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelPointValues, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelProgress, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelPointValues, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed

        if (this.jTablePointValues.getSelectedRowCount() > 0) {

            String startDateString = jTextFieldStartTime.getText();
            String endDateString = jTextFieldEndTime.getText();

            DateTime startDate = DateTime.parse(startDateString).withZone(DateTimeZone.UTC);
            DateTime endDate = DateTime.parse(endDateString).withZone(DateTimeZone.UTC);;

            Hours hoursPeriod = Hours.hoursBetween(startDate, endDate);

            jProgressBar.setMaximum(hoursPeriod.getHours());
            jProgressBar.setValue(0);
            jProgressBar.setStringPainted(true);

            List<UpsertPoint> selectedTableRows = new ArrayList<>();
            int[] selectedRowsIndicies = this.jTablePointValues.getSelectedRows();
            DGPointTableModel model = (DGPointTableModel) this.jTablePointValues.getModel();
            for (int index : selectedRowsIndicies) {
                int modelIndex = jTablePointValues.convertRowIndexToModel(index);
                UpsertPoint row = model.getRowFromTable(modelIndex);
                selectedTableRows.add(row);
            }

            this.jButtonStart.setEnabled(false);
            historyPushTimerStart = DateTime.now();
            lapsedTimeTimer = new Timer(1000, lapsedTimeUpdater);
            lapsedTimeTimer.start();

            controller.putHistory(selectedTableRows, EnumResolutions.FIVEMINUTE, startDate, endDate);

        }


    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jTablePointValuesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTablePointValuesMousePressed
        this.jButtonStart.setEnabled(this.jTablePointValues.getSelectedRowCount() > 0);
    }//GEN-LAST:event_jTablePointValuesMousePressed

    private void fillDatapointsTable(List<UpsertPoint> rows) {

        List<UpsertPoint> visibleRows = rows;

        List<String> patternNames = new ArrayList<>();
        for (EnumPattern enumPattern : EnumPattern.values()) {
            patternNames.add(enumPattern.getName());
        }

        List<String> periodNames = new ArrayList<>();
        for (EnumPeriod periodName : EnumPeriod.values()) {
            periodNames.add(periodName.getName());
        }

        this.jTablePointValues.setRowSorter(null);
        DGPointTableModel tm = new DGPointTableModel(visibleRows);
        this.jTablePointValues.setModel(tm);

        int patternColumnNumber = EnumDGTableColumns.Pattern.getColumnNumber();
        TableColumn patternColumn = jTablePointValues.getColumnModel().getColumn(patternColumnNumber);
        JComboBox patternComboBox = new JComboBox(patternNames.toArray());
        patternColumn.setCellEditor(new DefaultCellEditor(patternComboBox));

        int periodColumnNumber = EnumDGTableColumns.Period.getColumnNumber();
        TableColumn periodColumn = jTablePointValues.getColumnModel().getColumn(periodColumnNumber);
        JComboBox periodComboBox = new JComboBox(periodNames.toArray());
        periodColumn.setCellEditor(new DefaultCellEditor(periodComboBox));

        this.jTablePointValues.setDefaultRenderer(Object.class, new DGTableCellRenderer());
        this.jTablePointValues.setAutoCreateRowSorter(true);
        fixColumnWidths(jTablePointValues);
    }

    private void fixColumnWidths(JTable t) {

        for (int i = 0; i < t.getColumnCount(); i++) {
            TableColumn column = t.getColumnModel().getColumn(i);
            column.setPreferredWidth(75);

        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelLapsedTime;
    private javax.swing.JPanel jPanelOptions;
    private javax.swing.JPanel jPanelPointValues;
    private javax.swing.JPanel jPanelProgress;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTablePointValues;
    private javax.swing.JTextField jTextFieldEndTime;
    private javax.swing.JTextField jTextFieldStartTime;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (propName.equals(PropertyChangeNames.StationDatapointHistoryOneHourPushed.getName())) {
            int count = jProgressBar.getValue();
            count++;
            jProgressBar.setValue(Math.min(count, jProgressBar.getMaximum()));

        } else if (propName.equals(PropertyChangeNames.StationHistoryAllPushed.getName())) {
            jProgressBar.setBackground(Color.GREEN);
            jProgressBar.invalidate();
            jProgressBar.repaint();

            if (lapsedTimeTimer != null) {
                lapsedTimeTimer.stop();
            }

            DateTime historyPusTimerEnd = DateTime.now();
            Period period = new Period(historyPushTimerStart, historyPusTimerEnd);
            System.out.println("lapsed time: " + PeriodFormat.getDefault().print(period));
            System.out.println(String.format("%02d:%02d:%02d", period.getHours(), period.getMinutes(), period.getSeconds()));

            String lapsedTimeString = String.format("%03d %02d:%02d:%02d", period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());
            jLabelLapsedTime.setText(lapsedTimeString);

            Object[] options = {"OK"};
            JOptionPane.showOptionDialog(null,
                    String.format("Lapsed time: %03d days %02d:%02d:%02d", period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds()),
                    "Done!",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);

            this.dispose();
        }
    }
}
