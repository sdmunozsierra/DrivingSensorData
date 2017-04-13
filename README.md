# Crowdsourcing Data using Smartphone's sensors
In this proposal, we aim at collecting smart phones’ sensors data from drivers in the city and analyze it to explore drivers’ interactions with road topology. Such data can be analyzed in combinations with traffic regulations, crash and traffic violations databases. The ultimate goal of this proposal is to improve drivers’ safety and inform the responsible agency to take actions on traffic control and management.

## Features

### Application Features
The application is packed with the following:
* __Smartphone's GPS Location Services:__ Obtain latitude, longitude, and altitude values.
* __Accelerometer and Gyroscope Sensors:__ Collect X, Y, and Z positions in a 3D plane.
* __Interval setting:__ Choose how many times _per second_ record data values.
* __Save records__ to file.
* __Read records:__ Display the records from file.
* __Delete records:__ Erase the records from file.

### UI/UX Features
The applicatino is focused on a friendly user interface for easy experience out of the box.
* Inspired on [Material Design](https://material.io/guidelines/#introduction-principles) by Google.
* Uses a fancy [Bottom bar](https://github.com/roughike/BottomBar) to perform the actions.

## Dependencies and Permissions

__Permissions__
* Access Fine Location (GPS Location)
* Write External Storage (File Management)

__Dependencies__
* Google Play Services (See Notes)
* [Bottom bar](https://github.com/roughike/BottomBar)

### Upcpming Features

* Send file through email

## Notes

Location is developed using __Google Play Services__. This is because the Android.Location services are not longer maintained and the documentatino recommend to switch to GP Services.

_If you're cloning from source:_ Download and set up [google play services](https://developers.google.com/android/guides/setup) in Android Studio. You must edit build.graddle to add the dependency and be sure you _update_ the version number each time [Google Play services](https://developers.google.com/android/guides/releases) is updated.

## Download, Test, Fork, Feedback

### Download 

For detailed steps on how to build and install from source please visit the [App's Wiki](https://github.com/sdmunozsierra/DrivingSensorData/wiki).

To use in physical device:
1. Download the lattest _unsigned_ .APK [ADD LINK]
2. Enable unknown sources
3. Install
4. Test
5. Feedback 

To use in an emulator:
1. download the latest Android x86 Image with Google API (Else won't work).
2. Install
3. Test 
4. Feedback
  
### Tested on Devices
* Alcatel OneTouch Pixi 4 (7") [9000A] - Android Marshmallow 6.0 
