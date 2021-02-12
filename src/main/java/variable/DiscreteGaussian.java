package variable;

import java.util.Random;

public class DiscreteGaussian extends DiscreteVariable
{
    int mean;
    double variance;

    public DiscreteGaussian(Random rand, int low, int high, int variance, int mean, int step)
    {
        super(rand, low, high, step);
        this.mean = mean;
        this.variance = variance;
    }

    @Override public int sample()
    {
        return Math.max(low, Math.min(high, (int) Math.round((rand.nextGaussian() * variance) + mean)));
    }

    public void bias(int goodMean)
    {
        mean += (goodMean - mean) / 6;
        this.variance *= 0.95;
    }
}
