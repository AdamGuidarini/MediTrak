# MediTrak
An Android app that helps users keep track of their medications.

### Requirements:

  + Android 8.0 Oreo or newer
  + 10 MB storage

### Getting Started:
  + To install the app, go the releases tab and selected the latest pre-release version of the app. Your Android device my require you to give your browser permission to download an APK from it instead of the Play Store.
  + Alternatively, to get the newest (and possibly unstable version) you can clone the repo then in Android Studio: Build -> Build Bundle(s)/APK(s) -> Build APK(s). You can then transfer the APK to your phone however you want and use the app.
  + Eventually I do plan on releasing this app on Google Play, but it still has a couple rough patchs that need ironing out before it's ready for that.

### Status:
  
  + In active development, this app is a personal project that I work on in my freetime.
  + The app is largely functioning as intended with notifications and nvaigation to past and future weeks being fully functional. Most of the updates I'm planning will have to do more with styling than new features, however I do have a couple ideas for how to make the app more helpful in the works.

### Completed:

  + AddMedication activty - Gives users the ability to add a new medication.
  + MainActivity - Provides a GUI to view all medications to be taken during the current week. User can mark medications taken or not. Users can also use the three buttons on the bottom to view future or previous weeks or return to the current week.
  + Medication notes - Allows users to take notes to keep track of how their medications affect them.
  + MyMedications activity - Displays all medications stored in the database. Allows users to edit, cancel, or delete them, make notes about them, and view statistics about them.
  + Notifications - Push notifications that remind you to take your medications at the times you set.
  + Settings - Make changes to how the app us used, such as how long before the scheduled dose time a medication can be marked as taken.

### In progress:

  + Refactoring and code clean-up.
  + User interface improvements.
  + Bug fixes - There's always a bug hidding somewhere.

### Planned:

+ Cancel medication - Medication will no longer be scheduled but notes an other related data will not be deleted.
+ Database Cleaner - Delete doses for medications whose schedules have been changed.
+ Mark a medication as take from its notification.
