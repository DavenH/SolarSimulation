package physics;

public class Year  extends Scalar
{
    public Year(double value)
    {
        super(value);
    }

    public Second toSecond()
    {
        return new Second(value * 86400 * 365.2498);
    }

    public Hour toHour()
    {
        return new Hour(value * 3600);
    }
}
