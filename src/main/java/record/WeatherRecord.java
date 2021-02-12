package record;

public class WeatherRecord
{
    public double temp;
    public double solarWm2;
    public double windDirection;
    public double windspeed;
    public int day;
    public int hour;
    public long time;

    public WeatherRecord(double temp,
                         double solarWm2,
                         double windDirection,
                         double windspeed,
                         int day,
                         int hour,
                         long time)
    {
        this.temp = temp;
        this.solarWm2 = solarWm2;
        this.windDirection = windDirection;
        this.windspeed = windspeed;
        this.day = day;
        this.hour = hour;
        this.time = time;
    }

    public WeatherRecord(WeatherRecord record)
    {
        this.temp = record.temp;
        this.solarWm2 = record.solarWm2;
        this.windDirection = record.windDirection;
        this.windspeed = record.windspeed;
        this.day = record.day;
        this.hour = record.hour;
        this.time = record.time;
    }

    public WeatherRecord interpolate(double k, WeatherRecord record)
    {
        return new WeatherRecord(temp * (1 - k) + record.temp * k,
                solarWm2 * (1 - k) + record.solarWm2 * k,
                windDirection * (1 - k) + record.windDirection * k, // non-linear, but who cares
                windspeed * (1 - k) + record.windspeed * k,
                day,
                hour,
                time
            );
    }

    public int advanceTimeBy30Minutes(int minute)
    {
        int m = minute + 30;

        if(m > 59)
        {
            m -= 60;

            ++hour;
            if(hour > 23)
            {
                hour -= 24;
                ++day;
            }
        }

        return m;
    }
}