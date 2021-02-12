package physics;

public class Watt extends Scalar
{
    public Watt()
    {
        super();
    }

    public Watt(double value)
    {
        super(value);
    }

    public Watt(Second toSecond, Joule joule)
    {
        super(toSecond.value * joule.value);
    }

    public Watt(Volt volt, Amp amp)
    {
        super(volt.value * amp.value);
    }

    Joule mul(Second seconds)
    {
        return new Joule(value * seconds.value);
    }

    public Amp div(Volt volts)
    {
        return new Amp(value / volts.value);
    }

    public Volt div(Amp amps)
    {
        return new Volt(value / amps.value);
    }

    public Watt addI(Watt averagePowerAtTimeW)
    {
        value += averagePowerAtTimeW.value;
        return this;
    }
}
