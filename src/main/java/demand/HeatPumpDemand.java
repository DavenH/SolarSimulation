package demand;

import physics.Watt;

public class HeatPumpDemand implements Demand
{
    enum CompressorStage {
        Heating, Cooling, Idle
    }

//    CompressorStage compStage;
//    boolean fanOn;
//
//    double internalTemperature;
//    double targetTemperature;
//    double refrigerantTemp;
//    double refrigerantEnergy;
//    double frost;

    final double heatCapacityOfAirJPerKgDegC = 1010;
    final double heatCapacityOfRefrigerantJPerKgDegC = 840;
    final double kgOfRefrigerantInHeatPump = 1.3;
    final double kgAirPerM3 = 1.293;
    final double m3InHeatingVolume = 8 * 34 * 20 / 3.28 / 3.28 / 3.28;
    final double kgAirInHeatingVolume = kgAirPerM3 * m3InHeatingVolume;

    public HeatPumpDemand()
    {
    }

    @Override
    public Watt getAveragePowerAtTimeW(DemandData demandData)
    {
        Watt result = new Watt();
        double cop = copLookup(demandData.record.temp);
        double t = demandData.record.temp;
        final double windChillFactorWithVel = 0.005;

        if(t < 15)
        {
            double duty = 1 - 0.033 * (t + 20);
            duty *= (1 + demandData.record.windspeed * windChillFactorWithVel);
            duty = Math.max(0, Math.min(1, duty));

            // defrosting
            if(duty > 0.5 && demandData.record.time % 8400 < 20 * 60)
            {
                result.value = 800.0;
            }
            else
            {// heating cycle
                final int intermittency = 900;
                if(demandData.record.time % intermittency < duty * intermittency)
                {
                    result.value = duty * 900;
                }
            }
        }

        else if(t > 27)
        {
            final int intermittency = 900;
            double duty = 0 + (t - 25) * 0.1;
            duty = Math.min(1, duty);

            if(demandData.record.time % intermittency < duty * intermittency)
            {
                result.value = 500;
            }
        }

        return result;
    }

    private double copLookup(double temp)
    {
        return 1.5 + 6/28 * (temp - (-22));
    }

    @Override
    public String getName()
    {
        return "HeatPump";
    }
}
