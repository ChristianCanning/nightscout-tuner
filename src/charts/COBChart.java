import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class COBChart
{

    public COBChart(double[] xData, double[] yData, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        Map<Object, Object> customXAxisTickLabelsMap = new HashMap<>();
        for(int i = 0; i < xData.length; i++)
        {
            if(i % 180 == 0)
            {
                customXAxisTickLabelsMap.put(i, LocalTime.of(i / 60, i % 60));
            }

        }

        XYChart chart = new XYChart(700, 500);
        chart.setTitle("COB Chart " + dateStart.toLocalDate() + " to " + dateEnd.toLocalDate().minusDays(1));
        chart.setXAxisTitle("Time(hours)");
        chart.setYAxisTitle("Carbs on Board");
        XYSeries series = chart.addSeries("COB", xData, yData);
        series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.setCustomXAxisTickLabelsMap(customXAxisTickLabelsMap);
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