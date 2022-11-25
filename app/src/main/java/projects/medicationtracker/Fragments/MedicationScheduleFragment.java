package projects.medicationtracker.Fragments;

import android.os.Bundle;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;

import kotlin.Triple;
import projects.medicationtracker.Dialogs.DoseInfoDialog;
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
    public static final String MEDICATIONS = "medications";
    public static final String DAY_OF_WEEK = "dayOfWeek";
    public static final String DAY_IN_CURRENT_WEEK = "dayInCurrentWeek";
    public static final String DAY_NUMBER = "dayNumber";
    private View rootView;

    private static ArrayList<Medication> meds;
    private static LinearLayoutCompat checkBoxHolder;
    private static DBHelper db;
    private static String dayOfWeek;
    private static LocalDate dayInCurrentWeek;
    private static int dayNumber;

    /**
     * Required empty constructor
     */
    public MedicationScheduleFragment() {}

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
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

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
        checkBoxHolder = rootView.findViewById(R.id.medicationSchedule);
        TextView dayLabel = rootView.findViewById(R.id.dateLabel);
        LocalDate thisSunday = TimeFormatting.whenIsSunday(dayInCurrentWeek);
        ArrayList<RelativeLayout> layouts = new ArrayList<>();
        db = new DBHelper(rootView.getContext());

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
                    layouts.add(buildCheckbox(medication, time));
                }
            }
        }

        if (layouts.size() == 0)
        {
            TextView textView = new TextView(rootView.getContext());
            String noMed = "No medications for " + dayOfWeek;

            TextViewUtils.setTextViewParams(textView, noMed, checkBoxHolder);
        }
        else
        {
            sortSchedule(layouts);

            for (RelativeLayout layout : layouts)
            {
                checkBoxHolder.addView(layout);
            }
        }
    }

    private RelativeLayout buildCheckbox(Medication medication, LocalDateTime time)
    {
        RelativeLayout rl = new RelativeLayout(rootView.getContext());
        CheckBox thisMedication = new CheckBox(rootView.getContext());
        long medId = medication.getMedId();
        Triple<Medication, Long, LocalDateTime> tag;
        long doseRowId = db.getDoseId(medId, TimeFormatting.localDateTimeToString(time));
        ImageButton button = new ImageButton(rootView.getContext());

        button.setBackgroundResource(android.R.drawable.ic_menu_info_details);

        rl.addView(thisMedication);
        rl.addView(button);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );

        button.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        params.addRule(RelativeLayout.CENTER_VERTICAL);

        button.setOnClickListener(v ->
        {
            DoseInfoDialog doseInfo = new DoseInfoDialog(
                    db.getDoseId(medId, TimeFormatting.localDateTimeToString(time)), db
            );
            doseInfo.show(getChildFragmentManager(), null);
        });

        // Set Checkbox label
        String medName = medication.getMedName();
        String dosage;
        if (medication.getMedDosage() == (int) medication.getMedDosage())
        {
            dosage = String.format(Locale.getDefault(), "%d", (int) medication.getMedDosage());
        }
        else
        {
            dosage = String.valueOf(medication.getMedDosage());
        }

        if (doseRowId != -1 && db.getTaken(doseRowId)) thisMedication.setChecked(true);

        dosage += " " + medication.getMedDosageUnits();

        String dosageTime =
                TimeFormatting.formatTimeForUser(time.getHour(), time.getMinute());

        String thisMedicationLabel = medName + " - " + dosage + " - " + dosageTime;

        thisMedication.setText(thisMedicationLabel);

        tag = new Triple<>(medication, doseRowId, time);

        thisMedication.setTag(tag);

        thisMedication.setOnCheckedChangeListener((compoundButton, b) ->
        {
            Triple<Medication, Long, LocalDateTime> tvTag =
                    (Triple<Medication, Long, LocalDateTime>) thisMedication.getTag();
            final Long doseId = tvTag.getSecond();
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
                        tvTag.getFirst(),
                        tvTag.getThird()
                );

                db.updateDoseStatus(
                        id,
                        TimeFormatting.localDateTimeToString(LocalDateTime.now()),
                        true
                );
            }
        });

        return rl;
    }

    /**
     * Sorts CheckBoxes in medication schedule.
     * @param layouts ArrayList of all relative layout in the schedule.
     */
    private void sortSchedule(ArrayList<RelativeLayout> layouts)
    {
        int count = layouts.size();

        for (int i = 1; i < count; i++)
        {
            for (int j = 0; j < (count - i); j++)
            {
                CheckBox child1 = (CheckBox) layouts.get(j).getChildAt(0);
                CheckBox child2 = (CheckBox) layouts.get(j + 1).getChildAt(0);

                Triple<Medication, Long, LocalDateTime> child1Pair =
                        (Triple<Medication, Long, LocalDateTime>) child1.getTag();
                Triple<Medication, Long, LocalDateTime> child2Pair =
                        (Triple<Medication, Long, LocalDateTime>) child2.getTag();

                LocalDateTime child1Time = child1Pair.getThird();
                LocalDateTime child2Time = child2Pair.getThird();

                if (child1Time != null && child1Time.isAfter(child2Time))
                {
                    RelativeLayout temp = layouts.get(j);

                    layouts.set(j, layouts.get(j + 1));
                    layouts.set(j + 1, temp);
                }
            }
        }
    }
}