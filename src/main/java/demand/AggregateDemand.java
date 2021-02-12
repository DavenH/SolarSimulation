package demand;

import physics.Watt;

import java.util.ArrayList;
import java.util.List;

public class AggregateDemand implements Demand
{
    public List<Demand> demands = new ArrayList<>();

    @Override
    public Watt getAveragePowerAtTimeW(DemandData demandData)
    {
        Watt totalDemand = new Watt();

        for(Demand demand : demands)
            totalDemand.addI(demand.getAveragePowerAtTimeW(demandData));

        return totalDemand;
    }

    public void addDemand(Demand demand)
    {
        demands.add(demand);
    }

    @Override
    public String getName()
    {
        return "AggregateDemand";
    }
}
