package physics;

public class Volt extends Scalar
{
    public Volt(double value)
    {
        super(value);
    }

    public Watt mul(Amp amp)
    {
        return new Watt(this, amp);
    }
}
