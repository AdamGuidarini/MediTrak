package projects.medicationtracker.Fragments;

import android.os.Bundle;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.TextViewUtils;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Medication;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MedicationScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedicationScheduleFragment extends Fragment
{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String MEDICATIONS = "medications";
    public static final String DAY_OF_WEEK = "dayOfWeek";
    public static final String DAY_IN_CURRENT_WEEK = "dayInCurrentWeek";
    public static final String DAY_NUMBER = "dayNumber";
    private View rootView;

    private static ArrayList<Medication> meds;
    private static String dayOfWeek;
    private static LocalDate dayInCurrentWeek;
    private static int dayNumber;

    public MedicationScheduleFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param medications Medications to display in schedule.
     * @param day The name of the day this fragment will display
     * @param aDayThisWeek A LocalDate in the week the user is viewing
     * @param dayNum the number of the day in the week being viewed (0 Sunday - 6 Saturday)
     *
     * @return A new instance of fragment MedicationScheduleFragment.
     */
    public static MedicationScheduleFragment newInstance(
            ArrayList<Medication> medications,
            String day,
            LocalDate aDayThisWeek,
            int dayNum
    )
    {
        MedicationScheduleFragment fragment = new MedicationScheduleFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(MEDICATIONS, (Parcelable) medications);
        bundle.putString(DAY_OF_WEEK, day);
        bundle.putLong("dayInCurrentWeek", aDayThisWeek.toEpochDay());
        bundle.putInt("dayNumber", dayNum);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    /**
     * Builds an instance of the fragment
     * @return The fragment inflated
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    )
    {
        assert getArguments() != null;
        meds = getArguments().getParcelableArrayList(MEDICATIONS);
        dayOfWeek = getArguments().getString(DAY_OF_WEEK);
        dayInCurrentWeek = LocalDate.ofEpochDay(getArguments().getLong(DAY_IN_CURRENT_WEEK));
        dayNumber = getArguments().getInt(DAY_NUMBER);

        rootView = inflater.inflate(
                R.layout.fragment_medication_schedule,
                container,
                false
        );

        createSchedule(rootView);

        return rootView;
    }

    /**
     * Creates a list of the medications for the current given day in places them in the fragment
     * @param rootView The main view of the fragment
     */
    private void createSchedule(View rootView)
    {
        LinearLayoutCompat checkBoxHolder = rootView.findViewById(R.id.medicationSchedule);
        TextView dayLabel = rootView.findViewById(R.id.dateLabel);
        LocalDate thisSunday = TimeFormatting.whenIsSunday(dayInCurrentWeek);
        DBHelper db = new DBHelper(rootView.getContext());

        checkBoxHolder.setOrientation(LinearLayoutCompat.VERTICAL);

        String dayLabelString =
                dayOfWeek + " " + TimeFormatting.localDateToString(thisSunday.plusDays(dayNumber));
        dayLabel.setText(dayLabelString);

        for (Medication medication : meds)
        {
            for (LocalDateTime time : medication.getTimes())
            {
                if (time.toLocalDate().isEqual(thisSunday.plusDays(dayNumber)) && !time.isBefore(medication.getStartDate()))
                {
                    CheckBox thisMedication = new CheckBox(rootView.getContext());
                    long medId = medication.getMedId();
                    Pair<Long, LocalDateTime> tag;

                    // Set Checkbox label
                    String medName = medication.getMedName();
                    String dosage =
                            medication.getMedDosage() + " " + medication.getMedDosageUnits();
                    String dosageTime =
                            TimeFormatting.formatTimeForUser(time.getHour(), time.getMinute());

                    String thisMedicationLabel = medName + " - " + dosage + " - " + dosageTime;
                    thisMedication.setText(thisMedicationLabel);

                    // Check database for this dosage, if not add it
                    // if it is, get the DoseId
                    long rowid = db.getDoseId(medId, TimeFormatting.localDateTimeToString(time));

                    tag = Pair.create(rowid, time);

                    thisMedication.setTag(tag);

                    if (rowid != -1 && db.getTaken(rowid))
                        thisMedication.setChecked(true);

                    thisMedication.setOnCheckedChangeListener((compoundButton, b) ->
                    {
                        Pair<Long, LocalDateTime> tvTag =
                                (Pair<Long, LocalDateTime>) thisMedication.getTag();
                        final Long doseId = tvTag.first;
                        int timeBeforeDose = db.getTimeBeforeDose();

                        if (
                                LocalDateTime.now().isBefore(time.minusHours(timeBeforeDose))
                                && timeBeforeDose != -1
                        )
                        {
                            thisMedication.setChecked(false);
                            Toast.makeText(
                                    rootView.getContext(),
                                    "Cannot take medications more than "
                                            + timeBeforeDose + " hours in advance",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }


                        String now = TimeFormatting.localDateTimeToString(LocalDateTime.now());

                        if (doseId != -1)
                        {
                            db.updateDoseStatus(doseId, now, thisMedication.isChecked());
                        }
                        else
                        {
                            long id = db.addToMedicationTracker(
                                    medication,
                                    tvTag.second
                            );

                            db.updateDoseStatus(
                                    id,
                                    TimeFormatting.localDateTimeToString(LocalDateTime.now()),
                                    true
                            );
                        }
                    });

                    checkBoxHolder.addView(thisMedication);
                }
            }
        }

        if (checkBoxHolder.getChildCount() == 0)
        {
            TextView textView = new TextView(rootView.getContext());
            String noMed = "No medications for " + dayOfWeek;

            TextViewUtils.setTextViewParams(textView, noMed, checkBoxHolder);
        }
        else
        {
            sortMedicationCheckBoxes(checkBoxHolder);
        }
    }

    /**
     * Sorts CheckBoxes in medication schedule.
     * @param parentLayout Layout containing CheckBoxes to sort.
     */
    private void sortMedicationCheckBoxes(LinearLayoutCompat parentLayout)
    {
        int count = parentLayout.getChildCount();

        for (int i = 1; i < count; i++)
        {
            for (int j = 0; j < (count - i); j++)
            {
                CheckBox child1 = (CheckBox) parentLayout.getChildAt(j);
                CheckBox child2 = (CheckBox) parentLayout.getChildAt(j + 1);

                Pair<Long, LocalDateTime> child1Pair = (Pair<Long, LocalDateTime>) child1.getTag();
                Pair<Long, LocalDateTime> child2Pair = (Pair<Long, LocalDateTime>) child2.getTag();

                LocalDateTime child1Time = child1Pair.second;
                LocalDateTime child2Time = child2Pair.second;

                if (child1Time != null && child1Time.isAfter(child2Time))
                {
                    CheckBox temp = new CheckBox(parentLayout.getContext());
                    temp.setText(child1.getText());
                    temp.setTag(child1.getTag());
                    temp.setChecked(child1.isChecked());

                    child1.setText(child2.getText());
                    child1.setTag(child2.getTag());
                    child1.setChecked(child2.isChecked());

                    child2.setText(temp.getText());
                    child2.setTag(temp.getTag());
                    child2.setChecked(temp.isChecked());
                }
            }
        }
    }
}