# MedicationTracker
An Android app that helps users keep track of their medications.

Development plan as of January 22, 2022:

Requirements:

  + Android 8.0 Oreo or newer
  + 10 MB storage

Status:
  
  + In active development, this app is a personal project started when.
  + App can be used as a checklist for medications but notifications are semi-functional. The app can generate separate notifications for each medication dose but if you delete a medication notifications for the current scheduling week will still occur...which isn't exactly what I want.

Completed:

  + AddMedication activty - Gives users the ability to add a new medication.
  + MainActivity - Provides a GUI to view all medications to be taken during the current week. User can mark medications taken or not.
  + Medication notes - Allows users to take notes to keep track of how their medications affect them.
  + MyMedications activity - Displays all medications stored in the database. Allows users to edit, cancel, or delete them, make notes about them, and view statistics about them.

In progress:

  + Refactoring and code clean-up.
  + Bug fixes - The app sometimes crashes when editing medicaiton times.
  + Notifications - Push notifications are partially functional, however at the moment there is no way to stop pending notifications for deleted medications. The way notifications are created will also need to be changed to use a single repeating alarm instead of multiple single use alarms.

Planned:

  + Improvements to MainActivity - Allow the user to see medications from previous and future weeks.
  + Settings - Make changes to how the app us used, such as how long before the scheduled dose time a medication can be marked as taken.
  + Cancel medication - Medication will no longer be scheduled but notes an other related data will not be deleted.
  + Database Cleaner - Delete doses for medications whose schedules have been changed.