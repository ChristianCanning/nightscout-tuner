import java.time.*;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Float.NaN;

// Class to do most of the math involved. However, some of the math is also in the chart class because it pertains to specific charts.
public class Calculations
{
    // This method is no longer used. It was used to lay the foundation for getNetBasals. This gets the total insulin delivered by basals
    // and temp basals. This method can only be used one day at a time. To access multiple dates, use a loop to access one day at a time.
    public static double getTotalBasals(TempBasal[] tempBasals, BasalProfile[] basalProfiles)
    {
        //Take only one basal profile for this period. Hence why this won't work on multiple days if the basal rates were changed. This also means that the code won't
        //account for the basal rate changing in the middle of the day. However, ideally the user will only specify date ranges in which they didn't change their basal profile.

        //Move the basal profile to an arrayList so that we can add a pseudo basal.
        ArrayList<Basal> profile =  new ArrayList<>();
        Collections.addAll(profile, basalProfiles[0].getProfile());

        // This is the pseudo basal we add.
        // The pseudo basal is used for the loop so that when running the true last basals (position profile.length-2),
        // we can still compare it to the end of the day because the last basal runs throughout the rest of the day. The actual rate
        // of this pseudo basal is never accessed; however, as a precaution, the value is set to NaN so that the code errors out if accessed.
        profile.add(new Basal(NaN, LocalTime.of(23, 23, 59)));

        double basalInsulin = 0; // Variable to hold the sum of basalInsulin in a day
        //Loop through all the tempBasals.
        for(int i = 0; i < tempBasals.length-1; i++)
        {
            // The expected difference is the duration that the temp basal should have lasted. Sometimes (but not usually), this value is inaccurate, or the duration finishes and it
            // doesn't do another temp basal, but instead goes back to the basal profile. To account for this, we have to compare the initial duration length reported
            // with how long the basal actually ran.
            double expectedDifference = tempBasals[i].getDuration() * 60;
            //The actual difference is the difference between start of current temp basal and start of next temp basal in seconds
            double actualDifference = Duration.between(tempBasals[i].getCreated_at().toLocalDateTime(), tempBasals[i+1].getCreated_at().toLocalDateTime()).getSeconds();

            // If the durations between the start of the current temp basal and the start of the next temp basal doesn't match the duration within the
            // current temp basal.
            if(expectedDifference != actualDifference)
            {
                //Loop through every basal in the basal profile, excluding the pseudo basal
                for(int j = 0; j < profile.size()-1; j++)
                {
                    LocalTime basalStart = profile.get(j).getTime(); // Get the start time of the basal
                    LocalTime basalEnd = profile.get(j+1).getTime(); // Get the end time of the basal (the start time of the next basal)
                    LocalTime tempStart = tempBasals[i].getCreated_at().toLocalTime(); // tempStart is the start of the current temp basal

                    // 'profileStart' is when the tempBasal finishes and starts doing the regular basal from the basal profile. If the stated duration in the current tempBasal
                    // goes over the start of the next temp basal, then the duration is incorrect, so only set
                    // profileStart to be the start of the next temp basal, effectively not running the code beneath because the profileStart is equal to the profileEnd.
                    LocalTime profileStart = (tempBasals[i].getCreated_at().plusSeconds((long)(tempBasals[i].getDuration() * 60)).compareTo(tempBasals[i+1].getCreated_at()) > 0) ?
                            tempBasals[i+1].getCreated_at().toLocalTime() : tempStart.plusSeconds((long)(tempBasals[i].getDuration() * 60));
                    LocalTime profileEnd = tempBasals[i+1].getCreated_at().toLocalTime(); //profileEnd is when the next tempBasal begins and the regular basal in the basal profile stops

                    if (profileStart.compareTo(basalStart) <= 0 && profileEnd.compareTo(basalEnd) >= 0)
                    {
                        long duration = Duration.between(tempBasals[i].getCreated_at().toLocalDateTime().plusSeconds((long)(tempBasals[i].getDuration() * 60)), tempBasals[i + 1].getCreated_at().toLocalDateTime()).getSeconds();
                        basalInsulin += profile.get(j).getValue() * ((duration / 60.0) / 60.0);
                    }
                    else if (profileStart.compareTo(basalStart) >= 0 && profileStart.compareTo(basalEnd) <= 0)
                    {
                        long duration;
                        if (profileEnd.compareTo(basalEnd) <= 0)
                        {
                            duration = Duration.between(profileStart, profileEnd).getSeconds();
                        }
                        else
                        {
                            duration = Duration.between(profileStart, basalEnd).getSeconds();
                        }
                        basalInsulin += profile.get(j).getValue() * ((duration / 60.0) / 60.0);
                    }
                    else if (profileEnd.compareTo(basalStart) >= 0 && profileEnd.compareTo(basalEnd) <= 0)
                    {
                        long duration = Duration.between(basalStart, profileEnd).getSeconds();
                        basalInsulin += profile.get(j).getValue() * ((duration / 60.0) / 60.0);
                    }
                }
            }
            //Add the temp basal insulin. If the time that the temp basal starts plus the duration is less than the next created_at, then everything is normal.
            // If it is greater, then Nightscouts logs are incorrect and the actual time it ran was the difference between the temp basal start time and the next temp basal start.
            basalInsulin += (tempBasals[i].getCreated_at().plusSeconds((long)(tempBasals[i].getDuration() * 60)).compareTo(tempBasals[i+1].getCreated_at()) > 0) ?
                    tempBasals[i].getRate() * (actualDifference/3600.0) : tempBasals[i].getRate() * (tempBasals[i].getDuration() / 60.0);
        }
        //This code assumes that after the last temp basal, the rest of the day was on a normal basal rate. While this is not ideal, testing showed this to have minimal effects on reporting and calculations.
        return basalInsulin; // return total insulin taken in the day due to basals and temp basals.
    }


