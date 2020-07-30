import java.time.*;

// The insulin-to-carb ratio object which holds the time of day at which it is active, known as 'time', and the insulin-to-carb ratio value,
// known as 'value'.
public class CarbRatio
{
    private final double value;
    private final LocalTime time;

    public CarbRatio(double value, LocalTime time)
    {
        this.value = value;
        this.time = time;
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
