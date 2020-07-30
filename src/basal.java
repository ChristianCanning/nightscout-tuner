import java.time.*;

public class basal
{
    private LocalTime time;
    private double value;

    public basal(double value, LocalTime time)
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
