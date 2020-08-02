import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class BGChart
{

    public BGChart(double[] xData, double[] yData, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        Map<Object, Object> customXAxisTickLabelsMap = new HashMap<>();
        for(int i = 0; i < xData.length; i++)
        {
            if((i * 5) % 180 == 0)
            {
                customXAxisTickLabelsMap.put(i*5, LocalTime.of((i * 5) / 60, (i * 5) % 60));
            }

        }

        XYChart chart = new XYChart(715, 500);
        chart.setTitle("BGs Chart " + dateStart.toLocalDate() + " to " + dateEnd.toLocalDate().minusDays(1));
        chart.setXAxisTitle("Time(hours)");
        chart.setYAxisTitle("Blood Glucose");
        XYSeries series = chart.addSeries("BGs", xData, yData);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.setCustomXAxisTickLabelsMap(customXAxisTickLabelsMap);

        double max = 300;
        double min = 40;
        for(double i : yData)
        {
            if(i > max)
                max = i;
            if(i < min)
                min = i;
        }
        chart.getStyler().setYAxisMax(max);
        chart.getStyler().setYAxisMin(min);

        Main.getGUI().addChart(new XChartPanel<XYChart>(chart));
    }
    public BGChart(double[] xData, double[] yData, double[] yDataAdjusted, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        Map<Object, Object> customXAxisTickLabelsMap = new HashMap<>();
        for(int i = 0; i < xData.length; i++)
        {
            if((i * 5) % 180 == 0)
            {
                customXAxisTickLabelsMap.put(i*5, LocalTime.of((i * 5) / 60, (i * 5) % 60));
            }

        }

        XYChart chart = new XYChart(700, 500);
        chart.setTitle("Adjusted BGs Chart " + dateStart.toLocalDate() + " to " + dateEnd.toLocalDate().minusDays(1));
        chart.setXAxisTitle("Time(hours)");
        chart.setYAxisTitle("Blood Glucose");
        XYSeries series = chart.addSeries("Before", xData, yData);
        XYSeries series2 = chart.addSeries("After", xData, yDataAdjusted);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.setCustomXAxisTickLabelsMap(customXAxisTickLabelsMap);

        double max = 300;
        double min = 40;
        for(double i : yData)
        {
            if(i > max)
                max = i;
            if(i < min)
                min = i;
        }
        for(double i : yDataAdjusted)
        {
            if(i > max)
                max = i;
            if(i < min)
                min = i;
        }
        chart.getStyler().setYAxisMax(max);
        chart.getStyler().setYAxisMin(min);

        Main.getGUI().addChart(new XChartPanel<XYChart>(chart));
    }
}