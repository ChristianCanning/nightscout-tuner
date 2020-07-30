import java.time.LocalTime;

public class modifyInsulin
{
    private double amount;
    private LocalTime time;
    public modifyInsulin(double amount, LocalTime time)
    {
        this.amount = amount;
        this.time = time;
    }
    public void setAmount(double amount)
    {
        this.amount = amount;
    }

    public double getAmount()
    {
        return amount;
    }
    public LocalTime getTime()
    {
        return time;
    }

}
