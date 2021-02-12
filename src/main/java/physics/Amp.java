package physics;

public class Amp extends Scalar
{
    public Amp(double value)
    {
        super(value);
    }

    public Watt mul(Volt volts)
    {
        return new Watt(volts, this);
    }

    public Charge mul(Hour hour)
    {
        return new Charge(value * hour.toSecond().value);
    }
}
