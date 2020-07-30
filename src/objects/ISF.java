import java.time.*;

// The ISF (Insulin Sensitivity Factor) object which holds the ISF value, known as 'value' and the time of day at which it starts being active,
// known as 'time'.
public class ISF
{
    private final double value;
    private final LocalTime time;

    public ISF(double value, LocalTime time)
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
