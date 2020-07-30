import java.time.*;

public class CarbRatioProfile
{
    private CarbRatio[] profile;
    private ZonedDateTime startDate;

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
