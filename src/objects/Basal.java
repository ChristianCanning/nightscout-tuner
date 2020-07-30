import java.time.*;

// The Basal object holds basals (insulin delivered in a 1 hour period) for the BasalProfile. A basal consists of the time of day that it
// starts being active, known as 'time' and the insulin rate (insulin delivered per hour), known as 'value'.
public class Basal
{
    private final LocalTime time;
    private final double value;

    public Basal(double value, LocalTime time)
    {
        this.time = time;
        this.value = value;
    }

    public double getValue()
    {
        return value;
    }
    public LocalTime getTime()
    {
        return time;
    }
}
