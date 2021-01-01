import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class BGChart
{
    DecimalFormat df = new DecimalFormat("#.##");

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

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.setCustomXAxisTickLabelsMap(customXAxisTickLabelsMap);

        double max = 300;
        double min = 40;
        double sum = 0;
        double count = 0;
        for(double i : yData)
        {
            if(!Double.isNaN(i))
                sum += i;
            if(i > 0)
                count++;
            if(i > max)
                max = i;
            if(i < min)
                min = i;
        }


        XYSeries series = chart.addSeries("BGs(AVG BG: " + df.format(sum/count) + ")", xData, yData);
        chart.getStyler().setYAxisMax(max);
        chart.getStyler().setYAxisMin(min);

        Main.getGUI().addChart(new XChartPanel<XYChart>(chart));
    }
    public BGChart(double[] xData, double[] yData, double[] yDataAdjusted, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        System.out.println("Start Stream Before");
        for(double f : yData)
        {
            System.out.println(f);
        }
        System.out.println("End Stream Before");

        System.out.println("Start Stream Adjusted");
        for(double o : yDataAdjusted)
        {
            System.out.println(o);
        }
        System.out.println("End Stream Adjusted");


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

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.setCustomXAxisTickLabelsMap(customXAxisTickLabelsMap);

        double max = 300;
        double min = 40;

        double sum = 0;
        double count = 0;
        for(double i : yData)
        {
            if(!Double.isNaN(i))
                sum += i;
            if(i > 0)
                count++;
            if(i > max)
                max = i;
            if(i < min)
                min = i;
        }

        double countAfter = 0;
        double sumAfter = 0;
        for(double i : yDataAdjusted)
        {
            if(!Double.isNaN(i))
                sumAfter += i;
            if(i > 0)
                countAfter++;
            if(i > max)
                max = i;
            if(i < min)
                min = i;
        }


        XYSeries series = chart.addSeries("Before(AVG BG: " + df.format(sum/count) + ")", xData, yData);
        XYSeries series2 = chart.addSeries("After(AVG BG: " + df.format(sumAfter/countAfter) + ")", xData, yDataAdjusted);
        chart.getStyler().setYAxisMax(max);
        chart.getStyler().setYAxisMin(min);

        Main.getGUI().addChart(new XChartPanel<XYChart>(chart));
    }
}