import java.time.*;

public class ISF
{
    private double value;
    private LocalTime time;

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
