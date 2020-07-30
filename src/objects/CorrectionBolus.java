import java.time.*;

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
}
