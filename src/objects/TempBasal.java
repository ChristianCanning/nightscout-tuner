import java.time.*;

// The TempBasal object holds information about TempBasals that were run. The 'created_at' refers to the date and time that the temp basal
// was created at. The 'rate' refers to the amount of insulin delivered in a one hour period. The 'duration' states how long the TempBasal
// should have lasted. However, sometimes (but not usually) the duration is incorrect. When a TempBasal is uploaded to Nightscout, the initial
// duration is something like 30 minutes. When the TempBasal ends, NightScout is supposed to update the duration to match how long the TempBasal
// actually lasted. Sometimes, this doesn't happen, and so the duration stays on 30 minutes (or whatever the initial duration was)--meaning the
// duration held within this object is incorrect when this happens.
public class TempBasal implements Comparable<TempBasal>
{
    private double rate;
    private double duration;
    private ZonedDateTime created_at;

    public TempBasal(double rate, double duration, ZonedDateTime created_at)
    {
        this.rate =  rate;
        this.duration = duration;
        this.created_at = created_at;
    }

    public double getRate()
    {
        return rate;
    }
    public double getDuration()
    {
        return duration;
    }
    public ZonedDateTime getCreated_at()
    {
        return created_at;
    }

    @Override
    public int compareTo(TempBasal o)
    {
        return getCreated_at().compareTo(o.getCreated_at());
    }
}
