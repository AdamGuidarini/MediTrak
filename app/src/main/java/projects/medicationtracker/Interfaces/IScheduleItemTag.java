package projects.medicationtracker.Interfaces;

import java.time.LocalDateTime;

import projects.medicationtracker.SimpleClasses.Medication;

public interface IScheduleItemTag
{
    Medication medication = new Medication();
    Long doseId = 0L;
    LocalDateTime doseTime = LocalDateTime.now();
}
