# ISSOT
The ISS Orbit Tracker (ISSOT) is an Android Things project that keeps track of the International Space Station. It calculates and displays the azimuth and elevation during the ISS pass. This relatively simple device will give you a visual indication of the location of the Space Station, even if you can't see it.

### Locating the ISS earth
The ISS conducts operations in the low earth orbit (LEO) about 400 km above the earth's surface and orbits the planet approximately every 90 minutes. Its location in the sky can be determined using the “look angles”; which are two angles called: 

Azimuth (ψ): This is the angle measured in the plane parallel to the horizon from the polar north going clockwise. This angle has a value between 0° and 360° 

Elevation (El): This is the angle measured in the plane perpendicular to the horizon going up towards to the ISS. This angle has a value between –90° and +90° such that the horizontal direction is 0°. A vertically upwards is +90° and vertically downwards is –90°.

The figure below shows the addtional vectors and angles needed to calculate the elevation angle.

![angles](/images/geometry_elevation_angle.jpg)

rS is the vector from the center of the earth to the ISS
rE is the vector from the center of the earth to the  earth station (the tracker)
d is the vector from the tracker to the ISS.  
γ is the central angle measured between rE and rS
ψ is the angle measured from rE to d
