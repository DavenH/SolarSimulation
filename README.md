Solar Battery Simulation
===

A battery can be a great expense in a solar system installation. So much so, that it often puts the cost 
out of reach of typical households. 

This project aims to find the perfect sizing of solar array, battery capacity, angles of the solar panels, 
and other parameters to minimize total system costs.  
 
---

###Approach  
 
I use real weather data of solar irradiance, cloud cover, temperature, taken from local measurements to 
simulate solar generation capacity. I use a model of typical household appliances to simulate demand and 
calibrate this usage with my past seasonal power consumption data.  

The trick is to find the best zenith angle of the solar array that maximizes solar generation when demand is at its peak. 
The challenge in northern climates is that electricity demand peaks in winter when the number of solar hours per day is lowest. 


### Results

--- 
#### Solar System Analysis
 
Demand, solar generation, battery depth of discharge, and solar irradiance over a period of approx 2 weeks.
Note how the battery discharges overnight, but not to a DOD that degrades its long term performance.  
![](media/simulation-14d.png)

--- 
This is the simulation over 128 years; note the battery capacity degrading over a period of approx 48 years from regular cycling. 

![](media/simulation-128y.png)

---

Values determined by grid search for the best total system cost as a function of battery size. 
Costs of battery and backup generator are amortized annually.
![](media/SolarVsTime2.png)

---

#### Demand Analysis

![](media/demand-over-year.png)

---

Yearly domestic demand distribution

![](media/demand-pie-chart.png)