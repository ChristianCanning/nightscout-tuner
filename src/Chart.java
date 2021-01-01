import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.text.DecimalFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Chart
{
    // Show the average BGs during the specified period. If showChart is set to false, the chart is not shown, however the average BGs are returned
    // in an array.
    public static double[] averageBGs(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, boolean showChart)
    {
        BG[] bgs = ParseJSON.getBG(url, dateStart, dateEnd); // Get all BGs between dates
        int duration = (int)Duration.between(dateStart.toLocalDateTime(), dateEnd.toLocalDateTime()).toDays(); // Count number of days
        BG[][] bgMatrix = new BG[duration][288]; // Create a matrix so that each row contains the BGs for one day

        // Create array to count the number of BGs that are added for each column in bgMatrix (so the same time, but different day. These are the
        // values we want to average.) We need to keep count of the number of BGs in each column, because on some days the sensor may have stopped
        // reading, leading to less blood glucose values in specific columns. This is why we can't just sum up each column and divide by the number
        // of rows.
        int[] countBGs = new int[288];
        int day = 0;
        int count = 0;
        for(int i = 0; i < bgs.length; i++)
        {
            if(i != 0 && bgs[i].getDate().toLocalDate().compareTo(bgs[i-1].getDate().toLocalDate()) > 0)
            {
                day++;
                count = 0;
            }
            // gap is the difference between the previous bg and the current one. If the gap is greater than 5 minutes, then we know there's a gap in readings
            int gap = (count == 0) ? 0 : (int)Duration.between(bgs[i-1].getDate().toLocalDateTime(), bgs[i].getDate().toLocalDateTime()).toMinutes();
            if(gap > 5)
            {

                count += gap/5 -1;
                bgMatrix[day][count] = bgs[i];
                countBGs[count] += 1;
                count++;
            }
            else
            {
                bgMatrix[day][count] = bgs[i];
                countBGs[count] += 1;
                count++;
            }

        }


        double[] summedBGs = new double[288]; // Array to sum up all the columns in the bgMatrix
        for (BG[] matrix : bgMatrix) {
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[j] != null)
                    summedBGs[j] += matrix[j].getBG();
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
            Thread chart = new Thread(() -> new BGChart(xData, averagedBGs, dateStart, dateEnd));
            chart.start();
        }

        return averagedBGs;
    }

    //Show average basals over the course of dateStart to dateEnd in intervals based on the period(in minutes). If  showChart is set to false,
    // the chart is not shown, and it returns an array of basals averaged in intervals based on the period(in mins) given.
    public static double[] averageBasals(String urlString, ZonedDateTime dateStart, ZonedDateTime dateEnd, int period, boolean showChart)
    {
        int days = (int)ChronoUnit.DAYS.between(dateStart.toLocalDate(), dateEnd.toLocalDate()); // Number of days between dateStart and dateEnd
        double[] averagedBasals = new double[1440/period]; // Array to hold average basals

        // Loop through to get the net basals(basals and temp basals together) for one day at a time, because Calculations.getNetBasals()
        // can only reliably get the basals for one day at a time. We add the basals one day at a time to the averagedBasals
        for(int i = 0; i < days; i++)
        {
            ZonedDateTime currentDay = dateStart.toLocalDateTime().plusDays(i).atZone(ZoneId.systemDefault());
            ZonedDateTime nextDay = currentDay.plusDays(1);

            TempBasal[] tempBasals = ParseJSON.getTempBasal(urlString, currentDay, nextDay);
            BasalProfile[] basalProfiles = ParseJSON.getBasalProfile(currentDay, nextDay);

            ArrayList<TempBasal> netBasals = Calculations.getNetBasals(tempBasals, basalProfiles);
            int j = 0;
            // Add basal averages for the currentDay to our averagedBasals array
            for(double insulin : Calculations.getBasalAverage(netBasals, period))
            {
                averagedBasals[j] += insulin;
                j++;
            }
        }

        // Turn the sums of the basals into averages
        for(int i = 0; i < averagedBasals.length; i++)
        {
            averagedBasals[i] = averagedBasals[i]/days * (60.0/period);
        }

        // Generate the xData for our chart
        double[] xData = new double[averagedBasals.length];
        for(int i =0; i < xData.length; i++)
        {
            xData[i] = i;
        }

        if(showChart)
        {
            Thread chart = new Thread(() -> new BasalChart(xData, averagedBasals, dateStart, dateEnd));

            chart.start();
        }

        return averagedBasals;
    }

    // Display a chart with the average Carbs on Board updated every minute over the course of the dates given.
    public static void averageCOB(String urlString, ZonedDateTime dateStart, ZonedDateTime dateEnd, double hourRate)
    {
        // The hourRate is how many carbs can be absorbed by the user within an hour
        int days = (int)ChronoUnit.DAYS.between(dateStart.toLocalDate(), dateEnd.toLocalDate()); // Number of days
        double[] averaged = new double[1440]; // Array to hold average COB for every minute in the day
        int[] count = new int[1440]; // Array to hold the count so we can divide each position in averaged by the correct amount to get an average

        // Loop goes one day at a time, getting the carbs on board for the day and adding it to the correct positions in the averaged array.
        // This creates a sum of the carbs on board for each position.
        for(int i = 0; i < days; i++)
        {
            ZonedDateTime currentDay = dateStart.toLocalDateTime().plusDays(i).atZone(ZoneId.systemDefault());
            ZonedDateTime nextDay = currentDay.plusDays(1);

            MealBolus[] mealBoluses = ParseJSON.getMealBolus(urlString, currentDay, nextDay);

            double[] COB = Calculations.getAverageCOB(mealBoluses, hourRate, currentDay, dateEnd);
            for(int j = 0; j < COB.length; j++)
            {
                if (COB[j] > 0)
                {
                    count[j] += 1;
                    averaged[j] += COB[j];
                }
            }
        }

        // Divide the sums of the COB by the count to get the actual average
        for(int i = 0; i < averaged.length; i++)
        {
            averaged[i] = averaged[i]/count[i];
        }

        // Create the xData for the chart
        double[] xData = new double[1440];
        for(int i =0; i < xData.length; i++)
        {
            xData[i] = i;
        }

        Thread chart = new Thread(() -> new COBChart(xData, averaged, dateStart, dateEnd));

        chart.start();

    }

    // Returns the Glucose Infusion Rate (GIR) curve for the insulin/kg given.
    public static double[] GIRCurve(double insulinKG, boolean showChart)
    {
        //insulinKG refers to insulin/kg (insulin divided by kilograms)

        //Note: Anywhere in my code, small refers to the GIR curve for .1 units/kg and medium refers to the GIR curve for .2 units/kg

        double[] smallXData = new double[1920]; // Generate xData for .1 units/kg GIR curve updating every 15 seconds over an 8 hour duration
        double[] mediumXData = new double[1920]; // Generate xData for .2 units/kg GIR curve updating every 15 seconds over an 8 hour duration

        // Fill up each xData with the appropriate values. The xData for each has units of hours, not seconds, not minutes. So when each xData
        // position increases by 15 seconds, we are actually increasing it by ~.00416667 hours.
        for(int i = 0; i < 1920; i++)
        {
            double x = i * (15.0/3600);
            smallXData[i] = x;
            mediumXData[i] = x;
        }
        double[] smallYData = Calculations.getSmallYData(smallXData); // Populate the yData for the .1 units/kg curve
        double[] mediumYData = Calculations.getMediumYData(mediumXData); // Populate the yData for the .2 units/kg curve

        // smallMedium refers to small/medium (small divided by medium). For each point in the .1 units/kg GIR curve, it is divided by
        // the corresponding point in the .2 units/kg GIR curve. This gets the ratio of each point between the .1 units/kg curve and the .2 units/kg curve
        double[] smallMedium = new double[smallYData.length];
        for(int i = 0; i < smallMedium.length; i++)
        {
            smallMedium[i] = smallYData[i]/mediumYData[i];
        }

        //Create an array to hold the y data for our new curve that we need to generate based on what the insulinKG was input as
        double[] newCurveY = new double[1920];
        // ****NOTE**** I need to explain the calculations below properly, so I'll come back and explain this once I complete the github page
        // for this project
        for(int i = 0; i < newCurveY.length; i++)
        {
            //y = -1.4426950408889700ln(x) - 3.3219280948873900
            double pow = -1.44269504088897 * Math.log(insulinKG) - 3.32192809488739;

            //y = -0.0455826595478078x + 0.9205489113464720
            double yRate = -0.0455826595478078 * smallXData[i] + 0.9205489113464720;
            double yDiff = Math.pow(yRate, pow) * smallMedium[i];
            double yMultiplier = Math.pow(yDiff, pow);

            double value = smallYData[i] * yMultiplier;
            if(i != 0 && Math.abs(newCurveY[i-1] - value) > .05)
                newCurveY[i] = newCurveY[i-1];
            else
                newCurveY[i] = value;
        }

        // Find the peak value in the new GIR curve that we just generated based off of the insulinKG
        double peakValue = 0;
        for(double i : newCurveY)
        {
            if(i > peakValue)
                peakValue = i;
        }

        // 'stop' refers to when we want to stop the curve that we just generated. The curve will be 8 hours long, but after a certain point in
        // that 8 hours, the y data for the GIR curve is almost 0, so we need to stop it. Right now, I've decided that a GIR curve is essentially
        // complete once its y data reaches 1% of its peak value.
        double stop = peakValue *.01;
        int count = 0; // count keeps track of how many positions we are going to include in the curve (the number of positions before stop)
        for(int i = 0; i < newCurveY.length; i++)
        {
            //y = -1.4426950408889700ln(x) - 3.3219280948873900
            double pow = -1.44269504088897 * Math.log(insulinKG) - 3.32192809488739;
            //y = -0.0455826595478078x + 0.9205489113464720
            double yRate = -0.0455826595478078 * smallXData[i] + 0.9205489113464720;
            double yDiff = Math.pow(yRate, pow) * smallMedium[i];
            double yMultiplier = Math.pow(yDiff, pow);

            double value = smallYData[i] * yMultiplier;
            if(i != 0 && Math.abs(newCurveY[i-1] - value) > .05)
                newCurveY[i] = newCurveY[i-1];
            else
                newCurveY[i] = value;

            if(!Double.isNaN((15.0/3600) * (smallYData[i] * yMultiplier)) && (15.0) * (smallYData[i] * yMultiplier) > 0)
                count++;
            if(i > 50 && newCurveY[i] < stop)
                i = 9999999;
        }

        //Below we copy over the GIR curve for our insulinKG, but we "chop" off the points after the yData goes below 1% of the peak in the GIR curve.
        // We do this using our count that we got for the number of positions that exist before the yData goes below 1% of its peak.
        double[] newCurveChopY = new double[count];
        double[] newCurveChopX = new double[count];
        int count0 = 0; // Count the number of data points that are below 0 at the beginning of our curve. We use this so that we don't include them
        for(int i = 0; i < newCurveChopY.length+count0; i++)
        {
            double x = (i-count0) * (15.0/3600);
            if(newCurveY[i] > 0)
            {
                newCurveChopY[i-count0] += newCurveY[i];
                newCurveChopX[i-count0] += x;
            }
            else
            {
                count0++;
            }
        }

        if(showChart)
        {
            XYChart chart = new XYChartBuilder().width(800).height(600).title("Charts").xAxisTitle("Nominal time (h)").yAxisTitle("GIR(mg/(kg*min))").build();
            chart.addSeries(insulinKG + "", newCurveChopX, newCurveChopY);
            new SwingWrapper(chart).displayChart();
        }

        return newCurveChopY;
    }

    // Return the percentage of the GIR curve that the currentTime is plus the next 5 minutes.
    public static double getGIRCurvePercentage(double[] curveY, double currentTime)
    {
        // curveY is the yData for the GIR curve for whatever insulin/kg we are using.
        //The currentTime is the time since the insulin was delivered on the GIR curve. The currentTime is in the format of hours.

        //get the x position that the currentTime would fall on the GIR curve. We divide the currentTime(which is in hours) by how many
        // hours 15 seconds is. (15 seconds / 60 seconds in a minute / 60 minutes in an hour)
        int pos = (int)Math.round(currentTime/(15.0/3600));

        // Get the total area under the GIR curve
        double totalArea = 0;
        for (double v : curveY) {
            totalArea += v * (15.0 / 3600);
        }

        // Get our partial area under the GIR curve for the currentTime plus the next 5 minutes
        double partialArea = 0;
        for(int i = 0; i < 20 && i+pos < curveY.length; i++)
        {
            partialArea += curveY[pos+i] * (15.0/3600);
        }

        // Return what percentage of the GIR curve our currentTime plus the next 5 minutes would be.
        return partialArea/totalArea;
    }

    public static String[][] adjustAverageBGs(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, int min, double isf, int period, double weight, double[] DIA, double[] basalAverages)
    {
        DecimalFormat df = new DecimalFormat("#.##");


        double averageDIA = 0;
        for(int i = 144; i < 432; i++)
        {
            averageDIA += DIA[i];
        }
        averageDIA = averageDIA/288;

        double[] averagedBGs = averageBGs(url, dateStart, dateEnd, false);
        BG[] adjustedBGs = new BG[576];
        for(int i = 0; i < averagedBGs.length; i++)
        {
            int hour = i * 5 / 60;
            int minute = i * 5 - (60 * hour);
            adjustedBGs[i+144] = new BG(averagedBGs[i], LocalTime.of(hour, minute));
        }
        System.arraycopy(adjustedBGs, 288, adjustedBGs, 0, 144);
        System.arraycopy(adjustedBGs, 144, adjustedBGs, 432, 144);
        BG[] tempBGs = new BG[adjustedBGs.length];
        for(int i = 0; i < adjustedBGs.length; i++)
        {
            tempBGs[i] = new BG(adjustedBGs[i].getBG(), adjustedBGs[i].getTime());
        }
        double[] xData = new double[averagedBGs.length];
        for(int i = 0; i < xData.length; i++)
            xData[i] = i * 5;


        double totalInsulin = 0;





        double[] extraInsulin = new double[1440/period];


        double[] insulinDelivered = new double[basalAverages.length];
        for(int b = 0; b < insulinDelivered.length; b++)
        {
            insulinDelivered[b] = basalAverages[b]/(60.0/period);
        }

        System.out.println("Adjusting BGs... ");
        double totalRaise = 0;
        double previousTotalRaise = -1;
        double count = 0;
        while(totalRaise - previousTotalRaise > 0)
        {
            count++;
            System.out.println(df.format(totalInsulin) + " units applied(subtract insulin)");
            previousTotalRaise = totalRaise;
            for(int i = 144; i < adjustedBGs.length-144; i+=period/5)
            {
                double insulin = .01;
                boolean canRun = false;
                while(!canRun)
                {
                    double raise = insulin * isf;
                    double[] curveY = GIRCurve(insulin/weight, false);
                    for(int j = i; j < i + period/5; j++)
                    {
                        double length = DIA[j];
                        for(int k = j; k < length + j; k++)
                        {
                            double currentTime = (k -j) * 5.0 / 60;
                            double percentage = getGIRCurvePercentage(curveY, currentTime);
                            for(int l = k; l < length + j; l++)
                            {
                                tempBGs[l].setBg(tempBGs[l].getBG() + (raise * percentage));
                            }
                            for(int l = k; l < length + j; l++)
                            {
                                double currentInsulin = insulinDelivered[(i-144)/(period/5)];
                                if(tempBGs[k].getBG() < min)
                                    canRun = true;
                                if(currentInsulin - insulin * 6 <= 0)
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
                            tempBGs[a] = new BG(adjustedBGs[a].getBG(), adjustedBGs[a].getTime());
                        }
                    }
                    else
                    {
                        canRun = true;
                        insulin -= .01;
                    }
                }
                    insulin *= .005;
                if(insulin > 0)
                {
                    insulinDelivered[(i-144)/(period/5)] -= insulin * 6;
                    for(int j = i; j < i + period/5; j++)
                    {
                        double raise = insulin * isf;
                        totalInsulin -= insulin;
                        double[] curveY = GIRCurve(insulin/weight, false);
                        double length = DIA[j];
                        for(int k = j; k < length + j; k++)
                        {
                            double currentTime = (k -j) * 5.0 / 60;
                            double percentage = getGIRCurvePercentage(curveY, currentTime);
                            totalRaise += raise * percentage;
                            extraInsulin[(i-144)/(period/5)] -= insulin * percentage;
                            for(int l = k; l < length + j; l++)
                            {
                                adjustedBGs[l].setBg(adjustedBGs[l].getBG() + (raise * percentage));
                            }
                        }
                    }
                }

                for(int a = 0; a < adjustedBGs.length; a++)
                {
                    tempBGs[a] = new BG(adjustedBGs[a].getBG(), adjustedBGs[a].getTime());
                }
            }
        }

        double totalDrop = 0;
        double previousTotalDrop = -4;

        while(totalDrop - previousTotalDrop > 0)
        {
            count++;
            System.out.println(df.format(totalInsulin) + " units applied(add insulin)");
            previousTotalDrop = totalDrop;
            for(int i = 144; i < adjustedBGs.length-144; i+=period/5)
            {
                double insulin = .01;
                boolean canRun = false;
                while(!canRun)
                {
                    canRun = true;
                    double drop = insulin * isf;
                    double[] curveY = GIRCurve(insulin/weight, false);
                    for(int j = i; j < i + period/5; j++)
                    {
                        double length = DIA[j];
                        for(int a = j - 1; length <= 0; a--)
                        {
                            length = DIA[a];
                        }
                        for(int k = j; k < length + j; k++)
                        {
                            double currentTime = (k -j) * 5.0 / 60;
                            double percentage = getGIRCurvePercentage(curveY, currentTime);
                            for(int l = k; l < length + j; l++)
                            {
                                if(tempBGs[l].getBG() - (drop * percentage) < min)
                                    canRun = false;
                                tempBGs[l].setBg(tempBGs[l].getBG() - (drop * percentage));
                            }
                        }
                    }
                    if(canRun)
                    {
                        insulin += .01;
                        canRun = false;
                        for(int a = 0; a < adjustedBGs.length; a++)
                        {
                            tempBGs[a] = new BG(adjustedBGs[a].getBG(), adjustedBGs[a].getTime());
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
                        double[] curveY = GIRCurve(insulin/weight, false);
                        double length = DIA[j];
                        for(int k = j; k < length + j; k++)
                        {
                            double currentTime = (k -j) * 5.0 / 60;
                            double percentage = getGIRCurvePercentage(curveY, currentTime);
                            totalInsulin += insulin * percentage;
                            extraInsulin[(i-144)/(period/5)] += insulin * percentage;
                            for(int l = k; l < length + j; l++)
                            {
                                adjustedBGs[l].setBg(adjustedBGs[l].getBG() - (drop * percentage));
                            }
                        }
                    }
                }
                for(int a = 0; a < adjustedBGs.length; a++)
                {
                    tempBGs[a] = new BG(adjustedBGs[a].getBG(), adjustedBGs[a].getTime());
                }
            }
        }



        double averagedSum = 0;
        double countAverageBGs = 0;
        for(double i : averagedBGs)
        {
            if(!Double.isNaN(i))
            {
                averagedSum += i;
                countAverageBGs++;
            }
        }

        double adjustedSum = 0;
        double countAdjustedBGs = 0;
        for(int i = 144; i < 432; i++)
        {
            if(!Double.isNaN(adjustedBGs[i].getBG()))
            {
                adjustedSum += adjustedBGs[i].getBG();
                countAdjustedBGs++;
            }
        }

        System.out.println("AVG BG before: " + averagedSum/countAverageBGs);
        System.out.println("AVG BG after: " + adjustedSum/countAdjustedBGs);
        System.out.println("System Recommends: " + totalInsulin + " total units");
        double actualInsulin = (averagedSum/countAverageBGs - adjustedSum/countAdjustedBGs) / isf * (288/averageDIA);
        System.out.println("Insulin we can deliver is: " + actualInsulin + " total units");


        String[][] extraInsulinCorrected = new String[1440/period+1][4];

        extraInsulinCorrected[0][1] = "Basals";
        extraInsulinCorrected[0][2] = "Basal w/ Temp";
        extraInsulinCorrected[0][3] = "Recommended";
        BasalProfile[] basalProfiles = ParseJSON.getBasalProfile(dateStart, dateEnd);
        Basal[] basals = basalProfiles[basalProfiles.length-1].getProfile();
        for (Basal basal : basals) {
            int pos = (basal.getTime().getHour() * 60 + basal.getTime().getMinute()) / period;
            extraInsulinCorrected[pos + 1][1] = basal.getValue() + "";
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

        double[] adjustedBGsDouble = new double[288];
        for(int i = 144; i < 432; i++)
        {
            adjustedBGsDouble[i-144] = adjustedBGs[i].getBG();
        }

        Thread chart = new Thread(() -> new BGChart(xData, averagedBGs, adjustedBGsDouble, dateStart, dateEnd));
        chart.start();
        return extraInsulinCorrected;

    }

    public static String[][] manualAdjustAverageBGs(String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, int min, double isf, int period, double weight, double[] DIA, double[] basalAverages)
    {
        DecimalFormat df = new DecimalFormat("#.##");


        double averageDIA = 0;
        for(int i = 144; i < 432; i++)
        {
            averageDIA += DIA[i];
        }
        averageDIA = averageDIA/288;

        double[] averagedBGs = averageBGs(url, dateStart, dateEnd, false);
        BG[] adjustedBGs = new BG[576];
        for(int i = 0; i < averagedBGs.length; i++)
        {
            int hour = i * 5 / 60;
            int minute = i * 5 - (60 * hour);
            adjustedBGs[i+144] = new BG(averagedBGs[i], LocalTime.of(hour, minute));
        }
        System.arraycopy(adjustedBGs, 288, adjustedBGs, 0, 144);
        System.arraycopy(adjustedBGs, 144, adjustedBGs, 432, 144);

        double[] xData = new double[averagedBGs.length];
        for(int i = 0; i < xData.length; i++)
            xData[i] = i * 5;

        //IMPORTANT
        double totalInsulin = 0;
        double[] inputBasals =
        {
            1.05,  // 00:00
            2.22,  // 01:00
            2.14,  // 02:00
            0.92,  // 03:00
            1.36,  // 04:00
            1.22,  // 05:00
            1.35, // 06:00
            1.01, // 07:00
            0.94,  // 08:00
            1.5,  // 09:00
            1.84,  // 10:00
            0.89,  // 11:00
            0.62,  // 12:00
            0.52,  // 13:00
            0.64,  // 14:00
            0.49,  // 15:00
            0.72,  // 16:00
            1.08,  // 17:00
            1.46,  // 18:00
            0.78,  // 19:00
            0.66,  // 20:00
            0.93,  // 21:00
            0.97,  // 22:00
            0.65 // 23:00
        };

        double[] extraInsulin = new double[1440/period];

        double totalRaise = 0;
        for(int i = 144; i < adjustedBGs.length-145; i+=period/5)
        {
            System.out.println(i);
            double insulin = (basalAverages[(i-144)/(period/5)] - inputBasals[(i-144)/(period/5)]) / (period/5.0);
            if(insulin > 0)
            {
                for(int j = i; j < i + period/5; j++)
                {
                    double raise = insulin * isf;
                    totalInsulin -= insulin;
                    double[] curveY = GIRCurve(insulin/weight, false);
                    double length = DIA[j];
                    for(int k = j; k < length + j; k++)
                    {
                        double currentTime = (k -j) * 5.0 / 60;
                        double percentage = getGIRCurvePercentage(curveY, currentTime);
                        totalRaise += raise * percentage;
                        extraInsulin[(i-144)/(period/5)] -= insulin * percentage;
                        for(int l = k; l < length + j; l++)
                        {
                            adjustedBGs[l].setBg(adjustedBGs[l].getBG() + (raise * percentage));
                        }
                    }
                }
            }
        }
        double totalDrop = 0;
        double previousTotalDrop = -4;
        for(int i = 144; i < adjustedBGs.length-145; i+=period/5)
        {
            double insulin = (inputBasals[(i-144)/(period/5)] - basalAverages[(i-144)/(period/5)]) / (period/5.0);
            if(insulin > 0)
            {
                for(int j = i; j < i + period/5; j++)
                {
                    double drop = insulin * isf;
                    totalDrop += drop;
                    double[] curveY = GIRCurve(insulin/weight, false);
                    double length = DIA[j];
                    for(int k = j; k < length + j; k++)
                    {
                        double currentTime = (k -j) * 5.0 / 60;
                        double percentage = getGIRCurvePercentage(curveY, currentTime);
                        totalInsulin += insulin * percentage;
                        extraInsulin[(i-144)/(period/5)] += insulin * percentage;
                        for(int l = k; l < length + j; l++)
                        {
                            adjustedBGs[l].setBg(adjustedBGs[l].getBG() - (drop * percentage));
                        }
                    }
                }
            }
        }



        double averagedSum = 0;
        double countAverageBGs = 0;
        for(double i : averagedBGs)
        {
            if(!Double.isNaN(i))
            {
                averagedSum += i;
                countAverageBGs++;
            }
        }

        double adjustedSum = 0;
        double countAdjustedBGs = 0;
        for(int i = 144; i < 432; i++)
        {
            if(!Double.isNaN(adjustedBGs[i].getBG()))
            {
                adjustedSum += adjustedBGs[i].getBG();
                countAdjustedBGs++;
            }
        }

        System.out.println("AVG BG before: " + averagedSum/countAverageBGs);
        System.out.println("AVG BG after: " + adjustedSum/countAdjustedBGs);
        System.out.println("System Recommends: " + totalInsulin + " total units");
        double actualInsulin = (averagedSum/countAverageBGs - adjustedSum/countAdjustedBGs) / isf * (288/averageDIA);
        System.out.println("Insulin we can deliver is: " + actualInsulin + " total units");


        String[][] extraInsulinCorrected = new String[1440/period+1][4];

        extraInsulinCorrected[0][1] = "Basals";
        extraInsulinCorrected[0][2] = "Basal w/ Temp";
        extraInsulinCorrected[0][3] = "Recommended";
        BasalProfile[] basalProfiles = ParseJSON.getBasalProfile(dateStart, dateEnd);
        Basal[] basals = basalProfiles[basalProfiles.length-1].getProfile();
        for (Basal basal : basals) {
            int pos = (basal.getTime().getHour() * 60 + basal.getTime().getMinute()) / period;
            extraInsulinCorrected[pos + 1][1] = basal.getValue() + "";
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

        double[] adjustedBGsDouble = new double[288];
        for(int i = 144; i < 432; i++)
        {
            adjustedBGsDouble[i-144] = adjustedBGs[i].getBG();
        }

        Thread chart = new Thread(() -> new BGChart(xData, averagedBGs, adjustedBGsDouble, dateStart, dateEnd));
        chart.start();
        return extraInsulinCorrected;

    }



}
