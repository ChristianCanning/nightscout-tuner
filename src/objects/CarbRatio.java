import java.time.*;

public class CarbRatio
{
    private double value;
    private LocalTime time;

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
