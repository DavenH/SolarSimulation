package record;

import physics.Joule;

public class DailyState
{
    public Joule dailyGeneration = new Joule();
    public Joule dailyDemand = new Joule();

    public double leastDOD = 1;
    public double maxDOD = 1;

    public double minTemp = 100;
    public double maxTemp = -100;

    public void updateTempLimits(double temp)
    {
        minTemp = Math.min(temp, minTemp);
        maxTemp = Math.max(temp, maxTemp);
    }

    public void updateDOD(double depthOfDischarge)
    {
        leastDOD = Math.min(leastDOD, depthOfDischarge);
        maxDOD = Math.max(maxDOD, depthOfDischarge);
    }
}
