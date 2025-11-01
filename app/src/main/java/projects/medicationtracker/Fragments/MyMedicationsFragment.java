package projects.medicationtracker.Fragments;

import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;
import static projects.medicationtracker.MainActivity.preferences;
import static projects.medicationtracker.MediTrak.formatter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import projects.medicationtracker.AddMedication;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.MedicationHistory;
import projects.medicationtracker.MedicationNotes;
import projects.medicationtracker.R;
import projects.medicationtracker.Models.Medication;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyMedicationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyMedicationsFragment extends Fragment {
    TextView name;
    TextView dosage;
    TextView doseUnit;
    TextView alias;
    TextView frequency;
    TextView remainingDose;
    TextView takenSince;
    TextView endDate;
    TextView instructions;
    Button notesButton;
    Button editButton;
    Button historyButton;

    public MyMedicationsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MyMedicationsFragment.
     */
    public static MyMedicationsFragment newInstance() {
        return new MyMedicationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Medication med = requireArguments().getParcelable("MediTrakCore/Medication");

        final View rootView = inflater.inflate(R.layout.fragment_my_medications, container, false);

        insertMedicationData(med, rootView);

        return rootView;
    }

    private void insertMedicationData(Medication medication, View v) {
        DBHelper db = new DBHelper(getContext());
        LocalTime[] times = db.getMedicationTimes(medication.getId());
        LocalDateTime[] dateTimes = new LocalDateTime[times.length];
        LinearLayout barrier = v.findViewById(R.id.barrier);
        LinearLayout barrier1 = v.findViewById(R.id.barrier1);
        LinearLayout barrier2 = v.findViewById(R.id.barrier2);
        LinearLayout barrier3 = v.findViewById(R.id.barrier3);
        LinearLayout barrier4 = v.findViewById(R.id.barrier4);


        name = v.findViewById(R.id.myMedCardMedicationName);
        dosage = v.findViewById(R.id.dosage_amount);
        doseUnit = v.findViewById(R.id.dosage_unit);
        alias = v.findViewById(R.id.myMedCardAlias);
        frequency = v.findViewById(R.id.myMedCardFrequency);
        remainingDose = v.findViewById(R.id.remainingDoses);
        takenSince = v.findViewById(R.id.myMedCardTakenSince);
        endDate = v.findViewById(R.id.endDate);
        instructions = v.findViewById(R.id.instructions);
        notesButton = v.findViewById(R.id.myMedsNotes);
        editButton = v.findViewById(R.id.myMedsEdit);
        historyButton = v.findViewById(R.id.history_button);

        for (int i = 0; i < times.length; i++) {
            dateTimes[i] = LocalDateTime.of(medication.getStartDate().toLocalDate(), times[i]);
        }

        medication.setTimes(dateTimes);

        name.setText(medication.getName());
        dosage.setText(formatter.format(medication.getDosage()));
        doseUnit.setText(medication.getDosageUnits());

        String label = medication.generateFrequencyLabel(
                getContext(),
                preferences.getString(DATE_FORMAT),
                preferences.getString(TIME_FORMAT)
        );

        frequency.setText(label);

        String doseLimit = medication.getDoseAmount() > -1  ?
                String.valueOf(medication.getDoseAmount()) : "N/A";

        remainingDose.setText(doseLimit);

        alias.setText(medication.getAlias().isEmpty() ? "N/A" : medication.getAlias());

        LocalDateTime start = medication.getParent() == null ? medication.getStartDate() : medication.getParent().getStartDate();

        String beginning = DateTimeFormatter.ofPattern(
                preferences.getString(DATE_FORMAT),
                Locale.getDefault()
        ).format(start);

        takenSince.setText(beginning);

        LocalDateTime end = medication.getEndDate();

        if (end == null || (end != null && end.toLocalDate().isEqual(LocalDate.of(9999, 12,31)))) {
            endDate.setText("N/A");
        } else {
            String endSt = DateTimeFormatter.ofPattern(
                    preferences.getString(DATE_FORMAT),
                    Locale.getDefault()
            ).format(end);

            endDate.setText(endSt);
        }

        if (medication.getInstructions() == null) {
            instructions.setText("N/A");
        } else {
            instructions.setText(medication.getInstructions().isEmpty() ? "N/A" : medication.getInstructions());
        }

        Intent notesIntent = new Intent(getActivity(), MedicationNotes.class);
        notesIntent.putExtra("medId", medication.getId());

        notesButton.setOnClickListener(view ->
        {
            getActivity().finish();
            getActivity().startActivity(notesIntent);
        });

        Intent editMedIntent = new Intent(getActivity(), AddMedication.class);
        editMedIntent.putExtra("medId", medication.getId());

        editButton.setOnClickListener(view ->
        {
            getActivity().finish();
            getActivity().startActivity(editMedIntent);
        });

        Intent intent = new Intent(getActivity(), MedicationHistory.class);

        historyButton.setOnClickListener(view -> {
            intent.putExtra("ID", medication.getId());
            getActivity().finish();
            startActivity(intent);
        });

        barrier.setBackgroundColor(name.getCurrentTextColor());
        barrier1.setBackgroundColor(name.getCurrentTextColor());
        barrier2.setBackgroundColor(name.getCurrentTextColor());
        barrier3.setBackgroundColor(name.getCurrentTextColor());
        barrier4.setBackgroundColor(name.getCurrentTextColor());
    }
}
