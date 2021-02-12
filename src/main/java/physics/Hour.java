package physics;

public class Hour extends Scalar
{
    public Hour(double value)
    {
        super(value);
    }

    public Second toSecond()
    {
        return new Second(value / 3600.0);
    }

    public Watt mul(Joule joule)
    {
        return new Watt(toSecond(), joule);
    }
}
