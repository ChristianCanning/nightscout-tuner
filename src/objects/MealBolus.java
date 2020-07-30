import java.time.*;

public class MealBolus
{
    private int carbs;
    private int absorptionTime;
    private ZonedDateTime timestamp;

    public MealBolus(int carbs, int absorptionTime, ZonedDateTime timestamp)
    {
        this.carbs = carbs;
        this.absorptionTime = absorptionTime;
        this.timestamp = timestamp;
    }

    public int getCarbs()
    {
        return carbs;
    }
    public int getAbsorptionTime()
    {
        return absorptionTime;
    }
    public ZonedDateTime getTimestamp()
    {
        return timestamp;
    }
}
