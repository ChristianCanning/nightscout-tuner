import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.text.DecimalFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;

public class chart
{
    // Show the average BGs during the specified period. If showChart is set to false, the chart is not shown, however the average BGs are returned
    // in an array.
    public static double[] averageBGs(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, boolean showChart)
    {
        bg[] bgs = parseJSON.getBG(url, dateStart, dateEnd); // Get all BGs between dates
        int duration = (int)Duration.between(dateStart.toLocalDateTime(), dateEnd.toLocalDateTime()).toDays(); // Count number of days
        bg[][] bgMatrix = new bg[duration][288]; // Create a matrix so that each row contains the BGs for one day

        // Create array to count the number of BGs that are added for each column in bgMatrix (so the same time, but different day. These are the
        // values we want to average.) We need to keep count of the number of BGs in each column, because on some days the sensor may have stopped
        // reading, leading to less blood glucose values in specific columns. This is why we can't just sum up each column and divide by the number
        // of rows.
        int[] countBGs = new int[288];
        int day = 0;
        int count = 0;
        for(int i = 1; i < bgs.length; i++)
        {
            if(bgs[i].getDate().toLocalDate().compareTo(bgs[i-1].getDate().toLocalDate()) > 0)
            {
                day++;
                count = 0;
            }
            int gap = (int)Duration.between(bgs[i-1].getDate().toLocalDateTime(), bgs[i].getDate().toLocalDateTime()).toMinutes();
            if(gap > 5)
            {
                count += gap/5 -1;
                bgMatrix[day][count] = new bg(0, bgs[i].getDate());
                countBGs[count] += 1;
            }
            else
            {
                bgMatrix[day][count] = bgs[i];
                countBGs[count] += 1;
                count++;
            }

        }

        double[] summedBGs = new double[288]; // Array to sum up all the columns in the bgMatrix
        for(int i = 0; i < bgMatrix.length; i++)
        {
            for(int j = 0; j < bgMatrix[i].length; j++)
            {
                if(bgMatrix[i][j] != null)
                    summedBGs[j] += bgMatrix[i][j].getBG();
            }
        }
        double[] averagedBGs = new double[288]; // Array to average each BG sum using the counts we got
        for(int i = 0; i < averagedBGs.length; i++)
        {
            averagedBGs[i] = summedBGs[i]/countBGs[i];
        }


        double[] xData = new double[288]; // Create the xData for the chart when we display it. This increases by 5 minute intervals.
        for(int i =0; i < xData.length; i++)
        {
            xData[i] = 5*i;
        }

        // Show the chart if we had showChart set to true when we called this method
        if(showChart)
        {
            Thread chart = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    new bgChart(xData, averagedBGs, dateStart, dateEnd);
                }
            });
            chart.start();
        }

        return averagedBGs;


    }
    public static double[] averageBasals(String urlString, ZonedDateTime dateStart, ZonedDateTime dateEnd, int period, boolean showChart)
    {

        int diff = (int)ChronoUnit.DAYS.between(dateStart.toLocalDate(), dateEnd.toLocalDate());
        double[] averaged = new double[1440/period+1];
        for(int i = 0; i < diff; i++)
        {
            ZonedDateTime currentDay = dateStart.toLocalDateTime().plusDays(i).atZone(ZoneId.systemDefault());
            ZonedDateTime nextDay = currentDay.plusDays(1);

            tempBasal[] tempBasals = parseJSON.getTempBasal(urlString, currentDay, nextDay);
            basalProfile[] basalProfiles = parseJSON.getBasalProfile(currentDay, nextDay);
            ZonedDateTime tempCurrent =  currentDay.toLocalDateTime().atZone(ZoneId.systemDefault());
            int count = 0;
            while (basalProfiles.length == 0)
            {
                tempCurrent = tempCurrent.minusDays(1);
                basalProfiles = parseJSON.getBasalProfile(tempCurrent, nextDay);
                if(count > 2)
                {
                    throw new java.lang.Error("ERROR IN AVERAGEBASALS IN CHART. THE BASALPROFILE WAS EMPTY FOR TOO LONG");
                }
            }

            ArrayList<tempBasal> netBasals = calculations.getNetBasals(tempBasals, basalProfiles);
            Collections.sort(netBasals);
            int j = 0;
            for(double insulin : calculations.getBasalAverage(netBasals, period))
            {
                averaged[j] += insulin;
                j++;
            }
        }
        for(int i = 0; i < averaged.length-1; i++)
        {
            averaged[i] = averaged[i]/diff * (60.0/period);
        }

        averaged[1440/period] = averaged[1440/period-1];
        double[] xData = new double[1440/period+1];
        for(int i =0; i < xData.length; i++)
        {
            xData[i] = i;
        }

        if(showChart)
        {
            Thread chart = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    new basalChart(xData, averaged, dateStart, dateEnd);
                }
            });

            chart.start();
        }

        double[] averagedReturned = new double[averaged.length-1];
        for(int i = 0; i < averagedReturned.length; i++)
        {
            averagedReturned[i] = averaged[i];
        }
        return averagedReturned;
    }
    public static void averageCOB(String urlString, ZonedDateTime dateStart, ZonedDateTime dateEnd, double hourRate)
    {
        int diff = (int)ChronoUnit.DAYS.between(dateStart.toLocalDate(), dateEnd.toLocalDate());
        double[] averaged = new double[1440];
        int[] count = new int[1440];
        for(int i = 0; i < diff; i++)
        {
            ZonedDateTime currentDay = dateStart.toLocalDateTime().plusDays(i).atZone(ZoneId.systemDefault());
            ZonedDateTime nextDay = currentDay.plusDays(1);

            mealBolus[] mealBoluses = parseJSON.getMealBolus(urlString, currentDay, nextDay);
            ZonedDateTime tempCurrent =  currentDay.toLocalDateTime().atZone(ZoneId.systemDefault());

            double[] COB = calculations.getAverageCOB(mealBoluses, hourRate, currentDay, dateEnd);
            for(int j = 0; j < COB.length; j++)
            {
                if (COB[j] > 0)
                {
                    count[j] += 1;
                    averaged[j] += COB[j];
                }
            }
        }
        for(int i = 0; i < averaged.length; i++)
        {
            averaged[i] = averaged[i]/count[i];
        }

        double[] xData = new double[1440];
        for(int i =0; i < xData.length; i++)
        {
            xData[i] = i;
        }

        Thread chart = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                new COBChart(xData, averaged, dateStart, dateEnd);
            }
        });

        chart.start();

    }

    public static double[] insulinCurve(double insulinKG)
    {
        double[] smallXData = new double[1920];
        double[] mediumXData = new double[1920];
        double[] largeXData = new double[1920];
        for(int i = 0; i < 1920; i++)
        {
            double x = i * (15.0/3600);
            smallXData[i] = x;
            mediumXData[i] = x;
            largeXData[i] = x;
        }
        double[] smallYData = calculations.getSmallYData(smallXData);
        double[] mediumYData = calculations.getMediumYData(mediumXData);
        double[] largeYData = calculations.getLargeYData(largeXData);


        double[] smallMedium = new double[smallYData.length];
        for(int i = 0; i < smallMedium.length; i++)
        {
            smallMedium[i] = smallYData[i]/mediumYData[i];
        }

        double area = 0;
        double[] newline = new double[1920];
        for(int i = 0; i < newline.length; i++)
        {
            //y = -1.4426950408889700ln(x) - 3.3219280948873900
            double pow = -1.44269504088897 * Math.log(insulinKG) - 3.32192809488739;

            //y = -0.0455826595478078x + 0.9205489113464720
            double yRate = -0.0455826595478078 * smallXData[i] + 0.9205489113464720;
            double yDiff = Math.pow(yRate, pow) * smallMedium[i];
            double yMultiplier = Math.pow(yDiff, pow);

            if(!Double.isNaN((15.0) * (smallYData[i] * yMultiplier)) && (15.0) * (smallYData[i] * yMultiplier) > 0)
            {
                area += (15.0/3600) * (smallYData[i] * yMultiplier);
                //System.out.println(area += (15.0/3600) * (smallYData[i] * yMultiplier));
            }
            double value = smallYData[i] * yMultiplier;
            if(i != 0 && Math.abs(newline[i-1] - value) > .05)
                newline[i] = newline[i-1];
            else
                newline[i] = value;

        }
        //System.out.println(area);
        double peakValue = 0;
        for(double i : newline)
        {
            if(i > peakValue)
                peakValue = i;
        }
        double stop = peakValue *.01;
        double currentArea = 0;
        int count = 0;
        //System.out.println(stop);
        for(int i = 0; i < newline.length; i++)
        {
            //y = -1.4426950408889700ln(x) - 3.3219280948873900
            double pow = -1.44269504088897 * Math.log(insulinKG) - 3.32192809488739;
            //y = -0.0455826595478078x + 0.9205489113464720
            double yRate = -0.0455826595478078 * smallXData[i] + 0.9205489113464720;
            double yDiff = Math.pow(yRate, pow) * smallMedium[i];
            double yMultiplier = Math.pow(yDiff, pow);
            //System.out.println(smallXData[i] + ", " + (smallYData[i] * yMultiplier));

            double value = smallYData[i] * yMultiplier;
            if(i != 0 && Math.abs(newline[i-1] - value) > .05)
                newline[i] = newline[i-1];
            else
                newline[i] = value;



            if(!Double.isNaN((15.0/3600) * (smallYData[i] * yMultiplier))&& (15.0) * (smallYData[i] * yMultiplier) > 0)
            {
                currentArea += (15.0/3600) * (smallYData[i] * yMultiplier);
                count++;
            }
            if(i > 50 && newline[i] < stop)
                i = 100000;


            //System.out.println(currentArea);
            //System.out.println(area);
        }
        double[] newlineChopY = new double[count];
        double[] newlineChopX = new double[count];
        int start = 0;
        for(int i = 0; i < newlineChopY.length; i++)
        {
            double x = (i-start) * (15.0/3600);
            if(newline[i] > 0)
            {
                newlineChopY[i-start] += newline[i];
                newlineChopX[i-start] += x;
            }
            else
            {
                start++;
            }
        }
        //System.out.println(area/3600.0);

        //XYChart chart = new XYChartBuilder().width(800).height(600).title("Charts").xAxisTitle("Nominal time (h)").yAxisTitle("GIR(mg/(kg*min))").build();
        //chart.addSeries(".1 U/KG", smallXData, smallYData);
        //chart.addSeries(".2 U/KG", mediumXData, mediumYData);
        //chart.addSeries(".4 U/KG", largeXData, largeYData);
        //chart.addSeries("Comparison", newlineChopX, newlineChopY);
        //new SwingWrapper(chart).displayChart();

        return newlineChopY;
    }
    public static double getInsulinCurvePercentage(double[] curveY, double currentTime)
    {
        //CurrentTime has to be in hours
        int pos = 0;
        double[] curveX = new double[curveY.length];
        for(int i = 0; i < curveX.length; i++)
        {
            curveX[i] = i * (15.0/3600);
            if((i * (15.0/3600)) == currentTime)
            {
                pos = i;
            }
        }
        double totalArea = 0;
        for(int i = 0; i < curveY.length; i++)
        {
            totalArea += curveY[i] * (15.0/3600);
        }
        double partialArea = 0;
        for(int i = 0; i < 20 && i+pos < curveY.length; i++)
        {
            partialArea += curveY[pos+i] * (15.0/3600);
        }
        return partialArea/totalArea;
    }

    public static String[][] adjustAverageBGs(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, int min, double isf, int period, double weight, double[] dia, double[] basalAverages)
    {
        //min = 120;
        double avgDia = 0;
        for(int i = 144; i < 432; i++)
        {
            avgDia += dia[i];
        }
        avgDia = avgDia/288;
        double[] averagedBGs = averageBGs(url, dateStart, dateEnd, false);
        modifyBG[] adjustedBGs = new modifyBG[576];
        for(int i = 0; i < averagedBGs.length; i++)
        {
            int hour = i * 5 / 60;
            int minute = i * 5 - (60 * hour);
            adjustedBGs[i+144] = new modifyBG(averagedBGs[i], LocalTime.of(hour, minute));
        }
        for(int i = 288; i < 432; i++)
        {
            adjustedBGs[i-288] = adjustedBGs[i];
        }
        for(int i = 144; i < 288; i++)
        {
            adjustedBGs[i+288] = adjustedBGs[i];
        }
        modifyBG[] tempBGs = new modifyBG[adjustedBGs.length];
        for(int i = 0; i < adjustedBGs.length; i++)
        {
            tempBGs[i] = new modifyBG(adjustedBGs[i].getBg(), adjustedBGs[i].getTime());
        }
        double[] xData = new double[averagedBGs.length];
        for(int i = 0; i < xData.length; i++)
            xData[i] = i * 5;




        double totalInsulin = 0;
        double totalDrop = 0;
        double previousTotalDrop = -2;
        double[] extraInsulin = new double[1440/period];
        while(totalDrop - previousTotalDrop > 1)
        {
            previousTotalDrop = totalDrop;
            System.out.println(totalInsulin);
            System.out.println(totalDrop);
            for(int i = 144; i < adjustedBGs.length-144; i+=period/5)
            {
                //System.out.println(i);
                //System.out.println(i);
                double insulin = .01;
                boolean canRun = false;
                while(!canRun)
                {
                    //System.out.println(insulin);
                    canRun = true;
                    double drop = insulin * isf;
                    double[] curveY = insulinCurve(insulin/weight);
                    for(int j = i; j < i + period/5; j++)
                    {
                        double length = dia[j];
                        for(int a = j - 1; length <= 0; a--)
                        {
                            length = dia[a];
                        }
                        for(int k = j; k < length + j; k++)
                        {
                            double currentTime = (k -j) * 5.0 / 60;
                            double percentage = getInsulinCurvePercentage(curveY, currentTime);
                            for(int l = k; l < length + j; l++)
                            {
                                if(tempBGs[l].getBg() - (drop * percentage) < min)
                                    canRun = false;
                                tempBGs[l].setBg(tempBGs[l].getBg() - (drop * percentage));
                            }
                        }
                    }
                    if(canRun)
                    {
                        insulin += .01;
                        canRun = false;
                        for(int a = 0; a < adjustedBGs.length; a++)
                        {
                            tempBGs[a] = new modifyBG(adjustedBGs[a].getBg(), adjustedBGs[a].getTime());
                        }
                    }
                    else
                    {
                        insulin -= .01;
                        canRun = true;
                    }
                }
                insulin *= .01;
                if(insulin > 0)
                {
                    for(int j = i; j < i + period/5; j++)
                    {
                        double drop = insulin * isf;
                        totalDrop += drop;
                        double[] curveY = insulinCurve(insulin/weight);
                        double length = dia[j];
                        for(int k = j; k < length + j; k++)
                        {
                            double currentTime = (k -j) * 5.0 / 60;
                            double percentage = getInsulinCurvePercentage(curveY, currentTime);
                            totalInsulin += insulin * percentage;
                            extraInsulin[(i-144)/(period/5)] += insulin * percentage;
                            for(int l = k; l < length + j; l++)
                            {
                                adjustedBGs[l].setBg(adjustedBGs[l].getBg() - (drop * percentage));
                            }
                        }
                    }
                }

                //System.out.println(totalInsulin);
                for(int a = 0; a < adjustedBGs.length; a++)
                {
                    tempBGs[a] = new modifyBG(adjustedBGs[a].getBg(), adjustedBGs[a].getTime());
                }
                //System.out.println(minimum);
            }
        }

        double totalRaise = 0;
        double previousTotalRaise = -4;
        while(totalRaise - previousTotalRaise > 1)
        {
            previousTotalRaise = totalRaise;
            System.out.println(totalInsulin);
            System.out.println(totalRaise);
            for(int i = 144; i < adjustedBGs.length-144; i+=period/5)
            {
                double insulin = .01;
                boolean canRun = false;
                while(!canRun)
                {
                    //System.out.println(insulin);
                    canRun = true;
                    double raise = insulin * isf;
                    double[] curveY = insulinCurve(insulin/weight);
                    for(int j = i; j < i + period/5; j++)
                    {
                        double length = dia[j];
                        for(int a = j - 1; length <= 0; a--)
                        {
                            length = dia[a];
                        }
                        for(int k = j; k < length + j; k++)
                        {
                            double currentTime = (k -j) * 5.0 / 60;
                            double percentage = getInsulinCurvePercentage(curveY, currentTime);
                            for(int l = k; l < length + j; l++)
                            {
                                tempBGs[l].setBg(tempBGs[l].getBg() + (raise * percentage));
                            }
                            for(int l = k; l < length + j; l++)
                            {
                                double currentInsulin = basalAverages[(i-144)/(period/5)] * (60.0/period);
                                if(tempBGs[l].getBg() > min ||  currentInsulin - (insulin * (period/5))*(60.0/period) < currentInsulin/2.0)
                                    canRun = false;
                            }
                        }
                    }
                    if(canRun)
                    {
                        canRun = false;
                        insulin += .01;
                        for(int a = 0; a < adjustedBGs.length; a++)
                        {
                            tempBGs[a] = new modifyBG(adjustedBGs[a].getBg(), adjustedBGs[a].getTime());
                        }
                    }
                    else
                    {
                        canRun = true;
                        insulin -= .01;
                    }
                }
                insulin *= .1;
                if(insulin > 0)
                {
                    for(int j = i; j < i + period/5; j++)
                    {
                        double raise = insulin * isf;
                        totalInsulin -= insulin;
                        double[] curveY = insulinCurve(insulin/weight);
                        double length = dia[j];
                        for(int k = j; k < length + j; k++)
                        {
                            double currentTime = (k -j) * 5.0 / 60;
                            double percentage = getInsulinCurvePercentage(curveY, currentTime);
                            totalRaise += raise * percentage;
                            extraInsulin[(i-144)/(period/5)] -= insulin * percentage;
                            for(int l = k; l < length + j; l++)
                            {
                                adjustedBGs[l].setBg(adjustedBGs[l].getBg() + (raise * percentage));
                            }
                        }
                    }
                }

                //System.out.println(totalInsulin);
                for(int a = 0; a < adjustedBGs.length; a++)
                {
                    tempBGs[a] = new modifyBG(adjustedBGs[a].getBg(), adjustedBGs[a].getTime());
                }
                //System.out.println(minimum);
            }
        }

        double averageSum = 0;
        for(double i : averagedBGs)
        {
            if(!Double.isNaN(i))
                averageSum += i;
        }
        System.out.println("AVG BG before: " + averageSum/averagedBGs.length);
        double adjustSum = 0;
        for(int i = 144; i < 432; i++)
        {
            if(!Double.isNaN(adjustedBGs[i].getBg()))
                adjustSum += adjustedBGs[i].getBg();
        }
        System.out.println("AVG BG after: " + adjustSum/288);
        System.out.println("was: " + totalInsulin);
        double actualInsulin = (averageSum/averagedBGs.length - adjustSum/288) / isf * (288/avgDia);
        System.out.println("should have been: " + actualInsulin);

        String[][] extraInsulinCorrected = new String[1440/period+1][4];
        DecimalFormat df = new DecimalFormat("#.##");
        extraInsulinCorrected[0][1] = "Basals";
        extraInsulinCorrected[0][2] = "Basal w/ Temp";
        extraInsulinCorrected[0][3] = "Recommended";
        basalProfile[] basalProfiles = parseJSON.getBasalProfile(dateStart, dateEnd);
        basal[] basals = basalProfiles[basalProfiles.length-1].getProfile();
        for(int i = 0; i < basals.length; i++)
        {
            int pos = (basals[i].getTime().getHour() * 60 + basals[i].getTime().getMinute())/period;
            extraInsulinCorrected[pos+1][1] = basals[i].getValue() + "";
        }
        for(int i = 1; i < extraInsulinCorrected.length; i++)
        {
            String value = extraInsulinCorrected[i][1];
            for(int j = i+1; j < extraInsulin.length && extraInsulinCorrected[j][1] == null; j++)
            {
                if(extraInsulinCorrected[j][1] == null)
                    extraInsulinCorrected[j][1] = value;
            }
        }
        if(extraInsulinCorrected[extraInsulinCorrected.length-1][1] == null)
            extraInsulinCorrected[extraInsulinCorrected.length-1][1] = extraInsulinCorrected[extraInsulinCorrected.length-2][1];
        for(int i = 0; i < extraInsulin.length; i++)
        {
            extraInsulinCorrected[i+1][0] = LocalTime.of(0, 0).plusMinutes(i * period).toString();
            extraInsulinCorrected[i+1][2] = df.format(basalAverages[i]);
            extraInsulinCorrected[i+1][3] = df.format(extraInsulin[i] * (actualInsulin/totalInsulin) * (60.0/period) + basalAverages[i]);
        }




        double[] test = new double[288];
        for(int i = 144; i < 432; i++)
        {
            test[i-144] = adjustedBGs[i].getBg();
        }
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Charts").xAxisTitle("Nominal time (h)").yAxisTitle("GIR(mg/(kg*min))").build();
        chart.getStyler().setYAxisMax(250.0);
        chart.getStyler().setYAxisMin(50.0);
        chart.addSeries("original", xData, averagedBGs);
        chart.addSeries("new", xData, test);
        new SwingWrapper(chart).displayChart();

        return extraInsulinCorrected;

    }




}
