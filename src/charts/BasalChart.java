import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class BasalChart
{

    public BasalChart(double[] xData, double[] yData, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        int period = (int)(24.0/xData.length * 60);
        Map<Object, Object> customXAxisTickLabelsMap = new HashMap<>();
        for(int i = 0; i < xData.length; i++)
        {
            if(i * period % 180 == 0)
                customXAxisTickLabelsMap.put(i, LocalTime.of(i * period / 60, i * period % 60));
        }

        XYChart chart = new XYChart(700, 500);

        chart.setTitle("Basal Chart " + dateStart.toLocalDate() + " to " + dateEnd.toLocalDate().minusDays(1));
        chart.setXAxisTitle("Time(hours)");
        chart.setYAxisTitle("Rate(units/hour)");
        XYSeries series = chart.addSeries("basals", xData, yData);
        series.setMarker(SeriesMarkers.NONE);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.setCustomXAxisTickLabelsMap(customXAxisTickLabelsMap);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Step);

        double peak = 4;
        for(double i : yData)
        {
            if(i > peak)
                peak = i;
        }
        chart.getStyler().setYAxisMax(peak);
        chart.getStyler().setYAxisMin(0.0);


        Main.getGUI().addChart(new XChartPanel<XYChart>(chart));

    }
}