# MediTrak
An Android app that helps users keep track of their medications.

### Requirements:

  + Android 8.0 Oreo or newer
  + 10 MB storage

### Status:
  
  + In active development, this app is a personal project that I work on in my freetime.
  + The app is mostly complete and can largely be used for its intended purpose. Improvements to the UI, performance, and additional features are planned.

### Completed:

  + AddMedication activty - Gives users the ability to add a new medication.
  + MainActivity - Provides a GUI to view all medications to be taken during the current week. User can mark medications taken or not.
  + Medication notes - Allows users to take notes to keep track of how their medications affect them.
  + MyMedications activity - Displays all medications stored in the database. Allows users to edit, cancel, or delete them, make notes about them, and view statistics about them.
  + Notifications - Push notifications that remind you to take your medications at the times you set.
  + Settings - Make changes to how the app us used, such as how long before the scheduled dose time a medication can be marked as taken.

### In progress:

  + Refactoring and code clean-up.
  + Bug fixes - The app sometimes crashes when editing medicaiton times.

### Planned:

+ Cancel medication - Medication will no longer be scheduled but notes an other related data will not be deleted.
  + Database Cleaner - Delete doses for medications whose schedules have been changed.
  + User interface improvements.
  + Mark a medication as take from it's notification.
