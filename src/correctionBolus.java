import java.time.*;

public class correctionBolus
{
    private double insulin;
    private ZonedDateTime timestamp;

    public correctionBolus(double insulin, ZonedDateTime timestamp)
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
