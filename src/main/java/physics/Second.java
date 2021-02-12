package physics;

public class Second extends Scalar
{
    public Second(double value)
    {
        super(value);
    }

    public Hour toHour()
    {
        return new Hour(value / 3600.0);
    }

    public Joule mul(Watt powerAverage)
    {
        return new Joule(this, powerAverage);
    }
}
