import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.time.ZonedDateTime;
import java.util.Arrays;

public class oldChart extends chart
{
    /*
    public static void adjustAverageSgvs(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, int min, int max, double isf, int period)
    {
        double[] averagedSgvs = averageSgvs(url, dateStart, dateEnd, false);

        double[] adjustedSgvs = new double[averagedSgvs.length];
        for(int m = 0; m < adjustedSgvs.length; m++)
        {
            adjustedSgvs[m] = averagedSgvs[m];
        }
        double total = 0;
        for(int k = 0; k < 2; k++)
        {
            for(int i = 0; i < adjustedSgvs.length; i++)
            {
                if(adjustedSgvs[i] >= 90 && adjustedSgvs[i] <= 92)
                {
                    adjustedSgvs[i] = adjustedSgvs[i];
                }
                else if(adjustedSgvs[i] > 92)
                {
                    double diff = adjustedSgvs[i] - 92;
                    double insulin = diff/isf;
                    double[] curveY = insulinCurve(insulin/80.0);
                    //Length is in minutes
                    boolean run = true;
                    for(int j = i; j < adjustedSgvs.length-1 && adjustedSgvs[j] > 92; j++)
                    {
                        //currentTime is in hours
                        double currentTime = ((j-i)*5)/60.0;
                        double percentage = getInsulinCurvePercentage(curveY, currentTime);
                        double drop = diff * percentage;
                        double length = curveY.length * (15.0/60);
                        double stop = (j+length/5 > adjustedSgvs.length) ? adjustedSgvs.length : j+length/5;
                        boolean adjusted = false;
                        boolean not0 = true;
                        while(!adjusted && not0)
                        {
                            for(int a = j+1; a < stop; a++)
                            {
                                if(adjustedSgvs[a] - drop < 90)
                                    run = false;
                            }
                            if (run)
                            {
                                adjusted = true;
                            }
                            else
                            {
                                insulin = (insulin == .01) ? -1 : .01;
                                System.out.println(k);
                                if(insulin < .001)
                                    not0 = false;
                                if(not0)
                                {
                                    curveY = insulinCurve(insulin/80.0);
                                    percentage = getInsulinCurvePercentage(curveY, currentTime);
                                    drop = diff * percentage;
                                    length = curveY.length * (15.0/60);
                                    stop = (j+length/5 > adjustedSgvs.length) ? adjustedSgvs.length : j+length/5;
                                }

                            }
                        }

                        if(run)
                        {
                            adjustedSgvs[j] = adjustedSgvs[j] - drop;
                            total += insulin * percentage;
                            for(int a = j+1; a < stop; a++)
                            {
                                adjustedSgvs[a] = adjustedSgvs[a] - drop;
                            }

                        }

                    }

                    //i = 20000;

                }

            }
        }

        System.out.println(total + "total insulin");
        double sum = 0;
        double[] xData = new double[adjustedSgvs.length];
        for(int i = 0; i < xData.length; i++)
        {
            if(!Double.isNaN(adjustedSgvs[i]))
                sum += adjustedSgvs[i];
            xData[i] = i * 5;
        }
        System.out.println(sum/adjustedSgvs.length);
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Charts").xAxisTitle("Nominal time (h)").yAxisTitle("GIR(mg/(kg*min))").build();
        chart.addSeries("original", xData, averagedSgvs);
        chart.addSeries("new", xData, adjustedSgvs);
        new SwingWrapper(chart).displayChart();


    }
    public static void adjustAverageSgvs2(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, int min, int max, double isf, int period, double weight)
    {
        double[] averagedSgvs = averageSgvs(url, dateStart, dateEnd, false);
        double[] adjustedSgvs = Arrays.copyOf(averagedSgvs, averagedSgvs.length);
        double[] xData = new double[adjustedSgvs.length];
        for(int i = 0; i < xData.length; i++)
            xData[i] = i * 5;

        double peakValue = 0;
        do
        {
            //System.out.println(repeat);
            int peak = 0;
            peakValue = 0;
            for(int i = 0; i < adjustedSgvs.length; i++)
            {
                if(adjustedSgvs[i] > peakValue)
                {
                    peak = i;
                    peakValue = adjustedSgvs[i];
                }
            }
            System.out.println(peakValue);
            double diff = adjustedSgvs[peak] - 100;
            double insulin = diff/isf;
            double[] insulinCurveY = insulinCurve(insulin/weight);
            int peakY = 0;
            double peakYValue = insulinCurveY[0];
            for(int i = 0; i < insulinCurveY.length; i++)
            {
                if(insulinCurveY[i] > peakYValue)
                {
                    peakY = i;
                    peakYValue = insulinCurveY[i];
                }
            }
            int start = peak - (int)Math.round(peakY * 15.0/60 /5);
            int stop = (start + insulinCurveY.length * (15.0/60/5) > adjustedSgvs.length) ?
                    adjustedSgvs.length : (int)Math.round(start + insulinCurveY.length * (15.0/60/5));
            for(int i = start; i < stop; i++)
            {
                double currentTime = ((i-start) * 5) / 60.0; // In hours. Each i is in 5 minute intervals
                double percentage = getInsulinCurvePercentage(insulinCurveY, currentTime);
                double drop = insulin * percentage;
                boolean canRun = false;
                boolean allPass = true;
                boolean halt = false;
                double lower = insulin * .5;
                while(!canRun && !halt)
                {
                    for(int j = i; j < stop; j++)
                    {
                        if(adjustedSgvs[j] - drop < 90)
                            allPass = false;
                    }
                    if(allPass)
                        canRun = true;
                    else
                    {
                        allPass = true; // reset allPass. This doesn't mean that allPass is actually true.
                        if(insulin - lower > .001)
                        {
                            insulin = insulin - lower;
                            insulinCurveY = insulinCurve(insulin/weight);
                            peakY = 0;
                            peakYValue = insulinCurveY[0];
                            for(int j = 0; j < insulinCurveY.length; j++)
                            {
                                if(insulinCurveY[j] > peakYValue)
                                {
                                    peakY = j;
                                    peakYValue = insulinCurveY[j];
                                }
                            }
                            start = peak - (int)Math.round(peakY * 15.0/60 /5);
                            stop = (start + insulinCurveY.length * (15.0/60/5) > adjustedSgvs.length) ?
                                    adjustedSgvs.length : (int)Math.round(start + insulinCurveY.length * (15.0/60/5));
                            i = start;
                        }
                        else
                            halt = true;

                    }
                }
                if(!halt)
                {
                    for(int j = i; j < stop; j++)
                    {
                        adjustedSgvs[j] = adjustedSgvs[j] - drop;
                    }
                }


            }
        }while (peakValue > 160);



        XYChart chart = new XYChartBuilder().width(800).height(600).title("Charts").xAxisTitle("Nominal time (h)").yAxisTitle("GIR(mg/(kg*min))").build();
        chart.addSeries("original", xData, averagedSgvs);
        chart.addSeries("new", xData, adjustedSgvs);
        new SwingWrapper(chart).displayChart();
    }
    public static void adjustAverageSgvs3(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, int min, double max, double isf, int period, double weight)
    {
        double[] averagedSgvs = averageSgvs(url, dateStart, dateEnd, false);
        double[] adjustedSgvs = Arrays.copyOf(averagedSgvs, averagedSgvs.length);
        double[] xData = new double[adjustedSgvs.length];
        for(int i = 0; i < xData.length; i++)
            xData[i] = i * 5;
        for(int repeat = 0; repeat < 2; repeat++)
        {
            for(int i = 0; i < adjustedSgvs.length; i++)
            {
                if(adjustedSgvs[i] <= 100)
                {
                    adjustedSgvs[i] = adjustedSgvs[i];
                }
                else if(!Double.isNaN(adjustedSgvs[i] - 100))
                {
                    double diff = adjustedSgvs[i] - 100;
                    double insulin = diff/isf;
                    double[] curveY = insulinCurve(insulin/weight);
                    int length = (int)Math.round(curveY.length * 15.0 / 60.0 / 5);
                    int stop = Math.min(i + length, adjustedSgvs.length);
                    for(int j = i; j < stop; j++)
                    {
                        double currentTime = (j-i) * 5 / 60.0;
                        double percentage = getInsulinCurvePercentage(curveY, currentTime);
                        double drop = percentage * insulin;
                        for(int k = j; k < stop; k++)
                        {
                            adjustedSgvs[k] = adjustedSgvs[k] - drop;
                        }
                    }
                }
            }
        }
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Charts").xAxisTitle("Nominal time (h)").yAxisTitle("GIR(mg/(kg*min))").build();
        chart.addSeries("original", xData, averagedSgvs);
        chart.addSeries("new", xData, adjustedSgvs);
        new SwingWrapper(chart).displayChart();

    }
    public static void adjustAverageSvgs4(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, int target, int min, double isf, int period, double weight)
    {

        double[] averagedSgvs = averageSgvs(url, dateStart, dateEnd, false);
        double[] adjustedSgvs = Arrays.copyOf(averagedSgvs, averagedSgvs.length);
        double[] xData = new double[adjustedSgvs.length];
        for(int i = 0; i < xData.length; i++)
            xData[i] = i * 5;

        double[] diffs = new double[48];
        double totalInsulin = 0;
        for(int repeat = 0; repeat < 10; repeat ++)
        {
            for(int i = 0; i < adjustedSgvs.length; i += 6)
            {
                double sum = 0;
                int count = 6;
                for(int j = i; j < i+6; j++)
                {
                    if(!Double.isNaN(adjustedSgvs[j]))
                        sum += adjustedSgvs[j];
                    else
                        count--;
                }
                diffs[i/6] = sum/count - target;
            }
            System.out.println(repeat);
            for(int i = 287; i >= 0; i--)
            {
                //System.out.println(i);
                double diff = diffs[i/6]/5.0;
                if(diff > 0)
                {
                    double insulin = diff/isf;
                    double[] curveY = insulinCurve(insulin/weight);
                    double curveYDuration = curveY.length*(15/300.0);
                    double stop = Math.min(i + curveYDuration, adjustedSgvs.length);
                    int pos = 0;
                    for(int j = i ; j < stop; j++)
                    {
                        //System.out.println(1);
                        double currentTime = pos * 5 / 60.0;
                        double percentage = getInsulinCurvePercentage(curveY, currentTime);
                        double drop = diff * percentage;
                        boolean canRun = false;
                        boolean shouldRun = true;
                        double part = insulin * .1;
                        while(!canRun && shouldRun)
                        {
                            canRun = true;
                            for(int a = j; a < stop; a++)
                            {
                                if(adjustedSgvs[a] - drop < min)
                                    canRun = false;
                            }
                            if(!canRun)
                            {
                                if(insulin - part > .0001)
                                {
                                    insulin = insulin - part;
                                    curveY = insulinCurve(insulin/weight);
                                    curveYDuration = curveY.length*(15/300.0);
                                    stop = Math.min(i + curveYDuration, adjustedSgvs.length);
                                    percentage = getInsulinCurvePercentage(curveY, currentTime);
                                    drop = diff * percentage;
                                }
                                else
                                {
                                    shouldRun = false;
                                }
                            }

                        }
                        if(shouldRun)
                        {
                            //System.out.println(insulin * percentage);
                            totalInsulin += insulin * percentage;
                            for(int a = j; a < stop; a++)
                            {
                                adjustedSgvs[a] = adjustedSgvs[a] - drop;
                            }
                        }
                        pos++;
                    }
                    if(stop == adjustedSgvs.length)
                    {
                        stop = (curveYDuration + i) - adjustedSgvs.length;
                        for(int j = 0 ; j < stop; j++)
                        {
                            //System.out.println(1);
                            double currentTime = pos * 5 / 60.0;
                            double percentage = getInsulinCurvePercentage(curveY, currentTime);
                            double drop = diff * percentage;
                            boolean canRun = false;
                            boolean shouldRun = true;
                            double part = insulin * .1;
                            while(!canRun && shouldRun)
                            {
                                canRun = true;
                                for(int a = j; a < stop; a++)
                                {
                                    if(adjustedSgvs[a] - drop < min)
                                        canRun = false;
                                }
                                if(!canRun)
                                {
                                    if(insulin - part > .0001)
                                    {
                                        insulin = insulin - part;
                                        curveY = insulinCurve(insulin/weight);
                                        curveYDuration = curveY.length*(15/300.0);
                                        stop = Math.min(i + curveYDuration, adjustedSgvs.length);
                                        percentage = getInsulinCurvePercentage(curveY, currentTime);
                                        drop = diff * percentage;
                                    }
                                    else
                                    {
                                        shouldRun = false;
                                    }
                                }

                            }
                            if(shouldRun)
                            {
                                //totalInsulin += insulin * percentage;
                                //System.out.println(insulin * percentage);
                                for(int a = j; a < stop; a++)
                                {
                                    //System.out.println(a);
                                    adjustedSgvs[a] = adjustedSgvs[a] - drop;
                                }
                            }
                            pos++;
                        }
                    }
                }

            }
        }


        double averageSum = 0;
        for(double i : averagedSgvs)
        {
            if(!Double.isNaN(i))
                averageSum += i;
        }
        System.out.println(averageSum/averagedSgvs.length);
        double adjustSum = 0;
        for(double i : adjustedSgvs)
        {
            if(!Double.isNaN(i))
                adjustSum += i;
        }
        System.out.println(adjustSum/adjustedSgvs.length);

        System.out.println(totalInsulin);

        XYChart chart = new XYChartBuilder().width(800).height(600).title("Charts").xAxisTitle("Nominal time (h)").yAxisTitle("GIR(mg/(kg*min))").build();
        chart.addSeries("original", xData, averagedSgvs);
        chart.addSeries("new", xData, adjustedSgvs);
        new SwingWrapper(chart).displayChart();
    }
        public static void adjustAverageSvgs(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, int target, int min, double isf, int period, double weight)
    {
        double[] averagedSgvs = averageSgvs(url, dateStart, dateEnd, false);
        double[] adjustedSgvs = Arrays.copyOf(averagedSgvs, averagedSgvs.length);
        double[] tempSgvs = Arrays.copyOf(adjustedSgvs, adjustedSgvs.length);
        double[] xData = new double[adjustedSgvs.length];
        for(int i = 0; i < xData.length; i++)
            xData[i] = i * 5;

        double totalInsulin = 0;
        for(int i = 0; i < averagedSgvs.length; i+=6)
        {
            //System.out.println(i);
            double insulin = .001;
            boolean canRun = true;
            while(canRun)
            {
                //System.out.println(insulin);
                double[] curveY = insulinCurve(insulin/weight);
                for(int j = i; j < i + 6; j++)
                {
                    double length = curveY.length * 15 / 60.0 / 5.0;
                    double stop = Math.min(j + length, tempSgvs.length);


                    double total = 0;
                    for(int k = j; k < stop; k++)
                    {
                        double currentTime = (k-j) * 5 / 60.0;
                        double percentage = getInsulinCurvePercentage(curveY, currentTime);
                        total += percentage;
                        double drop = (insulin*isf) * percentage;
                        for(int l = k; l < stop; l++)
                        {
                            if(tempSgvs[l] - drop < min)
                                canRun = false;
                            tempSgvs[l] = tempSgvs[l] - drop;
                        }
                    }


                    if(stop == tempSgvs.length)
                    {
                        stop = (j+length) - tempSgvs.length;
                        for(int k = 0; k < stop; k++)
                        {
                            double currentTime = (k-j) * 5 / 60.0;
                            double percentage = getInsulinCurvePercentage(curveY, currentTime);
                            total += percentage;
                            double drop = (insulin*isf) * percentage;
                            for(int l = k; l < stop; l++)
                            {
                                if(tempSgvs[l] - drop < min)
                                    canRun = false;
                                tempSgvs[l] = tempSgvs[l] - drop;
                            }
                        }
                    }
                }
                if(canRun)
                {
                    tempSgvs = Arrays.copyOf(adjustedSgvs, adjustedSgvs.length);
                    insulin += .001;
                }
                else
                    insulin -= .001;
            }
            if(insulin > 0)
            {
                double[] curveY = insulinCurve(insulin/weight);
                for(int j = i; j < i + 6; j++)
                {
                    double length = curveY.length * 15 / 60.0 / 5.0;
                    double stop = Math.min(j + length, adjustedSgvs.length);

                    for(int k = j; k < stop; k++)
                    {
                        double currentTime = (k-j) * 5 / 60.0;
                        double percentage = getInsulinCurvePercentage(curveY, currentTime);
                        double drop = (insulin*isf) * percentage;
                        totalInsulin += insulin * percentage;
                        for(int l = k; l < stop; l++)
                        {
                            adjustedSgvs[l] = adjustedSgvs[l] - drop;
                        }
                    }
                    if(stop == adjustedSgvs.length)
                    {
                        stop = (j+length) - adjustedSgvs.length;
                        for(int k = 0; k < stop; k++)
                        {
                            double currentTime = (k-j) * 5 / 60.0;
                            double percentage = getInsulinCurvePercentage(curveY, currentTime);
                            double drop = (insulin*isf) * percentage;
                            totalInsulin += insulin * percentage;
                            for(int l = k; l < stop; l++)
                            {
                                adjustedSgvs[l] = adjustedSgvs[l] - drop;
                            }
                        }
                    }
                }
                tempSgvs = Arrays.copyOf(adjustedSgvs, adjustedSgvs.length);
            }


        }
        double sum  = 0;
        double count = 0;
        for(double i : averagedSgvs)
        {
            if(!Double.isNaN(i))
            {
                sum += i;
                count++;
            }

        }
        double sum2 = 0;
        double count2 = 0;
        for(double i : adjustedSgvs)
        {
            if(!Double.isNaN(i))
            {
                sum2 += i;
                count2++;
            }

        }
        System.out.println(sum/count);
        System.out.println(sum2/count2);
        System.out.println(totalInsulin);
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Charts").xAxisTitle("Nominal time (h)").yAxisTitle("GIR(mg/(kg*min))").build();
        chart.addSeries("original", xData, averagedSgvs);
        chart.addSeries("new", xData, adjustedSgvs);
        new SwingWrapper(chart).displayChart();
    }

        public static void adjustAverageSgvs(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, int target, int min, double isf, int period, double weight)
    {

        double[] averagedSgvs = averageSgvs(url, dateStart, dateEnd, false);
        double[] adjustedSgvs = Arrays.copyOf(averagedSgvs, averagedSgvs.length);
        double[] xData = new double[adjustedSgvs.length];
        for(int i = 0; i < xData.length; i++)
            xData[i] = i * 5;

        double[] diffs = new double[48];
        double totalInsulin = 0;
        for(int repeat = 0; repeat < 10; repeat ++)
        {
            for(int i = 0; i < adjustedSgvs.length; i += 6)
            {
                double sum = 0;
                int count = 6;
                for(int j = i; j < i+6; j++)
                {
                    if(!Double.isNaN(adjustedSgvs[j]))
                        sum += adjustedSgvs[j];
                    else
                        count--;
                }
                diffs[i/6] = sum/count - target;
            }
            System.out.println(repeat);
            for(int i = 287; i >= 0; i--)
            {
                //System.out.println(i);
                double diff = diffs[i/6]/5.0;
                if(diff > 0)
                {
                    double insulin = diff/isf;
                    double[] curveY = insulinCurve(insulin/weight);
                    double curveYDuration = curveY.length*(15/300.0);
                    double stop = Math.min(i + curveYDuration, adjustedSgvs.length);
                    int pos = 0;
                    for(int j = i ; j < stop; j++)
                    {
                        //System.out.println(1);
                        double currentTime = pos * 5 / 60.0;
                        double percentage = getInsulinCurvePercentage(curveY, currentTime);
                        double drop = diff * percentage;
                        boolean canRun = false;
                        boolean shouldRun = true;
                        double part = insulin * .1;
                        while(!canRun && shouldRun)
                        {
                            canRun = true;
                            for(int a = j; a < stop; a++)
                            {
                                if(adjustedSgvs[a] - drop < min)
                                    canRun = false;
                            }
                            if(!canRun)
                            {
                                if(insulin - part > .0001)
                                {
                                    insulin = insulin - part;
                                    curveY = insulinCurve(insulin/weight);
                                    curveYDuration = curveY.length*(15/300.0);
                                    stop = Math.min(i + curveYDuration, adjustedSgvs.length);
                                    percentage = getInsulinCurvePercentage(curveY, currentTime);
                                    drop = diff * percentage;
                                }
                                else
                                {
                                    shouldRun = false;
                                }
                            }

                        }
                        if(shouldRun)
                        {
                            //System.out.println(insulin * percentage);
                            totalInsulin += insulin * percentage;
                            for(int a = j; a < stop; a++)
                            {
                                adjustedSgvs[a] = adjustedSgvs[a] - drop;
                            }
                        }
                        pos++;
                    }
                    if(stop == adjustedSgvs.length)
                    {
                        stop = (curveYDuration + i) - adjustedSgvs.length;
                        for(int j = 0 ; j < stop; j++)
                        {
                            //System.out.println(1);
                            double currentTime = pos * 5 / 60.0;
                            double percentage = getInsulinCurvePercentage(curveY, currentTime);
                            double drop = diff * percentage;
                            boolean canRun = false;
                            boolean shouldRun = true;
                            double part = insulin * .1;
                            while(!canRun && shouldRun)
                            {
                                canRun = true;
                                for(int a = j; a < stop; a++)
                                {
                                    if(adjustedSgvs[a] - drop < min)
                                        canRun = false;
                                }
                                if(!canRun)
                                {
                                    if(insulin - part > .0001)
                                    {
                                        insulin = insulin - part;
                                        curveY = insulinCurve(insulin/weight);
                                        curveYDuration = curveY.length*(15/300.0);
                                        stop = Math.min(i + curveYDuration, adjustedSgvs.length);
                                        percentage = getInsulinCurvePercentage(curveY, currentTime);
                                        drop = diff * percentage;
                                    }
                                    else
                                    {
                                        shouldRun = false;
                                    }
                                }

                            }
                            if(shouldRun)
                            {
                                totalInsulin += insulin * percentage;
                                //System.out.println(insulin * percentage);
                                for(int a = j; a < stop; a++)
                                {
                                    //System.out.println(a);
                                    adjustedSgvs[a] = adjustedSgvs[a] - drop;
                                }
                            }
                            pos++;
                        }
                    }
                }

            }
        }


        double averageSum = 0;
        for(double i : averagedSgvs)
        {
            if(!Double.isNaN(i))
                averageSum += i;
        }
        System.out.println(averageSum/averagedSgvs.length);
        double adjustSum = 0;
        for(double i : adjustedSgvs)
        {
            if(!Double.isNaN(i))
                adjustSum += i;
        }
        System.out.println(adjustSum/adjustedSgvs.length);

        System.out.println(totalInsulin);

        XYChart chart = new XYChartBuilder().width(800).height(600).title("Charts").xAxisTitle("Nominal time (h)").yAxisTitle("GIR(mg/(kg*min))").build();
        chart.addSeries("original", xData, averagedSgvs);
        chart.addSeries("new", xData, adjustedSgvs);
        new SwingWrapper(chart).displayChart();
    }
     */
}
