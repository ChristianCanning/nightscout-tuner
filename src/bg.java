import java.time.*;

public class bg
{
    private int bg;
    private ZonedDateTime date;

    public bg(int bg, ZonedDateTime date)
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
