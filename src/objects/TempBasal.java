import java.time.*;

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
