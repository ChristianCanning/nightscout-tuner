import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.time.ZonedDateTime;

public class BGChart
{

    public BGChart(double[] xData, double[] yData, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        XYChart chart = QuickChart.getChart("BG Chart " + dateStart.toLocalDate() + " to " + dateEnd.toLocalDate(), "Minutes", "BG", "Averaged BGs", xData, yData);

        chart.getStyler().setYAxisMax(400.0);
        chart.getStyler().setYAxisMin(0.0);

        JPanel chartPanel = new XChartPanel<XYChart>(chart);

        Main.getGUI().addChart(chartPanel);
    }
}