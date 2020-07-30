import java.time.*;

// The MealBolus object contains info about carbs entered, known as 'carbs', the absorption time for the carbs, and the day and time at which it
// was entered. Despite bolus being in the name, Loop does not upload info about insulin in the MealBolus. Instead, Loop uploads insulin delivered
// for meals inside of the CorrectionBolus object.
public class MealBolus
{
    private final int carbs;
    private final int absorptionTime;
    private final ZonedDateTime timestamp;

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
