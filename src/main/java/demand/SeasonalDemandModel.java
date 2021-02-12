package demand;

import physics.Watt;

public class SeasonalDemandModel implements Demand
{
    int denomHourlyUsage[] = { 24, 35, 61, 61, 61, 61, 40, 30, 20, 20, 24, 24, 24, 24, 24, 27, 22, 20, 16, 15, 15, 16, 17, 19 };
    double hourlyUsages[][] = new double[365][24];

    public SeasonalDemandModel()
    {
        final int minMonthly = 350;
        final int maxMonthly = 1000;

        double amplitude = maxMonthly - minMonthly;

        for(int dayOfYear = 0; dayOfYear < 365; ++dayOfYear)
        {
            double phaseInYear = (dayOfYear - 319.0) / 365.0 * 2 * Math.PI;
            double seasonalSwing = (Math.sin(phaseInYear) + 1) / 2.0;
            double monthlyUsage = seasonalSwing * amplitude + minMonthly;
            double dailyUsage = monthlyUsage / 30.5;

            for(int hourOfDay = 0; hourOfDay < 24; ++hourOfDay)
            {
                double hourlyUsage = dailyUsage / denomHourlyUsage[hourOfDay];
                hourlyUsages[dayOfYear][hourOfDay] = hourlyUsage;
            }
        }
    }

    @Override
    public Watt getAveragePowerAtTimeW(DemandData demandData)
    {
        // TODO
        return new Watt(hourlyUsages[demandData.record.day][demandData.record.hour] / 60);
    }

    @Override
    public String getName()
    {
        return "SeasonalDemand";
    }
}
