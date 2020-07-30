import java.time.*;

// The Basal object holds basals for the BasalProfile. A basal consists of the start time, known as 'time' and the
// insulin rate (insulin delivered per hour), known as 'value'.
public class Basal
{
    private LocalTime time;
    private double value;

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
