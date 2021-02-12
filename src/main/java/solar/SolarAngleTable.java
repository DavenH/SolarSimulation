package solar;

import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.SPA;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class SolarAngleTable
{
    final double angles[][][][] = new double[365][24][60][2];

    public SolarAngleTable()
    {
        final double degToRad = 2 * Math.PI / 360;
        final double lat = 45.749499;
        final double lon = -66.88411;
        final double latRad = lat * degToRad;
        final double lonRad = lon * degToRad;
        final int timeZoneHr = -3;

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("America/Halifax"));

        for(int dayOfYear = 0; dayOfYear < 365; ++dayOfYear)
        {
            for(int hourOfDay = 0; hourOfDay < 24; ++hourOfDay)
            {
                for(int minuteOfHour = 0; minuteOfHour < 60; ++minuteOfHour)
                {
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
                    calendar.set(Calendar.YEAR, 2020);

                    AzimuthZenithAngle position = SPA.calculateSolarPosition(calendar, lat, lon, 120, 68, 1010, 11);

                    double zenith = position.getZenithAngle() * degToRad;
                    double azimuth = position.getAzimuth() * degToRad;

                    angles[dayOfYear][hourOfDay][minuteOfHour][0] = zenith;
                    angles[dayOfYear][hourOfDay][minuteOfHour][1] = azimuth;
                }
            }
        }
    }

    public double getZenith(int day, int hour, int minute)
    {
        return angles[day][hour][minute][0];
    }

    public double getAzimuth(int day, int hour, int minute)
    {
        return angles[day][hour][minute][1];
    }


    public static void main(String[] args)
    {
        new SolarAngleTable();
    }
}
