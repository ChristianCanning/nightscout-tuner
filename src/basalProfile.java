import java.time.*;

public class basalProfile
{
    private basal[] profile;
    private ZonedDateTime startDate;

    public basalProfile(basal[] profile, ZonedDateTime startDate)
    {
        this.profile = profile.clone();
        this.startDate = startDate;
    }

    public basal[] getProfile()
    {
        return profile;
    }
    public ZonedDateTime getStartDate()
    {
        return startDate;
    }
    public int getTimePos(LocalTime time)
    {
        LocalTime maxTime = LocalTime.parse("23:59:59");
        for(int i = 0; i < profile.length; i++)
        {
            if(i == profile.length - 1)
            {
                if(time.compareTo(profile[i].getTime()) >= 0  && time.compareTo(maxTime) <= 0)
                    return i;
            }
            else if(time.compareTo(profile[i].getTime()) >= 0  && time.compareTo(profile[i+1].getTime()) <= 0)
            {
                return i;
            }
        }
        System.out.println("FAILURE. getPos in the basalProfile class has encountered that the time given is not possible.");
        return 9999999;
    }
}