    // Get the net basals in a day. The net basals are what each basal rate was at, at any point in the day, taking into account temp basals and
    // the basal profile. Each entry in the ArrayList returned is in the format of a tempBasal, which has when the basal begins and how long it lasts.
    public static ArrayList<TempBasal> getNetBasals(TempBasal[] tempBasals, BasalProfile[] basalProfiles)
    {
        ArrayList<TempBasal> netBasals = new ArrayList<>(); // Create ArrayList to hold all the basals.

        //Take only one basal profile for this period. Hence why this won't work on multiple days if the basal rates were changed. This also means that the code won't
        //account for the basal rate changing in the middle of the day. Until this is addressed, to get the maximum benefit from this code, ideally users will need to make basal
        //profile changes late in the day to minimize calculation errors.
        ArrayList<Basal> profile =  new ArrayList<>();
        Collections.addAll(profile, basalProfiles[0].getProfile());

        // This is the pseudo basal we add.
        // The pseudo basal is used for the loop so that when at the true last basals (position profile.length-2),
        // we can still compare it to the end of the day because the last basal runs throughout the rest of the day. The actual rate
        // of this pseudo basal is never accessed; however, as a precaution, the value is set to NaN so that the code errors out if accessed.
        profile.add(new Basal(NaN, LocalTime.of(23, 23, 59)));

        for(int i = 0; i < tempBasals.length-1; i++)
        {
            // The expected difference is the duration that the temp basal is reported to have lasted. Sometimes (but not usually), this value is inaccurate, or the duration finishes and it
            // doesn't do another temp basal, but instead goes back to the basal profile. To account for this, we have to compare how long it was reported there
            // is until the next temp basal starts, and how long there actually was.
            double expectedDifference = tempBasals[i].getDuration() * 60;
            //The actual difference is the difference between start of current temp basal and start of next temp basal in seconds
            double actualDifference = Duration.between(tempBasals[i].getCreated_at().toLocalDateTime(), tempBasals[i+1].getCreated_at().toLocalDateTime()).getSeconds();

            // If the durations between the start of the current temp basal and the start of the next temp basal doesn't match the duration within the
            // current temp basal.
            if(expectedDifference != actualDifference)
            {
                //Loop through every basal in the basal profile, excluding the pseudo basal
                for(int j = 0; j < profile.size()-1; j++)
                {
                    LocalTime basalStart = profile.get(j).getTime(); // Start of our current basal in the basal profile
                    LocalTime basalEnd = profile.get(j+1).getTime(); // end of our current basal (start of the next basal) in the basal profile
                    LocalTime tempStart = tempBasals[i].getCreated_at().toLocalTime();
                    // tempStart is the start of the current temp basal
                    // 'profileStart' is when the tempBasal finishes and starts doing the regular basal from the basal profile. If the stated duration in the current tempBasal
                    // goes over the start of the next temp basal, then the duration reported is incorrect, so only set
                    // profileStart to be the start of the next temp basal, effectively not running the code beneath because the profileStart is equal to the profileEnd.
                    LocalTime profileStart = (tempBasals[i].getCreated_at().plusSeconds((long)(tempBasals[i].getDuration() * 60)).compareTo(tempBasals[i+1].getCreated_at()) > 0) ?
                            tempBasals[i+1].getCreated_at().toLocalTime() : tempStart.plusSeconds((long)(tempBasals[i].getDuration() * 60));
                    LocalTime profileEnd = tempBasals[i+1].getCreated_at().toLocalTime(); //profileEnd is when the next tempBasal begins and the regular basal in the basal profile stops
                    if (profileStart.compareTo(basalStart) <= 0 && profileEnd.compareTo(basalEnd) >= 0)
                    {
                        int duration = (int) Duration.between(tempBasals[i].getCreated_at().toLocalDateTime().plusSeconds((long)(tempBasals[i].getDuration() * 60)), tempBasals[i + 1].getCreated_at().toLocalDateTime()).getSeconds();
                        if (duration != 0)
                            netBasals.add(new TempBasal(profile.get(j).getValue(), (duration / 60.0), tempBasals[i].getCreated_at().toLocalDate().atTime(profileStart).atZone(ZoneId.systemDefault())));
                    }
                    else if (profileStart.compareTo(basalStart) >= 0 && profileStart.compareTo(basalEnd) <= 0)
                    {
                        int duration;
                        if (profileEnd.compareTo(basalEnd) <= 0)
                        {
                            duration = (int) Duration.between(profileStart, profileEnd).getSeconds();
                        }
                        else
                        {
                            duration = (int) Duration.between(profileStart, basalEnd).getSeconds();
                        }
                        if (duration != 0)
                            netBasals.add(new TempBasal(profile.get(j).getValue(), (duration / 60.0), tempBasals[i].getCreated_at().toLocalDate().atTime(profileStart).atZone(ZoneId.systemDefault())));
                    }
                    else if (profileEnd.compareTo(basalStart) >= 0 && profileEnd.compareTo(basalEnd) <= 0)
                    {
                        int duration = (int) Duration.between(basalStart, profileEnd).getSeconds();
                        if (duration != 0)
                            netBasals.add(new TempBasal(profile.get(j).getValue(), (duration / 60.0), tempBasals[i].getCreated_at().toLocalDate().atTime(profileStart).atZone(ZoneId.systemDefault())));

                    }
                }
            }
            //Add the temp basal insulin. If the time that the temp basal starts plus the duration is less than the next created_at, then everything is normal.
            // If it is greater, then Nightscouts logs are incorrect and the actual time it ran was the difference between the temp basal start time and the next temp basal start.
            if (tempBasals[i].getCreated_at().plusSeconds((long)(tempBasals[i].getDuration() * 60)).compareTo(tempBasals[i+1].getCreated_at()) > 0)
                netBasals.add(new TempBasal(tempBasals[i].getRate(), actualDifference/60.0, tempBasals[i].getCreated_at()));
            else
                netBasals.add(new TempBasal(tempBasals[i].getRate(), tempBasals[i].getDuration(), tempBasals[i].getCreated_at()));
        }
        //This code assumes that after the last temp basal, the rest of the day was on a normal basal rate. This is not ideal, but testing shows little effect overall to calculations.

        Collections.sort(netBasals); // Make sure the netBasals are sorted based on their times, since they could be out of order
        return netBasals; // Return arraylist consisting of entries that show when a basal starts, and how long it lasts
    }

