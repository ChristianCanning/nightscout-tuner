import java.time.*;

public class carbratio
{
    private double value;
    private LocalTime time;

    public carbratio(double value, LocalTime time)
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
