package projects.medicationtracker;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static java.time.temporal.TemporalAdjusters.previous;
import static java.util.Calendar.SUNDAY;

public class CardCreator
{
    /**
     * Creates a schedule for the given patient's medications
     *
     * @param medications An ArrayList of Medications. Will be searched for
     *                    Medications where patientName equals name passed to method.
     * @param name The name of the patient whose Medications should be displayed
     * @param db The database from which to pull data
     * @param scheduleLayout The LinearLayout into which the cards will be placed
     **************************************************************************/
    public static void createMedicationSchedule(ArrayList<Medication> medications, String name, DBHelper db, LinearLayout scheduleLayout)
    {
        ArrayList<Medication> medicationsForThisPatient = new ArrayList<>();

        for (int i = 0; i < medications.size(); i++)
        {
            if (medications.get(i).getPatientName().equals(name))
                medicationsForThisPatient.add(medications.get(i));
        }

        String[] days = {" Sunday", " Monday", " Tuesday", " Wednesday", " Thursday", " Friday", " Saturday"};

        for (int ii = 0; ii < 7; ii++)
            createDayOfWeekCards(days[ii], ii, medicationsForThisPatient, scheduleLayout, db, scheduleLayout.getContext());
    }

    /**
     * Creates a CardView for each day of the week containing information
     * on the medications to be taken that day
     *
     * @param dayOfWeek The day of the week represented by the CardView.
     * @param day The number representing the day of the week
     *            - Sunday = 0
     *            - Monday = 1
     *            - Tuesday = 2
     *            - Wednesday = 3
     *            - Thursday = 4
     *            - Friday = 5
     *            - Saturday = 6
     * @param medications The list of medications to be taken on the given day
     * @param layout The LinearLayout in which to place the CardView
     **************************************************************************/
    public static void createDayOfWeekCards (String dayOfWeek, int day, ArrayList<Medication> medications, LinearLayout layout, DBHelper db, Context context)
    {
        CardView thisDayCard = new CardView(context);
        TextView dayLabel = new TextView(context);
        LinearLayout ll = new LinearLayout(context);

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llParams);
        ll.setOrientation(LinearLayout.VERTICAL);

        setCardParams(thisDayCard);

        // Add day to top of card
        dayLabel.setText(dayOfWeek);
        ll.addView(dayLabel);

        // Add medications
        thisDayCard.addView(ll);

        LocalDate thisSunday = LocalDate.now().with(previous(DayOfWeek.of(SUNDAY)));

        for (int i = 0; i < medications.size(); i++)
        {
            for (LocalDateTime time : medications.get(i).getTimes())
            {
                if (time.toLocalDate().isEqual(thisSunday.plusDays(day - 1)))
                {
                    CheckBox thisMedication = new CheckBox(ll.getContext());
                    int medId = medications.get(i).getMedId();

                    // Set Checkbox label
                    String medName = medications.get(i).getMedName();
                    String dosage = medications.get(i).getMedDosage() + " " + medications.get(i).getMedDosageUnits();
                    String dosageTime = TimeFormatting.formatTimeForUser(time.getHour(), time.getMinute());

                    String thisMedicationLabel = medName + " - " + dosage + " - " + dosageTime;
                    thisMedication.setText(thisMedicationLabel);

                    //TODO Change this so it is done with AlarmManager
                    // Check database for this dosage, if not add it

                    // if it is, get the DoseId
                    long rowid = 0;

                    if (!db.isInMedicationTracker(medications.get(i), time))
                    {
                        LocalDateTime startDate = medications.get(i).getStartDate();
                        if (time.isEqual(startDate) || time.isAfter(startDate))
                        {
                            rowid = db.addToMedicationTracker(medications.get(i), time);
                            if (rowid == -1)
                                Toast.makeText(context, "An error occurred when attempting to write data to database", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        rowid = db.getDoseId(medId, TimeFormatting.LocalDateTimeToString(time));
                    }

                    if (rowid > 0)
                    {
                        thisMedication.setTag(rowid);

                        if (db.getTaken(rowid))
                            thisMedication.setChecked(true);

                        thisMedication.setOnCheckedChangeListener((compoundButton, b) ->
                        {
                            final int doseId = Integer.parseInt(thisMedication.getTag().toString());

                            if (LocalDateTime.now().isBefore(time.minusHours(2)))
                            {
                                thisMedication.setChecked(false);
                                Toast.makeText(context, "Cannot take medications more than 2 hours in advance", Toast.LENGTH_SHORT).show();
                                return;
                            }


                            String now = TimeFormatting.LocalDateTimeToString(LocalDateTime.now());
                            db.updateDoseStatus(doseId, now, thisMedication.isChecked());
                        });

                        ll.addView(thisMedication);
                    }
                }
            }
        }

        if (ll.getChildCount() == 1)
        {
            TextView textView = new TextView(thisDayCard.getContext());
            String noMed = "No medications for " + dayOfWeek;

            textView.setText(noMed);
            ll.addView(textView);
        }

        layout.addView(thisDayCard);
    }

    /**
     * Creates a CardView containing all information on a Medication
     * @param medication The Medication whose details will be displayed.
     * @param db The database storing data on the medication.
     * @param baseLayout The LinearLayout in which to place the card
     **************************************************************************/
    public static void createMyMedCards(Medication medication, DBHelper db, LinearLayout baseLayout)
    {
        Context context = baseLayout.getContext();
        CardView thisMedCard = new CardView(context);
        LinearLayout thisMedLayout = new LinearLayout(context);
        baseLayout.addView(thisMedCard);

        setCardParams(thisMedCard);

        thisMedCard.addView(thisMedLayout);

        // Add name to thisMedLayout
        TextView name = new TextView(context);
        name.setText(medication.getMedName());
        thisMedLayout.addView(name);

    }

    /**
     * Sets a CardView's parameters to a set standard
     * @param cardView The CardView whose parameters will be set
     **************************************************************************/
    public static void setCardParams(CardView cardView)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(layoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
        marginLayoutParams.setMargins(25, 40, 25, 40);
        cardView.requestLayout();
    }
}
