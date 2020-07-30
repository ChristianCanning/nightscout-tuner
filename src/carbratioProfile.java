import java.time.*;

public class carbratioProfile
{
    private carbratio[] profile;
    private ZonedDateTime startDate;

    public carbratioProfile(carbratio[] profile, ZonedDateTime startDate)
    {
        this.profile = profile.clone();
        this.startDate = startDate;
    }

    public carbratio[] getProfile()
    {
        return profile;
    }
    public ZonedDateTime getStartDate()
    {
        return startDate;
    }
}