    // Calculates the average basal over every period specified. The period is in minutes. So using a period of 30 would return an array consisting of the
    // average basal every 30 minutes.
    public static double[] getBasalAverage(ArrayList<TempBasal> netBasals, int period)
    {
        // Create array to hold averages. 1440 minutes in a day. 1440 minutes / period gets how many positions we need
        double[] basalAverage = new double[1440/period];
        for(int i = 0; i < basalAverage.length; i++)
        {
            // For every position in basalAverage, loop through the netBasals
            for (TempBasal netBasal : netBasals)
            {
                // basalAverageStart is the start time of the current basalAverage position currently being processed.
                LocalTime basalAverageStart = LocalTime.of(0, 0).plusMinutes(i * period);
                // basalAverageEnd is the end time of the current basalAverage position currently being processed (or the start time of the next basal average).
                // The basalAverageEnd is nonInclusive, so that on the final loop, it does not go past 23 hours, 59 mins, and 59 seconds. If it went to
                // 24 hours, then it would loop back around to 0 hours, 0 mins, and 0 seconds.
                LocalTime basalAverageEnd = basalAverageStart.plusSeconds(period * 60 - 1);
                LocalTime netStart = netBasal.getCreated_at().toLocalTime(); // The start time of the current netBasal
                LocalTime netEnd = netStart.plusSeconds((long) (netBasal.getDuration() * 60)); // End time of current netBasal

                if (netStart.compareTo(basalAverageStart) <= 0 && netEnd.compareTo(basalAverageEnd) >= 0) {
                    long duration = Duration.between(basalAverageStart, basalAverageEnd).getSeconds();
                    basalAverage[i] += netBasal.getRate() * (duration / 3600.0);
                } else if (netStart.compareTo(basalAverageStart) >= 0 && netStart.compareTo(basalAverageEnd) <= 0) {
                    long duration;
                    if (netEnd.compareTo(basalAverageEnd) >= 0) {
                        duration = Duration.between(netStart, basalAverageEnd).getSeconds();
                    } else {
                        duration = Duration.between(netStart, netEnd).getSeconds();
                    }
                    basalAverage[i] += netBasal.getRate() * (duration / 3600.0);
                } else if (netEnd.compareTo(basalAverageStart) >= 0 && netEnd.compareTo(basalAverageEnd) <= 0) {
                    long duration = Duration.between(basalAverageStart, netEnd).getSeconds();
                    basalAverage[i] += netBasal.getRate() * (duration / 3600.0);
                }
            }
        }
        return basalAverage;
    }

