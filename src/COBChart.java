import org.knowm.xchart.*;

import javax.swing.*;
import java.time.ZonedDateTime;

public class COBChart
{

    public COBChart(double[] xData, double[] yData, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        XYChart chart = QuickChart.getChart("COB Chart " + dateStart.toLocalDate() + " to " + dateEnd.toLocalDate() , "Minutes", "COB", "Average COB", xData, yData);

        double max = 0;
        for(double i : yData)
        {
            //System.out.println(i);
            if (i > max)
            {
                max = i;
            }
        }
        chart.getStyler().setYAxisMax(max);
        chart.getStyler().setYAxisMin(0.0);
        //chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Step);

        JPanel chartPanel = new XChartPanel<XYChart>(chart);

        main.getGUI().addChart(chartPanel);

    }
}