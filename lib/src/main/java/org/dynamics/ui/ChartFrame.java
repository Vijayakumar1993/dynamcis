package org.dynamics.ui;

import org.dynamics.model.Event;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.util.List;

public class ChartFrame {

    public DefaultCategoryDataset createChart(String title,List<Event> events, JTabbedPane frame){
            DefaultCategoryDataset dataset = createDataset();
            JFreeChart chart = ChartFactory.createBarChart(
                    "Sample Line Chart", // Chart title
                    "X-Axis",            // X-Axis Label
                    "Y-Axis",            // Y-Axis Label
                    dataset,             // Dataset
                    PlotOrientation.VERTICAL, // Chart orientation
                    true,                // Include legend
                    true,                // Tooltips
                    false                // URLs
            );

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
            frame.add(title,chartPanel);
            return dataset;
    }

    private static DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Series1", "Category1");
        dataset.addValue(4.0, "Series1", "Category2");
        dataset.addValue(3.0, "Series1", "Category3");
        dataset.addValue(5.0, "Series1", "Category4");
        return dataset;
    }
}
