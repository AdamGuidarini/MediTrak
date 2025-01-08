# MediTrak
An Android app that helps you keep track of your medications.

## About

MediTrak is an Android application designed to make it easier for users to keep track of their medications and to help them remember to take them. It offers the ability to add medications for multpile patients, set medication reminders on a variety of intervals, and take notes on a medication to record any adverse effects if might cause. All user data is stored locally is not share with either the developer or any other third party.

## Requirements

- Android 10+

## Languages
 - **English** (Default)
 - Dutch by [ruditimmermans](https://github.com/ruditimmermans)<sup>1,2</sup>
 - German by [uDEV2019](https://github.com/uDEV2019)<sup>1,2</sup>
 - Italian<sup>4</sup>
 - Spanish by [zaovb](https://github.com/zaovb)<sup>1,3</sup>
 - Turkish by [mikropsoft](https://github.com/mikropsoft)<sup>1,2</sup>

<ol style="font-size: 0.8em;">
<li> Translated by native speaker.</li>
<li> Additional translations provided by non-native speaker through a machine translator.</li>
<li> Additional translations provided by non-native speaker.</li>
<li> Translated by a non-native speaker.</li>
</ol>

## Building the App

It is strongly recommended to use [Android Studio](https://developer.android.com/studio) to build and test this application.

### Additional Dependencies
#### C/C++ SQLite3 Library

This project uses the ````sqlite3.c```` and ````sqlite3.h```` files in its NDK portion. These can be found [here](https://www.sqlite.org/download.html) in the zip file under "Source Code". In order for CMake to find them, and enviroment variable called ````SQLITE3_LIB_PATH```` has to be set to their path.

##### Setting the Envrioment Variable

- Linux (Native)
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

[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" height="90" alt="Get it on GitHub"/>](https://github.com/AdamGuidarini/MediTrak/releases)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height=90 alt="Get it on IzzyOnDroid"/>](https://apt.izzysoft.de/fdroid/index/apk/projects.medicationtracker)

### GitHub

- The latest releases can be found here: [releases](https://github.com/AdamGuidarini/MediTrak/releases). You may need to enable downloading apps from unknown sources to install it.
- You can also use the app [Obtainium](https://github.com/ImranR98/Obtainium) or others like it to automatically update to the newest release on GitHub.

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

<div style="display: flex;" align="center">
 <img src="https://github.com/AdamGuidarini/MediTrak/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/01.png?raw=true" width="15%">
 <img src="https://github.com/AdamGuidarini/MediTrak/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/02.png?raw=true" width="15%">
 <img src="https://github.com/AdamGuidarini/MediTrak/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/03.png?raw=true" width="15%">
 <img src="https://github.com/AdamGuidarini/MediTrak/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/04.png?raw=true" width="15%">
 <img src="https://github.com/AdamGuidarini/MediTrak/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/06.png?raw=true" width="15%">
 <img src="https://github.com/AdamGuidarini/MediTrak/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/05.png?raw=true" width="15%">
</div>
