import java.time.*;

public class ModifyBG
{
    private double bg;
    private LocalTime time;

    public ModifyBG(double bg, LocalTime time)
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