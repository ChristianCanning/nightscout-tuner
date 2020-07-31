import java.time.*;

// The Insulin Sensitivity Factor profile, which contains an array of ISFs for a day, known as 'profile'. The ISFProfile holds data for the day it
// was active, known as 'startDate'. Contrary to the name of 'startDate', this is only a day at which the profile was active, which may or may not be
// the actual day that the profile started. Loop uploads the profile multiple times to Nightscout, even if it wasn't updated.
public class ISFProfile
{
    private final ISF[] profile;
    private final ZonedDateTime startDate;

    public ISFProfile(ISF[] profile, ZonedDateTime startDate)
    {
        this.profile = profile.clone();
        this.startDate = startDate;
    }

    public ISF[] getProfile()
    {
        return profile;
    }
    public ZonedDateTime getStartDate()
    {
        return startDate;
    }
}
