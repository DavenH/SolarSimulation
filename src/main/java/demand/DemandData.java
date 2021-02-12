package demand;

import battery.Battery;
import record.WeatherRecord;
import solar.SolarSystem;

import java.util.Random;

public class DemandData
{
    public SolarSystem solar;
    public Battery battery;
    public WeatherRecord record;
    public int minute;
    public Random random;

    public DemandData(SolarSystem solar,
                      Battery battery,
                      WeatherRecord record,
                      int minute,
                      Random random)
    {
        this.solar = solar;
        this.battery = battery;
        this.record = record;
        this.minute = minute;
        this.random = random;
    }
}
