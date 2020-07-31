import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.time.ZonedDateTime;

public class GIRCurveChart
{
    public GIRCurveChart(double[] xData, double[] yData, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        XYChart chart = QuickChart.getChart("Insulin Curve Chart " + dateStart.toLocalDate() + " to " + dateEnd.toLocalDate() , "Minutes", "Insulin Curve", "Average ", xData, yData);

        double max = 0;
        for(double i : yData)
        {
            if (i > max)
            {
                max = i;
            }
        }
        chart.getStyler().setYAxisMax(max);
        chart.getStyler().setYAxisMin(0.0);

        JPanel chartPanel = new XChartPanel<XYChart>(chart);

        Main.getGUI().addChart(chartPanel);

    }
}
