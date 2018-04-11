# ISSOT
The ISS Orbit Tracker (ISSOT) is an [Android Things](https://developer.android.com/things/get-started/index.html) project that keeps track of the [International Space Station](https://www.nasa.gov/pdf/508318main_ISS_ref_guide_nov2010.pdf). It calculates and displays the azimuth and elevation during the ISS orbits. This relatively simple device will give you a visual indication of the location of the Space Station, even if you can't see it.

### Locating the ISS from Earth
The ISS conducts operations in the low earth orbit (LEO) about 400 km above the earth's surface. It travels from west to east on an orbital inclination of 51.6 degrees. Each orbit takes 90-93 minutes, depending on the exact altitude of the ISS. Its location in the sky can be determined using two “look angles” called, Azimuth & Elevation angles.

Azimuth (ψ) is the angle measured in the plane parallel to the horizon from the polar north going clockwise. This angle has a value between 0° and 360°.

Elevation (El) is the angle measured in the plane perpendicular to the horizon going up towards to the ISS. This angle has a value between –90° and +90° such that the horizontal direction is 0°. A vertically upwards is +90° and vertically downwards is –90°.

The figure below shows the addtional vectors and angles needed to calculate the elevation angle.

![angles](/images/geometry_elevation_angle.jpg)
+ rS is the vector from the center of the earth to the ISS
+ rE is the vector from the center of the earth to the  earth station (the tracker)
+ d is the vector from the tracker to the ISS.  
+ γ is the central angle measured between rE and rS
+ ψ is the angle measured from rE to d

## How it works
Built using a Raspberry Pi 3b and the Android Things embedded OS platform, ISSOT utilizes tracking information and motors to point at the ISS. To do so, it uses the [WTIA REST API](https://wheretheiss.at/w/developer) to perform the orbital propagation and coordinate the system transformations. The motors are driven by a [DC & Stepper Motor HAT](https://pinout.xyz/pinout/dc_stepper_motor_hat); which connects to the Raspberry Pi. The stepper motor (geared down with a chain drive) controls the azimuth and the servo controls the elevation. A three(3) wires slip-ring is installed in the bottom of the 6" tube to prevent the cables from getting tangled and allow electrical continuity through the rotating shaft.

The device requires to be initialized at [true north](https://www.ngdc.noaa.gov/geomag-web/#declination) at the moment of powering it up.  After that, it goes through a quick range-of-motion routine before starting to point at the ISS.

> NOTE: While apps like [Spot the Station](https://spotthestation.nasa.gov/home.cfm) alert you when the ISS will be visible, this device lets you know where it is at any point in its orbit.

### What you'll need

- [Android Studio 3.0+](https://developer.android.com/studio/index.html).
- Install Android Things on the Raspberry Pi 3 (flashing instructions [here](https://developer.android.com/things/hardware/raspberrypi.html)).
- The following individual components:

Part             | Qty 
---------------- | ----
[Raspberry Pi 3 Model B](https://www.adafruit.com/product/3055)<br /> | 1 
[DC & Stepper Motor HAT](https://www.adafruit.com/product/2348)<br /> | 1 
[Pimoroni Rainbow HAT](https://www.adafruit.com/product/3354)<br /> | 1 
[5V 4A Power Supply](https://www.ebay.com/itm/152944199414)<br />*or similar* | 1 
[Slip Ring - 3 Wire (10A)](https://www.sparkfun.com/products/13063)<br /> | 1
[Stepper Motor](https://www.pololu.com/product/1200)<br /> | 1 
[HS-322HD Servo](https://www.servocity.com/hs-322hd-servo)<br /> | 1 
[Lightweight HUB Horn](https://www.servocity.com/lightweight-hub-horns)<br /> | 1
[Set Screw Hub - 5mm Bore](https://www.servocity.com/770-set-screw-hubs)<br /> | 1 
[1" Bore, Face Tapped Clamping HUB, 1.50" Pattern](https://www.servocity.com/1-bore-clamping-hub-a)<br /> | 1
[1" Stainless Steel Tubing](https://www.servocity.com/1-00-stainless-steel-tubing)<br />*length: 6.00"* | 1
[Standard Servo Plate C](https://www.servocity.com/standard-servo-plate-c)<br /> | 1
[0.250" Chain (5 Feet)](https://www.servocity.com/0-250-chain-5-feet)<br /> | 1
[3.85" (11 hole) Aluminum Beams (2 PACK)](https://www.servocity.com/3-85-aluminum-beam)<br /> | 1
[1" Bore 0.250" Pitch Aliminum HUB Sprocket](https://www.servocity.com/1-50-aluminum-hub-mount-sprockets-0-250-pitch)<br />*# of teeth: 40T* | 1
[1/2" Bore 0.250" Pitch Aluminum HUB Sprocket](https://www.servocity.com/0-770-aluminum-hub-mount-sprockets-0-250-pitch)<br />*# of teeth: 16T* | 1
[90° Single Angle Pattern Bracket](https://www.servocity.com/90-single-angle-channel-bracket)<br /> | 1
[90° Single Angle Short Pattern Bracket](https://www.servocity.com/90-single-angle-short-channel-bracket)<br /> | 2
[1" Bore Bottom Tapped Clamping Mount](https://www.servocity.com/1-bore-bottom-tapped-clamping-mounts)<br /> | 2 
[1" Bore Side Tapped Pillow Block](https://www.servocity.com/1-000-bore-quad-pillow-block)<br /> | 2
[5/16" – 3/8" Rubber End Cap](https://www.servocity.com/0-375-3-8-rubber-end-cap)<br /> | 2 
[Large Square Screw Plate](https://www.servocity.com/large-square-screw-plate)<br /> | 3
[Flat Dual Pattern Bracket](https://www.servocity.com/flat-dual-channel-bracket)<br /> | 3
[Actobotics® Hardware Pack A](https://www.servocity.com/actobotics-hardware-pack-a)<br />*(optional, but recommended)*| 1
[Splitter Power Adapter Cable 5.5 x 2.1mm](https://www.ebay.com/itm/162601585346) <br />*(optional)* | 1
[Wood Rectangle Plaque](https://www.amazon.com/gp/product/B0085TGTAS/ref=ox_sc_act_title_1?smid=ATVPDKIKX0DER&psc=1) <br />*(optional)* | 1
[Chain Breaker Tool](https://www.pitsco.com/TETRIX-Chain-Breaker-Tool) <br />*(optional)* | 1

> Most of the aluminum structural is based on G. Hillhouse's prototype design.


## References
- http://www.satflare.com/track.asp?q=25544#TOP
- https://eol.jsc.nasa.gov/Tools/orbitTutorial.htm
