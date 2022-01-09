# MedicationTracker
An Android app that helps users keep track of their medications.

Development plan as of May 25, 2021:

Requirements:

  + Android 8.0 Oreo or newer
  + 10 MB storage

Status:
  
  + In active development.
  + App can be used as a checklist for medications but notifications are not yet functional.

Completed:

  + AddMedication activty - Gives users the ability to add a new medication.
  + MainActivity - Provides a GUI to view all medications to be taken during the current week. User can mark medications taken or not.
  + Medication notes - Allows users to take notes to keep track of how their medications affect them.
  + MyMedications activity - Displays all medications stored in the database. Allows users to edit, cancel, or delete them, make notes about them, and view statistics about them.

In progress:

  + Refactoring and code clean-up.

  + Notifications - Push notifications to alter user that it is time to take a medication.

Planned:

  + Improvements to MainActivity - Allow the user to see medications from previous weeks.
  + Settings - Make changes to how the app us used, such as how long before the scheduled dose time a medication can be marked as taken.
  + Cancel medication - Medication will no longer be scheduled but notes an other related data will not be deleted.
