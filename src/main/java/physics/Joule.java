package physics;

public class Joule extends Scalar
{
    public Joule()
    {
    }

    public Joule(Second second, Watt powerAverage)
    {
        super(second.value * powerAverage.value);
    }

    public Joule(double value)
    {
        super(value);
    }

    public KiloWattHour toKwh()
    {
        return new KiloWattHour(value  / 3600.0 / 1000.0);
    }

    public Watt div(Second second)
    {
        return new Watt(value / second.value);
    }

    public Watt div(Hour hour)
    {
        return new Watt(value / hour.toSecond().value);
    }

    public Joule addI(Joule joule)
    {
        value += joule.value;
        return this;
    }

    public Joule sub(Joule joule)
    {
        return new Joule(value - joule.value);
    }

    public Joule subI(Joule joule)
    {
        value -= joule.value;
        return this;
    }

    public Joule addI(KiloWattHour kwh)
    {
        return new Joule(value + kwh.toJoule().value);
    }

    public Joule copy()
    {
        return new Joule(value);
    }

    public Joule div(double k)
    {
        return new Joule(value / k);
    }

    public Joule mul(double k)
    {
        return new Joule(value * k);
    }

    public Joule mulI(double k)
    {
        value *= k;
        return this;
    }

    public Second div(Watt watts)
    {
        return new Second(value / watts.value);
    }
}
