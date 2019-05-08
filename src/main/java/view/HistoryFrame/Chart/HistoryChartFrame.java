package view.HistoryFrame.Chart;

import controller.Controller;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import model.DataPoints.HistoryQueryResults;
import model.PropertyChangeNames;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class HistoryChartFrame extends javax.swing.JFrame implements PropertyChangeListener {

    private final Controller controller;
    private final HistoryQueryResults history;

    public HistoryChartFrame(Controller controller, HistoryQueryResults history) {
        initComponents();

        this.controller = controller;
        this.history = history;

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ChartPanel CP = getChart(history);
        this.setContentPane(CP);
    }

    private ChartPanel getChart(HistoryQueryResults history) {

        DefaultCategoryDataset ds = getDataSet(history);

        JFreeChart chart = ChartFactory.createLineChart(
                "Datapoint Values",
                "Date",
                "Values",
                ds,
                PlotOrientation.VERTICAL,
                true, true, false);

        chart.setBackgroundPaint(Color.white);
        chart.getCategoryPlot().setDomainAxis(new SparselyLabeledCategoryAxis(15));
        CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        ChartPanel CP = new ChartPanel(chart);
        return CP;
    }

    private DefaultCategoryDataset getDataSet(HistoryQueryResults history) {

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        DateTimeFormatter fromStringFmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateTimeFormatter toStringFmt = DateTimeFormat.forPattern("MM/dd HH:mm");

        for (DateTime ts : history.getTimestamps()) {

            String tsString = ts.toString(toStringFmt);

            List<Object> vals = (List<Object>) history.getTimeStampToValuesArray().get(ts);

            int pointIndex = 0;
            for (String dpName : history.getPointNames()) {

                if (vals != null && pointIndex < vals.size()) {
                    Object val = vals.get(pointIndex);
                    if (val instanceof Double) {
                        ds.addValue((Double) val, dpName, tsString);
                    } else if (val instanceof Integer) {
                        ds.addValue((Integer) val, dpName, tsString);
                    } else if (val instanceof Boolean) {
                        ds.addValue(((Boolean) val) ? 1.0 : 0.0, dpName, tsString);
                    } else if (val == null) {
                        ds.addValue((Double) 0.0, dpName, tsString);
                    }
                }
                pointIndex++;

            }
        }
        return ds;
    }

    @Override
    public void dispose() {
        controller.removePropChangeListener(this);
        super.dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 662, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 432, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (propName.equals(PropertyChangeNames.LoginResponseReturned.getName())) {
            dispose();
        }
    }
}
