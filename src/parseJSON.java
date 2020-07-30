import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.*;
import java.util.ArrayList;

public class parseJSON
{
    private static String profileString; // String which contains the entire Nightscout profile.

    //Set the profileString to be the profile JSON from nightscout if we haven't already grabbed the profile.
    public static void setProfile(String url)
    {
        if(profileString == null)
        {
            System.out.print("Grabbing profile JSON from Nightscout... ");
            try {
                URL json = new URL(url + "api/v1/profile.json");
                BufferedReader br = new BufferedReader(new InputStreamReader(json.openStream()));
                profileString = br.readLine();
                System.out.println("Success(" + profileString.getBytes("UTF-8").length/1024.0 + " KB)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Get all Blood Glucose values from Nightscout within the specified dates. Nightscout's JSONs refers to BGs as SGVs (sugar glucose values)
    public static bg[] getBG(String urlString, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        // Download a JSON with all the BGs and set it equal to the bgString
        String bgString = "";
        System.out.print("Grabbing BGs JSON from Nightscout... ");
        try {
            URL json = new URL(urlString + "api/v1/entries/sgv.json?find[dateString][$gte]=" + dateStart.minusDays(2).toLocalDate().toString() + "&find[dateString][$lte]=" + dateEnd.plusDays(1).toLocalDate().toString() + "&count=1000000");
            BufferedReader br = new BufferedReader(new InputStreamReader(json.openStream()));
            bgString = br.readLine();
            System.out.println("Success(" + bgString.getBytes("UTF-8").length/1024.0 + " KB)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray bgJSON = new JSONArray(bgString); // Turn bgString into a JSON array
        ArrayList<bg> bgList = new ArrayList<bg>(); // Create an ArrayList to hold all the BGs that we are going to parse
        // For each loop which grabs every 'sgv' (bg) entry and adds them to the bgList
        for(Object i : bgJSON)
        {
            JSONObject entry = (JSONObject)i;
            String dateString = entry.getString("dateString");
            ZonedDateTime date = LocalDateTime.parse(dateString.substring(0, dateString.length()-1)).atZone(ZoneId.of("GMT")).withZoneSameInstant(ZoneId.systemDefault());
            int bgValue = entry.getInt("sgv");
            if((date.compareTo(dateStart)) >= 0 && date.compareTo(dateEnd) <=0)
                bgList.add(0, new bg(bgValue, date));
        }
        return bgList.toArray(new bg[bgList.size()]); // Return bgList as an array
    }

    // Get all tempBasals from Nightscout within the specified dates
    public static tempBasal[] getTempBasal(String urlString, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        // Download a JSON with all the treatments and set it equal to the treatmentsString
        String treatmentsString = "";
        System.out.print("Grabbing treatments:tempBasals JSON from Nightscout... ");
        try {
            URL json = new URL(urlString + "api/v1/treatments.json?find[created_at][$gte]=" + dateStart.minusDays(1).toLocalDate().toString() + "&find[created_at][$lte]=" + dateEnd.plusDays(2).toLocalDate().toString() + "&find[eventType]=Temp+Basal");
            BufferedReader br = new BufferedReader(new InputStreamReader(json.openStream()));
            treatmentsString = br.readLine();
            System.out.println("Success(" + treatmentsString.getBytes("UTF-8").length/1024.0 + " KB)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray tempBasalJSON = new JSONArray(treatmentsString); // Turn treatmentsString into a JSON array
        ArrayList<tempBasal> tempBasalList= new ArrayList<tempBasal>(); // Create ArrayList to hold all the tempBasals
        // For each loop to parse the tempBasals and add them to tempBasalList
        for(Object i : tempBasalJSON)
        {
            JSONObject entry = (JSONObject)i;
            String created_atString = entry.getString("created_at");
            ZonedDateTime created_at = LocalDateTime.parse(created_atString.substring(0, created_atString.length()-1)).atZone(ZoneId.of("GMT")).withZoneSameInstant(ZoneId.systemDefault());
            double rate = entry.getDouble("rate");
            double duration = entry.getDouble("duration");
            if((created_at.compareTo(dateStart)) >= 0 && created_at.compareTo(dateEnd) <=0)
                tempBasalList.add(0, new tempBasal(rate, duration, created_at));
        }
        return tempBasalList.toArray(new tempBasal[tempBasalList.size()]); // return all the temp basals as an array
    }

    // Get all the correction boluses (non basal insulin deliveries, such as insulin delivered for meals) within the specified dates.
    public static correctionBolus[] getCorrectionBolus(String urlString, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        // Download a JSON with all the treatments and set it equal to the treatmentsString
        String treatmentsString = "";
        System.out.print("Grabbing treatments:correctionBoluses JSON from Nightscout... ");
        try {
            URL json = new URL(urlString + "api/v1/treatments.json?find[created_at][$gte]=" + dateStart.minusDays(1).toLocalDate().toString() + "&find[created_at][$lte]=" + dateEnd.plusDays(2).toLocalDate().toString() + "&find[eventType]=Correction+Bolus");
            BufferedReader br = new BufferedReader(new InputStreamReader(json.openStream()));
            treatmentsString = br.readLine();
            System.out.println("Success(" + treatmentsString.getBytes("UTF-8").length/1024.0 + " KB)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray correctionBolusJSON = new JSONArray(treatmentsString); // Turn treatmentsString into a JSON array
        ArrayList<correctionBolus> correctionBolusList= new ArrayList<correctionBolus>(); // Create ArrayList to hold all the correction boluses

        //For each loop to grab all the correction boluses and put them in the correctionBolusList
        for(Object i : correctionBolusJSON)
        {
            JSONObject entry = (JSONObject)i;
            String created_atString = entry.getString("timestamp");
            ZonedDateTime timestamp = LocalDateTime.parse(created_atString.substring(0, created_atString.length()-1)).atZone(ZoneId.of("GMT")).withZoneSameInstant(ZoneId.systemDefault());
            double insulin = entry.getDouble("insulin");
            if((timestamp.compareTo(dateStart)) >= 0 && timestamp.compareTo(dateEnd) <=0)
                correctionBolusList.add(0, new correctionBolus(insulin, timestamp));

        }
        return correctionBolusList.toArray(new correctionBolus[correctionBolusList.size()]);
    }

    // Get all the meal boluses within the specified dates. Curiously, meal boluses only contain data for the carbs, and not the insulin delivered.
    // the insulin delivered from meals is stored within the correction boluses
    public static mealBolus[] getMealBolus(String urlString, ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        // Download a JSON with all the treatments and set it equal to the treatmentsString
        String treatmentsString = "";
        System.out.print("Grabbing treatments:mealBoluses JSON from Nightscout... ");
        try {
            URL json = new URL(urlString + "api/v1/treatments.json?find[created_at][$gte]=" + dateStart.minusDays(1).toLocalDate().toString() + "&find[created_at][$lte]=" + dateEnd.plusDays(2).toLocalDate().toString() + "&find[eventType]=Meal+Bolus");
            BufferedReader br = new BufferedReader(new InputStreamReader(json.openStream()));
            treatmentsString = br.readLine();
            System.out.println("Success(" + treatmentsString.getBytes("UTF-8").length/1024.0 + " KB)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray mealBolusJSON = new JSONArray(treatmentsString); // Turn treatmentsString into a JSON array
        ArrayList<mealBolus> mealBolusList= new ArrayList<mealBolus>(); // Create ArrayList to hold all the meal boluses
        // For each loop to put every meal bolus into the mealBolusList
        for(Object i : mealBolusJSON)
        {
            JSONObject entry = (JSONObject)i;
            String created_atString = entry.getString("timestamp");
            ZonedDateTime timestamp = LocalDateTime.parse(created_atString.substring(0, created_atString.length()-1)).atZone(ZoneId.of("GMT")).withZoneSameInstant(ZoneId.systemDefault());
            int carbs = entry.getInt("carbs");
            int absorptionTime = entry.getInt("absorptionTime");
            if((timestamp.compareTo(dateStart)) >= 0 && timestamp.compareTo(dateEnd) <=0)
                mealBolusList.add(0, new mealBolus(carbs, absorptionTime, timestamp));

        }
        return mealBolusList.toArray(new mealBolus[mealBolusList.size()]); // Return mealBolusList as an array
    }

    // Get all the basalProfiles within the specified dates. Profiles are uploaded to nighscout at irregular intervals, so some days may not have a
    // profile uploaded. If that's the case, we increment back by one day until we find a basal profile.
    public static basalProfile[] getBasalProfile(ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        // *The profile string is a private variable at the top of this class.*
        JSONArray profileJSON = new JSONArray(profileString); // Turn the profile string into a JSON array
        ArrayList<basalProfile> basalProfileList = new ArrayList<basalProfile>(); // Create arraylist to hold the basal profiles
        // Create previousProfile1 which will hold the previous profile (*1* profile prior) as we loop through the profileJSON. This is initialized to 1970 to make sure that it is
        // always before the first date that we check in the profileJSON
        basalProfile previousProfile1 = new basalProfile(new basal[0], LocalDateTime.parse("1970-01-01T00:00").atZone(ZoneId.of("GMT")));
        // previousProfile2 is the same as previousProfile1, except it will be 2 positions previous (*2* profiles prior). So previousProfile1 gets set to previousProfile2 after
        // each loop, and previousProfile1 gets set to the profile we just looped through.
        basalProfile previousProfile2 = new basalProfile(new basal[0], LocalDateTime.parse("1970-01-01T00:00").atZone(ZoneId.of("GMT")));
        //loop through every profile in profileJSON. Each profile will contain a basal profile
        for(Object i : profileJSON)
        {
            JSONObject entry = (JSONObject)i;
            String startDateString = entry.getString("startDate");
            // The start date is the date that the current basal profile begins
            ZonedDateTime startDate = LocalDateTime.parse(startDateString.substring(0, startDateString.length()-1)).atZone(ZoneId.of("GMT")).withZoneSameInstant(ZoneId.systemDefault());
            JSONObject store = entry.getJSONObject("store");
            JSONObject Default = store.getJSONObject("Default");
            JSONArray basal = Default.getJSONArray("basal");
            basal[] basalArray = new basal[basal.length()]; // Create array of basals to hold in the basal profile
            // Add each basal to the basal profile
            for(int j =0; j < basal.length(); j++)
            {
                JSONObject basalEntry = basal.getJSONObject(j);
                double value = basalEntry.getDouble("value");
                LocalTime time = LocalTime.parse(basalEntry.getString("time"));
                basalArray[j] = new basal(value, time);
            }

            // *Remember that dateStart and dateEnd are our start and end dates for grabbing profiles*
            // What we want to do with these if statements is check to make sure that the profile we are currently checking falls within the dateStart
            // and dateEnd range. We can't just check to see if the profile's start is in the range of dateStart and dateEnd, because the profile
            // could start before our dateStart and dateEnd, and continue into or past the date range. Also, the start date of a profile is the date
            // that is held within the basalProfile object, however the end date of the basal profile is simply the start date of the next basal profile

            // If the previousProfile1's date is not between dateStart and dateEnd
            if(!(previousProfile1.getStartDate().compareTo(dateStart) >= 0 && previousProfile1.getStartDate().compareTo(dateEnd) <=0))
            {
                //If the previousProfile1's date is before dateEnd and the current profile's date is before dateEnd and previousProfile2's date is after dateStart
                if (previousProfile1.getStartDate().compareTo(dateEnd) <= 0 && startDate.compareTo(dateEnd) <= 0 && previousProfile2.getStartDate().compareTo(dateStart) >= 0)
                    basalProfileList.add(0, previousProfile1);
            }
            // If the current profile's date is after dateStart and before dateEnd
            if(startDate.compareTo(dateStart) >= 0 && startDate.compareTo(dateEnd) <=0)
                basalProfileList.add(0, new basalProfile(basalArray, startDate));
            previousProfile2 = new basalProfile(previousProfile1.getProfile(), previousProfile1.getStartDate()); // Set previousProfile2 to previousProfile1
            previousProfile1 = new basalProfile(basalArray, startDate); // Set previousProfile1 to the currentProfile

        }

        // Do a recursion loop on this method if we failed to find any basal profiles within the specified dates.
        // Continues going backwards by one day until we find a basal profile.
        for(int i = 1; basalProfileList.size() == 0; i++)
        {
            basalProfile[] temp = getBasalProfile(dateStart.minusDays(i), dateEnd);
            if(temp.length > 0)
            {
                for(basalProfile a : temp)
                {
                    basalProfileList.add(a);
                }
            }
        }

        return basalProfileList.toArray(new basalProfile[basalProfileList.size()]); // Return the basalProfile list as an array
    }

    // Grab the carbratioProfiles within the specified dates. The rest of the code below follows the same concept as the basalProfile.
    // Refer to the getBasalProfile method for further explanation.
    public static carbratioProfile[] getCarbratioProfile(ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        JSONArray profileJSON = new JSONArray(profileString);
        ArrayList<carbratioProfile> carbratioProfileList = new ArrayList<carbratioProfile>();
        carbratioProfile previousProfile1 = new carbratioProfile(new carbratio[0], LocalDateTime.parse("1970-01-01T00:00").atZone(ZoneId.of("GMT")));
        carbratioProfile previousProfile2 = new carbratioProfile(new carbratio[0], LocalDateTime.parse("1970-01-01T00:00").atZone(ZoneId.of("GMT")));
        for(Object i : profileJSON)
        {
            JSONObject entry = (JSONObject)i;
            String startDateString = entry.getString("startDate");
            ZonedDateTime startDate = LocalDateTime.parse(startDateString.substring(0, startDateString.length()-1)).atZone(ZoneId.of("GMT")).withZoneSameInstant(ZoneId.systemDefault());
            JSONObject store = entry.getJSONObject("store");
            JSONObject Default = store.getJSONObject("Default");
            JSONArray carbratio = Default.getJSONArray("carbratio");
            carbratio[] carbratioArray = new carbratio[carbratio.length()];
            for(int j =0; j < carbratio.length(); j++)
            {
                JSONObject basalEntry = carbratio.getJSONObject(j);
                double value = basalEntry.getDouble("value");
                LocalTime time = LocalTime.parse(basalEntry.getString("time"));
                carbratioArray[j] = new carbratio(value, time);
            }
            if(!(previousProfile1.getStartDate().compareTo(dateStart) >= 0 && previousProfile1.getStartDate().compareTo(dateEnd) <=0))
            {
                if (previousProfile1.getStartDate().compareTo(dateEnd) <= 0 && startDate.compareTo(dateEnd) <= 0 && previousProfile2.getStartDate().compareTo(dateStart) >= 0)
                    carbratioProfileList.add(0, previousProfile1);
            }
            if((startDate.compareTo(dateStart)) >= 0 && startDate.compareTo(dateEnd) <=0)
                carbratioProfileList.add(0, new carbratioProfile(carbratioArray, startDate));
            previousProfile2 = new carbratioProfile(previousProfile1.getProfile(), previousProfile1.getStartDate());
            previousProfile1 = new carbratioProfile(carbratioArray, startDate);
        }
        return carbratioProfileList.toArray(new carbratioProfile[carbratioProfileList.size()]);
    }

    // Grab the ISF (Insulin Sensitivity Factor) profiles within the specified dates. The rest of the code below follows the same concept
    // as the basalProfile. Refer to the getBasalProfile method for further explanation. Nightscout JSONs refer to ISF as 'sens'.
    public static isfProfile[] getIsfProfile(ZonedDateTime dateStart, ZonedDateTime dateEnd)
    {
        JSONArray profileJSON = new JSONArray(profileString);
        ArrayList<isfProfile> isfProfileList = new ArrayList<isfProfile>();
        isfProfile previousProfile1 = new isfProfile(new isf[0], LocalDateTime.parse("1970-01-01T00:00").atZone(ZoneId.of("GMT")));
        isfProfile previousProfile2 = new isfProfile(new isf[0], LocalDateTime.parse("1970-01-01T00:00").atZone(ZoneId.of("GMT")));
        for(Object i : profileJSON)
        {
            JSONObject entry = (JSONObject)i;
            String startDateString = entry.getString("startDate");
            ZonedDateTime startDate = LocalDateTime.parse(startDateString.substring(0, startDateString.length()-1)).atZone(ZoneId.of("GMT")).withZoneSameInstant(ZoneId.systemDefault());
            JSONObject store = entry.getJSONObject("store");
            JSONObject Default = store.getJSONObject("Default");
            JSONArray sens = Default.getJSONArray("sens");
            isf[] isfArray = new isf[sens.length()];
            for(int j =0; j < sens.length(); j++)
            {
                JSONObject basalEntry = sens.getJSONObject(j);
                double value = basalEntry.getDouble("value");
                LocalTime time = LocalTime.parse(basalEntry.getString("time"));
                isfArray[j] = new isf(value, time);
            }
            if(!(previousProfile1.getStartDate().compareTo(dateStart) >= 0 && previousProfile1.getStartDate().compareTo(dateEnd) <=0))
            {
                if (previousProfile1.getStartDate().compareTo(dateEnd) <= 0 && startDate.compareTo(dateEnd) <= 0 && previousProfile2.getStartDate().compareTo(dateStart) >= 0)
                    isfProfileList.add(0, previousProfile1);
            }
            if((startDate.compareTo(dateStart)) >= 0 && startDate.compareTo(dateEnd) <=0)
                isfProfileList.add(0, new isfProfile(isfArray, startDate));
            previousProfile2 = new isfProfile(previousProfile1.getProfile(), previousProfile1.getStartDate());
            previousProfile1 = new isfProfile(isfArray, startDate);
        }
        return isfProfileList.toArray(new isfProfile[isfProfileList.size()]);
    }

}
