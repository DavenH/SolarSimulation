package physics;

public class KiloWattHour extends Scalar
{
    public KiloWattHour(double value)
    {
        super(value);
    }

    public Joule toJoule()
    {
        return new Joule(3600 * 1000 * value);
    }

    public Joule mul(Second seconds)
    {
        return toJoule();
    }

    public Hour div(Watt watts)
    {
        return new Hour(value / 1000.0 / watts.value);
    }

    public Watt div(Hour hours)
    {
        return new Watt(value / 1000 / hours.toSecond().value);
    }
}
