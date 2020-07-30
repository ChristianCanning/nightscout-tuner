import java.time.LocalTime;

public class ModifyInsulin
{
    private double amount;
    private LocalTime time;
    public ModifyInsulin(double amount, LocalTime time)
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
