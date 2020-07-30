import org.knowm.xchart.*;

import javax.swing.*;
import java.time.ZonedDateTime;

public class BasalChart
{

    public BasalChart(double[] xData, double[] yData, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        XYChart chart = QuickChart.getChart("Basal Chart " + dateStart.toLocalDate() + " to " + dateEnd.toLocalDate() , "Time", "Basal", "Average Basals", xData, yData);

        chart.getStyler().setYAxisMax(5.0);
        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Step);

        JPanel chartPanel = new XChartPanel<XYChart>(chart);

        Main.getGUI().addChart(chartPanel);

    }
}