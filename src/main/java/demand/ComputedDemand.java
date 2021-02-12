package demand;


import physics.Watt;

import java.util.function.Function;

public class ComputedDemand implements Demand
{
    Function<DemandData, Watt> wattsInMinute;
    String name;

    public ComputedDemand(Function<DemandData, Watt> wattsInMinute, String name)
    {
        this.wattsInMinute = wattsInMinute;
        this.name = name;
    }

    @Override
    public Watt getAveragePowerAtTimeW(DemandData demandData)
    {
        return wattsInMinute.apply(demandData);
    }

    @Override
    public String getName()
    {
        return name;
    }
}
