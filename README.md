# MediTrak
An Android app that helps users keep track of their medications.

## About

MediTrak is an Android application designed to make it easier for users to keep track of their medications and to help them remember to take them. It offers the ability to add medications for multpile patients, set medication reminders on a variety of intervals, and take notes on a medication to record any adverse effects if might cause. All user data is stored locally is not share with either the developer or any other third party.

## Requirements

+ Android 8.0 Oreo or newer
+ 15 MB storage

## Building the App

It is strongly recommended to use [Android Studio](https://developer.android.com/studio) to build and test this application.

### Additional Dependencies
#### C/C++ SQLite3 Library

This project uses the ````sqlite3.c```` and ````sqlite3.h```` files in its NDK portion. These can be found [here](https://www.sqlite.org/download.html) in the zip file under "Source Code". In order for CMake to find them, and enviroment variable called ````SQLITE3_LIB_PATH```` has to be set to their path.

##### Setting the Envrioment Variable

- Linux (native)
  ````
  export SQLITE3_LIB_PATH=/path/to/dir/
  ````

  It is strongly recommended to add this to the ````.bashrc```` file in your home directory so they will persist across sessions.

- Linux (Flatpak)

  ````
  flatpak override --user --env=SQLITE3_LIB_PATH=/path/to/dir/ com.google.AndroidStudio
  ````

- Windows
  ````
  [Environment]::SetEnvironmentVariable('SQLITE3_LIB_PATH', 'C:\path\to\dir', 'Machine')
  ````

  This will create an environment variable that will persist across sessions.

## Installation

### GitHub

The latest releases can be found here: [releases](https://github.com/AdamGuidarini/MediTrak/releases). You may need to enable downloading apps from unknown sources to install it.

### F-Droid

Thanks to [IzzySoft](https://github.com/IzzySoft), this app is available in the [IzzyOnDroid](https://apt.izzysoft.de/fdroid/) F-Droid repository [here](https://apt.izzysoft.de/fdroid/index/apk/projects.medicationtracker/).

##### Adding the repository to the F-Droid App

- Download [F-Droid](https://f-droid.org/) (if not already installed)
- Navigate to "Settings"
- Under the "My Apps" section, select "Repositories"
- Click on the "+" button
- Copy the following URL: https://apt.izzysoft.de/fdroid/repo?fingerprint=3BF0D6ABFEAE2F401707B6D966BE743BF0EEE49C2561B9BA39073711F628937A
- After that, you should be able to find it by searching for "MediTrak"

## Screenshots

<img src="https://github.com/AdamGuidarini/MediTrak/assets/45023561/fb0a3f87-60be-4ff8-a918-26a11f2ac9ec" width=10% height=10%>
<img src="https://github.com/AdamGuidarini/MediTrak/assets/45023561/be7fbc82-8c02-445e-a540-eab809df52d1" width=10% height=10%>
<img src="https://github.com/AdamGuidarini/MediTrak/assets/45023561/6cea93a1-56db-41fd-91c2-45450e82cc0a" width=10% height=10%>
<img src="https://github.com/AdamGuidarini/MediTrak/assets/45023561/3b0f4d3a-5625-4690-a752-8b1afe44eb86" width=10% height=10%>
