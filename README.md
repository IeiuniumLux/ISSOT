# ISSOT
The ISS Orbit Tracker (ISSOT) is an [Android Things](https://developer.android.com/things/get-started/index.html) project that keeps track of the [International Space Station](https://www.nasa.gov/pdf/508318main_ISS_ref_guide_nov2010.pdf). It calculates and displays the azimuth and elevation during the ISS pass. This relatively simple device will give you a visual indication of the location of the Space Station, even if you can't see it.

### Locating the ISS earth
The ISS conducts operations in the low earth orbit (LEO) about 400 km above the earth's surface and orbits the planet approximately every 90 minutes. Its location in the sky can be determined using the “look angles”; which are two angles called: 

Azimuth (ψ): This is the angle measured in the plane parallel to the horizon from the polar north going clockwise. This angle has a value between 0° and 360° 

Elevation (El): This is the angle measured in the plane perpendicular to the horizon going up towards to the ISS. This angle has a value between –90° and +90° such that the horizontal direction is 0°. A vertically upwards is +90° and vertically downwards is –90°.

The figure below shows the addtional vectors and angles needed to calculate the elevation angle.

![angles](/images/geometry_elevation_angle.jpg)

+ rS is the vector from the center of the earth to the ISS
+ rE is the vector from the center of the earth to the  earth station (the tracker)
+ d is the vector from the tracker to the ISS.  
+ γ is the central angle measured between rE and rS
+ ψ is the angle measured from rE to d


## What you'll need

- [Android Studio 3.0+](https://developer.android.com/studio/index.html)
- The following individual components:
  - [Raspberry Pi 3 Model B](https://www.adafruit.com/product/3055)
  - [DC & Stepper Motor HAT](https://www.adafruit.com/product/2348)
  - [Pimoroni Rainbow HAT](https://www.adafruit.com/product/3354)
  - [5V 2.4A Power Supply](https://www.adafruit.com/product/1995)
  - [Stepper Motor](https://www.pololu.com/product/1200)
  - [HS-322HD Servo](https://www.servocity.com/hs-322hd-servo)
  - [Set Screw Hub - 5mm Bore](https://www.servocity.com/770-set-screw-hubs)
  - [5/16" – 3/8" Rubber End Cap](https://www.servocity.com/0-375-3-8-rubber-end-cap)
  - [Two(2) 1" Bore Bottom Tapped Clamping Mount](https://www.servocity.com/1-bore-bottom-tapped-clamping-mounts)
  -
  - [Actobotics® Hardware Pack A - (optional, but recommended)](https://www.servocity.com/actobotics-hardware-pack-a)
  
- Install Android Things on the Raspberry Pi 3 (flashing instructions [here](https://developer.android.com/things/hardware/raspberrypi.html))


## References
- https://spotthestation.nasa.gov/home.cfm
- https://www.ngdc.noaa.gov/geomag-web/#declination
