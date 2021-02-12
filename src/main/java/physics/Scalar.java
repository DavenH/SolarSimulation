package physics;

public class Scalar
{
    public double value;

    public Scalar()
    {
        this.value = 0;
    }

    public Scalar(double value)
    {
        this.value = value;
    }

    public void maxI(double k)
    {
        value = Math.max(k, value);
    }

    public void minI(double k)
    {
        value = Math.min(k, value);
    }

    public boolean gt(double k)
    {
        return value > k;
    }

    public boolean gte(double k)
    {
        return value >= k;
    }

    public boolean lte(double k)
    {
        return value <= k;
    }

    public boolean lt(double k)
    {
        return value < k;
    }
}