    // Return an array of the average carbs on board for every minute in a day.
    public static double[] getAverageCOB(MealBolus[] mealBoluses, double absorptionRate, ZonedDateTime currentDay, ZonedDateTime dateEnd)
    {
        //absorptionRate = carbs absorbed per hour
        double minuteRate = absorptionRate / 60.0; // carbs absorbed per minute
        double[] COB = new double[1440]; // COB array to hold COB for every minute in a day
        double[] count = new double[1440];
        for(MealBolus i : mealBoluses)
        {
            int minutes = i.getTimestamp().getHour() * 60 + i.getTimestamp().getMinute(); // Get the minute that the carbs were taken
            int pos = minutes + 10; // Assume that carbs start entering the blood stream around 10 minutes later
            double amount = i.getCarbs(); // Amount of carbs taken

            //Decrease carbs on board over time using the minuteRate until we hit 0 or reach the end of the day
            while(amount > 0 && pos < 1440)
            {
                COB[pos] += amount;
                count[pos]++;
                pos++;
                amount -= minuteRate;
            }
            // If we had reached the end of the day before carbs were fully absorbed, loop back around to the start of the day
            if(pos >= 1440 && currentDay.plusDays(1).compareTo(dateEnd) < 0)
            {
                pos = 0;
                while(amount > 0 && pos < 1440)
                {
                    COB[pos] += amount;
                    count[pos]++;
                    pos++;
                    amount -= minuteRate;
                }
            }
        }
        //Average each position of the carbs on board
        for(int i = 0; i < COB.length; i++)
        {
            if(count[i] > 0)
                COB[i] += COB[i]/count[i];
        }
        return COB;
    }


