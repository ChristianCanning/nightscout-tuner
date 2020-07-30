import java.time.*;

public class modifyBG
{
    private double bg;
    private LocalTime time;

    public modifyBG(double bg, LocalTime time)
    {
        this.bg = bg;
        this.time = time;
    }
    public void setBg(double bg)
    {
        this.bg = bg;
    }


    public double getBg()
    {
        return bg;
    }
    public LocalTime getTime()
    {
        return time;
    }
}