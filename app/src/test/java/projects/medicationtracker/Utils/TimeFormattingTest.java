package projects.medicationtracker.Utils;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.Test;

import projects.medicationtracker.Models.Medication;

public class TimeFormattingTest {

    @Test
    public void stringToLocalDateTime_parsesDateTimeWithoutSeconds() {
        LocalDateTime parsed = TimeFormatting.stringToLocalDateTime("2025-04-08 00:00");

        assertEquals(LocalDateTime.of(2025, 4, 8, 0, 0, 0), parsed);
    }

    @Test
    public void medicationConstructor_parsesLegacyTimesWithoutSeconds() {
        Medication medication = new Medication(
                "Example",
                "Patient",
                "mg",
                new String[]{"2025-04-08 00:00"},
                "2025-04-08 00:00",
                1L,
                1440,
                1.0f,
                ""
        );

        assertEquals(LocalDateTime.of(2025, 4, 8, 0, 0, 0), medication.getStartDate());
        assertEquals(LocalDateTime.of(2025, 4, 8, 0, 0, 0), medication.getTimes()[0]);
    }

    @Test
    public void stringToLocalTime_parsesTimeWithoutSeconds() {
        LocalTime parsed = TimeFormatting.stringToLocalTime("00:00");

        assertEquals(LocalTime.of(0, 0, 0), parsed);
    }

    @Test
    public void stringToLocalTime_parsesSingleDigitSeconds() {
        LocalTime parsed = TimeFormatting.stringToLocalTime("21:45:5");

        assertEquals(LocalTime.of(21, 45, 50), parsed);
    }
}

