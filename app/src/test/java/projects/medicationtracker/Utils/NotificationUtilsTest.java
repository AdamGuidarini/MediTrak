package projects.medicationtracker.Utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import projects.medicationtracker.Models.Medication;

public class NotificationUtilsTest {

    @Test
    public void canScheduleNotifications_returnsTrueForActiveMedicationWithoutChild() {
        Medication med = new Medication();
        med.setActiveStatus(true);

        assertTrue(NotificationUtils.canScheduleNotifications(med, null));
    }

    @Test
    public void canScheduleNotifications_returnsFalseForInactiveMedication() {
        Medication med = new Medication();
        med.setActiveStatus(false);

        assertFalse(NotificationUtils.canScheduleNotifications(med, null));
    }

    @Test
    public void canScheduleNotifications_returnsFalseForParentMedication() {
        Medication parent = new Medication();
        parent.setActiveStatus(true);
        Medication child = new Medication();
        parent.setChild(child);

        assertFalse(NotificationUtils.canScheduleNotifications(parent, null));
    }

    @Test
    public void canScheduleNotifications_returnsFalseForNullMedication() {
        assertFalse(NotificationUtils.canScheduleNotifications(null, null));
    }
}
