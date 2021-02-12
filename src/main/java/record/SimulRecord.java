package record;

public class SimulRecord
{
    public int batterySizeAh;
    public int panelArea;
    public int batteryReplacementAgeYears;
    public double collectorZenith;
    public double collectorAzimuth;
    public double generatorStarts;
    public double totalCost;
    public double totalDemandKWH;
    public double generatorCost;
    public double batteryCost;
    public double solarPowerCost;
    public double excessGeneration;

    public SimulRecord()
    {
        this.totalCost = Double.MAX_VALUE;
    }

    public SimulRecord(int batterySizeAh,
                       int batteryReplacementAgeYears,
                       int panelArea,
                       double collectorZenith,
                       double collectorAzimuth,
                       double generatorStarts,
                       double totalCost,
                       double generatorCost,
                       double batteryCost,
                       double solarPowerCost,
                       double totalDemandKWH,
                       double excessGeneration)
    {
        this.batterySizeAh = batterySizeAh;
        this.panelArea = panelArea;
        this.collectorZenith = collectorZenith;
        this.collectorAzimuth = collectorAzimuth;
        this.batteryReplacementAgeYears = batteryReplacementAgeYears;
        this.generatorStarts = generatorStarts;
        this.totalCost = totalCost;
        this.totalDemandKWH = totalDemandKWH;
        this.generatorCost = generatorCost;
        this.batteryCost = batteryCost;
        this.solarPowerCost = solarPowerCost;
        this.excessGeneration = excessGeneration;
    }

    public SimulRecord copy()
    {
        return new SimulRecord(batterySizeAh,
                batteryReplacementAgeYears,
                panelArea,
                collectorZenith,
                collectorAzimuth,
                generatorStarts,
                totalCost,
                generatorCost,
                batteryCost,
                solarPowerCost,
                totalDemandKWH,
                excessGeneration);
    }
    public void print()
    {
        System.out.println(String.format("%3.2f\t%3.2f\t%3.2f\t%3.2f\t%d\t%d\t%d\t%3.2f\t%3.1f\t%3.1f\t%3.2f",
                totalCost,
                generatorCost,
                batteryCost,
                solarPowerCost,
                batterySizeAh,
                batteryReplacementAgeYears,
                panelArea,
                collectorZenith,
                collectorAzimuth,
                excessGeneration,
                generatorStarts));
    }

    public void printHeaders()
    {
        System.out.println("Cost\tGen cost\tmain.java.battery.Battery Cost\tSolar Cost\tmain.java.battery.Battery Size Ah\tmain.java.battery.Battery Obsel Yr\tPanel Area M2\tZenith\tAzimuth\tExcess Gen kWh\tGenerator Starts\t");
    }

    public void set(SimulRecord state)
    {
        this.batterySizeAh = state.batterySizeAh;
        this.panelArea = state.panelArea;
        this.collectorZenith = state.collectorZenith;
        this.collectorAzimuth = state.collectorAzimuth;
        this.batteryReplacementAgeYears = state.batteryReplacementAgeYears;
        this.generatorStarts = state.generatorStarts;
        this.totalCost = state.totalCost;
        this.totalDemandKWH = state.totalDemandKWH;
        this.generatorCost = state.generatorCost;
        this.batteryCost = state.batteryCost;
        this.solarPowerCost = state.solarPowerCost;
        this.excessGeneration = state.excessGeneration;
    }
}
