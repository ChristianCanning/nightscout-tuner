import java.time.*;

// The BG (Blood Glucose) object
public class BG
{
    private int bg;
    private ZonedDateTime date;

    public BG(int bg, ZonedDateTime date)
    {
        this.bg = bg;
        this.date = date;
    }


    public int getBG()
    {
        return bg;
    }
    public ZonedDateTime getDate()
    {
        return date;
    }
}
