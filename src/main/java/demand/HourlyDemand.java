package demand;

import physics.Watt;

public class HourlyDemand implements Demand
{
    int hourlyWH[];
    String name;

    public HourlyDemand(int[] hourlyWH, String name)
    {
        this.hourlyWH = hourlyWH;
        this.name = name;
    }

    @Override
    public Watt getAveragePowerAtTimeW(DemandData demandData)
    {
        double fractionOfHour = demandData.minute / 60.0;

        if(fractionOfHour < 0.5)
            return new Watt((hourlyWH[(demandData.record.hour + 23 % 24)] * (0.5 - fractionOfHour) + hourlyWH[demandData.record.hour] * (0.5 + fractionOfHour)));
        else
            return new Watt((hourlyWH[demandData.record.hour] * (1.5 - fractionOfHour) + hourlyWH[(demandData.record.hour + 1) % 24] * (fractionOfHour - 0.5)));
    }

    @Override
    public String getName()
    {
        return name;
    }
}
