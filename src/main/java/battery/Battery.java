package battery;

import physics.Joule;
import record.DailyState;

public class Battery
{
    final Joule maxEnergy;
    final double volts;
    final double dischargInefficiency;
    final double chargInefficiency;
    final int maxDOD;     //in percent
    final int lifetimeCycles;
    final int weightKg;
    final int selfDischargeRatePerMo;

    Joule minEnergy;
    double ageDays;
    double cycleLoss;
    double ageLoss;
    Joule energy;

    public Battery(int ampHours,
                   double volts,
                   double dischargInefficiency,
                   int maxDOD,
                   int lifetimeCycles,
                   int weightKg,
                   int selfDischargeRatePerMo)
    {
        maxEnergy = new Joule(ampHours * volts * 3600);
        this.volts = volts;
        this.dischargInefficiency = dischargInefficiency;
        this.chargInefficiency = dischargInefficiency;
        this.maxDOD = maxDOD;
        this.lifetimeCycles = lifetimeCycles;
        this.weightKg = weightKg;
        this.selfDischargeRatePerMo = selfDischargeRatePerMo;
        ageDays = 0;
        cycleLoss = 1;
        ageLoss = 1;

        this.energy = maxEnergy.copy();
        this.minEnergy = getMaxEnergy().mul((100 - maxDOD) / 100.0);
    }

    public Joule charge(Joule joules)
    {
        Joule oldEnergy = energy.copy();
        Joule actualCharge = joules.mul(chargInefficiency);

        energy.addI(actualCharge).minI(getMaxUsableEnergy().value);

        return energy.sub(oldEnergy);
    }

    public Joule draw(Joule joules)
    {
        Joule oldEnergy = energy.copy();
        Joule actualDraw = joules.div(dischargInefficiency);

        energy.subI(actualDraw).maxI(minEnergy.value);

        return oldEnergy.sub(energy).mul(dischargInefficiency);
    }

    public Joule getMaxEnergy()
    {
        return maxEnergy;
    }

    public Joule getMaxUsableEnergy()
    {
        return maxEnergy.mul(cycleLoss);
    }

    public double getFractionUsableEnergy()
    {
        return cycleLoss;
    }

    public double getDepthOfDischarge()
    {
        return energy.value / maxEnergy.value;
    }

    public double getVoltage()
    {
        double x = getDepthOfDischarge();
        return volts * (0.95 + 1 / (2000 * (x + 0.005)) - 1 / (40 * (1 - x)));
    }

    public void ageOneDay(DailyState currState)
    {
        double y = currState.maxDOD - 0.5;
        double x = currState.leastDOD - 0.5;

        double x2 = x*x;
        double y2 = y*y;

        double cRate = currState.dailyDemand.value / maxEnergy.value / 24;

        double ppmCapacityLoss = Math.min(0,
                - 30.1 * y
                + 21.7 * x
                - 9.4 * y2
                + 3.78 * x2
                - 96 * y2 * y
                + 137 * x2 * x
        );

        cycleLoss += ppmCapacityLoss / 1e6;
        ageLoss = Math.pow(0.98, ageDays / 365);

        ++ageDays;
    }

    public double getAge()
    {
        return ageDays;
    }

    public void setAge(double age)
    {
        ageDays = age;
        cycleLoss = 1;
    }
}
