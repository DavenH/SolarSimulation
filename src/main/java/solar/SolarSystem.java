package solar;

import physics.Joule;
import physics.Second;
import physics.Watt;
import record.WeatherRecord;

public class SolarSystem
{
    private final double collectorAzimuth, collectorSlope;
    private final double efficiency;
    private final double volts;

    private SolarAngleTable table;
    private double areaM2;
    private int ageDays;
//    public double zeniths[][][] = new double[365][24][60];
//    public double azimuths[][][] = new double[365][24][60];

    public SolarSystem(SolarAngleTable table, double area, double volts, double collectorSlope, double collectorAzimuth, double efficiency)
    {
        final double degToRad = 2 * Math.PI / 360;

        this.ageDays = 0;
        this.table = table;
        this.volts = volts;
        this.areaM2 = area;
        this.collectorAzimuth = collectorAzimuth * degToRad;
        this.collectorSlope = collectorSlope * degToRad;
        this.efficiency = efficiency;
    }

    public Joule getEnergyInMinute(WeatherRecord record, int minute)
    {
        double temperatureLoss = Math.min(1.05, Math.pow(1.005, record.temp - 25));
        double ageLoss = Math.pow(0.993, ageDays / 365);
        double inefficiencies = efficiency / temperatureLoss * ageLoss;

        double zenith = table.getZenith(record.day, record.hour, minute);
        double azimuth = table.getAzimuth(record.day, record.hour, minute);
        double zenithIncidence = Math.max(0, Math.cos(zenith - this.collectorSlope));
        double azimuthIncidence = Math.max(0, Math.cos(Math.PI + azimuth - this.collectorAzimuth));
        double incidence = zenithIncidence * azimuthIncidence;

        Watt solarWatts = new Watt(areaM2 * record.solarWm2 * inefficiencies * incidence);
        return new Joule(new Second(60), solarWatts);
    }

    public void setArea(double area)
    {
        this.areaM2 = area;
    }

    public void setAgeDays(int ageDays)
    {
        this.ageDays = ageDays;
    }

    public void ageOneDay()
    {
        ++ ageDays;
    }

    public int getAgeDays()
    {
        return ageDays;
    }

    public double getVolts()
    {
        return volts;
    }
}
