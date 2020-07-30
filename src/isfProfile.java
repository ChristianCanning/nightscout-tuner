import java.time.*;

public class isfProfile
{
    private isf[] profile;
    private ZonedDateTime startDate;

    public isfProfile(isf[] profile, ZonedDateTime startDate)
    {
        this.profile = profile.clone();
        this.startDate = startDate;
    }

    public isf[] getProfile()
    {
        return profile;
    }
    public ZonedDateTime getStartDate()
    {
        return startDate;
    }
}
