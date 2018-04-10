# ISSOT
The ISS Orbit Tracker (ISSOT) is an Android Things project that keeps track of the International Space Station. It calculates and displays the azimuth and elevation during the ISS pass. This relatively simple device will give you a visual indication of the location of the Space Station, even if you can't see it.

### Locating the ISS earth
The ISS conducts operations in the low earth orbit (LEO) about 400 km above the earth's surface and it orbits the planet approximately every 90 minutes. Its location in the sky can be determined using the “look angles”; which are two angles called: 

Azimuth (ψ): This is the angle measured in the plane parallel to the horizon measure from the polar north (the top-most point on Earth) going clockwise. So, this angle has a value between 0° and 360° 

Elevation (El): This is the angle measured in the plane perpendicular to the horizon measure from the horizon going up towards to the vertical line to the horizon. So, this angle has a value between –90° and +90° such that the horizontal direction is 0°, Vertically upwards +90°, vertically downwards is –90°.

In addition to these angles, it is also useful to know the distance (d) from the tracker to the ISS and the earth’s central angle (γ); which is the angle at the center of earth between the location of the tracker and the location of the ISS. The above angles are shown in the following figure:

![angles](/images/geometry_elevation_angle.jpg)