    // The getSmallYData, getMediumYData, and getLargeYData below return the insulin GIR (Glucose Infusion Rate) curves for .1u/kg, .2u/kg, and .4u/kg (units of insulin per kilogram of body weight) respectively.
    // Literature published on insulin aspart (Fiasp) and insulin lispro (Humalog) show little difference to the overall calculations required for this programs main objective--auto-calculating basals.
    // Additionally, I compared the difference between the published duration of insulin activity curves and the GIR curves. I chose the GIR curves for insulin aspart because those values will produce
    // similar values to other fast acting insulins. I used the raw data for the published .1u/kg, .2u/kg, and .4u/kg curves and plotted them in excel.
    // I then found a polynomial equation for each curve that covered more than 99 % of the original
    // data points, with the intention of smoothing out any irregularities in the original data set that could affect stuff later. The equations
    // listed in each of these methods are the equations that almost perfectly cover each data set. Each method returns the data with the x axis updating in 15 second intervals.
    public static double[] getSmallYData(double[] smallXData)
    {
        //using the .1 U/kg curve
        double[] smallYData = new double[1920];
        // y = 0.0033820425120803x5 - 0.0962642502970792x4 + 1.0161233494860400x3 - 4.7280409167367000x2 + 8.2811624637053000x - 0.4658832073238300
        for(int i = 0; i < smallYData.length; i++)
        {
            double x = smallXData[i];
            double y = 0.0033820425120803 * Math.pow(x, 5) - 0.0962642502970792 * Math.pow(x, 4) + 1.0161233494860400 * Math.pow(x, 3) -
                    4.7280409167367000 * Math.pow(x, 2) + 8.2811624637053000 * x - 0.4658832073238300;
            smallYData[i] = y;
        }
        return smallYData;
    }
    public static double[] getMediumYData(double[] mediumXData)
    {
        //Using the .2 U/kg curve
        double[] mediumYData = new double[1920];
        //y = 0.0004449113905105x6 - 0.0097881251143144x5 + 0.0487062677027909x4 + 0.3395509285035820x3 - 3.8635372657493500x2 + 9.8215306047782600x - 0.5016675029655920
        // Medium curve
        for(int i = 0; i < mediumXData.length; i++)
        {
            double x = mediumXData[i];
            double y = 0.0004449113905105 * Math.pow(x, 6) - 0.0097881251143144 * Math.pow(x, 5) + 0.0487062677027909 * Math.pow(x, 4) +
                    0.3395509285035820 * Math.pow(x, 3) - 3.8635372657493500 * Math.pow(x, 2) + 9.8215306047782600 * x - 0.5016675029655920;
            mediumYData[i] = y;
        }
        return mediumYData;
    }
    public static double[] getLargeYData(double[] largeXData)
    {
        //Using the .4 U/kg curve
        double[] largeYData = new double[1920];

        //y = -0.0224550824431891x4 + 0.5324819868175370x3 - 4.2740977490209200x2 + 11.6354217632198000x - 0.0653457810255797
        // Large curve
        for(int i = 0; i < largeXData.length; i++)
        {
            double x = largeXData[i];
            double y = -0.0224550824431891 * Math.pow(x, 4) + 0.5324819868175370 * Math.pow(x, 3) - 4.2740977490209200 * Math.pow(x, 2) +
                    11.6354217632198000 * x - 0.0653457810255797;
            largeYData[i] = y;
        }
        return largeYData;
    }

    // This method was used to find the pattern for how the GIR curves for insulin/kg changed as the curves became smaller.
    // It currently isn't referenced anywhere, as the data gathered here was used to find an equation.
    // The method works by finding the % that each point on the .1kg(small) curve is to the same point on the .2u/kg(medium) curve, as well as the
    // % between each point in the .2u/kg(medium) and .4u/kg(large) curves. For example, say at x = 5, the .1u/kg y value was at 4, the .2u/kg y value
    // was at 7, and the .4u/kg value was at 10. To compare the .1u/kg and .2u/kg, i would do 4/7 = .571, and for comparing the .2u/kg and .4u/kg curve I would do
    // 7/10 = .7 . I did this for every corresponding x value on each curve, and then I compared the percentages between each other. So I did
    // .571 / .7 = .815. I then plotted all these compared percentages in excel and got a fairly linear equation that I was able to use to calculate
    // what percentage of the .1u/kg curve any insulin/kg below .1u/kg would be.
    public static double[] getComparison(double[] smallYData, double[] mediumYData, double[] largeYData)
    {
        double[] smallMedium = new double[smallYData.length];
        for(int i = 0; i < smallMedium.length; i++)
        {
            smallMedium[i] = smallYData[i]/mediumYData[i];
        }
        double[] mediumLarge = new double[largeYData.length];
        for(int i = 0; i < mediumLarge.length; i++)
        {
            mediumLarge[i] = mediumYData[i]/largeYData[i];
        }
        double[] comparison = new double[1920];
        for(int i = 0; i < smallMedium.length; i++)
        {
            comparison[i] = smallMedium[i]/mediumLarge[i];
        }

        return comparison;
    }

