package projects.medicationtracker.Fragments;

import static projects.medicationtracker.Fragments.AddEditFormFragment.MINUTES_IN_DAY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import projects.medicationtracker.EditMedication;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.MedicationNotes;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Medication;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyMedicationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyMedicationsFragment extends Fragment
{
    TextView name;
    TextView dosage;
    TextView alias;
    TextView frequency;
    TextView takenSince;
    Button notesButton;
    Button editButton;

    public MyMedicationsFragment()
    {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MyMedicationsFragment.
     */
    public static MyMedicationsFragment newInstance()
    {
        return new MyMedicationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        long medId = requireArguments().getLong("MedId");

        final View rootView = inflater.inflate(R.layout.fragment_my_medications, container, false);

        insertMedicationData(medId, rootView);

        return rootView;
    }

    private void insertMedicationData(long medId, View v)
    {
        DBHelper db = new DBHelper(getContext());
        Medication medication = db.getMedication(medId);
        LocalTime[] times = db.getMedicationTimes(medId);
        LocalDateTime[] dateTimes = new LocalDateTime[times.length];
        String dosageVal;

        name = v.findViewById(R.id.myMedCardMedicationName);
        dosage = v.findViewById(R.id.myMedCardDosage);
        alias = v.findViewById(R.id.myMedCardAlias);
        frequency = v.findViewById(R.id.myMedCardFrequency);
        takenSince = v.findViewById(R.id.myMedCardTakenSince);
        notesButton = v.findViewById(R.id.myMedsNotes);
        editButton = v.findViewById(R.id.myMedsEdit);

        for (int i = 0; i < times.length; i++)
        {
            dateTimes[i] = LocalDateTime.of(medication.getStartDate().toLocalDate(), times[i]);
        }

        medication.setTimes(dateTimes);

        if (medication.getDosage() == (int) medication.getDosage())
        {
            dosageVal = String.format(Locale.getDefault(), "%d", (int) medication.getDosage());
        }
        else
        {
            dosageVal = String.valueOf(medication.getDosage());
        }

        name.setText(getString(R.string.med_name, medication.getName()));
        dosage.setText(getString(R.string.dosage, dosageVal, medication.getDosageUnits()));

        StringBuilder freqLabel;

        if (medication.getFrequency() == MINUTES_IN_DAY && (medication.getTimes().length == 1))
        {
            String time = TimeFormatting.localTimeToString(medication.getTimes()[0].toLocalTime());
            freqLabel = new StringBuilder(getString(R.string.taken_daily_at) + " " + time);
        }
        else if (medication.getFrequency() == MINUTES_IN_DAY && (medication.getTimes().length > 1))
        {
            freqLabel = new StringBuilder(getString(R.string.taken_daily_at));

            for (int i = 0; i < medication.getTimes().length; i++)
            {
                LocalTime time = medication.getTimes()[i].toLocalTime();
                freqLabel.append(TimeFormatting.localTimeToString(time));

                if (i != (medication.getTimes().length - 1))
                    freqLabel.append(", ");
            }
        }
        else if (medication.getFrequency() == 0)
        {
            freqLabel = new StringBuilder(getString(R.string.taken_as_needed));
        }
        else
        {
            freqLabel = new StringBuilder(getString(R.string.taken_every_lbl) + TimeFormatting.freqConversion(medication.getFrequency()));
        }

        frequency.setText(freqLabel);

        if (!medication.getAlias().equals(""))
        {
            alias.setVisibility(View.VISIBLE);
            alias.setText(getString(R.string.alias_lbl, medication.getAlias()));
        }

        takenSince.setText(getString(R.string.taken_since,TimeFormatting.localDateToString(medication.getStartDate().toLocalDate())));

        Intent notesIntent = new Intent(getActivity(), MedicationNotes.class);
        notesIntent.putExtra("medId", medication.getId());

        notesButton.setOnClickListener(view ->
        {
            getActivity().finish();
            getActivity().startActivity(notesIntent);
        });

        Intent editMedIntent = new Intent(getActivity(), EditMedication.class);
        editMedIntent.putExtra("medId", medication.getId());

        editButton.setOnClickListener(view ->
        {
            getActivity().finish();
            getActivity().startActivity(editMedIntent);
        });
    }
}
