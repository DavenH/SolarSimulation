import battery.Battery;
import demand.*;
import physics.Joule;
import physics.Second;
import physics.Watt;
import record.DailyState;
import record.SimulRecord;
import record.WeatherRecord;
import solar.SolarAngleTable;
import solar.SolarSystem;
import variable.DiscreteGaussian;
import variable.DiscreteVariable;
import variable.GaussianVariable;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class OffGridPowerSimulation
{
    enum WhatToPrint
    {
        DailyExtremes,
        SingleYearByTheHour,
        TwoWeekByTheMinute,
        BestCosts,
        LoadDistributionBySource,
    }

    public static void main(String[] args)
    {
        List<WeatherRecord> records = getRecords();
        AggregateDemand demandModel = new AggregateDemand();
        addDemandSources(demandModel);

        Random rand = new Random();

//        // grid search
//        searchRanges(records,
//                demandModel,
//                Pair.of(20, 80),
//                Pair.of(600, 1400),
//                Pair.of(70, 240),
//                70,
//                0,
//                WhatToPrint.BestCosts);
//
//        // grid search
//        searchRanges(records,
//                demandModel,
//                Pair.of(20, 70),
//                Pair.of(600, 1300),
//                Pair.of(100, 240),
//                70,
//                0,
//                WhatToPrint.BestCosts);

// print a particular config
        searchRanges(records,
                demandModel,
                new DiscreteVariable(rand, 40, 40, 1),
                new DiscreteVariable(rand, 1000, 1000, 50),
                new DiscreteVariable(rand, 160, 160, 2),
                new DiscreteVariable(rand, 73, 73, 1),
                0,
                WhatToPrint.DailyExtremes);
//
        Random random = new Random();

//         random search
//        getBestRecords(
//                records,
//                demandModel,
//                new DiscreteGaussian(random, 20,  50,   10,  40,  4),   // battery max age years
//                new DiscreteGaussian(random, 800, 1200, 30, 1000, 10),  // battery size Ah
//                new DiscreteGaussian(random, 60,  300,  40,  180, 10),  // main.java.solar size m2
////                new GaussianVariable(random, 50,  80,   65,  10),       // zenith angle deg
//                new GaussianVariable(random, 70,  70,   70,  0),       // zenith angle deg
//                new GaussianVariable(random, 0, 0,   0,   0),           // azimuth angle deg
//                WhatToPrint.BestCosts
//        );
    }

    private static SimulRecord getBestRecords(List<WeatherRecord> records,
                                              AggregateDemand demandModel,
                                              DiscreteGaussian batteryMaxAge,
                                              DiscreteGaussian batterySize,
                                              DiscreteGaussian solarSize,
                                              GaussianVariable zenithAngle,
                                              GaussianVariable azimuthAngle,
                                              WhatToPrint whatToPrint
                                          )
    {

        List<Callable<SimulRecord>> callables = new ArrayList<>();
        SolarAngleTable table = new SolarAngleTable();
        SimulRecord bestState = new SimulRecord();
        Object sync = new Object();

        bestState.printHeaders();
        int numThreads = 5;
        int numIterations = 100;

        ExecutorService service = Executors.newFixedThreadPool(numThreads);

        for(int i = 0; i < numThreads; ++i)
        {
            callables.add(new Callable<SimulRecord>()
            {
                @Override
                public SimulRecord call() throws Exception
                {
                    for(int j = 0; j < numIterations; ++j)
                    {
                        SimulRecord state = getTotalSystemAnnualCost(
                                records,
                                demandModel,
                                table,
                                batterySize.sample(),
                                batteryMaxAge.sample(),
                                solarSize.sample(),
                                zenithAngle.sample(),
                                azimuthAngle.sample(),
                                whatToPrint);

                        synchronized (sync)
                        {
                            if(state.totalCost < bestState.totalCost)
                            {
                                bestState.set(state);
                                bestState.print();
                            }

//                            zenithAngle.bias(bestState.collectorZenith);
//                            azimuthAngle.bias(bestState.collectorAzimuth);
//                            solarSize.bias(bestState.panelArea);
//                            batterySize.bias(bestState.batterySizeAh);
//                            batteryMaxAge.bias(bestState.batteryReplacementAgeYears);

                        }
                    }

                    System.out.println("Done thread " + Thread.currentThread().getId());
                    return null;
                }
            });
        }

        try
        {
            service.invokeAll(callables).stream().collect(Collectors.toList());
            service.shutdown();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return bestState;
    }

    private static void searchRanges(List<WeatherRecord> records,
                                     AggregateDemand demandModel,
                                     DiscreteVariable batteryAgeRangeYr,
                                     DiscreteVariable batterySizeRangeAh,
                                     DiscreteVariable solarSizeRangeM2,
                                     DiscreteVariable zenithAngles,
                                     double azimuth,
                                     WhatToPrint whatToPrint)
    {
        SolarAngleTable table = new SolarAngleTable();

        ExecutorService executorService = Executors.newFixedThreadPool(batterySizeRangeAh.numSteps());

        List<Callable<SimulRecord>> callables = new ArrayList<>();

        AtomicInteger lastS = new AtomicInteger((int) System.currentTimeMillis() / 1000);
        Object sync = new Object();

        SimulRecord bestState = new SimulRecord();

        if(whatToPrint == WhatToPrint.BestCosts)
            bestState.printHeaders();
        else if(whatToPrint == WhatToPrint.DailyExtremes)
        {
            System.out.println("Day\tMin Temp\tMax Temp\tGen kWh\tDemand kWh\tgenerator kWh\tmin DOD\tMaxEnergy");
        }

        abstract class SizedCallable implements Callable<SimulRecord>
        {
            int battSize;
            SizedCallable(int battSize) { this.battSize = battSize; }

            public abstract SimulRecord call();
        }

        for (int b = batterySizeRangeAh.low; b <= batterySizeRangeAh.high; b += batterySizeRangeAh.step)
        {
            callables.add(new SizedCallable(b)
            {
                @Override
                public SimulRecord call()
                {
                    for (int br = batteryAgeRangeYr.low; br <= batteryAgeRangeYr.high; br += batteryAgeRangeYr.step) {
                        for (int h = solarSizeRangeM2.low; h <= solarSizeRangeM2.high; h += solarSizeRangeM2.step) {
                            for(double z = zenithAngles.low; z <= zenithAngles.high; z += zenithAngles.step) {
                                SimulRecord state = getTotalSystemAnnualCost(records, demandModel, table, battSize, br, h, z, azimuth, whatToPrint);

                                if(whatToPrint != WhatToPrint.BestCosts)
                                    return null;

                                synchronized (sync)
                                {
                                    if(state.totalCost < bestState.totalCost && whatToPrint == WhatToPrint.BestCosts)
                                    {
                                        int endS = (int) System.currentTimeMillis() / 1000;

                                        System.out.print((endS - lastS.get()) + "s\t");
                                        lastS.set(endS);

                                        bestState.set(state);
                                        bestState.print();
                                    }
                                }
                            }
                        }
                    }

                    return null;
                }
            });
        }

        try
        {
            executorService.invokeAll(callables).stream().collect(Collectors.toList());
            executorService.shutdown();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        if(whatToPrint == WhatToPrint.BestCosts)
        {
            bestState.print();
        }
    }

    private static SimulRecord getTotalSystemAnnualCost(List<WeatherRecord> records,
                                                        AggregateDemand demandModel,
                                                        SolarAngleTable table,
                                                        int batterySizeAh,
                                                        int batteryObselAge,
                                                        int solarAreaM2,
                                                        double zenith,
                                                        double azimuth,
                                                        WhatToPrint whatToPrint)
    {
        SolarSystem solarSystem = new SolarSystem(table, solarAreaM2, 39.7, zenith, azimuth, 0.175);
        Battery battery = new Battery(batterySizeAh, 48, 0.95, 80, 5000, 600, 3);
        DailyState currState = new DailyState();

        final double panelLifespan = 25;

        Joule fromGenerator = new Joule();
        Joule genFromLast24h = new Joule();
        Joule totalExcessGeneration = new Joule();
        Joule totalDemand = new Joule();

        int batteriesUsed = 1;
        int generatorStarts = 0;
        int totalDays = 0;
        Random random = new Random(1);

        boolean isGeneratorOn = false;
        boolean wasGeneratorOn = false;

        int linesPrinted = 0;

        final Second minute = new Second(60);

        Map<String, Joule> monthlyLoadsBySource = new HashMap<>();
        if(whatToPrint == WhatToPrint.LoadDistributionBySource)
        {
            for(Demand demand : demandModel.demands)
            {
                System.out.print(demand.getName() + "\t");
            }
            System.out.println();
        }

        for(int iters = 0; iters < 8; ++iters)
        {
            for (int i = 1; i < records.size(); i++)
            {
                WeatherRecord prevRecord = records.get(i - 1);
                WeatherRecord nextRecord = records.get(i);

                try
                {
                    Joule hourlyDemand = new Joule();
                    Joule hourlyGeneration = new Joule();

                    for (int m = 0; m < 60; ++ m)
                    {
                        WeatherRecord record = prevRecord.interpolate(m / 60.0, nextRecord);
                        currState.updateTempLimits(record.temp);

                        int mm = m; //main.java.record.advanceTimeBy30Minutes(m);
                        Joule generation = solarSystem.getEnergyInMinute(record, mm);
                        currState.dailyGeneration.addI(generation);

                        DemandData demandData = new DemandData(solarSystem, battery, record, mm, random);

                        if(whatToPrint == WhatToPrint.LoadDistributionBySource)
                        {
                            for(Demand demand : demandModel.demands)
                            {
                                Joule old = monthlyLoadsBySource.getOrDefault(demand.getName(), new Joule());
                                old.addI(minute.mul(demand.getAveragePowerAtTimeW(demandData)));
                                monthlyLoadsBySource.put(demand.getName(), old);
                            }
                        }

                        Watt powerAverage = demandModel.getAveragePowerAtTimeW(demandData);
                        Joule demand = minute.mul(powerAverage);

                        hourlyDemand.addI(demand);
                        hourlyGeneration.addI(generation);


                        Joule excessDemand = demand.sub(generation);
                        excessDemand.maxI(0);
                        Joule excessGeneration = generation.sub(demand);
                        excessGeneration.maxI(0);
                        Joule unmetDemand = new Joule();

                        if(excessDemand.gt(0))
                        {
                            Joule fromBattery = battery.draw(excessDemand);
                            unmetDemand.addI(excessDemand.sub(fromBattery));

                            if(unmetDemand.gt(0.1))
                            {
                                fromGenerator.addI(unmetDemand);
                                isGeneratorOn = true;
                            }
                            else
                            {
                                isGeneratorOn = false;
                                wasGeneratorOn = false;
                            }
                        }
                        else if(excessGeneration.gt(0))
                        {
                            isGeneratorOn = false;
                            wasGeneratorOn = false;

                            Joule toBattery = battery.charge(excessGeneration);
                            excessGeneration.sub(toBattery);

                            totalExcessGeneration.addI(excessGeneration);
                        }

                        if(isGeneratorOn && ! wasGeneratorOn)
                        {
                            ++ generatorStarts;
                            wasGeneratorOn = true;
                        }

                        currState.updateDOD(battery.getDepthOfDischarge());

                        if(whatToPrint == WhatToPrint.TwoWeekByTheMinute && linesPrinted < 14 * 1440)
                        {
                            System.out.println(String.format("%d\t%3.1f\t%3.1f\t%5.2f\t%5.2f\t%5.2f\t%5.3f",
                                    prevRecord.day,
                                    currState.minTemp,
                                    record.solarWm2,
                                    generation.value,
                                    demand.value,
                                    unmetDemand.value,
                                    battery.getDepthOfDischarge()));

                            ++ linesPrinted;
                        }
                    }

                    if(whatToPrint == WhatToPrint.SingleYearByTheHour && linesPrinted < 365 * 24)
                    {
                        System.out.println(String.format("%3.1f\t%3.1f\t%5.2f\t%5.2f\t%5.2f",
                                currState.minTemp,
                                prevRecord.solarWm2,
                                hourlyGeneration.toKwh().value,
                                hourlyDemand.toKwh().value,
                                battery.getDepthOfDischarge()));

                        ++ linesPrinted;
                    }

                    currState.dailyDemand.addI(hourlyDemand);
                    totalDemand.addI(hourlyDemand);

                    if(prevRecord.hour == 23)
                    {
                        if(whatToPrint == WhatToPrint.DailyExtremes && totalDays % 30 == 29)
                        {
                            Joule generatorUsage24h = fromGenerator.sub(genFromLast24h);
                            genFromLast24h = fromGenerator.copy();

                            System.out.println(String.format("%d\t%3.1f\t%3.1f\t%5.2f\t%5.2f\t%5.2f\t%5.3f\t%5.3f",
                                    prevRecord.day,
                                    currState.minTemp,
                                    currState.maxTemp,
                                    currState.dailyGeneration.toKwh().value,
                                    currState.dailyDemand.toKwh().value,
                                    generatorUsage24h.toKwh().value,
                                    currState.leastDOD,
                                    battery.getFractionUsableEnergy()));

                            ++ linesPrinted;
                        }
                        else if(whatToPrint == WhatToPrint.LoadDistributionBySource && linesPrinted < 1000) // && totalDays % 7 == 0)
                        {
                            for(Demand demand : demandModel.demands)
                            {
                                Joule energy = monthlyLoadsBySource.getOrDefault(demand.getName(), new Joule());
                                System.out.print(String.format("%4.3f\t", energy.toKwh().value));
                            }
                            System.out.println();

                            monthlyLoadsBySource.clear();

                            ++linesPrinted;
                        }

                        battery.ageOneDay(currState);
                        currState = new DailyState();

                        ++ totalDays;

                        if(battery.getAge() / 365 > batteryObselAge)
                        {
                            battery.setAge(0);
                            ++ batteriesUsed;
                        }

                        solarSystem.ageOneDay();

                        if(solarSystem.getAgeDays() / 365 > panelLifespan)
                            solarSystem.setAgeDays(0);
                    }
                }
                catch (Exception e)
                {
                }
            }
        }

        final double panelAreaM2 = 1.9443;
        final double batteryCostPerWH = 0.42; //5 - 2.63158e-6 * battery.getMaxEnergyWH();
        final double solarPanelCost = 60;
        final double totalYears = totalDays / 365.24;
        final Watt generatorWatts = new Watt(5000.0);

        double generatorHoursPerYear = fromGenerator.div(generatorWatts).toHour().value / totalYears;
        double dollarsFuelPerYear = generatorHoursPerYear * 2.83 * 1.1;

        final double generatorCost = 2000;
        final double generatorLifespanHours = 2000;
        final double generatorCostPerHour = generatorCost / generatorLifespanHours;
        final double numPanels = solarAreaM2 / panelAreaM2;
        final double solarInstallCostPerPanel = 50;
        double solarPowerCostPerYear = (numPanels * solarPanelCost + Math.sqrt(numPanels) * solarInstallCostPerPanel) / panelLifespan;
        double generatorCostPerYear = dollarsFuelPerYear + generatorHoursPerYear * generatorCostPerHour + generatorStarts * 5;
        double batteryCostPerYear = batteryCostPerWH / 3600.0 * battery.getMaxEnergy().value / batteryObselAge;
        double totalCost = solarPowerCostPerYear + generatorCostPerYear + batteryCostPerYear;

        return new SimulRecord(
                batterySizeAh, batteryObselAge, solarAreaM2, zenith, azimuth,
                generatorStarts / totalYears,
                totalCost,
                generatorCostPerYear,
                batteryCostPerYear,
                solarPowerCostPerYear,
                totalDemand.toKwh().value / totalYears,
                totalExcessGeneration.toKwh().value / totalYears
        );
    }

    private static List<WeatherRecord> getRecords()
    {
        int lengths[] = new int[]{ 8, 10, 4, 5, 6, 6, 6, 5, 5, 8, 4, 5, 5, 9, 6, 5, 5, 4, 5, 3, 3, 2 };

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        TimeZone tz = TimeZone.getTimeZone("AST");
        Calendar.getInstance().setTimeZone(tz);
        Calendar cal = new GregorianCalendar();
        List<WeatherRecord> records = new ArrayList<>();

        try
        {
            InputStream is = OffGridPowerSimulation.class.getClassLoader().getResourceAsStream("CAN_NB_FREDERICTON_8101505_CWEEDS2011_T_N.WY3");

            Scanner s = new Scanner(is);
            s.nextLine();

            String[] fields = new String[lengths.length];
            int parseExceptions = 0;
            int numberFormatExceptions = 0;
            while (s.hasNext())
            {
                String line = s.nextLine();
                int index = 0;
                for (int i = 0; i < fields.length; ++ i)
                {
                    fields[i] = line.substring(index, index + lengths[i]);
                    index += lengths[i];
                }

                try
                {
                    Date date = sdf.parse(fields[1]);
                    cal.setTime(date);
                    int solarWm2 = Integer.valueOf(fields[5].substring(0, 5).trim());
                    int windDirection = Integer.valueOf(fields[16].substring(1, 5).trim());
                    int windspeed = Integer.valueOf(fields[17].substring(1, 4).trim());
                    //                int sunniness = Integer.valueOf(fields[19].trim());

                    int dryBulbTemp = Integer.valueOf(fields[15].trim());

                    //                if(sunniness == 99)
                    //                    continue;

                    double temperatureC = dryBulbTemp / 10.0;
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

                    records.add(new WeatherRecord(temperatureC, solarWm2, windDirection, windspeed, dayOfYear, hour, date.getTime()));
                }
                catch (ParseException e)
                {
                    ++ parseExceptions;
                }
                catch (NumberFormatException e)
                {
                    ++ numberFormatExceptions;
                }
            }

//            System.out.println("Parse exceptions: " + parseExceptions);
//            System.out.println("Number format exceptions: " + numberFormatExceptions);
            System.out.println("Total records: " + records.size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return records;
    }


    private static void addDemandSources(AggregateDemand demandModel)
    {
        // heat pump
        demandModel.addDemand(new HeatPumpDemand());

        // range
        demandModel.addDemand(new ComputedDemand(r -> {
            // a saturday evening dinner
            if(r.record.hour == 20)
            {
                if(r.record.day % 7 == 0)
                {
                    if(r.minute < 30)
                        return new Watt(1800.);
                }
                else
                {
                    if(r.minute < 15)
                        return new Watt(1800.);
                }
            }

            // baking something for an hour once a week
            if(r.record.day % 7 == 1 && r.record.hour == 14)
            {
                return new Watt(r.minute < 15 || r.minute % 5 < 2 ? 2000.0 : 0.0);
            }

            // fried eggs or whatnot in the morning
            if(r.record.hour == 9 && r.record.day % 7 == 4 && r.minute < 10)
            {
                return new Watt(1800.);
            }

            return new Watt(0.);
        }, "Range"));

        // wood insert
        demandModel.addDemand(new ComputedDemand(r -> {
            if(r.record.temp < 5 && r.record.hour >= 9 && r.record.hour <= 20)
                return new Watt(0.25 * 120);
            return new Watt(0.0);
        }, "Insert"));

        // pc & modem
        demandModel.addDemand(new ComputedDemand(r -> {
            int watts = 0;

            int semiRandHourChooser = ((r.record.day + r.record.hour) * 1947 % 9);
            if(semiRandHourChooser < 7 && r.record.hour >= 9 && r.record.hour <= 23)
                watts += (120 + 3 * 20 + 10);

            // watching something on projector
            if(r.record.hour == 23 && r.record.day % 3 == 0)
                watts += 300;

            return new Watt(watts);
        }, "PC"));

        // refrigerator
        demandModel.addDemand(new ComputedDemand(r -> {
//            long remainder = (r.main.java.record.timeSeconds % 3600;
            if(r.minute == 23)
                return new Watt((100 + r.random.nextInt(300)));
            if(r.minute > 23 && r.minute < 50)
                return new Watt(73 - r.minute / 6.0);
            return new Watt(0.);
        }, "Fridge"));

        // lights
        demandModel.addDemand(new ComputedDemand(r -> {
            if(r.record.hour >= 9 && r.record.hour <= 22 && r.record.solarWm2 < 200)
                return new Watt(80.0);
            return new Watt(0.);
        }, "Lighting"));

        // water heater
        demandModel.addDemand(new ComputedDemand(r -> {
            if(r.record.hour % 6 == 0 && r.minute < 20)
                return new Watt(1500.0);
            return new Watt(0.0);
        }, "Water Heater"));

        //        // dryer
        //        demandModel.addDemand(new ComputedDemand(r -> {
        //            if(r.)
        //                return 1500.0;
        //            return 0.0;
        //        }));

        // well pump
        demandModel.addDemand(new ComputedDemand(r -> {
            if(r.record.hour % 5 == 0 && r.minute < 1)
            {
                return new Watt(2000.0);
            }
            return new Watt(0.0);
        }, "Well Pump"));

        // cooking appliances
        demandModel.addDemand(new ComputedDemand(r -> {
            // toaster
            if(r.record.hour == 9 && r.minute < 5)
                return new Watt(800.0);

            // kettle
            switch(r.record.hour)
            {
                case 9: case 16: case 20:
                if(r.minute > 5 && r.minute < 10)
                    return new Watt(1500.0);

                break;
                default:
            }

            // stand mixer
            if(r.record.day % 7 > 5 && r.record.hour == 16)
                if(r.minute < 10)
                    return new Watt(4.2*120);

            return new Watt(0.0);
        }, "Cooking Apps"));
    }

}
