import java.time.*;

//The BasalProfile object holds an array of basals known as 'profile'. The BasalProfile holds a day at which it was active, known as
// 'startDate'. Contrary to the name of 'startDate', this is only a day at which the BasalProfile was active, which may or may not be
// the actual day that the basal profile started. Loop uploads the BasalProfile multiple times to Nightscout, even if the BasalProfile
// was not updated.
public class BasalProfile
{
    private Basal[] profile;
    private ZonedDateTime startDate;

    public BasalProfile(Basal[] profile, ZonedDateTime startDate)
    {
        this.profile = profile.clone();
        this.startDate = startDate;
    }

    public Basal[] getProfile()
    {
        return profile;
    }
    public ZonedDateTime getStartDate()
    {
        return startDate;
    }

}
