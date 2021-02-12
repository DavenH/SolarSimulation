package variable;

import java.util.Random;

public class DiscreteVariable
{
    public int low, high, step;
    final int range;
    public Random rand;

    public DiscreteVariable(Random rand, int low, int high, int step)
    {
        this.rand = rand;
        this.low = low;
        this.step = step;
        this.high = high;
        range = 1 + (high - low) / Math.max(1, step);
    }

    public int sample()
    {
        return rand.nextInt(range) * step + low;
    }

    public int getRange()
    {
        return high - low;
    }

    public int numSteps()
    {
        return range;
    }
}
