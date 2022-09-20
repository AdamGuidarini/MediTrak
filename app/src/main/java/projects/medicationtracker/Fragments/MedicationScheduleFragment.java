package projects.medicationtracker.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.ArrayList;

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
    private static final String DATE_LABEL = "dateLabel";
    private View rootView;

    private static ArrayList<Medication> meds;

    public MedicationScheduleFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param medications Medications to display in schedule.
     * @return A new instance of fragment MedicationScheduleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MedicationScheduleFragment newInstance(
            ArrayList<Medication> medications,
            String dateLabel
    )
    {
        MedicationScheduleFragment fragment = new MedicationScheduleFragment();
        Bundle args = new Bundle();

        meds = medications;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            meds = (ArrayList<Medication>) getArguments().getSerializable(MEDICATIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_medication_schedule, container, false);

        // Create med card here

        return rootView;
    }
}