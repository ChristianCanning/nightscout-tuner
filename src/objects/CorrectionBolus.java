import java.time.*;

// The CorrectionBolus object holds data for any insulin delivered that was not part of a basal or temp basal. It even holds the insulin delivered
// for meals. The 'insulin' is the amount of insulin delivered, and the 'timestamp' is the date and time at which the insulin began delivery.
public class CorrectionBolus
{
    private double insulin;
    private ZonedDateTime timestamp;

    public CorrectionBolus(double insulin, ZonedDateTime timestamp)
    {
        this.insulin = insulin;
        this.timestamp = timestamp;
    }

    public double getInsulin()
    {
        return insulin;
    }
    public ZonedDateTime getTimestamp()
    {
        return timestamp;
    }

    // The code below is used to create insulin boluses and modify them to model the effects.
    // The code below is not part of the original CorrectionBolus data that is retrieved from Nightscout.
    private LocalTime time;
    public CorrectionBolus(double insulin, LocalTime time)
    {
        this.insulin = insulin;
        this.time = time;
    }
    public LocalTime getTime()
    {
        return time;
    }
    public void setInsulin(double insulin)
    {
        this.insulin = insulin;
    }
}
