package projects.medicationtracker.Fragments;

import static projects.medicationtracker.Fragments.AddEditFormFragment.MINUTES_IN_DAY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
    // TODO: Rename and change types and number of parameters
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

    @SuppressLint("SetTextI18n")
    private void insertMedicationData(long medId, View v)
    {
        DBHelper db = new DBHelper(getContext());
        Medication medication = db.getMedication(medId);
        LocalTime[] times = db.getMedicationTimes(medId);
        LocalDateTime[] dateTimes = new LocalDateTime[times.length];

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

        name.setText("Medication name: " + medication.getMedName());
        dosage.setText("Dosage: " + medication.getMedDosage() + " " + medication.getMedDosageUnits());

        StringBuilder freqLabel;

        if (medication.getMedFrequency() == MINUTES_IN_DAY && (medication.getTimes().length == 1))
        {
            String time = TimeFormatting.localTimeToString(medication.getTimes()[0].toLocalTime());
            freqLabel = new StringBuilder("Taken daily at: " + time);
        }
        else if (medication.getMedFrequency() == MINUTES_IN_DAY && (medication.getTimes().length > 1))
        {
            freqLabel = new StringBuilder("Taken daily at: ");

            for (int i = 0; i < medication.getTimes().length; i++)
            {
                LocalTime time = medication.getTimes()[i].toLocalTime();
                freqLabel.append(TimeFormatting.localTimeToString(time));

                if (i != (medication.getTimes().length - 1))
                    freqLabel.append(", ");
            }
        }
        else
            freqLabel = new StringBuilder("Taken every: " + TimeFormatting.freqConversion(medication.getMedFrequency()));

        frequency.setText(freqLabel);

        if (!medication.getAlias().equals(""))
        {
            alias.setVisibility(View.VISIBLE);
            alias.setText("Alias: " + medication.getAlias());
        }

        takenSince.setText("Taken Since: " + TimeFormatting.localDateToString(medication.getStartDate().toLocalDate()));

        Intent notesIntent = new Intent(getActivity(), MedicationNotes.class);
        notesIntent.putExtra("medId", medication.getMedId());

        notesButton.setOnClickListener(view ->
        {
            getActivity().finish();
            getActivity().startActivity(notesIntent);
        });

        Intent editMedIntent = new Intent(getActivity(), EditMedication.class);
        editMedIntent.putExtra("medId", medication.getMedId());

        editButton.setOnClickListener(view ->
        {
            getActivity().finish();
            getActivity().startActivity(editMedIntent);
        });
    }
}
