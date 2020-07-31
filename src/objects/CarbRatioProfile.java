import java.time.*;

// The CarbRatioProfile object holds an array of CarbRatios for the day, known as 'profile'. The CarbRatioProfile holds data for the day it
// was active, known as 'startDate'. Contrary to the name of 'startDate', this is only a day at which the profile was active, which may or may not be
// the actual day that the profile started. Loop uploads the profile multiple times to Nightscout, even if it wasn't updated.
public class CarbRatioProfile
{
    private final CarbRatio[] profile;
    private final ZonedDateTime startDate;

    public CarbRatioProfile(CarbRatio[] profile, ZonedDateTime startDate)
    {
        this.profile = profile.clone();
        this.startDate = startDate;
    }

    public CarbRatio[] getProfile()
    {
        return profile;
    }
    public ZonedDateTime getStartDate()
    {
        return startDate;
    }
}
