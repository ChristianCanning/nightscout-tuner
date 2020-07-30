import java.time.*;

public class isf
{
    private double value;
    private LocalTime time;

    public isf(double value, LocalTime time)
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
