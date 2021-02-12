package variable;

import java.util.Random;

public class GaussianVariable
{
    double low;
    double high;
    double mean;
    double variance;
    Random rand;

    public GaussianVariable(Random rand, double low, double high, double mean, double variance)
    {
        this.low = low;
        this.high = high;
        this.mean = mean;
        this.variance = variance;
        this.rand = rand;
    }

    public double sample()
    {
        if(variance == 0)
            return mean;
        else
        {
            return Math.min(high, Math.max(low, mean + rand.nextGaussian() * variance));
        }
    }

    public void bias(double mean)
    {
        this.mean += (mean - this.mean) * 0.1;
        this.variance *= 0.95;
    }
}
