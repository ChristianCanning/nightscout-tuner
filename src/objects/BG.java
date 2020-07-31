import java.time.*;

// The BG (Blood Glucose) object, which holds a blood glucose value and the date and time at which it was recorded.
public class BG
{
    private double bg;
    private ZonedDateTime date;

    public BG(int bg, ZonedDateTime date)
    {
        this.bg = bg;
        this.date = date;
    }

    public double getBG()
    {
        return bg;
    }
    public ZonedDateTime getDate()
    {
        return date;
    }

    // The code below is used to create BG values and modify them to do things such as adjusting BGs.
    // The code below is not part of the original BG data that is retrieved from Nightscout.
    private LocalTime time;

    public BG(double bg, LocalTime time)
    {
        this.bg = bg;
        this.time = time;
    }

    public void setBg(double bg)
    {
        this.bg = bg;
    }
    public LocalTime getTime()
    {
        return time;
    }
}
