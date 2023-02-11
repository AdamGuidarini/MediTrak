package projects.medicationtracker.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

import kotlin.Triple;
import projects.medicationtracker.Dialogs.AddAsNeededDoseDialog;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        LocalDate thisDate;

        assert getArguments() != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            meds = getArguments().getParcelableArrayList(MEDICATIONS, Medication.class);
        }
        else
        {
            meds = getArguments().getParcelableArrayList(MEDICATIONS);
        }

        dayOfWeek = getArguments().getString(DAY_OF_WEEK + "_" + container.getId());
        dayInCurrentWeek = LocalDate.ofEpochDay(getArguments().getLong(DAY_IN_CURRENT_WEEK + "_" + container.getId()));
        dayNumber = getArguments().getInt(DAY_NUMBER + "_" + container.getId());

        thisDate = TimeFormatting.whenIsSunday(dayInCurrentWeek).plusDays(dayNumber);

        rootView = inflater.inflate(
                R.layout.fragment_medication_schedule,
                container,
                false
        );

        if (meds.stream().anyMatch(m -> m.isActive() && m.getFrequency() == 0 && (!m.getStartDate().toLocalDate().isAfter(thisDate))))
        {
            LinearLayout plusAsNeeded = rootView.findViewById(R.id.plusAsNeeded);

            plusAsNeeded.setTag(thisDate);

            plusAsNeeded.setVisibility(View.VISIBLE);
            plusAsNeeded.setOnClickListener(v ->
            {
                AddAsNeededDoseDialog asNeededDialog = new AddAsNeededDoseDialog(
                        meds.stream().filter(m -> m.getFrequency() == 0 && m.isActive()).collect(Collectors.toCollection(ArrayList::new)),
                        (LocalDate) v.getTag(),
                        db
                );
                asNeededDialog.show(getParentFragmentManager(), null);
            });
        }

        createSchedule(rootView);

        return rootView;
    }

    /**
     * Creates a list of the medications for the current given day in places them in the fragment
     * @param rootView The main view of the fragment
     */
    private void createSchedule(View rootView)
    {
        LinearLayout checkBoxHolder = rootView.findViewById(R.id.medicationSchedule);
        TextView dayLabel = rootView.findViewById(R.id.dateLabel);
        LocalDate thisSunday = TimeFormatting.whenIsSunday(dayInCurrentWeek);
        ArrayList<RelativeLayout> layouts = new ArrayList<>();
        db = new DBHelper(rootView.getContext());

        checkBoxHolder.setOrientation(LinearLayout.VERTICAL);

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
            String noMed = getString(R.string.no_meds_for_day, dayOfWeek);

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
        long medId = medication.getId();
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
        String medName = medication.getName();
        String dosage;
        if (medication.getDosage() == (int) medication.getDosage())
        {
            dosage = String.format(Locale.getDefault(), "%d", (int) medication.getDosage());
        }
        else
        {
            dosage = String.valueOf(medication.getDosage());
        }

        if (doseRowId != -1 && db.getTaken(doseRowId)) thisMedication.setChecked(true);

        dosage += " " + medication.getDosageUnits();

        String dosageTime = TimeFormatting.formatTimeForUser(time.getHour(), time.getMinute());

        String thisMedicationLabel = medName + " - " + dosage + " - " + (medication.getFrequency() > 0 ? dosageTime : getString(R.string.as_needed));

        thisMedication.setText(thisMedicationLabel);

        tag = new Triple<>(medication, doseRowId, time);

        thisMedication.setTag(tag);

        thisMedication.setOnCheckedChangeListener((compoundButton, b) ->
        {
            Triple<Medication, Long, LocalDateTime> tvTag =
                    (Triple<Medication, Long, LocalDateTime>) thisMedication.getTag();
            final Long doseId = tvTag.getSecond();
            int timeBeforeDose = db.getTimeBeforeDose();

            if (LocalDateTime.now().isBefore(time.minusHours(timeBeforeDose)) && timeBeforeDose != -1)
            {
                thisMedication.setChecked(false);
                Toast.makeText(
                        rootView.getContext(),
                        getString(R.string.cannot_take_more_than_hours, timeBeforeDose),
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