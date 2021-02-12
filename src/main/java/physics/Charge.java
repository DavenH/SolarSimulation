package physics;

public class Charge extends Scalar
{
    public Charge(double value)
    {
        super(value);
    }

    public Charge toAmpHour()
    {
        return new Charge(value / 3600.0);
    }
}