    // Return the average Duration of Insulin Activity for each 5 minute period in the day. DIA is equal to the length of the GIR curve.
    public static double[] getDIA(CorrectionBolus[] correctionBoluses, String url, ZonedDateTime dateStart, ZonedDateTime dateEnd, double weight, int insulinPool)
    {
        double[] basalAverage = Chart.averageBasals(url, dateStart, dateEnd, 5, false); // Average basals every 5 minutes
        // Create array that keeps track of insulin for every 5 minute position. There are 288 5-minute positions in a 24 hour period,
        // however, this program uses 576 positions to make the code easier to account for wrapping around to the next day later on. This program uses modifyInsulin objects
        // so that it can have multiple positions in the array reference the same insulin amount. When calculating the effects of insulin on BG, the program has to account for
        // the effect of insulin administered at the end of the day because it will have an impact on BG values well into the next day. With this in mind, positions 0-143 represent the second half of the day,
        // from 12:00 to 23:55. Positions 144-432 represent the entire day of 00:00 to 23:55. Positions 433-575 represent the first half of the day,
        // from 00:00 to 11:55. Again, the use of positions 0-143 and 433-575 are used to accurately model the effect insulin delivered late in the day has on the next day. For example, if I gave
        // 10 units of insulin at 23:00, not all of it would be absorbed by 00:00, so the program needs to account for the effect of the remaining insulin by wrapping around to the beginning
        // of the next day. Just to be clear, there are only 288 modifyInsulin objects, so every position that references a specific time is also referenced somewhere else--meaning that
        // if the program modifies the insulin at position 144 (00:00), position 289 still equal position 144 (with both holding the new value).
        CorrectionBolus[] insulinDeliveredArr = new CorrectionBolus[576];

        //Add the basal insulin for each 5 minute period to insulinDeliveredArr.
        for(int i = 0; i < basalAverage.length; i++)
        {
            int hour = i * 5 / 60;
            int minute = i * 5 - (60 * hour);
            insulinDeliveredArr[i+144] = new CorrectionBolus(basalAverage[i]/12, LocalTime.of(hour, minute));
        }

        // Set positions 0-143 equal to positions 288-432
        System.arraycopy(insulinDeliveredArr, 288, insulinDeliveredArr, 0, 144);
        // Set positions 432-575 equal to positions 144-287
        System.arraycopy(insulinDeliveredArr, 144, insulinDeliveredArr, 432, 144);

        //Add correction bolus insulin to insulinDeliveredArr.
        for (CorrectionBolus correctionBolus : correctionBoluses)
        {
            int pos = (correctionBolus.getTimestamp().getHour() * 60 + correctionBolus.getTimestamp().getMinute()) / 5;
            insulinDeliveredArr[pos + 144].setInsulin(insulinDeliveredArr[pos + 144].getInsulin() + correctionBolus.getInsulin());
        }

        // Get the Duration of Insulin Activity for each 5 minute period. DIA is used so that when this program models lowering blood glucose value
        // by an amount, we know how many positions after that to subsequently also lower the same amount. This method gets the insulin from the previous positions
        // based on what the insulinPool is set to. InsulinPool is used to model the pooling of insulin delivery and
        // its effect on insulin absorption in the body. If the insulinPool is set to 30 minutes, the duration of insulin activity for a seleccted 5 minute period
        // is based on the the total insulin delivered over the previous 30 minutes to generate an insulin curve.
        // The length of the insulin curve is the calculated duration of insulin activity for the specified 5 minute position.
        double[] DIA = new double[576];
        for(int i = 144; i < 432; i++)
        {
            double insulin = 0;
            for(int j = i - insulinPool/5; j < i; j++)
            {
                insulin += insulinDeliveredArr[j].getInsulin();
            }
            DIA[i] += Chart.GIRCurve(insulin/weight, false).length * 15.0 / 60.0 / 5.0;
        }

        // Set positions 0-143 equal to positions 288-432
        System.arraycopy(DIA, 288, DIA, 0, 144);
        // Set positions 432-575 equal to positions 144-287
        System.arraycopy(DIA, 144, DIA, 432, 144);
        return DIA;
    }





}
