import java.time.*;

public class ISFProfile
{
    private ISF[] profile;
    private ZonedDateTime startDate;

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
