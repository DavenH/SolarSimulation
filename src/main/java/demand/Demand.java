package demand;


import physics.Watt;

public interface Demand
{
    Watt getAveragePowerAtTimeW(DemandData demandData);
    String getName();
}
